package com.icandy.build;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.icandy.common.Logger;
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
    private final Logger logger;
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
        this.logger = new Logger(ImageDownloader.class);
        this.maxRetries = 3;
        // Initialize rate limit window to 0 - will be set on first request
        this.rateLimitWindowStart = 0;
        this.requestCount = 0;
        
        logger.info("ImageDownloader initialized with default settings");
    }
    
    /**
     * Creates an ImageDownloader with a custom HTTP client.
     * 
     * @param httpClient Custom OkHttpClient instance
     */
    public ImageDownloader(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        this.logger = new Logger(ImageDownloader.class);
        this.maxRetries = 3;
        // Initialize rate limit window to 0 - will be set on first request
        this.rateLimitWindowStart = 0;
        this.requestCount = 0;
        
        logger.info("ImageDownloader initialized with custom HTTP client");
    }
    
    /**
     * Sets the Unsplash API access key for authentication.
     * 
     * @param accessKey The Unsplash API access key
     */
    public void setApiKey(String accessKey) {
        if (accessKey == null || accessKey.trim().isEmpty()) {
            logger.warning("Empty or null API key provided");
            this.accessKey = null;
        } else {
            this.accessKey = accessKey.trim();
            logger.info("API key set successfully");
        }
    }
    
    /**
     * Sets the maximum number of retries for failed requests.
     * 
     * @param maxRetries Maximum retry count
     */
    public void setMaxRetries(int maxRetries) {
        if (maxRetries < 0) {
            logger.warning("Invalid maxRetries value, using 0", String.format("value=%d", maxRetries));
            this.maxRetries = 0;
        } else {
            this.maxRetries = maxRetries;
            logger.info("Max retries set", String.format("maxRetries=%d", maxRetries));
        }
    }
    
    /**
     * Loads API credentials from a properties file.
     * Expected properties: access_key, application_id, secret_key
     * 
     * @param propertiesFilePath Path to the properties file
     * @throws IOException if the file cannot be read
     */
    public void loadCredentials(String propertiesFilePath) throws IOException {
        logger.info("Loading credentials from file", propertiesFilePath);
        
        try {
            // Expand ~ to user home directory
            String expandedPath = propertiesFilePath.replaceFirst("^~", System.getProperty("user.home"));
            Path path = Paths.get(expandedPath);
            
            if (!Files.exists(path)) {
                IOException e = new IOException("Credentials file not found: " + expandedPath);
                logger.error("Credentials file not found", expandedPath, e);
                throw e;
            }
            
            if (!Files.isReadable(path)) {
                IOException e = new IOException("Credentials file is not readable: " + expandedPath);
                logger.error("Credentials file not readable", expandedPath, e);
                throw e;
            }
            
            Properties props = new Properties();
            try (InputStream input = Files.newInputStream(path)) {
                props.load(input);
            } catch (IOException e) {
                logger.error("Failed to read properties file", expandedPath, e);
                throw new IOException("Failed to read properties file: " + e.getMessage(), e);
            }
            
            this.accessKey = props.getProperty("access_key");
            
            if (this.accessKey == null || this.accessKey.trim().isEmpty()) {
                IOException e = new IOException("access_key not found or empty in properties file");
                logger.error("Invalid credentials file", "Missing or empty access_key", e);
                throw e;
            }
            
            this.accessKey = this.accessKey.trim();
            logger.info("Credentials loaded successfully", expandedPath);
            
        } catch (IOException e) {
            logger.error("Failed to load credentials", propertiesFilePath, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error loading credentials", propertiesFilePath, e);
            throw new IOException("Unexpected error loading credentials: " + e.getMessage(), e);
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
        logger.info("Searching for images", String.format("query=%s, count=%d", query, count));
        
        // Validate inputs
        if (accessKey == null || accessKey.trim().isEmpty()) {
            IOException e = new IOException("API key not set. Call setApiKey() or loadCredentials() first.");
            logger.error("API key not configured", query, e);
            throw e;
        }
        
        if (query == null || query.trim().isEmpty()) {
            logger.warning("Empty search query provided", "Returning empty results");
            return new String[0];
        }
        
        if (count <= 0) {
            logger.warning("Invalid count provided", String.format("count=%d", count));
            return new String[0];
        }
        
        try {
            // Check rate limit before making request
            checkRateLimit();
            
            // Build the API URL
            HttpUrl url = HttpUrl.parse(UNSPLASH_API_BASE + SEARCH_ENDPOINT)
                .newBuilder()
                .addQueryParameter("query", query.trim())
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
                    return handleSearchResponse(response, count, query);
                }
            }, "search images for: " + query);
            
            // Increment request count after successful request
            incrementRequestCount();
            
            logger.info("Image search completed", 
                String.format("query=%s, found=%d images", query, results.length));
            return results;
            
        } catch (IOException e) {
            logger.error("Image search failed", query, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during image search", query, e);
            throw new IOException("Unexpected error during image search: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if we're approaching the rate limit and sleeps if necessary.
     * Unsplash free tier allows 50 requests per hour.
     */
    private void checkRateLimit() {
        // If this is the first request, initialize the window
        if (rateLimitWindowStart == 0) {
            rateLimitWindowStart = System.currentTimeMillis();
            logger.info("Rate limit tracking initialized");
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - rateLimitWindowStart;
        
        // If more than an hour has passed, reset the counter
        if (elapsedTime >= ONE_HOUR_MS) {
            requestCount = 0;
            rateLimitWindowStart = currentTime;
            logger.info("Rate limit window reset", String.format("previousCount=%d", requestCount));
            return;
        }
        
        // If we've hit the limit, wait until the hour is up
        if (requestCount >= UNSPLASH_HOURLY_LIMIT) {
            long remainingTime = ONE_HOUR_MS - elapsedTime;
            long remainingMinutes = remainingTime / (60 * 1000);
            long remainingSeconds = (remainingTime % (60 * 1000)) / 1000;
            
            logger.warning("Rate limit reached, waiting for reset", 
                String.format("requests=%d, waitTime=%dm %ds", requestCount, remainingMinutes, remainingSeconds));
            
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
                logger.error("Interrupted while waiting for rate limit reset", "", e);
                throw new RuntimeException("Interrupted while waiting for rate limit reset", e);
            }
            
            // Reset counter after waiting
            requestCount = 0;
            rateLimitWindowStart = System.currentTimeMillis();
            
            System.out.println("Rate limit window reset. Resuming downloads...");
            System.out.println();
            logger.info("Rate limit wait completed, resuming operations");
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
    private String[] handleSearchResponse(Response response, int count, String query) throws IOException {
        if (!response.isSuccessful()) {
            if (response.code() == 429 || response.code() == 403) {
                // Rate limit exceeded (429 is standard, but Unsplash also uses 403)
                RateLimitException e = new RateLimitException("Unsplash API rate limit exceeded (code: " + response.code() + ")");
                logger.warning("API rate limit exceeded", String.format("query=%s, code=%d", query, response.code()));
                throw e;
            } else if (response.code() == 401) {
                IOException e = new IOException("Unauthorized: Invalid API key");
                logger.error("API authentication failed", String.format("query=%s, code=%d", query, response.code()), e);
                throw e;
            } else {
                IOException e = new IOException("API request failed with code: " + response.code());
                logger.error("API request failed", String.format("query=%s, code=%d", query, response.code()), e);
                throw e;
            }
        }
        
        String responseBody;
        try {
            responseBody = response.body().string();
        } catch (IOException e) {
            logger.error("Failed to read API response body", query, e);
            throw new IOException("Failed to read API response: " + e.getMessage(), e);
        }
        
        JsonObject jsonResponse;
        try {
            jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            logger.error("Invalid JSON in API response", query, e);
            throw new IOException("Invalid JSON in API response: " + e.getMessage(), e);
        }
        
        JsonArray results = jsonResponse.getAsJsonArray("results");
        
        if (results == null || results.size() == 0) {
            logger.info("No images found in API response", query);
            return new String[0];
        }
        
        List<String> imageUrls = new ArrayList<>();
        int limit = Math.min(count, results.size());
        
        for (int i = 0; i < limit; i++) {
            try {
                JsonObject result = results.get(i).getAsJsonObject();
                JsonObject urls = result.getAsJsonObject("urls");
                
                // Use "regular" size for good quality without huge file sizes
                String imageUrl = urls.get("regular").getAsString();
                imageUrls.add(imageUrl);
            } catch (Exception e) {
                logger.warning("Failed to parse image URL from result", 
                    String.format("query=%s, index=%d, error=%s", query, i, e.getMessage()));
                // Continue with other results
            }
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
            logger.warning("Empty image URL provided for download", localPath);
            return false;
        }
        
        if (localPath == null || localPath.trim().isEmpty()) {
            logger.warning("Empty local path provided for download", imageUrl);
            return false;
        }
        
        logger.info("Starting image download", String.format("url=%s, path=%s", imageUrl, localPath));
        
        try {
            // Ensure parent directory exists
            Path path = Paths.get(localPath);
            Path parentDir = path.getParent();
            if (parentDir != null) {
                try {
                    Files.createDirectories(parentDir);
                } catch (IOException e) {
                    logger.error("Failed to create parent directory", parentDir.toString(), e);
                    return false;
                }
            }
            
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
                        long totalBytes = 0;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            totalBytes += bytesRead;
                        }
                        
                        logger.info("Image download completed", 
                            String.format("path=%s, size=%d bytes", localPath, totalBytes));
                    }
                    
                    return true;
                }
            }, "download image: " + imageUrl);
            
        } catch (IOException e) {
            logger.error("Image download failed", String.format("url=%s, path=%s", imageUrl, localPath), e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error during image download", 
                String.format("url=%s, path=%s", imageUrl, localPath), e);
            return false;
        }
    }
    
    /**
     * Executes an operation with retry logic for handling transient failures.
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation, String operationName) throws IOException {
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
                
                logger.networkRetry(operationName, rateLimitAttempts, MAX_RATE_LIMIT_RETRIES, "Rate limit exceeded");
                
                if (rateLimitAttempts >= MAX_RATE_LIMIT_RETRIES) {
                    // Don't keep retrying rate limits - fail fast
                    logger.error("Rate limit retry limit exceeded", 
                        String.format("operation=%s, attempts=%d", operationName, rateLimitAttempts), e);
                    throw e;
                }
                
                if (attempts < maxRetries) {
                    int delayMs = RATE_LIMIT_RETRY_DELAY_MS * rateLimitAttempts; // Linear backoff
                    logger.info("Rate limit retry delay", 
                        String.format("operation=%s, delayMs=%d", operationName, delayMs));
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
                
                logger.networkRetry(operationName, attempts, maxRetries, e.getMessage());
                
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
        logger.error("All retries exhausted", 
            String.format("operation=%s, attempts=%d", operationName, attempts), lastException);
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
