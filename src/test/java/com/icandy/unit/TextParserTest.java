package com.icandy.unit;

import com.icandy.build.TextParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TextParser class.
 */
class TextParserTest {
    
    private TextParser parser;
    
    @BeforeEach
    void setUp() throws IOException {
        parser = new TextParser();
        parser.loadStopWords("data/stopwords.txt");
    }
    
    @Test
    void testParseIntoWords() {
        String text = "Hello world! This is a test.";
        String[] words = parser.parseIntoWords(text);
        
        assertNotNull(words);
        assertTrue(words.length > 0);
        assertTrue(containsWord(words, "hello"));
        assertTrue(containsWord(words, "world"));
        assertTrue(containsWord(words, "test"));
    }
    
    @Test
    void testParseIntoPhrases() {
        String text = "Hello world! This is a test. How are you?";
        String[] phrases = parser.parseIntoPhrases(text);
        
        assertNotNull(phrases);
        assertEquals(3, phrases.length);
        assertEquals("Hello world", phrases[0]);
        assertEquals("This is a test", phrases[1]);
        assertEquals("How are you", phrases[2]);
    }
    
    @Test
    void testIsStopWord() {
        assertTrue(parser.isStopWord("the"));
        assertTrue(parser.isStopWord("a"));
        assertTrue(parser.isStopWord("is"));
        assertFalse(parser.isStopWord("hello"));
        assertFalse(parser.isStopWord("world"));
    }
    
    @Test
    void testFilterStopWords() {
        String[] words = {"hello", "the", "world", "is", "a", "test"};
        String[] filtered = parser.filterStopWords(words);
        
        assertNotNull(filtered);
        assertEquals(3, filtered.length);
        assertTrue(containsWord(filtered, "hello"));
        assertTrue(containsWord(filtered, "world"));
        assertTrue(containsWord(filtered, "test"));
        assertFalse(containsWord(filtered, "the"));
        assertFalse(containsWord(filtered, "a"));
        assertFalse(containsWord(filtered, "is"));
    }
    
    @Test
    void testMapPhrasesToWords() {
        String[] phrases = {"Hello world", "This is a test"};
        Map<Integer, String[]> mapping = parser.mapPhrasesToWords(phrases);
        
        assertNotNull(mapping);
        assertEquals(2, mapping.size());
        
        String[] phrase0Words = mapping.get(0);
        assertEquals(2, phrase0Words.length);
        assertTrue(containsWord(phrase0Words, "hello"));
        assertTrue(containsWord(phrase0Words, "world"));
        
        String[] phrase1Words = mapping.get(1);
        assertEquals(1, phrase1Words.length);
        assertTrue(containsWord(phrase1Words, "test"));
        assertFalse(containsWord(phrase1Words, "is"));
        assertFalse(containsWord(phrase1Words, "a"));
    }
    
    @Test
    void testEmptyInput() {
        String[] words = parser.parseIntoWords("");
        assertEquals(0, words.length);
        
        String[] phrases = parser.parseIntoPhrases("");
        assertEquals(0, phrases.length);
    }
    
    @Test
    void testNullInput() {
        String[] words = parser.parseIntoWords(null);
        assertEquals(0, words.length);
        
        String[] phrases = parser.parseIntoPhrases(null);
        assertEquals(0, phrases.length);
    }
    
    @Test
    void testMinimumWordLength() {
        // Words with length < 3 should be treated as stop words
        assertTrue(parser.isStopWord("a"));
        assertTrue(parser.isStopWord("I"));
        assertTrue(parser.isStopWord("is"));
        assertTrue(parser.isStopWord("at"));
        assertTrue(parser.isStopWord("to"));
        assertTrue(parser.isStopWord("be"));
        assertTrue(parser.isStopWord("or"));
        
        // Words with length >= 3 should not be automatically filtered
        assertFalse(parser.isStopWord("cat"));
        assertFalse(parser.isStopWord("dog"));
        assertFalse(parser.isStopWord("run"));
        assertFalse(parser.isStopWord("sun"));
    }
    
    @Test
    void testFilterShortWords() {
        String[] words = {"I", "am", "a", "cat", "in", "the", "sun"};
        String[] filtered = parser.filterStopWords(words);
        
        // Only "cat" and "sun" should remain (length >= 3 and not in stop words list)
        assertNotNull(filtered);
        assertEquals(2, filtered.length);
        assertTrue(containsWord(filtered, "cat"));
        assertTrue(containsWord(filtered, "sun"));
        
        // Short words should be filtered out
        assertFalse(containsWord(filtered, "I"));
        assertFalse(containsWord(filtered, "am"));
        assertFalse(containsWord(filtered, "a"));
        assertFalse(containsWord(filtered, "in"));
    }
    
    private boolean containsWord(String[] words, String target) {
        for (String word : words) {
            if (word.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }
}
