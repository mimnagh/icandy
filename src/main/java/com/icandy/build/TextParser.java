package com.icandy.build;

import com.icandy.common.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * TextParser is responsible for reading and parsing text script files.
 * It splits text into phrases (sentences) and individual words, filters stop words,
 * and creates mappings between phrases and their content words.
 */
public class TextParser {
    
    private Set<String> stopWords;
    private final Logger logger;
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+\\s*");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    
    /**
     * Creates a TextParser with an empty stop words set.
     * Use loadStopWords() to populate the stop words.
     */
    public TextParser() {
        this.stopWords = new HashSet<>();
        this.logger = new Logger(TextParser.class);
        logger.info("TextParser initialized with empty stop words set");
    }
    
    /**
     * Creates a TextParser with the provided stop words set.
     * 
     * @param stopWords Set of words to filter out during parsing
     */
    public TextParser(Set<String> stopWords) {
        this.stopWords = new HashSet<>(stopWords);
        this.logger = new Logger(TextParser.class);
        logger.info("TextParser initialized with custom stop words", 
            String.format("stopWordsCount=%d", stopWords.size()));
    }
    
    /**
     * Loads stop words from a file (one word per line).
     * 
     * @param stopWordsFilePath Path to the stop words file
     * @throws IOException if the file cannot be read
     */
    public void loadStopWords(String stopWordsFilePath) throws IOException {
        logger.info("Loading stop words from file", stopWordsFilePath);
        
        try {
            Path path = Path.of(stopWordsFilePath);
            
            if (!Files.exists(path)) {
                IOException e = new IOException("Stop words file not found: " + stopWordsFilePath);
                logger.error("Stop words file not found", stopWordsFilePath, e);
                throw e;
            }
            
            if (!Files.isReadable(path)) {
                IOException e = new IOException("Stop words file is not readable: " + stopWordsFilePath);
                logger.error("Stop words file not readable", stopWordsFilePath, e);
                throw e;
            }
            
            List<String> lines;
            try {
                lines = Files.readAllLines(path);
            } catch (IOException e) {
                logger.error("Failed to read stop words file", stopWordsFilePath, e);
                throw new IOException("Failed to read stop words file: " + e.getMessage(), e);
            }
            
            this.stopWords = lines.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("#")) // Allow comments in stop words file
                .collect(Collectors.toSet());
                
            logger.info("Stop words loaded successfully", 
                String.format("file=%s, count=%d", stopWordsFilePath, stopWords.size()));
                
        } catch (IOException e) {
            logger.error("Failed to load stop words", stopWordsFilePath, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error loading stop words", stopWordsFilePath, e);
            throw new IOException("Unexpected error loading stop words: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parses text content into individual words.
     * Extracts all words, converts to lowercase, and removes duplicates.
     * 
     * @param textContent The text to parse
     * @return Array of unique words in lowercase
     */
    public String[] parseIntoWords(String textContent) {
        if (textContent == null) {
            logger.warning("Null text content provided for word parsing");
            return new String[0];
        }
        
        if (textContent.trim().isEmpty()) {
            logger.warning("Empty text content provided for word parsing");
            return new String[0];
        }
        
        try {
            Set<String> words = new LinkedHashSet<>();
            var matcher = WORD_PATTERN.matcher(textContent);
            
            while (matcher.find()) {
                String word = matcher.group().toLowerCase();
                words.add(word);
            }
            
            String[] result = words.toArray(new String[0]);
            logger.info("Text parsed into words", 
                String.format("textLength=%d, uniqueWords=%d", textContent.length(), result.length));
            return result;
            
        } catch (PatternSyntaxException e) {
            logger.error("Regex pattern error during word parsing", "Using fallback parsing", e);
            // Fallback: simple split by whitespace
            return Arrays.stream(textContent.toLowerCase().split("\\s+"))
                .filter(word -> !word.isEmpty())
                .distinct()
                .toArray(String[]::new);
        } catch (Exception e) {
            logger.error("Unexpected error during word parsing", "Using fallback parsing", e);
            // Fallback: simple split by whitespace
            return Arrays.stream(textContent.toLowerCase().split("\\s+"))
                .filter(word -> !word.isEmpty())
                .distinct()
                .toArray(String[]::new);
        }
    }
    
    /**
     * Parses text content into displayable phrases (lines).
     * Splits on newlines and trims whitespace.
     * 
     * @param textContent The text to parse
     * @return Array of phrases (one per line)
     */
    public String[] parseIntoPhrases(String textContent) {
        if (textContent == null) {
            logger.warning("Null text content provided for phrase parsing");
            return new String[0];
        }
        
        if (textContent.trim().isEmpty()) {
            logger.warning("Empty text content provided for phrase parsing");
            return new String[0];
        }
        
        try {
            // Split by newlines (handles both \n and \r\n)
            String[] rawPhrases = textContent.split("\\r?\\n");
            
            // Filter out empty lines and trim whitespace
            String[] result = Arrays.stream(rawPhrases)
                .map(String::trim)
                .filter(phrase -> !phrase.isEmpty())
                .toArray(String[]::new);
                
            logger.info("Text parsed into phrases", 
                String.format("textLength=%d, phrases=%d", textContent.length(), result.length));
            return result;
            
        } catch (PatternSyntaxException e) {
            logger.error("Regex pattern error during phrase parsing", "Using fallback parsing", e);
            // Fallback: split by common sentence endings
            return Arrays.stream(textContent.split("[.!?]+"))
                .map(String::trim)
                .filter(phrase -> !phrase.isEmpty())
                .toArray(String[]::new);
        } catch (Exception e) {
            logger.error("Unexpected error during phrase parsing", "Using fallback parsing", e);
            // Fallback: return entire text as single phrase
            return new String[]{textContent.trim()};
        }
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
            return true; // Treat null as stop word
        }
        
        String trimmedWord = word.trim();
        
        // Treat words with length < 3 as stop words
        if (trimmedWord.length() < 3) {
            return true;
        }
        
        return stopWords.contains(trimmedWord.toLowerCase());
    }
    
    /**
     * Filters stop words from a list of words.
     * 
     * @param words Array of words to filter
     * @return Array of words with stop words removed
     */
    public String[] filterStopWords(String[] words) {
        if (words == null) {
            logger.warning("Null words array provided for stop word filtering");
            return new String[0];
        }
        
        try {
            String[] result = Arrays.stream(words)
                .filter(Objects::nonNull) // Filter out null words
                .filter(word -> !isStopWord(word))
                .toArray(String[]::new);
                
            logger.info("Stop words filtered", 
                String.format("originalCount=%d, filteredCount=%d, removed=%d", 
                    words.length, result.length, words.length - result.length));
            return result;
            
        } catch (Exception e) {
            logger.error("Error during stop word filtering", "Returning original array", e);
            return words; // Return original array if filtering fails
        }
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
            logger.warning("Null phrases array provided for phrase-to-words mapping");
            return new HashMap<>();
        }
        
        try {
            Map<Integer, String[]> phraseToWords = new HashMap<>();
            
            for (int i = 0; i < phrases.length; i++) {
                String phrase = phrases[i];
                
                if (phrase == null) {
                    logger.warning("Null phrase found at index", String.valueOf(i));
                    phraseToWords.put(i, new String[0]);
                    continue;
                }
                
                // Extract words from this phrase
                Set<String> wordsInPhrase = new LinkedHashSet<>();
                
                try {
                    var matcher = WORD_PATTERN.matcher(phrase);
                    
                    while (matcher.find()) {
                        String word = matcher.group().toLowerCase();
                        // Only include content words (non-stop words)
                        if (!isStopWord(word)) {
                            wordsInPhrase.add(word);
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Error parsing phrase at index", 
                        String.format("index=%d, phrase=%s, error=%s", i, phrase, e.getMessage()));
                    // Fallback: simple word extraction
                    String[] simpleWords = phrase.toLowerCase().split("\\s+");
                    for (String word : simpleWords) {
                        if (!isStopWord(word)) {
                            wordsInPhrase.add(word);
                        }
                    }
                }
                
                phraseToWords.put(i, wordsInPhrase.toArray(new String[0]));
            }
            
            logger.info("Phrase-to-words mapping created", 
                String.format("phrases=%d, mappings=%d", phrases.length, phraseToWords.size()));
            return phraseToWords;
            
        } catch (Exception e) {
            logger.error("Error creating phrase-to-words mapping", "Returning empty map", e);
            return new HashMap<>();
        }
    }
}
