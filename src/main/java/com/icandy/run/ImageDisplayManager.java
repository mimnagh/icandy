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
 * - Displaying images using configurable layout algorithms via LayoutEngine
 * - Swapping images with animated transitions via TransitionEngine
 * - Applying visual effects via VisualEffectsManager
 * - Tracking which images have been shown
 * - Cycling through available images
 * - Handling missing images gracefully
 * - Selecting subset of images when phrase has more content words than display slots
 * - Frame-based animation system with delta time
 * 
 * Requirements: 4.3, 4.4, 5.2, 5.3, 5.4, 9.1, 10.1, 12.6, 7.3
 */
public class ImageDisplayManager {
    
    private static final Logger LOGGER = Logger.getLogger(ImageDisplayManager.class.getName());
    private static final int DEFAULT_SIMULTANEOUS_IMAGES = 3;
    
    private final PApplet parent;
    private final AssociationManager associationManager;
    private final boolean isTestMode;
    
    // Engine integrations
    private LayoutEngine layoutEngine;
    private TransitionEngine transitionEngine;
    private VisualEffectsManager visualEffectsManager;
    
    // Configuration
    private int simultaneousImageCount;
    
    // Current state
    private String[] currentWords;
    private Map<String, List<String>> wordToImagePaths;
    private Map<String, List<PImage>> imageCache;
    private Map<String, Integer> currentImageIndices;
    private List<DisplaySlot> displaySlots;
    private ImageInfo[] currentImageInfos;
    private ImagePosition[] currentPositions;
    
    /**
     * Represents a display slot for an image with enhanced state tracking.
     */
    private static class DisplaySlot {
        String word;
        PImage image;
        ImageInfo imageInfo;
        ImagePosition position;
        
        DisplaySlot(String word, PImage image, ImageInfo imageInfo, ImagePosition position) {
            this.word = word;
            this.image = image;
            this.imageInfo = imageInfo;
            this.position = position;
        }
        
        // Legacy constructor for backward compatibility
        DisplaySlot(String word, PImage image, int x, int y, int width, int height) {
            this.word = word;
            this.image = image;
            this.imageInfo = new ImageInfo(image, word, "");
            this.position = new ImagePosition();
            this.position.x = x;
            this.position.y = y;
            this.position.width = width;
            this.position.height = height;
            this.position.scale = 1.0f;
            this.position.opacity = 1.0f;
            this.position.rotation = 0.0f;
            this.position.timestamp = System.currentTimeMillis();
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
        this.currentImageInfos = new ImageInfo[0];
        this.currentPositions = new ImagePosition[0];
        
        // Detect if we're in a test environment (PApplet not fully initialized)
        this.isTestMode = (parent.g == null);
    }
    
    /**
     * Sets the LayoutEngine for configurable image positioning.
     * 
     * @param layoutEngine The layout engine to use for positioning images
     */
    public void setLayoutEngine(LayoutEngine layoutEngine) {
        this.layoutEngine = layoutEngine;
        
        // Recalculate layout if we have current images
        if (currentImageInfos.length > 0) {
            calculateLayoutWithEngine();
        }
    }
    
    /**
     * Sets the TransitionEngine for animated image swapping.
     * 
     * @param transitionEngine The transition engine to use for image swaps
     */
    public void setTransitionEngine(TransitionEngine transitionEngine) {
        this.transitionEngine = transitionEngine;
    }
    
    /**
     * Sets the VisualEffectsManager for image enhancement.
     * 
     * @param visualEffectsManager The visual effects manager to use for image effects
     */
    public void setVisualEffectsManager(VisualEffectsManager visualEffectsManager) {
        this.visualEffectsManager = visualEffectsManager;
    }
    
    /**
     * Updates the display manager each frame for animations and transitions.
     * 
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    public void update(float deltaTime) {
        // Update layout engine for transitions
        if (layoutEngine != null) {
            layoutEngine.update(deltaTime);
            
            // Get updated positions from layout engine
            ImagePosition[] updatedPositions = layoutEngine.getCurrentPositions();
            if (updatedPositions != null && updatedPositions.length == displaySlots.size()) {
                for (int i = 0; i < displaySlots.size() && i < updatedPositions.length; i++) {
                    displaySlots.get(i).position = updatedPositions[i];
                }
            }
        }
        
        // Update transition engine
        if (transitionEngine != null) {
            transitionEngine.update(deltaTime);
        }
        
        // Update visual effects (particles, etc.)
        if (visualEffectsManager != null) {
            visualEffectsManager.updateParticles(deltaTime);
        }
    }
    
    /**
     * Sets the images for the current phrase based on its content words.
     * 
     * This method:
     * - Retrieves image paths for each word from the association manager
     * - Preloads images into cache (only if not already cached)
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
        
        // Preload images for words that aren't already cached
        for (Map.Entry<String, List<String>> entry : wordToImagePaths.entrySet()) {
            String word = entry.getKey();
            // Only preload if this word isn't already in the cache
            if (!imageCache.containsKey(word) || imageCache.get(word).isEmpty()) {
                preloadImages(entry.getValue().toArray(new String[0]));
            }
        }
        
        // Initialize display slots
        initializeDisplaySlots();
    }
    
    /**
     * Initializes display slots by selecting which words/images to display.
     * Uses LayoutEngine for positioning if available, falls back to grid layout.
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
            currentImageInfos = new ImageInfo[0];
            currentPositions = new ImagePosition[0];
            return;
        }
        
        // Reset image indices for new phrase
        currentImageIndices.clear();
        for (String word : wordsWithImages) {
            currentImageIndices.put(word, 0);
        }
        
        // Select words to display
        List<String> selectedWords = selectWordsForDisplay(wordsWithImages);
        
        // Create ImageInfo objects for selected words
        createImageInfos(selectedWords);
        
        // Calculate layout using LayoutEngine or fallback to grid
        if (layoutEngine != null) {
            calculateLayoutWithEngine();
        } else {
            calculateLayoutFallback(selectedWords);
        }
    }
    
    /**
     * Creates ImageInfo objects for the selected words.
     * 
     * @param selectedWords List of words to create ImageInfo objects for
     */
    private void createImageInfos(List<String> selectedWords) {
        currentImageInfos = new ImageInfo[selectedWords.size()];
        
        for (int i = 0; i < selectedWords.size(); i++) {
            String word = selectedWords.get(i);
            PImage image = getCurrentImageForWord(word);
            
            // Create ImageInfo with image metadata
            currentImageInfos[i] = new ImageInfo(image, word, "");
        }
    }
    
    /**
     * Calculates layout using the LayoutEngine.
     */
    private void calculateLayoutWithEngine() {
        if (layoutEngine == null || currentImageInfos.length == 0) {
            return;
        }
        
        // Calculate positions using layout engine
        currentPositions = layoutEngine.calculateLayout(currentImageInfos);
        
        // Create display slots from calculated positions
        displaySlots.clear();
        for (int i = 0; i < currentImageInfos.length && i < currentPositions.length; i++) {
            ImageInfo imageInfo = currentImageInfos[i];
            ImagePosition position = currentPositions[i];
            
            displaySlots.add(new DisplaySlot(imageInfo.word, imageInfo.image, imageInfo, position));
        }
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
     * Calculates the layout for displaying images in a grid (fallback when no LayoutEngine).
     * 
     * @param selectedWords List of words to display
     */
    private void calculateLayoutFallback(List<String> selectedWords) {
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
        
        // Create display slots with legacy constructor
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
     * Images are rendered using their calculated positions with appropriate scaling.
     * Visual effects are applied if VisualEffectsManager is available.
     */
    public void displayCurrentImages() {
        if (isTestMode || displaySlots.isEmpty()) {
            return;
        }
        
        for (DisplaySlot slot : displaySlots) {
            if (slot.image != null && slot.position != null) {
                displayImageWithEffects(slot);
            }
        }
        
        // Render particles if visual effects manager is available
        if (visualEffectsManager != null) {
            visualEffectsManager.renderParticles();
        }
    }
    
    /**
     * Displays a single image with position and visual effects.
     * 
     * @param slot The display slot containing image and position information
     */
    private void displayImageWithEffects(DisplaySlot slot) {
        PImage imageToRender = slot.image;
        ImagePosition pos = slot.position;
        
        // Apply visual effects if available
        if (visualEffectsManager != null) {
            imageToRender = visualEffectsManager.applyEffects(slot.image);
        }
        
        // Save current transformation matrix
        parent.pushMatrix();
        
        // Apply transformations
        parent.translate(pos.x + pos.width / 2, pos.y + pos.height / 2);
        
        if (pos.rotation != 0) {
            parent.rotate(parent.radians(pos.rotation));
        }
        
        if (pos.scale != 1.0f) {
            parent.scale(pos.scale);
        }
        
        // Apply opacity
        if (pos.opacity < 1.0f) {
            parent.tint(255, pos.opacity * 255);
        }
        
        // Calculate aspect-ratio-preserving dimensions
        float imageAspect = (float) imageToRender.width / imageToRender.height;
        float slotAspect = pos.width / pos.height;
        
        float drawWidth, drawHeight;
        if (imageAspect > slotAspect) {
            // Image is wider - fit to width
            drawWidth = pos.width;
            drawHeight = pos.width / imageAspect;
        } else {
            // Image is taller - fit to height
            drawHeight = pos.height;
            drawWidth = pos.height * imageAspect;
        }
        
        // Draw image centered at origin (due to translate)
        parent.image(imageToRender, -drawWidth / 2, -drawHeight / 2, drawWidth, drawHeight);
        
        // Reset tint
        parent.noTint();
        
        // Restore transformation matrix
        parent.popMatrix();
    }
    
    /**
     * Swaps the currently displayed images with different images from the same words' image sets.
     * 
     * This method:
     * - Selects images that have not been recently displayed for each word
     * - Uses TransitionEngine for animated transitions if available
     * - Updates the display slots with new images
     * - Cycles through available images when all have been shown
     * 
     * Requirements: 5.3, 5.4, 5.5, 10.1, 10.7
     */
    public void swapImages() {
        if (displaySlots.isEmpty()) {
            return;
        }
        
        // Collect new images for swapping
        List<PImage> newImages = new ArrayList<>();
        List<String> newWords = new ArrayList<>();
        
        for (DisplaySlot slot : displaySlots) {
            String word = slot.word;
            List<PImage> images = imageCache.get(word);
            
            if (images == null || images.isEmpty()) {
                newImages.add(slot.image); // Keep current image
                newWords.add(word);
                continue;
            }
            
            // Only swap if there are multiple images for this word
            if (images.size() > 1) {
                // Advance to next image
                int currentIndex = currentImageIndices.getOrDefault(word, 0);
                int nextIndex = (currentIndex + 1) % images.size();
                currentImageIndices.put(word, nextIndex);
                
                newImages.add(images.get(nextIndex));
                newWords.add(word);
            } else {
                newImages.add(slot.image); // Keep current image
                newWords.add(word);
            }
        }
        
        // Apply transition if TransitionEngine is available
        if (transitionEngine != null && !newImages.isEmpty()) {
            swapImagesWithTransition(newImages, newWords);
        } else {
            swapImagesInstant(newImages, newWords);
        }
    }
    
    /**
     * Swaps images using animated transitions via TransitionEngine.
     * 
     * @param newImages List of new images to display
     * @param newWords List of words corresponding to new images
     */
    private void swapImagesWithTransition(List<PImage> newImages, List<String> newWords) {
        // Create new ImageInfo objects for transition
        ImageInfo[] newImageInfos = new ImageInfo[newImages.size()];
        for (int i = 0; i < newImages.size(); i++) {
            newImageInfos[i] = new ImageInfo(newImages.get(i), newWords.get(i), "");
        }
        
        // Calculate new positions if layout engine is available
        ImagePosition[] newPositions = null;
        if (layoutEngine != null) {
            newPositions = layoutEngine.calculateLayout(newImageInfos);
        }
        
        // Start transitions for each image
        for (int i = 0; i < displaySlots.size() && i < newImages.size(); i++) {
            DisplaySlot slot = displaySlots.get(i);
            PImage newImage = newImages.get(i);
            String newWord = newWords.get(i);
            
            // Create new ImageInfo and position
            ImageInfo newImageInfo = new ImageInfo(newImage, newWord, "");
            ImagePosition newPosition = (newPositions != null && i < newPositions.length) 
                ? newPositions[i] 
                : slot.position.copy(); // Use current position if no new layout
            
            // Start transition via TransitionEngine
            ImageState newState = new ImageState(newPosition);
            transitionEngine.startImageTransition(newImageInfo, newState);
            
            // Update slot immediately (transition engine will handle animation)
            slot.image = newImage;
            slot.word = newWord;
            slot.imageInfo = newImageInfo;
            // Position will be updated by TransitionEngine via update() method
        }
        
        // Update current state
        currentImageInfos = newImageInfos;
        if (newPositions != null) {
            currentPositions = newPositions;
        }
    }
    
    /**
     * Swaps images instantly without transitions (fallback).
     * 
     * @param newImages List of new images to display
     * @param newWords List of words corresponding to new images
     */
    private void swapImagesInstant(List<PImage> newImages, List<String> newWords) {
        for (int i = 0; i < displaySlots.size() && i < newImages.size(); i++) {
            DisplaySlot slot = displaySlots.get(i);
            
            // Update the slot with the new image
            slot.image = newImages.get(i);
            slot.word = newWords.get(i);
            slot.imageInfo = new ImageInfo(newImages.get(i), newWords.get(i), "");
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
        this.currentImageInfos = new ImageInfo[0];
        this.currentPositions = new ImagePosition[0];
    }
    
    /**
     * Clears cached images for words that are not in the current phrase.
     * This helps manage memory by removing images that are no longer needed.
     */
    public void clearUnusedImages() {
        if (currentWords.length == 0) {
            return;
        }
        
        // Get set of current words
        Set<String> currentWordSet = new HashSet<>(Arrays.asList(currentWords));
        
        // Remove cached images for words not in current phrase
        imageCache.keySet().removeIf(word -> !currentWordSet.contains(word));
        currentImageIndices.keySet().removeIf(word -> !currentWordSet.contains(word));
    }
}
