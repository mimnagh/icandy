package com.icandy.unit;

import com.icandy.build.AssociationManager;
import com.icandy.build.BuildOrchestrator;
import com.icandy.build.ImageDownloader;
import com.icandy.build.TextParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BuildOrchestrator.
 * Tests complete build workflow, error handling, and progress reporting.
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6
 */
class BuildOrchestratorTest {
    
    private BuildOrchestrator orchestrator;
    private MockImageDownloader mockDownloader;
    private AssociationManager associationManager;
    private TextParser textParser;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() throws IOException {
        // Create mock components
        Set<String> stopWords = new HashSet<>();
        stopWords.add("the");
        stopWords.add("a");
        stopWords.add("is");
        stopWords.add("and");
        
        textParser = new TextParser(stopWords);
        mockDownloader = new MockImageDownloader();
        associationManager = new AssociationManager();
        
        orchestrator = new BuildOrchestrator(textParser, mockDownloader, associationManager);
    }
    
    @Test
    void testCompleteWorkflow_WithSampleText() throws IOException {
        // Create sample text file
        Path textFile = tempDir.resolve("sample.txt");
        String sampleText = "Hello world. This is a test. The sun is shining.";
        Files.writeString(textFile, sampleText);
        
        // Create config file
        Path configFile = createTestConfig();
        orchestrator.loadConfiguration(configFile.toString());
        
        // Run build
        orchestrator.runBuild(textFile.toString());
        
        // Verify progress tracking
        assertTrue(orchestrator.getProcessedWordCount() > 0);
        
        // Verify associations were created for content words
        assertTrue(associationManager.hasWord("hello"));
        assertTrue(associationManager.hasWord("world"));
        assertTrue(associationManager.hasWord("test"));
        assertTrue(associationManager.hasWord("sun"));
        assertTrue(associationManager.hasWord("shining"));
        
        // Verify stop words were filtered
        assertFalse(associationManager.hasWord("the"));
        assertFalse(associationManager.hasWord("a"));
        assertFalse(associationManager.hasWord("is"));
    }
    
    @Test
    void testWorkflow_WithEmptyTextFile() throws IOException {
        Path textFile = tempDir.resolve("empty.txt");
        Files.writeString(textFile, "   ");
        
        Path configFile = createTestConfig();
        orchestrator.loadConfiguration(configFile.toString());
        
        assertThrows(IOException.class, () -> {
            orchestrator.runBuild(textFile.toString());
        });
    }
    
    @Test
    void testWorkflow_WithNonExistentFile() throws IOException {
        Path configFile = createTestConfig();
        orchestrator.loadConfiguration(configFile.toString());
        
        assertThrows(IOException.class, () -> {
            orchestrator.runBuild("nonexistent.txt");
        });
    }
    
    @Test
    void testErrorHandling_PartialImageDownloadFailure() throws IOException {
        // Configure mock to fail on specific words
        mockDownloader.setFailWords(Set.of("fail"));
        
        Path textFile = tempDir.resolve("test.txt");
        Files.writeString(textFile, "Hello fail world.");
        
        Path configFile = createTestConfig();
        orchestrator.loadConfiguration(configFile.toString());
        
        // Should not throw - should handle failure gracefully
        orchestrator.runBuild(textFile.toString());
        
        // Verify successful words were processed
        assertTrue(associationManager.hasWord("hello"));
        assertTrue(associationManager.hasWord("world"));
        
        // Verify failed word was tracked
        assertEquals(1, orchestrator.getFailedWordCount());
        assertTrue(orchestrator.getFailedWords().contains("fail"));
    }
    
    @Test
    void testErrorHandling_NoImagesFound() throws IOException {
        // Configure mock to return no images
        mockDownloader.setReturnNoImages(true);
        
        Path textFile = tempDir.resolve("test.txt");
        Files.writeString(textFile, "Hello world.");
        
        Path configFile = createTestConfig();
        orchestrator.loadConfiguration(configFile.toString());
        
        orchestrator.runBuild(textFile.toString());
        
        // All words should fail
        assertEquals(2, orchestrator.getFailedWordCount());
    }
    
    @Test
    void testProgressReporting() throws IOException {
        Path textFile = tempDir.resolve("test.txt");
        Files.writeString(textFile, "One two three four five.");
        
        Path configFile = createTestConfig();
        orchestrator.loadConfiguration(configFile.toString());
        
        orchestrator.runBuild(textFile.toString());
        
        // Verify progress tracking
        assertEquals(5, orchestrator.getProcessedWordCount());
        assertEquals(0, orchestrator.getFailedWordCount());
    }
    
    @Test
    void testConfiguration_InvalidImagesPerWord() throws IOException {
        Path configFile = tempDir.resolve("config.json");
        String configJson = String.format(
            "{\n" +
            "  \"build\": {\n" +
            "    \"imagesPerWord\": -5,\n" +
            "    \"imageStorageDir\": \"%s\",\n" +
            "    \"associationsFile\": \"%s\",\n" +
            "    \"stopWordsFile\": \"%s\",\n" +
            "    \"unsplashPropertiesFile\": \"%s\"\n" +
            "  }\n" +
            "}",
            tempDir.resolve("images").toString(),
            tempDir.resolve("associations.json").toString(),
            createStopWordsFile().toString(),
            createUnsplashPropsFile().toString()
        );
        Files.writeString(configFile, configJson);
        
        // Should use default value (5) instead of invalid -5
        orchestrator.loadConfiguration(configFile.toString());
        
        Path textFile = tempDir.resolve("test.txt");
        Files.writeString(textFile, "Hello world.");
        
        orchestrator.runBuild(textFile.toString());
        
        // Should still work with default value
        assertTrue(associationManager.hasWord("hello"));
    }
    
    @Test
    void testConfiguration_MissingUnsplashCredentials() throws IOException {
        Path configFile = tempDir.resolve("config.json");
        String configJson = String.format(
            "{\n" +
            "  \"build\": {\n" +
            "    \"imagesPerWord\": 3,\n" +
            "    \"imageStorageDir\": \"%s\",\n" +
            "    \"associationsFile\": \"%s\",\n" +
            "    \"stopWordsFile\": \"%s\"\n" +
            "  }\n" +
            "}",
            tempDir.resolve("images").toString(),
            tempDir.resolve("associations.json").toString(),
            createStopWordsFile().toString()
        );
        Files.writeString(configFile, configJson);
        
        assertThrows(IOException.class, () -> {
            orchestrator.loadConfiguration(configFile.toString());
        });
    }
    
    @Test
    void testWorkflow_OnlyStopWords() throws IOException {
        Path textFile = tempDir.resolve("stopwords.txt");
        Files.writeString(textFile, "The a is and.");
        
        Path configFile = createTestConfig();
        orchestrator.loadConfiguration(configFile.toString());
        
        // Should handle gracefully - no content words to process
        orchestrator.runBuild(textFile.toString());
        
        assertEquals(0, orchestrator.getProcessedWordCount());
        assertEquals(0, associationManager.getWordCount());
    }
    
    /**
     * Helper method to create a test configuration file.
     */
    private Path createTestConfig() throws IOException {
        Path configFile = tempDir.resolve("config.json");
        String configJson = String.format(
            "{\n" +
            "  \"build\": {\n" +
            "    \"imagesPerWord\": 3,\n" +
            "    \"imageStorageDir\": \"%s\",\n" +
            "    \"associationsFile\": \"%s\",\n" +
            "    \"stopWordsFile\": \"%s\",\n" +
            "    \"unsplashPropertiesFile\": \"%s\",\n" +
            "    \"maxRetries\": 2\n" +
            "  }\n" +
            "}",
            tempDir.resolve("images").toString(),
            tempDir.resolve("associations.json").toString(),
            createStopWordsFile().toString(),
            createUnsplashPropsFile().toString()
        );
        Files.writeString(configFile, configJson);
        return configFile;
    }
    
    /**
     * Helper method to create a stop words file.
     */
    private Path createStopWordsFile() throws IOException {
        Path stopWordsFile = tempDir.resolve("stopwords.txt");
        String stopWords = "the\na\nis\nand\n";
        Files.writeString(stopWordsFile, stopWords);
        return stopWordsFile;
    }
    
    /**
     * Helper method to create a mock Unsplash properties file.
     */
    private Path createUnsplashPropsFile() throws IOException {
        Path propsFile = tempDir.resolve("unsplash.properties");
        String props = "access_key=test_key_12345\n";
        Files.writeString(propsFile, props);
        return propsFile;
    }
    
    /**
     * Mock ImageDownloader for testing without actual API calls.
     */
    private static class MockImageDownloader extends ImageDownloader {
        private Set<String> failWords = new HashSet<>();
        private boolean returnNoImages = false;
        
        public void setFailWords(Set<String> words) {
            this.failWords = words;
        }
        
        public void setReturnNoImages(boolean returnNoImages) {
            this.returnNoImages = returnNoImages;
        }
        
        @Override
        public void loadCredentials(String propertiesFilePath) throws IOException {
            // Mock - do nothing
            setApiKey("mock_key");
        }
        
        @Override
        public String[] searchImages(String query, int count) throws IOException {
            if (returnNoImages) {
                return new String[0];
            }
            
            if (failWords.contains(query)) {
                throw new IOException("Mock failure for word: " + query);
            }
            
            // Return mock image URLs
            String[] urls = new String[count];
            for (int i = 0; i < count; i++) {
                urls[i] = "https://mock.unsplash.com/" + query + "_" + i + ".jpg";
            }
            return urls;
        }
        
        @Override
        public boolean downloadImage(String imageUrl, String localPath) {
            if (imageUrl.contains("fail")) {
                return false;
            }
            
            // Mock download - create empty file
            try {
                Path path = Path.of(localPath);
                Files.createDirectories(path.getParent());
                Files.writeString(path, "mock image data");
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
