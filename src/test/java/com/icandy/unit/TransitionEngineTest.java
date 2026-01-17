package com.icandy.unit;

import com.icandy.run.*;
import com.icandy.run.TransitionConfig.SlideDirection;
import com.icandy.run.TransitionConfig.ZoomMode;
import com.icandy.run.TransitionConfig.BlendMode;
import com.icandy.common.ConfigurationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TransitionEngine and transition effects.
 * Tests all transition types, configuration, and timing functionality.
 */
public class TransitionEngineTest {
    
    private TransitionEngine engine;
    private TransitionConfig config;
    private ImageInfo testImage;
    private ImageState startState;
    private ImageState targetState;
    
    @BeforeEach
    void setUp() {
        config = new TransitionConfig();
        engine = new TransitionEngine();
        
        // Create test image
        testImage = new ImageInfo(null, "test", "test.jpg", 100, 100);
        
        // Create test states
        startState = new ImageState(0, 0, 100, 100, 0, 1.0f, 1.0f, 1.0f);
        targetState = new ImageState(200, 150, 120, 120, 0.5f, 1.0f, 1.2f, 1.2f);
    }
    
    @Test
    void testTransitionEngineBasicFunctionality() {
        // Test engine initialization
        assertNotNull(engine);
        assertFalse(engine.hasActiveTransitions());
        assertEquals(0, engine.getActiveTransitionCount());
        assertTrue(engine.isEnabled());
        
        // Test setting effect and config
        FadeTransition fade = new FadeTransition();
        engine.setEffect(fade);
        engine.setConfig(config);
        
        assertEquals(fade, engine.getCurrentEffect());
        assertNotNull(engine.getConfig());
    }
    
    @Test
    void testImageTransition() {
        FadeTransition fade = new FadeTransition();
        engine.setEffect(fade);
        engine.setConfig(config);
        
        // Start a transition
        engine.startImageTransition(testImage, targetState);
        
        assertTrue(engine.hasActiveTransitions());
        assertEquals(1, engine.getActiveTransitionCount());
        
        // Update transition
        engine.update(100.0f); // 100ms elapsed
        
        ImageState currentState = engine.getCurrentImageState(testImage);
        assertNotNull(currentState);
        
        // Should still be transitioning
        assertTrue(engine.hasActiveTransitions());
        
        // Complete the transition
        engine.update(500.0f); // Total 600ms elapsed (> 500ms duration)
        
        assertFalse(engine.hasActiveTransitions());
        assertEquals(0, engine.getActiveTransitionCount());
    }
    
    @Test
    void testLayoutTransition() {
        FadeTransition fade = new FadeTransition();
        engine.setEffect(fade);
        
        // Test with stagger enabled
        config.setEnableStagger(true);
        config.setStaggerDelay(25.0f);
        engine.setConfig(config);
        
        ImageInfo[] images = {
            new ImageInfo(null, "test1", "test1.jpg", 100, 100),
            new ImageInfo(null, "test2", "test2.jpg", 100, 100),
            new ImageInfo(null, "test3", "test3.jpg", 100, 100)
        };
        
        ImagePosition[] positions = {
            new ImagePosition(0, 0, 100, 100),
            new ImagePosition(150, 0, 100, 100),
            new ImagePosition(300, 0, 100, 100)
        };
        
        engine.startLayoutTransition(images, positions);
        
        assertEquals(3, engine.getActiveTransitionCount());
        
        // Update and check stagger timing
        engine.update(30.0f); // 30ms elapsed
        
        // First image should be transitioning, others might not have started yet due to stagger
        assertTrue(engine.hasActiveTransitions());
    }
    
    @Test
    void testTransitionEngineDisabled() {
        engine.setEnabled(false);
        assertFalse(engine.isEnabled());
        
        FadeTransition fade = new FadeTransition();
        engine.setEffect(fade);
        
        // Should not start transitions when disabled
        engine.startImageTransition(testImage, targetState);
        
        assertFalse(engine.hasActiveTransitions());
        assertEquals(0, engine.getActiveTransitionCount());
    }
    
    @Test
    void testStopTransitions() {
        FadeTransition fade = new FadeTransition();
        engine.setEffect(fade);
        engine.setConfig(config);
        
        // Start multiple transitions
        ImageInfo image1 = new ImageInfo(null, "test1", "test1.jpg", 100, 100);
        ImageInfo image2 = new ImageInfo(null, "test2", "test2.jpg", 100, 100);
        
        engine.startImageTransition(image1, targetState);
        engine.startImageTransition(image2, targetState);
        
        assertEquals(2, engine.getActiveTransitionCount());
        
        // Stop specific transition
        engine.stopTransition(image1);
        assertEquals(1, engine.getActiveTransitionCount());
        
        // Stop all transitions
        engine.stopAllTransitions();
        assertEquals(0, engine.getActiveTransitionCount());
        assertFalse(engine.hasActiveTransitions());
    }
    
    @Test
    void testFadeTransition() {
        FadeTransition fade = new FadeTransition(EasingFunction.LINEAR);
        
        assertEquals("Fade", fade.getName());
        assertEquals(EasingFunction.LINEAR, fade.getEasingFunction());
        
        // Test transition
        fade.startTransition(startState, targetState, 1000.0f);
        assertFalse(fade.isComplete());
        
        // Test progress
        ImageState midState = fade.updateTransition(0.5f);
        assertNotNull(midState);
        assertFalse(fade.isComplete());
        
        // Complete transition
        ImageState finalState = fade.updateTransition(1.0f);
        assertNotNull(finalState);
        assertTrue(fade.isComplete());
        
        // Test cross-fade functionality
        fade.startCrossFade(startState, targetState, 500.0f);
        ImageState[] crossFadeStates = fade.updateCrossFade(0.5f);
        assertEquals(2, crossFadeStates.length);
        assertNotNull(crossFadeStates[0]); // Outgoing state
        assertNotNull(crossFadeStates[1]); // Incoming state
    }
    
    @Test
    void testSlideTransition() {
        SlideTransition slide = new SlideTransition(SlideDirection.RIGHT, EasingFunction.EASE_OUT, true);
        
        assertEquals("Slide", slide.getName());
        assertEquals(SlideDirection.RIGHT, slide.getSlideDirection());
        assertEquals(EasingFunction.EASE_OUT, slide.getEasingFunction());
        assertTrue(slide.isEnableBounce());
        
        // Set screen dimensions
        slide.setScreenDimensions(800, 600);
        
        // Test transition
        slide.startTransition(startState, targetState, 1000.0f);
        assertFalse(slide.isComplete());
        
        ImageState midState = slide.updateTransition(0.5f);
        assertNotNull(midState);
        
        ImageState finalState = slide.updateTransition(1.0f);
        assertTrue(slide.isComplete());
        
        // Test direction changes
        slide.setSlideDirection(SlideDirection.UP);
        assertEquals(SlideDirection.UP, slide.getSlideDirection());
    }
    
    @Test
    void testZoomTransition() {
        ZoomTransition zoom = new ZoomTransition(ZoomMode.ZOOM_BOTH, EasingFunction.BOUNCE, true);
        
        assertEquals("Zoom", zoom.getName());
        assertEquals(ZoomMode.ZOOM_BOTH, zoom.getZoomMode());
        assertEquals(EasingFunction.BOUNCE, zoom.getEasingFunction());
        assertTrue(zoom.isCombineFade());
        
        // Set zoom center
        zoom.setZoomCenter(0.3f, 0.7f);
        
        // Test transition
        zoom.startTransition(startState, targetState, 800.0f);
        assertFalse(zoom.isComplete());
        
        ImageState midState = zoom.updateTransition(0.5f);
        assertNotNull(midState);
        
        ImageState finalState = zoom.updateTransition(1.0f);
        assertTrue(zoom.isComplete());
        
        // Test mode changes
        zoom.setZoomMode(ZoomMode.ZOOM_OUT);
        assertEquals(ZoomMode.ZOOM_OUT, zoom.getZoomMode());
    }
    
    @Test
    void testRotateTransition() {
        float rotationAngle = (float) Math.PI; // 180 degrees
        RotateTransition rotate = new RotateTransition(rotationAngle, false, EasingFunction.ELASTIC);
        
        assertEquals("Rotate", rotate.getName());
        assertEquals(rotationAngle, rotate.getRotationAngle(), 0.001f);
        assertFalse(rotate.isClockwise());
        assertEquals(EasingFunction.ELASTIC, rotate.getEasingFunction());
        
        // Set rotation center
        rotate.setRotationCenter(0.25f, 0.75f);
        
        // Test transition
        rotate.startTransition(startState, targetState, 1200.0f);
        assertFalse(rotate.isComplete());
        
        ImageState midState = rotate.updateTransition(0.5f);
        assertNotNull(midState);
        
        ImageState finalState = rotate.updateTransition(1.0f);
        assertTrue(rotate.isComplete());
        
        // Test configuration changes
        rotate.setCombineScale(false);
        assertFalse(rotate.isCombineScale());
        
        rotate.setCombineFade(false);
        assertFalse(rotate.isCombineFade());
    }
    
    @Test
    void testMorphTransition() {
        MorphTransition morph = new MorphTransition(BlendMode.OVERLAY, EasingFunction.EASE_IN_OUT, 0.8f);
        
        assertEquals("Morph", morph.getName());
        assertEquals(BlendMode.OVERLAY, morph.getBlendMode());
        assertEquals(EasingFunction.EASE_IN_OUT, morph.getEasingFunction());
        assertEquals(0.8f, morph.getMorphIntensity(), 0.001f);
        
        // Test transition
        morph.startTransition(startState, targetState, 1500.0f);
        assertFalse(morph.isComplete());
        
        ImageState midState = morph.updateTransition(0.5f);
        assertNotNull(midState);
        
        ImageState finalState = morph.updateTransition(1.0f);
        assertTrue(morph.isComplete());
        
        // Test configuration changes
        morph.setEnableVertexMorph(false);
        assertFalse(morph.isEnableVertexMorph());
        
        morph.setEnableColorBlend(false);
        assertFalse(morph.isEnableColorBlend());
        
        morph.setMorphIntensity(0.5f);
        assertEquals(0.5f, morph.getMorphIntensity(), 0.001f);
    }
    
    @Test
    void testEasingFunctions() {
        // Test most easing functions (excluding elastic and bounce which can overshoot)
        EasingFunction[] normalEasings = {
            EasingFunction.LINEAR, EasingFunction.EASE_IN, 
            EasingFunction.EASE_OUT, EasingFunction.EASE_IN_OUT
        };
        
        for (EasingFunction easing : normalEasings) {
            float result = easing.apply(0.5f);
            assertTrue(result >= 0.0f && result <= 1.0f, 
                "Easing function " + easing + " returned invalid value: " + result);
        }
        
        // Test elastic and bounce separately (they can overshoot)
        float elasticResult = EasingFunction.ELASTIC.apply(0.5f);
        assertTrue(elasticResult >= 0.0f, "Elastic easing should not go below 0");
        
        float bounceResult = EasingFunction.BOUNCE.apply(0.5f);
        assertTrue(bounceResult >= 0.0f && bounceResult <= 1.2f, 
            "Bounce easing should stay within reasonable bounds");
        
        // Test boundary conditions
        assertEquals(0.0f, EasingFunction.LINEAR.apply(0.0f), 0.001f);
        assertEquals(1.0f, EasingFunction.LINEAR.apply(1.0f), 0.001f);
        assertEquals(0.5f, EasingFunction.LINEAR.apply(0.5f), 0.001f);
        
        // Test clamping
        assertEquals(0.0f, EasingFunction.LINEAR.apply(-0.5f), 0.001f);
        assertEquals(1.0f, EasingFunction.LINEAR.apply(1.5f), 0.001f);
    }
    
    @Test
    void testTransitionConfig() {
        TransitionConfig config = new TransitionConfig();
        
        // Test defaults
        assertEquals(500.0f, config.getDuration(), 0.001f);
        assertEquals(EasingFunction.EASE_IN_OUT, config.getEasingFunction());
        assertFalse(config.isEnableStagger());
        assertEquals(50.0f, config.getStaggerDelay(), 0.001f);
        
        // Test setters with validation
        config.setDuration(1000.0f);
        assertEquals(1000.0f, config.getDuration(), 0.001f);
        
        config.setDuration(-100.0f); // Should clamp to minimum
        assertEquals(1.0f, config.getDuration(), 0.001f);
        
        config.setEasingFunction(EasingFunction.BOUNCE);
        assertEquals(EasingFunction.BOUNCE, config.getEasingFunction());
        
        config.setEasingFunction(null); // Should use default
        assertEquals(EasingFunction.LINEAR, config.getEasingFunction());
        
        // Test copy functionality
        TransitionConfig copy = config.copy();
        assertNotSame(config, copy);
        assertEquals(config.getDuration(), copy.getDuration(), 0.001f);
        assertEquals(config.getEasingFunction(), copy.getEasingFunction());
    }
    
    @Test
    void testImageState() {
        ImageState state = new ImageState();
        
        // Test defaults
        assertEquals(0.0f, state.getX(), 0.001f);
        assertEquals(0.0f, state.getY(), 0.001f);
        assertEquals(1.0f, state.getOpacity(), 0.001f);
        
        // Test setters with validation
        state.setOpacity(1.5f); // Should clamp to 1.0
        assertEquals(1.0f, state.getOpacity(), 0.001f);
        
        state.setOpacity(-0.5f); // Should clamp to 0.0
        assertEquals(0.0f, state.getOpacity(), 0.001f);
        
        // Test lerp functionality
        ImageState start = new ImageState(0, 0, 100, 100, 0, 0.0f, 1.0f, 1.0f);
        ImageState end = new ImageState(200, 200, 200, 200, 1.0f, 1.0f, 2.0f, 2.0f);
        
        ImageState mid = start.lerp(end, 0.5f);
        assertEquals(100.0f, mid.getX(), 0.001f);
        assertEquals(100.0f, mid.getY(), 0.001f);
        assertEquals(0.5f, mid.getOpacity(), 0.001f);
        
        // Test conversion to ImagePosition
        ImagePosition position = state.toImagePosition();
        assertNotNull(position);
    }
    
    @Test
    void testTransitionEffectFactory() {
        // Test effect creation by name
        TransitionEffect fade = TransitionEffectFactory.createEffect("fade");
        assertNotNull(fade);
        assertTrue(fade instanceof FadeTransition);
        
        TransitionEffect slide = TransitionEffectFactory.createEffect("slide");
        assertNotNull(slide);
        assertTrue(slide instanceof SlideTransition);
        
        TransitionEffect zoom = TransitionEffectFactory.createEffect("zoom");
        assertNotNull(zoom);
        assertTrue(zoom instanceof ZoomTransition);
        
        TransitionEffect rotate = TransitionEffectFactory.createEffect("rotate");
        assertNotNull(rotate);
        assertTrue(rotate instanceof RotateTransition);
        
        TransitionEffect morph = TransitionEffectFactory.createEffect("morph");
        assertNotNull(morph);
        assertTrue(morph instanceof MorphTransition);
        
        // Test invalid effect name
        TransitionEffect invalid = TransitionEffectFactory.createEffect("invalid");
        assertNull(invalid);
        
        // Test null effect name
        TransitionEffect nullEffect = TransitionEffectFactory.createEffect((String) null);
        assertNull(nullEffect);
    }
    
    @Test
    void testConfigurationManagerIntegration() {
        ConfigurationManager configManager = new ConfigurationManager();
        
        // Test factory with configuration - cast to avoid ambiguity
        TransitionEffect effect = TransitionEffectFactory.createEffect((ConfigurationManager) configManager);
        assertNotNull(effect);
        assertTrue(effect instanceof FadeTransition); // Default effect
        
        TransitionConfig config = TransitionEffectFactory.createConfig(configManager);
        assertNotNull(config);
        assertEquals(500.0f, config.getDuration(), 0.001f); // Default duration
        
        // Test with null configuration
        TransitionEffect nullConfigEffect = TransitionEffectFactory.createEffect((ConfigurationManager) null);
        assertNotNull(nullConfigEffect);
        assertTrue(nullConfigEffect instanceof FadeTransition);
    }
}