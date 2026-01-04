package com.icandy.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
}
