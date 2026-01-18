package com.icandy.build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.icandy.common.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

/**
 * AssociationManager manages the mapping between words and their associated images.
 * It handles storing, retrieving, and persisting word-to-image associations.
 */
public class AssociationManager {
    
    private Map<String, List<String>> associations;
    private final Logger logger;
    private Gson gson;
    
    /**
     * Creates an AssociationManager with an empty associations map.
     */
    public AssociationManager() {
        this.associations = new HashMap<>();
        this.logger = new Logger(AssociationManager.class);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        logger.info("AssociationManager initialized");
    }
    
    /**
     * Adds an association between a word and its image paths.
     * If the word already has associations, the new paths are added to the existing list.
     * 
     * @param word The word to associate with images
     * @param imagePaths Array of image file paths to associate with the word
     */
    public void addAssociation(String word, String[] imagePaths) {
        if (word == null || word.trim().isEmpty()) {
            logger.warning("Empty or null word provided for association");
            return;
        }
        
        if (imagePaths == null || imagePaths.length == 0) {
            logger.warning("Empty or null image paths provided for word", word);
            return;
        }
        
        try {
            String normalizedWord = word.toLowerCase().trim();
            
            // Get existing list or create new one
            List<String> imageList = associations.getOrDefault(normalizedWord, new ArrayList<>());
            
            // Track if we added any valid paths
            int initialSize = imageList.size();
            
            // Add new image paths
            for (String imagePath : imagePaths) {
                if (imagePath != null && !imagePath.trim().isEmpty()) {
                    imageList.add(imagePath.trim());
                }
            }
            
            // Only add to map if we actually added at least one valid image path
            if (imageList.size() > initialSize) {
                associations.put(normalizedWord, imageList);
                logger.info("Association added", 
                    String.format("word=%s, newImages=%d, totalImages=%d", 
                        normalizedWord, imageList.size() - initialSize, imageList.size()));
            } else {
                logger.warning("No valid image paths added for word", normalizedWord);
            }
            
        } catch (Exception e) {
            logger.error("Error adding association", word, e);
        }
    }
    
    /**
     * Retrieves the image paths associated with a word.
     * 
     * @param word The word to look up
     * @return Array of image file paths, or empty array if word has no associations
     */
    public String[] getImagesForWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return new String[0];
        }
        
        String normalizedWord = word.toLowerCase().trim();
        List<String> imageList = associations.get(normalizedWord);
        
        if (imageList == null || imageList.isEmpty()) {
            return new String[0];
        }
        
        return imageList.toArray(new String[0]);
    }
    
    /**
     * Saves all associations to a JSON file.
     * The file format includes associations and metadata.
     * 
     * @param filepath Path to the output JSON file
     * @throws IOException if the file cannot be written
     */
    public void saveToFile(String filepath) throws IOException {
        if (filepath == null || filepath.trim().isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("Filepath cannot be null or empty");
            logger.error("Invalid filepath for save operation", "", e);
            throw e;
        }
        
        logger.info("Saving associations to file", filepath);
        
        try {
            // Create the data structure to serialize
            Map<String, Object> data = new HashMap<>();
            data.put("associations", associations);
            
            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("created", Instant.now().toString());
            metadata.put("wordCount", associations.size());
            
            int totalImages = associations.values().stream()
                .mapToInt(List::size)
                .sum();
            metadata.put("imageCount", totalImages);
            
            data.put("metadata", metadata);
            
            // Serialize to JSON
            String json;
            try {
                json = gson.toJson(data);
            } catch (Exception e) {
                logger.error("Failed to serialize associations to JSON", filepath, e);
                throw new IOException("Failed to serialize associations: " + e.getMessage(), e);
            }
            
            // Write to file
            Path path = Path.of(filepath);
            Path parent = path.getParent();
            if (parent != null) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    logger.error("Failed to create parent directories", parent.toString(), e);
                    throw new IOException("Failed to create parent directories: " + e.getMessage(), e);
                }
            }
            
            try {
                Files.writeString(path, json);
                logger.info("Associations saved successfully", 
                    String.format("file=%s, words=%d, images=%d", filepath, associations.size(), totalImages));
            } catch (IOException e) {
                logger.error("Failed to write associations file", filepath, e);
                throw new IOException("Failed to write associations file: " + e.getMessage(), e);
            }
            
        } catch (IOException e) {
            logger.error("Failed to save associations", filepath, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error saving associations", filepath, e);
            throw new IOException("Unexpected error saving associations: " + e.getMessage(), e);
        }
    }
    
    /**
     * Loads associations from a JSON file.
     * Replaces any existing associations in memory.
     * 
     * @param filepath Path to the JSON file to load
     * @throws IOException if the file cannot be read or parsed
     */
    public void loadFromFile(String filepath) throws IOException {
        if (filepath == null || filepath.trim().isEmpty()) {
            IllegalArgumentException e = new IllegalArgumentException("Filepath cannot be null or empty");
            logger.error("Invalid filepath for load operation", "", e);
            throw e;
        }
        
        logger.info("Loading associations from file", filepath);
        
        try {
            Path path = Path.of(filepath);
            
            if (!Files.exists(path)) {
                IOException e = new IOException("File does not exist: " + filepath);
                logger.error("Associations file not found", filepath, e);
                throw e;
            }
            
            if (!Files.isReadable(path)) {
                IOException e = new IOException("File is not readable: " + filepath);
                logger.error("Associations file not readable", filepath, e);
                throw e;
            }
            
            // Read JSON from file
            String json;
            try {
                json = Files.readString(path);
            } catch (IOException e) {
                logger.error("Failed to read associations file", filepath, e);
                throw new IOException("Failed to read associations file: " + e.getMessage(), e);
            }
            
            // Deserialize directly to the expected structure
            TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {};
            Map<String, Object> data;
            try {
                data = gson.fromJson(json, typeToken.getType());
            } catch (JsonSyntaxException e) {
                logger.error("Invalid JSON format in associations file", filepath, e);
                throw new IOException("Invalid JSON format: " + e.getMessage(), e);
            }
            
            if (data == null || !data.containsKey("associations")) {
                IOException e = new IOException("Invalid associations file format: missing 'associations' key");
                logger.error("Invalid associations file format", filepath, e);
                throw e;
            }
            
            // Extract and validate associations
            Object associationsObj = data.get("associations");
            if (!(associationsObj instanceof Map)) {
                IOException e = new IOException("Invalid associations file format: 'associations' must be a map");
                logger.error("Invalid associations data structure", filepath, e);
                throw e;
            }
            
            // Cast to proper type - Gson ensures the structure matches
            @SuppressWarnings("unchecked")
            Map<String, Object> rawAssociations = (Map<String, Object>) associationsObj;
            
            // Convert to proper typed map
            Map<String, List<String>> loadedAssociations = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawAssociations.entrySet()) {
                String word = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> rawList = (List<Object>) value;
                    List<String> imageList = new ArrayList<>();
                    for (Object item : rawList) {
                        if (item instanceof String) {
                            imageList.add((String) item);
                        }
                    }
                    loadedAssociations.put(word, imageList);
                }
            }
            
            this.associations = loadedAssociations;
            
            int totalImages = associations.values().stream()
                .mapToInt(List::size)
                .sum();
                
            logger.info("Associations loaded successfully", 
                String.format("file=%s, words=%d, images=%d", filepath, associations.size(), totalImages));
                
        } catch (IOException e) {
            logger.error("Failed to load associations", filepath, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error loading associations", filepath, e);
            throw new IOException("Unexpected error loading associations: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifies that all image files referenced in associations exist on disk.
     * 
     * @return true if all image files exist, false if any are missing
     */
    public boolean verifyImageFiles() {
        try {
            for (Map.Entry<String, List<String>> entry : associations.entrySet()) {
                String word = entry.getKey();
                List<String> imagePaths = entry.getValue();
                
                for (String imagePath : imagePaths) {
                    Path path = Path.of(imagePath);
                    if (!Files.exists(path)) {
                        logger.warning("Missing image file", 
                            String.format("word=%s, file=%s", word, imagePath));
                        return false;
                    }
                }
            }
            
            logger.info("All image files verified successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Error during image file verification", "", e);
            return false;
        }
    }
    
    /**
     * Gets a list of all missing image files.
     * Useful for logging and error reporting.
     * 
     * @return List of image file paths that don't exist on disk
     */
    public List<String> getMissingImageFiles() {
        List<String> missingFiles = new ArrayList<>();
        
        try {
            for (Map.Entry<String, List<String>> entry : associations.entrySet()) {
                String word = entry.getKey();
                List<String> imagePaths = entry.getValue();
                
                for (String imagePath : imagePaths) {
                    try {
                        Path path = Path.of(imagePath);
                        if (!Files.exists(path)) {
                            missingFiles.add(imagePath);
                            logger.warning("Missing image file detected", 
                                String.format("word=%s, file=%s", word, imagePath));
                        }
                    } catch (Exception e) {
                        logger.warning("Error checking image file", 
                            String.format("word=%s, file=%s, error=%s", word, imagePath, e.getMessage()));
                        missingFiles.add(imagePath);
                    }
                }
            }
            
            if (!missingFiles.isEmpty()) {
                logger.warning("Missing image files found", 
                    String.format("count=%d", missingFiles.size()));
            }
            
        } catch (Exception e) {
            logger.error("Error getting missing image files", "", e);
        }
        
        return missingFiles;
    }
    
    /**
     * Gets the total number of words with associations.
     * 
     * @return Number of unique words
     */
    public int getWordCount() {
        return associations.size();
    }
    
    /**
     * Gets the total number of images across all associations.
     * 
     * @return Total number of image paths
     */
    public int getImageCount() {
        return associations.values().stream()
            .mapToInt(List::size)
            .sum();
    }
    
    /**
     * Gets all words that have associations.
     * 
     * @return Set of words with image associations
     */
    public Set<String> getAllWords() {
        return new HashSet<>(associations.keySet());
    }
    
    /**
     * Checks if a word has any associations.
     * 
     * @param word The word to check
     * @return true if the word has at least one image association, false otherwise
     */
    public boolean hasWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        String normalizedWord = word.toLowerCase().trim();
        return associations.containsKey(normalizedWord) && 
               !associations.get(normalizedWord).isEmpty();
    }
    
    /**
     * Clears all associations from memory.
     */
    public void clear() {
        associations.clear();
    }
}
