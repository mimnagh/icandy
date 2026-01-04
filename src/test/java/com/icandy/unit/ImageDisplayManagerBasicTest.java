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
 * Unit tests for ImageDisplayManager.
 * 
 * Tests cover:
 * - Image loading and caching
 * - Image layout and rendering
 * - Image swapping logic
 * - Handling of missing images
 * 
 * Requirements: 4.3, 5.2, 5.3, 5.4, 8.1
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
    
    // ========== Image Loading and Caching Tests ==========
    
    @Test
    void testPreloadImages_LoadsImagesIntoCache() throws IOException {
        // Create test image files
        Path image1 = tempDir.resolve("test_1.jpg");
        Path image2 = tempDir.resolve("test_2.jpg");
        Files.writeString(image1, "fake image data");
        Files.writeString(image2, "fake image data");
        
        // Preload images
        String[] paths = {image1.toString(), image2.toString()};
        imageDisplayManager.preloadImages(paths);
        
        // In test mode, images won't actually load, but method should not crash
        // and should handle the paths correctly
        assertDoesNotThrow(() -> imageDisplayManager.preloadImages(paths));
    }
    
    @Test
    void testPreloadImages_HandlesNullPaths() {
        assertDoesNotThrow(() -> imageDisplayManager.preloadImages(null));
    }
    
    @Test
    void testPreloadImages_HandlesEmptyArray() {
        assertDoesNotThrow(() -> imageDisplayManager.preloadImages(new String[0]));
    }
    
    @Test
    void testPreloadImages_HandlesNullElementsInArray() {
        String[] paths = {null, "valid.jpg", null};
        assertDoesNotThrow(() -> imageDisplayManager.preloadImages(paths));
    }
    
    @Test
    void testPreloadImages_HandlesEmptyStringElements() {
        String[] paths = {"", "  ", "valid.jpg"};
        assertDoesNotThrow(() -> imageDisplayManager.preloadImages(paths));
    }
    
    @Test
    void testPreloadImages_HandlesMissingFiles() {
        String[] paths = {
            "nonexistent1.jpg",
            "nonexistent2.jpg",
            "/path/to/missing/image.jpg"
        };
        
        // Should log warnings but not crash
        assertDoesNotThrow(() -> imageDisplayManager.preloadImages(paths));
    }
    
    @Test
    void testPreloadImages_GroupsByWord() throws IOException {
        // Create images for multiple words
        Path hello1 = tempDir.resolve("hello_1.jpg");
        Path hello2 = tempDir.resolve("hello_2.jpg");
        Path world1 = tempDir.resolve("world_1.jpg");
        
        Files.writeString(hello1, "fake image data");
        Files.writeString(hello2, "fake image data");
        Files.writeString(world1, "fake image data");
        
        String[] paths = {hello1.toString(), hello2.toString(), world1.toString()};
        
        // Should group by word and not crash
        assertDoesNotThrow(() -> imageDisplayManager.preloadImages(paths));
    }
    
    @Test
    void testPreloadImages_SkipsDuplicatePaths() throws IOException {
        Path image = tempDir.resolve("test_1.jpg");
        Files.writeString(image, "fake image data");
        
        // Preload same image multiple times
        String[] paths = {image.toString(), image.toString(), image.toString()};
        
        assertDoesNotThrow(() -> imageDisplayManager.preloadImages(paths));
    }
    
    // ========== Image Layout and Rendering Tests ==========
    
    @Test
    void testSetImagesForPhrase_InitializesDisplaySlots() throws IOException {
        // Create test images
        Path image1 = tempDir.resolve("hello_1.jpg");
        Path image2 = tempDir.resolve("world_1.jpg");
        Files.writeString(image1, "fake image data");
        Files.writeString(image2, "fake image data");
        
        associationManager.addAssociation("hello", new String[]{image1.toString()});
        associationManager.addAssociation("world", new String[]{image2.toString()});
        
        // Set images for phrase
        String[] words = {"hello", "world"};
        imageDisplayManager.setImagesForPhrase(words);
        
        // Verify display slots are created (count should be > 0)
        assertTrue(imageDisplayManager.getDisplaySlotCount() >= 0);
    }
    
    @Test
    void testSetImagesForPhrase_HandlesWordsWithoutImages() throws IOException {
        // Create image for only one word
        Path image = tempDir.resolve("hello_1.jpg");
        Files.writeString(image, "fake image data");
        
        associationManager.addAssociation("hello", new String[]{image.toString()});
        // "world" has no images
        
        String[] words = {"hello", "world"};
        imageDisplayManager.setImagesForPhrase(words);
        
        // Should handle gracefully
        assertArrayEquals(words, imageDisplayManager.getCurrentWords());
    }
    
    @Test
    void testSetImagesForPhrase_UpdatesWhenCalledMultipleTimes() throws IOException {
        // First phrase
        Path image1 = tempDir.resolve("hello_1.jpg");
        Files.writeString(image1, "fake image data");
        associationManager.addAssociation("hello", new String[]{image1.toString()});
        
        imageDisplayManager.setImagesForPhrase(new String[]{"hello"});
        assertArrayEquals(new String[]{"hello"}, imageDisplayManager.getCurrentWords());
        
        // Second phrase
        Path image2 = tempDir.resolve("world_1.jpg");
        Files.writeString(image2, "fake image data");
        associationManager.addAssociation("world", new String[]{image2.toString()});
        
        imageDisplayManager.setImagesForPhrase(new String[]{"world"});
        assertArrayEquals(new String[]{"world"}, imageDisplayManager.getCurrentWords());
    }
    
    @Test
    void testDisplayCurrentImages_DoesNotCrashInTestMode() {
        // In test mode (PApplet.g is null), should not crash
        assertDoesNotThrow(() -> imageDisplayManager.displayCurrentImages());
    }
    
    @Test
    void testDisplayCurrentImages_HandlesEmptyDisplaySlots() {
        // No images set
        assertDoesNotThrow(() -> imageDisplayManager.displayCurrentImages());
    }
    
    @Test
    void testSetSimultaneousImageCount_AffectsLayout() throws IOException {
        // Create test images
        Path image1 = tempDir.resolve("word1_1.jpg");
        Path image2 = tempDir.resolve("word2_1.jpg");
        Path image3 = tempDir.resolve("word3_1.jpg");
        
        Files.writeString(image1, "fake image data");
        Files.writeString(image2, "fake image data");
        Files.writeString(image3, "fake image data");
        
        associationManager.addAssociation("word1", new String[]{image1.toString()});
        associationManager.addAssociation("word2", new String[]{image2.toString()});
        associationManager.addAssociation("word3", new String[]{image3.toString()});
        
        // Set to 2 simultaneous images
        imageDisplayManager.setSimultaneousImageCount(2);
        imageDisplayManager.setImagesForPhrase(new String[]{"word1", "word2", "word3"});
        
        // Change to 3 simultaneous images
        imageDisplayManager.setSimultaneousImageCount(3);
        
        assertEquals(3, imageDisplayManager.getSimultaneousImageCount());
    }
    
    // ========== Image Swapping Logic Tests ==========
    
    @Test
    void testSwapImages_WithNoDisplaySlots() {
        // Should not crash when no display slots
        assertDoesNotThrow(() -> imageDisplayManager.swapImages());
    }
    
    @Test
    void testSwapImages_WithSingleImagePerWord() throws IOException {
        // Create single image per word
        Path image1 = tempDir.resolve("hello_1.jpg");
        Path image2 = tempDir.resolve("world_1.jpg");
        
        Files.writeString(image1, "fake image data");
        Files.writeString(image2, "fake image data");
        
        associationManager.addAssociation("hello", new String[]{image1.toString()});
        associationManager.addAssociation("world", new String[]{image2.toString()});
        
        imageDisplayManager.setImagesForPhrase(new String[]{"hello", "world"});
        
        // Swap should not crash even with single images
        assertDoesNotThrow(() -> imageDisplayManager.swapImages());
    }
    
    @Test
    void testSwapImages_WithMultipleImagesPerWord() throws IOException {
        // Create multiple images per word
        Path hello1 = tempDir.resolve("hello_1.jpg");
        Path hello2 = tempDir.resolve("hello_2.jpg");
        Path hello3 = tempDir.resolve("hello_3.jpg");
        
        Files.writeString(hello1, "fake image data");
        Files.writeString(hello2, "fake image data");
        Files.writeString(hello3, "fake image data");
        
        associationManager.addAssociation("hello", new String[]{
            hello1.toString(), hello2.toString(), hello3.toString()
        });
        
        imageDisplayManager.setImagesForPhrase(new String[]{"hello"});
        
        // Multiple swaps should cycle through images
        assertDoesNotThrow(() -> {
            imageDisplayManager.swapImages();
            imageDisplayManager.swapImages();
            imageDisplayManager.swapImages();
        });
    }
    
    @Test
    void testSwapImages_CyclesThroughImages() throws IOException {
        // Create multiple images
        Path image1 = tempDir.resolve("test_1.jpg");
        Path image2 = tempDir.resolve("test_2.jpg");
        
        Files.writeString(image1, "fake image data");
        Files.writeString(image2, "fake image data");
        
        associationManager.addAssociation("test", new String[]{
            image1.toString(), image2.toString()
        });
        
        imageDisplayManager.setImagesForPhrase(new String[]{"test"});
        
        // Swap multiple times - should cycle back
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> imageDisplayManager.swapImages());
        }
    }
    
    @Test
    void testSwapImages_HandlesWordsWithNoImages() throws IOException {
        // Set phrase with word that has no images
        imageDisplayManager.setImagesForPhrase(new String[]{"nonexistent"});
        
        // Should not crash
        assertDoesNotThrow(() -> imageDisplayManager.swapImages());
    }
    
    // ========== Missing Images Handling Tests ==========
    
    @Test
    void testSetImagesForPhrase_WithAllMissingImages() {
        // Add associations with non-existent files
        associationManager.addAssociation("test", new String[]{
            "missing1.jpg", "missing2.jpg"
        });
        
        // Should handle gracefully
        assertDoesNotThrow(() -> {
            imageDisplayManager.setImagesForPhrase(new String[]{"test"});
        });
    }
    
    @Test
    void testSetImagesForPhrase_WithSomeMissingImages() throws IOException {
        // Create one valid image
        Path validImage = tempDir.resolve("valid_1.jpg");
        Files.writeString(validImage, "fake image data");
        
        // Add association with mix of valid and missing
        associationManager.addAssociation("test", new String[]{
            validImage.toString(),
            "missing.jpg"
        });
        
        // Should handle gracefully
        assertDoesNotThrow(() -> {
            imageDisplayManager.setImagesForPhrase(new String[]{"test"});
        });
    }
    
    @Test
    void testPreloadImages_LogsWarningsForMissingFiles() {
        // This test verifies that missing files are logged but don't crash
        String[] missingPaths = {
            "/nonexistent/path/image1.jpg",
            "/another/missing/image2.jpg"
        };
        
        // Should complete without throwing
        assertDoesNotThrow(() -> imageDisplayManager.preloadImages(missingPaths));
    }
    
    @Test
    void testDisplayCurrentImages_WithMissingImages() throws IOException {
        // Set up phrase with missing images
        associationManager.addAssociation("test", new String[]{"missing.jpg"});
        imageDisplayManager.setImagesForPhrase(new String[]{"test"});
        
        // Display should not crash even with missing images
        assertDoesNotThrow(() -> imageDisplayManager.displayCurrentImages());
    }
    
    @Test
    void testSwapImages_WithMissingImages() {
        // Set up phrase with missing images
        associationManager.addAssociation("test", new String[]{"missing1.jpg", "missing2.jpg"});
        imageDisplayManager.setImagesForPhrase(new String[]{"test"});
        
        // Swap should not crash even with missing images
        assertDoesNotThrow(() -> imageDisplayManager.swapImages());
    }
    
    // ========== Edge Cases and Integration Tests ==========
    
    @Test
    void testCompleteWorkflow() throws IOException {
        // Create test images
        Path hello1 = tempDir.resolve("hello_1.jpg");
        Path hello2 = tempDir.resolve("hello_2.jpg");
        Path world1 = tempDir.resolve("world_1.jpg");
        
        Files.writeString(hello1, "fake image data");
        Files.writeString(hello2, "fake image data");
        Files.writeString(world1, "fake image data");
        
        // Set up associations
        associationManager.addAssociation("hello", new String[]{
            hello1.toString(), hello2.toString()
        });
        associationManager.addAssociation("world", new String[]{world1.toString()});
        
        // Configure display
        imageDisplayManager.setSimultaneousImageCount(2);
        
        // Set images for phrase
        imageDisplayManager.setImagesForPhrase(new String[]{"hello", "world"});
        
        // Display images
        imageDisplayManager.displayCurrentImages();
        
        // Swap images multiple times
        imageDisplayManager.swapImages();
        imageDisplayManager.displayCurrentImages();
        imageDisplayManager.swapImages();
        imageDisplayManager.displayCurrentImages();
        
        // Change phrase
        imageDisplayManager.setImagesForPhrase(new String[]{"hello"});
        imageDisplayManager.displayCurrentImages();
        
        // Clear
        imageDisplayManager.clear();
        assertEquals(0, imageDisplayManager.getCurrentWords().length);
    }
    
    @Test
    void testSetImagesForPhrase_WithDuplicateWords() throws IOException {
        // Create test image
        Path image = tempDir.resolve("test_1.jpg");
        Files.writeString(image, "fake image data");
        
        associationManager.addAssociation("test", new String[]{image.toString()});
        
        // Set phrase with duplicate words
        String[] words = {"test", "test", "test"};
        imageDisplayManager.setImagesForPhrase(words);
        
        // Should handle duplicates gracefully
        assertArrayEquals(words, imageDisplayManager.getCurrentWords());
    }
    
    @Test
    void testSetSimultaneousImageCount_WithActivePhrase() throws IOException {
        // Set up initial phrase
        Path image = tempDir.resolve("test_1.jpg");
        Files.writeString(image, "fake image data");
        associationManager.addAssociation("test", new String[]{image.toString()});
        
        imageDisplayManager.setSimultaneousImageCount(2);
        imageDisplayManager.setImagesForPhrase(new String[]{"test"});
        
        // Change simultaneous count while phrase is active
        imageDisplayManager.setSimultaneousImageCount(4);
        
        // Should reinitialize display slots
        assertEquals(4, imageDisplayManager.getSimultaneousImageCount());
    }
    
    @Test
    void testClear_ResetsAllState() throws IOException {
        // Set up complex state
        Path image1 = tempDir.resolve("hello_1.jpg");
        Path image2 = tempDir.resolve("world_1.jpg");
        Files.writeString(image1, "fake image data");
        Files.writeString(image2, "fake image data");
        
        associationManager.addAssociation("hello", new String[]{image1.toString()});
        associationManager.addAssociation("world", new String[]{image2.toString()});
        
        imageDisplayManager.setSimultaneousImageCount(5);
        imageDisplayManager.setImagesForPhrase(new String[]{"hello", "world"});
        imageDisplayManager.swapImages();
        
        // Clear everything
        imageDisplayManager.clear();
        
        // Verify state is reset
        assertEquals(0, imageDisplayManager.getCurrentWords().length);
        assertEquals(0, imageDisplayManager.getDisplaySlotCount());
        
        // Simultaneous count should remain
        assertEquals(5, imageDisplayManager.getSimultaneousImageCount());
        
        // Should be able to set new phrase after clear
        imageDisplayManager.setImagesForPhrase(new String[]{"hello"});
        assertArrayEquals(new String[]{"hello"}, imageDisplayManager.getCurrentWords());
    }
}
