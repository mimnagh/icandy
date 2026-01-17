package com.icandy.run;

/**
 * Interface for layout algorithms that position images on screen.
 * Different implementations provide different positioning strategies
 * (grid, collage, circular, flowing).
 */
public interface LayoutAlgorithm {
    
    /**
     * Calculate positions for a set of images based on the layout configuration.
     * 
     * @param images Array of images to position
     * @param config Configuration parameters for this layout algorithm
     * @return Array of calculated positions, one for each image
     */
    ImagePosition[] calculatePositions(ImageInfo[] images, LayoutConfig config);
    
    /**
     * Update positions over time for animations and transitions.
     * This method is called each frame to update any ongoing animations.
     * 
     * @param positions Current positions to update
     * @param deltaTime Time elapsed since last update in milliseconds
     */
    void updatePositions(ImagePosition[] positions, float deltaTime);
}