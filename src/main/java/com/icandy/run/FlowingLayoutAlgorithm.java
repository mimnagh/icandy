package com.icandy.run;

/**
 * Flowing layout algorithm that positions images along curved Bezier paths.
 * Supports configurable flow direction, curvature, and organic spacing.
 */
public class FlowingLayoutAlgorithm implements LayoutAlgorithm {
    
    @Override
    public ImagePosition[] calculatePositions(ImageInfo[] images, LayoutConfig config) {
        if (images == null || images.length == 0) {
            return new ImagePosition[0];
        }
        
        ImagePosition[] positions = new ImagePosition[images.length];
        
        // Generate flow path based on direction and curvature
        FlowPath path = generateFlowPath(config);
        
        // Distribute images along the path
        distributeImagesAlongPath(images, positions, path, config);
        
        return positions;
    }
    
    @Override
    public void updatePositions(ImagePosition[] positions, float deltaTime) {
        // Flowing layout is static - no ongoing animations
        // Individual positions may be updated by transition engine
    }
    
    /**
     * Generate a flow path based on configuration.
     */
    private FlowPath generateFlowPath(LayoutConfig config) {
        float startX, startY, endX, endY;
        float control1X, control1Y, control2X, control2Y;
        
        // Define path endpoints based on flow direction
        switch (config.flowDirection) {
            case HORIZONTAL:
                startX = config.displayRegion.x + config.gridPadding;
                startY = config.displayRegion.getCenterY();
                endX = config.displayRegion.x + config.displayRegion.width - config.gridPadding;
                endY = config.displayRegion.getCenterY();
                
                // Add curvature with control points
                float verticalOffset = config.pathCurvature * config.displayRegion.height * 0.3f;
                control1X = startX + (endX - startX) * 0.33f;
                control1Y = startY - verticalOffset;
                control2X = startX + (endX - startX) * 0.67f;
                control2Y = startY + verticalOffset;
                break;
                
            case VERTICAL:
                startX = config.displayRegion.getCenterX();
                startY = config.displayRegion.y + config.gridPadding;
                endX = config.displayRegion.getCenterX();
                endY = config.displayRegion.y + config.displayRegion.height - config.gridPadding;
                
                // Add curvature with control points
                float horizontalOffset = config.pathCurvature * config.displayRegion.width * 0.3f;
                control1X = startX - horizontalOffset;
                control1Y = startY + (endY - startY) * 0.33f;
                control2X = startX + horizontalOffset;
                control2Y = startY + (endY - startY) * 0.67f;
                break;
                
            case DIAGONAL_UP:
                startX = config.displayRegion.x + config.gridPadding;
                startY = config.displayRegion.y + config.displayRegion.height - config.gridPadding;
                endX = config.displayRegion.x + config.displayRegion.width - config.gridPadding;
                endY = config.displayRegion.y + config.gridPadding;
                
                // Add curvature perpendicular to diagonal
                float perpOffset = config.pathCurvature * config.displayRegion.width * 0.2f;
                control1X = startX + (endX - startX) * 0.33f + perpOffset;
                control1Y = startY + (endY - startY) * 0.33f;
                control2X = startX + (endX - startX) * 0.67f - perpOffset;
                control2Y = startY + (endY - startY) * 0.67f;
                break;
                
            case DIAGONAL_DOWN:
                startX = config.displayRegion.x + config.gridPadding;
                startY = config.displayRegion.y + config.gridPadding;
                endX = config.displayRegion.x + config.displayRegion.width - config.gridPadding;
                endY = config.displayRegion.y + config.displayRegion.height - config.gridPadding;
                
                // Add curvature perpendicular to diagonal
                float perpOffset2 = config.pathCurvature * config.displayRegion.width * 0.2f;
                control1X = startX + (endX - startX) * 0.33f - perpOffset2;
                control1Y = startY + (endY - startY) * 0.33f;
                control2X = startX + (endX - startX) * 0.67f + perpOffset2;
                control2Y = startY + (endY - startY) * 0.67f;
                break;
                
            default:
                // Default to horizontal
                startX = config.displayRegion.x + config.gridPadding;
                startY = config.displayRegion.getCenterY();
                endX = config.displayRegion.x + config.displayRegion.width - config.gridPadding;
                endY = config.displayRegion.getCenterY();
                control1X = startX + (endX - startX) * 0.33f;
                control1Y = startY;
                control2X = startX + (endX - startX) * 0.67f;
                control2Y = startY;
                break;
        }
        
        return new FlowPath(startX, startY, control1X, control1Y, control2X, control2Y, endX, endY);
    }
    
    /**
     * Distribute images along the generated flow path.
     */
    private void distributeImagesAlongPath(ImageInfo[] images, ImagePosition[] positions,
                                         FlowPath path, LayoutConfig config) {
        
        for (int i = 0; i < images.length; i++) {
            // Calculate position along path (0.0 to 1.0)
            float t = images.length > 1 ? (float) i / (images.length - 1) : 0.5f;
            
            // Add some organic variation to spacing
            float variation = (float) Math.sin(i * 0.7) * 0.05f; // Small random-like variation
            t = Math.max(0.0f, Math.min(1.0f, t + variation));
            
            // Calculate position on Bezier curve
            PathPoint point = path.getPointAt(t);
            
            // Calculate image dimensions
            ImageDimensions dims = calculateImageDimensions(images[i], config, t);
            
            // Center image at path point
            float imageX = point.x - dims.width / 2.0f;
            float imageY = point.y - dims.height / 2.0f;
            
            positions[i] = new ImagePosition(imageX, imageY, dims.width, dims.height);
            positions[i].scale = config.globalScale;
            
            // Rotate image to follow path direction
            positions[i].rotation = point.tangentAngle;
        }
    }
    
    /**
     * Calculate appropriate dimensions for an image in flowing layout.
     */
    private ImageDimensions calculateImageDimensions(ImageInfo image, LayoutConfig config, float pathPosition) {
        // Base size varies along path for organic effect
        float baseSize = config.pathSpacing * 0.8f;
        
        // Add size variation along path
        float sizeVariation = 1.0f + 0.3f * (float) Math.sin(pathPosition * Math.PI * 2);
        baseSize *= sizeVariation;
        
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
                // Make all images similar size with slight variation
                return new ImageDimensions(baseSize, baseSize * 0.8f);
                
            case CROP:
                // Scale to fill base size, maintaining aspect ratio
                return new ImageDimensions(baseSize, baseSize);
                
            default:
                return new ImageDimensions(baseSize, baseSize);
        }
    }
    
    /**
     * Helper class representing a cubic Bezier curve path.
     */
    private static class FlowPath {
        private final float startX, startY;
        private final float control1X, control1Y;
        private final float control2X, control2Y;
        private final float endX, endY;
        
        FlowPath(float startX, float startY, float control1X, float control1Y,
                float control2X, float control2Y, float endX, float endY) {
            this.startX = startX;
            this.startY = startY;
            this.control1X = control1X;
            this.control1Y = control1Y;
            this.control2X = control2X;
            this.control2Y = control2Y;
            this.endX = endX;
            this.endY = endY;
        }
        
        /**
         * Get a point on the Bezier curve at parameter t (0.0 to 1.0).
         */
        PathPoint getPointAt(float t) {
            // Cubic Bezier curve formula
            float u = 1.0f - t;
            float tt = t * t;
            float uu = u * u;
            float uuu = uu * u;
            float ttt = tt * t;
            
            // Calculate position
            float x = uuu * startX + 3 * uu * t * control1X + 3 * u * tt * control2X + ttt * endX;
            float y = uuu * startY + 3 * uu * t * control1Y + 3 * u * tt * control2Y + ttt * endY;
            
            // Calculate tangent for rotation
            float tangentX = 3 * uu * (control1X - startX) + 6 * u * t * (control2X - control1X) + 3 * tt * (endX - control2X);
            float tangentY = 3 * uu * (control1Y - startY) + 6 * u * t * (control2Y - control1Y) + 3 * tt * (endY - control2Y);
            
            float tangentAngle = (float) Math.toDegrees(Math.atan2(tangentY, tangentX));
            
            return new PathPoint(x, y, tangentAngle);
        }
    }
    
    /**
     * Helper class representing a point on the path with tangent information.
     */
    private static class PathPoint {
        final float x, y;
        final float tangentAngle;
        
        PathPoint(float x, float y, float tangentAngle) {
            this.x = x;
            this.y = y;
            this.tangentAngle = tangentAngle;
        }
    }
    
    /**
     * Helper class to hold image dimensions.
     */
    private static class ImageDimensions {
        final float width;
        final float height;
        
        ImageDimensions(float width, float height) {
            this.width = width;
            this.height = height;
        }
    }
}