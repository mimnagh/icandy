package com.icandy.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icandy.run.EasingFunction;
import com.icandy.run.LayoutConfig;
import com.icandy.run.TransitionConfig;
import com.icandy.run.VisualEffectsConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ConfigurationManager handles loading and validation of configuration settings
 * for both build and run phases of iCandy.
 * 
 * Configuration is loaded from a JSON file and provides sensible defaults
 * for any missing or invalid values.
 */
public class ConfigurationManager {
    
    // Build phase configuration
    private int imagesPerWord = 5;
    private String unsplashPropertiesFile = "~/.icandy/unsplash.properties";
    private String imageStorageDir = "data/images";
    private String associationsFile = "data/associations.json";
    private String stopWordsFile = "data/stopwords.txt";
    private int maxRetries = 3;
    
    // Run phase configuration
    private int beatSensitivity = 100;
    private int minPhraseDuration = 2000;
    private int maxPhraseDuration = 10000;
    private int msPerWord = 300;
    private int frameRate = 30;
    private int textSize = 48;
    private String textColor = "#FFFFFF";
    private String backgroundColor = "#000000";
    private boolean enableKeyboardNavigation = true;
    private int simultaneousImageCount = 3;
    private boolean loopPhrases = true;
    private String audioSource = "microphone";
    
    // Layout configuration
    private LayoutConfig layoutConfig = new LayoutConfig();
    private String currentLayoutAlgorithm = "grid";
    private float layoutTransitionDuration = 1000.0f;
    
    // Transition configuration
    private String currentTransitionEffect = "fade";
    private float transitionDuration = 500.0f;
    private String transitionEasing = "ease_in_out";
    private boolean enableStagger = false;
    private float staggerDelay = 50.0f;
    private String slideDirection = "left";
    private String zoomMode = "zoom_in";
    private float rotationAngle = 1.5708f; // PI/2 radians (90 degrees)
    private String blendMode = "normal";
    private float morphIntensity = 1.0f;
    
    // Visual effects configuration
    private VisualEffectsConfig visualEffectsConfig = new VisualEffectsConfig();
    
    /**
     * Creates a ConfigurationManager with default values.
     */
    public ConfigurationManager() {
        // All fields initialized with defaults above
    }
    
    /**
     * Loads configuration from a JSON file.
     * Validates all values and uses defaults for missing or invalid values.
     * 
     * @param configFilePath Path to the configuration file
     * @throws IOException if the file cannot be read or parsed
     */
    public void loadFromFile(String configFilePath) throws IOException {
        // Expand ~ to user home directory
        String expandedPath = configFilePath.replaceFirst("^~", System.getProperty("user.home"));
        Path path = Paths.get(expandedPath);
        
        if (!Files.exists(path)) {
            throw new IOException("Configuration file not found: " + expandedPath);
        }
        
        String json = Files.readString(path);
        JsonObject config;
        
        try {
            config = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            throw new IOException("Failed to parse configuration file: " + e.getMessage(), e);
        }
        
        // Load build configuration
        if (config.has("build")) {
            loadBuildConfiguration(config.getAsJsonObject("build"));
        }
        
        // Load run configuration
        if (config.has("run")) {
            loadRunConfiguration(config.getAsJsonObject("run"));
        }
        
        // Load layout configuration
        if (config.has("layout")) {
            loadLayoutConfiguration(config.getAsJsonObject("layout"));
        }
        
        // Load transition configuration
        if (config.has("transitions")) {
            loadTransitionConfiguration(config.getAsJsonObject("transitions"));
        }
        
        // Load visual effects configuration
        if (config.has("visualEffects")) {
            loadVisualEffectsConfiguration(config.getAsJsonObject("visualEffects"));
        }
    }
    
    /**
     * Loads build phase configuration from JSON object.
     */
    private void loadBuildConfiguration(JsonObject buildConfig) {
        // Images per word
        if (buildConfig.has("imagesPerWord")) {
            try {
                int value = buildConfig.get("imagesPerWord").getAsInt();
                if (value > 0) {
                    this.imagesPerWord = value;
                } else {
                    logWarning("imagesPerWord must be positive, using default: " + this.imagesPerWord);
                }
            } catch (Exception e) {
                logWarning("Invalid imagesPerWord value, using default: " + this.imagesPerWord);
            }
        }
        
        // Unsplash properties file
        if (buildConfig.has("unsplashPropertiesFile")) {
            try {
                this.unsplashPropertiesFile = buildConfig.get("unsplashPropertiesFile").getAsString();
            } catch (Exception e) {
                logWarning("Invalid unsplashPropertiesFile value, using default: " + this.unsplashPropertiesFile);
            }
        }
        
        // Image storage directory
        if (buildConfig.has("imageStorageDir")) {
            try {
                this.imageStorageDir = buildConfig.get("imageStorageDir").getAsString();
            } catch (Exception e) {
                logWarning("Invalid imageStorageDir value, using default: " + this.imageStorageDir);
            }
        }
        
        // Associations file
        if (buildConfig.has("associationsFile")) {
            try {
                this.associationsFile = buildConfig.get("associationsFile").getAsString();
            } catch (Exception e) {
                logWarning("Invalid associationsFile value, using default: " + this.associationsFile);
            }
        }
        
        // Stop words file
        if (buildConfig.has("stopWordsFile")) {
            try {
                this.stopWordsFile = buildConfig.get("stopWordsFile").getAsString();
            } catch (Exception e) {
                logWarning("Invalid stopWordsFile value, using default: " + this.stopWordsFile);
            }
        }
        
        // Max retries
        if (buildConfig.has("maxRetries")) {
            try {
                int value = buildConfig.get("maxRetries").getAsInt();
                if (value >= 0) {
                    this.maxRetries = value;
                } else {
                    logWarning("maxRetries cannot be negative, using default: " + this.maxRetries);
                }
            } catch (Exception e) {
                logWarning("Invalid maxRetries value, using default: " + this.maxRetries);
            }
        }
    }
    
    /**
     * Loads run phase configuration from JSON object.
     */
    private void loadRunConfiguration(JsonObject runConfig) {
        // Beat sensitivity
        if (runConfig.has("beatSensitivity")) {
            try {
                int value = runConfig.get("beatSensitivity").getAsInt();
                if (value > 0) {
                    this.beatSensitivity = value;
                } else {
                    logWarning("beatSensitivity must be positive, using default: " + this.beatSensitivity);
                }
            } catch (Exception e) {
                logWarning("Invalid beatSensitivity value, using default: " + this.beatSensitivity);
            }
        }
        
        // Min phrase duration
        if (runConfig.has("minPhraseDuration")) {
            try {
                int value = runConfig.get("minPhraseDuration").getAsInt();
                if (value > 0) {
                    this.minPhraseDuration = value;
                } else {
                    logWarning("minPhraseDuration must be positive, using default: " + this.minPhraseDuration);
                }
            } catch (Exception e) {
                logWarning("Invalid minPhraseDuration value, using default: " + this.minPhraseDuration);
            }
        }
        
        // Max phrase duration
        if (runConfig.has("maxPhraseDuration")) {
            try {
                int value = runConfig.get("maxPhraseDuration").getAsInt();
                if (value > 0 && value >= this.minPhraseDuration) {
                    this.maxPhraseDuration = value;
                } else {
                    logWarning("maxPhraseDuration must be positive and >= minPhraseDuration, using default: " + this.maxPhraseDuration);
                }
            } catch (Exception e) {
                logWarning("Invalid maxPhraseDuration value, using default: " + this.maxPhraseDuration);
            }
        }
        
        // Milliseconds per word
        if (runConfig.has("msPerWord")) {
            try {
                int value = runConfig.get("msPerWord").getAsInt();
                if (value > 0) {
                    this.msPerWord = value;
                } else {
                    logWarning("msPerWord must be positive, using default: " + this.msPerWord);
                }
            } catch (Exception e) {
                logWarning("Invalid msPerWord value, using default: " + this.msPerWord);
            }
        }
        
        // Frame rate
        if (runConfig.has("frameRate")) {
            try {
                int value = runConfig.get("frameRate").getAsInt();
                if (value > 0) {
                    this.frameRate = value;
                } else {
                    logWarning("frameRate must be positive, using default: " + this.frameRate);
                }
            } catch (Exception e) {
                logWarning("Invalid frameRate value, using default: " + this.frameRate);
            }
        }
        
        // Text size
        if (runConfig.has("textSize")) {
            try {
                int value = runConfig.get("textSize").getAsInt();
                if (value > 0) {
                    this.textSize = value;
                } else {
                    logWarning("textSize must be positive, using default: " + this.textSize);
                }
            } catch (Exception e) {
                logWarning("Invalid textSize value, using default: " + this.textSize);
            }
        }
        
        // Text color
        if (runConfig.has("textColor")) {
            try {
                String value = runConfig.get("textColor").getAsString();
                if (isValidHexColor(value)) {
                    this.textColor = value;
                } else {
                    logWarning("textColor must be a valid hex color, using default: " + this.textColor);
                }
            } catch (Exception e) {
                logWarning("Invalid textColor value, using default: " + this.textColor);
            }
        }
        
        // Background color
        if (runConfig.has("backgroundColor")) {
            try {
                String value = runConfig.get("backgroundColor").getAsString();
                if (isValidHexColor(value)) {
                    this.backgroundColor = value;
                } else {
                    logWarning("backgroundColor must be a valid hex color, using default: " + this.backgroundColor);
                }
            } catch (Exception e) {
                logWarning("Invalid backgroundColor value, using default: " + this.backgroundColor);
            }
        }
        
        // Enable keyboard navigation
        if (runConfig.has("enableKeyboardNavigation")) {
            try {
                this.enableKeyboardNavigation = runConfig.get("enableKeyboardNavigation").getAsBoolean();
            } catch (Exception e) {
                logWarning("Invalid enableKeyboardNavigation value, using default: " + this.enableKeyboardNavigation);
            }
        }
        
        // Simultaneous image count
        if (runConfig.has("simultaneousImageCount")) {
            try {
                int value = runConfig.get("simultaneousImageCount").getAsInt();
                if (value > 0) {
                    this.simultaneousImageCount = value;
                } else {
                    logWarning("simultaneousImageCount must be positive, using default: " + this.simultaneousImageCount);
                }
            } catch (Exception e) {
                logWarning("Invalid simultaneousImageCount value, using default: " + this.simultaneousImageCount);
            }
        }
        
        // Loop phrases
        if (runConfig.has("loopPhrases")) {
            try {
                this.loopPhrases = runConfig.get("loopPhrases").getAsBoolean();
            } catch (Exception e) {
                logWarning("Invalid loopPhrases value, using default: " + this.loopPhrases);
            }
        }
        
        // Audio source
        if (runConfig.has("audioSource")) {
            try {
                this.audioSource = runConfig.get("audioSource").getAsString();
            } catch (Exception e) {
                logWarning("Invalid audioSource value, using default: " + this.audioSource);
            }
        }
    }
    
    /**
     * Loads transition configuration from JSON object.
     */
    private void loadTransitionConfiguration(JsonObject transitionConfig) {
        // Current transition effect
        if (transitionConfig.has("effect")) {
            try {
                String effect = transitionConfig.get("effect").getAsString().toLowerCase();
                if (isValidTransitionEffect(effect)) {
                    this.currentTransitionEffect = effect;
                } else {
                    logWarning("Invalid transition effect '" + effect + "', using default: " + this.currentTransitionEffect);
                }
            } catch (Exception e) {
                logWarning("Invalid transition effect value, using default: " + this.currentTransitionEffect);
            }
        }
        
        // Transition duration
        if (transitionConfig.has("duration")) {
            try {
                float value = transitionConfig.get("duration").getAsFloat();
                if (value > 0) {
                    this.transitionDuration = value;
                } else {
                    logWarning("Transition duration must be positive, using default: " + this.transitionDuration);
                }
            } catch (Exception e) {
                logWarning("Invalid transition duration, using default: " + this.transitionDuration);
            }
        }
        
        // Transition easing
        if (transitionConfig.has("easing")) {
            try {
                String easing = transitionConfig.get("easing").getAsString().toLowerCase();
                if (isValidEasingFunction(easing)) {
                    this.transitionEasing = easing;
                } else {
                    logWarning("Invalid easing function '" + easing + "', using default: " + this.transitionEasing);
                }
            } catch (Exception e) {
                logWarning("Invalid easing function value, using default: " + this.transitionEasing);
            }
        }
        
        // Enable stagger
        if (transitionConfig.has("enableStagger")) {
            try {
                this.enableStagger = transitionConfig.get("enableStagger").getAsBoolean();
            } catch (Exception e) {
                logWarning("Invalid enableStagger value, using default: " + this.enableStagger);
            }
        }
        
        // Stagger delay
        if (transitionConfig.has("staggerDelay")) {
            try {
                float value = transitionConfig.get("staggerDelay").getAsFloat();
                if (value >= 0) {
                    this.staggerDelay = value;
                } else {
                    logWarning("Stagger delay cannot be negative, using default: " + this.staggerDelay);
                }
            } catch (Exception e) {
                logWarning("Invalid stagger delay value, using default: " + this.staggerDelay);
            }
        }
        
        // Slide direction
        if (transitionConfig.has("slideDirection")) {
            try {
                String direction = transitionConfig.get("slideDirection").getAsString().toLowerCase();
                if (isValidSlideDirection(direction)) {
                    this.slideDirection = direction;
                } else {
                    logWarning("Invalid slide direction '" + direction + "', using default: " + this.slideDirection);
                }
            } catch (Exception e) {
                logWarning("Invalid slide direction value, using default: " + this.slideDirection);
            }
        }
        
        // Zoom mode
        if (transitionConfig.has("zoomMode")) {
            try {
                String mode = transitionConfig.get("zoomMode").getAsString().toLowerCase();
                if (isValidZoomMode(mode)) {
                    this.zoomMode = mode;
                } else {
                    logWarning("Invalid zoom mode '" + mode + "', using default: " + this.zoomMode);
                }
            } catch (Exception e) {
                logWarning("Invalid zoom mode value, using default: " + this.zoomMode);
            }
        }
        
        // Rotation angle
        if (transitionConfig.has("rotationAngle")) {
            try {
                this.rotationAngle = transitionConfig.get("rotationAngle").getAsFloat();
            } catch (Exception e) {
                logWarning("Invalid rotation angle value, using default: " + this.rotationAngle);
            }
        }
        
        // Blend mode
        if (transitionConfig.has("blendMode")) {
            try {
                String mode = transitionConfig.get("blendMode").getAsString().toLowerCase();
                if (isValidBlendMode(mode)) {
                    this.blendMode = mode;
                } else {
                    logWarning("Invalid blend mode '" + mode + "', using default: " + this.blendMode);
                }
            } catch (Exception e) {
                logWarning("Invalid blend mode value, using default: " + this.blendMode);
            }
        }
        
        // Morph intensity
        if (transitionConfig.has("morphIntensity")) {
            try {
                float value = transitionConfig.get("morphIntensity").getAsFloat();
                if (value >= 0.0f && value <= 1.0f) {
                    this.morphIntensity = value;
                } else {
                    logWarning("Morph intensity must be between 0.0 and 1.0, using default: " + this.morphIntensity);
                }
            } catch (Exception e) {
                logWarning("Invalid morph intensity value, using default: " + this.morphIntensity);
            }
        }
    }
    
    /**
     * Loads layout configuration from JSON object.
     */
    private void loadLayoutConfiguration(JsonObject layoutConfigJson) {
        // Current layout algorithm
        if (layoutConfigJson.has("algorithm")) {
            try {
                String algorithm = layoutConfigJson.get("algorithm").getAsString().toLowerCase();
                if (isValidLayoutAlgorithm(algorithm)) {
                    this.currentLayoutAlgorithm = algorithm;
                } else {
                    logWarning("Invalid layout algorithm '" + algorithm + "', using default: " + this.currentLayoutAlgorithm);
                }
            } catch (Exception e) {
                logWarning("Invalid layout algorithm value, using default: " + this.currentLayoutAlgorithm);
            }
        }
        
        // Layout transition duration
        if (layoutConfigJson.has("transitionDuration")) {
            try {
                float value = layoutConfigJson.get("transitionDuration").getAsFloat();
                if (value > 0) {
                    this.layoutTransitionDuration = value;
                } else {
                    logWarning("Layout transition duration must be positive, using default: " + this.layoutTransitionDuration);
                }
            } catch (Exception e) {
                logWarning("Invalid layout transition duration, using default: " + this.layoutTransitionDuration);
            }
        }
        
        // Display region
        if (layoutConfigJson.has("displayRegion")) {
            try {
                JsonObject region = layoutConfigJson.getAsJsonObject("displayRegion");
                float x = region.has("x") ? region.get("x").getAsFloat() : layoutConfig.displayRegion.x;
                float y = region.has("y") ? region.get("y").getAsFloat() : layoutConfig.displayRegion.y;
                float width = region.has("width") ? region.get("width").getAsFloat() : layoutConfig.displayRegion.width;
                float height = region.has("height") ? region.get("height").getAsFloat() : layoutConfig.displayRegion.height;
                
                if (width > 0 && height > 0) {
                    layoutConfig.displayRegion = new LayoutConfig.Rectangle(x, y, width, height);
                } else {
                    logWarning("Display region width and height must be positive, using defaults");
                }
            } catch (Exception e) {
                logWarning("Invalid display region configuration, using defaults");
            }
        }
        
        // Global scale
        if (layoutConfigJson.has("globalScale")) {
            try {
                float value = layoutConfigJson.get("globalScale").getAsFloat();
                if (value > 0) {
                    layoutConfig.globalScale = value;
                } else {
                    logWarning("Global scale must be positive, using default: " + layoutConfig.globalScale);
                }
            } catch (Exception e) {
                logWarning("Invalid global scale value, using default: " + layoutConfig.globalScale);
            }
        }
        
        // Aspect ratio mode
        if (layoutConfigJson.has("aspectRatioMode")) {
            try {
                String mode = layoutConfigJson.get("aspectRatioMode").getAsString().toUpperCase();
                layoutConfig.aspectRatioMode = LayoutConfig.AspectRatioMode.valueOf(mode);
            } catch (Exception e) {
                logWarning("Invalid aspect ratio mode, using default: " + layoutConfig.aspectRatioMode);
            }
        }
        
        // Grid configuration
        if (layoutConfigJson.has("grid")) {
            loadGridConfiguration(layoutConfigJson.getAsJsonObject("grid"));
        }
        
        // Collage configuration
        if (layoutConfigJson.has("collage")) {
            loadCollageConfiguration(layoutConfigJson.getAsJsonObject("collage"));
        }
        
        // Circular configuration
        if (layoutConfigJson.has("circular")) {
            loadCircularConfiguration(layoutConfigJson.getAsJsonObject("circular"));
        }
        
        // Flowing configuration
        if (layoutConfigJson.has("flowing")) {
            loadFlowingConfiguration(layoutConfigJson.getAsJsonObject("flowing"));
        }
        
        // Validate the final configuration
        layoutConfig.validate();
    }
    
    /**
     * Load grid-specific configuration.
     */
    private void loadGridConfiguration(JsonObject gridConfig) {
        if (gridConfig.has("spacing")) {
            try {
                float value = gridConfig.get("spacing").getAsFloat();
                if (value >= 0) {
                    layoutConfig.gridSpacing = value;
                }
            } catch (Exception e) {
                logWarning("Invalid grid spacing value, using default");
            }
        }
        
        if (gridConfig.has("padding")) {
            try {
                float value = gridConfig.get("padding").getAsFloat();
                if (value >= 0) {
                    layoutConfig.gridPadding = value;
                }
            } catch (Exception e) {
                logWarning("Invalid grid padding value, using default");
            }
        }
        
        if (gridConfig.has("alignment")) {
            try {
                String alignment = gridConfig.get("alignment").getAsString().toUpperCase();
                layoutConfig.gridAlignment = LayoutConfig.Alignment.valueOf(alignment);
            } catch (Exception e) {
                logWarning("Invalid grid alignment value, using default");
            }
        }
        
        if (gridConfig.has("rows")) {
            try {
                int value = gridConfig.get("rows").getAsInt();
                if (value >= 0) {
                    layoutConfig.gridRows = value;
                }
            } catch (Exception e) {
                logWarning("Invalid grid rows value, using default");
            }
        }
        
        if (gridConfig.has("cols")) {
            try {
                int value = gridConfig.get("cols").getAsInt();
                if (value >= 0) {
                    layoutConfig.gridCols = value;
                }
            } catch (Exception e) {
                logWarning("Invalid grid cols value, using default");
            }
        }
    }
    
    /**
     * Load collage-specific configuration.
     */
    private void loadCollageConfiguration(JsonObject collageConfig) {
        if (collageConfig.has("minSize")) {
            try {
                float value = collageConfig.get("minSize").getAsFloat();
                if (value > 0) {
                    layoutConfig.minSize = value;
                }
            } catch (Exception e) {
                logWarning("Invalid collage minSize value, using default");
            }
        }
        
        if (collageConfig.has("maxSize")) {
            try {
                float value = collageConfig.get("maxSize").getAsFloat();
                if (value > 0) {
                    layoutConfig.maxSize = value;
                }
            } catch (Exception e) {
                logWarning("Invalid collage maxSize value, using default");
            }
        }
        
        if (collageConfig.has("minRotation")) {
            try {
                layoutConfig.minRotation = collageConfig.get("minRotation").getAsFloat();
            } catch (Exception e) {
                logWarning("Invalid collage minRotation value, using default");
            }
        }
        
        if (collageConfig.has("maxRotation")) {
            try {
                layoutConfig.maxRotation = collageConfig.get("maxRotation").getAsFloat();
            } catch (Exception e) {
                logWarning("Invalid collage maxRotation value, using default");
            }
        }
        
        if (collageConfig.has("overlapAmount")) {
            try {
                float value = collageConfig.get("overlapAmount").getAsFloat();
                if (value >= 0 && value <= 1) {
                    layoutConfig.overlapAmount = value;
                }
            } catch (Exception e) {
                logWarning("Invalid collage overlapAmount value, using default");
            }
        }
    }
    
    /**
     * Load circular-specific configuration.
     */
    private void loadCircularConfiguration(JsonObject circularConfig) {
        if (circularConfig.has("radius")) {
            try {
                float value = circularConfig.get("radius").getAsFloat();
                if (value > 0) {
                    layoutConfig.circleRadius = value;
                }
            } catch (Exception e) {
                logWarning("Invalid circular radius value, using default");
            }
        }
        
        if (circularConfig.has("arcSpan")) {
            try {
                float value = circularConfig.get("arcSpan").getAsFloat();
                if (value > 0 && value <= 360) {
                    layoutConfig.arcSpan = value;
                }
            } catch (Exception e) {
                logWarning("Invalid circular arcSpan value, using default");
            }
        }
        
        if (circularConfig.has("rotationDirection")) {
            try {
                String direction = circularConfig.get("rotationDirection").getAsString().toUpperCase();
                layoutConfig.rotationDirection = LayoutConfig.RotationDirection.valueOf(direction);
            } catch (Exception e) {
                logWarning("Invalid circular rotationDirection value, using default");
            }
        }
    }
    
    /**
     * Load flowing-specific configuration.
     */
    private void loadFlowingConfiguration(JsonObject flowingConfig) {
        if (flowingConfig.has("pathCurvature")) {
            try {
                float value = flowingConfig.get("pathCurvature").getAsFloat();
                if (value >= 0) {
                    layoutConfig.pathCurvature = value;
                }
            } catch (Exception e) {
                logWarning("Invalid flowing pathCurvature value, using default");
            }
        }
        
        if (flowingConfig.has("flowDirection")) {
            try {
                String direction = flowingConfig.get("flowDirection").getAsString().toUpperCase();
                layoutConfig.flowDirection = LayoutConfig.FlowDirection.valueOf(direction);
            } catch (Exception e) {
                logWarning("Invalid flowing flowDirection value, using default");
            }
        }
        
        if (flowingConfig.has("pathSpacing")) {
            try {
                float value = flowingConfig.get("pathSpacing").getAsFloat();
                if (value > 0) {
                    layoutConfig.pathSpacing = value;
                }
            } catch (Exception e) {
                logWarning("Invalid flowing pathSpacing value, using default");
            }
        }
    }
    
    /**
     * Loads visual effects configuration from JSON object.
     */
    private void loadVisualEffectsConfiguration(JsonObject visualEffectsConfig) {
        // Blur settings
        if (visualEffectsConfig.has("enableBlur")) {
            try {
                this.visualEffectsConfig.enableBlur = visualEffectsConfig.get("enableBlur").getAsBoolean();
            } catch (Exception e) {
                logWarning("Invalid enableBlur value, using default: " + this.visualEffectsConfig.enableBlur);
            }
        }
        
        if (visualEffectsConfig.has("blurRadius")) {
            try {
                float value = visualEffectsConfig.get("blurRadius").getAsFloat();
                if (value >= 0) {
                    this.visualEffectsConfig.blurRadius = value;
                } else {
                    logWarning("Blur radius cannot be negative, using default: " + this.visualEffectsConfig.blurRadius);
                }
            } catch (Exception e) {
                logWarning("Invalid blurRadius value, using default: " + this.visualEffectsConfig.blurRadius);
            }
        }
        
        // Color filter settings
        if (visualEffectsConfig.has("colorFilter")) {
            try {
                String filter = visualEffectsConfig.get("colorFilter").getAsString().toUpperCase();
                this.visualEffectsConfig.colorFilter = VisualEffectsConfig.ColorFilterType.valueOf(filter);
            } catch (Exception e) {
                logWarning("Invalid colorFilter value, using default: " + this.visualEffectsConfig.colorFilter);
            }
        }
        
        if (visualEffectsConfig.has("colorIntensity")) {
            try {
                float value = visualEffectsConfig.get("colorIntensity").getAsFloat();
                if (value >= 0 && value <= 2.0f) {
                    this.visualEffectsConfig.colorIntensity = value;
                } else {
                    logWarning("Color intensity must be between 0.0 and 2.0, using default: " + this.visualEffectsConfig.colorIntensity);
                }
            } catch (Exception e) {
                logWarning("Invalid colorIntensity value, using default: " + this.visualEffectsConfig.colorIntensity);
            }
        }
        
        if (visualEffectsConfig.has("tintColor")) {
            try {
                String colorStr = visualEffectsConfig.get("tintColor").getAsString();
                if (isValidHexColor(colorStr)) {
                    this.visualEffectsConfig.tintColor = parseHexColor(colorStr);
                } else {
                    logWarning("Invalid tintColor format, using default");
                }
            } catch (Exception e) {
                logWarning("Invalid tintColor value, using default");
            }
        }
        
        // Brightness/contrast settings
        if (visualEffectsConfig.has("brightness")) {
            try {
                float value = visualEffectsConfig.get("brightness").getAsFloat();
                if (value >= -1.0f && value <= 1.0f) {
                    this.visualEffectsConfig.brightness = value;
                } else {
                    logWarning("Brightness must be between -1.0 and 1.0, using default: " + this.visualEffectsConfig.brightness);
                }
            } catch (Exception e) {
                logWarning("Invalid brightness value, using default: " + this.visualEffectsConfig.brightness);
            }
        }
        
        if (visualEffectsConfig.has("contrast")) {
            try {
                float value = visualEffectsConfig.get("contrast").getAsFloat();
                if (value >= -1.0f && value <= 1.0f) {
                    this.visualEffectsConfig.contrast = value;
                } else {
                    logWarning("Contrast must be between -1.0 and 1.0, using default: " + this.visualEffectsConfig.contrast);
                }
            } catch (Exception e) {
                logWarning("Invalid contrast value, using default: " + this.visualEffectsConfig.contrast);
            }
        }
        
        if (visualEffectsConfig.has("gamma")) {
            try {
                float value = visualEffectsConfig.get("gamma").getAsFloat();
                if (value >= 0.1f && value <= 3.0f) {
                    this.visualEffectsConfig.gamma = value;
                } else {
                    logWarning("Gamma must be between 0.1 and 3.0, using default: " + this.visualEffectsConfig.gamma);
                }
            } catch (Exception e) {
                logWarning("Invalid gamma value, using default: " + this.visualEffectsConfig.gamma);
            }
        }
        
        // Particle system settings
        if (visualEffectsConfig.has("enableParticles")) {
            try {
                this.visualEffectsConfig.enableParticles = visualEffectsConfig.get("enableParticles").getAsBoolean();
            } catch (Exception e) {
                logWarning("Invalid enableParticles value, using default: " + this.visualEffectsConfig.enableParticles);
            }
        }
        
        if (visualEffectsConfig.has("particleCount")) {
            try {
                int value = visualEffectsConfig.get("particleCount").getAsInt();
                if (value >= 0 && value <= 1000) {
                    this.visualEffectsConfig.particleCount = value;
                } else {
                    logWarning("Particle count must be between 0 and 1000, using default: " + this.visualEffectsConfig.particleCount);
                }
            } catch (Exception e) {
                logWarning("Invalid particleCount value, using default: " + this.visualEffectsConfig.particleCount);
            }
        }
        
        if (visualEffectsConfig.has("particleType")) {
            try {
                String type = visualEffectsConfig.get("particleType").getAsString().toUpperCase();
                this.visualEffectsConfig.particleType = VisualEffectsConfig.ParticleType.valueOf(type);
            } catch (Exception e) {
                logWarning("Invalid particleType value, using default: " + this.visualEffectsConfig.particleType);
            }
        }
        
        if (visualEffectsConfig.has("particleColor")) {
            try {
                String colorStr = visualEffectsConfig.get("particleColor").getAsString();
                if (isValidHexColor(colorStr)) {
                    this.visualEffectsConfig.particleColor = parseHexColor(colorStr);
                } else {
                    logWarning("Invalid particleColor format, using default");
                }
            } catch (Exception e) {
                logWarning("Invalid particleColor value, using default");
            }
        }
        
        if (visualEffectsConfig.has("particleLifetime")) {
            try {
                float value = visualEffectsConfig.get("particleLifetime").getAsFloat();
                if (value >= 100.0f && value <= 10000.0f) {
                    this.visualEffectsConfig.particleLifetime = value;
                } else {
                    logWarning("Particle lifetime must be between 100 and 10000 ms, using default: " + this.visualEffectsConfig.particleLifetime);
                }
            } catch (Exception e) {
                logWarning("Invalid particleLifetime value, using default: " + this.visualEffectsConfig.particleLifetime);
            }
        }
        
        // Border effects settings
        if (visualEffectsConfig.has("enableGlow")) {
            try {
                this.visualEffectsConfig.enableGlow = visualEffectsConfig.get("enableGlow").getAsBoolean();
            } catch (Exception e) {
                logWarning("Invalid enableGlow value, using default: " + this.visualEffectsConfig.enableGlow);
            }
        }
        
        if (visualEffectsConfig.has("glowRadius")) {
            try {
                float value = visualEffectsConfig.get("glowRadius").getAsFloat();
                if (value >= 0) {
                    this.visualEffectsConfig.glowRadius = value;
                } else {
                    logWarning("Glow radius cannot be negative, using default: " + this.visualEffectsConfig.glowRadius);
                }
            } catch (Exception e) {
                logWarning("Invalid glowRadius value, using default: " + this.visualEffectsConfig.glowRadius);
            }
        }
        
        if (visualEffectsConfig.has("glowColor")) {
            try {
                String colorStr = visualEffectsConfig.get("glowColor").getAsString();
                if (isValidHexColor(colorStr)) {
                    this.visualEffectsConfig.glowColor = parseHexColor(colorStr);
                } else {
                    logWarning("Invalid glowColor format, using default");
                }
            } catch (Exception e) {
                logWarning("Invalid glowColor value, using default");
            }
        }
        
        if (visualEffectsConfig.has("enableShadow")) {
            try {
                this.visualEffectsConfig.enableShadow = visualEffectsConfig.get("enableShadow").getAsBoolean();
            } catch (Exception e) {
                logWarning("Invalid enableShadow value, using default: " + this.visualEffectsConfig.enableShadow);
            }
        }
        
        if (visualEffectsConfig.has("shadowOffset")) {
            try {
                float value = visualEffectsConfig.get("shadowOffset").getAsFloat();
                this.visualEffectsConfig.shadowOffsetX = value;
                this.visualEffectsConfig.shadowOffsetY = value;
            } catch (Exception e) {
                logWarning("Invalid shadowOffset value, using defaults");
            }
        }
        
        // Validate the final configuration
        this.visualEffectsConfig.validate();
    }
    
    /**
     * Parses a hex color string to an integer color value.
     * 
     * @param hexColor Hex color string (e.g., "#FF0000")
     * @return Integer color value
     */
    private int parseHexColor(String hexColor) {
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }
        return (int) Long.parseLong("FF" + hexColor, 16); // Add full alpha
    }
    
    /**
     * Check if a layout algorithm name is valid.
     */
    private boolean isValidLayoutAlgorithm(String algorithm) {
        return algorithm.equals("grid") || algorithm.equals("collage") || 
               algorithm.equals("circular") || algorithm.equals("flowing");
    }
    
    /**
     * Check if a transition effect name is valid.
     */
    private boolean isValidTransitionEffect(String effect) {
        return effect.equals("fade") || effect.equals("slide") || 
               effect.equals("zoom") || effect.equals("rotate") || effect.equals("morph");
    }
    
    /**
     * Check if an easing function name is valid.
     */
    private boolean isValidEasingFunction(String easing) {
        return easing.equals("linear") || easing.equals("ease_in") || 
               easing.equals("ease_out") || easing.equals("ease_in_out") ||
               easing.equals("bounce") || easing.equals("elastic");
    }
    
    /**
     * Check if a slide direction is valid.
     */
    private boolean isValidSlideDirection(String direction) {
        return direction.equals("up") || direction.equals("down") || 
               direction.equals("left") || direction.equals("right") ||
               direction.equals("up_left") || direction.equals("up_right") ||
               direction.equals("down_left") || direction.equals("down_right");
    }
    
    /**
     * Check if a zoom mode is valid.
     */
    private boolean isValidZoomMode(String mode) {
        return mode.equals("zoom_in") || mode.equals("zoom_out") || mode.equals("zoom_both");
    }
    
    /**
     * Check if a blend mode is valid.
     */
    private boolean isValidBlendMode(String mode) {
        return mode.equals("normal") || mode.equals("multiply") || 
               mode.equals("screen") || mode.equals("overlay") ||
               mode.equals("soft_light") || mode.equals("hard_light") ||
               mode.equals("color_dodge") || mode.equals("color_burn");
    }
    
    /**
     * Validates if a string is a valid hex color code.
     */
    private boolean isValidHexColor(String color) {
        if (color == null) {
            return false;
        }
        return color.matches("^#[0-9A-Fa-f]{6}$");
    }
    
    /**
     * Logs a warning message to stderr.
     */
    private void logWarning(String message) {
        System.err.println("Warning: " + message);
    }
    
    // Build phase getters
    
    public int getImagesPerWord() {
        return imagesPerWord;
    }
    
    public String getUnsplashPropertiesFile() {
        return unsplashPropertiesFile;
    }
    
    public String getImageStorageDir() {
        return imageStorageDir;
    }
    
    public String getAssociationsFile() {
        return associationsFile;
    }
    
    public String getStopWordsFile() {
        return stopWordsFile;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    // Run phase getters
    
    public int getBeatSensitivity() {
        return beatSensitivity;
    }
    
    public int getMinPhraseDuration() {
        return minPhraseDuration;
    }
    
    public int getMaxPhraseDuration() {
        return maxPhraseDuration;
    }
    
    public int getMsPerWord() {
        return msPerWord;
    }
    
    public int getFrameRate() {
        return frameRate;
    }
    
    public int getTextSize() {
        return textSize;
    }
    
    public String getTextColor() {
        return textColor;
    }
    
    public String getBackgroundColor() {
        return backgroundColor;
    }
    
    public boolean isKeyboardNavigationEnabled() {
        return enableKeyboardNavigation;
    }
    
    public int getSimultaneousImageCount() {
        return simultaneousImageCount;
    }
    
    public boolean isLoopPhrasesEnabled() {
        return loopPhrases;
    }
    
    public String getAudioSource() {
        return audioSource;
    }
    
    // Layout configuration getters
    
    public LayoutConfig getLayoutConfig() {
        return layoutConfig.copy();
    }
    
    public String getCurrentLayoutAlgorithm() {
        return currentLayoutAlgorithm;
    }
    
    public float getLayoutTransitionDuration() {
        return layoutTransitionDuration;
    }
    
    /**
     * Update the current layout algorithm.
     * 
     * @param algorithm The new layout algorithm ("grid", "collage", "circular", "flowing")
     */
    public void setCurrentLayoutAlgorithm(String algorithm) {
        if (isValidLayoutAlgorithm(algorithm.toLowerCase())) {
            this.currentLayoutAlgorithm = algorithm.toLowerCase();
        } else {
            logWarning("Invalid layout algorithm: " + algorithm);
        }
    }
    
    /**
     * Update layout configuration parameters dynamically.
     * 
     * @param newConfig The new layout configuration
     */
    public void updateLayoutConfig(LayoutConfig newConfig) {
        if (newConfig != null) {
            this.layoutConfig = newConfig.copy();
            this.layoutConfig.validate();
        }
    }
    
    /**
     * Update specific layout parameters for the current algorithm.
     * 
     * @param parameterName The name of the parameter to update
     * @param value The new value for the parameter
     */
    public void updateLayoutParameter(String parameterName, float value) {
        if (parameterName == null) {
            logWarning("Layout parameter name cannot be null");
            return;
        }
        
        switch (parameterName.toLowerCase()) {
            // Global parameters
            case "globalscale":
                layoutConfig.globalScale = Math.max(0.1f, Math.min(5.0f, value));
                break;
                
            // Grid parameters
            case "gridspacing":
                layoutConfig.gridSpacing = Math.max(0.0f, Math.min(100.0f, value));
                break;
            case "gridpadding":
                layoutConfig.gridPadding = Math.max(0.0f, Math.min(200.0f, value));
                break;
            case "gridrows":
                layoutConfig.gridRows = Math.max(0, Math.min(10, (int) value));
                break;
            case "gridcols":
                layoutConfig.gridCols = Math.max(0, Math.min(10, (int) value));
                break;
                
            // Collage parameters
            case "minsize":
                layoutConfig.minSize = Math.max(0.1f, Math.min(2.0f, value));
                if (layoutConfig.minSize > layoutConfig.maxSize) {
                    layoutConfig.maxSize = layoutConfig.minSize;
                }
                break;
            case "maxsize":
                layoutConfig.maxSize = Math.max(layoutConfig.minSize, Math.min(3.0f, value));
                break;
            case "minrotation":
                layoutConfig.minRotation = Math.max(-180.0f, Math.min(180.0f, value));
                if (layoutConfig.minRotation > layoutConfig.maxRotation) {
                    layoutConfig.maxRotation = layoutConfig.minRotation;
                }
                break;
            case "maxrotation":
                layoutConfig.maxRotation = Math.max(layoutConfig.minRotation, Math.min(180.0f, value));
                break;
            case "overlapamount":
                layoutConfig.overlapAmount = Math.max(0.0f, Math.min(1.0f, value));
                break;
                
            // Circular parameters
            case "circleradius":
                layoutConfig.circleRadius = Math.max(50.0f, Math.min(1000.0f, value));
                break;
            case "arcspan":
                layoutConfig.arcSpan = Math.max(30.0f, Math.min(360.0f, value));
                break;
                
            // Flowing parameters
            case "pathcurvature":
                layoutConfig.pathCurvature = Math.max(0.0f, Math.min(2.0f, value));
                break;
            case "pathspacing":
                layoutConfig.pathSpacing = Math.max(10.0f, Math.min(500.0f, value));
                break;
                
            default:
                logWarning("Unknown layout parameter: " + parameterName);
                break;
        }
    }
    
    /**
     * Update layout display region.
     * 
     * @param x X coordinate of the region
     * @param y Y coordinate of the region
     * @param width Width of the region
     * @param height Height of the region
     */
    public void updateLayoutDisplayRegion(float x, float y, float width, float height) {
        if (width > 0 && height > 0) {
            layoutConfig.displayRegion = new LayoutConfig.Rectangle(x, y, width, height);
        } else {
            logWarning("Display region width and height must be positive");
        }
    }
    
    /**
     * Update layout aspect ratio mode.
     * 
     * @param mode The new aspect ratio mode ("preserve", "stretch", "crop")
     */
    public void updateLayoutAspectRatioMode(String mode) {
        if (mode != null) {
            try {
                LayoutConfig.AspectRatioMode aspectMode = LayoutConfig.AspectRatioMode.valueOf(mode.toUpperCase());
                layoutConfig.aspectRatioMode = aspectMode;
            } catch (IllegalArgumentException e) {
                logWarning("Invalid aspect ratio mode: " + mode);
            }
        }
    }
    
    /**
     * Update grid alignment.
     * 
     * @param alignment The new alignment ("center", "left", "right", "top", "bottom", etc.)
     */
    public void updateGridAlignment(String alignment) {
        if (alignment != null) {
            try {
                LayoutConfig.Alignment gridAlign = LayoutConfig.Alignment.valueOf(alignment.toUpperCase());
                layoutConfig.gridAlignment = gridAlign;
            } catch (IllegalArgumentException e) {
                logWarning("Invalid grid alignment: " + alignment);
            }
        }
    }
    
    /**
     * Update circular rotation direction.
     * 
     * @param direction The new rotation direction ("clockwise", "counterclockwise")
     */
    public void updateCircularRotationDirection(String direction) {
        if (direction != null) {
            try {
                LayoutConfig.RotationDirection rotDir = LayoutConfig.RotationDirection.valueOf(direction.toUpperCase());
                layoutConfig.rotationDirection = rotDir;
            } catch (IllegalArgumentException e) {
                logWarning("Invalid rotation direction: " + direction);
            }
        }
    }
    
    /**
     * Update flowing direction.
     * 
     * @param direction The new flow direction ("horizontal", "vertical", "diagonal_up", "diagonal_down")
     */
    public void updateFlowingDirection(String direction) {
        if (direction != null) {
            try {
                LayoutConfig.FlowDirection flowDir = LayoutConfig.FlowDirection.valueOf(direction.toUpperCase());
                layoutConfig.flowDirection = flowDir;
            } catch (IllegalArgumentException e) {
                logWarning("Invalid flow direction: " + direction);
            }
        }
    }
    
    /**
     * Set layout transition duration.
     * 
     * @param duration The new transition duration in milliseconds
     */
    public void setLayoutTransitionDuration(float duration) {
        if (duration > 0) {
            this.layoutTransitionDuration = duration;
        } else {
            logWarning("Layout transition duration must be positive: " + duration);
        }
    }
    
    // Transition configuration getters
    
    public String getCurrentTransitionEffect() {
        return currentTransitionEffect;
    }
    
    public float getTransitionDuration() {
        return transitionDuration;
    }
    
    public String getTransitionEasing() {
        return transitionEasing;
    }
    
    public boolean isStaggerEnabled() {
        return enableStagger;
    }
    
    public float getStaggerDelay() {
        return staggerDelay;
    }
    
    public String getSlideDirection() {
        return slideDirection;
    }
    
    public String getZoomMode() {
        return zoomMode;
    }
    
    public float getRotationAngle() {
        return rotationAngle;
    }
    
    public String getBlendMode() {
        return blendMode;
    }
    
    public float getMorphIntensity() {
        return morphIntensity;
    }
    
    /**
     * Update the current transition effect.
     * 
     * @param effect The new transition effect ("fade", "slide", "zoom", "rotate", "morph")
     */
    public void setCurrentTransitionEffect(String effect) {
        if (isValidTransitionEffect(effect.toLowerCase())) {
            this.currentTransitionEffect = effect.toLowerCase();
        } else {
            logWarning("Invalid transition effect: " + effect);
        }
    }
    
    /**
     * Update transition duration.
     * 
     * @param duration The new duration in milliseconds
     */
    public void setTransitionDuration(float duration) {
        if (duration > 0) {
            this.transitionDuration = duration;
        } else {
            logWarning("Transition duration must be positive: " + duration);
        }
    }
    
    /**
     * Update transition easing function.
     * 
     * @param easing The new easing function
     */
    public void setTransitionEasing(String easing) {
        if (isValidEasingFunction(easing.toLowerCase())) {
            this.transitionEasing = easing.toLowerCase();
        } else {
            logWarning("Invalid easing function: " + easing);
        }
    }
    
    /**
     * Create a TransitionConfig object from current settings.
     * 
     * @return A new TransitionConfig with current settings
     */
    public TransitionConfig createTransitionConfig() {
        TransitionConfig config = new TransitionConfig();
        config.setDuration(this.transitionDuration);
        
        // Convert string easing to enum
        EasingFunction easingEnum;
        switch (this.transitionEasing.toLowerCase()) {
            case "linear":
                easingEnum = EasingFunction.LINEAR;
                break;
            case "ease_in":
                easingEnum = EasingFunction.EASE_IN;
                break;
            case "ease_out":
                easingEnum = EasingFunction.EASE_OUT;
                break;
            case "ease_in_out":
                easingEnum = EasingFunction.EASE_IN_OUT;
                break;
            case "bounce":
                easingEnum = EasingFunction.BOUNCE;
                break;
            case "elastic":
                easingEnum = EasingFunction.ELASTIC;
                break;
            default:
                easingEnum = EasingFunction.EASE_IN_OUT;
                break;
        }
        config.setEasingFunction(easingEnum);
        
        config.setEnableStagger(this.enableStagger);
        config.setStaggerDelay(this.staggerDelay);
        
        // Convert string slide direction to enum
        TransitionConfig.SlideDirection slideDir;
        switch (this.slideDirection.toLowerCase()) {
            case "up":
                slideDir = TransitionConfig.SlideDirection.UP;
                break;
            case "down":
                slideDir = TransitionConfig.SlideDirection.DOWN;
                break;
            case "left":
                slideDir = TransitionConfig.SlideDirection.LEFT;
                break;
            case "right":
                slideDir = TransitionConfig.SlideDirection.RIGHT;
                break;
            case "up_left":
                slideDir = TransitionConfig.SlideDirection.UP_LEFT;
                break;
            case "up_right":
                slideDir = TransitionConfig.SlideDirection.UP_RIGHT;
                break;
            case "down_left":
                slideDir = TransitionConfig.SlideDirection.DOWN_LEFT;
                break;
            case "down_right":
                slideDir = TransitionConfig.SlideDirection.DOWN_RIGHT;
                break;
            default:
                slideDir = TransitionConfig.SlideDirection.LEFT;
                break;
        }
        config.setSlideDirection(slideDir);
        
        // Convert string zoom mode to enum
        TransitionConfig.ZoomMode zoomModeEnum;
        switch (this.zoomMode.toLowerCase()) {
            case "zoom_in":
                zoomModeEnum = TransitionConfig.ZoomMode.ZOOM_IN;
                break;
            case "zoom_out":
                zoomModeEnum = TransitionConfig.ZoomMode.ZOOM_OUT;
                break;
            case "zoom_both":
                zoomModeEnum = TransitionConfig.ZoomMode.ZOOM_BOTH;
                break;
            default:
                zoomModeEnum = TransitionConfig.ZoomMode.ZOOM_IN;
                break;
        }
        config.setZoomMode(zoomModeEnum);
        
        config.setRotationAngle(this.rotationAngle);
        
        // Convert string blend mode to enum
        TransitionConfig.BlendMode blendModeEnum;
        switch (this.blendMode.toLowerCase()) {
            case "normal":
                blendModeEnum = TransitionConfig.BlendMode.NORMAL;
                break;
            case "multiply":
                blendModeEnum = TransitionConfig.BlendMode.MULTIPLY;
                break;
            case "screen":
                blendModeEnum = TransitionConfig.BlendMode.SCREEN;
                break;
            case "overlay":
                blendModeEnum = TransitionConfig.BlendMode.OVERLAY;
                break;
            case "soft_light":
                blendModeEnum = TransitionConfig.BlendMode.SOFT_LIGHT;
                break;
            case "hard_light":
                blendModeEnum = TransitionConfig.BlendMode.HARD_LIGHT;
                break;
            case "color_dodge":
                blendModeEnum = TransitionConfig.BlendMode.COLOR_DODGE;
                break;
            case "color_burn":
                blendModeEnum = TransitionConfig.BlendMode.COLOR_BURN;
                break;
            default:
                blendModeEnum = TransitionConfig.BlendMode.NORMAL;
                break;
        }
        config.setBlendMode(blendModeEnum);
        
        return config;
    }
    
    /**
     * Update transition configuration from a TransitionConfig object.
     * 
     * @param config The new transition configuration
     */
    public void updateTransitionConfig(TransitionConfig config) {
        if (config != null) {
            this.transitionDuration = config.getDuration();
            this.transitionEasing = config.getEasingFunction().name().toLowerCase();
            this.enableStagger = config.isEnableStagger();
            this.staggerDelay = config.getStaggerDelay();
            this.slideDirection = config.getSlideDirection().name().toLowerCase();
            this.zoomMode = config.getZoomMode().name().toLowerCase();
            this.rotationAngle = config.getRotationAngle();
            this.blendMode = config.getBlendMode().name().toLowerCase();
        }
    }
    
    /**
     * Update specific transition parameters.
     * 
     * @param parameterName The name of the parameter to update
     * @param value The new value for the parameter
     */
    public void updateTransitionParameter(String parameterName, float value) {
        if (parameterName == null) {
            logWarning("Transition parameter name cannot be null");
            return;
        }
        
        switch (parameterName.toLowerCase()) {
            case "duration":
                setTransitionDuration(value);
                break;
            case "staggerdelay":
                this.staggerDelay = Math.max(0.0f, value);
                break;
            case "rotationangle":
                this.rotationAngle = value;
                break;
            case "morphintensity":
                this.morphIntensity = Math.max(0.0f, Math.min(1.0f, value));
                break;
            default:
                logWarning("Unknown transition parameter: " + parameterName);
                break;
        }
    }
    
    /**
     * Update transition boolean parameters.
     * 
     * @param parameterName The name of the parameter to update
     * @param value The new boolean value
     */
    public void updateTransitionParameter(String parameterName, boolean value) {
        if (parameterName == null) {
            logWarning("Transition parameter name cannot be null");
            return;
        }
        
        switch (parameterName.toLowerCase()) {
            case "enablestagger":
                this.enableStagger = value;
                break;
            default:
                logWarning("Unknown boolean transition parameter: " + parameterName);
                break;
        }
    }
    
    /**
     * Update transition string parameters.
     * 
     * @param parameterName The name of the parameter to update
     * @param value The new string value
     */
    public void updateTransitionParameter(String parameterName, String value) {
        if (parameterName == null || value == null) {
            logWarning("Transition parameter name and value cannot be null");
            return;
        }
        
        switch (parameterName.toLowerCase()) {
            case "effect":
                setCurrentTransitionEffect(value);
                break;
            case "easing":
                setTransitionEasing(value);
                break;
            case "slidedirection":
                if (isValidSlideDirection(value.toLowerCase())) {
                    this.slideDirection = value.toLowerCase();
                } else {
                    logWarning("Invalid slide direction: " + value);
                }
                break;
            case "zoommode":
                if (isValidZoomMode(value.toLowerCase())) {
                    this.zoomMode = value.toLowerCase();
                } else {
                    logWarning("Invalid zoom mode: " + value);
                }
                break;
            case "blendmode":
                if (isValidBlendMode(value.toLowerCase())) {
                    this.blendMode = value.toLowerCase();
                } else {
                    logWarning("Invalid blend mode: " + value);
                }
                break;
            default:
                logWarning("Unknown string transition parameter: " + parameterName);
                break;
        }
    }
    
    // Visual effects configuration getters
    
    /**
     * Gets the current visual effects configuration.
     * 
     * @return A copy of the current visual effects configuration
     */
    public VisualEffectsConfig getVisualEffectsConfig() {
        return visualEffectsConfig.copy();
    }
    
    /**
     * Updates the visual effects configuration.
     * 
     * @param config The new visual effects configuration
     */
    public void setVisualEffectsConfig(VisualEffectsConfig config) {
        if (config != null) {
            this.visualEffectsConfig = config.copy();
            this.visualEffectsConfig.validate();
        }
    }
    
    /**
     * Update specific visual effects parameters.
     * 
     * @param parameterName The name of the parameter to update
     * @param value The new value for the parameter
     */
    public void updateVisualEffectsParameter(String parameterName, float value) {
        if (parameterName == null) {
            logWarning("Visual effects parameter name cannot be null");
            return;
        }
        
        switch (parameterName.toLowerCase()) {
            // Blur parameters
            case "blurradius":
                visualEffectsConfig.blurRadius = Math.max(0.0f, Math.min(50.0f, value));
                break;
                
            // Color filter parameters
            case "colorintensity":
                visualEffectsConfig.colorIntensity = Math.max(0.0f, Math.min(2.0f, value));
                break;
                
            // Brightness/contrast parameters
            case "brightness":
                visualEffectsConfig.brightness = Math.max(-1.0f, Math.min(1.0f, value));
                break;
            case "contrast":
                visualEffectsConfig.contrast = Math.max(-1.0f, Math.min(1.0f, value));
                break;
            case "gamma":
                visualEffectsConfig.gamma = Math.max(0.1f, Math.min(3.0f, value));
                break;
                
            // Particle system parameters
            case "particlecount":
                visualEffectsConfig.particleCount = Math.max(0, Math.min(1000, (int) value));
                break;
            case "particlelifetime":
                visualEffectsConfig.particleLifetime = Math.max(100.0f, Math.min(10000.0f, value));
                break;
            case "particlesize":
                visualEffectsConfig.particleSize = Math.max(0.5f, Math.min(20.0f, value));
                break;
            case "particlespeed":
                visualEffectsConfig.particleSpeed = Math.max(0.0f, Math.min(500.0f, value));
                break;
                
            // Border effects parameters
            case "glowradius":
                visualEffectsConfig.glowRadius = Math.max(0.0f, Math.min(50.0f, value));
                break;
            case "shadowoffsetx":
                visualEffectsConfig.shadowOffsetX = value;
                break;
            case "shadowoffsety":
                visualEffectsConfig.shadowOffsetY = value;
                break;
            case "shadowblur":
                visualEffectsConfig.shadowBlur = Math.max(0.0f, Math.min(20.0f, value));
                break;
            case "outlinethickness":
                visualEffectsConfig.outlineThickness = Math.max(0.0f, Math.min(10.0f, value));
                break;
                
            default:
                logWarning("Unknown visual effects parameter: " + parameterName);
                break;
        }
    }
    
    /**
     * Update visual effects boolean parameters.
     * 
     * @param parameterName The name of the parameter to update
     * @param value The new boolean value
     */
    public void updateVisualEffectsParameter(String parameterName, boolean value) {
        if (parameterName == null) {
            logWarning("Visual effects parameter name cannot be null");
            return;
        }
        
        switch (parameterName.toLowerCase()) {
            case "enableblur":
                visualEffectsConfig.enableBlur = value;
                break;
            case "enablemotionblur":
                visualEffectsConfig.enableMotionBlur = value;
                break;
            case "enableselectiveblur":
                visualEffectsConfig.enableSelectiveBlur = value;
                break;
            case "enableparticles":
                visualEffectsConfig.enableParticles = value;
                break;
            case "enableglow":
                visualEffectsConfig.enableGlow = value;
                break;
            case "enableshadow":
                visualEffectsConfig.enableShadow = value;
                break;
            case "enableoutline":
                visualEffectsConfig.enableOutline = value;
                break;
            default:
                logWarning("Unknown boolean visual effects parameter: " + parameterName);
                break;
        }
    }
    
    /**
     * Update visual effects string parameters.
     * 
     * @param parameterName The name of the parameter to update
     * @param value The new string value
     */
    public void updateVisualEffectsParameter(String parameterName, String value) {
        if (parameterName == null || value == null) {
            logWarning("Visual effects parameter name and value cannot be null");
            return;
        }
        
        switch (parameterName.toLowerCase()) {
            case "colorfilter":
                try {
                    VisualEffectsConfig.ColorFilterType filter = VisualEffectsConfig.ColorFilterType.valueOf(value.toUpperCase());
                    visualEffectsConfig.colorFilter = filter;
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid color filter type: " + value);
                }
                break;
            case "particletype":
                try {
                    VisualEffectsConfig.ParticleType type = VisualEffectsConfig.ParticleType.valueOf(value.toUpperCase());
                    visualEffectsConfig.particleType = type;
                } catch (IllegalArgumentException e) {
                    logWarning("Invalid particle type: " + value);
                }
                break;
            default:
                logWarning("Unknown string visual effects parameter: " + parameterName);
                break;
        }
    }
    
    /**
     * Update visual effects color parameters.
     * 
     * @param parameterName The name of the parameter to update
     * @param colorValue The new color value as hex string (e.g., "#FF0000")
     */
    public void updateVisualEffectsColor(String parameterName, String colorValue) {
        if (parameterName == null || colorValue == null) {
            logWarning("Visual effects parameter name and color value cannot be null");
            return;
        }
        
        if (!isValidHexColor(colorValue)) {
            logWarning("Invalid hex color format: " + colorValue);
            return;
        }
        
        int color = parseHexColor(colorValue);
        
        switch (parameterName.toLowerCase()) {
            case "tintcolor":
                visualEffectsConfig.tintColor = color;
                break;
            case "particlecolor":
                visualEffectsConfig.particleColor = color;
                break;
            case "glowcolor":
                visualEffectsConfig.glowColor = color;
                break;
            case "shadowcolor":
                visualEffectsConfig.shadowColor = color;
                break;
            case "outlinecolor":
                visualEffectsConfig.outlineColor = color;
                break;
            default:
                logWarning("Unknown color visual effects parameter: " + parameterName);
                break;
        }
    }
    
    /**
     * Toggle a visual effect on or off.
     * 
     * @param effectName The name of the effect to toggle
     */
    public void toggleVisualEffect(String effectName) {
        if (effectName == null) {
            logWarning("Effect name cannot be null");
            return;
        }
        
        switch (effectName.toLowerCase()) {
            case "blur":
                visualEffectsConfig.enableBlur = !visualEffectsConfig.enableBlur;
                break;
            case "motionblur":
                visualEffectsConfig.enableMotionBlur = !visualEffectsConfig.enableMotionBlur;
                break;
            case "selectiveblur":
                visualEffectsConfig.enableSelectiveBlur = !visualEffectsConfig.enableSelectiveBlur;
                break;
            case "particles":
                visualEffectsConfig.enableParticles = !visualEffectsConfig.enableParticles;
                break;
            case "glow":
                visualEffectsConfig.enableGlow = !visualEffectsConfig.enableGlow;
                break;
            case "shadow":
                visualEffectsConfig.enableShadow = !visualEffectsConfig.enableShadow;
                break;
            case "outline":
                visualEffectsConfig.enableOutline = !visualEffectsConfig.enableOutline;
                break;
            default:
                logWarning("Unknown visual effect: " + effectName);
                break;
        }
    }
    
    /**
     * Reset all visual effects to their default values.
     */
    public void resetVisualEffects() {
        this.visualEffectsConfig = new VisualEffectsConfig();
    }
    
    /**
     * Get the current intensity/quality level for visual effects.
     * This can be used to adjust effect quality based on performance.
     * 
     * @return A value between 0.0 (lowest quality) and 1.0 (highest quality)
     */
    public float getVisualEffectsQuality() {
        // Calculate quality based on enabled effects and their intensity
        float quality = 1.0f;
        
        if (visualEffectsConfig.enableBlur) {
            quality *= Math.max(0.5f, 1.0f - (visualEffectsConfig.blurRadius / 50.0f));
        }
        
        if (visualEffectsConfig.enableParticles) {
            quality *= Math.max(0.3f, 1.0f - (visualEffectsConfig.particleCount / 1000.0f));
        }
        
        return Math.max(0.1f, Math.min(1.0f, quality));
    }
    
    /**
     * Adjust visual effects quality for performance optimization.
     * 
     * @param targetQuality The target quality level (0.0 to 1.0)
     */
    public void adjustVisualEffectsQuality(float targetQuality) {
        targetQuality = Math.max(0.0f, Math.min(1.0f, targetQuality));
        
        if (targetQuality < 0.3f) {
            // Low quality: disable expensive effects
            visualEffectsConfig.enableBlur = false;
            visualEffectsConfig.enableParticles = false;
            visualEffectsConfig.particleCount = Math.min(visualEffectsConfig.particleCount, 20);
        } else if (targetQuality < 0.7f) {
            // Medium quality: reduce effect intensity
            visualEffectsConfig.blurRadius = Math.min(visualEffectsConfig.blurRadius, 5.0f);
            visualEffectsConfig.particleCount = Math.min(visualEffectsConfig.particleCount, 100);
        }
        // High quality: keep all effects enabled
    }
}
