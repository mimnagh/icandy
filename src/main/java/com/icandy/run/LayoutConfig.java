package com.icandy.run;

/**
 * Configuration class for layout algorithms.
 * Contains parameters for all supported layout types and global settings.
 */
public class LayoutConfig {
    
    // Global layout settings
    /** Display region where images can be positioned */
    public Rectangle displayRegion;
    
    /** Global scale factor applied to all images */
    public float globalScale;
    
    /** How to handle aspect ratios when scaling images */
    public AspectRatioMode aspectRatioMode;
    
    // Grid layout settings
    /** Number of rows in grid layout (0 = auto-calculate) */
    public int gridRows;
    
    /** Number of columns in grid layout (0 = auto-calculate) */
    public int gridCols;
    
    /** Spacing between grid cells */
    public float gridSpacing;
    
    /** Padding around the entire grid */
    public float gridPadding;
    
    /** Alignment of grid within display region */
    public Alignment gridAlignment;
    
    // Collage layout settings
    /** Minimum size factor for collage images (relative to original) */
    public float minSize;
    
    /** Maximum size factor for collage images (relative to original) */
    public float maxSize;
    
    /** Minimum rotation angle in degrees for collage */
    public float minRotation;
    
    /** Maximum rotation angle in degrees for collage */
    public float maxRotation;
    
    /** Amount of overlap allowed between images (0.0 = no overlap, 1.0 = full overlap) */
    public float overlapAmount;
    
    // Circular layout settings
    /** Radius of the circle for circular layout */
    public float circleRadius;
    
    /** Arc span in degrees (360 = full circle) */
    public float arcSpan;
    
    /** Direction of rotation for circular layout */
    public RotationDirection rotationDirection;
    
    // Flowing layout settings
    /** Curvature of the flow path (0.0 = straight, 1.0 = very curved) */
    public float pathCurvature;
    
    /** Direction of the flow */
    public FlowDirection flowDirection;
    
    /** Spacing between images along the path */
    public float pathSpacing;
    
    /**
     * Create a LayoutConfig with default values suitable for most use cases.
     */
    public LayoutConfig() {
        // Global defaults
        this.displayRegion = new Rectangle(0, 0, 1920, 1080);
        this.globalScale = 1.0f;
        this.aspectRatioMode = AspectRatioMode.PRESERVE;
        
        // Grid defaults
        this.gridRows = 0; // Auto-calculate
        this.gridCols = 0; // Auto-calculate
        this.gridSpacing = 20.0f;
        this.gridPadding = 40.0f;
        this.gridAlignment = Alignment.CENTER;
        
        // Collage defaults
        this.minSize = 0.3f;
        this.maxSize = 0.8f;
        this.minRotation = -15.0f;
        this.maxRotation = 15.0f;
        this.overlapAmount = 0.1f;
        
        // Circular defaults
        this.circleRadius = 300.0f;
        this.arcSpan = 360.0f;
        this.rotationDirection = RotationDirection.CLOCKWISE;
        
        // Flowing defaults
        this.pathCurvature = 0.5f;
        this.flowDirection = FlowDirection.HORIZONTAL;
        this.pathSpacing = 100.0f;
    }
    
    /**
     * Create a copy of this LayoutConfig.
     * 
     * @return A new LayoutConfig with the same values
     */
    public LayoutConfig copy() {
        LayoutConfig copy = new LayoutConfig();
        
        // Copy global settings
        copy.displayRegion = this.displayRegion.copy();
        copy.globalScale = this.globalScale;
        copy.aspectRatioMode = this.aspectRatioMode;
        
        // Copy grid settings
        copy.gridRows = this.gridRows;
        copy.gridCols = this.gridCols;
        copy.gridSpacing = this.gridSpacing;
        copy.gridPadding = this.gridPadding;
        copy.gridAlignment = this.gridAlignment;
        
        // Copy collage settings
        copy.minSize = this.minSize;
        copy.maxSize = this.maxSize;
        copy.minRotation = this.minRotation;
        copy.maxRotation = this.maxRotation;
        copy.overlapAmount = this.overlapAmount;
        
        // Copy circular settings
        copy.circleRadius = this.circleRadius;
        copy.arcSpan = this.arcSpan;
        copy.rotationDirection = this.rotationDirection;
        
        // Copy flowing settings
        copy.pathCurvature = this.pathCurvature;
        copy.flowDirection = this.flowDirection;
        copy.pathSpacing = this.pathSpacing;
        
        return copy;
    }
    
    /**
     * Validate and clamp configuration values to reasonable ranges.
     */
    public void validate() {
        // Clamp global scale
        globalScale = Math.max(0.1f, Math.min(5.0f, globalScale));
        
        // Clamp grid settings
        gridRows = Math.max(0, Math.min(10, gridRows));
        gridCols = Math.max(0, Math.min(10, gridCols));
        gridSpacing = Math.max(0.0f, Math.min(100.0f, gridSpacing));
        gridPadding = Math.max(0.0f, Math.min(200.0f, gridPadding));
        
        // Clamp collage settings
        minSize = Math.max(0.1f, Math.min(2.0f, minSize));
        maxSize = Math.max(minSize, Math.min(3.0f, maxSize));
        minRotation = Math.max(-180.0f, Math.min(180.0f, minRotation));
        maxRotation = Math.max(minRotation, Math.min(180.0f, maxRotation));
        overlapAmount = Math.max(0.0f, Math.min(1.0f, overlapAmount));
        
        // Clamp circular settings
        circleRadius = Math.max(50.0f, Math.min(1000.0f, circleRadius));
        arcSpan = Math.max(30.0f, Math.min(360.0f, arcSpan));
        
        // Clamp flowing settings
        pathCurvature = Math.max(0.0f, Math.min(2.0f, pathCurvature));
        pathSpacing = Math.max(10.0f, Math.min(500.0f, pathSpacing));
    }
    
    // Supporting enums and classes
    
    /**
     * Rectangle class for defining display regions.
     */
    public static class Rectangle {
        public float x, y, width, height;
        
        public Rectangle(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public Rectangle copy() {
            return new Rectangle(x, y, width, height);
        }
        
        public float getCenterX() {
            return x + width / 2.0f;
        }
        
        public float getCenterY() {
            return y + height / 2.0f;
        }
    }
    
    /**
     * How to handle aspect ratios when scaling images.
     */
    public enum AspectRatioMode {
        PRESERVE,    // Keep original aspect ratio
        STRETCH,     // Stretch to fit dimensions
        CROP         // Crop to fit dimensions
    }
    
    /**
     * Alignment options for grid layout.
     */
    public enum Alignment {
        CENTER,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
    
    /**
     * Rotation direction for circular layout.
     */
    public enum RotationDirection {
        CLOCKWISE,
        COUNTERCLOCKWISE
    }
    
    /**
     * Flow direction for flowing layout.
     */
    public enum FlowDirection {
        HORIZONTAL,
        VERTICAL,
        DIAGONAL_UP,
        DIAGONAL_DOWN
    }
}