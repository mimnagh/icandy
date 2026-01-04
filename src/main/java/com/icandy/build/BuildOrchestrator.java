package com.icandy.build;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * BuildOrchestrator coordinates the build phase workflow.
 * It orchestrates TextParser, ImageDownloader, and AssociationManager to:
 * 1. Parse text into phrases and words
 * 2. Filter stop words
 * 3. Download images for each content word
 * 4. Save associations to disk
 */
public class BuildOrchestrator {
    
    private final TextParser textParser;
    private final ImageDownloader imageDownloader;
    private final AssociationManager associationManager;
    
    // Configuration
    private int imagesPerWord;
    private String imageStorageDir;
    private String associationsFile;
    private int maxRetries;
    
    // Progress tracking
    private int totalWords;
    private int processedWords;
    private int failedWords;
    private List<String> failedWordsList;
    
    /**
     * Creates a BuildOrchestrator with default components.
     */
    public BuildOrchestrator() {
        this.textParser = new TextParser();
        this.imageDownloader = new ImageDownloader();
        this.associationManager = new AssociationManager();
        this.failedWordsList = new ArrayList<>();
        
        // Default configuration
        this.imagesPerWord = 5;
        this.imageStorageDir = "data/images";
        this.associationsFile = "data/associations.json";
        this.maxRetries = 3;
    }
    
    /**
     * Creates a BuildOrchestrator with custom components (for testing).
     */
    public BuildOrchestrator(TextParser textParser, ImageDownloader imageDownloader, 
                            AssociationManager associationManager) {
        this.textParser = textParser;
        this.imageDownloader = imageDownloader;
        this.associationManager = associationManager;
        this.failedWordsList = new ArrayList<>();
        
        // Default configuration
        this.imagesPerWord = 5;
        this.imageStorageDir = "data/images";
        this.associationsFile = "data/associations.json";
        this.maxRetries = 3;
    }
    
    /**
     * Loads configuration from a JSON file.
     * 
     * @param configFilePath Path to the configuration file
     * @throws IOException if the file cannot be read or parsed
     */
    public void loadConfiguration(String configFilePath) throws IOException {
        // Expand ~ to user home directory
        String expandedPath = configFilePath.replaceFirst("^~", System.getProperty("user.home"));
        Path path = Paths.get(expandedPath);
        
        if (!Files.exists(path)) {
            throw new IOException("Configuration file not found: " + expandedPath);
        }
        
        String json = Files.readString(path);
        JsonObject config = JsonParser.parseString(json).getAsJsonObject();
        
        if (!config.has("build")) {
            throw new IOException("Configuration file missing 'build' section");
        }
        
        JsonObject buildConfig = config.getAsJsonObject("build");
        
        // Load build configuration
        if (buildConfig.has("imagesPerWord")) {
            this.imagesPerWord = buildConfig.get("imagesPerWord").getAsInt();
            if (this.imagesPerWord <= 0) {
                System.err.println("Warning: imagesPerWord must be positive, using default: 5");
                this.imagesPerWord = 5;
            }
        }
        
        if (buildConfig.has("imageStorageDir")) {
            this.imageStorageDir = buildConfig.get("imageStorageDir").getAsString();
        }
        
        if (buildConfig.has("associationsFile")) {
            this.associationsFile = buildConfig.get("associationsFile").getAsString();
        }
        
        if (buildConfig.has("maxRetries")) {
            this.maxRetries = buildConfig.get("maxRetries").getAsInt();
            if (this.maxRetries < 0) {
                System.err.println("Warning: maxRetries cannot be negative, using default: 3");
                this.maxRetries = 3;
            }
            imageDownloader.setMaxRetries(this.maxRetries);
        }
        
        // Load stop words file
        if (buildConfig.has("stopWordsFile")) {
            String stopWordsFile = buildConfig.get("stopWordsFile").getAsString();
            try {
                textParser.loadStopWords(stopWordsFile);
                System.out.println("Loaded stop words from: " + stopWordsFile);
            } catch (IOException e) {
                System.err.println("Warning: Could not load stop words file: " + e.getMessage());
                System.err.println("Continuing without stop words filtering.");
            }
        }
        
        // Load Unsplash credentials
        if (buildConfig.has("unsplashPropertiesFile")) {
            String unsplashPropsFile = buildConfig.get("unsplashPropertiesFile").getAsString();
            try {
                imageDownloader.loadCredentials(unsplashPropsFile);
                System.out.println("Loaded Unsplash credentials from: " + unsplashPropsFile);
            } catch (IOException e) {
                throw new IOException("Failed to load Unsplash credentials: " + e.getMessage(), e);
            }
        } else {
            throw new IOException("Configuration missing 'unsplashPropertiesFile'");
        }
    }
    
    /**
     * Runs the complete build workflow.
     * 
     * @param textFilePath Path to the text script file
     * @throws IOException if any file operations fail
     */
    public void runBuild(String textFilePath) throws IOException {
        System.out.println("=== iCandy Build Phase ===");
        System.out.println("Text file: " + textFilePath);
        System.out.println();
        
        // Step 1: Read and validate text file
        System.out.println("[1/5] Reading text file...");
        String textContent = readTextFile(textFilePath);
        System.out.println("Read " + textContent.length() + " characters");
        System.out.println();
        
        // Step 2: Parse text into phrases and words
        System.out.println("[2/5] Parsing text...");
        String[] phrases = textParser.parseIntoPhrases(textContent);
        String[] allWords = textParser.parseIntoWords(textContent);
        String[] contentWords = textParser.filterStopWords(allWords);
        
        System.out.println("Found " + phrases.length + " phrases");
        System.out.println("Found " + allWords.length + " unique words");
        System.out.println("Found " + contentWords.length + " content words (after filtering stop words)");
        System.out.println();
        
        if (contentWords.length == 0) {
            System.err.println("Warning: No content words found after filtering. Nothing to process.");
            return;
        }
        
        // Step 3: Create phrase-to-words mapping
        System.out.println("[3/5] Creating phrase-to-words mapping...");
        Map<Integer, String[]> phraseToWords = textParser.mapPhrasesToWords(phrases);
        System.out.println("Created mappings for " + phraseToWords.size() + " phrases");
        System.out.println();
        
        // Step 4: Download images for each content word
        System.out.println("[4/5] Downloading images...");
        System.out.println("Processing " + contentWords.length + " content words (" + imagesPerWord + " images per word)");
        System.out.println();
        
        totalWords = contentWords.length;
        processedWords = 0;
        failedWords = 0;
        failedWordsList.clear();
        
        for (String word : contentWords) {
            processWord(word);
        }
        
        System.out.println();
        System.out.println("Download complete:");
        System.out.println("  - Successfully processed: " + (processedWords - failedWords) + " words");
        System.out.println("  - Failed: " + failedWords + " words");
        
        if (failedWords > 0) {
            System.out.println("  - Failed words: " + String.join(", ", failedWordsList));
        }
        System.out.println();
        
        // Step 5: Save associations to file
        System.out.println("[5/5] Saving associations...");
        associationManager.saveToFile(associationsFile);
        System.out.println("Saved associations to: " + associationsFile);
        System.out.println("Total words with images: " + associationManager.getWordCount());
        System.out.println("Total images: " + associationManager.getImageCount());
        System.out.println();
        
        System.out.println("=== Build Complete ===");
    }
    
    /**
     * Reads a text file and returns its content.
     */
    private String readTextFile(String textFilePath) throws IOException {
        Path path = Paths.get(textFilePath);
        
        if (!Files.exists(path)) {
            throw new IOException("Text file not found: " + textFilePath);
        }
        
        if (!Files.isRegularFile(path)) {
            throw new IOException("Path is not a file: " + textFilePath);
        }
        
        String content = Files.readString(path);
        
        if (content.trim().isEmpty()) {
            throw new IOException("Text file is empty: " + textFilePath);
        }
        
        return content;
    }
    
    /**
     * Processes a single word: searches for images and downloads them.
     * Handles failures gracefully and continues processing.
     */
    private void processWord(String word) {
        processedWords++;
        
        System.out.print("[" + processedWords + "/" + totalWords + "] Processing '" + word + "'... ");
        
        try {
            // Search for images
            String[] imageUrls = imageDownloader.searchImages(word, imagesPerWord);
            
            if (imageUrls.length == 0) {
                System.out.println("No images found");
                failedWords++;
                failedWordsList.add(word);
                return;
            }
            
            // Download each image
            List<String> downloadedPaths = new ArrayList<>();
            int successCount = 0;
            
            for (int i = 0; i < imageUrls.length; i++) {
                String imageUrl = imageUrls[i];
                String filename = sanitizeFilename(word) + "_" + (i + 1) + ".jpg";
                String localPath = imageStorageDir + "/" + filename;
                
                boolean success = imageDownloader.downloadImage(imageUrl, localPath);
                
                if (success) {
                    downloadedPaths.add(localPath);
                    successCount++;
                }
            }
            
            if (downloadedPaths.isEmpty()) {
                System.out.println("Failed to download any images");
                failedWords++;
                failedWordsList.add(word);
            } else {
                // Add association
                associationManager.addAssociation(word, downloadedPaths.toArray(new String[0]));
                System.out.println("Downloaded " + successCount + "/" + imageUrls.length + " images");
            }
            
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            failedWords++;
            failedWordsList.add(word);
        }
    }
    
    /**
     * Sanitizes a word to create a valid filename.
     * Removes special characters and replaces spaces with underscores.
     */
    private String sanitizeFilename(String word) {
        return word.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
    }
    
    /**
     * Gets the number of words that failed to process.
     * 
     * @return Number of failed words
     */
    public int getFailedWordCount() {
        return failedWords;
    }
    
    /**
     * Gets the list of words that failed to process.
     * 
     * @return List of failed words
     */
    public List<String> getFailedWords() {
        return new ArrayList<>(failedWordsList);
    }
    
    /**
     * Gets the total number of words processed.
     * 
     * @return Number of processed words
     */
    public int getProcessedWordCount() {
        return processedWords;
    }
}
