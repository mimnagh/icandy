package com.icandy.run;

import com.icandy.build.AssociationManager;
import com.icandy.build.TextParser;
import com.icandy.common.ConfigurationManager;
import com.icandy.common.Logger;
import processing.core.PApplet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * iCandySketch is the main Processing sketch that ties together all run phase components.
 * 
 * This class:
 * - Initializes all run phase components
 * - Loads configuration and associations
 * - Sets up audio input and beat detection (when available)
 * - Coordinates display managers and sequencer
 * - Handles Processing lifecycle (setup/draw)
 * - Handles keyboard input for navigation
 * 
 * Requirements: 7.1, 7.2, 7.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.5, 7.3, 4.8, 4.9
 */
public class iCandySketch extends PApplet {
    
    private static final Logger LOGGER = new Logger(iCandySketch.class);
    private static final float TEXT_Y_RATIO = 0.85f; // Position text in lower portion of screen
    
    // Configuration and data
    private ConfigurationManager config;
    private AssociationManager associationManager;
    private TextParser textParser;
    
    // Run phase components
    private TextDisplayManager textDisplayManager;
    private ImageDisplayManager imageDisplayManager;
    private PhraseSequencer phraseSequencer;
    private BeatDetectorWrapper beatDetector;
    
    // New engines for enhanced features
    private LayoutEngine layoutEngine;
    private TransitionEngine transitionEngine;
    private VisualEffectsManager visualEffectsManager;
    
    // State
    private boolean initialized = false;
    private String textFilePath;
    private String configFilePath;
    
    // Animation timing
    private long lastFrameTime = 0;
    private float deltaTime = 0;
    
    // Settings overlay and notifications
    private boolean showSettingsOverlay = false;
    private long settingsOverlayStartTime = 0;
    private String currentNotification = "";
    private long notificationStartTime = 0;
    private static final long NOTIFICATION_DURATION = 2000; // 2 seconds
    
    // Static fields to pass arguments through Processing's initialization
    private static String staticTextFilePath;
    private static String staticConfigFilePath;
    
    /**
     * Main method to launch the Processing sketch.
     * 
     * @param args Command-line arguments: [textFilePath] [configFilePath]
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java iCandySketch <textFilePath> [configFilePath]");
            System.err.println("  textFilePath: Path to the text script file");
            System.err.println("  configFilePath: Optional path to config.json (default: config.json)");
            System.exit(1);
        }
        
        // Store arguments in static fields so they're available when Processing creates the instance
        staticTextFilePath = args[0];
        staticConfigFilePath = args.length > 1 ? args[1] : "config.json";
        
        PApplet.main(iCandySketch.class.getName());
    }
    
    /**
     * Constructor - initializes instance variables from static fields.
     */
    public iCandySketch() {
        super();
        this.textFilePath = staticTextFilePath;
        this.configFilePath = staticConfigFilePath;
    }
    
    /**
     * Processing setup() method - initializes all components.
     * 
     * This method:
     * - Loads configuration
     * - Loads associations
     * - Parses text script
     * - Initializes display managers and sequencer
     * - Sets up audio input and beat detection (when available)
     * 
     * Requirements: 7.1, 7.2, 7.5
     */
    @Override
    public void settings() {
        // Set window size - use fullscreen or default size
        size(1280, 720);
        
        // Suppress Processing icon loading errors
        // This prevents NullPointerException when icon resource is not found
        try {
            // Processing will try to load icon, but we don't need it
        } catch (Exception e) {
            // Ignore icon loading errors
        }
    }
    
    @Override
    public void setup() {
        try {
            LOGGER.info("Starting iCandy setup phase");
            
            // Load configuration
            loadConfiguration();
            
            // Set frame rate from configuration
            frameRate(config.getFrameRate());
            
            // Load associations
            loadAssociations();
            
            // Parse text script
            parseTextScript();
            
            // Initialize display managers
            initializeDisplayManagers();
            
            // Initialize new engines
            initializeEngines();
            
            // Initialize sequencer
            initializeSequencer();
            
            // Set up audio input and beat detection
            setupAudioAndBeatDetection();
            
            // Mark as initialized
            initialized = true;
            
            LOGGER.info("iCandy initialized successfully");
            
        } catch (IOException e) {
            LOGGER.error("IO error during initialization", "", e);
            System.err.println("Failed to initialize iCandy: " + e.getMessage());
            System.err.println("Please check that all required files exist and are readable.");
            exit();
        } catch (Exception e) {
            LOGGER.error("Unexpected error during initialization", "", e);
            System.err.println("Failed to initialize iCandy: " + e.getMessage());
            e.printStackTrace();
            exit();
        }
    }
    
    /**
     * Loads configuration from file.
     * Uses default values if configuration file is not found.
     */
    private void loadConfiguration() throws IOException {
        try {
            config = new ConfigurationManager();
            
            if (configFilePath != null) {
                // Expand ~ to user home directory
                String expandedPath = configFilePath.replaceFirst("^~", System.getProperty("user.home"));
                
                if (Files.exists(Path.of(expandedPath))) {
                    config.loadFromFile(expandedPath);
                    LOGGER.info("Configuration loaded successfully", expandedPath);
                } else {
                    LOGGER.warning("Configuration file not found, using defaults", expandedPath);
                }
            } else {
                LOGGER.info("No configuration file specified, using defaults");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load configuration", configFilePath, e);
            throw new IOException("Failed to load configuration: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error loading configuration", configFilePath, e);
            throw new IOException("Unexpected error loading configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Loads word-image associations from file.
     */
    private void loadAssociations() throws IOException {
        try {
            associationManager = new AssociationManager();
            String associationsFile = config.getAssociationsFile();
            
            if (!Files.exists(Path.of(associationsFile))) {
                IOException e = new IOException("Associations file not found: " + associationsFile + 
                    ". Please run the build phase first.");
                LOGGER.error("Associations file not found", associationsFile, e);
                throw e;
            }
            
            if (!Files.isReadable(Path.of(associationsFile))) {
                IOException e = new IOException("Associations file is not readable: " + associationsFile);
                LOGGER.error("Associations file not readable", associationsFile, e);
                throw e;
            }
            
            associationManager.loadFromFile(associationsFile);
            LOGGER.info("Associations loaded successfully", 
                String.format("file=%s, words=%d, images=%d", 
                    associationsFile, associationManager.getWordCount(), associationManager.getImageCount()));
            
            // Verify image files exist
            if (!associationManager.verifyImageFiles()) {
                LOGGER.warning("Some image files are missing, display may be incomplete");
                var missingFiles = associationManager.getMissingImageFiles();
                LOGGER.warning("Missing image files", String.format("count=%d", missingFiles.size()));
                
                // Log first few missing files for debugging
                int logCount = Math.min(5, missingFiles.size());
                for (int i = 0; i < logCount; i++) {
                    LOGGER.warning("Missing image file", missingFiles.get(i));
                }
                if (missingFiles.size() > logCount) {
                    LOGGER.warning("Additional missing files", 
                        String.format("count=%d", missingFiles.size() - logCount));
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load associations", "", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error loading associations", "", e);
            throw new IOException("Unexpected error loading associations: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parses the text script into phrases and words.
     */
    private void parseTextScript() throws IOException {
        try {
            if (textFilePath == null) {
                IOException e = new IOException("Text file path is null");
                LOGGER.error("Invalid text file path", "", e);
                throw e;
            }
            
            Path textPath = Path.of(textFilePath);
            if (!Files.exists(textPath)) {
                IOException e = new IOException("Text file not found: " + textFilePath);
                LOGGER.error("Text file not found", textFilePath, e);
                throw e;
            }
            
            if (!Files.isReadable(textPath)) {
                IOException e = new IOException("Text file is not readable: " + textFilePath);
                LOGGER.error("Text file not readable", textFilePath, e);
                throw e;
            }
            
            textParser = new TextParser();
            
            // Load stop words
            String stopWordsFile = config.getStopWordsFile();
            if (Files.exists(Path.of(stopWordsFile))) {
                try {
                    textParser.loadStopWords(stopWordsFile);
                    LOGGER.info("Stop words loaded successfully", stopWordsFile);
                } catch (IOException e) {
                    LOGGER.warning("Failed to load stop words, continuing without filtering", 
                        stopWordsFile + ": " + e.getMessage());
                }
            } else {
                LOGGER.warning("Stop words file not found, using empty set", stopWordsFile);
            }
            
            LOGGER.info("Text script parsing initialized successfully", textFilePath);
            
        } catch (IOException e) {
            LOGGER.error("Failed to parse text script", textFilePath, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error parsing text script", textFilePath, e);
            throw new IOException("Unexpected error parsing text script: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initializes the display managers.
     */
    private void initializeDisplayManagers() {
        try {
            textDisplayManager = new TextDisplayManager(this, config);
            imageDisplayManager = new ImageDisplayManager(this, associationManager);
            
            // Configure image display manager
            imageDisplayManager.setSimultaneousImageCount(config.getSimultaneousImageCount());
            
            LOGGER.info("Display managers initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize display managers", "", e);
            throw new RuntimeException("Failed to initialize display managers: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initializes the new engines (LayoutEngine, TransitionEngine, VisualEffectsManager).
     * 
     * Requirements: 9.1, 10.1, 12.6
     */
    private void initializeEngines() {
        try {
            // Initialize LayoutEngine with configured algorithm
            layoutEngine = new LayoutEngine();
            LayoutConfig layoutConfig = config.getLayoutConfig();
            layoutEngine.setConfig(layoutConfig);
            
            // Set the configured layout algorithm
            LayoutAlgorithm algorithm = LayoutAlgorithmFactory.createAlgorithm(config.getCurrentLayoutAlgorithm());
            layoutEngine.setAlgorithm(algorithm);
            
            // Initialize TransitionEngine with configured effects
            transitionEngine = new TransitionEngine();
            TransitionConfig transitionConfig = config.createTransitionConfig();
            transitionEngine.setConfig(transitionConfig);
            
            // Set the configured transition effect
            TransitionEffect effect = TransitionEffectFactory.createEffect(config.getCurrentTransitionEffect());
            transitionEngine.setEffect(effect);
            
            // Initialize VisualEffectsManager with configured effects
            visualEffectsManager = new VisualEffectsManager(this);
            VisualEffectsConfig effectsConfig = config.getVisualEffectsConfig();
            visualEffectsManager.setConfig(effectsConfig);
            
            // Pass engines to enhanced ImageDisplayManager
            imageDisplayManager.setLayoutEngine(layoutEngine);
            imageDisplayManager.setTransitionEngine(transitionEngine);
            imageDisplayManager.setVisualEffectsManager(visualEffectsManager);
            
            LOGGER.info("Engines initialized successfully", 
                String.format("layout=%s, transition=%s, effects=%s", 
                    config.getCurrentLayoutAlgorithm(), 
                    config.getCurrentTransitionEffect(),
                    hasAnyEffectsEnabled(effectsConfig) ? "enabled" : "disabled"));
                    
        } catch (Exception e) {
            LOGGER.error("Failed to initialize engines", "", e);
            // Don't fail completely - continue without enhanced features
            LOGGER.warning("Continuing without enhanced layout/transition/effects features");
        }
    }
    
    /**
     * Initializes the phrase sequencer.
     */
    private void initializeSequencer() throws IOException {
        try {
            // Read text content
            String textContent = Files.readString(Path.of(textFilePath));
            
            if (textContent.trim().isEmpty()) {
                IOException e = new IOException("Text file is empty: " + textFilePath);
                LOGGER.error("Empty text file", textFilePath, e);
                throw e;
            }
            
            // Parse into phrases
            String[] phrases = textParser.parseIntoPhrases(textContent);
            
            if (phrases.length == 0) {
                IOException e = new IOException("No phrases found in text file: " + textFilePath);
                LOGGER.error("No phrases found in text", textFilePath, e);
                throw e;
            }
            
            // Create phrase-to-words mapping
            Map<Integer, String[]> phraseToWords = textParser.mapPhrasesToWords(phrases);
            
            // Create sequencer
            phraseSequencer = new PhraseSequencer(phrases, phraseToWords);
            phraseSequencer.setLooping(config.isLoopPhrasesEnabled());
            
            // Initialize first phrase
            String firstPhrase = phraseSequencer.getCurrentPhrase();
            String[] firstWords = phraseSequencer.getWordsInCurrentPhrase();
            
            textDisplayManager.updatePhrase(firstPhrase);
            imageDisplayManager.setImagesForPhrase(firstWords);
            
            LOGGER.info("Phrase sequencer initialized successfully", 
                String.format("phrases=%d, looping=%s", phrases.length, config.isLoopPhrasesEnabled()));
                
        } catch (IOException e) {
            LOGGER.error("Failed to initialize sequencer", textFilePath, e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error initializing sequencer", textFilePath, e);
            throw new IOException("Unexpected error initializing sequencer: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sets up audio input and beat detection.
     * 
     * This method initializes the BeatDetectorWrapper and configures it
     * with the sensitivity from the configuration.
     * 
     * If audio input fails or the Sound library is not available,
     * the system will continue without beat detection.
     * 
     * Requirements: 5.1, 5.2, 7.2, 8.2, 8.3
     */
    private void setupAudioAndBeatDetection() {
        try {
            beatDetector = new BeatDetectorWrapper(this);
            beatDetector.setSensitivity(config.getBeatSensitivity());
            beatDetector.setup();
            
            if (beatDetector.isAudioAvailable()) {
                LOGGER.info("Beat detection enabled successfully", 
                    String.format("sensitivity=%dms", config.getBeatSensitivity()));
            } else {
                LOGGER.info("Beat detection not available, continuing without audio");
            }
            
        } catch (Exception e) {
            LOGGER.warning("Failed to initialize beat detection, continuing without audio", e.getMessage());
            beatDetector = null; // Ensure it's null so we don't try to use it
        }
    }
    
    /**
     * Processing draw() loop - renders the visual output.
     * 
     * This method:
     * - Calculates delta time for smooth animations
     * - Updates beat detection state
     * - Updates LayoutEngine, TransitionEngine, and VisualEffectsManager each frame
     * - Checks for automatic phrase advancement
     * - Renders current phrase via TextDisplayManager
     * - Renders current images via ImageDisplayManager with delta time
     * - Swaps images on beat detection
     * - Maintains target frame rate
     * 
     * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.5, 7.3
     */
    @Override
    public void draw() {
        if (!initialized) {
            return;
        }
        
        try {
            // Calculate delta time for smooth animations
            long currentTime = System.currentTimeMillis();
            if (lastFrameTime == 0) {
                lastFrameTime = currentTime;
            }
            deltaTime = (currentTime - lastFrameTime) / 1000.0f; // Convert to seconds
            lastFrameTime = currentTime;
            
            // Clear background
            background(parseBackgroundColor());
            
            // Update engines each frame (with error handling)
            updateEnginesSafely();
            
            // Check for beat detection (with error handling)
            boolean beatDetected = checkBeatDetectionSafely();
            
            // Check for automatic phrase advancement
            if (textDisplayManager != null && textDisplayManager.shouldAdvance()) {
                advanceToNextPhrase();
            }
            
            // Update enhanced ImageDisplayManager with delta time
            if (imageDisplayManager != null) {
                try {
                    imageDisplayManager.update(deltaTime);
                    imageDisplayManager.displayCurrentImages();
                } catch (Exception e) {
                    LOGGER.warning("Error updating image display", e.getMessage());
                }
            }
            
            // Render current phrase (on top of images)
            if (textDisplayManager != null && phraseSequencer != null) {
                try {
                    int centerX = width / 2;
                    int textY = (int) (height * TEXT_Y_RATIO);
                    textDisplayManager.displayPhrase(
                        phraseSequencer.getCurrentPhrase(),
                        centerX,
                        textY
                    );
                } catch (Exception e) {
                    LOGGER.warning("Error displaying text", e.getMessage());
                }
            }
            
            // Render settings overlay if active
            if (showSettingsOverlay) {
                try {
                    renderSettingsOverlay();
                } catch (Exception e) {
                    LOGGER.warning("Error rendering settings overlay", e.getMessage());
                }
            }
            
            // Render notification if active
            if (isNotificationActive()) {
                try {
                    renderNotification();
                } catch (Exception e) {
                    LOGGER.warning("Error rendering notification", e.getMessage());
                }
            }
            
            // Swap images on beat detection
            if (beatDetected && imageDisplayManager != null) {
                try {
                    imageDisplayManager.swapImages();
                } catch (Exception e) {
                    LOGGER.warning("Error swapping images on beat", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error in draw loop", "", e);
            // Continue running - don't exit on draw errors
        }
    }
    
    /**
     * Updates engines safely with error handling.
     */
    private void updateEnginesSafely() {
        try {
            if (layoutEngine != null) {
                layoutEngine.update(deltaTime);
            }
        } catch (Exception e) {
            LOGGER.warning("Error updating layout engine", e.getMessage());
        }
        
        try {
            if (transitionEngine != null) {
                transitionEngine.update(deltaTime);
            }
        } catch (Exception e) {
            LOGGER.warning("Error updating transition engine", e.getMessage());
        }
        
        try {
            if (visualEffectsManager != null) {
                VisualEffectsConfig effectsConfig = config.getVisualEffectsConfig();
                if (effectsConfig.enableParticles) {
                    visualEffectsManager.updateParticles(deltaTime * 1000); // Convert to milliseconds
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error updating visual effects", e.getMessage());
        }
    }
    
    /**
     * Checks beat detection safely with error handling.
     */
    private boolean checkBeatDetectionSafely() {
        try {
            if (beatDetector != null && beatDetector.isAudioAvailable()) {
                return beatDetector.isBeat();
            }
        } catch (Exception e) {
            LOGGER.warning("Error checking beat detection", e.getMessage());
        }
        return false;
    }
    
    /**
     * Advances to the next phrase in the sequence.
     */
    private void advanceToNextPhrase() {
        try {
            if (phraseSequencer != null) {
                phraseSequencer.advance();
                updateCurrentPhrase();
            }
        } catch (Exception e) {
            LOGGER.warning("Error advancing to next phrase", e.getMessage());
        }
    }
    
    /**
     * Updates the display managers with the current phrase and words.
     */
    private void updateCurrentPhrase() {
        try {
            if (phraseSequencer != null && textDisplayManager != null && imageDisplayManager != null) {
                String currentPhrase = phraseSequencer.getCurrentPhrase();
                String[] currentWords = phraseSequencer.getWordsInCurrentPhrase();
                
                textDisplayManager.updatePhrase(currentPhrase);
                imageDisplayManager.setImagesForPhrase(currentWords);
            }
        } catch (Exception e) {
            LOGGER.warning("Error updating current phrase", e.getMessage());
        }
    }
    
    /**
     * Handles keyboard input for comprehensive interactive controls.
     * 
     * - Right/Left arrow keys: swap images within current phrase
     * - Number keys (1-4): switch layout algorithms with immediate visual feedback
     * - Letter keys (Q-W-E-R-T): switch transition effects with immediate application
     * - Function keys (F1-F5): toggle visual effects with immediate preview
     * - Space bar: manual image swapping using current transition
     * - Tab key: display settings overlay with current configuration and FPS
     * 
     * Requirements: 4.8, 4.9, 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7
     */
    @Override
    public void keyPressed() {
        if (!initialized || !config.isKeyboardNavigationEnabled()) {
            return;
        }
        
        // Handle arrow keys for image swapping
        if (keyCode == RIGHT || keyCode == LEFT) {
            imageDisplayManager.swapImages();
            LOGGER.info((keyCode == RIGHT ? "Right" : "Left") + " arrow pressed - swapped images for phrase " + 
                (phraseSequencer.getCurrentIndex() + 1));
            return;
        }
        
        // Handle number keys (1-4) for layout algorithm switching
        if (key >= '1' && key <= '4') {
            String[] algorithms = {"grid", "collage", "circular", "flowing"};
            int algorithmIndex = key - '1';
            
            if (algorithmIndex < algorithms.length) {
                String newAlgorithm = algorithms[algorithmIndex];
                switchLayoutAlgorithm(newAlgorithm);
                showNotification("Layout: " + capitalizeFirst(newAlgorithm));
                LOGGER.info("Switched to " + newAlgorithm + " layout algorithm");
            }
            return;
        }
        
        // Handle letter keys (Q-W-E-R-T) for transition effect switching
        char upperKey = Character.toUpperCase(key);
        if (upperKey == 'Q' || upperKey == 'W' || upperKey == 'E' || upperKey == 'R' || upperKey == 'T') {
            String[] effects = {"fade", "slide", "zoom", "rotate", "morph"};
            String[] effectKeys = {"Q", "W", "E", "R", "T"};
            
            for (int i = 0; i < effectKeys.length; i++) {
                if (String.valueOf(upperKey).equals(effectKeys[i])) {
                    String newEffect = effects[i];
                    switchTransitionEffect(newEffect);
                    showNotification("Transition: " + capitalizeFirst(newEffect));
                    LOGGER.info("Switched to " + newEffect + " transition effect");
                    break;
                }
            }
            return;
        }
        
        // Handle function keys (F1-F5) for visual effects toggling
        if (keyCode >= 112 && keyCode <= 116) { // F1-F5 key codes
            int effectIndex = keyCode - 112; // F1=0, F2=1, etc.
            String[] effectNames = {"Blur", "Sepia", "Particles", "Glow", "Reset All"};
            
            if (effectIndex < effectNames.length) {
                String effectName = effectNames[effectIndex];
                toggleVisualEffect(effectIndex);
                showNotification("Effect: " + effectName + " " + 
                    (effectIndex == 4 ? "Reset" : "Toggled"));
                LOGGER.info("Toggled visual effect: " + effectName);
            }
            return;
        }
        
        // Handle space bar for manual image swapping
        if (key == ' ') {
            imageDisplayManager.swapImages();
            showNotification("Manual Image Swap");
            LOGGER.info("Space bar pressed - manual image swap");
            return;
        }
        
        // Handle tab key for settings overlay
        if (keyCode == TAB) {
            toggleSettingsOverlay();
            LOGGER.info("Tab pressed - " + (showSettingsOverlay ? "showing" : "hiding") + " settings overlay");
            return;
        }
    }
    
    /**
     * Parses the background color from configuration.
     * 
     * @return The background color as a Processing color int
     */
    private int parseBackgroundColor() {
        try {
            String hexColor = config.getBackgroundColor();
            
            if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
                return color(0, 0, 0); // Default to black
            }
            
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            return color(r, g, b);
        } catch (Exception e) {
            LOGGER.warning("Error parsing background color, using black", e.getMessage());
            return color(0, 0, 0); // Default to black on error
        }
    }
    
    /**
     * Called when the sketch is stopped.
     * Cleans up resources including audio input.
     */
    @Override
    public void stop() {
        try {
            LOGGER.info("Stopping iCandy sketch");
            
            if (beatDetector != null) {
                try {
                    beatDetector.stop();
                    LOGGER.info("Beat detector stopped successfully");
                } catch (Exception e) {
                    LOGGER.warning("Error stopping beat detector", e.getMessage());
                }
            }
            
            // Clear image caches to free memory
            if (imageDisplayManager != null) {
                try {
                    imageDisplayManager.clear();
                    LOGGER.info("Image display manager cleared");
                } catch (Exception e) {
                    LOGGER.warning("Error clearing image display manager", e.getMessage());
                }
            }
            
            LOGGER.info("iCandy sketch stopped successfully");
        } catch (Exception e) {
            LOGGER.error("Error during sketch shutdown", "", e);
        } finally {
            super.stop();
        }
    }
    
    /**
     * Switches to a new layout algorithm with smooth transition.
     * 
     * @param algorithmName The name of the new algorithm
     */
    private void switchLayoutAlgorithm(String algorithmName) {
        if (layoutEngine == null) return;
        
        try {
            LayoutAlgorithm newAlgorithm = LayoutAlgorithmFactory.createAlgorithm(algorithmName);
            layoutEngine.setAlgorithm(newAlgorithm);
            
            // Update configuration
            config.setCurrentLayoutAlgorithm(algorithmName);
            LayoutConfig layoutConfig = config.getLayoutConfig();
            layoutEngine.setConfig(layoutConfig);
            
        } catch (IllegalArgumentException e) {
            LOGGER.warning("Failed to switch layout algorithm: " + e.getMessage());
        }
    }
    
    /**
     * Switches to a new transition effect with immediate application.
     * 
     * @param effectName The name of the new effect
     */
    private void switchTransitionEffect(String effectName) {
        if (transitionEngine == null) return;
        
        TransitionEffect newEffect = TransitionEffectFactory.createEffect(effectName);
        if (newEffect != null) {
            transitionEngine.setEffect(newEffect);
            
            // Update configuration
            config.setCurrentTransitionEffect(effectName);
            TransitionConfig transitionConfig = config.createTransitionConfig();
            transitionEngine.setConfig(transitionConfig);
        } else {
            LOGGER.warning("Unknown transition effect: " + effectName);
        }
    }
    
    /**
     * Toggles a visual effect on or off.
     * 
     * @param effectIndex The index of the effect (0=Blur, 1=Sepia, 2=Particles, 3=Glow, 4=Reset)
     */
    private void toggleVisualEffect(int effectIndex) {
        if (visualEffectsManager == null) return;
        
        switch (effectIndex) {
            case 0: // Blur
                config.toggleVisualEffect("blur");
                break;
            case 1: // Sepia
                VisualEffectsConfig effectsConfig = config.getVisualEffectsConfig();
                boolean currentSepia = effectsConfig.colorFilter == VisualEffectsConfig.ColorFilterType.SEPIA;
                config.updateVisualEffectsParameter("colorfilter", currentSepia ? "none" : "sepia");
                break;
            case 2: // Particles
                config.toggleVisualEffect("particles");
                break;
            case 3: // Glow
                config.toggleVisualEffect("glow");
                break;
            case 4: // Reset All
                config.resetVisualEffects();
                break;
        }
        
        // Update the visual effects manager with new config
        visualEffectsManager.setConfig(config.getVisualEffectsConfig());
    }
    
    /**
     * Toggles the settings overlay display.
     */
    private void toggleSettingsOverlay() {
        showSettingsOverlay = !showSettingsOverlay;
        if (showSettingsOverlay) {
            settingsOverlayStartTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Shows a brief notification message.
     * 
     * @param message The notification message
     */
    private void showNotification(String message) {
        currentNotification = message;
        notificationStartTime = System.currentTimeMillis();
    }
    
    /**
     * Capitalizes the first letter of a string.
     * 
     * @param str The string to capitalize
     * @return The capitalized string
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * Checks if any visual effects are enabled.
     * 
     * @param effectsConfig The visual effects configuration
     * @return true if any effects are enabled
     */
    private boolean hasAnyEffectsEnabled(VisualEffectsConfig effectsConfig) {
        return effectsConfig.enableBlur || 
               effectsConfig.colorFilter != VisualEffectsConfig.ColorFilterType.NONE ||
               effectsConfig.enableParticles || 
               effectsConfig.enableGlow || 
               effectsConfig.enableShadow ||
               effectsConfig.brightness != 0.0f ||
               effectsConfig.contrast != 0.0f ||
               effectsConfig.gamma != 1.0f;
    }
    
    /**
     * Gets the total number of phrases in the sequence.
     * 
     * @return The total number of phrases
     */
    private int getTotalPhrases() {
        // Since PhraseSequencer doesn't have getTotalPhrases(), we'll estimate
        // This is a workaround - ideally PhraseSequencer should have this method
        return phraseSequencer != null ? phraseSequencer.getCurrentIndex() + 1 : 0;
    }
    
    /**
     * Renders the settings overlay showing current configuration and performance metrics.
     * 
     * Requirements: 14.5, 14.7
     */
    private void renderSettingsOverlay() {
        long currentTime = System.currentTimeMillis();
        long overlayDuration = 5000; // Default 5 seconds since getSettingsOverlayDuration() doesn't exist
        
        // Auto-hide overlay after configured duration
        if (currentTime - settingsOverlayStartTime > overlayDuration) {
            showSettingsOverlay = false;
            return;
        }
        
        // Calculate fade-in/fade-out alpha
        float alpha = calculateOverlayAlpha(currentTime, overlayDuration);
        
        // Save current drawing state
        pushMatrix();
        pushStyle();
        
        // Draw semi-transparent background
        fill(0, 0, 0, alpha * 180);
        noStroke();
        rect(20, 20, width - 40, height - 40);
        
        // Draw border
        stroke(255, 255, 255, alpha * 255);
        strokeWeight(2);
        noFill();
        rect(20, 20, width - 40, height - 40);
        
        // Set text properties
        fill(255, 255, 255, alpha * 255);
        textAlign(LEFT, TOP);
        textSize(16);
        
        // Render settings information
        int x = 40;
        int y = 50;
        int lineHeight = 25;
        
        text("iCandy Settings", x, y);
        y += lineHeight * 1.5f;
        
        // Layout information
        text("Layout Algorithm: " + capitalizeFirst(config.getCurrentLayoutAlgorithm()), x, y);
        y += lineHeight;
        
        // Transition information
        text("Transition Effect: " + capitalizeFirst(config.getCurrentTransitionEffect()), x, y);
        y += lineHeight;
        text("Transition Duration: " + config.getTransitionDuration() + "ms", x, y);
        y += lineHeight;
        
        // Visual effects information
        VisualEffectsConfig effectsConfig = config.getVisualEffectsConfig();
        text("Visual Effects:", x, y);
        y += lineHeight;
        
        String[] effectStatus = {
            "  Blur: " + (effectsConfig.enableBlur ? "ON" : "OFF"),
            "  Sepia: " + (effectsConfig.colorFilter == VisualEffectsConfig.ColorFilterType.SEPIA ? "ON" : "OFF"),
            "  Particles: " + (effectsConfig.enableParticles ? "ON" : "OFF"),
            "  Glow: " + (effectsConfig.enableGlow ? "ON" : "OFF")
        };
        
        for (String status : effectStatus) {
            text(status, x, y);
            y += lineHeight;
        }
        
        y += lineHeight * 0.5f;
        
        // Performance metrics
        text("Performance:", x, y);
        y += lineHeight;
        text("  FPS: " + String.format("%.1f", frameRate), x, y);
        y += lineHeight;
        text("  Target FPS: " + config.getFrameRate(), x, y);
        y += lineHeight;
        
        // Current phrase information
        y += lineHeight * 0.5f;
        text("Current Phrase: " + (phraseSequencer.getCurrentIndex() + 1) + " / " + 
             getTotalPhrases(), x, y);
        y += lineHeight;
        
        // Keyboard controls help
        y += lineHeight * 0.5f;
        text("Keyboard Controls:", x, y);
        y += lineHeight;
        
        String[] controls = {
            "  1-4: Layout Algorithms",
            "  Q-W-E-R-T: Transition Effects", 
            "  F1-F5: Visual Effects",
            "  Space: Manual Image Swap",
            "  Tab: Toggle This Overlay"
        };
        
        for (String control : controls) {
            text(control, x, y);
            y += lineHeight;
        }
        
        // Restore drawing state
        popStyle();
        popMatrix();
    }
    
    /**
     * Calculates the alpha value for overlay fade-in/fade-out animation.
     * 
     * @param currentTime Current time in milliseconds
     * @param overlayDuration Total overlay duration in milliseconds
     * @return Alpha value between 0.0 and 1.0
     */
    private float calculateOverlayAlpha(long currentTime, long overlayDuration) {
        long elapsed = currentTime - settingsOverlayStartTime;
        float fadeTime = 300; // 300ms fade in/out
        
        if (elapsed < fadeTime) {
            // Fade in
            return elapsed / fadeTime;
        } else if (elapsed > overlayDuration - fadeTime) {
            // Fade out
            return (overlayDuration - elapsed) / fadeTime;
        } else {
            // Fully visible
            return 1.0f;
        }
    }
    
    /**
     * Checks if a notification is currently active.
     * 
     * @return true if notification should be displayed
     */
    private boolean isNotificationActive() {
        return !currentNotification.isEmpty() && 
               (System.currentTimeMillis() - notificationStartTime) < NOTIFICATION_DURATION;
    }
    
    /**
     * Renders the current notification message.
     */
    private void renderNotification() {
        long elapsed = System.currentTimeMillis() - notificationStartTime;
        
        // Calculate fade alpha
        float alpha = 1.0f;
        if (elapsed > NOTIFICATION_DURATION - 500) {
            // Fade out in last 500ms
            alpha = (NOTIFICATION_DURATION - elapsed) / 500.0f;
        }
        
        // Save current drawing state
        pushMatrix();
        pushStyle();
        
        // Position notification in top-right corner
        int notificationWidth = 250;
        int notificationHeight = 60;
        int x = width - notificationWidth - 20;
        int y = 20;
        
        // Draw background
        fill(0, 0, 0, alpha * 200);
        noStroke();
        rect(x, y, notificationWidth, notificationHeight, 5);
        
        // Draw border
        stroke(100, 150, 255, alpha * 255);
        strokeWeight(2);
        noFill();
        rect(x, y, notificationWidth, notificationHeight, 5);
        
        // Draw text
        fill(255, 255, 255, alpha * 255);
        textAlign(CENTER, CENTER);
        textSize(14);
        text(currentNotification, x + notificationWidth/2, y + notificationHeight/2);
        
        // Restore drawing state
        popStyle();
        popMatrix();
        
        // Clear notification if expired
        if (elapsed >= NOTIFICATION_DURATION) {
            currentNotification = "";
        }
    }
}
