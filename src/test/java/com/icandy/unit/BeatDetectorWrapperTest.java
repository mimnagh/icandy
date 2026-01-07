package com.icandy.unit;

import com.icandy.run.BeatDetectorWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BeatDetectorWrapper.
 * 
 * Tests:
 * - Beat detection initialization
 * - Sensitivity configuration
 * - Fallback behavior when audio unavailable
 * 
 * Requirements: 5.1, 5.2, 8.2, 8.3
 */
class BeatDetectorWrapperTest {
    
    private PApplet mockApplet;
    private BeatDetectorWrapper beatDetector;
    
    @BeforeEach
    void setUp() {
        // Create a minimal PApplet for testing
        mockApplet = new PApplet();
        beatDetector = new BeatDetectorWrapper(mockApplet);
    }
    
    /**
     * Test that BeatDetectorWrapper can be created without errors.
     */
    @Test
    void testConstructor() {
        assertNotNull(beatDetector, "BeatDetectorWrapper should be created");
        assertFalse(beatDetector.isInitialized(), "Should not be initialized before setup()");
        assertFalse(beatDetector.isAudioAvailable(), "Audio should not be available before setup()");
    }
    
    /**
     * Test that setup() initializes the beat detector.
     * 
     * Note: This test will pass whether or not the Processing Sound library is available.
     * If the library is not available, the wrapper should gracefully fall back.
     */
    @Test
    void testSetup() {
        beatDetector.setup();
        
        assertTrue(beatDetector.isInitialized(), "Should be initialized after setup()");
        // Audio availability depends on whether Processing Sound library is present
        // and whether microphone is available, so we don't assert on it
    }
    
    /**
     * Test that isBeat() returns false when audio is not available.
     */
    @Test
    void testIsBeatWithoutAudio() {
        beatDetector.setup();
        
        // If audio is not available, isBeat() should always return false
        if (!beatDetector.isAudioAvailable()) {
            assertFalse(beatDetector.isBeat(), "isBeat() should return false when audio unavailable");
        }
    }
    
    /**
     * Test that isBeat() returns false before initialization.
     */
    @Test
    void testIsBeatBeforeInitialization() {
        assertFalse(beatDetector.isBeat(), "isBeat() should return false before initialization");
    }
    
    /**
     * Test sensitivity configuration with valid values.
     */
    @Test
    void testSetSensitivityValid() {
        int testSensitivity = 200;
        beatDetector.setSensitivity(testSensitivity);
        
        assertEquals(testSensitivity, beatDetector.getSensitivity(), 
            "Sensitivity should be set to the specified value");
    }
    
    /**
     * Test sensitivity configuration with invalid (zero) value.
     */
    @Test
    void testSetSensitivityZero() {
        beatDetector.setSensitivity(0);
        
        // Should use default value (100ms) when invalid value provided
        assertEquals(100, beatDetector.getSensitivity(), 
            "Sensitivity should use default value when zero provided");
    }
    
    /**
     * Test sensitivity configuration with invalid (negative) value.
     */
    @Test
    void testSetSensitivityNegative() {
        beatDetector.setSensitivity(-50);
        
        // Should use default value (100ms) when invalid value provided
        assertEquals(100, beatDetector.getSensitivity(), 
            "Sensitivity should use default value when negative provided");
    }
    
    /**
     * Test that sensitivity can be updated after initialization.
     */
    @Test
    void testSetSensitivityAfterSetup() {
        beatDetector.setup();
        
        int newSensitivity = 150;
        beatDetector.setSensitivity(newSensitivity);
        
        assertEquals(newSensitivity, beatDetector.getSensitivity(), 
            "Sensitivity should be updated after setup()");
    }
    
    /**
     * Test default sensitivity value.
     */
    @Test
    void testDefaultSensitivity() {
        assertEquals(100, beatDetector.getSensitivity(), 
            "Default sensitivity should be 100ms");
    }
    
    /**
     * Test that stop() can be called safely.
     */
    @Test
    void testStop() {
        beatDetector.setup();
        
        // Should not throw exception
        assertDoesNotThrow(() -> beatDetector.stop(), 
            "stop() should not throw exception");
    }
    
    /**
     * Test that stop() can be called before setup without errors.
     */
    @Test
    void testStopBeforeSetup() {
        // Should not throw exception even if not initialized
        assertDoesNotThrow(() -> beatDetector.stop(), 
            "stop() should not throw exception before setup()");
    }
    
    /**
     * Test fallback behavior when Processing Sound library is not available.
     * 
     * This test verifies that the wrapper handles missing library gracefully.
     */
    @Test
    void testFallbackBehavior() {
        beatDetector.setup();
        
        // After setup, should be initialized regardless of audio availability
        assertTrue(beatDetector.isInitialized(), 
            "Should be initialized even if audio unavailable");
        
        // If audio is not available, isBeat() should return false
        if (!beatDetector.isAudioAvailable()) {
            assertFalse(beatDetector.isBeat(), 
                "isBeat() should return false when audio unavailable");
        }
    }
    
    /**
     * Test that multiple calls to isBeat() don't cause errors.
     */
    @Test
    void testMultipleIsBeatCalls() {
        beatDetector.setup();
        
        // Should not throw exception on multiple calls
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                beatDetector.isBeat();
            }
        }, "Multiple isBeat() calls should not throw exception");
    }
    
    /**
     * Test that sensitivity enforcement prevents beats too close together.
     * 
     * This test verifies that even if the underlying beat detector reports
     * multiple beats, the wrapper enforces the sensitivity threshold.
     */
    @Test
    void testSensitivityEnforcement() {
        beatDetector.setup();
        beatDetector.setSensitivity(1000); // 1 second between beats
        
        // Multiple rapid calls to isBeat() should not all return true
        // (assuming beats are detected, which may not happen in test environment)
        int beatCount = 0;
        for (int i = 0; i < 100; i++) {
            if (beatDetector.isBeat()) {
                beatCount++;
            }
        }
        
        // In a test environment without audio, beatCount should be 0
        // In a real environment with audio, sensitivity should limit beats
        assertTrue(beatCount <= 1, 
            "Sensitivity should limit beat detection frequency");
    }
}
