package com.icandy.build;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TextParser is responsible for reading and parsing text script files.
 * It splits text into phrases (sentences) and individual words, filters stop words,
 * and creates mappings between phrases and their content words.
 */
public class TextParser {
    
    private Set<String> stopWords;
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+\\s*");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    
    /**
     * Creates a TextParser with an empty stop words set.
     * Use loadStopWords() to populate the stop words.
     */
    public TextParser() {
        this.stopWords = new HashSet<>();
    }
    
    /**
     * Creates a TextParser with the provided stop words set.
     * 
     * @param stopWords Set of words to filter out during parsing
     */
    public TextParser(Set<String> stopWords) {
        this.stopWords = new HashSet<>(stopWords);
    }
    
    /**
     * Loads stop words from a file (one word per line).
     * 
     * @param stopWordsFilePath Path to the stop words file
     * @throws IOException if the file cannot be read
     */
    public void loadStopWords(String stopWordsFilePath) throws IOException {
        Path path = Path.of(stopWordsFilePath);
        this.stopWords = Files.readAllLines(path).stream()
            .map(String::trim)
            .map(String::toLowerCase)
            .filter(line -> !line.isEmpty())
            .collect(Collectors.toSet());
    }
    
    /**
     * Parses text content into individual words.
     * Extracts all words, converts to lowercase, and removes duplicates.
     * 
     * @param textContent The text to parse
     * @return Array of unique words in lowercase
     */
    public String[] parseIntoWords(String textContent) {
        if (textContent == null || textContent.trim().isEmpty()) {
            return new String[0];
        }
        
        Set<String> words = new LinkedHashSet<>();
        var matcher = WORD_PATTERN.matcher(textContent);
        
        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            words.add(word);
        }
        
        return words.toArray(new String[0]);
    }
    
    /**
     * Parses text content into displayable phrases (lines).
     * Splits on newlines and trims whitespace.
     * 
     * @param textContent The text to parse
     * @return Array of phrases (one per line)
     */
    public String[] parseIntoPhrases(String textContent) {
        if (textContent == null || textContent.trim().isEmpty()) {
            return new String[0];
        }
        
        // Split by newlines (handles both \n and \r\n)
        String[] rawPhrases = textContent.split("\\r?\\n");
        
        // Filter out empty lines and trim whitespace
        return Arrays.stream(rawPhrases)
            .map(String::trim)
            .filter(phrase -> !phrase.isEmpty())
            .toArray(String[]::new);
    }
    
    /**
     * Checks if a word is a stop word.
     * A word is considered a stop word if:
     * - It's in the stop words list, OR
     * - It has length less than 3 characters
     * 
     * @param word The word to check (case-insensitive)
     * @return true if the word is a stop word, false otherwise
     */
    public boolean isStopWord(String word) {
        if (word == null) {
            return false;
        }
        // Treat words with length < 3 as stop words
        if (word.length() < 3) {
            return true;
        }
        return stopWords.contains(word.toLowerCase());
    }
    
    /**
     * Filters stop words from a list of words.
     * 
     * @param words Array of words to filter
     * @return Array of words with stop words removed
     */
    public String[] filterStopWords(String[] words) {
        if (words == null) {
            return new String[0];
        }
        
        return Arrays.stream(words)
            .filter(word -> !isStopWord(word))
            .toArray(String[]::new);
    }
    
    /**
     * Creates a mapping from phrase index to content words in that phrase.
     * Content words are words that are not stop words.
     * 
     * @param phrases Array of phrases to analyze
     * @return Map from phrase index to array of content words in that phrase
     */
    public Map<Integer, String[]> mapPhrasesToWords(String[] phrases) {
        if (phrases == null) {
            return new HashMap<>();
        }
        
        Map<Integer, String[]> phraseToWords = new HashMap<>();
        
        for (int i = 0; i < phrases.length; i++) {
            String phrase = phrases[i];
            
            // Extract words from this phrase
            Set<String> wordsInPhrase = new LinkedHashSet<>();
            var matcher = WORD_PATTERN.matcher(phrase);
            
            while (matcher.find()) {
                String word = matcher.group().toLowerCase();
                // Only include content words (non-stop words)
                if (!isStopWord(word)) {
                    wordsInPhrase.add(word);
                }
            }
            
            phraseToWords.put(i, wordsInPhrase.toArray(new String[0]));
        }
        
        return phraseToWords;
    }
}
