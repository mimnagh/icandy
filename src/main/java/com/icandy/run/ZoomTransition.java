package com.icandy.run;

import com.icandy.run.TransitionConfig.ZoomMode;

/**
 * Zoom transition effect that scales images in or out during swaps.
 * Supports zoom in, zoom out, and combined zoom effects with configurable center points.
 */
public class ZoomTransition implements TransitionEffect {
    private ImageState startState;
    private ImageState targetState;
    private float duration;
    private EasingFunction easingFunction;
    private ZoomMode zoomMode;
    private float zoomCenterX;
    private float zoomCenterY;
    private boolean combineFade;
    private boolean isComplete;
    
    /**
     * Creates a new zoom transition with default settings.
     */
    public ZoomTransition() {
        this(ZoomMode.ZOOM_IN, EasingFunction.EASE_IN_OUT, true);
    }
    
    /**
     * Creates a new zoom transition with specified mode and easing.
     * 
     * @param zoomMode The zoom mode (in, out, or both)
     * @param easingFunction The easing function to use
     * @param combineFade Whether to combine with fade effect
     */
    public ZoomTransition(ZoomMode zoomMode, EasingFunction easingFunction, boolean combineFade) {
        this.zoomMode = zoomMode != null ? zoomMode : ZoomMode.ZOOM_IN;
        this.easingFunction = easingFunction != null ? easingFunction : EasingFunction.EASE_IN_OUT;
        this.combineFade = combineFade;
        this.isComplete = false;
        this.zoomCenterX = 0.5f; // Center of image (0.0 to 1.0)
        this.zoomCenterY = 0.5f;
    }
    
    /**
     * Sets the zoom center point as a fraction of the image dimensions.
     * 
     * @param centerX X center (0.0 = left edge, 1.0 = right edge)
     * @param centerY Y center (0.0 = top edge, 1.0 = bottom edge)
     */
    public void setZoomCenter(float centerX, float centerY) {
        this.zoomCenterX = Math.max(0.0f, Math.min(1.0f, centerX));
        this.zoomCenterY = Math.max(0.0f, Math.min(1.0f, centerY));
    }
    
    @Override
    public void startTransition(ImageState from, ImageState to, float duration) {
        this.targetState = to != null ? new ImageState(to) : new ImageState();
        this.duration = Math.max(1.0f, duration);
        this.isComplete = false;
        
        // Calculate the starting state based on zoom mode
        this.startState = calculateStartState(targetState, zoomMode);
    }
    
    /**
     * Calculates the starting state based on zoom mode.
     * 
     * @param targetState The final target state
     * @param mode The zoom mode
     * @return The calculated starting state
     */
    private ImageState calculateStartState(ImageState targetState, ZoomMode mode) {
        ImageState startState = new ImageState(targetState);
        
        switch (mode) {
            case ZOOM_IN:
                // Start with zero scale (invisible) and zoom in
                startState.setScaleX(0.0f);
                startState.setScaleY(0.0f);
                if (combineFade) {
                    startState.setOpacity(0.0f);
                }
                break;
                
            case ZOOM_OUT:
                // Start with large scale and zoom out
                startState.setScaleX(targetState.getScaleX() * 2.0f);
                startState.setScaleY(targetState.getScaleY() * 2.0f);
                if (combineFade) {
                    startState.setOpacity(0.5f);
                }
                break;
                
            case ZOOM_BOTH:
                // Start with zero scale, zoom to overshoot, then settle
                startState.setScaleX(0.0f);
                startState.setScaleY(0.0f);
                if (combineFade) {
                    startState.setOpacity(0.0f);
                }
                break;
        }
        
        // Adjust position to maintain zoom center point
        adjustPositionForZoomCenter(startState, targetState);
        
        return startState;
    }
    
    /**
     * Adjusts the position to maintain the specified zoom center point.
     * 
     * @param currentState The state to adjust
     * @param targetState The target state for reference
     */
    private void adjustPositionForZoomCenter(ImageState currentState, ImageState targetState) {
        // Calculate the offset needed to maintain zoom center
        float targetCenterX = targetState.getX() + targetState.getWidth() * zoomCenterX;
        float targetCenterY = targetState.getY() + targetState.getHeight() * zoomCenterY;
        
        float currentCenterX = currentState.getX() + currentState.getWidth() * currentState.getScaleX() * zoomCenterX;
        float currentCenterY = currentState.getY() + currentState.getHeight() * currentState.getScaleY() * zoomCenterY;
        
        // Adjust position to align centers
        currentState.setX(currentState.getX() + (targetCenterX - currentCenterX));
        currentState.setY(currentState.getY() + (targetCenterY - currentCenterY));
    }
    
    @Override
    public ImageState updateTransition(float progress) {
        if (startState == null || targetState == null) {
            return new ImageState();
        }
        
        // Clamp progress to valid range
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        
        // Apply easing function
        float easedProgress = easingFunction.apply(progress);
        
        // Handle different zoom modes
        ImageState currentState = calculateCurrentState(easedProgress);
        
        // Mark as complete when progress reaches 1.0
        if (progress >= 1.0f) {
            isComplete = true;
        }
        
        return currentState;
    }
    
    /**
     * Calculates the current state based on eased progress and zoom mode.
     * 
     * @param easedProgress The eased progress value
     * @return The current interpolated state
     */
    private ImageState calculateCurrentState(float easedProgress) {
        ImageState currentState;
        
        switch (zoomMode) {
            case ZOOM_IN:
            case ZOOM_OUT:
                // Simple linear interpolation
                currentState = startState.lerp(targetState, easedProgress);
                break;
                
            case ZOOM_BOTH:
                // Two-phase animation: zoom in with overshoot, then settle
                if (easedProgress < 0.7f) {
                    // Phase 1: Zoom in with overshoot
                    float phase1Progress = easedProgress / 0.7f;
                    ImageState overshootState = new ImageState(targetState);
                    overshootState.setScaleX(targetState.getScaleX() * 1.2f);
                    overshootState.setScaleY(targetState.getScaleY() * 1.2f);
                    if (combineFade) {
                        overshootState.setOpacity(1.0f);
                    }
                    currentState = startState.lerp(overshootState, phase1Progress);
                } else {
                    // Phase 2: Settle to target
                    float phase2Progress = (easedProgress - 0.7f) / 0.3f;
                    ImageState overshootState = new ImageState(targetState);
                    overshootState.setScaleX(targetState.getScaleX() * 1.2f);
                    overshootState.setScaleY(targetState.getScaleY() * 1.2f);
                    currentState = overshootState.lerp(targetState, phase2Progress);
                }
                break;
                
            default:
                currentState = startState.lerp(targetState, easedProgress);
                break;
        }
        
        // Adjust position to maintain zoom center
        adjustPositionForZoomCenter(currentState, targetState);
        
        return currentState;
    }
    
    @Override
    public boolean isComplete() {
        return isComplete;
    }
    
    @Override
    public String getName() {
        return "Zoom";
    }
    
    /**
     * Sets the zoom mode.
     * 
     * @param zoomMode The new zoom mode
     */
    public void setZoomMode(ZoomMode zoomMode) {
        this.zoomMode = zoomMode != null ? zoomMode : ZoomMode.ZOOM_IN;
    }
    
    /**
     * Gets the current zoom mode.
     * 
     * @return The current zoom mode
     */
    public ZoomMode getZoomMode() {
        return zoomMode;
    }
    
    /**
     * Sets the easing function for this zoom transition.
     * 
     * @param easingFunction The new easing function
     */
    public void setEasingFunction(EasingFunction easingFunction) {
        this.easingFunction = easingFunction != null ? easingFunction : EasingFunction.LINEAR;
    }
    
    /**
     * Gets the current easing function.
     * 
     * @return The current easing function
     */
    public EasingFunction getEasingFunction() {
        return easingFunction;
    }
    
    /**
     * Sets whether to combine zoom with fade effect.
     * 
     * @param combineFade Whether to combine with fade
     */
    public void setCombineFade(boolean combineFade) {
        this.combineFade = combineFade;
    }
    
    /**
     * Checks if zoom is combined with fade effect.
     * 
     * @return true if combined with fade
     */
    public boolean isCombineFade() {
        return combineFade;
    }
    
    @Override
    public String toString() {
        return String.format("ZoomTransition{mode=%s, easing=%s, fade=%s, center=(%.2f,%.2f), complete=%s}",
                zoomMode, easingFunction, combineFade, zoomCenterX, zoomCenterY, isComplete);
    }
}