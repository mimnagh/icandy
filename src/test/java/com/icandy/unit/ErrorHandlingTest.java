package com.icandy.unit;

import com.icandy.common.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for error handling and logging functionality.
 * 
 * This test class validates that the error handling enhancements
 * work correctly across the run phase components.
 */
public class ErrorHandlingTest {
    
    @TempDir
    Path tempDir;
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalErr;
    
    @BeforeEach
    void setUp() {
        // Capture System.err for testing logging fallback
        outputStream = new ByteArrayOutputStream();
        originalErr = System.err;
        System.setErr(new PrintStream(outputStream));
    }
    
    @Test
    void testLoggerCreation() {
        // Test that Logger can be created without errors
        Logger logger = new Logger("TestComponent");
        assertNotNull(logger);
        
        // Test logging methods don't throw exceptions
        assertDoesNotThrow(() -> {
            logger.info("Test info message");
            logger.warning("Test warning message");
            logger.error("Test error message");
        });
    }
    
    @Test
    void testLoggerWithClass() {
        // Test Logger creation with class parameter
        Logger logger = new Logger(ErrorHandlingTest.class);
        assertNotNull(logger);
        
        assertDoesNotThrow(() -> {
            logger.info("Test message from class logger");
        });
    }
    
    @Test
    void testLoggerPerformanceLogging() {
        Logger logger = new Logger("PerformanceTest");
        
        assertDoesNotThrow(() -> {
            logger.performance("test operation", 150L);
            logger.performance("test operation with context", 250L, "processed 10 items");
            logger.frameRate(45.5f, 60.0f); // Should log warning for low FPS
            logger.frameRate(58.2f, 60.0f); // Should log normal info
        });
    }
    
    @Test
    void testLoggerFileOperations() {
        Logger logger = new Logger("FileTest");
        
        assertDoesNotThrow(() -> {
            logger.fileOperation("read", "/test/file.txt", true);
            logger.fileOperation("write", "/test/file.txt", false);
            logger.fileOperation("delete", "/test/file.txt", true, "file size: 1024 bytes");
            logger.missingFile("/missing/file.txt", "check file path");
            logger.missingFile("/another/missing.txt", null);
        });
    }
    
    @Test
    void testLoggerSystemPhases() {
        Logger logger = new Logger("SystemTest");
        
        assertDoesNotThrow(() -> {
            logger.systemPhase("build", "starting");
            logger.systemPhase("build", "completed");
            logger.systemShutdown("ImageDisplayManager", true);
            logger.systemShutdown("BeatDetectorWrapper", false);
        });
    }
    
    @Test
    void testLoggerNetworkRetry() {
        Logger logger = new Logger("NetworkTest");
        
        assertDoesNotThrow(() -> {
            logger.networkRetry("download image", 1, 3, "Connection timeout");
            logger.networkRetry("API call", 2, 5, "Rate limit exceeded");
        });
    }
    
    @Test
    void testLoggerWithExceptions() {
        Logger logger = new Logger("ExceptionTest");
        
        Exception testException = new RuntimeException("Test exception");
        
        assertDoesNotThrow(() -> {
            logger.error("Error with exception", testException);
            logger.error("Error with context and exception", "test context", testException);
        });
    }
    
    @Test
    void testLoggerProgress() {
        Logger logger = new Logger("ProgressTest");
        
        assertDoesNotThrow(() -> {
            logger.progress(1, 10, "Processing first item");
            logger.progress(5, 10, "Processing fifth item");
            logger.progress(10, 10, "Processing complete");
        });
    }
}