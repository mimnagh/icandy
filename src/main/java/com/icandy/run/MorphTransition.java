package com.icandy.run;

import com.icandy.run.TransitionConfig.BlendMode;

/**
 * Morph transition effect that blends between old and new images using shape interpolation.
 * Implements color blending and morphing effects with configurable blend modes.
 */
public class MorphTransition implements TransitionEffect {
    private ImageState startState;
    private ImageState targetState;
    private float duration;
    private EasingFunction easingFunction;
    private BlendMode blendMode;
    private float morphIntensity;
    private boolean enableVertexMorph;
    private boolean enableColorBlend;
    private boolean isComplete;
    
    /**
     * Creates a new morph transition with default settings.
     */
    public MorphTransition() {
        this(BlendMode.NORMAL, EasingFunction.EASE_IN_OUT, 1.0f);
    }
    
    /**
     * Creates a new morph transition with specified blend mode and intensity.
     * 
     * @param blendMode The blend mode to use for morphing
     * @param easingFunction The easing function to use
     * @param morphIntensity The intensity of the morph effect (0.0 to 1.0)
     */
    public MorphTransition(BlendMode blendMode, EasingFunction easingFunction, float morphIntensity) {
        this.blendMode = blendMode != null ? blendMode : BlendMode.NORMAL;
        this.easingFunction = easingFunction != null ? easingFunction : EasingFunction.EASE_IN_OUT;
        this.morphIntensity = Math.max(0.0f, Math.min(1.0f, morphIntensity));
        this.enableVertexMorph = true;
        this.enableColorBlend = true;
        this.isComplete = false;
    }
    
    @Override
    public void startTransition(ImageState from, ImageState to, float duration) {
        this.startState = from != null ? new ImageState(from) : new ImageState();
        this.targetState = to != null ? new ImageState(to) : new ImageState();
        this.duration = Math.max(1.0f, duration);
        this.isComplete = false;
        
        // For morph transitions, we start with the original state
        // and gradually transform to the target state
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
        
        // Create morphed state
        ImageState currentState = createMorphedState(easedProgress);
        
        // Mark as complete when progress reaches 1.0
        if (progress >= 1.0f) {
            isComplete = true;
        }
        
        return currentState;
    }
    
    /**
     * Creates a morphed state by blending between start and target states.
     * 
     * @param progress The morphing progress from 0.0 to 1.0
     * @return The morphed image state
     */
    private ImageState createMorphedState(float progress) {
        // Apply morph intensity to the progress
        float morphProgress = progress * morphIntensity;
        
        // Basic interpolation between states
        ImageState morphedState = startState.lerp(targetState, morphProgress);
        
        // Apply vertex morphing effects if enabled
        if (enableVertexMorph) {
            applyVertexMorphing(morphedState, progress);
        }
        
        // Apply color blending effects if enabled
        if (enableColorBlend) {
            applyColorBlending(morphedState, progress);
        }
        
        // Apply blend mode effects
        applyBlendModeEffects(morphedState, progress);
        
        return morphedState;
    }
    
    /**
     * Applies vertex morphing effects to create organic shape changes.
     * 
     * @param state The state to modify
     * @param progress The morphing progress
     */
    private void applyVertexMorphing(ImageState state, float progress) {
        // Simulate organic morphing by applying subtle size and position variations
        float morphWave = (float) Math.sin(progress * Math.PI * 2) * 0.1f * morphIntensity;
        
        // Apply wave-like distortion to dimensions
        float widthMorph = 1.0f + morphWave * (float) Math.sin(progress * Math.PI * 3);
        float heightMorph = 1.0f + morphWave * (float) Math.cos(progress * Math.PI * 3);
        
        state.setWidth(state.getWidth() * widthMorph);
        state.setHeight(state.getHeight() * heightMorph);
        
        // Apply subtle position morphing
        float positionMorph = morphWave * 5.0f;
        state.setX(state.getX() + positionMorph * (float) Math.sin(progress * Math.PI * 4));
        state.setY(state.getY() + positionMorph * (float) Math.cos(progress * Math.PI * 4));
    }
    
    /**
     * Applies color blending effects during morphing.
     * 
     * @param state The state to modify
     * @param progress The morphing progress
     */
    private void applyColorBlending(ImageState state, float progress) {
        // Simulate color morphing by modulating opacity in waves
        float colorWave = (float) Math.sin(progress * Math.PI * 6) * 0.2f * morphIntensity;
        float baseOpacity = state.getOpacity();
        
        // Apply color blending through opacity modulation
        float morphedOpacity = baseOpacity + colorWave;
        morphedOpacity = Math.max(0.0f, Math.min(1.0f, morphedOpacity));
        
        state.setOpacity(morphedOpacity);
    }
    
    /**
     * Applies blend mode specific effects during morphing.
     * 
     * @param state The state to modify
     * @param progress The morphing progress
     */
    private void applyBlendModeEffects(ImageState state, float progress) {
        switch (blendMode) {
            case MULTIPLY:
                // Darken effect during morph
                state.setOpacity(state.getOpacity() * (0.7f + 0.3f * progress));
                break;
                
            case SCREEN:
                // Brighten effect during morph
                state.setOpacity(Math.min(1.0f, state.getOpacity() * (1.0f + 0.3f * (1.0f - progress))));
                break;
                
            case OVERLAY:
                // Contrast effect during morph
                float contrastFactor = 1.0f + (float) Math.sin(progress * Math.PI) * 0.3f * morphIntensity;
                state.setScaleX(state.getScaleX() * contrastFactor);
                state.setScaleY(state.getScaleY() * contrastFactor);
                break;
                
            case SOFT_LIGHT:
                // Soft glow effect during morph
                float glowFactor = 1.0f + (float) Math.sin(progress * Math.PI) * 0.1f * morphIntensity;
                state.setScaleX(state.getScaleX() * glowFactor);
                state.setScaleY(state.getScaleY() * glowFactor);
                state.setOpacity(state.getOpacity() * (0.9f + 0.1f * (float) Math.sin(progress * Math.PI)));
                break;
                
            case HARD_LIGHT:
                // Sharp contrast effect during morph
                if (progress < 0.5f) {
                    state.setOpacity(state.getOpacity() * (0.5f + progress));
                } else {
                    state.setOpacity(state.getOpacity() * (1.5f - progress));
                }
                break;
                
            case COLOR_DODGE:
                // Bright highlight effect during morph
                state.setOpacity(Math.min(1.0f, state.getOpacity() * (1.0f + progress * 0.5f)));
                break;
                
            case COLOR_BURN:
                // Dark shadow effect during morph
                state.setOpacity(state.getOpacity() * (1.0f - progress * 0.3f));
                break;
                
            case NORMAL:
            default:
                // No special blend mode effects
                break;
        }
    }
    
    @Override
    public boolean isComplete() {
        return isComplete;
    }
    
    @Override
    public String getName() {
        return "Morph";
    }
    
    /**
     * Sets the blend mode for morphing.
     * 
     * @param blendMode The new blend mode
     */
    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode != null ? blendMode : BlendMode.NORMAL;
    }
    
    /**
     * Gets the current blend mode.
     * 
     * @return The current blend mode
     */
    public BlendMode getBlendMode() {
        return blendMode;
    }
    
    /**
     * Sets the morph intensity.
     * 
     * @param morphIntensity The intensity (0.0 to 1.0)
     */
    public void setMorphIntensity(float morphIntensity) {
        this.morphIntensity = Math.max(0.0f, Math.min(1.0f, morphIntensity));
    }
    
    /**
     * Gets the current morph intensity.
     * 
     * @return The morph intensity
     */
    public float getMorphIntensity() {
        return morphIntensity;
    }
    
    /**
     * Sets the easing function for this morph transition.
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
     * Enables or disables vertex morphing.
     * 
     * @param enableVertexMorph Whether to enable vertex morphing
     */
    public void setEnableVertexMorph(boolean enableVertexMorph) {
        this.enableVertexMorph = enableVertexMorph;
    }
    
    /**
     * Checks if vertex morphing is enabled.
     * 
     * @return true if vertex morphing is enabled
     */
    public boolean isEnableVertexMorph() {
        return enableVertexMorph;
    }
    
    /**
     * Enables or disables color blending.
     * 
     * @param enableColorBlend Whether to enable color blending
     */
    public void setEnableColorBlend(boolean enableColorBlend) {
        this.enableColorBlend = enableColorBlend;
    }
    
    /**
     * Checks if color blending is enabled.
     * 
     * @return true if color blending is enabled
     */
    public boolean isEnableColorBlend() {
        return enableColorBlend;
    }
    
    @Override
    public String toString() {
        return String.format("MorphTransition{blend=%s, intensity=%.2f, easing=%s, vertex=%s, color=%s, complete=%s}",
                blendMode, morphIntensity, easingFunction, enableVertexMorph, enableColorBlend, isComplete);
    }
}