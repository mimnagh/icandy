package com.icandy.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Centralized logging utility for iCandy application.
 * Provides structured logging with file output and console output.
 */
public class Logger {
    
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final java.util.logging.Logger javaLogger;
    private final String componentName;
    
    /**
     * Creates a logger for the specified component.
     * 
     * @param componentName Name of the component (e.g., "BuildOrchestrator", "ImageDownloader")
     */
    public Logger(String componentName) {
        this.componentName = componentName;
        this.javaLogger = java.util.logging.Logger.getLogger("iCandy." + componentName);
        
        // Configure logger
        setupLogger();
    }
    
    /**
     * Creates a logger for the specified class.
     * 
     * @param clazz The class to create a logger for
     */
    public Logger(Class<?> clazz) {
        this(clazz.getSimpleName());
    }
    
    /**
     * Sets up the logger with file and console handlers.
     */
    private void setupLogger() {
        try {
            // Ensure log directory exists
            Path logDir = Paths.get(LOG_DIR);
            Files.createDirectories(logDir);
            
            // Create log file with current date and timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String logFileName = "icandy-" + timestamp + ".log";
            Path logFile = logDir.resolve(logFileName);
            
            // Add file handler if not already present
            if (javaLogger.getHandlers().length == 0) {
                FileHandler fileHandler = new FileHandler(logFile.toString(), true);
                fileHandler.setFormatter(new iCandyLogFormatter());
                javaLogger.addHandler(fileHandler);
                
                // Set level to INFO by default
                javaLogger.setLevel(Level.INFO);
                
                // Log startup message
                javaLogger.info("iCandy logging initialized - log file: " + logFile.toString());
            }
            
        } catch (IOException e) {
            // Fallback to console-only logging
            System.err.println("Warning: Could not set up file logging: " + e.getMessage());
            System.err.println("Continuing with console-only logging");
        } catch (Exception e) {
            // Handle any other setup errors
            System.err.println("Error setting up logger: " + e.getMessage());
        }
    }
    
    /**
     * Logs an info message.
     * 
     * @param message The message to log
     */
    public void info(String message) {
        javaLogger.info(message);
    }
    
    /**
     * Logs an info message with context.
     * 
     * @param message The message to log
     * @param context Additional context information
     */
    public void info(String message, String context) {
        javaLogger.info(message + " [Context: " + context + "]");
    }
    
    /**
     * Logs a warning message.
     * 
     * @param message The warning message to log
     */
    public void warning(String message) {
        javaLogger.warning(message);
    }
    
    /**
     * Logs a warning message with context.
     * 
     * @param message The warning message to log
     * @param context Additional context information
     */
    public void warning(String message, String context) {
        javaLogger.warning(message + " [Context: " + context + "]");
    }
    
    /**
     * Logs an error message.
     * 
     * @param message The error message to log
     */
    public void error(String message) {
        javaLogger.severe(message);
    }
    
    /**
     * Logs an error message with exception.
     * 
     * @param message The error message to log
     * @param exception The exception that caused the error
     */
    public void error(String message, Throwable exception) {
        javaLogger.log(Level.SEVERE, message, exception);
    }
    
    /**
     * Logs an error message with context and exception.
     * 
     * @param message The error message to log
     * @param context Additional context information
     * @param exception The exception that caused the error
     */
    public void error(String message, String context, Throwable exception) {
        javaLogger.log(Level.SEVERE, message + " [Context: " + context + "]", exception);
    }
    
    /**
     * Logs a progress message (info level).
     * 
     * @param current Current progress value
     * @param total Total progress value
     * @param message Progress message
     */
    public void progress(int current, int total, String message) {
        info(String.format("[%d/%d] %s", current, total, message));
    }
    
    /**
     * Logs performance metrics with timing information.
     * 
     * @param operation The operation being measured
     * @param durationMs Duration in milliseconds
     */
    public void performance(String operation, long durationMs) {
        info(String.format("Performance: %s completed in %dms", operation, durationMs));
    }
    
    /**
     * Logs performance metrics with additional context.
     * 
     * @param operation The operation being measured
     * @param durationMs Duration in milliseconds
     * @param context Additional performance context (e.g., "processed 100 images")
     */
    public void performance(String operation, long durationMs, String context) {
        info(String.format("Performance: %s completed in %dms [%s]", operation, durationMs, context));
    }
    
    /**
     * Logs frame rate performance metrics.
     * 
     * @param fps Current frames per second
     * @param targetFps Target frames per second
     */
    public void frameRate(float fps, float targetFps) {
        if (fps < targetFps * 0.8f) {
            warning(String.format("Performance: Low frame rate %.1f fps (target: %.1f fps)", fps, targetFps));
        } else {
            info(String.format("Performance: Frame rate %.1f fps", fps));
        }
    }
    
    /**
     * Logs network retry attempts.
     * 
     * @param operation The network operation being retried
     * @param attempt Current attempt number
     * @param maxAttempts Maximum number of attempts
     * @param error The error that caused the retry
     */
    public void networkRetry(String operation, int attempt, int maxAttempts, String error) {
        warning(String.format("Network retry %d/%d for %s: %s", attempt, maxAttempts, operation, error));
    }
    
    /**
     * Logs file system operations with detailed information.
     * 
     * @param operation The file operation (e.g., "read", "write", "delete")
     * @param filePath The file path involved
     * @param success Whether the operation succeeded
     */
    public void fileOperation(String operation, String filePath, boolean success) {
        if (success) {
            info(String.format("File %s successful: %s", operation, filePath));
        } else {
            warning(String.format("File %s failed: %s", operation, filePath));
        }
    }
    
    /**
     * Logs file system operations with additional context.
     * 
     * @param operation The file operation (e.g., "read", "write", "delete")
     * @param filePath The file path involved
     * @param success Whether the operation succeeded
     * @param context Additional context (e.g., file size, error details)
     */
    public void fileOperation(String operation, String filePath, boolean success, String context) {
        if (success) {
            info(String.format("File %s successful: %s [%s]", operation, filePath, context));
        } else {
            warning(String.format("File %s failed: %s [%s]", operation, filePath, context));
        }
    }
    
    /**
     * Logs missing file warnings with suggestions.
     * 
     * @param filePath The missing file path
     * @param suggestion Optional suggestion for resolving the issue
     */
    public void missingFile(String filePath, String suggestion) {
        if (suggestion != null && !suggestion.isEmpty()) {
            warning(String.format("Missing file: %s - %s", filePath, suggestion));
        } else {
            warning(String.format("Missing file: %s", filePath));
        }
    }
    
    /**
     * Logs configuration loading and validation.
     * 
     * @param configFile The configuration file path
     * @param success Whether loading succeeded
     * @param validationErrors List of validation errors (if any)
     */
    public void configurationLoad(String configFile, boolean success, List<String> validationErrors) {
        if (success) {
            info(String.format("Configuration loaded successfully: %s", configFile));
        } else {
            error(String.format("Configuration loading failed: %s", configFile));
        }
        
        if (validationErrors != null && !validationErrors.isEmpty()) {
            for (String error : validationErrors) {
                warning(String.format("Configuration validation: %s", error));
            }
        }
    }
    
    /**
     * Logs system startup and initialization phases.
     * 
     * @param phase The initialization phase (e.g., "build", "run", "setup")
     * @param status The status (e.g., "starting", "completed", "failed")
     */
    public void systemPhase(String phase, String status) {
        info(String.format("System %s: %s", phase, status));
    }
    
    /**
     * Logs system shutdown and cleanup.
     * 
     * @param component The component being shut down
     * @param cleanShutdown Whether shutdown was clean
     */
    public void systemShutdown(String component, boolean cleanShutdown) {
        if (cleanShutdown) {
            info(String.format("System shutdown: %s completed cleanly", component));
        } else {
            warning(String.format("System shutdown: %s did not complete cleanly", component));
        }
    }
    
    /**
     * Custom formatter for iCandy log messages.
     */
    private static class iCandyLogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String level = record.getLevel().getName();
            String loggerName = record.getLoggerName();
            String message = record.getMessage();
            
            // Extract component name from logger name
            String component = loggerName.startsWith("iCandy.") ? 
                loggerName.substring(7) : loggerName;
            
            StringBuilder sb = new StringBuilder();
            sb.append(timestamp)
              .append(" [").append(level).append("] ")
              .append("[").append(component).append("] ")
              .append(message);
            
            // Add exception if present
            if (record.getThrown() != null) {
                sb.append("\n");
                Throwable thrown = record.getThrown();
                sb.append("Exception: ").append(thrown.getClass().getSimpleName())
                  .append(": ").append(thrown.getMessage());
                
                // Add stack trace for severe errors
                if (record.getLevel() == Level.SEVERE) {
                    for (StackTraceElement element : thrown.getStackTrace()) {
                        sb.append("\n\tat ").append(element.toString());
                    }
                }
            }
            
            sb.append("\n");
            return sb.toString();
        }
    }
}