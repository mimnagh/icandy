package com.icandy.run;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages animated transitions between image states.
 * Coordinates multiple simultaneous transitions with configurable effects and timing.
 */
public class TransitionEngine {
    private TransitionEffect currentEffect;
    private TransitionConfig config;
    private Map<ImageInfo, TransitionState> activeTransitions;
    private float currentTime;
    private boolean enabled;
    
    /**
     * Creates a new transition engine with default configuration.
     */
    public TransitionEngine() {
        this.config = new TransitionConfig();
        this.activeTransitions = new ConcurrentHashMap<>();
        this.currentTime = 0.0f;
        this.enabled = true;
    }
    
    /**
     * Creates a new transition engine with specified effect and configuration.
     * 
     * @param effect The default transition effect
     * @param config The transition configuration
     */
    public TransitionEngine(TransitionEffect effect, TransitionConfig config) {
        this();
        this.currentEffect = effect;
        this.config = config != null ? config : new TransitionConfig();
    }
    
    /**
     * Sets the current transition effect.
     * 
     * @param effect The transition effect to use for new transitions
     */
    public void setEffect(TransitionEffect effect) {
        this.currentEffect = effect;
    }
    
    /**
     * Sets the transition configuration.
     * 
     * @param config The new configuration
     */
    public void setConfig(TransitionConfig config) {
        this.config = config != null ? config : new TransitionConfig();
    }
    
    /**
     * Starts a transition for a specific image to a new state.
     * 
     * @param image The image to transition
     * @param newState The target state for the image
     */
    public void startImageTransition(ImageInfo image, ImageState newState) {
        if (!enabled || image == null || newState == null || currentEffect == null) {
            return;
        }
        
        // Get current state (either from active transition or default)
        ImageState currentState = getCurrentImageState(image);
        
        // Create new transition state
        TransitionState transition = new TransitionState(
            image, currentEffect, currentState, newState, config.getDuration(), currentTime
        );
        
        activeTransitions.put(image, transition);
    }
    
    /**
     * Starts transitions for multiple images to new positions (layout transitions).
     * 
     * @param newPositions Array of new positions for images
     */
    public void startLayoutTransition(ImagePosition[] newPositions) {
        if (!enabled || newPositions == null || currentEffect == null) {
            return;
        }
        
        // Note: This method has limited functionality since ImagePosition doesn't contain ImageInfo
        // Use startLayoutTransition(ImageInfo[], ImagePosition[]) for full functionality
        float staggerDelay = config.isEnableStagger() ? config.getStaggerDelay() : 0.0f;
        
        for (int i = 0; i < newPositions.length; i++) {
            ImagePosition position = newPositions[i];
            if (position != null) {
                // For layout transitions, we need to associate positions with images
                // This is a simplified approach - in practice, you'd need to track which image goes where
                ImageState newState = new ImageState(position);
                
                // We can't get ImageInfo from ImagePosition directly, so we'll need to modify this
                // For now, skip positions without associated ImageInfo
                continue;
            }
        }
    }
    
    /**
     * Starts transitions for multiple images to new positions (layout transitions).
     * 
     * @param images Array of images to transition
     * @param newPositions Array of new positions for the images
     */
    public void startLayoutTransition(ImageInfo[] images, ImagePosition[] newPositions) {
        if (!enabled || images == null || newPositions == null || currentEffect == null) {
            return;
        }
        
        int count = Math.min(images.length, newPositions.length);
        float staggerDelay = config.isEnableStagger() ? config.getStaggerDelay() : 0.0f;
        
        for (int i = 0; i < count; i++) {
            ImageInfo image = images[i];
            ImagePosition position = newPositions[i];
            
            if (image != null && position != null) {
                ImageState newState = new ImageState(position);
                ImageState currentState = getCurrentImageState(image);
                
                // Apply stagger delay if enabled
                float transitionStartTime = currentTime + (i * staggerDelay);
                
                TransitionState transition = new TransitionState(
                    image, currentEffect, currentState, newState, 
                    config.getDuration(), transitionStartTime
                );
                
                activeTransitions.put(image, transition);
            }
        }
    }
    
    /**
     * Updates all active transitions based on elapsed time.
     * 
     * @param deltaTime The time elapsed since last update in milliseconds
     */
    public void update(float deltaTime) {
        currentTime += deltaTime;
        
        // Update all active transitions and remove completed ones
        Iterator<Map.Entry<ImageInfo, TransitionState>> iterator = activeTransitions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ImageInfo, TransitionState> entry = iterator.next();
            TransitionState transition = entry.getValue();
            
            transition.update(currentTime);
            
            if (transition.isComplete()) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Gets the current state of an image (either transitioning or final state).
     * 
     * @param image The image to get the state for
     * @return The current image state
     */
    public ImageState getCurrentImageState(ImageInfo image) {
        if (image == null) {
            return new ImageState();
        }
        
        TransitionState transition = activeTransitions.get(image);
        if (transition != null && transition.isActive()) {
            return transition.update(currentTime);
        }
        
        // Return default state if no active transition
        return new ImageState();
    }
    
    /**
     * Checks if there are any active transitions.
     * 
     * @return true if transitions are currently running
     */
    public boolean hasActiveTransitions() {
        return !activeTransitions.isEmpty();
    }
    
    /**
     * Gets the number of active transitions.
     * 
     * @return The count of active transitions
     */
    public int getActiveTransitionCount() {
        return activeTransitions.size();
    }
    
    /**
     * Stops all active transitions immediately.
     */
    public void stopAllTransitions() {
        for (TransitionState transition : activeTransitions.values()) {
            transition.stop();
        }
        activeTransitions.clear();
    }
    
    /**
     * Stops a specific image's transition.
     * 
     * @param image The image to stop transitioning
     */
    public void stopTransition(ImageInfo image) {
        TransitionState transition = activeTransitions.get(image);
        if (transition != null) {
            transition.stop();
            activeTransitions.remove(image);
        }
    }
    
    /**
     * Enables or disables the transition engine.
     * 
     * @param enabled Whether transitions should be active
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            stopAllTransitions();
        }
    }
    
    /**
     * Checks if the transition engine is enabled.
     * 
     * @return true if transitions are enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Gets the current transition effect.
     * 
     * @return The current effect, or null if none set
     */
    public TransitionEffect getCurrentEffect() {
        return currentEffect;
    }
    
    /**
     * Gets the current configuration.
     * 
     * @return The current transition configuration
     */
    public TransitionConfig getConfig() {
        return config;
    }
    
    /**
     * Gets all active transitions (for debugging/monitoring).
     * 
     * @return A copy of the active transitions map
     */
    public Map<ImageInfo, TransitionState> getActiveTransitions() {
        return new HashMap<>(activeTransitions);
    }
    
    @Override
    public String toString() {
        return String.format("TransitionEngine{effect=%s, activeTransitions=%d, enabled=%s}",
                currentEffect != null ? currentEffect.getName() : "none", 
                activeTransitions.size(), enabled);
    }
}