package com.icandy.run;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Collage layout algorithm that creates artistic layouts with random sizes,
 * rotations, and controlled overlap for an organic, non-uniform appearance.
 */
public class CollageLayoutAlgorithm implements LayoutAlgorithm {
    
    private Random random;
    private static final int MAX_PLACEMENT_ATTEMPTS = 50;
    private static final float COLLISION_MARGIN = 10.0f;
    
    public CollageLayoutAlgorithm() {
        this.random = new Random();
    }
    
    /**
     * Create a CollageLayoutAlgorithm with a specific seed for reproducible layouts.
     * 
     * @param seed Random seed for consistent results
     */
    public CollageLayoutAlgorithm(long seed) {
        this.random = new Random(seed);
    }
    
    @Override
    public ImagePosition[] calculatePositions(ImageInfo[] images, LayoutConfig config) {
        if (images == null || images.length == 0) {
            return new ImagePosition[0];
        }
        
        ImagePosition[] positions = new ImagePosition[images.length];
        List<PlacedImage> placedImages = new ArrayList<>();
        
        // Calculate usable area (with padding)
        float padding = Math.max(50.0f, config.gridPadding);
        float usableX = config.displayRegion.x + padding;
        float usableY = config.displayRegion.y + padding;
        float usableWidth = config.displayRegion.width - (2 * padding);
        float usableHeight = config.displayRegion.height - (2 * padding);
        
        // Place each image
        for (int i = 0; i < images.length; i++) {
            ImagePosition position = placeImageInCollage(images[i], config, placedImages,
                                                        usableX, usableY, usableWidth, usableHeight);
            positions[i] = position;
            
            // Add to placed images for collision detection
            placedImages.add(new PlacedImage(position));
        }
        
        return positions;
    }
    
    @Override
    public void updatePositions(ImagePosition[] positions, float deltaTime) {
        // Collage layout is static - no ongoing animations
        // Individual positions may be updated by transition engine
    }
    
    /**
     * Place a single image in the collage, avoiding excessive overlap.
     */
    private ImagePosition placeImageInCollage(ImageInfo image, LayoutConfig config,
                                            List<PlacedImage> placedImages,
                                            float usableX, float usableY,
                                            float usableWidth, float usableHeight) {
        
        // Generate random size within configured range
        float sizeScale = randomFloat(config.minSize, config.maxSize);
        
        // Calculate image dimensions
        float baseWidth = image.hasValidImage() ? image.originalWidth : 200.0f;
        float baseHeight = image.hasValidImage() ? image.originalHeight : 200.0f;
        
        // Apply size scaling while respecting aspect ratio
        float scaledWidth, scaledHeight;
        if (config.aspectRatioMode == LayoutConfig.AspectRatioMode.PRESERVE) {
            float aspectRatio = image.hasValidImage() ? image.aspectRatio : 1.0f;
            scaledWidth = baseWidth * sizeScale;
            scaledHeight = scaledWidth / aspectRatio;
        } else {
            scaledWidth = baseWidth * sizeScale;
            scaledHeight = baseHeight * sizeScale;
        }
        
        // Apply global scale
        scaledWidth *= config.globalScale;
        scaledHeight *= config.globalScale;
        
        // Generate random rotation
        float rotation = randomFloat(config.minRotation, config.maxRotation);
        
        // Try to find a good position with limited overlap
        ImagePosition bestPosition = null;
        float bestOverlapScore = Float.MAX_VALUE;
        
        for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt++) {
            // Generate random position within usable area
            float x = usableX + random.nextFloat() * (usableWidth - scaledWidth);
            float y = usableY + random.nextFloat() * (usableHeight - scaledHeight);
            
            ImagePosition candidate = new ImagePosition(x, y, scaledWidth, scaledHeight,
                                                       rotation, config.globalScale, 1.0f);
            
            // Calculate overlap score
            float overlapScore = calculateOverlapScore(candidate, placedImages, config.overlapAmount);
            
            if (overlapScore < bestOverlapScore) {
                bestOverlapScore = overlapScore;
                bestPosition = candidate;
                
                // If we found a position with acceptable overlap, use it
                if (overlapScore <= config.overlapAmount) {
                    break;
                }
            }
        }
        
        // If no position found, use the best one we found
        if (bestPosition == null) {
            // Fallback: place at random position
            float x = usableX + random.nextFloat() * (usableWidth - scaledWidth);
            float y = usableY + random.nextFloat() * (usableHeight - scaledHeight);
            bestPosition = new ImagePosition(x, y, scaledWidth, scaledHeight,
                                           rotation, config.globalScale, 1.0f);
        }
        
        return bestPosition;
    }
    
    /**
     * Calculate overlap score for a position against already placed images.
     * Lower scores indicate less overlap.
     */
    private float calculateOverlapScore(ImagePosition candidate, List<PlacedImage> placedImages, float allowedOverlap) {
        if (placedImages.isEmpty()) {
            return 0.0f;
        }
        
        float totalOverlap = 0.0f;
        
        for (PlacedImage placed : placedImages) {
            float overlap = calculateOverlapAmount(candidate, placed.position);
            totalOverlap += overlap;
        }
        
        // Normalize by number of placed images
        return totalOverlap / placedImages.size();
    }
    
    /**
     * Calculate the amount of overlap between two image positions.
     * Returns 0.0 for no overlap, 1.0 for complete overlap.
     */
    private float calculateOverlapAmount(ImagePosition pos1, ImagePosition pos2) {
        // Calculate bounding rectangles (ignoring rotation for simplicity)
        float left1 = pos1.x;
        float right1 = pos1.x + pos1.width;
        float top1 = pos1.y;
        float bottom1 = pos1.y + pos1.height;
        
        float left2 = pos2.x;
        float right2 = pos2.x + pos2.width;
        float top2 = pos2.y;
        float bottom2 = pos2.y + pos2.height;
        
        // Check for no overlap
        if (right1 <= left2 || right2 <= left1 || bottom1 <= top2 || bottom2 <= top1) {
            return 0.0f;
        }
        
        // Calculate overlap area
        float overlapLeft = Math.max(left1, left2);
        float overlapRight = Math.min(right1, right2);
        float overlapTop = Math.max(top1, top2);
        float overlapBottom = Math.min(bottom1, bottom2);
        
        float overlapArea = (overlapRight - overlapLeft) * (overlapBottom - overlapTop);
        
        // Calculate areas of both images
        float area1 = pos1.width * pos1.height;
        float area2 = pos2.width * pos2.height;
        float smallerArea = Math.min(area1, area2);
        
        // Return overlap as fraction of smaller image
        return overlapArea / smallerArea;
    }
    
    /**
     * Generate a random float between min and max (inclusive).
     */
    private float randomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
    
    /**
     * Helper class to track placed images for collision detection.
     */
    private static class PlacedImage {
        final ImagePosition position;
        
        PlacedImage(ImagePosition position) {
            this.position = position;
        }
    }
}