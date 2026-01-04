package com.icandy.build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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
    private Gson gson;
    
    /**
     * Creates an AssociationManager with an empty associations map.
     */
    public AssociationManager() {
        this.associations = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
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
            return;
        }
        
        if (imagePaths == null || imagePaths.length == 0) {
            return;
        }
        
        String normalizedWord = word.toLowerCase().trim();
        
        // Get existing list or create new one
        List<String> imageList = associations.getOrDefault(normalizedWord, new ArrayList<>());
        
        // Track if we added any valid paths
        int initialSize = imageList.size();
        
        // Add new image paths
        for (String imagePath : imagePaths) {
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                imageList.add(imagePath);
            }
        }
        
        // Only add to map if we actually added at least one valid image path
        if (imageList.size() > initialSize) {
            associations.put(normalizedWord, imageList);
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
            throw new IllegalArgumentException("Filepath cannot be null or empty");
        }
        
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
        String json = gson.toJson(data);
        
        // Write to file
        Path path = Path.of(filepath);
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, json);
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
            throw new IllegalArgumentException("Filepath cannot be null or empty");
        }
        
        Path path = Path.of(filepath);
        
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filepath);
        }
        
        try {
            // Read JSON from file
            String json = Files.readString(path);
            
            // Deserialize directly to the expected structure
            TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {};
            Map<String, Object> data = gson.fromJson(json, typeToken.getType());
            
            if (data == null || !data.containsKey("associations")) {
                throw new IOException("Invalid associations file format: missing 'associations' key");
            }
            
            // Extract and validate associations
            Object associationsObj = data.get("associations");
            if (!(associationsObj instanceof Map)) {
                throw new IOException("Invalid associations file format: 'associations' must be a map");
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
        } catch (com.google.gson.JsonSyntaxException e) {
            throw new IOException("Invalid JSON format: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifies that all image files referenced in associations exist on disk.
     * 
     * @return true if all image files exist, false if any are missing
     */
    public boolean verifyImageFiles() {
        for (List<String> imagePaths : associations.values()) {
            for (String imagePath : imagePaths) {
                Path path = Path.of(imagePath);
                if (!Files.exists(path)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Gets a list of all missing image files.
     * Useful for logging and error reporting.
     * 
     * @return List of image file paths that don't exist on disk
     */
    public List<String> getMissingImageFiles() {
        List<String> missingFiles = new ArrayList<>();
        
        for (List<String> imagePaths : associations.values()) {
            for (String imagePath : imagePaths) {
                Path path = Path.of(imagePath);
                if (!Files.exists(path)) {
                    missingFiles.add(imagePath);
                }
            }
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
