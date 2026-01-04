package com.icandy.unit;

import com.icandy.common.ConfigurationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigurationManager.
 */
class ConfigurationManagerTest {
    
    private ConfigurationManager config;
    
    @BeforeEach
    void setUp() {
        config = new ConfigurationManager();
    }
    
    @Test
    void testDefaultValues() {
        // Build phase defaults
        assertEquals(5, config.getImagesPerWord());
        assertEquals("~/.icandy/unsplash.properties", config.getUnsplashPropertiesFile());
        assertEquals("data/images", config.getImageStorageDir());
        assertEquals("data/associations.json", config.getAssociationsFile());
        assertEquals("data/stopwords.txt", config.getStopWordsFile());
        assertEquals(3, config.getMaxRetries());
        
        // Run phase defaults
        assertEquals(100, config.getBeatSensitivity());
        assertEquals(2000, config.getMinPhraseDuration());
        assertEquals(10000, config.getMaxPhraseDuration());
        assertEquals(300, config.getMsPerWord());
        assertEquals(30, config.getFrameRate());
        assertEquals(48, config.getTextSize());
        assertEquals("#FFFFFF", config.getTextColor());
        assertEquals("#000000", config.getBackgroundColor());
        assertTrue(config.isKeyboardNavigationEnabled());
        assertEquals(3, config.getSimultaneousImageCount());
        assertTrue(config.isLoopPhrasesEnabled());
        assertEquals("microphone", config.getAudioSource());
    }
    
    @Test
    void testLoadFromFile_ValidConfiguration(@TempDir Path tempDir) throws IOException {
        String configJson = "{\n" +
            "  \"build\": {\n" +
            "    \"imagesPerWord\": 10,\n" +
            "    \"unsplashPropertiesFile\": \"/custom/path/unsplash.properties\",\n" +
            "    \"imageStorageDir\": \"custom/images\",\n" +
            "    \"associationsFile\": \"custom/associations.json\",\n" +
            "    \"stopWordsFile\": \"custom/stopwords.txt\",\n" +
            "    \"maxRetries\": 5\n" +
            "  },\n" +
            "  \"run\": {\n" +
            "    \"beatSensitivity\": 200,\n" +
            "    \"minPhraseDuration\": 3000,\n" +
            "    \"maxPhraseDuration\": 15000,\n" +
            "    \"msPerWord\": 400,\n" +
            "    \"frameRate\": 60,\n" +
            "    \"textSize\": 64,\n" +
            "    \"textColor\": \"#FF0000\",\n" +
            "    \"backgroundColor\": \"#0000FF\",\n" +
            "    \"enableKeyboardNavigation\": false,\n" +
            "    \"simultaneousImageCount\": 5,\n" +
            "    \"loopPhrases\": false,\n" +
            "    \"audioSource\": \"line-in\"\n" +
            "  }\n" +
            "}";
        
        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, configJson);
        
        config.loadFromFile(configFile.toString());
        
        // Verify build phase values
        assertEquals(10, config.getImagesPerWord());
        assertEquals("/custom/path/unsplash.properties", config.getUnsplashPropertiesFile());
        assertEquals("custom/images", config.getImageStorageDir());
        assertEquals("custom/associations.json", config.getAssociationsFile());
        assertEquals("custom/stopwords.txt", config.getStopWordsFile());
        assertEquals(5, config.getMaxRetries());
        
        // Verify run phase values
        assertEquals(200, config.getBeatSensitivity());
        assertEquals(3000, config.getMinPhraseDuration());
        assertEquals(15000, config.getMaxPhraseDuration());
        assertEquals(400, config.getMsPerWord());
        assertEquals(60, config.getFrameRate());
        assertEquals(64, config.getTextSize());
        assertEquals("#FF0000", config.getTextColor());
        assertEquals("#0000FF", config.getBackgroundColor());
        assertFalse(config.isKeyboardNavigationEnabled());
        assertEquals(5, config.getSimultaneousImageCount());
        assertFalse(config.isLoopPhrasesEnabled());
        assertEquals("line-in", config.getAudioSource());
    }
    
    @Test
    void testLoadFromFile_PartialConfiguration(@TempDir Path tempDir) throws IOException {
        String configJson = "{\n" +
            "  \"build\": {\n" +
            "    \"imagesPerWord\": 7\n" +
            "  }\n" +
            "}";
        
        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, configJson);
        
        config.loadFromFile(configFile.toString());
        
        // Verify overridden value
        assertEquals(7, config.getImagesPerWord());
        
        // Verify defaults are still used for other values
        assertEquals("~/.icandy/unsplash.properties", config.getUnsplashPropertiesFile());
        assertEquals(3, config.getMaxRetries());
        assertEquals(100, config.getBeatSensitivity());
    }
    
    @Test
    void testLoadFromFile_InvalidImagesPerWord(@TempDir Path tempDir) throws IOException {
        String configJson = "{\n" +
            "  \"build\": {\n" +
            "    \"imagesPerWord\": -5\n" +
            "  }\n" +
            "}";
        
        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, configJson);
        
        config.loadFromFile(configFile.toString());
        
        // Should use default value for invalid input
        assertEquals(5, config.getImagesPerWord());
    }
    
    @Test
    void testLoadFromFile_InvalidMaxRetries(@TempDir Path tempDir) throws IOException {
        String configJson = "{\n" +
            "  \"build\": {\n" +
            "    \"maxRetries\": -1\n" +
            "  }\n" +
            "}";
        
        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, configJson);
        
        config.loadFromFile(configFile.toString());
        
        // Should use default value for invalid input
        assertEquals(3, config.getMaxRetries());
    }
    
    @Test
    void testLoadFromFile_InvalidHexColor(@TempDir Path tempDir) throws IOException {
        String configJson = "{\n" +
            "  \"run\": {\n" +
            "    \"textColor\": \"not-a-color\",\n" +
            "    \"backgroundColor\": \"#GGGGGG\"\n" +
            "  }\n" +
            "}";
        
        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, configJson);
        
        config.loadFromFile(configFile.toString());
        
        // Should use default values for invalid colors
        assertEquals("#FFFFFF", config.getTextColor());
        assertEquals("#000000", config.getBackgroundColor());
    }
    
    @Test
    void testLoadFromFile_InvalidDurations(@TempDir Path tempDir) throws IOException {
        String configJson = "{\n" +
            "  \"run\": {\n" +
            "    \"minPhraseDuration\": -1000,\n" +
            "    \"maxPhraseDuration\": 0\n" +
            "  }\n" +
            "}";
        
        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, configJson);
        
        config.loadFromFile(configFile.toString());
        
        // Should use default values for invalid durations
        assertEquals(2000, config.getMinPhraseDuration());
        assertEquals(10000, config.getMaxPhraseDuration());
    }
    
    @Test
    void testLoadFromFile_NonExistentFile() {
        assertThrows(IOException.class, () -> {
            config.loadFromFile("/nonexistent/config.json");
        });
    }
    
    @Test
    void testLoadFromFile_InvalidJson(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("invalid.json");
        Files.writeString(configFile, "not valid json {");
        
        assertThrows(IOException.class, () -> {
            config.loadFromFile(configFile.toString());
        });
    }
    
    @Test
    void testLoadFromFile_EmptyJson(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("empty.json");
        Files.writeString(configFile, "{}");
        
        // Should not throw, just use defaults
        config.loadFromFile(configFile.toString());
        
        assertEquals(5, config.getImagesPerWord());
        assertEquals(100, config.getBeatSensitivity());
    }
    
    @Test
    void testLoadFromFile_WithComments(@TempDir Path tempDir) throws IOException {
        String configJson = "{\n" +
            "  \"_comment\": \"This is a comment\",\n" +
            "  \"build\": {\n" +
            "    \"imagesPerWord\": 8\n" +
            "  }\n" +
            "}";
        
        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, configJson);
        
        // Should ignore comment field and load successfully
        config.loadFromFile(configFile.toString());
        
        assertEquals(8, config.getImagesPerWord());
    }
    
    @Test
    void testLoadFromFile_WithExtraFields(@TempDir Path tempDir) throws IOException {
        String configJson = "{\n" +
            "  \"build\": {\n" +
            "    \"imagesPerWord\": 6,\n" +
            "    \"unknownField\": \"should be ignored\"\n" +
            "  },\n" +
            "  \"unknownSection\": {\n" +
            "    \"someValue\": 123\n" +
            "  }\n" +
            "}";
        
        Path configFile = tempDir.resolve("config.json");
        Files.writeString(configFile, configJson);
        
        // Should ignore unknown fields and load successfully
        config.loadFromFile(configFile.toString());
        
        assertEquals(6, config.getImagesPerWord());
    }
}
