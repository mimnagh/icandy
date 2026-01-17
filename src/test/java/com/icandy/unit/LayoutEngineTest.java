package com.icandy.unit;

import com.icandy.run.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LayoutEngine and related classes.
 */
public class LayoutEngineTest {
    
    private LayoutEngine layoutEngine;
    private LayoutConfig config;
    
    @BeforeEach
    void setUp() {
        layoutEngine = new LayoutEngine();
        config = new LayoutConfig();
    }
    
    @Test
    void testLayoutEngineCreation() {
        assertNotNull(layoutEngine);
        assertNotNull(layoutEngine.getConfig());
        assertFalse(layoutEngine.isTransitioning());
        assertEquals(0.0f, layoutEngine.getTransitionProgress());
    }
    
    @Test
    void testLayoutConfigDefaults() {
        assertNotNull(config.displayRegion);
        assertEquals(1920, config.displayRegion.width);
        assertEquals(1080, config.displayRegion.height);
        assertEquals(1.0f, config.globalScale);
        assertEquals(LayoutConfig.AspectRatioMode.PRESERVE, config.aspectRatioMode);
        
        // Grid defaults
        assertEquals(0, config.gridRows);
        assertEquals(0, config.gridCols);
        assertEquals(20.0f, config.gridSpacing);
        assertEquals(40.0f, config.gridPadding);
        assertEquals(LayoutConfig.Alignment.CENTER, config.gridAlignment);
        
        // Collage defaults
        assertEquals(0.3f, config.minSize);
        assertEquals(0.8f, config.maxSize);
        assertEquals(-15.0f, config.minRotation);
        assertEquals(15.0f, config.maxRotation);
        assertEquals(0.1f, config.overlapAmount);
        
        // Circular defaults
        assertEquals(300.0f, config.circleRadius);
        assertEquals(360.0f, config.arcSpan);
        assertEquals(LayoutConfig.RotationDirection.CLOCKWISE, config.rotationDirection);
        
        // Flowing defaults
        assertEquals(0.5f, config.pathCurvature);
        assertEquals(LayoutConfig.FlowDirection.HORIZONTAL, config.flowDirection);
        assertEquals(100.0f, config.pathSpacing);
    }
    
    @Test
    void testLayoutConfigValidation() {
        // Test invalid values that should be clamped
        config.globalScale = -1.0f;
        config.gridRows = 20;
        config.minSize = -0.5f;
        config.maxSize = 10.0f;
        config.circleRadius = 10.0f;
        config.overlapAmount = 2.0f;
        
        config.validate();
        
        assertEquals(0.1f, config.globalScale); // Clamped to minimum
        assertEquals(10, config.gridRows); // Clamped to maximum
        assertEquals(0.1f, config.minSize); // Clamped to minimum
        assertEquals(3.0f, config.maxSize); // Clamped to maximum
        assertEquals(50.0f, config.circleRadius); // Clamped to minimum
        assertEquals(1.0f, config.overlapAmount); // Clamped to maximum
    }
    
    @Test
    void testLayoutConfigCopy() {
        config.globalScale = 2.0f;
        config.gridSpacing = 50.0f;
        config.minSize = 0.5f;
        
        LayoutConfig copy = config.copy();
        
        assertNotSame(config, copy);
        assertEquals(config.globalScale, copy.globalScale);
        assertEquals(config.gridSpacing, copy.gridSpacing);
        assertEquals(config.minSize, copy.minSize);
        
        // Verify deep copy of display region
        assertNotSame(config.displayRegion, copy.displayRegion);
        assertEquals(config.displayRegion.width, copy.displayRegion.width);
    }
    
    @Test
    void testImageInfoCreation() {
        ImageInfo imageInfo = new ImageInfo(null, "test", "test.jpg", 100, 200);
        
        assertEquals("test", imageInfo.word);
        assertEquals("test.jpg", imageInfo.filePath);
        assertEquals(100, imageInfo.originalWidth);
        assertEquals(200, imageInfo.originalHeight);
        assertEquals(0.5f, imageInfo.aspectRatio, 0.001f);
        assertFalse(imageInfo.hasValidImage());
    }
    
    @Test
    void testImagePositionCreation() {
        ImagePosition position = new ImagePosition(10, 20, 100, 200);
        
        assertEquals(10, position.x);
        assertEquals(20, position.y);
        assertEquals(100, position.width);
        assertEquals(200, position.height);
        assertEquals(0.0f, position.rotation);
        assertEquals(1.0f, position.scale);
        assertEquals(1.0f, position.opacity);
        
        assertEquals(60, position.getCenterX()); // 10 + 100/2
        assertEquals(120, position.getCenterY()); // 20 + 200/2
    }
    
    @Test
    void testImagePositionCopy() {
        ImagePosition original = new ImagePosition(10, 20, 100, 200, 45, 0.5f, 0.8f);
        ImagePosition copy = original.copy();
        
        assertNotSame(original, copy);
        assertEquals(original.x, copy.x);
        assertEquals(original.y, copy.y);
        assertEquals(original.width, copy.width);
        assertEquals(original.height, copy.height);
        assertEquals(original.rotation, copy.rotation);
        assertEquals(original.scale, copy.scale);
        assertEquals(original.opacity, copy.opacity);
    }
    
    @Test
    void testImagePositionTransform() {
        ImagePosition position = new ImagePosition(0, 0, 100, 100, 0, 1.0f, 1.0f);
        
        position.applyTransform(90, 2.0f, 0.5f);
        
        assertEquals(90, position.rotation);
        assertEquals(2.0f, position.scale);
        assertEquals(0.5f, position.opacity);
    }
    
    @Test
    void testLayoutEngineSetConfig() {
        LayoutConfig newConfig = new LayoutConfig();
        newConfig.globalScale = 2.0f;
        newConfig.gridSpacing = 30.0f;
        
        layoutEngine.setConfig(newConfig);
        
        LayoutConfig retrievedConfig = layoutEngine.getConfig();
        assertEquals(2.0f, retrievedConfig.globalScale);
        assertEquals(30.0f, retrievedConfig.gridSpacing);
        
        // Verify it's a copy, not the same object
        assertNotSame(newConfig, retrievedConfig);
    }
    
    @Test
    void testLayoutEngineWithNullAlgorithm() {
        ImageInfo[] images = {
            new ImageInfo(null, "test1", "test1.jpg", 100, 100),
            new ImageInfo(null, "test2", "test2.jpg", 200, 150)
        };
        
        ImagePosition[] positions = layoutEngine.calculateLayout(images);
        
        assertEquals(0, positions.length);
    }
    
    @Test
    void testLayoutEngineWithEmptyImages() {
        // Set a mock algorithm
        layoutEngine.setAlgorithm(new MockLayoutAlgorithm());
        
        ImageInfo[] emptyImages = {};
        ImagePosition[] positions = layoutEngine.calculateLayout(emptyImages);
        
        assertEquals(0, positions.length);
    }
    
    @Test
    void testLayoutEngineTransition() {
        ImagePosition[] targetPositions = {
            new ImagePosition(100, 100, 50, 50),
            new ImagePosition(200, 200, 60, 60)
        };
        
        layoutEngine.animateToNewLayout(targetPositions, 1000);
        
        assertTrue(layoutEngine.isTransitioning());
        assertEquals(0.0f, layoutEngine.getTransitionProgress());
        
        // Complete the transition
        layoutEngine.completeTransition();
        
        assertFalse(layoutEngine.isTransitioning());
        assertEquals(1.0f, layoutEngine.getTransitionProgress());
    }
    
    @Test
    void testRectangleClass() {
        LayoutConfig.Rectangle rect = new LayoutConfig.Rectangle(10, 20, 100, 200);
        
        assertEquals(10, rect.x);
        assertEquals(20, rect.y);
        assertEquals(100, rect.width);
        assertEquals(200, rect.height);
        assertEquals(60, rect.getCenterX()); // 10 + 100/2
        assertEquals(120, rect.getCenterY()); // 20 + 200/2
        
        LayoutConfig.Rectangle copy = rect.copy();
        assertNotSame(rect, copy);
        assertEquals(rect.x, copy.x);
        assertEquals(rect.y, copy.y);
        assertEquals(rect.width, copy.width);
        assertEquals(rect.height, copy.height);
    }
    
    /**
     * Mock layout algorithm for testing.
     */
    private static class MockLayoutAlgorithm implements LayoutAlgorithm {
        @Override
        public ImagePosition[] calculatePositions(ImageInfo[] images, LayoutConfig config) {
            ImagePosition[] positions = new ImagePosition[images.length];
            for (int i = 0; i < images.length; i++) {
                positions[i] = new ImagePosition(i * 100, i * 50, 80, 60);
            }
            return positions;
        }
        
        @Override
        public void updatePositions(ImagePosition[] positions, float deltaTime) {
            // Mock implementation - no updates needed for test
        }
    }
    
    @Test
    void testGridLayoutAlgorithm() {
        GridLayoutAlgorithm gridAlgorithm = new GridLayoutAlgorithm();
        ImageInfo[] images = {
            new ImageInfo(null, "test1", "test1.jpg", 100, 100),
            new ImageInfo(null, "test2", "test2.jpg", 100, 100),
            new ImageInfo(null, "test3", "test3.jpg", 100, 100),
            new ImageInfo(null, "test4", "test4.jpg", 100, 100)
        };
        
        ImagePosition[] positions = gridAlgorithm.calculatePositions(images, config);
        
        assertEquals(4, positions.length);
        
        // Verify all positions are within display region
        for (ImagePosition pos : positions) {
            assertTrue(pos.x >= config.displayRegion.x);
            assertTrue(pos.y >= config.displayRegion.y);
            assertTrue(pos.x + pos.width <= config.displayRegion.x + config.displayRegion.width);
            assertTrue(pos.y + pos.height <= config.displayRegion.y + config.displayRegion.height);
        }
        
        // Verify positions are different (not all at same location)
        boolean foundDifferentPositions = false;
        for (int i = 1; i < positions.length; i++) {
            if (positions[i].x != positions[0].x || positions[i].y != positions[0].y) {
                foundDifferentPositions = true;
                break;
            }
        }
        assertTrue(foundDifferentPositions, "Grid should position images at different locations");
    }
    
    @Test
    void testCollageLayoutAlgorithm() {
        CollageLayoutAlgorithm collageAlgorithm = new CollageLayoutAlgorithm(12345L); // Use seed for reproducible results
        ImageInfo[] images = {
            new ImageInfo(null, "test1", "test1.jpg", 100, 100),
            new ImageInfo(null, "test2", "test2.jpg", 150, 100),
            new ImageInfo(null, "test3", "test3.jpg", 100, 150)
        };
        
        ImagePosition[] positions = collageAlgorithm.calculatePositions(images, config);
        
        assertEquals(3, positions.length);
        
        // Verify all positions are within reasonable bounds
        for (ImagePosition pos : positions) {
            assertTrue(pos.width > 0, "Image width should be positive");
            assertTrue(pos.height > 0, "Image height should be positive");
            assertTrue(pos.rotation >= config.minRotation, "Rotation should be within configured range");
            assertTrue(pos.rotation <= config.maxRotation, "Rotation should be within configured range");
        }
        
        // Verify size variation (collage should have different sizes)
        boolean foundSizeVariation = false;
        for (int i = 1; i < positions.length; i++) {
            if (Math.abs(positions[i].width - positions[0].width) > 1.0f ||
                Math.abs(positions[i].height - positions[0].height) > 1.0f) {
                foundSizeVariation = true;
                break;
            }
        }
        assertTrue(foundSizeVariation, "Collage should have size variation");
    }
    
    @Test
    void testCircularLayoutAlgorithm() {
        CircularLayoutAlgorithm circularAlgorithm = new CircularLayoutAlgorithm();
        ImageInfo[] images = {
            new ImageInfo(null, "test1", "test1.jpg", 100, 100),
            new ImageInfo(null, "test2", "test2.jpg", 100, 100),
            new ImageInfo(null, "test3", "test3.jpg", 100, 100),
            new ImageInfo(null, "test4", "test4.jpg", 100, 100)
        };
        
        ImagePosition[] positions = circularAlgorithm.calculatePositions(images, config);
        
        assertEquals(4, positions.length);
        
        // Calculate center of display region
        float centerX = config.displayRegion.getCenterX();
        float centerY = config.displayRegion.getCenterY();
        
        // Verify images are positioned around the center
        for (ImagePosition pos : positions) {
            float imageCenterX = pos.getCenterX();
            float imageCenterY = pos.getCenterY();
            
            // Calculate distance from center
            float distance = (float) Math.sqrt(
                Math.pow(imageCenterX - centerX, 2) + Math.pow(imageCenterY - centerY, 2)
            );
            
            // Should be approximately at the configured radius (with some tolerance for image size)
            assertTrue(distance >= config.circleRadius * 0.5f, "Images should be positioned around the circle");
            assertTrue(distance <= config.circleRadius * 1.5f, "Images should not be too far from the circle");
        }
    }
    
    @Test
    void testFlowingLayoutAlgorithm() {
        FlowingLayoutAlgorithm flowingAlgorithm = new FlowingLayoutAlgorithm();
        ImageInfo[] images = {
            new ImageInfo(null, "test1", "test1.jpg", 100, 100),
            new ImageInfo(null, "test2", "test2.jpg", 100, 100),
            new ImageInfo(null, "test3", "test3.jpg", 100, 100)
        };
        
        ImagePosition[] positions = flowingAlgorithm.calculatePositions(images, config);
        
        assertEquals(3, positions.length);
        
        // Verify all positions are within display region bounds
        for (ImagePosition pos : positions) {
            assertTrue(pos.width > 0, "Image width should be positive");
            assertTrue(pos.height > 0, "Image height should be positive");
        }
        
        // For horizontal flow, images should be distributed along X axis
        if (config.flowDirection == LayoutConfig.FlowDirection.HORIZONTAL) {
            // Verify X positions are different and generally increasing
            boolean foundXVariation = false;
            for (int i = 1; i < positions.length; i++) {
                if (Math.abs(positions[i].x - positions[i-1].x) > 10.0f) {
                    foundXVariation = true;
                    break;
                }
            }
            assertTrue(foundXVariation, "Horizontal flow should distribute images along X axis");
        }
    }
    
    @Test
    void testLayoutAlgorithmFactory() {
        // Test valid algorithm creation
        LayoutAlgorithm gridAlgorithm = LayoutAlgorithmFactory.createAlgorithm("grid");
        assertNotNull(gridAlgorithm);
        assertTrue(gridAlgorithm instanceof GridLayoutAlgorithm);
        
        LayoutAlgorithm collageAlgorithm = LayoutAlgorithmFactory.createAlgorithm("collage");
        assertNotNull(collageAlgorithm);
        assertTrue(collageAlgorithm instanceof CollageLayoutAlgorithm);
        
        LayoutAlgorithm circularAlgorithm = LayoutAlgorithmFactory.createAlgorithm("circular");
        assertNotNull(circularAlgorithm);
        assertTrue(circularAlgorithm instanceof CircularLayoutAlgorithm);
        
        LayoutAlgorithm flowingAlgorithm = LayoutAlgorithmFactory.createAlgorithm("flowing");
        assertNotNull(flowingAlgorithm);
        assertTrue(flowingAlgorithm instanceof FlowingLayoutAlgorithm);
        
        // Test case insensitive
        LayoutAlgorithm gridAlgorithm2 = LayoutAlgorithmFactory.createAlgorithm("GRID");
        assertNotNull(gridAlgorithm2);
        assertTrue(gridAlgorithm2 instanceof GridLayoutAlgorithm);
        
        // Test invalid algorithm
        assertThrows(IllegalArgumentException.class, () -> {
            LayoutAlgorithmFactory.createAlgorithm("invalid");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            LayoutAlgorithmFactory.createAlgorithm(null);
        });
        
        // Test validation
        assertTrue(LayoutAlgorithmFactory.isValidAlgorithm("grid"));
        assertTrue(LayoutAlgorithmFactory.isValidAlgorithm("COLLAGE"));
        assertFalse(LayoutAlgorithmFactory.isValidAlgorithm("invalid"));
        assertFalse(LayoutAlgorithmFactory.isValidAlgorithm(null));
        
        // Test available algorithms
        String[] algorithms = LayoutAlgorithmFactory.getAvailableAlgorithms();
        assertEquals(4, algorithms.length);
        assertTrue(java.util.Arrays.asList(algorithms).contains("grid"));
        assertTrue(java.util.Arrays.asList(algorithms).contains("collage"));
        assertTrue(java.util.Arrays.asList(algorithms).contains("circular"));
        assertTrue(java.util.Arrays.asList(algorithms).contains("flowing"));
    }
    
    @Test
    void testLayoutEngineWithRealAlgorithm() {
        GridLayoutAlgorithm gridAlgorithm = new GridLayoutAlgorithm();
        layoutEngine.setAlgorithm(gridAlgorithm);
        
        ImageInfo[] images = {
            new ImageInfo(null, "test1", "test1.jpg", 100, 100),
            new ImageInfo(null, "test2", "test2.jpg", 100, 100)
        };
        
        ImagePosition[] positions = layoutEngine.calculateLayout(images);
        
        assertEquals(2, positions.length);
        assertNotNull(positions[0]);
        assertNotNull(positions[1]);
        
        // Verify positions are stored as current positions
        ImagePosition[] currentPositions = layoutEngine.getCurrentPositions();
        assertEquals(2, currentPositions.length);
    }
    
    @Test
    void testLayoutEngineTransitionWithRealPositions() {
        layoutEngine.setAlgorithm(new GridLayoutAlgorithm());
        
        ImageInfo[] images = {
            new ImageInfo(null, "test1", "test1.jpg", 100, 100)
        };
        
        // Calculate initial positions
        ImagePosition[] initialPositions = layoutEngine.calculateLayout(images);
        assertEquals(1, initialPositions.length);
        
        // Create target positions for transition
        ImagePosition[] targetPositions = {
            new ImagePosition(200, 200, 100, 100)
        };
        
        layoutEngine.animateToNewLayout(targetPositions, 1000); // Longer duration
        
        assertTrue(layoutEngine.isTransitioning(), "Should be transitioning after animateToNewLayout");
        
        // Complete transition immediately for testing
        layoutEngine.completeTransition();
        
        assertFalse(layoutEngine.isTransitioning());
        assertEquals(1.0f, layoutEngine.getTransitionProgress());
        
        ImagePosition[] finalPositions = layoutEngine.getCurrentPositions();
        assertEquals(200, finalPositions[0].x, 0.1f);
        assertEquals(200, finalPositions[0].y, 0.1f);
    }
}