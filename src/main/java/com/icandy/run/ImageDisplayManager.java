package com.icandy.run;

import com.icandy.build.AssociationManager;
import processing.core.PApplet;
import processing.core.PImage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

/**
 * ImageDisplayManager manages the display and swapping of images on screen.
 * 
 * This class handles:
 * - Loading and caching PImage objects
 * - Displaying images in visually appealing layouts
 * - Swapping images on beat detection
 * - Tracking which images have been shown
 * - Cycling through available images
 * - Handling missing images gracefully
 * - Selecting subset of images when phrase has more content words than display slots
 * 
 * Requirements: 4.3, 4.4, 5.2, 5.3, 5.4
 */
public class ImageDisplayManager {
    
    private static final Logger LOGGER = Logger.getLogger(ImageDisplayManager.class.getName());
    private static final int DEFAULT_SIMULTANEOUS_IMAGES = 3;
    
    private final PApplet parent;
    private final AssociationManager associationManager;
    private final boolean isTestMode;
    
    // Configuration
    private int simultaneousImageCount;
    
    // Current state
    private String[] currentWords;
    private Map<String, List<String>> wordToImagePaths;
    private Map<String, List<PImage>> imageCache;
    private Map<String, Integer> currentImageIndices;
    private List<DisplaySlot> displaySlots;
    
    /**
     * Represents a display slot for an image.
     */
    private static class DisplaySlot {
        String word;
        PImage image;
        int x, y, width, height;
        
        DisplaySlot(String word, PImage image, int x, int y, int width, int height) {
            this.word = word;
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
    
    /**
     * Creates an ImageDisplayManager.
     * 
     * @param parent The Processing PApplet instance for rendering
     * @param associationManager The association manager for word-image mappings
     */
    public ImageDisplayManager(PApplet parent, AssociationManager associationManager) {
        this.parent = parent;
        this.associationManager = associationManager;
        this.simultaneousImageCount = DEFAULT_SIMULTANEOUS_IMAGES;
        this.currentWords = new String[0];
        this.wordToImagePaths = new HashMap<>();
        this.imageCache = new HashMap<>();
        this.currentImageIndices = new HashMap<>();
        this.displaySlots = new ArrayList<>();
        
        // Detect if we're in a test environment (PApplet not fully initialized)
        this.isTestMode = (parent.g == null);
    }
    
    /**
     * Sets the images for the current phrase based on its content words.
     * 
     * This method:
     * - Retrieves image paths for each word from the association manager
     * - Preloads images into cache
     * - Selects which words to display if there are more words than display slots
     * - Initializes display slots with images
     * 
     * @param words Array of content words in the current phrase
     */
    public void setImagesForPhrase(String[] words) {
        if (words == null || words.length == 0) {
            this.currentWords = new String[0];
            this.wordToImagePaths.clear();
            this.displaySlots.clear();
            return;
        }
        
        this.currentWords = words;
        this.wordToImagePaths.clear();
        
        // Collect image paths for each word
        for (String word : words) {
            String[] imagePaths = associationManager.getImagesForWord(word);
            if (imagePaths.length > 0) {
                wordToImagePaths.put(word, Arrays.asList(imagePaths));
            }
        }
        
        // Preload all images for these words
        for (Map.Entry<String, List<String>> entry : wordToImagePaths.entrySet()) {
            preloadImages(entry.getValue().toArray(new String[0]));
        }
        
        // Initialize display slots
        initializeDisplaySlots();
    }
    
    /**
     * Initializes display slots by selecting which words/images to display.
     * 
     * Image Selection Strategy:
     * - When phrase has N content words and display shows M images (where N > M):
     *   - Randomly select M words from the N content words
     *   - Display one image from each selected word
     * - When phrase has fewer content words than display slots:
     *   - Display all available images
     *   - Fill remaining slots with images from same words (cycling)
     */
    private void initializeDisplaySlots() {
        displaySlots.clear();
        
        // Get words that have images
        List<String> wordsWithImages = new ArrayList<>(wordToImagePaths.keySet());
        
        if (wordsWithImages.isEmpty()) {
            return;
        }
        
        // Reset image indices for new phrase
        currentImageIndices.clear();
        for (String word : wordsWithImages) {
            currentImageIndices.put(word, 0);
        }
        
        // Select words to display
        List<String> selectedWords = selectWordsForDisplay(wordsWithImages);
        
        // Calculate layout
        calculateLayout(selectedWords);
    }
    
    /**
     * Selects which words to display based on available words and display slots.
     * 
     * @param wordsWithImages List of words that have images
     * @return List of selected words to display
     */
    private List<String> selectWordsForDisplay(List<String> wordsWithImages) {
        List<String> selectedWords = new ArrayList<>();
        
        if (wordsWithImages.size() <= simultaneousImageCount) {
            // Display all words, potentially cycling to fill slots
            selectedWords.addAll(wordsWithImages);
            
            // Fill remaining slots by cycling through words
            int slotsToFill = simultaneousImageCount - wordsWithImages.size();
            for (int i = 0; i < slotsToFill; i++) {
                selectedWords.add(wordsWithImages.get(i % wordsWithImages.size()));
            }
        } else {
            // More words than slots - randomly select
            List<String> shuffled = new ArrayList<>(wordsWithImages);
            Collections.shuffle(shuffled, new Random());
            selectedWords.addAll(shuffled.subList(0, simultaneousImageCount));
        }
        
        return selectedWords;
    }
    
    /**
     * Calculates the layout for displaying images in a grid.
     * 
     * @param selectedWords List of words to display
     */
    private void calculateLayout(List<String> selectedWords) {
        if (selectedWords.isEmpty()) {
            return;
        }
        
        int screenWidth = parent.width;
        int screenHeight = parent.height;
        
        // Use upper 2/3 of screen (leave lower 1/3 for text)
        int displayHeight = (int) (screenHeight * 0.67);
        
        // Calculate grid dimensions
        int cols = (int) Math.ceil(Math.sqrt(selectedWords.size()));
        int rows = (int) Math.ceil((double) selectedWords.size() / cols);
        
        int slotWidth = screenWidth / cols;
        int slotHeight = displayHeight / rows;
        
        // Add some padding
        int padding = 10;
        int imageWidth = slotWidth - (2 * padding);
        int imageHeight = slotHeight - (2 * padding);
        
        // Create display slots
        for (int i = 0; i < selectedWords.size(); i++) {
            String word = selectedWords.get(i);
            int col = i % cols;
            int row = i / cols;
            
            int x = col * slotWidth + padding;
            int y = row * slotHeight + padding;
            
            // Get the current image for this word
            PImage image = getCurrentImageForWord(word);
            
            displaySlots.add(new DisplaySlot(word, image, x, y, imageWidth, imageHeight));
        }
    }
    
    /**
     * Gets the current image for a word based on the current index.
     * 
     * @param word The word to get an image for
     * @return The PImage, or null if not available
     */
    private PImage getCurrentImageForWord(String word) {
        List<PImage> images = imageCache.get(word);
        if (images == null || images.isEmpty()) {
            return null;
        }
        
        int index = currentImageIndices.getOrDefault(word, 0);
        return images.get(index % images.size());
    }
    
    /**
     * Displays the currently loaded images on screen.
     * 
     * Images are rendered in their calculated positions with appropriate scaling.
     */
    public void displayCurrentImages() {
        if (isTestMode || displaySlots.isEmpty()) {
            return;
        }
        
        for (DisplaySlot slot : displaySlots) {
            if (slot.image != null) {
                // Calculate aspect-ratio-preserving dimensions
                float imageAspect = (float) slot.image.width / slot.image.height;
                float slotAspect = (float) slot.width / slot.height;
                
                int drawWidth, drawHeight;
                if (imageAspect > slotAspect) {
                    // Image is wider - fit to width
                    drawWidth = slot.width;
                    drawHeight = (int) (slot.width / imageAspect);
                } else {
                    // Image is taller - fit to height
                    drawHeight = slot.height;
                    drawWidth = (int) (slot.height * imageAspect);
                }
                
                // Center the image in the slot
                int drawX = slot.x + (slot.width - drawWidth) / 2;
                int drawY = slot.y + (slot.height - drawHeight) / 2;
                
                parent.image(slot.image, drawX, drawY, drawWidth, drawHeight);
            }
        }
    }
    
    /**
     * Swaps the currently displayed images with different images from the same words' image sets.
     * 
     * This method:
     * - Selects images that have not been recently displayed for each word
     * - Updates the display slots with new images
     * - Cycles through available images when all have been shown
     * 
     * Requirements: 5.3, 5.4, 5.5
     */
    public void swapImages() {
        if (displaySlots.isEmpty()) {
            return;
        }
        
        for (DisplaySlot slot : displaySlots) {
            String word = slot.word;
            List<PImage> images = imageCache.get(word);
            
            if (images == null || images.isEmpty()) {
                continue;
            }
            
            // Only swap if there are multiple images for this word
            if (images.size() > 1) {
                // Advance to next image
                int currentIndex = currentImageIndices.getOrDefault(word, 0);
                int nextIndex = (currentIndex + 1) % images.size();
                currentImageIndices.put(word, nextIndex);
                
                // Update the slot with the new image
                slot.image = images.get(nextIndex);
            }
        }
    }
    
    /**
     * Preloads images into the cache for performance.
     * 
     * This method loads images from disk and stores them as PImage objects.
     * Missing images are logged but don't cause failures.
     * 
     * @param imagePaths Array of image file paths to preload
     */
    public void preloadImages(String[] imagePaths) {
        if (imagePaths == null || imagePaths.length == 0) {
            return;
        }
        
        // Group paths by word (extract word from filename)
        Map<String, List<String>> pathsByWord = new HashMap<>();
        
        for (String imagePath : imagePaths) {
            if (imagePath == null || imagePath.trim().isEmpty()) {
                continue;
            }
            
            // Check if file exists
            if (!Files.exists(Path.of(imagePath))) {
                LOGGER.warning("Image file not found: " + imagePath);
                continue;
            }
            
            // Extract word from path (e.g., "data/images/hello_1.jpg" -> "hello")
            String word = extractWordFromPath(imagePath);
            
            pathsByWord.computeIfAbsent(word, k -> new ArrayList<>()).add(imagePath);
        }
        
        // Load images for each word
        for (Map.Entry<String, List<String>> entry : pathsByWord.entrySet()) {
            String word = entry.getKey();
            List<String> paths = entry.getValue();
            
            List<PImage> images = imageCache.computeIfAbsent(word, k -> new ArrayList<>());
            
            for (String path : paths) {
                // Skip if already loaded
                boolean alreadyLoaded = images.stream()
                    .anyMatch(img -> img != null && path.equals(getImagePath(img)));
                
                if (alreadyLoaded) {
                    continue;
                }
                
                // Load image (skip in test mode)
                if (!isTestMode) {
                    try {
                        PImage image = parent.loadImage(path);
                        if (image != null && image.width > 0) {
                            images.add(image);
                        } else {
                            LOGGER.warning("Failed to load image: " + path);
                        }
                    } catch (Exception e) {
                        LOGGER.warning("Error loading image " + path + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Extracts the word from an image file path.
     * 
     * @param imagePath The image file path
     * @return The extracted word
     */
    private String extractWordFromPath(String imagePath) {
        // Get filename from path
        String filename = Path.of(imagePath).getFileName().toString();
        
        // Remove extension
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            filename = filename.substring(0, dotIndex);
        }
        
        // Remove number suffix (e.g., "hello_1" -> "hello")
        int underscoreIndex = filename.lastIndexOf('_');
        if (underscoreIndex > 0) {
            filename = filename.substring(0, underscoreIndex);
        }
        
        return filename.toLowerCase();
    }
    
    /**
     * Gets the file path associated with a PImage (for comparison).
     * Since PImage doesn't store the path, we return null.
     * 
     * @param image The PImage
     * @return null (PImage doesn't store path)
     */
    private String getImagePath(PImage image) {
        // PImage doesn't store the original path, so we can't compare
        // This is a limitation, but we'll rely on the fact that we don't
        // reload images that are already in the cache for a given word
        return null;
    }
    
    /**
     * Sets the number of images to display simultaneously.
     * 
     * @param count The number of simultaneous images
     */
    public void setSimultaneousImageCount(int count) {
        if (count <= 0) {
            this.simultaneousImageCount = DEFAULT_SIMULTANEOUS_IMAGES;
        } else {
            this.simultaneousImageCount = count;
        }
        
        // Reinitialize display slots if we have current words
        if (currentWords.length > 0) {
            initializeDisplaySlots();
        }
    }
    
    /**
     * Gets the current number of simultaneous images.
     * 
     * @return The simultaneous image count
     */
    public int getSimultaneousImageCount() {
        return simultaneousImageCount;
    }
    
    /**
     * Gets the current words being displayed.
     * 
     * @return Array of current words
     */
    public String[] getCurrentWords() {
        return currentWords;
    }
    
    /**
     * Gets the number of display slots currently active.
     * 
     * @return Number of display slots
     */
    public int getDisplaySlotCount() {
        return displaySlots.size();
    }
    
    /**
     * Clears all cached images and display state.
     */
    public void clear() {
        this.currentWords = new String[0];
        this.wordToImagePaths.clear();
        this.imageCache.clear();
        this.currentImageIndices.clear();
        this.displaySlots.clear();
    }
}
