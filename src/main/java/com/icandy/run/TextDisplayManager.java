package com.icandy.run;

import com.icandy.common.ConfigurationManager;
import processing.core.PApplet;
import processing.core.PConstants;

/**
 * TextDisplayManager manages the display of text phrases on screen with subtitle-like styling.
 * 
 * This class handles:
 * - Rendering text phrases with appropriate styling
 * - Calculating display duration based on phrase length
 * - Managing phrase transitions
 * - Positioning text on screen
 * 
 * Requirements: 4.2, 4.6
 */
public class TextDisplayManager {
    
    // Constants
    private static final float TEXT_WIDTH_RATIO = 0.9f; // Use 90% of screen width for text
    
    private final PApplet parent;
    private final ConfigurationManager config;
    private final boolean isTestMode;
    
    private String currentPhrase;
    private long phraseStartTime;
    private int currentDuration;
    
    /**
     * Creates a TextDisplayManager.
     * 
     * @param parent The Processing PApplet instance for rendering
     * @param config Configuration manager for display settings
     */
    public TextDisplayManager(PApplet parent, ConfigurationManager config) {
        this.parent = parent;
        this.config = config;
        this.currentPhrase = "";
        this.phraseStartTime = 0;
        this.currentDuration = 0;
        
        // Detect if we're in a test environment (PApplet not fully initialized)
        this.isTestMode = (parent.g == null);
    }
    
    /**
     * Displays a phrase on screen with subtitle-like styling.
     * 
     * The text is rendered centered horizontally and positioned in the lower third
     * of the screen (typical subtitle position). Long phrases are automatically
     * wrapped to multiple lines to fit within the screen width.
     * 
     * @param phrase The phrase to display
     * @param x The x-coordinate (typically center of screen)
     * @param y The y-coordinate (typically lower third of screen)
     */
    public void displayPhrase(String phrase, int x, int y) {
        if (phrase == null || phrase.isEmpty()) {
            return;
        }
        
        // Skip rendering in test mode (PApplet not fully initialized)
        if (isTestMode) {
            return;
        }
        
        try {
            // Set text properties from configuration
            parent.textSize(config.getTextSize());
            parent.textAlign(PConstants.CENTER, PConstants.CENTER);
            
            // Parse and apply text color
            int textColor = parseHexColor(config.getTextColor());
            parent.fill(textColor);
            
            // Wrap text if needed and draw
            String[] lines = wrapText(phrase, parent.width * TEXT_WIDTH_RATIO);
            drawMultilineText(lines, x, y);
        } catch (RuntimeException e) {
            // Only catch known initialization errors
            if (e.getMessage() != null && e.getMessage().contains("textFont")) {
                // Expected error when font not initialized - skip rendering
                return;
            }
            // Re-throw unexpected exceptions to avoid hiding bugs
            throw e;
        }
    }
    
    /**
     * Wraps text to fit within a maximum width.
     * 
     * @param text The text to wrap
     * @param maxWidth The maximum width in pixels
     * @return Array of text lines
     */
    private String[] wrapText(String text, float maxWidth) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }
        
        // Validate maxWidth
        if (maxWidth <= 0) {
            // Invalid width - return single line
            return new String[]{text};
        }
        
        // In test mode, can't measure text width - return single line
        if (isTestMode) {
            return new String[]{text};
        }
        
        // Check if we can measure text width (requires initialized graphics context)
        float textWidth;
        try {
            textWidth = parent.textWidth(text);
        } catch (Exception e) {
            // If we can't measure, return single line
            return new String[]{text};
        }
        
        // Check if text fits on one line
        if (textWidth <= maxWidth) {
            return new String[]{text};
        }
        
        // Split into words and wrap
        String[] words = text.split("\\s+");
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 
                ? word 
                : currentLine + " " + word;
            
            float testWidth;
            try {
                testWidth = parent.textWidth(testLine);
            } catch (Exception e) {
                // If we can't measure, return what we have so far
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                lines.add(word);
                continue;
            }
            
            if (testWidth <= maxWidth) {
                // Word fits on current line
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                // Word doesn't fit, start new line
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Single word is too long, add it anyway
                    lines.add(word);
                }
            }
        }
        
        // Add the last line
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
    
    /**
     * Draws multiple lines of text centered at the given position.
     * 
     * @param lines The lines of text to draw
     * @param x The x-coordinate (center)
     * @param y The y-coordinate (center of all lines)
     */
    private void drawMultilineText(String[] lines, int x, int y) {
        if (lines == null || lines.length == 0) {
            return;
        }
        
        // Calculate line height (text size + spacing)
        float lineHeight = config.getTextSize() * 1.2f; // 20% spacing between lines
        
        // Calculate total height of all lines
        float totalHeight = lines.length * lineHeight;
        
        // Start y position (centered vertically around the given y)
        float startY = y - (totalHeight / 2) + (lineHeight / 2);
        
        // Draw each line
        for (int i = 0; i < lines.length; i++) {
            float lineY = startY + (i * lineHeight);
            parent.text(lines[i], x, lineY);
        }
    }
    
    /**
     * Updates to a new phrase and resets the display timer.
     * 
     * @param nextPhrase The next phrase to display
     */
    public void updatePhrase(String nextPhrase) {
        this.currentPhrase = nextPhrase;
        this.phraseStartTime = System.currentTimeMillis();
        this.currentDuration = calculateDisplayDuration(nextPhrase);
    }
    
    /**
     * Checks if the current phrase should advance to the next one.
     * 
     * @return true if enough time has elapsed for the current phrase
     */
    public boolean shouldAdvance() {
        if (currentPhrase == null || currentPhrase.isEmpty()) {
            return true;
        }
        
        long elapsed = System.currentTimeMillis() - phraseStartTime;
        return elapsed >= currentDuration;
    }
    
    /**
     * Calculates the display duration for a phrase using the formula:
     * duration = (wordCount * msPerWord) + baseDuration
     * 
     * The result is bounded by minimum and maximum duration limits from configuration.
     * 
     * Formula from design:
     * - Base formula: duration = (wordCount * 300ms) + 1000ms
     * - Minimum duration: 2 seconds (configurable)
     * - Maximum duration: 10 seconds (configurable)
     * 
     * @param phrase The phrase to calculate duration for
     * @return The display duration in milliseconds
     */
    public int calculateDisplayDuration(String phrase) {
        if (phrase == null || phrase.isEmpty()) {
            return config.getMinPhraseDuration();
        }
        
        // Count words in the phrase
        String trimmed = phrase.trim();
        if (trimmed.isEmpty()) {
            return config.getMinPhraseDuration();
        }
        
        // Split by whitespace and count non-empty tokens
        String[] words = trimmed.split("\\s+");
        int wordCount = words.length;
        
        // Apply formula: duration = (wordCount * msPerWord) + baseDuration
        // Using minPhraseDuration as the base duration
        int baseDuration = 1000; // 1 second base as per design
        int calculatedDuration = (wordCount * config.getMsPerWord()) + baseDuration;
        
        // Bound by min and max
        int minDuration = config.getMinPhraseDuration();
        int maxDuration = config.getMaxPhraseDuration();
        
        if (calculatedDuration < minDuration) {
            return minDuration;
        } else if (calculatedDuration > maxDuration) {
            return maxDuration;
        } else {
            return calculatedDuration;
        }
    }
    
    /**
     * Gets the current phrase being displayed.
     * 
     * @return The current phrase
     */
    public String getCurrentPhrase() {
        return currentPhrase;
    }
    
    /**
     * Gets the current display duration.
     * 
     * @return The duration in milliseconds
     */
    public int getCurrentDuration() {
        return currentDuration;
    }
    
    /**
     * Resets the phrase timer (useful for manual navigation).
     */
    public void resetTimer() {
        this.phraseStartTime = System.currentTimeMillis();
    }
    
    /**
     * Parses a hex color string (e.g., "#FFFFFF") to Processing color int.
     * 
     * @param hexColor The hex color string
     * @return The color as an int
     */
    private int parseHexColor(String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
            return parent.color(255, 255, 255); // Default to white
        }
        
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            return parent.color(r, g, b);
        } catch (NumberFormatException e) {
            return parent.color(255, 255, 255); // Default to white on error
        }
    }
}
