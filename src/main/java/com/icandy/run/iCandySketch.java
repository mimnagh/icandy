package com.icandy.run;

import com.icandy.build.AssociationManager;
import com.icandy.build.TextParser;
import com.icandy.common.ConfigurationManager;
import processing.core.PApplet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

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
    
    private static final Logger LOGGER = Logger.getLogger(iCandySketch.class.getName());
    private static final float TEXT_Y_RATIO = 0.85f; // Position text in lower portion of screen
    
    // Configuration and data
    private ConfigurationManager config;
    private AssociationManager associationManager;
    private TextParser textParser;
    
    // Run phase components
    private TextDisplayManager textDisplayManager;
    private ImageDisplayManager imageDisplayManager;
    private PhraseSequencer phraseSequencer;
    // BeatDetectorWrapper will be added in task 10
    
    // State
    private boolean initialized = false;
    private String textFilePath;
    private String configFilePath;
    
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
    }
    
    @Override
    public void setup() {
        try {
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
            
            // Initialize sequencer
            initializeSequencer();
            
            // Set up audio input and beat detection (task 10)
            // setupAudioAndBeatDetection();
            
            // Mark as initialized
            initialized = true;
            
            LOGGER.info("iCandy initialized successfully");
            
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize iCandy: " + e.getMessage());
            e.printStackTrace();
            exit();
        }
    }
    
    /**
     * Loads configuration from file.
     * Uses default values if configuration file is not found.
     */
    private void loadConfiguration() throws IOException {
        config = new ConfigurationManager();
        
        if (configFilePath != null && Files.exists(Path.of(configFilePath))) {
            config.loadFromFile(configFilePath);
            LOGGER.info("Configuration loaded from: " + configFilePath);
        } else {
            LOGGER.warning("Configuration file not found, using defaults: " + configFilePath);
        }
    }
    
    /**
     * Loads word-image associations from file.
     */
    private void loadAssociations() throws IOException {
        associationManager = new AssociationManager();
        String associationsFile = config.getAssociationsFile();
        
        if (!Files.exists(Path.of(associationsFile))) {
            throw new IOException("Associations file not found: " + associationsFile + 
                ". Please run the build phase first.");
        }
        
        associationManager.loadFromFile(associationsFile);
        LOGGER.info("Loaded " + associationManager.getWordCount() + " word associations with " + 
            associationManager.getImageCount() + " total images");
        
        // Verify image files exist
        if (!associationManager.verifyImageFiles()) {
            LOGGER.warning("Some image files are missing. Display may be incomplete.");
            var missingFiles = associationManager.getMissingImageFiles();
            LOGGER.warning("Missing files: " + missingFiles.size());
        }
    }
    
    /**
     * Parses the text script into phrases and words.
     */
    private void parseTextScript() throws IOException {
        if (textFilePath == null || !Files.exists(Path.of(textFilePath))) {
            throw new IOException("Text file not found: " + textFilePath);
        }
        
        textParser = new TextParser();
        
        // Load stop words
        String stopWordsFile = config.getStopWordsFile();
        if (Files.exists(Path.of(stopWordsFile))) {
            textParser.loadStopWords(stopWordsFile);
            LOGGER.info("Stop words loaded from: " + stopWordsFile);
        } else {
            LOGGER.warning("Stop words file not found, using empty set: " + stopWordsFile);
        }
    }
    
    /**
     * Initializes the display managers.
     */
    private void initializeDisplayManagers() {
        textDisplayManager = new TextDisplayManager(this, config);
        imageDisplayManager = new ImageDisplayManager(this, associationManager);
        
        // Configure image display manager
        imageDisplayManager.setSimultaneousImageCount(config.getSimultaneousImageCount());
        
        LOGGER.info("Display managers initialized");
    }
    
    /**
     * Initializes the phrase sequencer.
     */
    private void initializeSequencer() throws IOException {
        // Read text content
        String textContent = Files.readString(Path.of(textFilePath));
        
        // Parse into phrases
        String[] phrases = textParser.parseIntoPhrases(textContent);
        
        if (phrases.length == 0) {
            throw new IOException("No phrases found in text file: " + textFilePath);
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
        
        LOGGER.info("Phrase sequencer initialized with " + phrases.length + " phrases");
    }
    
    /**
     * Processing draw() loop - renders the visual output.
     * 
     * This method:
     * - Updates beat detection state
     * - Checks for automatic phrase advancement
     * - Renders current phrase via TextDisplayManager
     * - Renders current images via ImageDisplayManager
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
        
        // Clear background
        background(parseBackgroundColor());
        
        // Update beat detection state (task 10)
        // boolean beatDetected = updateBeatDetection();
        
        // Check for automatic phrase advancement
        if (textDisplayManager.shouldAdvance()) {
            advanceToNextPhrase();
        }
        
        // Render current images
        imageDisplayManager.displayCurrentImages();
        
        // Render current phrase (on top of images)
        int centerX = width / 2;
        int textY = (int) (height * TEXT_Y_RATIO);
        textDisplayManager.displayPhrase(
            phraseSequencer.getCurrentPhrase(),
            centerX,
            textY
        );
        
        // Swap images on beat detection (task 10)
        // if (beatDetected) {
        //     imageDisplayManager.swapImages();
        // }
    }
    
    /**
     * Advances to the next phrase in the sequence.
     */
    private void advanceToNextPhrase() {
        phraseSequencer.advance();
        updateCurrentPhrase();
    }
    
    /**
     * Updates the display managers with the current phrase and words.
     */
    private void updateCurrentPhrase() {
        String currentPhrase = phraseSequencer.getCurrentPhrase();
        String[] currentWords = phraseSequencer.getWordsInCurrentPhrase();
        
        textDisplayManager.updatePhrase(currentPhrase);
        imageDisplayManager.setImagesForPhrase(currentWords);
    }
    
    /**
     * Handles keyboard input for navigation.
     * 
     * - Right arrow key: advance to next phrase
     * - Left arrow key: go back to previous phrase
     * 
     * Requirements: 4.8, 4.9
     */
    @Override
    public void keyPressed() {
        if (!initialized || !config.isKeyboardNavigationEnabled()) {
            return;
        }
        
        if (keyCode == RIGHT) {
            // Advance to next phrase
            phraseSequencer.advance();
            updateCurrentPhrase();
            
            LOGGER.info("Advanced to phrase " + (phraseSequencer.getCurrentIndex() + 1) + 
                " of " + phraseSequencer.getPhraseCount());
            
        } else if (keyCode == LEFT) {
            // Go back to previous phrase
            phraseSequencer.goBack();
            updateCurrentPhrase();
            
            LOGGER.info("Went back to phrase " + (phraseSequencer.getCurrentIndex() + 1) + 
                " of " + phraseSequencer.getPhraseCount());
        }
    }
    
    /**
     * Parses the background color from configuration.
     * 
     * @return The background color as a Processing color int
     */
    private int parseBackgroundColor() {
        String hexColor = config.getBackgroundColor();
        
        if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
            return color(0, 0, 0); // Default to black
        }
        
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            return color(r, g, b);
        } catch (NumberFormatException e) {
            return color(0, 0, 0); // Default to black on error
        }
    }
}
