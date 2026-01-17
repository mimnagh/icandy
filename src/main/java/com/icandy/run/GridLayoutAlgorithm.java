package com.icandy.run;

/**
 * Grid layout algorithm that arranges images in a regular rectangular grid.
 * Supports configurable rows, columns, spacing, padding, and alignment options.
 */
public class GridLayoutAlgorithm implements LayoutAlgorithm {
    
    @Override
    public ImagePosition[] calculatePositions(ImageInfo[] images, LayoutConfig config) {
        if (images == null || images.length == 0) {
            return new ImagePosition[0];
        }
        
        ImagePosition[] positions = new ImagePosition[images.length];
        
        // Calculate grid dimensions
        GridDimensions grid = calculateGridDimensions(images.length, config);
        
        // Calculate cell dimensions
        float availableWidth = config.displayRegion.width - (2 * config.gridPadding);
        float availableHeight = config.displayRegion.height - (2 * config.gridPadding);
        
        float cellWidth = (availableWidth - (grid.cols - 1) * config.gridSpacing) / grid.cols;
        float cellHeight = (availableHeight - (grid.rows - 1) * config.gridSpacing) / grid.rows;
        
        // Calculate starting position based on alignment
        float startX = config.displayRegion.x + config.gridPadding;
        float startY = config.displayRegion.y + config.gridPadding;
        
        // Apply alignment adjustments
        applyAlignment(config.gridAlignment, grid, availableWidth, availableHeight, 
                      cellWidth, cellHeight, config.gridSpacing, startX, startY);
        
        // Position each image
        for (int i = 0; i < images.length; i++) {
            int row = i / grid.cols;
            int col = i % grid.cols;
            
            // Calculate cell position
            float cellX = startX + col * (cellWidth + config.gridSpacing);
            float cellY = startY + row * (cellHeight + config.gridSpacing);
            
            // Calculate image dimensions within cell
            ImageDimensions imageDims = calculateImageDimensions(images[i], cellWidth, cellHeight, config);
            
            // Center image within cell
            float imageX = cellX + (cellWidth - imageDims.width) / 2.0f;
            float imageY = cellY + (cellHeight - imageDims.height) / 2.0f;
            
            positions[i] = new ImagePosition(imageX, imageY, imageDims.width, imageDims.height);
            positions[i].scale = config.globalScale;
        }
        
        return positions;
    }
    
    @Override
    public void updatePositions(ImagePosition[] positions, float deltaTime) {
        // Grid layout is static - no ongoing animations
        // Individual positions may be updated by transition engine
    }
    
    /**
     * Calculate the optimal grid dimensions (rows and columns) for the given number of images.
     */
    private GridDimensions calculateGridDimensions(int imageCount, LayoutConfig config) {
        int rows = config.gridRows;
        int cols = config.gridCols;
        
        // Auto-calculate dimensions if not specified
        if (rows <= 0 && cols <= 0) {
            // Calculate square-ish grid
            cols = (int) Math.ceil(Math.sqrt(imageCount));
            rows = (int) Math.ceil((double) imageCount / cols);
        } else if (rows <= 0) {
            // Calculate rows based on columns
            rows = (int) Math.ceil((double) imageCount / cols);
        } else if (cols <= 0) {
            // Calculate columns based on rows
            cols = (int) Math.ceil((double) imageCount / rows);
        }
        
        return new GridDimensions(rows, cols);
    }
    
    /**
     * Apply alignment adjustments to the starting position.
     */
    private void applyAlignment(LayoutConfig.Alignment alignment, GridDimensions grid,
                               float availableWidth, float availableHeight,
                               float cellWidth, float cellHeight, float spacing,
                               float startX, float startY) {
        
        float totalGridWidth = grid.cols * cellWidth + (grid.cols - 1) * spacing;
        float totalGridHeight = grid.rows * cellHeight + (grid.rows - 1) * spacing;
        
        float horizontalExtra = availableWidth - totalGridWidth;
        float verticalExtra = availableHeight - totalGridHeight;
        
        switch (alignment) {
            case CENTER:
                startX += horizontalExtra / 2.0f;
                startY += verticalExtra / 2.0f;
                break;
            case LEFT:
                startY += verticalExtra / 2.0f;
                break;
            case RIGHT:
                startX += horizontalExtra;
                startY += verticalExtra / 2.0f;
                break;
            case TOP:
                startX += horizontalExtra / 2.0f;
                break;
            case BOTTOM:
                startX += horizontalExtra / 2.0f;
                startY += verticalExtra;
                break;
            case TOP_LEFT:
                // No adjustment needed
                break;
            case TOP_RIGHT:
                startX += horizontalExtra;
                break;
            case BOTTOM_LEFT:
                startY += verticalExtra;
                break;
            case BOTTOM_RIGHT:
                startX += horizontalExtra;
                startY += verticalExtra;
                break;
        }
    }
    
    /**
     * Calculate the dimensions for an image within a grid cell.
     */
    private ImageDimensions calculateImageDimensions(ImageInfo image, float cellWidth, float cellHeight, LayoutConfig config) {
        if (image == null || !image.hasValidImage()) {
            return new ImageDimensions(cellWidth * 0.8f, cellHeight * 0.8f);
        }
        
        float imageWidth = image.originalWidth;
        float imageHeight = image.originalHeight;
        
        switch (config.aspectRatioMode) {
            case PRESERVE:
                // Scale to fit within cell while preserving aspect ratio
                float scaleX = cellWidth / imageWidth;
                float scaleY = cellHeight / imageHeight;
                float scale = Math.min(scaleX, scaleY) * 0.9f; // Leave 10% margin
                
                return new ImageDimensions(imageWidth * scale, imageHeight * scale);
                
            case STRETCH:
                // Stretch to fill cell
                return new ImageDimensions(cellWidth * 0.9f, cellHeight * 0.9f);
                
            case CROP:
                // Scale to fill cell, cropping if necessary
                float cropScaleX = cellWidth / imageWidth;
                float cropScaleY = cellHeight / imageHeight;
                float cropScale = Math.max(cropScaleX, cropScaleY) * 0.9f;
                
                return new ImageDimensions(imageWidth * cropScale, imageHeight * cropScale);
                
            default:
                return new ImageDimensions(cellWidth * 0.8f, cellHeight * 0.8f);
        }
    }
    
    /**
     * Helper class to hold grid dimensions.
     */
    private static class GridDimensions {
        final int rows;
        final int cols;
        
        GridDimensions(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
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