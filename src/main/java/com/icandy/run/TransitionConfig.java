package com.icandy.run;

/**
 * Configuration class for transition effects.
 * Contains parameters that control how transitions behave.
 */
public class TransitionConfig {
    private float duration;
    private EasingFunction easingFunction;
    private float staggerDelay;
    private boolean enableStagger;
    
    // Effect-specific parameters
    private SlideDirection slideDirection;
    private ZoomMode zoomMode;
    private float rotationAngle;
    private BlendMode blendMode;
    
    /**
     * Creates a default transition configuration.
     */
    public TransitionConfig() {
        this.duration = 500.0f; // 500ms default
        this.easingFunction = EasingFunction.EASE_IN_OUT;
        this.staggerDelay = 50.0f; // 50ms stagger delay
        this.enableStagger = false;
        this.slideDirection = SlideDirection.LEFT;
        this.zoomMode = ZoomMode.ZOOM_IN;
        this.rotationAngle = (float) Math.PI / 4; // 45 degrees
        this.blendMode = BlendMode.NORMAL;
    }
    
    /**
     * Creates a transition configuration with specified duration and easing.
     * 
     * @param duration The transition duration in milliseconds
     * @param easingFunction The easing function to use
     */
    public TransitionConfig(float duration, EasingFunction easingFunction) {
        this();
        this.duration = Math.max(1.0f, duration);
        this.easingFunction = easingFunction != null ? easingFunction : EasingFunction.LINEAR;
    }
    
    // Getters
    public float getDuration() { return duration; }
    public EasingFunction getEasingFunction() { return easingFunction; }
    public float getStaggerDelay() { return staggerDelay; }
    public boolean isEnableStagger() { return enableStagger; }
    public SlideDirection getSlideDirection() { return slideDirection; }
    public ZoomMode getZoomMode() { return zoomMode; }
    public float getRotationAngle() { return rotationAngle; }
    public BlendMode getBlendMode() { return blendMode; }
    
    // Setters with validation
    public void setDuration(float duration) {
        this.duration = Math.max(1.0f, duration);
    }
    
    public void setEasingFunction(EasingFunction easingFunction) {
        this.easingFunction = easingFunction != null ? easingFunction : EasingFunction.LINEAR;
    }
    
    public void setStaggerDelay(float staggerDelay) {
        this.staggerDelay = Math.max(0.0f, staggerDelay);
    }
    
    public void setEnableStagger(boolean enableStagger) {
        this.enableStagger = enableStagger;
    }
    
    public void setSlideDirection(SlideDirection slideDirection) {
        this.slideDirection = slideDirection != null ? slideDirection : SlideDirection.LEFT;
    }
    
    public void setZoomMode(ZoomMode zoomMode) {
        this.zoomMode = zoomMode != null ? zoomMode : ZoomMode.ZOOM_IN;
    }
    
    public void setRotationAngle(float rotationAngle) {
        this.rotationAngle = rotationAngle;
    }
    
    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode != null ? blendMode : BlendMode.NORMAL;
    }
    
    /**
     * Creates a copy of this configuration.
     * 
     * @return A new TransitionConfig with the same values
     */
    public TransitionConfig copy() {
        TransitionConfig copy = new TransitionConfig();
        copy.duration = this.duration;
        copy.easingFunction = this.easingFunction;
        copy.staggerDelay = this.staggerDelay;
        copy.enableStagger = this.enableStagger;
        copy.slideDirection = this.slideDirection;
        copy.zoomMode = this.zoomMode;
        copy.rotationAngle = this.rotationAngle;
        copy.blendMode = this.blendMode;
        return copy;
    }
    
    @Override
    public String toString() {
        return String.format("TransitionConfig{duration=%.1f, easing=%s, stagger=%s, slideDir=%s, zoomMode=%s}",
                duration, easingFunction, enableStagger, slideDirection, zoomMode);
    }
    
    /**
     * Enumeration of slide directions for slide transitions.
     */
    public enum SlideDirection {
        UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
    }
    
    /**
     * Enumeration of zoom modes for zoom transitions.
     */
    public enum ZoomMode {
        ZOOM_IN,    // Scale from 0 to 1
        ZOOM_OUT,   // Scale from 1 to 0
        ZOOM_BOTH   // Scale from 0 to 1 then back to target scale
    }
    
    /**
     * Enumeration of blend modes for morph transitions.
     */
    public enum BlendMode {
        NORMAL, MULTIPLY, SCREEN, OVERLAY, SOFT_LIGHT, HARD_LIGHT, COLOR_DODGE, COLOR_BURN
    }
}