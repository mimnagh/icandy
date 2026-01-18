package com.icandy.run;

/**
 * Configuration class for visual effects parameters.
 * Contains settings for all supported visual effects including blur, color filters,
 * brightness/contrast, particle systems, and border effects.
 */
public class VisualEffectsConfig {
    
    // Blur settings
    public boolean enableBlur = false;
    public float blurRadius = 2.0f;
    public boolean enableMotionBlur = false;
    public boolean enableSelectiveBlur = false;
    
    // Color filter settings
    public ColorFilterType colorFilter = ColorFilterType.NONE;
    public float colorIntensity = 1.0f;
    public int tintColor = 0xFFFFFFFF; // White tint by default
    
    // Brightness/contrast settings
    public float brightness = 0.0f; // -1.0 to 1.0
    public float contrast = 0.0f;   // -1.0 to 1.0
    public float gamma = 1.0f;      // 0.1 to 3.0
    
    // Particle system settings
    public boolean enableParticles = false;
    public int particleCount = 50;
    public ParticleType particleType = ParticleType.SPARKLES;
    public int particleColor = 0xFFFFD700; // Gold color
    public float particleLifetime = 2000.0f; // milliseconds
    public float particleSize = 3.0f;
    public float particleSpeed = 50.0f;
    
    // Border effects settings
    public boolean enableGlow = false;
    public float glowRadius = 10.0f;
    public int glowColor = 0xFFFFFFFF; // White glow
    public boolean enableShadow = false;
    public float shadowOffsetX = 5.0f;
    public float shadowOffsetY = 5.0f;
    public float shadowBlur = 3.0f;
    public int shadowColor = 0x80000000; // Semi-transparent black
    public boolean enableOutline = false;
    public float outlineThickness = 2.0f;
    public int outlineColor = 0xFFFFFFFF; // White outline
    
    /**
     * Enumeration of supported color filter types.
     */
    public enum ColorFilterType {
        NONE,
        SEPIA,
        GRAYSCALE,
        VINTAGE,
        HIGH_CONTRAST,
        POSTERIZE
    }
    
    /**
     * Enumeration of supported particle types.
     */
    public enum ParticleType {
        SPARKLES,
        SMOKE,
        FIRE,
        SNOW,
        STARS,
        BUBBLES
    }
    
    /**
     * Creates a copy of this configuration.
     * 
     * @return A new VisualEffectsConfig with the same values
     */
    public VisualEffectsConfig copy() {
        VisualEffectsConfig copy = new VisualEffectsConfig();
        
        // Blur settings
        copy.enableBlur = this.enableBlur;
        copy.blurRadius = this.blurRadius;
        copy.enableMotionBlur = this.enableMotionBlur;
        copy.enableSelectiveBlur = this.enableSelectiveBlur;
        
        // Color filter settings
        copy.colorFilter = this.colorFilter;
        copy.colorIntensity = this.colorIntensity;
        copy.tintColor = this.tintColor;
        
        // Brightness/contrast settings
        copy.brightness = this.brightness;
        copy.contrast = this.contrast;
        copy.gamma = this.gamma;
        
        // Particle system settings
        copy.enableParticles = this.enableParticles;
        copy.particleCount = this.particleCount;
        copy.particleType = this.particleType;
        copy.particleColor = this.particleColor;
        copy.particleLifetime = this.particleLifetime;
        copy.particleSize = this.particleSize;
        copy.particleSpeed = this.particleSpeed;
        
        // Border effects settings
        copy.enableGlow = this.enableGlow;
        copy.glowRadius = this.glowRadius;
        copy.glowColor = this.glowColor;
        copy.enableShadow = this.enableShadow;
        copy.shadowOffsetX = this.shadowOffsetX;
        copy.shadowOffsetY = this.shadowOffsetY;
        copy.shadowBlur = this.shadowBlur;
        copy.shadowColor = this.shadowColor;
        copy.enableOutline = this.enableOutline;
        copy.outlineThickness = this.outlineThickness;
        copy.outlineColor = this.outlineColor;
        
        return copy;
    }
    
    /**
     * Validates and clamps configuration values to valid ranges.
     */
    public void validate() {
        // Clamp blur radius
        blurRadius = Math.max(0.0f, Math.min(50.0f, blurRadius));
        
        // Clamp color intensity
        colorIntensity = Math.max(0.0f, Math.min(2.0f, colorIntensity));
        
        // Clamp brightness and contrast
        brightness = Math.max(-1.0f, Math.min(1.0f, brightness));
        contrast = Math.max(-1.0f, Math.min(1.0f, contrast));
        gamma = Math.max(0.1f, Math.min(3.0f, gamma));
        
        // Clamp particle settings
        particleCount = Math.max(0, Math.min(1000, particleCount));
        particleLifetime = Math.max(100.0f, Math.min(10000.0f, particleLifetime));
        particleSize = Math.max(0.5f, Math.min(20.0f, particleSize));
        particleSpeed = Math.max(0.0f, Math.min(500.0f, particleSpeed));
        
        // Clamp glow and shadow settings
        glowRadius = Math.max(0.0f, Math.min(50.0f, glowRadius));
        shadowBlur = Math.max(0.0f, Math.min(20.0f, shadowBlur));
        outlineThickness = Math.max(0.0f, Math.min(10.0f, outlineThickness));
    }
    
    @Override
    public String toString() {
        return String.format("VisualEffectsConfig{blur=%s(%.1f), colorFilter=%s(%.1f), brightness=%.2f, contrast=%.2f, particles=%s(%d), glow=%s(%.1f), shadow=%s, outline=%s}",
                enableBlur, blurRadius, colorFilter, colorIntensity, brightness, contrast,
                enableParticles, particleCount, enableGlow, glowRadius, enableShadow, enableOutline);
    }
}