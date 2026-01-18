package com.icandy.unit;

import com.icandy.build.AssociationManager;
import com.icandy.run.ImageDisplayManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for verifying image swapping functionality.
 */
public class ImageSwapTest {
    
    private ImageDisplayManager imageDisplayManager;
    private AssociationManager associationManager;
    private TestPApplet testApplet;
    
    /**
     * Mock PApplet for testing
     */
    private static class TestPApplet extends PApplet {
        public TestPApplet() {
            super();
            this.width = 1280;
            this.height = 720;
        }
    }
    
    @BeforeEach
    public void setUp() {
        testApplet = new TestPApplet();
        associationManager = new AssociationManager();
        imageDisplayManager = new ImageDisplayManager(testApplet, associationManager);
        imageDisplayManager.setSimultaneousImageCount(3);
    }
    
    @Test
    public void testSwapImagesWithMultipleWords() {
        // Test that swapImages() method exists and can be called
        // This verifies the fix is in place
        assertDoesNotThrow(() -> {
            // Set up some test words
            String[] testWords = {"mary", "lamb", "school"};
            
            // This should not throw an exception even with no images loaded
            imageDisplayManager.setImagesForPhrase(testWords);
            imageDisplayManager.swapImages();
        });
    }
    
    @Test
    public void testSwapImagesWithEmptySlots() {
        // Test that swapImages() handles empty display slots gracefully
        assertDoesNotThrow(() -> {
            imageDisplayManager.swapImages();
        });
    }
    
    @Test
    public void testImageDisplayManagerInitialization() {
        // Verify the ImageDisplayManager is properly initialized
        assertNotNull(imageDisplayManager);
        
        // Test setting simultaneous image count
        imageDisplayManager.setSimultaneousImageCount(5);
        // Should not throw exception
    }
}