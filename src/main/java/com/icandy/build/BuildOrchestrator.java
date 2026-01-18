package com.icandy.build;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.icandy.common.Logger;

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
    private final Logger logger;
    
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
    private long buildStartTime;
    
    /**
     * Creates a BuildOrchestrator with default components.
     */
    public BuildOrchestrator() {
        this.textParser = new TextParser();
        this.imageDownloader = new ImageDownloader();
        this.associationManager = new AssociationManager();
        this.logger = new Logger(BuildOrchestrator.class);
        this.failedWordsList = new ArrayList<>();
        
        // Default configuration
        this.imagesPerWord = 5;
        this.imageStorageDir = "data/images";
        this.associationsFile = "data/associations.json";
        this.maxRetries = 3;
        
        logger.info("BuildOrchestrator initialized with default configuration");
    }
    
    /**
     * Creates a BuildOrchestrator with custom components (for testing).
     */
    public BuildOrchestrator(TextParser textParser, ImageDownloader imageDownloader, 
                            AssociationManager associationManager) {
        this.textParser = textParser;
        this.imageDownloader = imageDownloader;
        this.associationManager = associationManager;
        this.logger = new Logger(BuildOrchestrator.class);
        this.failedWordsList = new ArrayList<>();
        
        // Default configuration
        this.imagesPerWord = 5;
        this.imageStorageDir = "data/images";
        this.associationsFile = "data/associations.json";
        this.maxRetries = 3;
        
        logger.info("BuildOrchestrator initialized with custom components");
    }
    
    /**
     * Loads configuration from a JSON file.
     * 
     * @param configFilePath Path to the configuration file
     * @throws IOException if the file cannot be read or parsed
     */
    public void loadConfiguration(String configFilePath) throws IOException {
        logger.info("Loading configuration", configFilePath);
        
        try {
            // Expand ~ to user home directory
            String expandedPath = configFilePath.replaceFirst("^~", System.getProperty("user.home"));
            Path path = Paths.get(expandedPath);
            
            if (!Files.exists(path)) {
                IOException e = new IOException("Configuration file not found: " + expandedPath);
                logger.error("Configuration file not found", expandedPath, e);
                throw e;
            }
            
            if (!Files.isReadable(path)) {
                IOException e = new IOException("Configuration file is not readable: " + expandedPath);
                logger.error("Configuration file not readable", expandedPath, e);
                throw e;
            }
            
            String json;
            try {
                json = Files.readString(path);
            } catch (IOException e) {
                logger.error("Failed to read configuration file", expandedPath, e);
                throw new IOException("Failed to read configuration file: " + e.getMessage(), e);
            }
            
            JsonObject config;
            try {
                config = JsonParser.parseString(json).getAsJsonObject();
            } catch (JsonSyntaxException e) {
                logger.error("Invalid JSON in configuration file", expandedPath, e);
                throw new IOException("Invalid JSON in configuration file: " + e.getMessage(), e);
            }
            
            if (!config.has("build")) {
                IOException e = new IOException("Configuration file missing 'build' section");
                logger.error("Configuration validation failed", "Missing 'build' section", e);
                throw e;
            }
            
            JsonObject buildConfig = config.getAsJsonObject("build");
            
            // Load build configuration with validation
            loadBuildConfiguration(buildConfig);
            
            // Load stop words file
            loadStopWordsConfiguration(buildConfig);
            
            // Load Unsplash credentials
            loadUnsplashConfiguration(buildConfig);
            
            logger.info("Configuration loaded successfully", 
                String.format("imagesPerWord=%d, maxRetries=%d", imagesPerWord, maxRetries));
                
        } catch (IOException e) {
            logger.error("Failed to load configuration", configFilePath, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error loading configuration", configFilePath, e);
            throw new IOException("Unexpected error loading configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Loads build-specific configuration parameters.
     */
    private void loadBuildConfiguration(JsonObject buildConfig) {
        // Load images per word
        if (buildConfig.has("imagesPerWord")) {
            try {
                int value = buildConfig.get("imagesPerWord").getAsInt();
                if (value <= 0) {
                    logger.warning("Invalid imagesPerWord value, using default", 
                        String.format("value=%d, default=%d", value, this.imagesPerWord));
                } else {
                    this.imagesPerWord = value;
                }
            } catch (Exception e) {
                logger.warning("Failed to parse imagesPerWord, using default", e.getMessage());
            }
        }
        
        // Load image storage directory
        if (buildConfig.has("imageStorageDir")) {
            try {
                this.imageStorageDir = buildConfig.get("imageStorageDir").getAsString();
            } catch (Exception e) {
                logger.warning("Failed to parse imageStorageDir, using default", e.getMessage());
            }
        }
        
        // Load associations file path
        if (buildConfig.has("associationsFile")) {
            try {
                this.associationsFile = buildConfig.get("associationsFile").getAsString();
            } catch (Exception e) {
                logger.warning("Failed to parse associationsFile, using default", e.getMessage());
            }
        }
        
        // Load max retries
        if (buildConfig.has("maxRetries")) {
            try {
                int value = buildConfig.get("maxRetries").getAsInt();
                if (value < 0) {
                    logger.warning("Invalid maxRetries value, using default", 
                        String.format("value=%d, default=%d", value, this.maxRetries));
                } else {
                    this.maxRetries = value;
                    imageDownloader.setMaxRetries(this.maxRetries);
                }
            } catch (Exception e) {
                logger.warning("Failed to parse maxRetries, using default", e.getMessage());
            }
        }
    }
    
    /**
     * Loads stop words configuration.
     */
    private void loadStopWordsConfiguration(JsonObject buildConfig) {
        if (buildConfig.has("stopWordsFile")) {
            String stopWordsFile = buildConfig.get("stopWordsFile").getAsString();
            try {
                textParser.loadStopWords(stopWordsFile);
                logger.info("Stop words loaded successfully", stopWordsFile);
            } catch (IOException e) {
                logger.warning("Could not load stop words file, continuing without filtering", 
                    stopWordsFile + ": " + e.getMessage());
            } catch (Exception e) {
                logger.warning("Unexpected error loading stop words, continuing without filtering", 
                    stopWordsFile + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Loads Unsplash API configuration.
     */
    private void loadUnsplashConfiguration(JsonObject buildConfig) throws IOException {
        if (buildConfig.has("unsplashPropertiesFile")) {
            String unsplashPropsFile = buildConfig.get("unsplashPropertiesFile").getAsString();
            try {
                imageDownloader.loadCredentials(unsplashPropsFile);
                logger.info("Unsplash credentials loaded successfully", unsplashPropsFile);
            } catch (IOException e) {
                logger.error("Failed to load Unsplash credentials", unsplashPropsFile, e);
                throw new IOException("Failed to load Unsplash credentials: " + e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Unexpected error loading Unsplash credentials", unsplashPropsFile, e);
                throw new IOException("Unexpected error loading Unsplash credentials: " + e.getMessage(), e);
            }
        } else {
            IOException e = new IOException("Configuration missing 'unsplashPropertiesFile'");
            logger.error("Configuration validation failed", "Missing unsplashPropertiesFile", e);
            throw e;
        }
    }
    
    /**
     * Runs the complete build workflow.
     * 
     * @param textFilePath Path to the text script file
     * @throws IOException if any file operations fail
     */
    public void runBuild(String textFilePath) throws IOException {
        buildStartTime = System.currentTimeMillis();
        logger.info("Starting build phase", textFilePath);
        
        try {
            System.out.println("=== iCandy Build Phase ===");
            System.out.println("Text file: " + textFilePath);
            System.out.println();
            
            // Step 1: Read and validate text file
            System.out.println("[1/6] Reading text file...");
            logger.info("Step 1: Reading text file", textFilePath);
            String textContent = readTextFile(textFilePath);
            System.out.println("Read " + textContent.length() + " characters");
            logger.info("Text file read successfully", 
                String.format("file=%s, size=%d chars", textFilePath, textContent.length()));
            System.out.println();
            
            // Step 2: Load existing associations if available
            System.out.println("[2/6] Checking for existing associations...");
            logger.info("Step 2: Loading existing associations");
            loadExistingAssociations();
            
            // Step 3: Parse text into phrases and words
            System.out.println("[3/6] Parsing text...");
            logger.info("Step 3: Parsing text into phrases and words");
            String[] phrases = parseTextSafely(textContent);
            String[] allWords = textParser.parseIntoWords(textContent);
            String[] contentWords = textParser.filterStopWords(allWords);
            
            System.out.println("Found " + phrases.length + " phrases");
            System.out.println("Found " + allWords.length + " unique words");
            System.out.println("Found " + contentWords.length + " content words (after filtering stop words)");
            logger.info("Text parsing completed", 
                String.format("phrases=%d, words=%d, contentWords=%d", 
                    phrases.length, allWords.length, contentWords.length));
            System.out.println();
            
            if (contentWords.length == 0) {
                String message = "No content words found after filtering. Nothing to process.";
                logger.warning(message, "Check stop words configuration or text content");
                System.err.println("Warning: " + message);
                return;
            }
            
            // Step 4: Create phrase-to-words mapping
            System.out.println("[4/6] Creating phrase-to-words mapping...");
            logger.info("Step 4: Creating phrase-to-words mapping");
            Map<Integer, String[]> phraseToWords = createPhraseToWordsMapping(phrases);
            System.out.println("Created mappings for " + phraseToWords.size() + " phrases");
            logger.info("Phrase mapping completed", String.format("mappings=%d", phraseToWords.size()));
            System.out.println();
            
            // Step 5: Download images for words that need them
            System.out.println("[5/6] Downloading images...");
            logger.info("Step 5: Starting image download phase");
            downloadImagesForWords(contentWords);
            
            // Step 6: Save associations to file
            System.out.println("[6/6] Saving associations...");
            logger.info("Step 6: Saving associations to file");
            saveAssociationsSafely();
            System.out.println("Saved associations to: " + associationsFile);
            System.out.println("Total words with images: " + associationManager.getWordCount());
            System.out.println("Total images: " + associationManager.getImageCount());
            System.out.println();
            
            long buildDuration = System.currentTimeMillis() - buildStartTime;
            logger.performance("Complete build phase", buildDuration);
            
            System.out.println("=== Build Complete ===");
            logger.info("Build phase completed successfully", 
                String.format("duration=%dms, words=%d, images=%d", 
                    buildDuration, associationManager.getWordCount(), associationManager.getImageCount()));
            
        } catch (Exception e) {
            long buildDuration = System.currentTimeMillis() - buildStartTime;
            logger.error("Build phase failed", String.format("duration=%dms", buildDuration), e);
            
            // Save whatever associations we have so far before re-throwing
            System.err.println();
            System.err.println("=== Build Failed ===");
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            
            savePartialAssociations();
            
            // Re-throw the exception
            if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                throw new IOException("Build failed: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Safely parses text into phrases with error handling.
     */
    private String[] parseTextSafely(String textContent) {
        try {
            return textParser.parseIntoPhrases(textContent);
        } catch (Exception e) {
            logger.error("Failed to parse text into phrases", "Using fallback parsing", e);
            // Fallback: split by lines
            return textContent.split("\\r?\\n");
        }
    }
    
    /**
     * Safely creates phrase-to-words mapping with error handling.
     */
    private Map<Integer, String[]> createPhraseToWordsMapping(String[] phrases) {
        try {
            return textParser.mapPhrasesToWords(phrases);
        } catch (Exception e) {
            logger.error("Failed to create phrase-to-words mapping", "Using empty mapping", e);
            return new HashMap<>();
        }
    }
    
    /**
     * Downloads images for the specified content words.
     */
    private void downloadImagesForWords(String[] contentWords) {
        // Filter out words that already have sufficient images
        List<String> wordsNeedingImages = new ArrayList<>();
        int skippedWords = 0;
        
        for (String word : contentWords) {
            String[] existingImages = associationManager.getImagesForWord(word);
            if (existingImages.length >= imagesPerWord) {
                skippedWords++;
            } else {
                wordsNeedingImages.add(word);
            }
        }
        
        if (skippedWords > 0) {
            System.out.println("Skipping " + skippedWords + " words that already have sufficient images");
            logger.info("Skipped words with sufficient images", String.format("count=%d", skippedWords));
        }
        
        if (wordsNeedingImages.isEmpty()) {
            System.out.println("All words already have sufficient images. No downloads needed.");
            logger.info("No downloads needed - all words have sufficient images");
        } else {
            System.out.println("Processing " + wordsNeedingImages.size() + " words (" + imagesPerWord + " images per word)");
            logger.info("Starting image downloads", 
                String.format("wordsToProcess=%d, imagesPerWord=%d", wordsNeedingImages.size(), imagesPerWord));
            System.out.println();
            
            totalWords = wordsNeedingImages.size();
            processedWords = 0;
            failedWords = 0;
            failedWordsList.clear();
            
            for (String word : wordsNeedingImages) {
                processWord(word);
            }
            
            System.out.println();
            System.out.println("Download complete:");
            System.out.println("  - Successfully processed: " + (processedWords - failedWords) + " words");
            System.out.println("  - Failed: " + failedWords + " words");
            
            logger.info("Image download phase completed", 
                String.format("successful=%d, failed=%d, total=%d", 
                    processedWords - failedWords, failedWords, processedWords));
            
            if (failedWords > 0) {
                System.out.println("  - Failed words: " + String.join(", ", failedWordsList));
                logger.warning("Some words failed to download images", 
                    String.format("failedWords=%s", String.join(", ", failedWordsList)));
            }
        }
        System.out.println();
    }
    
    /**
     * Safely saves associations with error handling.
     */
    private void saveAssociationsSafely() throws IOException {
        try {
            associationManager.saveToFile(associationsFile);
            logger.fileOperation("save", associationsFile, true);
        } catch (IOException e) {
            logger.fileOperation("save", associationsFile, false);
            logger.error("Failed to save associations", associationsFile, e);
            throw e;
        }
    }
    
    /**
     * Saves partial associations when the build fails.
     * This ensures that successfully downloaded images are not lost.
     */
    private void savePartialAssociations() {
        try {
            if (associationManager.getWordCount() > 0) {
                System.err.println("Saving partial associations (" + 
                    associationManager.getWordCount() + " words, " + 
                    associationManager.getImageCount() + " images)...");
                logger.info("Saving partial associations due to build failure", 
                    String.format("words=%d, images=%d", 
                        associationManager.getWordCount(), associationManager.getImageCount()));
                        
                associationManager.saveToFile(associationsFile);
                System.err.println("Partial associations saved to: " + associationsFile);
                System.err.println("You can resume the build by running it again.");
                logger.info("Partial associations saved successfully", associationsFile);
            } else {
                System.err.println("No associations to save.");
                logger.info("No partial associations to save");
            }
        } catch (IOException saveError) {
            System.err.println("Warning: Failed to save partial associations: " + saveError.getMessage());
            logger.error("Failed to save partial associations", associationsFile, saveError);
        }
        System.err.println();
    }
    
    /**
     * Loads existing associations from file if it exists.
     * This allows incremental builds without re-downloading existing images.
     */
    private void loadExistingAssociations() {
        try {
            Path associationsPath = Paths.get(associationsFile);
            if (Files.exists(associationsPath)) {
                associationManager.loadFromFile(associationsFile);
                System.out.println("Loaded existing associations: " + 
                    associationManager.getWordCount() + " words, " + 
                    associationManager.getImageCount() + " images");
                logger.info("Existing associations loaded successfully", 
                    String.format("file=%s, words=%d, images=%d", 
                        associationsFile, associationManager.getWordCount(), associationManager.getImageCount()));
            } else {
                System.out.println("No existing associations found. Starting fresh.");
                logger.info("No existing associations file found", associationsFile);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load existing associations: " + e.getMessage());
            System.err.println("Starting with empty associations.");
            logger.warning("Failed to load existing associations, starting fresh", 
                associationsFile + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Warning: Unexpected error loading associations: " + e.getMessage());
            System.err.println("Starting with empty associations.");
            logger.error("Unexpected error loading existing associations", associationsFile, e);
        }
        System.out.println();
    }
    
    /**
     * Reads a text file and returns its content.
     */
    private String readTextFile(String textFilePath) throws IOException {
        try {
            Path path = Paths.get(textFilePath);
            
            if (!Files.exists(path)) {
                IOException e = new IOException("Text file not found: " + textFilePath);
                logger.error("Text file not found", textFilePath, e);
                throw e;
            }
            
            if (!Files.isRegularFile(path)) {
                IOException e = new IOException("Path is not a file: " + textFilePath);
                logger.error("Invalid file path", textFilePath, e);
                throw e;
            }
            
            if (!Files.isReadable(path)) {
                IOException e = new IOException("Text file is not readable: " + textFilePath);
                logger.error("File not readable", textFilePath, e);
                throw e;
            }
            
            String content = Files.readString(path);
            
            if (content.trim().isEmpty()) {
                IOException e = new IOException("Text file is empty: " + textFilePath);
                logger.error("Empty text file", textFilePath, e);
                throw e;
            }
            
            logger.fileOperation("read", textFilePath, true);
            return content;
            
        } catch (IOException e) {
            logger.fileOperation("read", textFilePath, false);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error reading text file", textFilePath, e);
            throw new IOException("Unexpected error reading text file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Processes a single word: searches for images and downloads them.
     * Handles failures gracefully and continues processing.
     * Saves associations incrementally after each successful word.
     */
    private void processWord(String word) {
        processedWords++;
        long wordStartTime = System.currentTimeMillis();
        
        System.out.print("[" + processedWords + "/" + totalWords + "] Processing '" + word + "'... ");
        logger.progress(processedWords, totalWords, "Processing word: " + word);
        
        try {
            // Search for images
            String[] imageUrls = searchImagesForWord(word);
            
            if (imageUrls.length == 0) {
                System.out.println("No images found");
                failedWords++;
                failedWordsList.add(word);
                logger.warning("No images found for word", word);
                return;
            }
            
            // Download each image
            List<String> downloadedPaths = downloadImagesForWord(word, imageUrls);
            
            if (downloadedPaths.isEmpty()) {
                System.out.println("Failed to download any images");
                failedWords++;
                failedWordsList.add(word);
                logger.warning("Failed to download any images for word", word);
            } else {
                // Add association
                associationManager.addAssociation(word, downloadedPaths.toArray(new String[0]));
                System.out.println("Downloaded " + downloadedPaths.size() + "/" + imageUrls.length + " images");
                
                long wordDuration = System.currentTimeMillis() - wordStartTime;
                logger.performance("Process word: " + word, wordDuration);
                
                // Save associations incrementally after each successful word
                // This allows safe interruption without losing progress
                saveAssociationsIncrementally();
            }
            
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            failedWords++;
            failedWordsList.add(word);
            logger.error("Failed to process word", word, e);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            failedWords++;
            failedWordsList.add(word);
            logger.error("Unexpected error processing word", word, e);
        }
    }
    
    /**
     * Searches for images for a specific word with error handling.
     */
    private String[] searchImagesForWord(String word) throws IOException {
        try {
            return imageDownloader.searchImages(word, imagesPerWord);
        } catch (IOException e) {
            logger.error("Image search failed for word", word, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during image search", word, e);
            throw new IOException("Unexpected error during image search: " + e.getMessage(), e);
        }
    }
    
    /**
     * Downloads images for a specific word with error handling.
     */
    private List<String> downloadImagesForWord(String word, String[] imageUrls) {
        List<String> downloadedPaths = new ArrayList<>();
        int successCount = 0;
        
        for (int i = 0; i < imageUrls.length; i++) {
            String imageUrl = imageUrls[i];
            String filename = sanitizeFilename(word) + "_" + (i + 1) + ".jpg";
            String localPath = imageStorageDir + "/" + filename;
            
            try {
                boolean success = imageDownloader.downloadImage(imageUrl, localPath);
                
                if (success) {
                    downloadedPaths.add(localPath);
                    successCount++;
                    logger.info("Image downloaded successfully", 
                        String.format("word=%s, file=%s", word, filename));
                } else {
                    logger.warning("Image download failed", 
                        String.format("word=%s, url=%s", word, imageUrl));
                }
            } catch (Exception e) {
                logger.error("Exception during image download", 
                    String.format("word=%s, url=%s", word, imageUrl), e);
            }
        }
        
        return downloadedPaths;
    }
    
    /**
     * Saves associations incrementally with error handling.
     */
    private void saveAssociationsIncrementally() {
        try {
            associationManager.saveToFile(associationsFile);
        } catch (IOException saveError) {
            logger.warning("Failed to save associations incrementally", 
                associationsFile + ": " + saveError.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error saving associations incrementally", associationsFile, e);
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
