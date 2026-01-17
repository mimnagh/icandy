package com.icandy.run;

/**
 * Circular layout algorithm that arranges images in circular or spiral patterns.
 * Supports configurable radius, arc span, rotation direction, and spiral mode.
 */
public class CircularLayoutAlgorithm implements LayoutAlgorithm {
    
    @Override
    public ImagePosition[] calculatePositions(ImageInfo[] images, LayoutConfig config) {
        if (images == null || images.length == 0) {
            return new ImagePosition[0];
        }
        
        ImagePosition[] positions = new ImagePosition[images.length];
        
        // Calculate center point of display region
        float centerX = config.displayRegion.getCenterX();
        float centerY = config.displayRegion.getCenterY();
        
        // Determine if we should use spiral mode
        boolean useSpiral = shouldUseSpiral(images.length, config);
        
        if (useSpiral) {
            calculateSpiralPositions(images, positions, config, centerX, centerY);
        } else {
            calculateCircularPositions(images, positions, config, centerX, centerY);
        }
        
        return positions;
    }
    
    @Override
    public void updatePositions(ImagePosition[] positions, float deltaTime) {
        // Circular layout is static - no ongoing animations
        // Individual positions may be updated by transition engine
    }
    
    /**
     * Calculate positions for a simple circular arrangement.
     */
    private void calculateCircularPositions(ImageInfo[] images, ImagePosition[] positions,
                                          LayoutConfig config, float centerX, float centerY) {
        
        float radius = config.circleRadius;
        float arcSpan = Math.min(360.0f, config.arcSpan); // Clamp to full circle
        
        // Calculate angle step between images
        float angleStep;
        if (images.length == 1) {
            angleStep = 0.0f;
        } else {
            angleStep = arcSpan / (images.length - 1);
            if (arcSpan >= 360.0f) {
                // For full circle, distribute evenly without overlap
                angleStep = 360.0f / images.length;
            }
        }
        
        // Starting angle (top of circle)
        float startAngle = -90.0f; // Start at top
        if (arcSpan < 360.0f) {
            // For partial arcs, center the arc
            startAngle = -90.0f - (arcSpan / 2.0f);
        }
        
        for (int i = 0; i < images.length; i++) {
            float angle = startAngle + (i * angleStep);
            
            // Apply rotation direction
            if (config.rotationDirection == LayoutConfig.RotationDirection.COUNTERCLOCKWISE) {
                angle = -angle;
            }
            
            // Convert to radians
            float angleRad = (float) Math.toRadians(angle);
            
            // Calculate position on circle
            float x = centerX + radius * (float) Math.cos(angleRad);
            float y = centerY + radius * (float) Math.sin(angleRad);
            
            // Calculate image dimensions
            ImageDimensions dims = calculateImageDimensions(images[i], config);
            
            // Center image at calculated position
            float imageX = x - dims.width / 2.0f;
            float imageY = y - dims.height / 2.0f;
            
            positions[i] = new ImagePosition(imageX, imageY, dims.width, dims.height);
            positions[i].scale = config.globalScale;
            
            // Optional: rotate images to face outward from center
            positions[i].rotation = angle + 90.0f; // Face outward
        }
    }
    
    /**
     * Calculate positions for a spiral arrangement.
     */
    private void calculateSpiralPositions(ImageInfo[] images, ImagePosition[] positions,
                                        LayoutConfig config, float centerX, float centerY) {
        
        float baseRadius = config.circleRadius * 0.3f; // Start smaller for spiral
        float maxRadius = config.circleRadius;
        
        // Calculate spiral parameters
        float totalAngle = config.arcSpan * (images.length / 8.0f); // Multiple rotations for spiral
        float angleStep = totalAngle / images.length;
        float radiusStep = (maxRadius - baseRadius) / images.length;
        
        for (int i = 0; i < images.length; i++) {
            float angle = i * angleStep;
            float radius = baseRadius + (i * radiusStep);
            
            // Apply rotation direction
            if (config.rotationDirection == LayoutConfig.RotationDirection.COUNTERCLOCKWISE) {
                angle = -angle;
            }
            
            // Convert to radians
            float angleRad = (float) Math.toRadians(angle);
            
            // Calculate position on spiral
            float x = centerX + radius * (float) Math.cos(angleRad);
            float y = centerY + radius * (float) Math.sin(angleRad);
            
            // Calculate image dimensions (smaller for inner spiral)
            ImageDimensions dims = calculateImageDimensions(images[i], config);
            
            // Scale down inner images for better spiral effect
            float spiralScale = 0.5f + 0.5f * (radius / maxRadius);
            dims.width *= spiralScale;
            dims.height *= spiralScale;
            
            // Center image at calculated position
            float imageX = x - dims.width / 2.0f;
            float imageY = y - dims.height / 2.0f;
            
            positions[i] = new ImagePosition(imageX, imageY, dims.width, dims.height);
            positions[i].scale = config.globalScale * spiralScale;
            
            // Rotate images to follow spiral direction
            positions[i].rotation = angle;
        }
    }
    
    /**
     * Determine if spiral mode should be used based on image count and configuration.
     */
    private boolean shouldUseSpiral(int imageCount, LayoutConfig config) {
        // Use spiral for larger numbers of images or when arc span is large
        return imageCount > 8 || config.arcSpan > 360.0f;
    }
    
    /**
     * Calculate appropriate dimensions for an image in circular layout.
     */
    private ImageDimensions calculateImageDimensions(ImageInfo image, LayoutConfig config) {
        // Base size calculation
        float baseSize = config.circleRadius * 0.15f; // Images are 15% of radius
        
        if (image == null || !image.hasValidImage()) {
            return new ImageDimensions(baseSize, baseSize);
        }
        
        float aspectRatio = image.aspectRatio;
        
        switch (config.aspectRatioMode) {
            case PRESERVE:
                // Scale to fit within base size while preserving aspect ratio
                if (aspectRatio > 1.0f) {
                    // Wider than tall
                    return new ImageDimensions(baseSize, baseSize / aspectRatio);
                } else {
                    // Taller than wide
                    return new ImageDimensions(baseSize * aspectRatio, baseSize);
                }
                
            case STRETCH:
                // Make all images the same size
                return new ImageDimensions(baseSize, baseSize);
                
            case CROP:
                // Scale to fill base size, maintaining aspect ratio
                return new ImageDimensions(baseSize, baseSize);
                
            default:
                return new ImageDimensions(baseSize, baseSize);
        }
    }
    
    /**
     * Helper class to hold image dimensions.
     */
    private static class ImageDimensions {
        float width;
        float height;
        
        ImageDimensions(float width, float height) {
            this.width = width;
            this.height = height;
        }
    }
}