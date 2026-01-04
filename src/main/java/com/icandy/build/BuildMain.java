package com.icandy.build;

import java.io.IOException;

/**
 * BuildMain is the command-line entry point for the iCandy build phase.
 * It accepts a text file path and optional configuration file path,
 * then orchestrates the complete build workflow.
 * 
 * Usage:
 *   java -jar icandy.jar <text-file> [config-file]
 *   
 * Arguments:
 *   text-file    Path to the text script file to process (required)
 *   config-file  Path to the configuration JSON file (optional, default: ~/.icandy/config.json)
 */
public class BuildMain {
    
    private static final String DEFAULT_CONFIG_PATH = "~/.icandy/config.json";
    
    /**
     * Main entry point for the build phase.
     * 
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // Parse command-line arguments
        if (args.length < 1) {
            displayUsage();
            System.exit(1);
        }
        
        String textFilePath = args[0];
        String configFilePath = args.length > 1 ? args[1] : DEFAULT_CONFIG_PATH;
        
        // Validate text file path
        if (textFilePath == null || textFilePath.trim().isEmpty()) {
            System.err.println("Error: Text file path cannot be empty");
            displayUsage();
            System.exit(1);
        }
        
        try {
            // Create orchestrator
            BuildOrchestrator orchestrator = new BuildOrchestrator();
            
            // Load configuration
            System.out.println("Loading configuration from: " + configFilePath);
            orchestrator.loadConfiguration(configFilePath);
            System.out.println();
            
            // Run build
            orchestrator.runBuild(textFilePath);
            
            // Exit with success
            System.exit(0);
            
        } catch (IOException e) {
            System.err.println();
            System.err.println("=== Build Failed ===");
            System.err.println("Error: " + e.getMessage());
            
            // Provide helpful suggestions based on error type
            if (e.getMessage().contains("Configuration file not found")) {
                System.err.println();
                System.err.println("Suggestion: Create a configuration file at " + configFilePath);
                System.err.println("You can copy config.json.example as a starting point.");
            } else if (e.getMessage().contains("Text file not found")) {
                System.err.println();
                System.err.println("Suggestion: Check that the text file path is correct.");
            } else if (e.getMessage().contains("Unsplash credentials")) {
                System.err.println();
                System.err.println("Suggestion: Ensure your Unsplash API credentials are configured.");
                System.err.println("Create a file at ~/.icandy/unsplash.properties with:");
                System.err.println("  access_key=YOUR_ACCESS_KEY");
            }
            
            System.exit(1);
            
        } catch (Exception e) {
            System.err.println();
            System.err.println("=== Build Failed ===");
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Displays usage instructions for the command-line interface.
     */
    private static void displayUsage() {
        System.out.println();
        System.out.println("iCandy Build Phase - Visual Text Processor");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar icandy.jar <text-file> [config-file]");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  text-file    Path to the text script file to process (required)");
        System.out.println("  config-file  Path to the configuration JSON file");
        System.out.println("               (optional, default: ~/.icandy/config.json)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar icandy.jar my_script.txt");
        System.out.println("  java -jar icandy.jar my_script.txt custom_config.json");
        System.out.println("  java -jar icandy.jar ~/Documents/poem.txt ~/.icandy/config.json");
        System.out.println();
        System.out.println("Configuration:");
        System.out.println("  The configuration file should be a JSON file with build settings.");
        System.out.println("  See config.json.example for a template.");
        System.out.println();
        System.out.println("  Required configuration:");
        System.out.println("    - Unsplash API credentials (in unsplash.properties file)");
        System.out.println("    - Stop words file (data/stopwords.txt)");
        System.out.println();
        System.out.println("Output:");
        System.out.println("  - Downloaded images: data/images/");
        System.out.println("  - Word-image associations: data/associations.json");
        System.out.println();
    }
}
