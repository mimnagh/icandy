package com.icandy.run;

import com.icandy.run.TransitionConfig.SlideDirection;

/**
 * Slide transition effect that moves images in from specified directions during swaps.
 * Supports configurable slide direction, bounce effects, and staggered timing.
 */
public class SlideTransition implements TransitionEffect {
    private ImageState startState;
    private ImageState targetState;
    private float duration;
    private EasingFunction easingFunction;
    private SlideDirection slideDirection;
    private boolean enableBounce;
    private boolean isComplete;
    private float screenWidth;
    private float screenHeight;
    
    /**
     * Creates a new slide transition with default settings.
     */
    public SlideTransition() {
        this(SlideDirection.LEFT, EasingFunction.EASE_OUT, false);
    }
    
    /**
     * Creates a new slide transition with specified direction and easing.
     * 
     * @param slideDirection The direction to slide from
     * @param easingFunction The easing function to use
     * @param enableBounce Whether to enable bounce effect
     */
    public SlideTransition(SlideDirection slideDirection, EasingFunction easingFunction, boolean enableBounce) {
        this.slideDirection = slideDirection != null ? slideDirection : SlideDirection.LEFT;
        this.easingFunction = easingFunction != null ? easingFunction : EasingFunction.EASE_OUT;
        this.enableBounce = enableBounce;
        this.isComplete = false;
        this.screenWidth = 800.0f; // Default screen dimensions
        this.screenHeight = 600.0f;
    }
    
    /**
     * Sets the screen dimensions for calculating slide distances.
     * 
     * @param width Screen width
     * @param height Screen height
     */
    public void setScreenDimensions(float width, float height) {
        this.screenWidth = Math.max(1.0f, width);
        this.screenHeight = Math.max(1.0f, height);
    }
    
    @Override
    public void startTransition(ImageState from, ImageState to, float duration) {
        this.targetState = to != null ? new ImageState(to) : new ImageState();
        this.duration = Math.max(1.0f, duration);
        this.isComplete = false;
        
        // Calculate the starting position based on slide direction
        this.startState = calculateStartPosition(targetState, slideDirection);
    }
    
    /**
     * Calculates the starting position for the slide based on direction.
     * 
     * @param targetState The final target state
     * @param direction The slide direction
     * @return The calculated starting state
     */
    private ImageState calculateStartPosition(ImageState targetState, SlideDirection direction) {
        ImageState startState = new ImageState(targetState);
        
        switch (direction) {
            case LEFT:
                startState.setX(targetState.getX() - screenWidth);
                break;
            case RIGHT:
                startState.setX(targetState.getX() + screenWidth);
                break;
            case UP:
                startState.setY(targetState.getY() - screenHeight);
                break;
            case DOWN:
                startState.setY(targetState.getY() + screenHeight);
                break;
            case UP_LEFT:
                startState.setX(targetState.getX() - screenWidth);
                startState.setY(targetState.getY() - screenHeight);
                break;
            case UP_RIGHT:
                startState.setX(targetState.getX() + screenWidth);
                startState.setY(targetState.getY() - screenHeight);
                break;
            case DOWN_LEFT:
                startState.setX(targetState.getX() - screenWidth);
                startState.setY(targetState.getY() + screenHeight);
                break;
            case DOWN_RIGHT:
                startState.setX(targetState.getX() + screenWidth);
                startState.setY(targetState.getY() + screenHeight);
                break;
        }
        
        return startState;
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
        
        // Apply bounce effect if enabled
        if (enableBounce && easedProgress > 0.8f) {
            easedProgress = applyBounceEffect(easedProgress);
        }
        
        // Interpolate between start and target states
        ImageState currentState = startState.lerp(targetState, easedProgress);
        
        // Mark as complete when progress reaches 1.0
        if (progress >= 1.0f) {
            isComplete = true;
        }
        
        return currentState;
    }
    
    /**
     * Applies a bounce effect to the easing progress.
     * 
     * @param progress The current eased progress
     * @return The progress with bounce effect applied
     */
    private float applyBounceEffect(float progress) {
        // Simple bounce effect: overshoot and settle back
        if (progress > 0.8f && progress < 1.0f) {
            float bouncePhase = (progress - 0.8f) / 0.2f; // 0 to 1 in the bounce phase
            float overshoot = (float) Math.sin(bouncePhase * Math.PI * 2) * 0.1f * (1.0f - bouncePhase);
            return progress + overshoot;
        }
        return progress;
    }
    
    @Override
    public boolean isComplete() {
        return isComplete;
    }
    
    @Override
    public String getName() {
        return "Slide";
    }
    
    /**
     * Sets the slide direction.
     * 
     * @param slideDirection The new slide direction
     */
    public void setSlideDirection(SlideDirection slideDirection) {
        this.slideDirection = slideDirection != null ? slideDirection : SlideDirection.LEFT;
    }
    
    /**
     * Gets the current slide direction.
     * 
     * @return The current slide direction
     */
    public SlideDirection getSlideDirection() {
        return slideDirection;
    }
    
    /**
     * Sets the easing function for this slide transition.
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
     * Enables or disables the bounce effect.
     * 
     * @param enableBounce Whether to enable bounce effect
     */
    public void setEnableBounce(boolean enableBounce) {
        this.enableBounce = enableBounce;
    }
    
    /**
     * Checks if bounce effect is enabled.
     * 
     * @return true if bounce effect is enabled
     */
    public boolean isEnableBounce() {
        return enableBounce;
    }
    
    @Override
    public String toString() {
        return String.format("SlideTransition{direction=%s, easing=%s, bounce=%s, complete=%s}",
                slideDirection, easingFunction, enableBounce, isComplete);
    }
}