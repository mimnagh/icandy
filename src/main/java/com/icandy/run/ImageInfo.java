package com.icandy.run;

import processing.core.PImage;

/**
 * Represents an image with its metadata for layout and effects processing.
 * Contains the image data along with information needed for positioning
 * and visual effects.
 */
public class ImageInfo {
    
    /** The Processing image object */
    public PImage image;
    
    /** The word this image is associated with */
    public String word;
    
    /** File path where this image is stored */
    public String filePath;
    
    /** Original width of the image in pixels */
    public float originalWidth;
    
    /** Original height of the image in pixels */
    public float originalHeight;
    
    /** Aspect ratio (width/height) of the image */
    public float aspectRatio;
    
    /**
     * Create a new ImageInfo with the specified parameters.
     * 
     * @param image The Processing image object
     * @param word The word this image represents
     * @param filePath Path to the image file
     */
    public ImageInfo(PImage image, String word, String filePath) {
        this.image = image;
        this.word = word;
        this.filePath = filePath;
        
        if (image != null) {
            this.originalWidth = image.width;
            this.originalHeight = image.height;
            this.aspectRatio = image.width / (float) image.height;
        } else {
            this.originalWidth = 0;
            this.originalHeight = 0;
            this.aspectRatio = 1.0f;
        }
    }
    
    /**
     * Create a new ImageInfo with explicit dimensions.
     * Useful for testing or when image is not yet loaded.
     * 
     * @param image The Processing image object (can be null)
     * @param word The word this image represents
     * @param filePath Path to the image file
     * @param width Original width of the image
     * @param height Original height of the image
     */
    public ImageInfo(PImage image, String word, String filePath, float width, float height) {
        this.image = image;
        this.word = word;
        this.filePath = filePath;
        this.originalWidth = width;
        this.originalHeight = height;
        this.aspectRatio = width / height;
    }
    
    /**
     * Check if this ImageInfo has a valid loaded image.
     * 
     * @return true if image is not null, false otherwise
     */
    public boolean hasValidImage() {
        return image != null;
    }
    
    @Override
    public String toString() {
        return String.format("ImageInfo{word='%s', filePath='%s', dimensions=%.0fx%.0f, aspectRatio=%.2f}", 
                           word, filePath, originalWidth, originalHeight, aspectRatio);
    }
}