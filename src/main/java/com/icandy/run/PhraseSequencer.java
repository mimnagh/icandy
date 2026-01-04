package com.icandy.run;

import java.util.Map;

/**
 * PhraseSequencer manages the sequence of phrases and timing with keyboard navigation.
 * It tracks the current position in the text script and provides methods to navigate
 * forward and backward through phrases, with optional looping behavior.
 */
public class PhraseSequencer {
    
    private final String[] phrases;
    private final Map<Integer, String[]> phraseToWords;
    private int currentIndex;
    private boolean looping;
    
    /**
     * Creates a PhraseSequencer with the given phrases and phrase-to-words mapping.
     * Looping is enabled by default.
     * 
     * @param phrases Array of phrases in sequence
     * @param phraseToWords Map from phrase index to content words in that phrase
     * @throws IllegalArgumentException if phrases is null or empty
     */
    public PhraseSequencer(String[] phrases, Map<Integer, String[]> phraseToWords) {
        if (phrases == null || phrases.length == 0) {
            throw new IllegalArgumentException("Phrases array cannot be null or empty");
        }
        if (phraseToWords == null) {
            throw new IllegalArgumentException("Phrase-to-words map cannot be null");
        }
        
        this.phrases = phrases;
        this.phraseToWords = phraseToWords;
        this.currentIndex = 0;
        this.looping = true;
    }
    
    /**
     * Gets the current phrase being displayed.
     * 
     * @return The current phrase string
     */
    public String getCurrentPhrase() {
        return phrases[currentIndex];
    }
    
    /**
     * Gets the content words in the current phrase.
     * Returns an empty array if no words are mapped for the current phrase.
     * 
     * @return Array of content words in the current phrase
     */
    public String[] getWordsInCurrentPhrase() {
        String[] words = phraseToWords.get(currentIndex);
        return words != null ? words : new String[0];
    }
    
    /**
     * Advances to the next phrase in the sequence.
     * If at the end and looping is enabled, wraps to the first phrase.
     * If at the end and looping is disabled, stays at the last phrase.
     */
    public void advance() {
        if (hasNext()) {
            currentIndex++;
        } else if (looping) {
            currentIndex = 0;
        }
        // If not looping and at end, stay at current position
    }
    
    /**
     * Goes back to the previous phrase in the sequence.
     * If at the beginning, stays at the first phrase.
     */
    public void goBack() {
        if (hasPrevious()) {
            currentIndex--;
        }
        // If at beginning, stay at current position
    }
    
    /**
     * Checks if there is a next phrase available.
     * 
     * @return true if not at the last phrase, false otherwise
     */
    public boolean hasNext() {
        return currentIndex < phrases.length - 1;
    }
    
    /**
     * Checks if there is a previous phrase available.
     * 
     * @return true if not at the first phrase, false otherwise
     */
    public boolean hasPrevious() {
        return currentIndex > 0;
    }
    
    /**
     * Resets the sequencer to the first phrase.
     */
    public void reset() {
        currentIndex = 0;
    }
    
    /**
     * Gets the current phrase index.
     * 
     * @return The zero-based index of the current phrase
     */
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    /**
     * Sets whether phrases should loop back to the beginning after the last phrase.
     * 
     * @param loop true to enable looping, false to disable
     */
    public void setLooping(boolean loop) {
        this.looping = loop;
    }
    
    /**
     * Gets the total number of phrases in the sequence.
     * 
     * @return The total phrase count
     */
    public int getPhraseCount() {
        return phrases.length;
    }
    
    /**
     * Checks if looping is currently enabled.
     * 
     * @return true if looping is enabled, false otherwise
     */
    public boolean isLooping() {
        return looping;
    }
}
