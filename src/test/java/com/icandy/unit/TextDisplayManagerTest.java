package com.icandy.unit;

import com.icandy.common.ConfigurationManager;
import com.icandy.run.TextDisplayManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TextDisplayManager.
 * 
 * Tests phrase rendering, duration calculation, and text positioning.
 * Requirements: 4.2, 4.6
 */
class TextDisplayManagerTest {
    
    private PApplet mockApplet;
    private ConfigurationManager config;
    private TextDisplayManager manager;
    
    @BeforeEach
    void setUp() {
        // Create a minimal PApplet for testing
        mockApplet = new PApplet();
        mockApplet.g = new processing.core.PGraphics();
        
        // Create configuration with default values
        config = new ConfigurationManager();
        
        // Create the manager
        manager = new TextDisplayManager(mockApplet, config);
    }
    
    @Test
    void testCalculateDisplayDuration_shortPhrase() {
        // Short phrase: "Hello world" (2 words)
        // Formula: (2 * 300) + 1000 = 1600ms
        // Min: 2000ms, so should return 2000ms
        String phrase = "Hello world";
        int duration = manager.calculateDisplayDuration(phrase);
        
        assertEquals(2000, duration, "Short phrase should use minimum duration");
    }
    
    @Test
    void testCalculateDisplayDuration_mediumPhrase() {
        // Medium phrase: 10 words
        // Formula: (10 * 300) + 1000 = 4000ms
        // Within bounds [2000, 10000], so should return 4000ms
        String phrase = "This is a medium length phrase with exactly ten words";
        int duration = manager.calculateDisplayDuration(phrase);
        
        assertEquals(4000, duration, "Medium phrase should use calculated duration");
    }
    
    @Test
    void testCalculateDisplayDuration_longPhrase() {
        // Long phrase: 40 words
        // Formula: (40 * 300) + 1000 = 13000ms
        // Max: 10000ms, so should return 10000ms
        StringBuilder longPhrase = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            longPhrase.append("word ");
        }
        
        int duration = manager.calculateDisplayDuration(longPhrase.toString().trim());
        
        assertEquals(10000, duration, "Long phrase should use maximum duration");
    }
    
    @Test
    void testCalculateDisplayDuration_emptyPhrase() {
        String phrase = "";
        int duration = manager.calculateDisplayDuration(phrase);
        
        assertEquals(2000, duration, "Empty phrase should use minimum duration");
    }
    
    @Test
    void testCalculateDisplayDuration_nullPhrase() {
        int duration = manager.calculateDisplayDuration(null);
        
        assertEquals(2000, duration, "Null phrase should use minimum duration");
    }
    
    @Test
    void testCalculateDisplayDuration_whitespaceOnly() {
        String phrase = "   \t\n  ";
        int duration = manager.calculateDisplayDuration(phrase);
        
        assertEquals(2000, duration, "Whitespace-only phrase should use minimum duration");
    }
    
    @Test
    void testUpdatePhrase() {
        String phrase = "Test phrase";
        manager.updatePhrase(phrase);
        
        assertEquals(phrase, manager.getCurrentPhrase(), "Current phrase should be updated");
        assertTrue(manager.getCurrentDuration() > 0, "Duration should be calculated");
    }
    
    @Test
    void testShouldAdvance_notEnoughTimeElapsed() {
        manager.updatePhrase("Test phrase");
        
        // Immediately check - should not advance yet
        assertFalse(manager.shouldAdvance(), "Should not advance immediately after update");
    }
    
    @Test
    void testShouldAdvance_enoughTimeElapsed() throws InterruptedException {
        // Create a very short phrase that will have minimum duration
        manager.updatePhrase("Hi");
        
        // Wait for the duration to elapse (plus a small buffer)
        Thread.sleep(manager.getCurrentDuration() + 100);
        
        assertTrue(manager.shouldAdvance(), "Should advance after duration elapsed");
    }
    
    @Test
    void testShouldAdvance_emptyPhrase() {
        manager.updatePhrase("");
        
        assertTrue(manager.shouldAdvance(), "Empty phrase should advance immediately");
    }
    
    @Test
    void testResetTimer() throws InterruptedException {
        manager.updatePhrase("Test phrase");
        
        // Wait a bit
        Thread.sleep(100);
        
        // Reset timer
        manager.resetTimer();
        
        // Should not advance immediately after reset
        assertFalse(manager.shouldAdvance(), "Should not advance immediately after timer reset");
    }
    
    @Test
    void testDisplayPhrase_nullPhrase() {
        // Should not throw exception
        assertDoesNotThrow(() -> manager.displayPhrase(null, 400, 300));
    }
    
    @Test
    void testDisplayPhrase_emptyPhrase() {
        // Should not throw exception
        assertDoesNotThrow(() -> manager.displayPhrase("", 400, 300));
    }
    
    @Test
    void testCalculateDisplayDuration_withCustomConfig() throws Exception {
        // Create a custom configuration
        ConfigurationManager customConfig = new ConfigurationManager();
        
        // Note: We can't easily modify config values without a config file,
        // so this test verifies the default behavior
        TextDisplayManager customManager = new TextDisplayManager(mockApplet, customConfig);
        
        // 5 words: (5 * 300) + 1000 = 2500ms
        String phrase = "One two three four five";
        int duration = customManager.calculateDisplayDuration(phrase);
        
        assertEquals(2500, duration, "Duration should follow formula with default config");
    }
    
    @Test
    void testDisplayPhrase_longPhraseWrapping() {
        // Create a very long phrase that would need wrapping
        String longPhrase = "This is a very long phrase that contains many words and should be wrapped across multiple lines when displayed on screen to ensure readability";
        
        // Should not throw exception even with long text
        assertDoesNotThrow(() -> manager.displayPhrase(longPhrase, 400, 300));
    }
    
    @Test
    void testDisplayPhrase_shortPhrase() {
        // Short phrase should display on single line
        String shortPhrase = "Hello world";
        
        // Should not throw exception
        assertDoesNotThrow(() -> manager.displayPhrase(shortPhrase, 400, 300));
    }
    
    @Test
    void testDisplayPhrase_singleWord() {
        // Single word should display correctly
        String singleWord = "Hello";
        
        // Should not throw exception
        assertDoesNotThrow(() -> manager.displayPhrase(singleWord, 400, 300));
    }
    
    @Test
    void testDisplayPhrase_multipleSpaces() {
        // Phrase with multiple spaces should be handled correctly
        String phrase = "Hello    world    with    spaces";
        
        // Should not throw exception
        assertDoesNotThrow(() -> manager.displayPhrase(phrase, 400, 300));
    }
}
