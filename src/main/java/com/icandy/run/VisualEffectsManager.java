package com.icandy.run;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * VisualEffectsManager applies visual effects and enhancements to images.
 * Supports blur effects, color filters, brightness/contrast adjustments,
 * particle systems, and border effects.
 */
public class VisualEffectsManager {
    
    private PApplet parent;
    private VisualEffectsConfig config;
    private Random random;
    private List<Particle> globalParticles;
    
    // Graphics buffers for effects
    private PGraphics effectBuffer;
    private PGraphics blurBuffer;
    private PGraphics glowBuffer;
    
    /**
     * Creates a new VisualEffectsManager.
     * 
     * @param parent The Processing PApplet instance
     */
    public VisualEffectsManager(PApplet parent) {
        this.parent = parent;
        this.config = new VisualEffectsConfig();
        this.random = new Random();
        this.globalParticles = new ArrayList<>();
        
        // Initialize graphics buffers lazily when needed
        // This allows for testing without full Processing initialization
    }
    
    /**
     * Creates a new VisualEffectsManager with initial configuration.
     * 
     * @param parent The Processing PApplet instance
     * @param config Initial visual effects configuration
     */
    public VisualEffectsManager(PApplet parent, VisualEffectsConfig config) {
        this(parent);
        setConfig(config);
    }
    
    /**
     * Initializes graphics buffers for effects processing.
     * This is called lazily when buffers are first needed.
     */
    private void initializeBuffers() {
        try {
            int width = Math.max(800, parent.width > 0 ? parent.width : 800);
            int height = Math.max(600, parent.height > 0 ? parent.height : 600);
            
            effectBuffer = parent.createGraphics(width, height);
            blurBuffer = parent.createGraphics(width, height);
            glowBuffer = parent.createGraphics(width, height);
        } catch (Exception e) {
            // In test environments, graphics may not be available
            // Set buffers to null and handle gracefully in methods
            effectBuffer = null;
            blurBuffer = null;
            glowBuffer = null;
        }
    }
    
    /**
     * Ensures buffers are initialized, creating them if needed.
     */
    private void ensureBuffersInitialized() {
        if (effectBuffer == null || blurBuffer == null || glowBuffer == null) {
            initializeBuffers();
        }
    }
    
    /**
     * Sets the visual effects configuration.
     * 
     * @param config The new configuration
     */
    public void setConfig(VisualEffectsConfig config) {
        if (config != null) {
            this.config = config.copy();
            this.config.validate();
        }
    }
    
    /**
     * Gets the current visual effects configuration.
     * 
     * @return A copy of the current configuration
     */
    public VisualEffectsConfig getConfig() {
        return config.copy();
    }
    
    /**
     * Sets the blur effect parameters.
     * 
     * @param radius Blur radius (0 to disable)
     */
    public void setBlurEffect(float radius) {
        config.enableBlur = radius > 0;
        config.blurRadius = Math.max(0.0f, Math.min(50.0f, radius));
        config.enableMotionBlur = false;
        config.enableSelectiveBlur = false;
    }
    
    /**
     * Sets the blur effect with specific type.
     * 
     * @param radius Blur radius (0 to disable)
     * @param enableMotionBlur Whether to enable motion blur
     * @param enableSelectiveBlur Whether to enable selective blur
     */
    public void setBlurEffect(float radius, boolean enableMotionBlur, boolean enableSelectiveBlur) {
        config.enableBlur = radius > 0;
        config.blurRadius = Math.max(0.0f, Math.min(50.0f, radius));
        config.enableMotionBlur = enableMotionBlur && config.enableBlur;
        config.enableSelectiveBlur = enableSelectiveBlur && config.enableBlur && !enableMotionBlur; // Selective blur takes precedence over motion blur
    }
    
    /**
     * Sets the color filter effect.
     * 
     * @param filter The color filter type
     * @param intensity Filter intensity (0.0 to 2.0)
     */
    public void setColorFilter(VisualEffectsConfig.ColorFilterType filter, float intensity) {
        config.colorFilter = filter != null ? filter : VisualEffectsConfig.ColorFilterType.NONE;
        config.colorIntensity = Math.max(0.0f, Math.min(2.0f, intensity));
    }
    
    /**
     * Sets color tinting parameters.
     * 
     * @param tintColor The tint color to apply
     * @param intensity Tint intensity (0.0 to 2.0)
     */
    public void setColorTint(int tintColor, float intensity) {
        config.tintColor = tintColor;
        config.colorIntensity = Math.max(0.0f, Math.min(2.0f, intensity));
    }
    
    /**
     * Sets brightness and contrast adjustments.
     * 
     * @param brightness Brightness adjustment (-1.0 to 1.0)
     * @param contrast Contrast adjustment (-1.0 to 1.0)
     */
    public void setBrightnessContrast(float brightness, float contrast) {
        config.brightness = Math.max(-1.0f, Math.min(1.0f, brightness));
        config.contrast = Math.max(-1.0f, Math.min(1.0f, contrast));
    }
    
    /**
     * Sets brightness, contrast, and gamma adjustments.
     * 
     * @param brightness Brightness adjustment (-1.0 to 1.0)
     * @param contrast Contrast adjustment (-1.0 to 1.0)
     * @param gamma Gamma correction (0.1 to 3.0)
     */
    public void setBrightnessContrastGamma(float brightness, float contrast, float gamma) {
        config.brightness = Math.max(-1.0f, Math.min(1.0f, brightness));
        config.contrast = Math.max(-1.0f, Math.min(1.0f, contrast));
        config.gamma = Math.max(0.1f, Math.min(3.0f, gamma));
    }
    
    /**
     * Sets the particle system configuration.
     * 
     * @param particleConfig Particle system configuration
     */
    public void setParticleSystem(VisualEffectsConfig particleConfig) {
        if (particleConfig != null) {
            config.enableParticles = particleConfig.enableParticles;
            config.particleCount = particleConfig.particleCount;
            config.particleType = particleConfig.particleType;
            config.particleColor = particleConfig.particleColor;
            config.particleLifetime = particleConfig.particleLifetime;
            config.particleSize = particleConfig.particleSize;
            config.particleSpeed = particleConfig.particleSpeed;
        }
    }
    
    /**
     * Sets the border effect parameters with enhanced options.
     * 
     * @param enableGlow Whether to enable glow effect
     * @param glowRadius Glow radius
     * @param glowColor Glow color
     * @param enableShadow Whether to enable shadow effect
     * @param shadowOffset Shadow offset
     * @param shadowBlur Shadow blur amount
     */
    public void setBorderEffect(boolean enableGlow, float glowRadius, int glowColor, 
                               boolean enableShadow, float shadowOffset, float shadowBlur) {
        config.enableGlow = enableGlow;
        config.glowRadius = Math.max(0.0f, glowRadius);
        config.glowColor = glowColor;
        config.enableShadow = enableShadow;
        config.shadowOffsetX = shadowOffset;
        config.shadowOffsetY = shadowOffset;
        config.shadowBlur = Math.max(0.0f, shadowBlur);
    }
    
    /**
     * Sets advanced border effects including outline and vintage frames.
     * 
     * @param enableGlow Whether to enable glow effect
     * @param glowRadius Glow radius
     * @param glowColor Glow color
     * @param enableShadow Whether to enable shadow effect
     * @param shadowOffsetX Shadow X offset
     * @param shadowOffsetY Shadow Y offset
     * @param shadowBlur Shadow blur amount
     * @param shadowColor Shadow color
     * @param enableOutline Whether to enable outline effect
     * @param outlineThickness Outline thickness
     * @param outlineColor Outline color
     */
    public void setAdvancedBorderEffects(boolean enableGlow, float glowRadius, int glowColor,
                                       boolean enableShadow, float shadowOffsetX, float shadowOffsetY, 
                                       float shadowBlur, int shadowColor,
                                       boolean enableOutline, float outlineThickness, int outlineColor) {
        config.enableGlow = enableGlow;
        config.glowRadius = Math.max(0.0f, glowRadius);
        config.glowColor = glowColor;
        config.enableShadow = enableShadow;
        config.shadowOffsetX = shadowOffsetX;
        config.shadowOffsetY = shadowOffsetY;
        config.shadowBlur = Math.max(0.0f, shadowBlur);
        config.shadowColor = shadowColor;
        config.enableOutline = enableOutline;
        config.outlineThickness = Math.max(0.0f, outlineThickness);
        config.outlineColor = outlineColor;
    }
    
    /**
     * Applies all configured visual effects to an image.
     * 
     * @param originalImage The original image to process
     * @return A new PImage with effects applied
     */
    public PImage applyEffects(PImage originalImage) {
        if (originalImage == null) {
            return null;
        }
        
        // Ensure buffers are available (may be null in test environments)
        ensureBuffersInitialized();
        
        // Start with the original image
        PImage processedImage = originalImage.copy();
        
        // Apply color filters first
        if (config.colorFilter != VisualEffectsConfig.ColorFilterType.NONE) {
            processedImage = applyColorFilter(processedImage);
        }
        
        // Apply brightness/contrast adjustments
        if (config.brightness != 0.0f || config.contrast != 0.0f || config.gamma != 1.0f) {
            processedImage = applyBrightnessContrast(processedImage);
        }
        
        // Apply blur effects (only if buffers are available)
        if (config.enableBlur && config.blurRadius > 0 && blurBuffer != null) {
            processedImage = applyBlur(processedImage);
        }
        
        return processedImage;
    }
    
    /**
     * Applies color filter effects to an image.
     * Uses Processing's tint() and colorMode() functions for better color manipulation.
     * 
     * @param image The image to process
     * @return The processed image
     */
    private PImage applyColorFilter(PImage image) {
        if (config.colorFilter == VisualEffectsConfig.ColorFilterType.NONE && config.tintColor == 0xFFFFFFFF) {
            return image;
        }
        
        // Create a graphics buffer for color processing (if available)
        if (effectBuffer != null && (effectBuffer.width != image.width || effectBuffer.height != image.height)) {
            try {
                effectBuffer = parent.createGraphics(image.width, image.height);
            } catch (Exception e) {
                effectBuffer = null;
            }
        }
        
        // Apply color filter using pixel manipulation for precise control
        PImage filtered = image.copy();
        filtered.loadPixels();
        
        for (int i = 0; i < filtered.pixels.length; i++) {
            int pixel = filtered.pixels[i];
            int alpha = (pixel >> 24) & 0xFF;
            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;
            
            // Apply color filter
            switch (config.colorFilter) {
                case SEPIA:
                    filtered.pixels[i] = applySepia(red, green, blue, alpha);
                    break;
                    
                case GRAYSCALE:
                    filtered.pixels[i] = applyGrayscale(red, green, blue, alpha);
                    break;
                    
                case VINTAGE:
                    filtered.pixels[i] = applyVintage(red, green, blue, alpha);
                    break;
                    
                case HIGH_CONTRAST:
                    filtered.pixels[i] = applyHighContrast(red, green, blue, alpha);
                    break;
                    
                case POSTERIZE:
                    filtered.pixels[i] = applyPosterize(red, green, blue, alpha);
                    break;
                    
                default:
                    // No filter, keep original
                    break;
            }
        }
        
        filtered.updatePixels();
        
        // Apply tint using Processing's tint function if specified and buffer is available
        if (config.tintColor != 0xFFFFFFFF && effectBuffer != null) {
            effectBuffer.beginDraw();
            effectBuffer.clear();
            effectBuffer.tint(config.tintColor, (int)(255 * config.colorIntensity));
            effectBuffer.image(filtered, 0, 0);
            effectBuffer.endDraw();
            return effectBuffer.copy();
        }
        
        return filtered;
    }
    
    /**
     * Applies sepia tone effect using color matrix transformation.
     * 
     * @param r Red component
     * @param g Green component  
     * @param b Blue component
     * @param a Alpha component
     * @return Processed pixel color
     */
    private int applySepia(int r, int g, int b, int a) {
        int sepiaRed = (int) Math.min(255, (r * 0.393 + g * 0.769 + b * 0.189) * config.colorIntensity);
        int sepiaGreen = (int) Math.min(255, (r * 0.349 + g * 0.686 + b * 0.168) * config.colorIntensity);
        int sepiaBlue = (int) Math.min(255, (r * 0.272 + g * 0.534 + b * 0.131) * config.colorIntensity);
        return (a << 24) | (sepiaRed << 16) | (sepiaGreen << 8) | sepiaBlue;
    }
    
    /**
     * Applies grayscale conversion.
     * 
     * @param r Red component
     * @param g Green component  
     * @param b Blue component
     * @param a Alpha component
     * @return Processed pixel color
     */
    private int applyGrayscale(int r, int g, int b, int a) {
        int gray = (int) ((r * 0.299 + g * 0.587 + b * 0.114) * config.colorIntensity);
        gray = Math.min(255, gray);
        return (a << 24) | (gray << 16) | (gray << 8) | gray;
    }
    
    /**
     * Applies vintage/retro color grading effect.
     * 
     * @param r Red component
     * @param g Green component  
     * @param b Blue component
     * @param a Alpha component
     * @return Processed pixel color
     */
    private int applyVintage(int r, int g, int b, int a) {
        // Vintage effect: enhance warm tones, reduce blues, add slight sepia
        float vintageRed = Math.min(255, r * 1.2f * config.colorIntensity);
        float vintageGreen = Math.min(255, g * 1.1f * config.colorIntensity);
        float vintageBlue = Math.min(255, b * 0.8f * config.colorIntensity);
        
        // Add slight sepia tint
        vintageRed = Math.min(255, vintageRed + (vintageGreen * 0.1f));
        vintageGreen = Math.min(255, vintageGreen + (vintageBlue * 0.05f));
        
        return (a << 24) | ((int)vintageRed << 16) | ((int)vintageGreen << 8) | (int)vintageBlue;
    }
    
    /**
     * Applies high contrast effect.
     * 
     * @param r Red component
     * @param g Green component  
     * @param b Blue component
     * @param a Alpha component
     * @return Processed pixel color
     */
    private int applyHighContrast(int r, int g, int b, int a) {
        // High contrast: push values toward extremes
        float contrastFactor = 1.5f * config.colorIntensity;
        int contrastRed = r > 128 ? Math.min(255, (int)(r * contrastFactor)) : Math.max(0, (int)(r * (2.0f - contrastFactor)));
        int contrastGreen = g > 128 ? Math.min(255, (int)(g * contrastFactor)) : Math.max(0, (int)(g * (2.0f - contrastFactor)));
        int contrastBlue = b > 128 ? Math.min(255, (int)(b * contrastFactor)) : Math.max(0, (int)(b * (2.0f - contrastFactor)));
        return (a << 24) | (contrastRed << 16) | (contrastGreen << 8) | contrastBlue;
    }
    
    /**
     * Applies posterize effect (reduces color depth).
     * 
     * @param r Red component
     * @param g Green component  
     * @param b Blue component
     * @param a Alpha component
     * @return Processed pixel color
     */
    private int applyPosterize(int r, int g, int b, int a) {
        // Posterize: reduce color depth
        int levels = Math.max(2, (int)(8 * config.colorIntensity));
        int step = 256 / levels;
        int posterRed = (r / step) * step;
        int posterGreen = (g / step) * step;
        int posterBlue = (b / step) * step;
        return (a << 24) | (posterRed << 16) | (posterGreen << 8) | posterBlue;
    }
    
    /**
     * Applies brightness, contrast, and gamma adjustments to an image.
     * Uses Processing's tint() and custom pixel manipulation for precise control.
     * 
     * @param image The image to process
     * @return The processed image
     */
    private PImage applyBrightnessContrast(PImage image) {
        // Create a graphics buffer for brightness/contrast processing (if available)
        if (effectBuffer != null && (effectBuffer.width != image.width || effectBuffer.height != image.height)) {
            try {
                effectBuffer = parent.createGraphics(image.width, image.height);
            } catch (Exception e) {
                effectBuffer = null;
            }
        }
        
        // For simple brightness adjustments, we can use Processing's tint() if buffer is available
        if (config.contrast == 0.0f && config.gamma == 1.0f && config.brightness != 0.0f && effectBuffer != null) {
            // Simple brightness adjustment using tint
            int brightness = (int)(255 + (config.brightness * 255));
            brightness = Math.max(0, Math.min(255, brightness));
            effectBuffer.beginDraw();
            effectBuffer.clear();
            effectBuffer.tint(brightness, brightness, brightness);
            effectBuffer.image(image, 0, 0);
            effectBuffer.endDraw();
            return effectBuffer.copy();
        } else {
            // Complex adjustments require pixel manipulation
            PImage adjusted = image.copy();
            adjusted.loadPixels();
            
            for (int i = 0; i < adjusted.pixels.length; i++) {
                int pixel = adjusted.pixels[i];
                int alpha = (pixel >> 24) & 0xFF;
                float red = (pixel >> 16) & 0xFF;
                float green = (pixel >> 8) & 0xFF;
                float blue = pixel & 0xFF;
                
                // Apply brightness (-100% to +100%)
                red = red + (config.brightness * 255);
                green = green + (config.brightness * 255);
                blue = blue + (config.brightness * 255);
                
                // Apply contrast (-100% to +100%)
                float contrast = config.contrast + 1.0f;
                red = ((red - 128) * contrast) + 128;
                green = ((green - 128) * contrast) + 128;
                blue = ((blue - 128) * contrast) + 128;
                
                // Apply gamma correction (0.1 to 3.0)
                if (config.gamma != 1.0f) {
                    red = 255 * (float)Math.pow(Math.max(0, red) / 255.0, 1.0 / config.gamma);
                    green = 255 * (float)Math.pow(Math.max(0, green) / 255.0, 1.0 / config.gamma);
                    blue = 255 * (float)Math.pow(Math.max(0, blue) / 255.0, 1.0 / config.gamma);
                }
                
                // Clamp values to valid range
                int finalRed = Math.max(0, Math.min(255, (int)red));
                int finalGreen = Math.max(0, Math.min(255, (int)green));
                int finalBlue = Math.max(0, Math.min(255, (int)blue));
                
                adjusted.pixels[i] = (alpha << 24) | (finalRed << 16) | (finalGreen << 8) | finalBlue;
            }
            
            adjusted.updatePixels();
            
            if (effectBuffer != null) {
                effectBuffer.beginDraw();
                effectBuffer.clear();
                effectBuffer.noTint();
                effectBuffer.image(adjusted, 0, 0);
                effectBuffer.endDraw();
                return effectBuffer.copy();
            } else {
                return adjusted;
            }
        }
    }
    
    /**
     * Applies blur effect to an image.
     * 
     * @param image The image to blur
     * @return The blurred image
     */
    private PImage applyBlur(PImage image) {
        PImage blurred = image.copy();
        
        if (config.enableMotionBlur) {
            // Apply motion blur by creating multiple offset copies
            blurred = applyMotionBlur(blurred);
        } else if (config.enableSelectiveBlur) {
            // Apply selective blur (blur background, keep foreground sharp)
            blurred = applySelectiveBlur(blurred);
        } else {
            // Apply standard Gaussian blur
            blurred = applyGaussianBlur(blurred);
        }
        
        return blurred;
    }
    
    /**
     * Applies Gaussian blur using Processing's built-in filter.
     * 
     * @param image The image to blur
     * @return The blurred image
     */
    private PImage applyGaussianBlur(PImage image) {
        PImage blurred = image.copy();
        
        // Apply blur multiple times for stronger effect
        int blurPasses = Math.max(1, (int)(config.blurRadius / 5));
        for (int i = 0; i < blurPasses; i++) {
            blurred.filter(PApplet.BLUR, Math.min(config.blurRadius / blurPasses, 10));
        }
        
        return blurred;
    }
    
    /**
     * Applies motion blur effect by creating multiple offset copies.
     * 
     * @param image The image to apply motion blur to
     * @return The motion blurred image
     */
    private PImage applyMotionBlur(PImage image) {
        // Create a graphics buffer for motion blur
        if (blurBuffer.width != image.width || blurBuffer.height != image.height) {
            blurBuffer = parent.createGraphics(image.width, image.height);
        }
        
        blurBuffer.beginDraw();
        blurBuffer.clear();
        blurBuffer.tint(255, 255 / Math.max(1, (int)config.blurRadius)); // Reduce opacity for each layer
        
        // Draw multiple offset copies to create motion blur
        int steps = Math.max(3, (int)(config.blurRadius / 2));
        float stepSize = config.blurRadius / steps;
        
        for (int i = 0; i < steps; i++) {
            float offset = i * stepSize;
            blurBuffer.image(image, offset, 0); // Horizontal motion blur
        }
        
        blurBuffer.endDraw();
        return blurBuffer.copy();
    }
    
    /**
     * Applies selective blur (blur background, keep foreground sharp).
     * This is a simplified implementation that blurs the entire image and then
     * overlays the original at reduced opacity to simulate selective focus.
     * 
     * @param image The image to apply selective blur to
     * @return The selectively blurred image
     */
    private PImage applySelectiveBlur(PImage image) {
        // Create blurred version
        PImage blurred = applyGaussianBlur(image);
        
        // Create a graphics buffer for selective blur
        if (blurBuffer.width != image.width || blurBuffer.height != image.height) {
            blurBuffer = parent.createGraphics(image.width, image.height);
        }
        
        blurBuffer.beginDraw();
        blurBuffer.clear();
        
        // Draw blurred background
        blurBuffer.image(blurred, 0, 0);
        
        // Draw sharp foreground in center area (simulating depth of field)
        blurBuffer.tint(255, 255); // Full opacity
        int centerX = image.width / 2;
        int centerY = image.height / 2;
        int focusWidth = (int)(image.width * 0.6f); // Focus area is 60% of image width
        int focusHeight = (int)(image.height * 0.6f);
        
        // Create a mask for the focus area (simple rectangular focus for now)
        PImage focusArea = image.get(centerX - focusWidth/2, centerY - focusHeight/2, focusWidth, focusHeight);
        blurBuffer.image(focusArea, centerX - focusWidth/2, centerY - focusHeight/2);
        
        blurBuffer.endDraw();
        return blurBuffer.copy();
    }
    
    /**
     * Generates particles around an image position with enhanced physics.
     * 
     * @param x Image center x position
     * @param y Image center y position
     * @param width Image width
     * @param height Image height
     */
    public void generateParticles(float x, float y, float width, float height) {
        if (!config.enableParticles) {
            return;
        }
        
        // Generate particles around the image bounds
        int particlesToGenerate = Math.min(config.particleCount / 10, 20); // Generate in batches
        
        for (int i = 0; i < particlesToGenerate; i++) {
            // Random position around image with different patterns based on type
            float particleX, particleY;
            
            switch (config.particleType) {
                case FIRE:
                    // Fire particles start at bottom of image
                    particleX = x + (random.nextFloat() - 0.5f) * width * 0.8f;
                    particleY = y + height / 2 + random.nextFloat() * height * 0.2f;
                    break;
                    
                case SMOKE:
                    // Smoke particles start near center and spread
                    particleX = x + (random.nextFloat() - 0.5f) * width * 0.6f;
                    particleY = y + (random.nextFloat() - 0.5f) * height * 0.4f;
                    break;
                    
                case SNOW:
                    // Snow particles start above the image
                    particleX = x + (random.nextFloat() - 0.5f) * width * 1.5f;
                    particleY = y - height / 2 - random.nextFloat() * height;
                    break;
                    
                case SPARKLES:
                case STARS:
                    // Sparkles and stars around the entire image
                    float angle = random.nextFloat() * 2 * (float)Math.PI;
                    float distance = (width + height) / 4 + random.nextFloat() * (width + height) / 4;
                    particleX = x + (float)Math.cos(angle) * distance;
                    particleY = y + (float)Math.sin(angle) * distance;
                    break;
                    
                case BUBBLES:
                    // Bubbles start at bottom and rise
                    particleX = x + (random.nextFloat() - 0.5f) * width;
                    particleY = y + height / 2 + random.nextFloat() * height * 0.3f;
                    break;
                    
                default:
                    // Default: random position around image
                    particleX = x + (random.nextFloat() - 0.5f) * width;
                    particleY = y + (random.nextFloat() - 0.5f) * height;
                    break;
            }
            
            // Random velocity based on particle type and physics
            float velocityX = (random.nextFloat() - 0.5f) * config.particleSpeed;
            float velocityY = (random.nextFloat() - 0.5f) * config.particleSpeed;
            
            // Adjust velocity based on particle type
            switch (config.particleType) {
                case SMOKE:
                case FIRE:
                    velocityY -= Math.abs(velocityY) * 0.5f; // Upward bias
                    velocityX *= 0.3f; // Less horizontal movement
                    break;
                    
                case SNOW:
                    velocityY = Math.abs(velocityY) * 0.3f; // Slow downward movement
                    velocityX *= 0.2f; // Gentle horizontal drift
                    break;
                    
                case SPARKLES:
                case STARS:
                    // Random directions with moderate speed
                    velocityX *= 0.5f;
                    velocityY *= 0.5f;
                    break;
                    
                case BUBBLES:
                    velocityY -= Math.abs(velocityY) * 0.7f; // Strong upward movement
                    velocityX *= 0.4f; // Some horizontal wobble
                    break;
            }
            
            // Random size variation
            float size = config.particleSize + (random.nextFloat() - 0.5f) * config.particleSize * 0.5f;
            size = Math.max(0.5f, size);
            
            // Random lifetime variation
            float lifetime = config.particleLifetime + (random.nextFloat() - 0.5f) * config.particleLifetime * 0.3f;
            lifetime = Math.max(500.0f, lifetime);
            
            Particle particle = new Particle(
                particleX, particleY,
                velocityX, velocityY,
                size,
                config.particleColor,
                lifetime,
                config.particleType
            );
            
            // Set physics parameters based on environment
            setParticlePhysics(particle);
            
            globalParticles.add(particle);
        }
        
        // Limit total particle count
        while (globalParticles.size() > config.particleCount) {
            globalParticles.remove(0);
        }
    }
    
    /**
     * Sets physics parameters for a particle based on its type and environment.
     * 
     * @param particle The particle to configure
     */
    private void setParticlePhysics(Particle particle) {
        switch (particle.getType()) {
            case FIRE:
                // Fire: strong upward movement, medium friction, no wind
                particle.setPhysics(-0.3f, 0.0f, 0.92f);
                break;
                
            case SMOKE:
                // Smoke: gentle upward movement, low friction, slight wind effect
                particle.setPhysics(-0.15f, 0.02f, 0.95f);
                break;
                
            case SNOW:
                // Snow: gentle downward movement, high friction, wind effect
                particle.setPhysics(0.08f, 0.05f, 0.99f);
                break;
                
            case SPARKLES:
                // Sparkles: moderate gravity, medium friction
                particle.setPhysics(0.12f, 0.0f, 0.96f);
                break;
                
            case STARS:
                // Stars: no gravity, no friction (floating)
                particle.setPhysics(0.0f, 0.0f, 1.0f);
                break;
                
            case BUBBLES:
                // Bubbles: gentle upward movement, high friction, slight wind
                particle.setPhysics(-0.08f, 0.03f, 0.98f);
                break;
        }
    }
    
    /**
     * Generates particles with collision detection around obstacles.
     * 
     * @param x Image center x position
     * @param y Image center y position
     * @param width Image width
     * @param height Image height
     * @param obstacles List of obstacle rectangles for collision detection
     */
    public void generateParticlesWithCollision(float x, float y, float width, float height, java.util.List<java.awt.Rectangle> obstacles) {
        generateParticles(x, y, width, height);
        
        // Apply collision detection to newly generated particles
        if (obstacles != null && !obstacles.isEmpty()) {
            for (Particle particle : globalParticles) {
                checkParticleCollisions(particle, obstacles);
            }
        }
    }
    
    /**
     * Checks and handles collisions between a particle and obstacles.
     * 
     * @param particle The particle to check
     * @param obstacles List of obstacle rectangles
     */
    private void checkParticleCollisions(Particle particle, java.util.List<java.awt.Rectangle> obstacles) {
        for (java.awt.Rectangle obstacle : obstacles) {
            if (obstacle.contains(particle.getX(), particle.getY())) {
                // Simple collision response: bounce off obstacle
                float newVelX = -particle.getVelocityX() * 0.7f; // Reduce velocity on bounce
                float newVelY = -particle.getVelocityY() * 0.7f;
                particle.setVelocity(newVelX, newVelY);
                
                // Move particle outside obstacle
                float centerX = (float)(obstacle.getCenterX());
                float centerY = (float)(obstacle.getCenterY());
                float dx = particle.getX() - centerX;
                float dy = particle.getY() - centerY;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                
                if (distance > 0) {
                    float pushX = (dx / distance) * (obstacle.width / 2 + particle.getSize());
                    float pushY = (dy / distance) * (obstacle.height / 2 + particle.getSize());
                    particle.setPosition(centerX + pushX, centerY + pushY);
                }
                break;
            }
        }
    }
    
    /**
     * Updates all particles in the system.
     * 
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    public void updateParticles(float deltaTime) {
        // Update all particles
        for (Particle particle : globalParticles) {
            particle.update(deltaTime);
        }
        
        // Remove dead particles
        globalParticles.removeIf(Particle::isDead);
    }
    
    /**
     * Renders all particles in the system with enhanced visual effects.
     */
    public void renderParticles() {
        if (!config.enableParticles || globalParticles.isEmpty()) {
            return;
        }
        
        parent.pushStyle();
        
        for (Particle particle : globalParticles) {
            renderParticle(particle);
        }
        
        parent.popStyle();
    }
    
    /**
     * Renders a single particle with type-specific visual effects.
     * 
     * @param particle The particle to render
     */
    private void renderParticle(Particle particle) {
        parent.pushMatrix();
        parent.translate(particle.getX(), particle.getY());
        
        int currentColor = particle.getCurrentColor();
        float size = particle.getSize();
        
        switch (particle.getType()) {
            case SPARKLES:
                renderSparkle(currentColor, size);
                break;
                
            case STARS:
                renderStar(currentColor, size);
                break;
                
            case SMOKE:
                renderSmoke(currentColor, size, particle.getAge() / particle.getLifetime());
                break;
                
            case FIRE:
                renderFire(currentColor, size, particle.getAge() / particle.getLifetime());
                break;
                
            case SNOW:
                renderSnowflake(currentColor, size);
                break;
                
            case BUBBLES:
                renderBubble(currentColor, size, particle.getOpacity());
                break;
                
            default:
                // Default: simple circle
                parent.fill(currentColor);
                parent.noStroke();
                parent.ellipse(0, 0, size, size);
                break;
        }
        
        parent.popMatrix();
    }
    
    /**
     * Renders a sparkle particle with radiating lines.
     */
    private void renderSparkle(int particleColor, float size) {
        parent.stroke(particleColor);
        parent.strokeWeight(Math.max(1, size / 4));
        parent.noFill();
        
        // Draw radiating lines
        float halfSize = size / 2;
        parent.line(-halfSize, 0, halfSize, 0);
        parent.line(0, -halfSize, 0, halfSize);
        parent.line(-halfSize * 0.7f, -halfSize * 0.7f, halfSize * 0.7f, halfSize * 0.7f);
        parent.line(-halfSize * 0.7f, halfSize * 0.7f, halfSize * 0.7f, -halfSize * 0.7f);
        
        // Central glow
        parent.fill(particleColor);
        parent.noStroke();
        parent.ellipse(0, 0, size / 3, size / 3);
    }
    
    /**
     * Renders a star particle with multiple points.
     */
    private void renderStar(int particleColor, float size) {
        parent.fill(particleColor);
        parent.noStroke();
        
        // Draw star shape
        parent.beginShape();
        int points = 5;
        float outerRadius = size / 2;
        float innerRadius = outerRadius * 0.4f;
        
        for (int i = 0; i < points * 2; i++) {
            float angle = (float)(i * Math.PI / points);
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;
            float x = (float)(Math.cos(angle) * radius);
            float y = (float)(Math.sin(angle) * radius);
            parent.vertex(x, y);
        }
        parent.endShape(PApplet.CLOSE);
    }
    
    /**
     * Renders a smoke particle with soft, expanding appearance.
     */
    private void renderSmoke(int particleColor, float size, float ageRatio) {
        // Smoke gets larger and more transparent over time
        float smokeSize = size * (1.0f + ageRatio * 2.0f);
        int alpha = (int)((1.0f - ageRatio) * 100); // Fade out over time
        
        parent.fill(particleColor & 0x00FFFFFF | (alpha << 24));
        parent.noStroke();
        
        // Draw multiple overlapping circles for soft effect
        for (int i = 0; i < 3; i++) {
            float offset = (i - 1) * smokeSize * 0.1f;
            parent.ellipse(offset, offset, smokeSize, smokeSize);
        }
    }
    
    /**
     * Renders a fire particle with flickering effect.
     */
    private void renderFire(int particleColor, float size, float ageRatio) {
        // Fire flickers and changes color over time
        float flicker = 0.8f + 0.4f * (float)Math.sin(System.currentTimeMillis() * 0.01f);
        float fireSize = size * flicker;
        
        // Color shifts from white/yellow to red/orange over time
        int red = (particleColor >> 16) & 0xFF;
        int green = (int)(((particleColor >> 8) & 0xFF) * (1.0f - ageRatio * 0.5f));
        int blue = (int)((particleColor & 0xFF) * (1.0f - ageRatio));
        int alpha = (particleColor >> 24) & 0xFF;
        
        int fireColor = (alpha << 24) | (red << 16) | (green << 8) | blue;
        
        parent.fill(fireColor);
        parent.noStroke();
        
        // Draw flame shape (elongated ellipse)
        parent.ellipse(0, 0, fireSize * 0.8f, fireSize * 1.2f);
        
        // Add bright center
        parent.fill(255, 255, 200, alpha);
        parent.ellipse(0, 0, fireSize * 0.4f, fireSize * 0.6f);
    }
    
    /**
     * Renders a snowflake particle with crystalline structure.
     */
    private void renderSnowflake(int particleColor, float size) {
        parent.stroke(particleColor);
        parent.strokeWeight(1);
        parent.noFill();
        
        // Draw snowflake pattern
        float halfSize = size / 2;
        
        // Main axes
        parent.line(-halfSize, 0, halfSize, 0);
        parent.line(0, -halfSize, 0, halfSize);
        parent.line(-halfSize * 0.7f, -halfSize * 0.7f, halfSize * 0.7f, halfSize * 0.7f);
        parent.line(-halfSize * 0.7f, halfSize * 0.7f, halfSize * 0.7f, -halfSize * 0.7f);
        
        // Small branches
        float branchSize = halfSize * 0.3f;
        for (int i = 0; i < 8; i++) {
            float angle = (float)(i * Math.PI / 4);
            float x1 = (float)(Math.cos(angle) * halfSize * 0.7f);
            float y1 = (float)(Math.sin(angle) * halfSize * 0.7f);
            float x2 = (float)(Math.cos(angle + Math.PI / 6) * branchSize);
            float y2 = (float)(Math.sin(angle + Math.PI / 6) * branchSize);
            float x3 = (float)(Math.cos(angle - Math.PI / 6) * branchSize);
            float y3 = (float)(Math.sin(angle - Math.PI / 6) * branchSize);
            
            parent.line(x1, y1, x1 + x2, y1 + y2);
            parent.line(x1, y1, x1 + x3, y1 + y3);
        }
    }
    
    /**
     * Renders a bubble particle with transparent appearance and highlight.
     */
    private void renderBubble(int particleColor, float size, float opacity) {
        // Bubble outline
        parent.noFill();
        parent.stroke(particleColor & 0x00FFFFFF | ((int)(opacity * 150) << 24));
        parent.strokeWeight(1);
        parent.ellipse(0, 0, size, size);
        
        // Bubble highlight (small white spot)
        parent.fill(255, 255, 255, (int)(opacity * 100));
        parent.noStroke();
        parent.ellipse(-size * 0.2f, -size * 0.2f, size * 0.3f, size * 0.3f);
        
        // Optional: subtle fill
        parent.fill(particleColor & 0x00FFFFFF | ((int)(opacity * 30) << 24));
        parent.noStroke();
        parent.ellipse(0, 0, size * 0.9f, size * 0.9f);
    }
    
    /**
     * Renders glow effect around an image using blur and blend.
     * 
     * @param x Image x position
     * @param y Image y position
     * @param width Image width
     * @param height Image height
     */
    public void renderGlow(float x, float y, float width, float height) {
        if (!config.enableGlow || config.glowRadius <= 0) {
            return;
        }
        
        // Create glow buffer if needed
        if (glowBuffer.width != (int)(width + config.glowRadius * 4) || 
            glowBuffer.height != (int)(height + config.glowRadius * 4)) {
            glowBuffer = parent.createGraphics((int)(width + config.glowRadius * 4), 
                                             (int)(height + config.glowRadius * 4));
        }
        
        glowBuffer.beginDraw();
        glowBuffer.clear();
        
        // Draw the base shape for glow
        glowBuffer.fill(config.glowColor);
        glowBuffer.noStroke();
        glowBuffer.rectMode(PApplet.CENTER);
        glowBuffer.rect(glowBuffer.width / 2, glowBuffer.height / 2, width, height);
        
        // Apply blur to create glow effect
        int blurPasses = Math.max(1, (int)(config.glowRadius / 3));
        for (int i = 0; i < blurPasses; i++) {
            glowBuffer.filter(PApplet.BLUR, Math.min(config.glowRadius / blurPasses, 8));
        }
        
        glowBuffer.endDraw();
        
        // Render the glow with blend mode
        parent.pushStyle();
        parent.blendMode(PApplet.ADD); // Additive blending for glow effect
        parent.tint(255, 128); // Semi-transparent
        parent.image(glowBuffer, x - glowBuffer.width / 2, y - glowBuffer.height / 2);
        parent.blendMode(PApplet.BLEND); // Reset blend mode
        parent.noTint();
        parent.popStyle();
    }
    
    /**
     * Renders drop shadow effect with configurable offset and blur.
     * 
     * @param x Image x position
     * @param y Image y position
     * @param width Image width
     * @param height Image height
     */
    public void renderShadow(float x, float y, float width, float height) {
        if (!config.enableShadow) {
            return;
        }
        
        parent.pushStyle();
        
        if (config.shadowBlur > 0) {
            // Render blurred shadow
            renderBlurredShadow(x, y, width, height);
        } else {
            // Render simple shadow
            parent.fill(config.shadowColor);
            parent.noStroke();
            parent.rectMode(PApplet.CENTER);
            
            float shadowX = x + config.shadowOffsetX;
            float shadowY = y + config.shadowOffsetY;
            parent.rect(shadowX, shadowY, width, height);
        }
        
        parent.popStyle();
    }
    
    /**
     * Renders a blurred drop shadow effect.
     * 
     * @param x Image x position
     * @param y Image y position
     * @param width Image width
     * @param height Image height
     */
    private void renderBlurredShadow(float x, float y, float width, float height) {
        // Create shadow buffer if needed
        int shadowBufferWidth = (int)(width + config.shadowBlur * 4);
        int shadowBufferHeight = (int)(height + config.shadowBlur * 4);
        
        if (effectBuffer.width != shadowBufferWidth || effectBuffer.height != shadowBufferHeight) {
            effectBuffer = parent.createGraphics(shadowBufferWidth, shadowBufferHeight);
        }
        
        effectBuffer.beginDraw();
        effectBuffer.clear();
        
        // Draw shadow shape
        effectBuffer.fill(config.shadowColor);
        effectBuffer.noStroke();
        effectBuffer.rectMode(PApplet.CENTER);
        effectBuffer.rect(effectBuffer.width / 2, effectBuffer.height / 2, width, height);
        
        // Apply blur
        int blurPasses = Math.max(1, (int)(config.shadowBlur / 2));
        for (int i = 0; i < blurPasses; i++) {
            effectBuffer.filter(PApplet.BLUR, Math.min(config.shadowBlur / blurPasses, 6));
        }
        
        effectBuffer.endDraw();
        
        // Render the shadow with offset
        float shadowX = x + config.shadowOffsetX - effectBuffer.width / 2;
        float shadowY = y + config.shadowOffsetY - effectBuffer.height / 2;
        parent.image(effectBuffer, shadowX, shadowY);
    }
    
    /**
     * Renders outline/stroke effect with configurable thickness and color.
     * 
     * @param x Image x position
     * @param y Image y position
     * @param width Image width
     * @param height Image height
     */
    public void renderOutline(float x, float y, float width, float height) {
        if (!config.enableOutline || config.outlineThickness <= 0) {
            return;
        }
        
        parent.pushStyle();
        parent.noFill();
        parent.stroke(config.outlineColor);
        parent.strokeWeight(config.outlineThickness);
        parent.rectMode(PApplet.CENTER);
        
        // Render multiple outline layers for smoother appearance
        if (config.outlineThickness > 2) {
            // Render multiple layers with decreasing opacity for smooth outline
            int layers = Math.min(5, (int)config.outlineThickness);
            for (int i = 0; i < layers; i++) {
                float layerThickness = config.outlineThickness * (layers - i) / layers;
                int layerAlpha = (int)(255 * (i + 1) / layers * 0.3f); // Fade outer layers
                
                parent.stroke(config.outlineColor & 0x00FFFFFF | (layerAlpha << 24));
                parent.strokeWeight(layerThickness);
                parent.rect(x, y, width + layerThickness, height + layerThickness);
            }
        } else {
            // Simple single outline
            parent.rect(x, y, width, height);
        }
        
        parent.popStyle();
    }
    
    /**
     * Renders vintage frame effects around an image.
     * 
     * @param x Image x position
     * @param y Image y position
     * @param width Image width
     * @param height Image height
     * @param frameStyle The vintage frame style (0-3)
     */
    public void renderVintageFrame(float x, float y, float width, float height, int frameStyle) {
        parent.pushStyle();
        
        switch (frameStyle) {
            case 0: // Classic ornate frame
                renderOrnateFrame(x, y, width, height);
                break;
                
            case 1: // Simple wooden frame
                renderWoodenFrame(x, y, width, height);
                break;
                
            case 2: // Art deco frame
                renderArtDecoFrame(x, y, width, height);
                break;
                
            case 3: // Polaroid-style frame
                renderPolaroidFrame(x, y, width, height);
                break;
                
            default:
                // Default to simple frame
                renderSimpleFrame(x, y, width, height);
                break;
        }
        
        parent.popStyle();
    }
    
    /**
     * Renders an ornate vintage frame with decorative corners.
     */
    private void renderOrnateFrame(float x, float y, float width, float height) {
        float frameThickness = Math.max(8, Math.min(width, height) * 0.05f);
        
        // Outer frame
        parent.fill(139, 69, 19); // Saddle brown
        parent.stroke(101, 67, 33); // Dark brown
        parent.strokeWeight(2);
        parent.rectMode(PApplet.CENTER);
        parent.rect(x, y, width + frameThickness * 2, height + frameThickness * 2);
        
        // Inner frame
        parent.fill(160, 82, 45); // Lighter brown
        parent.rect(x, y, width + frameThickness, height + frameThickness);
        
        // Decorative corners
        parent.fill(218, 165, 32); // Gold
        parent.noStroke();
        float cornerSize = frameThickness * 0.8f;
        
        // Corner decorations (simple circles for now)
        parent.ellipse(x - width/2 - frameThickness/2, y - height/2 - frameThickness/2, cornerSize, cornerSize);
        parent.ellipse(x + width/2 + frameThickness/2, y - height/2 - frameThickness/2, cornerSize, cornerSize);
        parent.ellipse(x - width/2 - frameThickness/2, y + height/2 + frameThickness/2, cornerSize, cornerSize);
        parent.ellipse(x + width/2 + frameThickness/2, y + height/2 + frameThickness/2, cornerSize, cornerSize);
    }
    
    /**
     * Renders a simple wooden frame.
     */
    private void renderWoodenFrame(float x, float y, float width, float height) {
        float frameThickness = Math.max(6, Math.min(width, height) * 0.04f);
        
        parent.fill(139, 90, 43); // Wood color
        parent.stroke(101, 67, 33); // Dark wood
        parent.strokeWeight(1);
        parent.rectMode(PApplet.CENTER);
        parent.rect(x, y, width + frameThickness * 2, height + frameThickness * 2);
        
        // Wood grain effect (simple lines)
        parent.stroke(160, 110, 60);
        parent.strokeWeight(0.5f);
        for (int i = 0; i < 5; i++) {
            float lineY = y - height/2 - frameThickness + i * frameThickness * 2 / 5;
            parent.line(x - width/2 - frameThickness, lineY, x + width/2 + frameThickness, lineY);
        }
    }
    
    /**
     * Renders an Art Deco style frame.
     */
    private void renderArtDecoFrame(float x, float y, float width, float height) {
        float frameThickness = Math.max(4, Math.min(width, height) * 0.03f);
        
        // Main frame
        parent.fill(47, 79, 79); // Dark slate gray
        parent.stroke(25, 25, 112); // Midnight blue
        parent.strokeWeight(2);
        parent.rectMode(PApplet.CENTER);
        parent.rect(x, y, width + frameThickness * 2, height + frameThickness * 2);
        
        // Art deco lines
        parent.stroke(255, 215, 0); // Gold
        parent.strokeWeight(1);
        
        // Horizontal lines
        parent.line(x - width/2 - frameThickness, y - height/2 - frameThickness/2, 
                   x + width/2 + frameThickness, y - height/2 - frameThickness/2);
        parent.line(x - width/2 - frameThickness, y + height/2 + frameThickness/2, 
                   x + width/2 + frameThickness, y + height/2 + frameThickness/2);
        
        // Vertical lines
        parent.line(x - width/2 - frameThickness/2, y - height/2 - frameThickness, 
                   x - width/2 - frameThickness/2, y + height/2 + frameThickness);
        parent.line(x + width/2 + frameThickness/2, y - height/2 - frameThickness, 
                   x + width/2 + frameThickness/2, y + height/2 + frameThickness);
    }
    
    /**
     * Renders a Polaroid-style frame.
     */
    private void renderPolaroidFrame(float x, float y, float width, float height) {
        float frameThickness = Math.max(10, Math.min(width, height) * 0.08f);
        float bottomThickness = frameThickness * 2; // Larger bottom border
        
        // White frame
        parent.fill(248, 248, 255); // Ghost white
        parent.stroke(220, 220, 220); // Light gray
        parent.strokeWeight(1);
        parent.rectMode(PApplet.CENTER);
        
        // Main frame with larger bottom
        parent.rect(x, y + (bottomThickness - frameThickness) / 2, 
                   width + frameThickness * 2, height + frameThickness + bottomThickness);
        
        // Slight shadow for depth
        parent.fill(200, 200, 200, 100);
        parent.noStroke();
        parent.rect(x + 2, y + 2 + (bottomThickness - frameThickness) / 2, 
                   width + frameThickness * 2, height + frameThickness + bottomThickness);
    }
    
    /**
     * Renders a simple frame as fallback.
     */
    private void renderSimpleFrame(float x, float y, float width, float height) {
        float frameThickness = Math.max(4, Math.min(width, height) * 0.02f);
        
        parent.fill(128, 128, 128); // Gray
        parent.stroke(64, 64, 64); // Dark gray
        parent.strokeWeight(1);
        parent.rectMode(PApplet.CENTER);
        parent.rect(x, y, width + frameThickness * 2, height + frameThickness * 2);
    }
    
    /**
     * Clears all particles from the system.
     */
    public void clearParticles() {
        globalParticles.clear();
    }
    
    /**
     * Gets the current number of active particles.
     * 
     * @return The number of active particles
     */
    public int getActiveParticleCount() {
        return globalParticles.size();
    }
    
    /**
     * Checks if any visual effects are currently enabled.
     * 
     * @return true if any effects are enabled, false otherwise
     */
    public boolean hasActiveEffects() {
        return config.enableBlur || 
               config.colorFilter != VisualEffectsConfig.ColorFilterType.NONE ||
               config.brightness != 0.0f || config.contrast != 0.0f || config.gamma != 1.0f ||
               config.enableParticles || config.enableGlow || config.enableShadow || config.enableOutline;
    }
}