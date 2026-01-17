package com.icandy.run;

/**
 * Represents the visual state of an image including position, size, rotation, and opacity.
 * Used by transition effects to animate between different states.
 */
public class ImageState {
    private float x;
    private float y;
    private float width;
    private float height;
    private float rotation;
    private float opacity;
    private float scaleX;
    private float scaleY;
    
    /**
     * Creates a new image state with default values.
     */
    public ImageState() {
        this(0, 0, 100, 100, 0, 1.0f, 1.0f, 1.0f);
    }
    
    /**
     * Creates a new image state with specified values.
     * 
     * @param x The x position
     * @param y The y position
     * @param width The width
     * @param height The height
     * @param rotation The rotation in radians
     * @param opacity The opacity (0.0 to 1.0)
     * @param scaleX The horizontal scale factor
     * @param scaleY The vertical scale factor
     */
    public ImageState(float x, float y, float width, float height, float rotation, float opacity, float scaleX, float scaleY) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
        this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }
    
    /**
     * Creates a copy of another image state.
     * 
     * @param other The state to copy
     */
    public ImageState(ImageState other) {
        this(other.x, other.y, other.width, other.height, other.rotation, other.opacity, other.scaleX, other.scaleY);
    }
    
    /**
     * Creates an image state from an image position.
     * 
     * @param position The image position to convert
     */
    public ImageState(ImagePosition position) {
        this(position.x, position.y, position.width, position.height, 
             position.rotation, position.opacity, position.scale, position.scale);
    }
    
    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getRotation() { return rotation; }
    public float getOpacity() { return opacity; }
    public float getScaleX() { return scaleX; }
    public float getScaleY() { return scaleY; }
    
    // Setters with validation
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setWidth(float width) { this.width = Math.max(0, width); }
    public void setHeight(float height) { this.height = Math.max(0, height); }
    public void setRotation(float rotation) { this.rotation = rotation; }
    public void setOpacity(float opacity) { this.opacity = Math.max(0.0f, Math.min(1.0f, opacity)); }
    public void setScaleX(float scaleX) { this.scaleX = scaleX; }
    public void setScaleY(float scaleY) { this.scaleY = scaleY; }
    
    /**
     * Linearly interpolates between this state and another state.
     * 
     * @param other The target state
     * @param t The interpolation factor (0.0 to 1.0)
     * @return A new interpolated image state
     */
    public ImageState lerp(ImageState other, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        
        return new ImageState(
            x + (other.x - x) * t,
            y + (other.y - y) * t,
            width + (other.width - width) * t,
            height + (other.height - height) * t,
            rotation + (other.rotation - rotation) * t,
            opacity + (other.opacity - opacity) * t,
            scaleX + (other.scaleX - scaleX) * t,
            scaleY + (other.scaleY - scaleY) * t
        );
    }
    
    /**
     * Converts this image state to an image position.
     * 
     * @return A new ImagePosition with this state's values
     */
    public ImagePosition toImagePosition() {
        return new ImagePosition(x, y, width, height, rotation, scaleX, opacity);
    }
    
    @Override
    public String toString() {
        return String.format("ImageState{x=%.1f, y=%.1f, w=%.1f, h=%.1f, rot=%.2f, opacity=%.2f, scaleX=%.2f, scaleY=%.2f}",
                x, y, width, height, rotation, opacity, scaleX, scaleY);
    }
}