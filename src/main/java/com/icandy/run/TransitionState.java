package com.icandy.run;

/**
 * Represents the state of an active transition.
 * Tracks the progress and timing of a transition effect.
 */
public class TransitionState {
    private boolean isActive;
    private TransitionEffect effect;
    private ImageState startState;
    private ImageState targetState;
    private float startTime;
    private float duration;
    private float progress;
    private ImageInfo imageInfo;
    
    /**
     * Creates a new inactive transition state.
     */
    public TransitionState() {
        this.isActive = false;
        this.progress = 0.0f;
    }
    
    /**
     * Creates a new transition state for a specific image.
     * 
     * @param imageInfo The image being transitioned
     * @param effect The transition effect to use
     * @param startState The starting image state
     * @param targetState The target image state
     * @param duration The transition duration in milliseconds
     * @param currentTime The current time in milliseconds
     */
    public TransitionState(ImageInfo imageInfo, TransitionEffect effect, ImageState startState, 
                          ImageState targetState, float duration, float currentTime) {
        this.imageInfo = imageInfo;
        this.effect = effect;
        this.startState = new ImageState(startState);
        this.targetState = new ImageState(targetState);
        this.duration = Math.max(1.0f, duration);
        this.startTime = currentTime;
        this.progress = 0.0f;
        this.isActive = true;
        
        if (effect != null) {
            effect.startTransition(startState, targetState, duration);
        }
    }
    
    /**
     * Updates the transition state based on current time.
     * 
     * @param currentTime The current time in milliseconds
     * @return The current interpolated image state
     */
    public ImageState update(float currentTime) {
        if (!isActive || effect == null) {
            return targetState != null ? new ImageState(targetState) : new ImageState();
        }
        
        float elapsed = currentTime - startTime;
        progress = Math.min(1.0f, elapsed / duration);
        
        if (progress >= 1.0f) {
            isActive = false;
            return new ImageState(targetState);
        }
        
        return effect.updateTransition(progress);
    }
    
    /**
     * Checks if the transition is complete.
     * 
     * @return true if the transition has finished
     */
    public boolean isComplete() {
        return !isActive || progress >= 1.0f || (effect != null && effect.isComplete());
    }
    
    /**
     * Stops the transition immediately.
     */
    public void stop() {
        isActive = false;
        progress = 1.0f;
    }
    
    // Getters
    public boolean isActive() { return isActive; }
    public TransitionEffect getEffect() { return effect; }
    public ImageState getStartState() { return startState; }
    public ImageState getTargetState() { return targetState; }
    public float getStartTime() { return startTime; }
    public float getDuration() { return duration; }
    public float getProgress() { return progress; }
    public ImageInfo getImageInfo() { return imageInfo; }
    
    // Setters
    public void setActive(boolean active) { this.isActive = active; }
    public void setEffect(TransitionEffect effect) { this.effect = effect; }
    public void setStartState(ImageState startState) { this.startState = startState; }
    public void setTargetState(ImageState targetState) { this.targetState = targetState; }
    public void setDuration(float duration) { this.duration = Math.max(1.0f, duration); }
    public void setImageInfo(ImageInfo imageInfo) { this.imageInfo = imageInfo; }
    
    @Override
    public String toString() {
        return String.format("TransitionState{active=%s, progress=%.2f, effect=%s, image=%s}",
                isActive, progress, effect != null ? effect.getName() : "none", 
                imageInfo != null ? imageInfo.word : "unknown");
    }
}