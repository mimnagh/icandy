package com.icandy.run;

/**
 * Factory class for creating layout algorithm instances.
 * Provides a centralized way to create and switch between different layout algorithms.
 */
public class LayoutAlgorithmFactory {
    
    /**
     * Create a layout algorithm instance by name.
     * 
     * @param algorithmName The name of the algorithm ("grid", "collage", "circular", "flowing")
     * @return A new instance of the specified layout algorithm
     * @throws IllegalArgumentException if the algorithm name is not recognized
     */
    public static LayoutAlgorithm createAlgorithm(String algorithmName) {
        if (algorithmName == null) {
            throw new IllegalArgumentException("Algorithm name cannot be null");
        }
        
        switch (algorithmName.toLowerCase()) {
            case "grid":
                return new GridLayoutAlgorithm();
            case "collage":
                return new CollageLayoutAlgorithm();
            case "circular":
                return new CircularLayoutAlgorithm();
            case "flowing":
                return new FlowingLayoutAlgorithm();
            default:
                throw new IllegalArgumentException("Unknown layout algorithm: " + algorithmName);
        }
    }
    
    /**
     * Create a layout algorithm instance by name with a seed for reproducible results.
     * Only applies to algorithms that support seeding (like CollageLayoutAlgorithm).
     * 
     * @param algorithmName The name of the algorithm
     * @param seed Random seed for reproducible layouts
     * @return A new instance of the specified layout algorithm
     * @throws IllegalArgumentException if the algorithm name is not recognized
     */
    public static LayoutAlgorithm createAlgorithm(String algorithmName, long seed) {
        if (algorithmName == null) {
            throw new IllegalArgumentException("Algorithm name cannot be null");
        }
        
        switch (algorithmName.toLowerCase()) {
            case "grid":
                return new GridLayoutAlgorithm();
            case "collage":
                return new CollageLayoutAlgorithm(seed);
            case "circular":
                return new CircularLayoutAlgorithm();
            case "flowing":
                return new FlowingLayoutAlgorithm();
            default:
                throw new IllegalArgumentException("Unknown layout algorithm: " + algorithmName);
        }
    }
    
    /**
     * Get all available layout algorithm names.
     * 
     * @return Array of algorithm names
     */
    public static String[] getAvailableAlgorithms() {
        return new String[]{"grid", "collage", "circular", "flowing"};
    }
    
    /**
     * Check if an algorithm name is valid.
     * 
     * @param algorithmName The algorithm name to check
     * @return true if the algorithm is supported, false otherwise
     */
    public static boolean isValidAlgorithm(String algorithmName) {
        if (algorithmName == null) {
            return false;
        }
        
        String name = algorithmName.toLowerCase();
        return name.equals("grid") || name.equals("collage") || 
               name.equals("circular") || name.equals("flowing");
    }
}