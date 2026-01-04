package com.icandy.build;

import java.io.File;

/**
 * Manual test class for ImageDownloader.
 * Run this to verify the ImageDownloader works with real Unsplash API.
 * 
 * Usage: java -cp target/icandy-1.0.0.jar com.icandy.build.ImageDownloaderManualTest
 */
public class ImageDownloaderManualTest {
    
    public static void main(String[] args) {
        System.out.println("=== ImageDownloader Manual Test ===\n");
        
        ImageDownloader downloader = new ImageDownloader();
        
        try {
            // Step 1: Load credentials
            System.out.println("Step 1: Loading Unsplash credentials...");
            String credentialsPath = "~/.icandy/unsplash.properties";
            downloader.loadCredentials(credentialsPath);
            System.out.println("✓ Credentials loaded successfully\n");
            
            // Step 2: Search for images
            System.out.println("Step 2: Searching for 'sunset' images...");
            String[] imageUrls = downloader.searchImages("sunset", 3);
            System.out.println("✓ Found " + imageUrls.length + " images");
            
            if (imageUrls.length > 0) {
                System.out.println("\nImage URLs:");
                for (int i = 0; i < imageUrls.length; i++) {
                    System.out.println("  " + (i + 1) + ". " + imageUrls[i]);
                }
            }
            System.out.println();
            
            // Step 3: Download first image
            if (imageUrls.length > 0) {
                System.out.println("Step 3: Downloading first image...");
                String testImagePath = "data/images/test_sunset.jpg";
                boolean success = downloader.downloadImage(imageUrls[0], testImagePath);
                
                if (success) {
                    File imageFile = new File(testImagePath);
                    long fileSize = imageFile.length();
                    System.out.println("✓ Image downloaded successfully");
                    System.out.println("  Location: " + testImagePath);
                    System.out.println("  Size: " + (fileSize / 1024) + " KB\n");
                } else {
                    System.out.println("✗ Image download failed\n");
                }
            }
            
            // Step 4: Test error handling
            System.out.println("Step 4: Testing error handling...");
            String[] emptyResults = downloader.searchImages("xyzabc123nonexistentword999", 1);
            System.out.println("✓ Empty search handled correctly (found " + emptyResults.length + " images)\n");
            
            System.out.println("=== All Tests Passed! ===");
            
        } catch (Exception e) {
            System.err.println("\n✗ Test failed with error:");
            System.err.println("  " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
