package com.icandy.unit;

import com.icandy.build.ImageDownloader;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ImageDownloader rate limiting functionality.
 */
class ImageDownloaderRateLimitTest {
    
    private ImageDownloader downloader;
    
    @BeforeEach
    void setUp() {
        // Create a mock HTTP client that returns successful responses
        OkHttpClient mockClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request request = chain.request();
                
                // Mock successful search response
                String mockResponse = "{\"results\": [{\"urls\": {\"regular\": \"https://example.com/image.jpg\"}}]}";
                
                return new Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(mockResponse, MediaType.parse("application/json")))
                    .build();
            })
            .build();
        
        downloader = new ImageDownloader(mockClient);
        downloader.setApiKey("test-key");
    }
    
    @Test
    void testRequestCountIncrementsAfterSuccessfulSearch() throws IOException {
        assertEquals(0, downloader.getRequestCount(), "Initial request count should be 0");
        
        downloader.searchImages("test", 1);
        assertEquals(1, downloader.getRequestCount(), "Request count should be 1 after first search");
        
        downloader.searchImages("test", 1);
        assertEquals(2, downloader.getRequestCount(), "Request count should be 2 after second search");
    }
    
    @Test
    void testResetRateLimitTracking() throws IOException {
        downloader.searchImages("test", 1);
        downloader.searchImages("test", 1);
        assertEquals(2, downloader.getRequestCount(), "Request count should be 2");
        
        downloader.resetRateLimitTracking();
        assertEquals(0, downloader.getRequestCount(), "Request count should be 0 after reset");
    }
    
    @Test
    void testRequestCountDoesNotIncrementOnFailedSearch() {
        // Create a downloader without setting API key to cause failure
        ImageDownloader failingDownloader = new ImageDownloader();
        
        assertEquals(0, failingDownloader.getRequestCount(), "Initial request count should be 0");
        
        assertThrows(IOException.class, () -> {
            failingDownloader.searchImages("test", 1);
        });
        
        assertEquals(0, failingDownloader.getRequestCount(), 
            "Request count should still be 0 after failed search");
    }
}
