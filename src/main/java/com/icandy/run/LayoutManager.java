package com.icandy.run;

import com.icandy.common.ConfigurationManager;

/**
 * High-level manager for layout algorithms and transitions.
 * Handles algorithm switching, configuration updates, and smooth transitions.
 */
public class LayoutManager {
    
    private LayoutEngine layoutEngine;
    private ConfigurationManager configManager;
    private String currentAlgorithmName;
    private LayoutAlgorithm currentAlgorithm;
    
    /**
     * Create a new LayoutManager with the specified configuration.
     * 
     * @param configManager Configuration manager for layout settings
     */
    public LayoutManager(ConfigurationManager configManager) {
        this.configManager = configManager;
        this.layoutEngine = new LayoutEngine();
        
        // Initialize with configured algorithm
        this.currentAlgorithmName = configManager.getCurrentLayoutAlgorithm();
        this.currentAlgorithm = LayoutAlgorithmFactory.createAlgorithm(currentAlgorithmName);
        
        // Set up layout engine
        this.layoutEngine.setAlgorithm(currentAlgorithm);
        this.layoutEngine.setConfig(configManager.getLayoutConfig());
        this.layoutEngine.setTransitionDuration(configManager.getLayoutTransitionDuration());
    }
    
    /**
     * Switch to a different layout algorithm with smooth transition.
     * 
     * @param algorithmName The name of the new algorithm
     * @param images Current images to reposition
     * @return true if the switch was successful, false if algorithm name is invalid
     */
    public boolean switchAlgorithm(String algorithmName, ImageInfo[] images) {
        if (!LayoutAlgorithmFactory.isValidAlgorithm(algorithmName)) {
            return false;
        }
        
        String newAlgorithmName = algorithmName.toLowerCase();
        if (newAlgorithmName.equals(currentAlgorithmName)) {
            // Already using this algorithm
            return true;
        }
        
        // Create new algorithm instance
        LayoutAlgorithm newAlgorithm = LayoutAlgorithmFactory.createAlgorithm(newAlgorithmName);
        
        // Calculate new positions
        ImagePosition[] newPositions = newAlgorithm.calculatePositions(images, layoutEngine.getConfig());
        
        // Start transition to new positions
        layoutEngine.animateToNewLayout(newPositions, configManager.getLayoutTransitionDuration());
        
        // Update current algorithm
        this.currentAlgorithm = newAlgorithm;
        this.currentAlgorithmName = newAlgorithmName;
        this.layoutEngine.setAlgorithm(newAlgorithm);
        
        // Update configuration manager
        configManager.setCurrentLayoutAlgorithm(newAlgorithmName);
        
        return true;
    }
    
    /**
     * Update layout configuration parameters and recalculate positions if needed.
     * 
     * @param newConfig The new layout configuration
     * @param images Current images to reposition
     */
    public void updateConfiguration(LayoutConfig newConfig, ImageInfo[] images) {
        if (newConfig == null) {
            return;
        }
        
        // Update layout engine configuration
        layoutEngine.setConfig(newConfig);
        
        // Update configuration manager
        configManager.updateLayoutConfig(newConfig);
        
        // Recalculate positions with new configuration
        if (images != null && images.length > 0) {
            ImagePosition[] newPositions = currentAlgorithm.calculatePositions(images, newConfig);
            layoutEngine.animateToNewLayout(newPositions, configManager.getLayoutTransitionDuration());
        }
    }
    
    /**
     * Calculate layout positions for a set of images.
     * 
     * @param images Array of images to position
     * @return Array of calculated positions
     */
    public ImagePosition[] calculateLayout(ImageInfo[] images) {
        return layoutEngine.calculateLayout(images);
    }
    
    /**
     * Update the layout manager each frame.
     * 
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    public void update(float deltaTime) {
        layoutEngine.update(deltaTime);
    }
    
    /**
     * Get the current image positions (including any transition interpolation).
     * 
     * @return Array of current image positions
     */
    public ImagePosition[] getCurrentPositions() {
        return layoutEngine.getCurrentPositions();
    }
    
    /**
     * Check if a layout transition is currently active.
     * 
     * @return true if transitioning, false otherwise
     */
    public boolean isTransitioning() {
        return layoutEngine.isTransitioning();
    }
    
    /**
     * Get the progress of the current transition.
     * 
     * @return Transition progress from 0.0 to 1.0
     */
    public float getTransitionProgress() {
        return layoutEngine.getTransitionProgress();
    }
    
    /**
     * Get the name of the current layout algorithm.
     * 
     * @return Current algorithm name
     */
    public String getCurrentAlgorithmName() {
        return currentAlgorithmName;
    }
    
    /**
     * Get the current layout configuration.
     * 
     * @return Current layout configuration
     */
    public LayoutConfig getCurrentConfig() {
        return layoutEngine.getConfig();
    }
    
    /**
     * Force completion of any ongoing transition.
     */
    public void completeTransition() {
        layoutEngine.completeTransition();
    }
    
    /**
     * Set the duration for layout transitions.
     * 
     * @param duration Duration in milliseconds
     */
    public void setTransitionDuration(float duration) {
        layoutEngine.setTransitionDuration(duration);
    }
}