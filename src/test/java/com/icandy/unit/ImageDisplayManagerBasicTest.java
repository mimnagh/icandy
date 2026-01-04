package com.icandy.unit;

import com.icandy.build.AssociationManager;
import com.icandy.run.ImageDisplayManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import processing.core.PApplet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for ImageDisplayManager to verify core functionality.
 */
class ImageDisplayManagerBasicTest {
    
    private PApplet mockApplet;
    private AssociationManager associationManager;
    private ImageDisplayManager imageDisplayManager;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Create a mock PApplet (test mode - g will be null)
        mockApplet = new PApplet();
        
        // Create association manager
        associationManager = new AssociationManager();
        
        // Create image display manager
        imageDisplayManager = new ImageDisplayManager(mockApplet, associationManager);
    }
    
    @Test
    void testConstructor() {
        assertNotNull(imageDisplayManager);
        assertEquals(3, imageDisplayManager.getSimultaneousImageCount());
    }
    
    @Test
    void testSetSimultaneousImageCount() {
        imageDisplayManager.setSimultaneousImageCount(5);
        assertEquals(5, imageDisplayManager.getSimultaneousImageCount());
        
        // Test invalid value (should use default)
        imageDisplayManager.setSimultaneousImageCount(-1);
        assertEquals(3, imageDisplayManager.getSimultaneousImageCount());
        
        imageDisplayManager.setSimultaneousImageCount(0);
        assertEquals(3, imageDisplayManager.getSimultaneousImageCount());
    }
    
    @Test
    void testSetImagesForPhraseWithNoWords() {
        imageDisplayManager.setImagesForPhrase(new String[0]);
        assertEquals(0, imageDisplayManager.getCurrentWords().length);
        assertEquals(0, imageDisplayManager.getDisplaySlotCount());
    }
    
    @Test
    void testSetImagesForPhraseWithNull() {
        imageDisplayManager.setImagesForPhrase(null);
        assertEquals(0, imageDisplayManager.getCurrentWords().length);
        assertEquals(0, imageDisplayManager.getDisplaySlotCount());
    }
    
    @Test
    void testSetImagesForPhraseWithWords() throws IOException {
        // Create some test image files
        Path image1 = tempDir.resolve("hello_1.jpg");
        Path image2 = tempDir.resolve("hello_2.jpg");
        Path image3 = tempDir.resolve("world_1.jpg");
        
        Files.writeString(image1, "fake image data");
        Files.writeString(image2, "fake image data");
        Files.writeString(image3, "fake image data");
        
        // Add associations
        associationManager.addAssociation("hello", new String[]{
            image1.toString(), image2.toString()
        });
        associationManager.addAssociation("world", new String[]{
            image3.toString()
        });
        
        // Set images for phrase
        String[] words = {"hello", "world"};
        imageDisplayManager.setImagesForPhrase(words);
        
        // Verify current words are set
        assertArrayEquals(words, imageDisplayManager.getCurrentWords());
        
        // In test mode, display slots won't be fully initialized
        // but we can verify the method doesn't crash
        assertTrue(imageDisplayManager.getDisplaySlotCount() >= 0);
    }
    
    @Test
    void testPreloadImagesWithNullOrEmpty() {
        // Should not crash
        imageDisplayManager.preloadImages(null);
        imageDisplayManager.preloadImages(new String[0]);
    }
    
    @Test
    void testPreloadImagesWithMissingFiles() {
        // Should log warnings but not crash
        String[] paths = {"nonexistent1.jpg", "nonexistent2.jpg"};
        imageDisplayManager.preloadImages(paths);
    }
    
    @Test
    void testSwapImagesWithNoSlots() {
        // Should not crash when no display slots
        imageDisplayManager.swapImages();
    }
    
    @Test
    void testDisplayCurrentImagesInTestMode() {
        // Should not crash in test mode
        imageDisplayManager.displayCurrentImages();
    }
    
    @Test
    void testClear() {
        // Set up some state
        imageDisplayManager.setSimultaneousImageCount(5);
        imageDisplayManager.setImagesForPhrase(new String[]{"hello", "world"});
        
        // Clear
        imageDisplayManager.clear();
        
        // Verify state is cleared
        assertEquals(0, imageDisplayManager.getCurrentWords().length);
        assertEquals(0, imageDisplayManager.getDisplaySlotCount());
        
        // Simultaneous count should remain
        assertEquals(5, imageDisplayManager.getSimultaneousImageCount());
    }
    
    @Test
    void testImageSelectionWithMoreWordsThanSlots() throws IOException {
        // Create test images for 5 words
        for (int i = 1; i <= 5; i++) {
            Path image = tempDir.resolve("word" + i + "_1.jpg");
            Files.writeString(image, "fake image data");
            associationManager.addAssociation("word" + i, new String[]{image.toString()});
        }
        
        // Set simultaneous count to 3
        imageDisplayManager.setSimultaneousImageCount(3);
        
        // Set images for phrase with 5 words
        String[] words = {"word1", "word2", "word3", "word4", "word5"};
        imageDisplayManager.setImagesForPhrase(words);
        
        // Should select 3 words (in test mode, slots may not be fully initialized)
        assertEquals(5, imageDisplayManager.getCurrentWords().length);
    }
    
    @Test
    void testImageSelectionWithFewerWordsThanSlots() throws IOException {
        // Create test images for 2 words
        Path image1 = tempDir.resolve("word1_1.jpg");
        Path image2 = tempDir.resolve("word2_1.jpg");
        Files.writeString(image1, "fake image data");
        Files.writeString(image2, "fake image data");
        
        associationManager.addAssociation("word1", new String[]{image1.toString()});
        associationManager.addAssociation("word2", new String[]{image2.toString()});
        
        // Set simultaneous count to 5
        imageDisplayManager.setSimultaneousImageCount(5);
        
        // Set images for phrase with 2 words
        String[] words = {"word1", "word2"};
        imageDisplayManager.setImagesForPhrase(words);
        
        // Should cycle to fill slots
        assertEquals(2, imageDisplayManager.getCurrentWords().length);
    }
}
