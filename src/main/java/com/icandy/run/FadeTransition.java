package com.icandy.run;

/**
 * Fade transition effect that gradually changes image opacity during transitions.
 * Supports configurable fade duration and easing functions for smooth cross-fades.
 */
public class FadeTransition implements TransitionEffect {
    private ImageState startState;
    private ImageState targetState;
    private float duration;
    private EasingFunction easingFunction;
    private boolean isComplete;
    
    /**
     * Creates a new fade transition with default easing.
     */
    public FadeTransition() {
        this(EasingFunction.EASE_IN_OUT);
    }
    
    /**
     * Creates a new fade transition with specified easing function.
     * 
     * @param easingFunction The easing function to use for the fade
     */
    public FadeTransition(EasingFunction easingFunction) {
        this.easingFunction = easingFunction != null ? easingFunction : EasingFunction.LINEAR;
        this.isComplete = false;
    }
    
    @Override
    public void startTransition(ImageState from, ImageState to, float duration) {
        this.startState = from != null ? new ImageState(from) : new ImageState();
        this.targetState = to != null ? new ImageState(to) : new ImageState();
        this.duration = Math.max(1.0f, duration);
        this.isComplete = false;
        
        // For fade transitions, we start with the target position but fade opacity
        this.startState.setOpacity(0.0f); // Start fully transparent
        this.targetState.setOpacity(1.0f); // End fully opaque
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
        
        // Create interpolated state
        ImageState currentState = new ImageState(targetState);
        
        // For fade transition, we primarily animate opacity
        float currentOpacity = startState.getOpacity() + 
                              (targetState.getOpacity() - startState.getOpacity()) * easedProgress;
        currentState.setOpacity(currentOpacity);
        
        // Also interpolate position for smooth movement during fade
        float currentX = startState.getX() + (targetState.getX() - startState.getX()) * easedProgress;
        float currentY = startState.getY() + (targetState.getY() - startState.getY()) * easedProgress;
        currentState.setX(currentX);
        currentState.setY(currentY);
        
        // Interpolate scale for subtle size changes during fade
        float currentScaleX = startState.getScaleX() + (targetState.getScaleX() - startState.getScaleX()) * easedProgress;
        float currentScaleY = startState.getScaleY() + (targetState.getScaleY() - startState.getScaleY()) * easedProgress;
        currentState.setScaleX(currentScaleX);
        currentState.setScaleY(currentScaleY);
        
        // Mark as complete when progress reaches 1.0
        if (progress >= 1.0f) {
            isComplete = true;
        }
        
        return currentState;
    }
    
    @Override
    public boolean isComplete() {
        return isComplete;
    }
    
    @Override
    public String getName() {
        return "Fade";
    }
    
    /**
     * Sets the easing function for this fade transition.
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
     * Creates a cross-fade transition between two images.
     * This is a specialized fade that handles transitioning from one image to another.
     * 
     * @param fromState The state of the outgoing image
     * @param toState The state of the incoming image
     * @param duration The transition duration in milliseconds
     */
    public void startCrossFade(ImageState fromState, ImageState toState, float duration) {
        this.startState = fromState != null ? new ImageState(fromState) : new ImageState();
        this.targetState = toState != null ? new ImageState(toState) : new ImageState();
        this.duration = Math.max(1.0f, duration);
        this.isComplete = false;
        
        // For cross-fade, the outgoing image starts opaque and fades out
        // while the incoming image starts transparent and fades in
        this.startState.setOpacity(1.0f);
        this.targetState.setOpacity(1.0f);
    }
    
    /**
     * Updates a cross-fade transition and returns both the outgoing and incoming states.
     * 
     * @param progress The transition progress from 0.0 to 1.0
     * @return Array containing [outgoingState, incomingState]
     */
    public ImageState[] updateCrossFade(float progress) {
        if (startState == null || targetState == null) {
            return new ImageState[]{new ImageState(), new ImageState()};
        }
        
        // Clamp progress to valid range
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        
        // Apply easing function
        float easedProgress = easingFunction.apply(progress);
        
        // Create outgoing state (fading out)
        ImageState outgoingState = new ImageState(startState);
        outgoingState.setOpacity(1.0f - easedProgress);
        
        // Create incoming state (fading in)
        ImageState incomingState = new ImageState(targetState);
        incomingState.setOpacity(easedProgress);
        
        // Mark as complete when progress reaches 1.0
        if (progress >= 1.0f) {
            isComplete = true;
        }
        
        return new ImageState[]{outgoingState, incomingState};
    }
    
    @Override
    public String toString() {
        return String.format("FadeTransition{easing=%s, complete=%s}", easingFunction, isComplete);
    }
}