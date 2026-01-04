package com.icandy.build;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * ImageDownloader handles communication with the Unsplash API and downloads images.
 * It authenticates with the API, searches for images by keyword, and saves them locally.
 */
public class ImageDownloader {
    
    private static final String UNSPLASH_API_BASE = "https://api.unsplash.com";
    private static final String SEARCH_ENDPOINT = "/search/photos";
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int RATE_LIMIT_RETRY_DELAY_MS = 5000; // 5 seconds initial delay
    private static final int MAX_RATE_LIMIT_RETRIES = 2; // Limit rate limit retries
    private static final int UNSPLASH_HOURLY_LIMIT = 50; // Unsplash free tier limit
    private static final long ONE_HOUR_MS = 60 * 60 * 1000; // 1 hour in milliseconds
    
    private final OkHttpClient httpClient;
    private String accessKey;
    private int maxRetries;
    
    // Rate limit tracking
    private int requestCount = 0;
    private long rateLimitWindowStart = 0;
    
    /**
     * Creates an ImageDownloader with default settings.
     */
    public ImageDownloader() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
        this.maxRetries = 3;
        // Initialize rate limit window to 0 - will be set on first request
        this.rateLimitWindowStart = 0;
        this.requestCount = 0;
    }
    
    /**
     * Creates an ImageDownloader with a custom HTTP client.
     * 
     * @param httpClient Custom OkHttpClient instance
     */
    public ImageDownloader(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        this.maxRetries = 3;
        // Initialize rate limit window to 0 - will be set on first request
        this.rateLimitWindowStart = 0;
        this.requestCount = 0;
    }
    
    /**
     * Sets the Unsplash API access key for authentication.
     * 
     * @param accessKey The Unsplash API access key
     */
    public void setApiKey(String accessKey) {
        this.accessKey = accessKey;
    }
    
    /**
     * Sets the maximum number of retries for failed requests.
     * 
     * @param maxRetries Maximum retry count
     */
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    /**
     * Loads API credentials from a properties file.
     * Expected properties: access_key, application_id, secret_key
     * 
     * @param propertiesFilePath Path to the properties file
     * @throws IOException if the file cannot be read
     */
    public void loadCredentials(String propertiesFilePath) throws IOException {
        // Expand ~ to user home directory
        String expandedPath = propertiesFilePath.replaceFirst("^~", System.getProperty("user.home"));
        Path path = Paths.get(expandedPath);
        
        if (!Files.exists(path)) {
            throw new IOException("Credentials file not found: " + expandedPath);
        }
        
        Properties props = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            props.load(input);
        }
        
        this.accessKey = props.getProperty("access_key");
        
        if (this.accessKey == null || this.accessKey.trim().isEmpty()) {
            throw new IOException("access_key not found in properties file");
        }
    }
    
    /**
     * Searches for images on Unsplash by query term.
     * Returns URLs of images that can be downloaded.
     * 
     * @param query Search term (e.g., a word from the text)
     * @param count Number of images to retrieve
     * @return Array of image URLs
     * @throws IOException if the API request fails
     */
    public String[] searchImages(String query, int count) throws IOException {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            throw new IOException("API key not set. Call setApiKey() or loadCredentials() first.");
        }
        
        if (query == null || query.trim().isEmpty()) {
            return new String[0];
        }
        
        if (count <= 0) {
            return new String[0];
        }
        
        // Check rate limit before making request
        checkRateLimit();
        
        // Build the API URL
        HttpUrl url = HttpUrl.parse(UNSPLASH_API_BASE + SEARCH_ENDPOINT)
            .newBuilder()
            .addQueryParameter("query", query)
            .addQueryParameter("per_page", String.valueOf(Math.min(count, 30))) // API max is 30
            .build();
        
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Client-ID " + accessKey)
            .addHeader("Accept-Version", "v1")
            .build();
        
        // Execute request with retry logic
        String[] results = executeWithRetry(() -> {
            try (Response response = httpClient.newCall(request).execute()) {
                return handleSearchResponse(response, count);
            }
        });
        
        // Increment request count after successful request
        incrementRequestCount();
        
        return results;
    }
    
    /**
     * Checks if we're approaching the rate limit and sleeps if necessary.
     * Unsplash free tier allows 50 requests per hour.
     */
    private void checkRateLimit() {
        // If this is the first request, initialize the window
        if (rateLimitWindowStart == 0) {
            rateLimitWindowStart = System.currentTimeMillis();
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - rateLimitWindowStart;
        
        // If more than an hour has passed, reset the counter
        if (elapsedTime >= ONE_HOUR_MS) {
            requestCount = 0;
            rateLimitWindowStart = currentTime;
            return;
        }
        
        // If we've hit the limit, wait until the hour is up
        if (requestCount >= UNSPLASH_HOURLY_LIMIT) {
            long remainingTime = ONE_HOUR_MS - elapsedTime;
            long remainingMinutes = remainingTime / (60 * 1000);
            long remainingSeconds = (remainingTime % (60 * 1000)) / 1000;
            
            System.out.println();
            System.out.println("========================================");
            System.out.println("RATE LIMIT REACHED");
            System.out.println("========================================");
            System.out.println("Unsplash free tier allows " + UNSPLASH_HOURLY_LIMIT + " requests per hour.");
            System.out.println("We've made " + requestCount + " requests in this session.");
            System.out.println("Waiting " + remainingMinutes + " minutes and " + remainingSeconds + " seconds");
            System.out.println("until the rate limit window resets...");
            System.out.println("========================================");
            System.out.println();
            
            try {
                Thread.sleep(remainingTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for rate limit reset", e);
            }
            
            // Reset counter after waiting
            requestCount = 0;
            rateLimitWindowStart = System.currentTimeMillis();
            
            System.out.println("Rate limit window reset. Resuming downloads...");
            System.out.println();
        }
    }
    
    /**
     * Increments the request count for rate limit tracking.
     */
    private void incrementRequestCount() {
        requestCount++;
    }
    
    /**
     * Gets the current request count (for testing purposes).
     */
    public int getRequestCount() {
        return requestCount;
    }
    
    /**
     * Resets the rate limit tracking (for testing purposes).
     */
    public void resetRateLimitTracking() {
        requestCount = 0;
        rateLimitWindowStart = System.currentTimeMillis();
    }
    
    /**
     * Handles the response from the Unsplash search API.
     */
    private String[] handleSearchResponse(Response response, int count) throws IOException {
        if (!response.isSuccessful()) {
            if (response.code() == 429 || response.code() == 403) {
                // Rate limit exceeded (429 is standard, but Unsplash also uses 403)
                throw new RateLimitException("Unsplash API rate limit exceeded (code: " + response.code() + ")");
            } else if (response.code() == 401) {
                throw new IOException("Unauthorized: Invalid API key");
            } else {
                throw new IOException("API request failed with code: " + response.code());
            }
        }
        
        String responseBody = response.body().string();
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray results = jsonResponse.getAsJsonArray("results");
        
        if (results == null || results.size() == 0) {
            return new String[0];
        }
        
        List<String> imageUrls = new ArrayList<>();
        int limit = Math.min(count, results.size());
        
        for (int i = 0; i < limit; i++) {
            JsonObject result = results.get(i).getAsJsonObject();
            JsonObject urls = result.getAsJsonObject("urls");
            
            // Use "regular" size for good quality without huge file sizes
            String imageUrl = urls.get("regular").getAsString();
            imageUrls.add(imageUrl);
        }
        
        return imageUrls.toArray(new String[0]);
    }
    
    /**
     * Downloads an image from a URL and saves it to a local path.
     * 
     * @param imageUrl URL of the image to download
     * @param localPath Local file path where the image should be saved
     * @return true if download succeeded, false otherwise
     */
    public boolean downloadImage(String imageUrl, String localPath) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }
        
        if (localPath == null || localPath.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Ensure parent directory exists
            Path path = Paths.get(localPath);
            Files.createDirectories(path.getParent());
            
            Request request = new Request.Builder()
                .url(imageUrl)
                .build();
            
            // Execute download with retry logic
            return executeWithRetry(() -> {
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Download failed with code: " + response.code());
                    }
                    
                    // Write image data to file
                    try (InputStream inputStream = response.body().byteStream();
                         FileOutputStream outputStream = new FileOutputStream(localPath)) {
                        
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    return true;
                }
            });
            
        } catch (IOException e) {
            System.err.println("Failed to download image from " + imageUrl + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Executes an operation with retry logic for handling transient failures.
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation) throws IOException {
        int attempts = 0;
        int rateLimitAttempts = 0;
        IOException lastException = null;
        
        while (attempts < maxRetries) {
            try {
                return operation.execute();
            } catch (RateLimitException e) {
                // Rate limit - use limited retries with longer backoff
                rateLimitAttempts++;
                attempts++;
                lastException = e;
                
                if (rateLimitAttempts >= MAX_RATE_LIMIT_RETRIES) {
                    // Don't keep retrying rate limits - fail fast
                    System.err.println("Rate limit exceeded after " + rateLimitAttempts + " attempts. " +
                                     "Unsplash free tier allows 50 requests per hour.");
                    throw e;
                }
                
                if (attempts < maxRetries) {
                    int delayMs = RATE_LIMIT_RETRY_DELAY_MS * rateLimitAttempts; // Linear backoff
                    System.err.println("Rate limit hit, waiting " + (delayMs / 1000) + " seconds before retry...");
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry delay", ie);
                    }
                }
            } catch (IOException e) {
                // Other IO errors - retry with shorter delay
                attempts++;
                lastException = e;
                
                if (attempts < maxRetries) {
                    try {
                        Thread.sleep(1000); // Brief delay before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry delay", ie);
                    }
                }
            }
        }
        
        // All retries exhausted
        throw lastException != null ? lastException : new IOException("Operation failed after " + maxRetries + " attempts");
    }
    
    /**
     * Functional interface for operations that can be retried.
     */
    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws IOException;
    }
    
    /**
     * Custom exception for rate limit errors.
     */
    private static class RateLimitException extends IOException {
        public RateLimitException(String message) {
            super(message);
        }
    }
}
