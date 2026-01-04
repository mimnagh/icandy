package com.icandy.unit;

import com.icandy.run.PhraseSequencer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PhraseSequencer class.
 * Tests forward navigation, backward navigation, boundary conditions, and looping behavior.
 */
class PhraseSequencerTest {
    
    private String[] phrases;
    private Map<Integer, String[]> phraseToWords;
    private PhraseSequencer sequencer;
    
    @BeforeEach
    void setUp() {
        // Create sample phrases and word mappings
        phrases = new String[]{
            "Hello world",
            "This is a test",
            "The sun is shining"
        };
        
        phraseToWords = new HashMap<>();
        phraseToWords.put(0, new String[]{"hello", "world"});
        phraseToWords.put(1, new String[]{"test"});
        phraseToWords.put(2, new String[]{"sun", "shining"});
        
        sequencer = new PhraseSequencer(phrases, phraseToWords);
    }
    
    @Test
    void testConstructorWithNullPhrases() {
        assertThrows(IllegalArgumentException.class, 
            () -> new PhraseSequencer(null, phraseToWords));
    }
    
    @Test
    void testConstructorWithEmptyPhrases() {
        assertThrows(IllegalArgumentException.class, 
            () -> new PhraseSequencer(new String[0], phraseToWords));
    }
    
    @Test
    void testConstructorWithNullPhraseToWords() {
        assertThrows(IllegalArgumentException.class, 
            () -> new PhraseSequencer(phrases, null));
    }
    
    @Test
    void testInitialState() {
        assertEquals("Hello world", sequencer.getCurrentPhrase());
        assertEquals(0, sequencer.getCurrentIndex());
        assertArrayEquals(new String[]{"hello", "world"}, sequencer.getWordsInCurrentPhrase());
        assertTrue(sequencer.isLooping());
    }
    
    @Test
    void testForwardNavigation() {
        // Start at phrase 0
        assertEquals(0, sequencer.getCurrentIndex());
        
        // Advance to phrase 1
        sequencer.advance();
        assertEquals(1, sequencer.getCurrentIndex());
        assertEquals("This is a test", sequencer.getCurrentPhrase());
        assertArrayEquals(new String[]{"test"}, sequencer.getWordsInCurrentPhrase());
        
        // Advance to phrase 2
        sequencer.advance();
        assertEquals(2, sequencer.getCurrentIndex());
        assertEquals("The sun is shining", sequencer.getCurrentPhrase());
        assertArrayEquals(new String[]{"sun", "shining"}, sequencer.getWordsInCurrentPhrase());
    }
    
    @Test
    void testBackwardNavigation() {
        // Move to last phrase
        sequencer.advance();
        sequencer.advance();
        assertEquals(2, sequencer.getCurrentIndex());
        
        // Go back to phrase 1
        sequencer.goBack();
        assertEquals(1, sequencer.getCurrentIndex());
        assertEquals("This is a test", sequencer.getCurrentPhrase());
        
        // Go back to phrase 0
        sequencer.goBack();
        assertEquals(0, sequencer.getCurrentIndex());
        assertEquals("Hello world", sequencer.getCurrentPhrase());
    }
    
    @Test
    void testHasNext() {
        assertTrue(sequencer.hasNext());
        
        sequencer.advance();
        assertTrue(sequencer.hasNext());
        
        sequencer.advance();
        assertFalse(sequencer.hasNext());
    }
    
    @Test
    void testHasPrevious() {
        assertFalse(sequencer.hasPrevious());
        
        sequencer.advance();
        assertTrue(sequencer.hasPrevious());
        
        sequencer.advance();
        assertTrue(sequencer.hasPrevious());
    }
    
    @Test
    void testBoundaryAtBeginning() {
        // Try to go back from first phrase
        assertEquals(0, sequencer.getCurrentIndex());
        sequencer.goBack();
        assertEquals(0, sequencer.getCurrentIndex()); // Should stay at 0
    }
    
    @Test
    void testLoopingEnabled() {
        // Move to last phrase
        sequencer.advance();
        sequencer.advance();
        assertEquals(2, sequencer.getCurrentIndex());
        
        // Advance should loop back to first phrase
        sequencer.advance();
        assertEquals(0, sequencer.getCurrentIndex());
        assertEquals("Hello world", sequencer.getCurrentPhrase());
    }
    
    @Test
    void testLoopingDisabled() {
        sequencer.setLooping(false);
        assertFalse(sequencer.isLooping());
        
        // Move to last phrase
        sequencer.advance();
        sequencer.advance();
        assertEquals(2, sequencer.getCurrentIndex());
        
        // Advance should stay at last phrase
        sequencer.advance();
        assertEquals(2, sequencer.getCurrentIndex());
        assertEquals("The sun is shining", sequencer.getCurrentPhrase());
    }
    
    @Test
    void testReset() {
        // Move to middle phrase
        sequencer.advance();
        assertEquals(1, sequencer.getCurrentIndex());
        
        // Reset should go back to first phrase
        sequencer.reset();
        assertEquals(0, sequencer.getCurrentIndex());
        assertEquals("Hello world", sequencer.getCurrentPhrase());
    }
    
    @Test
    void testGetPhraseCount() {
        assertEquals(3, sequencer.getPhraseCount());
    }
    
    @Test
    void testGetWordsInCurrentPhraseWithMissingMapping() {
        // Create sequencer with missing mapping for phrase 1
        Map<Integer, String[]> incompleteMap = new HashMap<>();
        incompleteMap.put(0, new String[]{"hello", "world"});
        // No mapping for index 1
        incompleteMap.put(2, new String[]{"sun", "shining"});
        
        PhraseSequencer seq = new PhraseSequencer(phrases, incompleteMap);
        seq.advance(); // Move to phrase 1
        
        // Should return empty array for missing mapping
        assertArrayEquals(new String[0], seq.getWordsInCurrentPhrase());
    }
    
    @Test
    void testSetLooping() {
        assertTrue(sequencer.isLooping());
        
        sequencer.setLooping(false);
        assertFalse(sequencer.isLooping());
        
        sequencer.setLooping(true);
        assertTrue(sequencer.isLooping());
    }
}
