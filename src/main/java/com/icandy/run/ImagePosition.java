package com.icandy.run;

/**
 * Represents the position and transformation of an image on screen.
 * Contains all the information needed to render an image at a specific
 * location with specific visual properties.
 */
public class ImagePosition {
    
    /** X coordinate position on screen */
    public float x;
    
    /** Y coordinate position on screen */
    public float y;
    
    /** Display width (may be different from original due to scaling) */
    public float width;
    
    /** Display height (may be different from original due to scaling) */
    public float height;
    
    /** Rotation angle in degrees */
    public float rotation;
    
    /** Scale factor (1.0 = original size) */
    public float scale;
    
    /** Alpha transparency (0.0 = fully transparent, 1.0 = fully opaque) */
    public float opacity;
    
    /** Timestamp when this position was calculated (for animation timing) */
    public long timestamp;
    
    /**
     * Create a new ImagePosition with default values.
     * Position at origin, no rotation, full scale and opacity.
     */
    public ImagePosition() {
        this(0, 0, 0, 0);
    }
    
    /**
     * Create a new ImagePosition with specified position and dimensions.
     * 
     * @param x X coordinate
     * @param y Y coordinate  
     * @param width Display width
     * @param height Display height
     */
    public ImagePosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = 0.0f;
        this.scale = 1.0f;
        this.opacity = 1.0f;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Create a new ImagePosition with all parameters specified.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Display width
     * @param height Display height
     * @param rotation Rotation angle in degrees
     * @param scale Scale factor
     * @param opacity Alpha transparency (0.0 to 1.0)
     */
    public ImagePosition(float x, float y, float width, float height, 
                        float rotation, float scale, float opacity) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.scale = scale;
        this.opacity = opacity;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Create a copy of this ImagePosition.
     * 
     * @return A new ImagePosition with the same values
     */
    public ImagePosition copy() {
        ImagePosition copy = new ImagePosition(x, y, width, height, rotation, scale, opacity);
        copy.timestamp = this.timestamp;
        return copy;
    }
    
    /**
     * Set the position coordinates.
     * 
     * @param x New X coordinate
     * @param y New Y coordinate
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Set the display dimensions.
     * 
     * @param width New display width
     * @param height New display height
     */
    public void setDimensions(float width, float height) {
        this.width = width;
        this.height = height;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Apply a transformation to this position.
     * 
     * @param rotation Additional rotation in degrees
     * @param scale Scale multiplier
     * @param opacity Opacity multiplier
     */
    public void applyTransform(float rotation, float scale, float opacity) {
        this.rotation += rotation;
        this.scale *= scale;
        this.opacity *= opacity;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the center X coordinate of this position.
     * 
     * @return Center X coordinate
     */
    public float getCenterX() {
        return x + width / 2.0f;
    }
    
    /**
     * Get the center Y coordinate of this position.
     * 
     * @return Center Y coordinate
     */
    public float getCenterY() {
        return y + height / 2.0f;
    }
    
    @Override
    public String toString() {
        return String.format("ImagePosition{pos=(%.1f,%.1f), size=%.1fx%.1f, rot=%.1f°, scale=%.2f, opacity=%.2f}", 
                           x, y, width, height, rotation, scale, opacity);
    }
}