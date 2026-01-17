package com.icandy.run;

/**
 * Manages layout algorithms and coordinates image positioning.
 * Provides a unified interface for different layout strategies and
 * handles transitions between layouts.
 */
public class LayoutEngine {
    
    /** Current layout algorithm being used */
    private LayoutAlgorithm currentAlgorithm;
    
    /** Configuration for layout algorithms */
    private LayoutConfig config;
    
    /** Current calculated positions */
    private ImagePosition[] currentPositions;
    
    /** Target positions for transitions */
    private ImagePosition[] targetPositions;
    
    /** Whether we're currently transitioning between layouts */
    private boolean isTransitioning;
    
    /** Progress of current transition (0.0 to 1.0) */
    private float transitionProgress;
    
    /** Duration of layout transitions in milliseconds */
    private float transitionDuration;
    
    /** Start time of current transition */
    private long transitionStartTime;
    
    /**
     * Create a new LayoutEngine with default configuration.
     */
    public LayoutEngine() {
        this.config = new LayoutConfig();
        this.currentPositions = new ImagePosition[0];
        this.targetPositions = new ImagePosition[0];
        this.isTransitioning = false;
        this.transitionProgress = 0.0f;
        this.transitionDuration = 1000.0f; // 1 second default
    }
    
    /**
     * Set the layout algorithm to use.
     * 
     * @param algorithm The layout algorithm implementation
     */
    public void setAlgorithm(LayoutAlgorithm algorithm) {
        this.currentAlgorithm = algorithm;
    }
    
    /**
     * Set the configuration for layout algorithms.
     * 
     * @param config Layout configuration
     */
    public void setConfig(LayoutConfig config) {
        if (config != null) {
            this.config = config.copy();
            this.config.validate();
        }
    }
    
    /**
     * Get the current layout configuration.
     * 
     * @return Current layout configuration
     */
    public LayoutConfig getConfig() {
        return config.copy();
    }
    
    /**
     * Calculate layout positions for a set of images.
     * 
     * @param images Array of images to position
     * @return Array of calculated positions
     */
    public ImagePosition[] calculateLayout(ImageInfo[] images) {
        if (currentAlgorithm == null || images == null || images.length == 0) {
            return new ImagePosition[0];
        }
        
        ImagePosition[] positions = currentAlgorithm.calculatePositions(images, config);
        
        // Store as current positions if not transitioning
        if (!isTransitioning) {
            currentPositions = copyPositions(positions);
        }
        
        return positions;
    }
    
    /**
     * Start an animated transition to new layout positions.
     * 
     * @param newPositions Target positions for the transition
     * @param duration Duration of transition in milliseconds
     */
    public void animateToNewLayout(ImagePosition[] newPositions, float duration) {
        if (newPositions == null || newPositions.length == 0) {
            return;
        }
        
        // Store current positions as starting point
        if (currentPositions.length != newPositions.length) {
            // If different number of images, create matching array
            currentPositions = new ImagePosition[newPositions.length];
            for (int i = 0; i < newPositions.length; i++) {
                currentPositions[i] = new ImagePosition();
            }
        }
        
        // Set up transition
        targetPositions = copyPositions(newPositions);
        transitionDuration = Math.max(100.0f, duration); // Minimum 100ms
        transitionStartTime = System.currentTimeMillis();
        isTransitioning = true;
        transitionProgress = 0.0f;
    }
    
    /**
     * Update the layout engine each frame.
     * Handles ongoing transitions and algorithm updates.
     * 
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    public void update(float deltaTime) {
        if (isTransitioning) {
            updateTransition(deltaTime);
        }
        
        // Update algorithm-specific animations
        if (currentAlgorithm != null && currentPositions != null) {
            currentAlgorithm.updatePositions(currentPositions, deltaTime);
        }
    }
    
    /**
     * Get the current positions (including any transition interpolation).
     * 
     * @return Array of current image positions
     */
    public ImagePosition[] getCurrentPositions() {
        return copyPositions(currentPositions);
    }
    
    /**
     * Check if a layout transition is currently active.
     * 
     * @return true if transitioning, false otherwise
     */
    public boolean isTransitioning() {
        return isTransitioning;
    }
    
    /**
     * Get the progress of the current transition.
     * 
     * @return Transition progress from 0.0 to 1.0
     */
    public float getTransitionProgress() {
        return transitionProgress;
    }
    
    /**
     * Set the duration for layout transitions.
     * 
     * @param duration Duration in milliseconds
     */
    public void setTransitionDuration(float duration) {
        this.transitionDuration = Math.max(100.0f, duration);
    }
    
    /**
     * Force completion of any ongoing transition.
     */
    public void completeTransition() {
        if (isTransitioning && targetPositions != null) {
            currentPositions = copyPositions(targetPositions);
            isTransitioning = false;
            transitionProgress = 1.0f;
        }
    }
    
    /**
     * Update the current transition state.
     * 
     * @param deltaTime Time elapsed since last update
     */
    private void updateTransition(float deltaTime) {
        if (!isTransitioning || targetPositions == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        float elapsed = currentTime - transitionStartTime;
        transitionProgress = elapsed / transitionDuration;
        
        if (transitionProgress >= 1.0f) {
            // Transition complete
            transitionProgress = 1.0f;
            currentPositions = copyPositions(targetPositions);
            isTransitioning = false;
        } else {
            // Interpolate between current and target positions
            interpolatePositions(transitionProgress);
        }
    }
    
    /**
     * Interpolate between current and target positions.
     * 
     * @param progress Interpolation progress (0.0 to 1.0)
     */
    private void interpolatePositions(float progress) {
        if (currentPositions == null || targetPositions == null) {
            return;
        }
        
        // Apply easing function (ease-out for smooth deceleration)
        float easedProgress = easeOut(progress);
        
        int count = Math.min(currentPositions.length, targetPositions.length);
        for (int i = 0; i < count; i++) {
            ImagePosition current = currentPositions[i];
            ImagePosition target = targetPositions[i];
            
            if (current != null && target != null) {
                // Interpolate position
                current.x = lerp(current.x, target.x, easedProgress);
                current.y = lerp(current.y, target.y, easedProgress);
                
                // Interpolate dimensions
                current.width = lerp(current.width, target.width, easedProgress);
                current.height = lerp(current.height, target.height, easedProgress);
                
                // Interpolate transformations
                current.rotation = lerpAngle(current.rotation, target.rotation, easedProgress);
                current.scale = lerp(current.scale, target.scale, easedProgress);
                current.opacity = lerp(current.opacity, target.opacity, easedProgress);
                
                current.timestamp = System.currentTimeMillis();
            }
        }
    }
    
    /**
     * Linear interpolation between two values.
     * 
     * @param start Starting value
     * @param end Ending value
     * @param progress Progress from 0.0 to 1.0
     * @return Interpolated value
     */
    private float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }
    
    /**
     * Linear interpolation for angles, handling wrap-around.
     * 
     * @param startAngle Starting angle in degrees
     * @param endAngle Ending angle in degrees
     * @param progress Progress from 0.0 to 1.0
     * @return Interpolated angle
     */
    private float lerpAngle(float startAngle, float endAngle, float progress) {
        // Normalize angles to 0-360 range
        startAngle = ((startAngle % 360) + 360) % 360;
        endAngle = ((endAngle % 360) + 360) % 360;
        
        // Find shortest path
        float diff = endAngle - startAngle;
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }
        
        return startAngle + diff * progress;
    }
    
    /**
     * Ease-out function for smooth deceleration.
     * 
     * @param t Input value (0.0 to 1.0)
     * @return Eased value (0.0 to 1.0)
     */
    private float easeOut(float t) {
        return 1.0f - (1.0f - t) * (1.0f - t);
    }
    
    /**
     * Create a deep copy of an array of ImagePositions.
     * 
     * @param positions Array to copy
     * @return New array with copied positions
     */
    private ImagePosition[] copyPositions(ImagePosition[] positions) {
        if (positions == null) {
            return null;
        }
        
        ImagePosition[] copy = new ImagePosition[positions.length];
        for (int i = 0; i < positions.length; i++) {
            copy[i] = positions[i] != null ? positions[i].copy() : null;
        }
        return copy;
    }
}