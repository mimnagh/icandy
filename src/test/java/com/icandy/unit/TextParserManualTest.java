package com.icandy.unit;

import com.icandy.build.TextParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * Manual test for TextParser to verify functionality without JUnit.
 */
public class TextParserManualTest {
    
    public static void main(String[] args) {
        try {
            TextParser parser = new TextParser();
            parser.loadStopWords("data/stopwords.txt");
            
            System.out.println("=== Testing TextParser ===\n");
            
            // Test 1: Parse into words
            System.out.println("Test 1: Parse into words");
            String text = "Hello world! This is a test.";
            String[] words = parser.parseIntoWords(text);
            System.out.println("Input: " + text);
            System.out.println("Words: " + Arrays.toString(words));
            System.out.println();
            
            // Test 2: Parse into phrases
            System.out.println("Test 2: Parse into phrases");
            String text2 = "Hello world! This is a test. How are you?";
            String[] phrases = parser.parseIntoPhrases(text2);
            System.out.println("Input: " + text2);
            System.out.println("Phrases: " + Arrays.toString(phrases));
            System.out.println();
            
            // Test 3: Check stop words
            System.out.println("Test 3: Check stop words");
            System.out.println("'the' is stop word: " + parser.isStopWord("the"));
            System.out.println("'hello' is stop word: " + parser.isStopWord("hello"));
            System.out.println();
            
            // Test 4: Filter stop words
            System.out.println("Test 4: Filter stop words");
            String[] testWords = {"hello", "the", "world", "is", "a", "test"};
            String[] filtered = parser.filterStopWords(testWords);
            System.out.println("Input: " + Arrays.toString(testWords));
            System.out.println("Filtered: " + Arrays.toString(filtered));
            System.out.println();
            
            // Test 5: Map phrases to words
            System.out.println("Test 5: Map phrases to words");
            String[] testPhrases = {"Hello world", "This is a test"};
            Map<Integer, String[]> mapping = parser.mapPhrasesToWords(testPhrases);
            System.out.println("Phrases: " + Arrays.toString(testPhrases));
            for (Map.Entry<Integer, String[]> entry : mapping.entrySet()) {
                System.out.println("Phrase " + entry.getKey() + " -> " + Arrays.toString(entry.getValue()));
            }
            System.out.println();
            
            System.out.println("=== All tests completed successfully! ===");
            
        } catch (IOException e) {
            System.err.println("Error loading stop words: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
