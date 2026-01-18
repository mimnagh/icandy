package com.icandy.unit;

import com.icandy.run.VisualEffectsConfig;
import com.icandy.run.VisualEffectsManager;
import com.icandy.run.VisualEffectsState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for VisualEffectsManager.
 */
public class VisualEffectsManagerTest {
    
    private VisualEffectsManager effectsManager;
    private PApplet mockApplet;
    
    @BeforeEach
    void setUp() {
        // Create a minimal mock PApplet for testing
        mockApplet = new PApplet() {
            @Override
            public void setup() {
                // Minimal setup for testing
            }
        };
        mockApplet.width = 800;
        mockApplet.height = 600;
        
        effectsManager = new VisualEffectsManager(mockApplet);
    }
    
    @Test
    void testDefaultConfiguration() {
        VisualEffectsConfig config = effectsManager.getConfig();
        
        assertNotNull(config);
        assertFalse(config.enableBlur);
        assertEquals(2.0f, config.blurRadius);
        assertEquals(VisualEffectsConfig.ColorFilterType.NONE, config.colorFilter);
        assertEquals(1.0f, config.colorIntensity);
        assertEquals(0.0f, config.brightness);
        assertEquals(0.0f, config.contrast);
        assertEquals(1.0f, config.gamma);
        assertFalse(config.enableParticles);
        assertFalse(config.enableGlow);
        assertFalse(config.enableShadow);
        assertFalse(config.enableOutline);
    }
    
    @Test
    void testSetBlurEffect() {
        effectsManager.setBlurEffect(5.0f);
        
        VisualEffectsConfig config = effectsManager.getConfig();
        assertTrue(config.enableBlur);
        assertEquals(5.0f, config.blurRadius);
        assertFalse(config.enableMotionBlur);
        assertFalse(config.enableSelectiveBlur);
    }
    
    @Test
    void testSetBlurEffectWithTypes() {
        effectsManager.setBlurEffect(3.0f, true, false);
        
        VisualEffectsConfig config = effectsManager.getConfig();
        assertTrue(config.enableBlur);
        assertEquals(3.0f, config.blurRadius);
        assertTrue(config.enableMotionBlur);
        assertFalse(config.enableSelectiveBlur);
    }
    
    @Test
    void testSetColorFilter() {
        effectsManager.setColorFilter(VisualEffectsConfig.ColorFilterType.SEPIA, 1.5f);
        
        VisualEffectsConfig config = effectsManager.getConfig();
        assertEquals(VisualEffectsConfig.ColorFilterType.SEPIA, config.colorFilter);
        assertEquals(1.5f, config.colorIntensity);
    }
    
    @Test
    void testSetBrightnessContrast() {
        effectsManager.setBrightnessContrast(0.3f, -0.2f);
        
        VisualEffectsConfig config = effectsManager.getConfig();
        assertEquals(0.3f, config.brightness);
        assertEquals(-0.2f, config.contrast);
    }
    
    @Test
    void testSetBrightnessContrastGamma() {
        effectsManager.setBrightnessContrastGamma(0.2f, 0.1f, 1.5f);
        
        VisualEffectsConfig config = effectsManager.getConfig();
        assertEquals(0.2f, config.brightness);
        assertEquals(0.1f, config.contrast);
        assertEquals(1.5f, config.gamma);
    }
    
    @Test
    void testSetParticleSystem() {
        VisualEffectsConfig particleConfig = new VisualEffectsConfig();
        particleConfig.enableParticles = true;
        particleConfig.particleCount = 100;
        particleConfig.particleType = VisualEffectsConfig.ParticleType.FIRE;
        particleConfig.particleColor = 0xFFFF0000; // Red
        particleConfig.particleLifetime = 3000.0f;
        
        effectsManager.setParticleSystem(particleConfig);
        
        VisualEffectsConfig config = effectsManager.getConfig();
        assertTrue(config.enableParticles);
        assertEquals(100, config.particleCount);
        assertEquals(VisualEffectsConfig.ParticleType.FIRE, config.particleType);
        assertEquals(0xFFFF0000, config.particleColor);
        assertEquals(3000.0f, config.particleLifetime);
    }
    
    @Test
    void testSetBorderEffect() {
        effectsManager.setBorderEffect(true, 8.0f, 0xFFFFFFFF, true, 3.0f, 2.0f);
        
        VisualEffectsConfig config = effectsManager.getConfig();
        assertTrue(config.enableGlow);
        assertEquals(8.0f, config.glowRadius);
        assertEquals(0xFFFFFFFF, config.glowColor);
        assertTrue(config.enableShadow);
        assertEquals(3.0f, config.shadowOffsetX);
        assertEquals(3.0f, config.shadowOffsetY);
        assertEquals(2.0f, config.shadowBlur);
    }
    
    @Test
    void testSetAdvancedBorderEffects() {
        effectsManager.setAdvancedBorderEffects(
            true, 10.0f, 0xFFFFD700, // Glow: enabled, radius 10, gold color
            true, 2.0f, 3.0f, 1.5f, 0x80000000, // Shadow: enabled, offset (2,3), blur 1.5, black
            true, 2.5f, 0xFFFF0000 // Outline: enabled, thickness 2.5, red
        );
        
        VisualEffectsConfig config = effectsManager.getConfig();
        assertTrue(config.enableGlow);
        assertEquals(10.0f, config.glowRadius);
        assertEquals(0xFFFFD700, config.glowColor);
        assertTrue(config.enableShadow);
        assertEquals(2.0f, config.shadowOffsetX);
        assertEquals(3.0f, config.shadowOffsetY);
        assertEquals(1.5f, config.shadowBlur);
        assertEquals(0x80000000, config.shadowColor);
        assertTrue(config.enableOutline);
        assertEquals(2.5f, config.outlineThickness);
        assertEquals(0xFFFF0000, config.outlineColor);
    }
    
    @Test
    void testHasActiveEffects() {
        // Initially no effects
        assertFalse(effectsManager.hasActiveEffects());
        
        // Enable blur
        effectsManager.setBlurEffect(3.0f);
        assertTrue(effectsManager.hasActiveEffects());
        
        // Reset and enable color filter
        effectsManager.setConfig(new VisualEffectsConfig());
        assertFalse(effectsManager.hasActiveEffects());
        
        effectsManager.setColorFilter(VisualEffectsConfig.ColorFilterType.GRAYSCALE, 1.0f);
        assertTrue(effectsManager.hasActiveEffects());
    }
    
    @Test
    void testParticleManagement() {
        // Enable particles
        VisualEffectsConfig config = new VisualEffectsConfig();
        config.enableParticles = true;
        config.particleCount = 50;
        config.particleType = VisualEffectsConfig.ParticleType.SPARKLES;
        effectsManager.setConfig(config);
        
        // Initially no particles
        assertEquals(0, effectsManager.getActiveParticleCount());
        
        // Generate particles
        effectsManager.generateParticles(400, 300, 100, 100);
        assertTrue(effectsManager.getActiveParticleCount() > 0);
        
        // Clear particles
        effectsManager.clearParticles();
        assertEquals(0, effectsManager.getActiveParticleCount());
    }
    
    @Test
    void testConfigurationValidation() {
        VisualEffectsConfig config = new VisualEffectsConfig();
        
        // Test invalid values that should be clamped
        config.blurRadius = -5.0f; // Should be clamped to 0
        config.colorIntensity = 3.0f; // Should be clamped to 2.0
        config.brightness = -2.0f; // Should be clamped to -1.0
        config.contrast = 2.0f; // Should be clamped to 1.0
        config.gamma = 0.05f; // Should be clamped to 0.1
        config.particleCount = -10; // Should be clamped to 0
        
        config.validate();
        
        assertEquals(0.0f, config.blurRadius);
        assertEquals(2.0f, config.colorIntensity);
        assertEquals(-1.0f, config.brightness);
        assertEquals(1.0f, config.contrast);
        assertEquals(0.1f, config.gamma);
        assertEquals(0, config.particleCount);
    }
    
    @Test
    void testVisualEffectsState() {
        VisualEffectsState state = new VisualEffectsState();
        
        // Test default state
        assertFalse(state.hasActiveEffects());
        assertEquals(0.0f, state.getBlurRadius());
        assertEquals(VisualEffectsConfig.ColorFilterType.NONE, state.getColorFilter());
        
        // Apply configuration
        VisualEffectsConfig config = new VisualEffectsConfig();
        config.enableBlur = true;
        config.blurRadius = 5.0f;
        config.colorFilter = VisualEffectsConfig.ColorFilterType.VINTAGE;
        config.brightness = 0.2f;
        
        state.applyConfig(config);
        
        assertTrue(state.hasActiveEffects());
        assertEquals(5.0f, state.getBlurRadius());
        assertEquals(VisualEffectsConfig.ColorFilterType.VINTAGE, state.getColorFilter());
        assertEquals(0.2f, state.getBrightness());
        
        // Reset state
        state.reset();
        assertFalse(state.hasActiveEffects());
        assertEquals(0.0f, state.getBlurRadius());
        assertEquals(VisualEffectsConfig.ColorFilterType.NONE, state.getColorFilter());
    }
}