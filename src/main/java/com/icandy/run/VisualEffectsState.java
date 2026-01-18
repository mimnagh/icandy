package com.icandy.run;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the current visual effects state for an image.
 * Contains information about applied effects and their parameters.
 */
public class VisualEffectsState {
    
    private float blurRadius;
    private VisualEffectsConfig.ColorFilterType colorFilter;
    private float colorIntensity;
    private int tintColor;
    private float brightness;
    private float contrast;
    private float gamma;
    private boolean hasParticles;
    private List<Particle> particles;
    private boolean hasGlow;
    private float glowRadius;
    private int glowColor;
    private boolean hasShadow;
    private float shadowOffsetX;
    private float shadowOffsetY;
    private float shadowBlur;
    private int shadowColor;
    private boolean hasOutline;
    private float outlineThickness;
    private int outlineColor;
    private long effectsAppliedTime;
    
    /**
     * Creates a new visual effects state with default values.
     */
    public VisualEffectsState() {
        this.blurRadius = 0.0f;
        this.colorFilter = VisualEffectsConfig.ColorFilterType.NONE;
        this.colorIntensity = 1.0f;
        this.tintColor = 0xFFFFFFFF;
        this.brightness = 0.0f;
        this.contrast = 0.0f;
        this.gamma = 1.0f;
        this.hasParticles = false;
        this.particles = new ArrayList<>();
        this.hasGlow = false;
        this.glowRadius = 0.0f;
        this.glowColor = 0xFFFFFFFF;
        this.hasShadow = false;
        this.shadowOffsetX = 0.0f;
        this.shadowOffsetY = 0.0f;
        this.shadowBlur = 0.0f;
        this.shadowColor = 0x80000000;
        this.hasOutline = false;
        this.outlineThickness = 0.0f;
        this.outlineColor = 0xFFFFFFFF;
        this.effectsAppliedTime = System.currentTimeMillis();
    }
    
    /**
     * Creates a visual effects state from a configuration.
     * 
     * @param config The configuration to apply
     */
    public VisualEffectsState(VisualEffectsConfig config) {
        this();
        applyConfig(config);
    }
    
    /**
     * Applies a visual effects configuration to this state.
     * 
     * @param config The configuration to apply
     */
    public void applyConfig(VisualEffectsConfig config) {
        this.blurRadius = config.enableBlur ? config.blurRadius : 0.0f;
        this.colorFilter = config.colorFilter;
        this.colorIntensity = config.colorIntensity;
        this.tintColor = config.tintColor;
        this.brightness = config.brightness;
        this.contrast = config.contrast;
        this.gamma = config.gamma;
        this.hasParticles = config.enableParticles;
        this.hasGlow = config.enableGlow;
        this.glowRadius = config.enableGlow ? config.glowRadius : 0.0f;
        this.glowColor = config.glowColor;
        this.hasShadow = config.enableShadow;
        this.shadowOffsetX = config.enableShadow ? config.shadowOffsetX : 0.0f;
        this.shadowOffsetY = config.enableShadow ? config.shadowOffsetY : 0.0f;
        this.shadowBlur = config.enableShadow ? config.shadowBlur : 0.0f;
        this.shadowColor = config.shadowColor;
        this.hasOutline = config.enableOutline;
        this.outlineThickness = config.enableOutline ? config.outlineThickness : 0.0f;
        this.outlineColor = config.outlineColor;
        this.effectsAppliedTime = System.currentTimeMillis();
    }
    
    // Getters
    public float getBlurRadius() { return blurRadius; }
    public VisualEffectsConfig.ColorFilterType getColorFilter() { return colorFilter; }
    public float getColorIntensity() { return colorIntensity; }
    public int getTintColor() { return tintColor; }
    public float getBrightness() { return brightness; }
    public float getContrast() { return contrast; }
    public float getGamma() { return gamma; }
    public boolean hasParticles() { return hasParticles; }
    public List<Particle> getParticles() { return particles; }
    public boolean hasGlow() { return hasGlow; }
    public float getGlowRadius() { return glowRadius; }
    public int getGlowColor() { return glowColor; }
    public boolean hasShadow() { return hasShadow; }
    public float getShadowOffsetX() { return shadowOffsetX; }
    public float getShadowOffsetY() { return shadowOffsetY; }
    public float getShadowBlur() { return shadowBlur; }
    public int getShadowColor() { return shadowColor; }
    public boolean hasOutline() { return hasOutline; }
    public float getOutlineThickness() { return outlineThickness; }
    public int getOutlineColor() { return outlineColor; }
    public long getEffectsAppliedTime() { return effectsAppliedTime; }
    
    // Setters
    public void setBlurRadius(float blurRadius) { 
        this.blurRadius = Math.max(0.0f, blurRadius); 
    }
    
    public void setColorFilter(VisualEffectsConfig.ColorFilterType colorFilter) { 
        this.colorFilter = colorFilter != null ? colorFilter : VisualEffectsConfig.ColorFilterType.NONE; 
    }
    
    public void setColorIntensity(float colorIntensity) { 
        this.colorIntensity = Math.max(0.0f, Math.min(2.0f, colorIntensity)); 
    }
    
    public void setTintColor(int tintColor) { 
        this.tintColor = tintColor; 
    }
    
    public void setBrightness(float brightness) { 
        this.brightness = Math.max(-1.0f, Math.min(1.0f, brightness)); 
    }
    
    public void setContrast(float contrast) { 
        this.contrast = Math.max(-1.0f, Math.min(1.0f, contrast)); 
    }
    
    public void setGamma(float gamma) { 
        this.gamma = Math.max(0.1f, Math.min(3.0f, gamma)); 
    }
    
    public void setHasParticles(boolean hasParticles) { 
        this.hasParticles = hasParticles; 
    }
    
    public void setParticles(List<Particle> particles) { 
        this.particles = particles != null ? particles : new ArrayList<>(); 
    }
    
    public void setGlow(boolean hasGlow, float glowRadius, int glowColor) {
        this.hasGlow = hasGlow;
        this.glowRadius = Math.max(0.0f, glowRadius);
        this.glowColor = glowColor;
    }
    
    public void setShadow(boolean hasShadow, float offsetX, float offsetY, float blur, int color) {
        this.hasShadow = hasShadow;
        this.shadowOffsetX = offsetX;
        this.shadowOffsetY = offsetY;
        this.shadowBlur = Math.max(0.0f, blur);
        this.shadowColor = color;
    }
    
    public void setOutline(boolean hasOutline, float thickness, int color) {
        this.hasOutline = hasOutline;
        this.outlineThickness = Math.max(0.0f, thickness);
        this.outlineColor = color;
    }
    
    /**
     * Adds a particle to this effects state.
     * 
     * @param particle The particle to add
     */
    public void addParticle(Particle particle) {
        if (particle != null) {
            particles.add(particle);
        }
    }
    
    /**
     * Removes dead particles from the particle list.
     */
    public void removeDeadParticles() {
        particles.removeIf(Particle::isDead);
    }
    
    /**
     * Updates all particles in this effects state.
     * 
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    public void updateParticles(float deltaTime) {
        for (Particle particle : particles) {
            particle.update(deltaTime);
        }
        removeDeadParticles();
    }
    
    /**
     * Checks if any visual effects are currently active.
     * 
     * @return true if any effects are active, false otherwise
     */
    public boolean hasActiveEffects() {
        return blurRadius > 0 || 
               colorFilter != VisualEffectsConfig.ColorFilterType.NONE ||
               brightness != 0.0f || contrast != 0.0f || gamma != 1.0f ||
               hasParticles || hasGlow || hasShadow || hasOutline;
    }
    
    /**
     * Resets all effects to their default (disabled) state.
     */
    public void reset() {
        this.blurRadius = 0.0f;
        this.colorFilter = VisualEffectsConfig.ColorFilterType.NONE;
        this.colorIntensity = 1.0f;
        this.tintColor = 0xFFFFFFFF;
        this.brightness = 0.0f;
        this.contrast = 0.0f;
        this.gamma = 1.0f;
        this.hasParticles = false;
        this.particles.clear();
        this.hasGlow = false;
        this.glowRadius = 0.0f;
        this.hasShadow = false;
        this.shadowOffsetX = 0.0f;
        this.shadowOffsetY = 0.0f;
        this.shadowBlur = 0.0f;
        this.hasOutline = false;
        this.outlineThickness = 0.0f;
        this.effectsAppliedTime = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return String.format("VisualEffectsState{blur=%.1f, filter=%s, brightness=%.2f, contrast=%.2f, particles=%d, glow=%s, shadow=%s, outline=%s}",
                blurRadius, colorFilter, brightness, contrast, particles.size(), hasGlow, hasShadow, hasOutline);
    }
}