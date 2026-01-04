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
 * Tests for incremental build functionality in BuildOrchestrator.
 * Verifies that existing associations are preserved and only new words are processed.
 */
class BuildOrchestratorIncrementalTest {
    
    @TempDir
    Path tempDir;
    
    private BuildOrchestrator orchestrator;
    private MockImageDownloader mockDownloader;
    private Path configFile;
    private Path stopWordsFile;
    private Path unsplashPropsFile;
    private Path associationsFile;
    
    @BeforeEach
    void setUp() throws IOException {
        // Create mock downloader that tracks which words were processed
        mockDownloader = new MockImageDownloader();
        
        // Create orchestrator with mock downloader
        TextParser textParser = new TextParser();
        AssociationManager associationManager = new AssociationManager();
        orchestrator = new BuildOrchestrator(textParser, mockDownloader, associationManager);
        
        // Create configuration files
        stopWordsFile = tempDir.resolve("stopwords.txt");
        Files.writeString(stopWordsFile, "a\nthe\nis\n");
        
        unsplashPropsFile = tempDir.resolve("unsplash.properties");
        Files.writeString(unsplashPropsFile, 
            "application_id=test_app_id\n" +
            "secret_key=test_secret\n" +
            "access_key=test_access\n");
        
        associationsFile = tempDir.resolve("associations.json");
        
        configFile = tempDir.resolve("config.json");
        String configJson = String.format("""
            {
              "build": {
                "imagesPerWord": 3,
                "unsplashPropertiesFile": "%s",
                "imageStorageDir": "%s",
                "associationsFile": "%s",
                "stopWordsFile": "%s",
                "maxRetries": 3
              }
            }
            """, 
            unsplashPropsFile.toString(),
            tempDir.resolve("images").toString(),
            associationsFile.toString(),
            stopWordsFile.toString());
        Files.writeString(configFile, configJson);
        
        orchestrator.loadConfiguration(configFile.toString());
    }
    
    @Test
    void testIncrementalBuild_SkipsExistingWords() throws IOException {
        // First build: process "hello world"
        Path textFile1 = tempDir.resolve("text1.txt");
        Files.writeString(textFile1, "Hello world.");
        
        orchestrator.runBuild(textFile1.toString());
        
        // Verify both words were processed
        assertTrue(mockDownloader.processedWords.contains("hello"));
        assertTrue(mockDownloader.processedWords.contains("world"));
        assertEquals(2, mockDownloader.processedWords.size());
        
        // Reset mock downloader
        mockDownloader.processedWords.clear();
        
        // Second build: process "hello world test"
        // Should only download images for "test" since "hello" and "world" already exist
        Path textFile2 = tempDir.resolve("text2.txt");
        Files.writeString(textFile2, "Hello world test.");
        
        orchestrator.runBuild(textFile2.toString());
        
        // Verify only "test" was processed
        assertFalse(mockDownloader.processedWords.contains("hello"), 
            "hello should be skipped (already has images)");
        assertFalse(mockDownloader.processedWords.contains("world"), 
            "world should be skipped (already has images)");
        assertTrue(mockDownloader.processedWords.contains("test"), 
            "test should be processed (new word)");
        assertEquals(1, mockDownloader.processedWords.size());
    }
    
    @Test
    void testIncrementalBuild_PreservesExistingAssociations() throws IOException {
        // First build
        Path textFile1 = tempDir.resolve("text1.txt");
        Files.writeString(textFile1, "Hello world.");
        
        orchestrator.runBuild(textFile1.toString());
        
        // Load associations and verify
        AssociationManager manager = new AssociationManager();
        manager.loadFromFile(associationsFile.toString());
        
        assertEquals(2, manager.getWordCount());
        assertEquals(6, manager.getImageCount()); // 2 words × 3 images
        
        // Second build with new word
        Path textFile2 = tempDir.resolve("text2.txt");
        Files.writeString(textFile2, "Hello world test.");
        
        orchestrator.runBuild(textFile2.toString());
        
        // Load associations again and verify all words are present
        manager.loadFromFile(associationsFile.toString());
        
        assertEquals(3, manager.getWordCount());
        assertEquals(9, manager.getImageCount()); // 3 words × 3 images
        
        // Verify all words have associations
        assertTrue(manager.hasWord("hello"));
        assertTrue(manager.hasWord("world"));
        assertTrue(manager.hasWord("test"));
    }
    
    @Test
    void testIncrementalBuild_NoExistingAssociations() throws IOException {
        // Build without existing associations file
        Path textFile = tempDir.resolve("text.txt");
        Files.writeString(textFile, "Hello world.");
        
        orchestrator.runBuild(textFile.toString());
        
        // Verify both words were processed
        assertTrue(mockDownloader.processedWords.contains("hello"));
        assertTrue(mockDownloader.processedWords.contains("world"));
        assertEquals(2, mockDownloader.processedWords.size());
    }
    
    @Test
    void testIncrementalBuild_AllWordsAlreadyExist() throws IOException {
        // First build
        Path textFile1 = tempDir.resolve("text1.txt");
        Files.writeString(textFile1, "Hello world test.");
        
        orchestrator.runBuild(textFile1.toString());
        
        // Reset mock downloader
        mockDownloader.processedWords.clear();
        
        // Second build with same words
        Path textFile2 = tempDir.resolve("text2.txt");
        Files.writeString(textFile2, "Hello world test.");
        
        orchestrator.runBuild(textFile2.toString());
        
        // Verify no words were processed
        assertEquals(0, mockDownloader.processedWords.size(), 
            "No words should be processed when all already have sufficient images");
    }
    
    /**
     * Mock ImageDownloader that tracks which words were processed.
     */
    private static class MockImageDownloader extends ImageDownloader {
        Set<String> processedWords = new HashSet<>();
        
        @Override
        public String[] searchImages(String query, int count) throws IOException {
            processedWords.add(query.toLowerCase());
            
            // Return mock URLs
            String[] urls = new String[count];
            for (int i = 0; i < count; i++) {
                urls[i] = "https://example.com/" + query + "_" + i + ".jpg";
            }
            return urls;
        }
        
        @Override
        public boolean downloadImage(String imageUrl, String localPath) {
            // Create empty file to simulate download
            Path path = Path.of(localPath);
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, "mock image data");
            } catch (IOException e) {
                return false;
            }
            return true;
        }
    }
}
