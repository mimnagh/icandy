package com.icandy.run;

import processing.core.PApplet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * BeatDetectorWrapper wraps Processing's BeatDetector for easier integration.
 * 
 * This class:
 * - Initializes Processing Sound library's BeatDetector
 * - Sets up microphone audio input
 * - Analyzes audio input for beats
 * - Provides simple boolean interface for beat detection
 * - Allows sensitivity configuration
 * - Handles audio input failures gracefully with fallback
 * 
 * Uses reflection to load Processing Sound library classes to avoid compile-time
 * dependency. If the library is not available, gracefully falls back to non-audio mode.
 * 
 * Requirements: 5.1, 5.2, 7.2, 8.2, 8.3
 */
public class BeatDetectorWrapper {
    
    private static final Logger LOGGER = Logger.getLogger(BeatDetectorWrapper.class.getName());
    private static final int DEFAULT_SENSITIVITY_MS = 100;
    private static final String AUDIO_IN_CLASS = "processing.sound.AudioIn";
    private static final String BEAT_DETECTOR_CLASS = "processing.sound.BeatDetector";
    
    private final PApplet parent;
    private Object audioInput;
    private Object beatDetector;
    private boolean initialized;
    private boolean audioAvailable;
    private int sensitivityMs;
    private long lastBeatTime;
    
    // Reflected methods
    private Method audioInStartMethod;
    private Method audioInStopMethod;
    private Method beatDetectorInputMethod;
    private Method beatDetectorSensitivityMethod;
    private Method beatDetectorIsBeatMethod;
    
    /**
     * Creates a new BeatDetectorWrapper.
     * 
     * @param parent The parent PApplet (Processing sketch)
     */
    public BeatDetectorWrapper(PApplet parent) {
        this.parent = parent;
        this.initialized = false;
        this.audioAvailable = false;
        this.sensitivityMs = DEFAULT_SENSITIVITY_MS;
        this.lastBeatTime = 0;
    }
    
    /**
     * Sets up the beat detector with microphone audio input.
     * 
     * This method:
     * - Initializes AudioIn for microphone input
     * - Creates and configures BeatDetector
     * - Handles audio input failures gracefully
     * - Falls back to non-audio mode if microphone unavailable or library not present
     * 
     * Requirements: 5.1, 5.2, 8.2, 8.3
     */
    public void setup() {
        try {
            // Try to load Processing Sound library classes using reflection
            Class<?> audioInClass = Class.forName(AUDIO_IN_CLASS);
            Class<?> beatDetectorClass = Class.forName(BEAT_DETECTOR_CLASS);
            Class<?> soundObjectClass = Class.forName("processing.sound.SoundObject");
            
            // Get constructors
            Constructor<?> audioInConstructor = audioInClass.getConstructor(PApplet.class, int.class);
            Constructor<?> beatDetectorConstructor = beatDetectorClass.getConstructor(PApplet.class);
            
            // Get methods
            audioInStartMethod = audioInClass.getMethod("start");
            audioInStopMethod = audioInClass.getMethod("stop");
            // The input method takes SoundObject, not AudioIn directly
            beatDetectorInputMethod = beatDetectorClass.getMethod("input", soundObjectClass);
            beatDetectorSensitivityMethod = beatDetectorClass.getMethod("sensitivity", int.class);
            beatDetectorIsBeatMethod = beatDetectorClass.getMethod("isBeat");
            
            // Create instances
            audioInput = audioInConstructor.newInstance(parent, 0);
            beatDetector = beatDetectorConstructor.newInstance(parent);
            
            // Start audio input
            audioInStartMethod.invoke(audioInput);
            
            // Configure beat detector
            beatDetectorInputMethod.invoke(beatDetector, audioInput);
            beatDetectorSensitivityMethod.invoke(beatDetector, sensitivityMs);
            
            audioAvailable = true;
            initialized = true;
            
            LOGGER.info("Beat detection initialized successfully with microphone input");
            
        } catch (ClassNotFoundException e) {
            // Processing Sound library not available
            audioAvailable = false;
            initialized = true;
            
            LOGGER.warning("Processing Sound library not found. Beat detection will not be available.");
            LOGGER.warning("To enable beat detection, install the Sound library in Processing IDE.");
            
        } catch (Exception e) {
            // Audio input failed - fall back to non-audio mode
            audioAvailable = false;
            initialized = true;
            
            LOGGER.warning("Failed to initialize audio input: " + e.getMessage());
            LOGGER.warning("Beat detection will not be available. Continuing without audio.");
        }
    }
    
    /**
     * Checks if a beat was detected.
     * 
     * This method returns true when an energy spike is detected in the audio input.
     * It respects the sensitivity setting to prevent detecting beats too frequently.
     * 
     * If audio is unavailable, this method always returns false.
     * 
     * @return true if a beat was detected, false otherwise
     * 
     * Requirements: 5.2
     */
    public boolean isBeat() {
        if (!initialized || !audioAvailable || beatDetector == null) {
            return false;
        }
        
        try {
            // Check if beat detector detected a beat using reflection
            Boolean detected = (Boolean) beatDetectorIsBeatMethod.invoke(beatDetector);
            
            if (detected != null && detected) {
                // Enforce sensitivity - don't report beats too frequently
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBeatTime >= sensitivityMs) {
                    lastBeatTime = currentTime;
                    LOGGER.info("Beat detected");
                    return true;
                }
                // Don't log ignored beats - just return false and let detector continue
            }
            
            return false;
            
        } catch (Exception e) {
            LOGGER.warning("Error checking beat detection: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sets the sensitivity for beat detection.
     * 
     * Sensitivity controls the minimum time between detected beats in milliseconds.
     * Lower values allow more frequent beat detection, higher values require more
     * time between beats.
     * 
     * @param milliseconds Minimum time between beats in milliseconds (must be positive)
     * 
     * Requirements: 5.2
     */
    public void setSensitivity(int milliseconds) {
        if (milliseconds <= 0) {
            LOGGER.warning("Invalid sensitivity value: " + milliseconds + ". Using default.");
            this.sensitivityMs = DEFAULT_SENSITIVITY_MS;
            return;
        }
        
        this.sensitivityMs = milliseconds;
        
        // Update beat detector if already initialized
        if (initialized && audioAvailable && beatDetector != null && beatDetectorSensitivityMethod != null) {
            try {
                beatDetectorSensitivityMethod.invoke(beatDetector, milliseconds);
                LOGGER.info("Beat detection sensitivity set to " + milliseconds + "ms");
            } catch (Exception e) {
                LOGGER.warning("Failed to update beat detector sensitivity: " + e.getMessage());
            }
        }
    }
    
    /**
     * Checks if audio input is available.
     * 
     * @return true if audio input is available and working, false otherwise
     */
    public boolean isAudioAvailable() {
        return audioAvailable;
    }
    
    /**
     * Checks if the beat detector has been initialized.
     * 
     * @return true if setup() has been called, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Gets the current sensitivity setting in milliseconds.
     * 
     * @return The sensitivity in milliseconds
     */
    public int getSensitivity() {
        return sensitivityMs;
    }
    
    /**
     * Stops the audio input and cleans up resources.
     * 
     * This method should be called when the sketch is closing.
     */
    public void stop() {
        if (audioInput != null && audioInStopMethod != null) {
            try {
                audioInStopMethod.invoke(audioInput);
                LOGGER.info("Audio input stopped");
            } catch (Exception e) {
                LOGGER.warning("Error stopping audio input: " + e.getMessage());
            }
        }
    }
}
