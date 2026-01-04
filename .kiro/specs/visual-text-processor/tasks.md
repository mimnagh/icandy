# Implementation Plan: iCandy

## Overview

This implementation plan breaks down the iCandy visual text processor into discrete, manageable tasks. The plan follows a logical progression: first establishing the build phase infrastructure, then implementing the run phase components, and finally integrating everything together. Each task builds on previous work to ensure incremental progress and early validation.

## Tasks

- [x] 1. Set up project structure and configuration
  - Create Processing sketch directory structure
  - Set up data directories (images/, data/, logs/)
  - Create configuration file template (config.json)
  - Create stop words file (data/stopwords.txt) with common English stop words
  - Set up build tool (Maven or Gradle) for dependency management
  - Add Processing core library and Sound library dependencies
  - _Requirements: 6.1, 6.2, 6.3_

- [ ] 2. Implement text parsing and stop word filtering
  - [ ] 2.1 Create TextParser class with phrase and word extraction
    - Implement parseIntoPhrases() to split text by sentences
    - Implement parseIntoWords() to extract individual words
    - Implement mapPhrasesToWords() to create phrase-to-content-words mapping
    - Handle punctuation and special characters appropriately
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [ ]* 2.2 Write property test for text parsing completeness
    - **Property 1: Text Parsing Completeness**
    - **Validates: Requirements 1.1, 1.3**

  - [ ] 2.3 Implement stop word filtering
    - Load stop words from configuration file
    - Implement isStopWord() method
    - Implement filterStopWords() to remove stop words from word list
    - _Requirements: 2.2_

  - [ ]* 2.4 Write property test for stop word filtering
    - **Property 2: Stop Word Filtering**
    - **Validates: Requirements 2.2**

  - [ ]* 2.5 Write property test for phrase structure preservation
    - **Property 3: Phrase Structure Preservation**
    - **Validates: Requirements 1.2**

- [ ] 3. Implement image downloading and Unsplash API integration
  - [ ] 3.1 Create ImageDownloader class
    - Implement Unsplash API authentication
    - Implement searchImages() to query Unsplash API
    - Implement downloadImage() to save images locally
    - Handle API rate limiting and errors
    - _Requirements: 2.3, 2.4, 2.5_

  - [ ]* 3.2 Write unit tests for ImageDownloader
    - Test API authentication
    - Test image search with mocked API responses
    - Test image download with mocked HTTP client
    - Test error handling (network failures, rate limits)
    - _Requirements: 2.3, 2.4, 2.5, 8.4_

  - [ ]* 3.3 Write property test for configured image download count
    - **Property 5: Configured Image Download Count**
    - **Validates: Requirements 2.5, 2.6**

- [ ] 4. Implement association management and persistence
  - [ ] 4.1 Create AssociationManager class
    - Implement addAssociation() to store word-image mappings
    - Implement getImagesForWord() to retrieve images for a word
    - Implement saveToFile() to serialize associations to JSON
    - Implement loadFromFile() to deserialize associations from JSON
    - Implement verifyImageFiles() to check file existence
    - _Requirements: 2.6, 3.1, 3.2, 3.3, 3.4_

  - [ ]* 4.2 Write property test for association persistence round-trip
    - **Property 6: Association Persistence Round-Trip**
    - **Validates: Requirements 2.8, 3.3**

  - [ ]* 4.3 Write property test for word-to-image mapping integrity
    - **Property 7: Word-to-Image Mapping Integrity**
    - **Validates: Requirements 3.1**

  - [ ]* 4.4 Write property test for image file verification
    - **Property 8: Image File Verification**
    - **Validates: Requirements 3.4**

- [ ] 5. Implement build phase orchestration
  - [ ] 5.1 Create BuildOrchestrator class
    - Coordinate TextParser, ImageDownloader, and AssociationManager
    - Implement runBuild() workflow
    - Add progress reporting
    - Handle partial failures gracefully
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [ ] 5.2 Create BuildMain class for command-line interface
    - Implement main() method to accept command-line arguments
    - Parse text file path and optional config file path
    - Instantiate and invoke BuildOrchestrator
    - Display usage instructions on error
    - _Requirements: 2.1_

  - [ ]* 5.3 Write property test for unique word processing
    - **Property 4: Unique Word Processing**
    - **Validates: Requirements 2.2, 2.4**

  - [ ]* 5.4 Write unit tests for BuildOrchestrator
    - Test complete build workflow with sample text
    - Test error handling and recovery
    - Test progress reporting
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

- [ ] 6. Checkpoint - Ensure build phase tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Implement configuration management
  - [ ] 7.1 Create ConfigurationManager class
    - Implement loadFromFile() to parse config.json
    - Implement getter methods for all configuration values
    - Validate configuration values and use defaults for invalid values
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [ ]* 7.2 Write property test for configuration effect on behavior
    - **Property 17: Configuration Effect on Behavior**
    - **Validates: Requirements 6.1, 6.2, 6.3**

  - [ ]* 7.3 Write unit tests for ConfigurationManager
    - Test loading valid configuration
    - Test handling invalid configuration values
    - Test default value fallback
    - _Requirements: 6.4_

- [ ] 8. Implement text display management
  - [ ] 8.1 Create TextDisplayManager class
    - Implement displayPhrase() to render text with subtitle styling
    - Implement calculateDisplayDuration() using the formula
    - Implement updatePhrase() for transitions
    - Position text appropriately on screen
    - _Requirements: 4.2, 4.6_

  - [ ]* 8.2 Write property test for duration calculation formula
    - **Property 13: Duration Calculation Formula**
    - **Validates: Requirements 4.6**

  - [ ]* 8.3 Write unit tests for TextDisplayManager
    - Test phrase rendering
    - Test duration calculation with various phrase lengths
    - Test text positioning
    - _Requirements: 4.2, 4.6_

- [ ] 9. Implement image display management
  - [ ] 9.1 Create ImageDisplayManager class
    - Implement setImagesForPhrase() to load images for current phrase
    - Implement displayCurrentImages() to render images in layout
    - Implement swapImages() to change displayed images
    - Implement preloadImages() for performance
    - Implement setSimultaneousImageCount() for configuration
    - Implement image selection strategy (handle more words than display slots)
    - Track which images have been shown to avoid immediate repetition
    - _Requirements: 4.3, 4.4, 5.2, 5.3, 5.4_

  - [ ]* 9.2 Write property test for phrase-image association
    - **Property 12: Phrase-Image Association**
    - **Validates: Requirements 4.3**

  - [ ]* 9.3 Write property test for image rotation without immediate repetition
    - **Property 15: Image Rotation Without Immediate Repetition**
    - **Validates: Requirements 5.4, 5.5**

  - [ ]* 9.4 Write unit tests for ImageDisplayManager
    - Test image loading and caching
    - Test image layout and rendering
    - Test image swapping logic
    - Test handling of missing images
    - _Requirements: 4.3, 5.2, 5.3, 5.4, 8.1_

- [ ] 10. Implement beat detection
  - [ ] 10.1 Create BeatDetectorWrapper class
    - Initialize Processing Sound library's BeatDetector
    - Implement setup() to configure microphone audio input
    - Implement isBeat() to check for beat detection
    - Implement setSensitivity() for configuration
    - Handle audio input failures gracefully with fallback
    - _Requirements: 5.1, 5.2, 7.2, 8.2, 8.3_

  - [ ]* 10.2 Write property test for beat-triggered image swap
    - **Property 14: Beat-Triggered Image Swap**
    - **Validates: Requirements 5.3**

  - [ ]* 10.3 Write property test for image stability without beats
    - **Property 16: Image Stability Without Beats**
    - **Validates: Requirements 5.6**

  - [ ]* 10.4 Write unit tests for BeatDetectorWrapper
    - Test beat detection initialization
    - Test sensitivity configuration
    - Test fallback behavior when audio unavailable
    - _Requirements: 5.1, 5.2, 8.2, 8.3_

- [ ] 11. Implement phrase sequencing with keyboard navigation
  - [ ] 11.1 Create PhraseSequencer class
    - Implement getCurrentPhrase() and getWordsInCurrentPhrase()
    - Implement advance() to move to next phrase
    - Implement goBack() to move to previous phrase
    - Implement hasNext() and hasPrevious() boundary checks
    - Implement setLooping() to configure looping behavior
    - Track current position in phrase sequence
    - Handle looping back to first phrase when reaching end
    - _Requirements: 4.1, 4.5, 4.7, 4.8, 4.9_

  - [ ]* 11.2 Write property test for sequential phrase display
    - **Property 9: Sequential Phrase Display**
    - **Validates: Requirements 4.1, 4.6, 4.8**

  - [ ]* 11.3 Write property test for backward navigation
    - **Property 10: Backward Navigation**
    - **Validates: Requirements 4.9**

  - [ ]* 11.4 Write property test for phrase looping
    - **Property 11: Phrase Looping**
    - **Validates: Requirements 4.7**

  - [ ]* 11.5 Write unit tests for PhraseSequencer
    - Test forward navigation
    - Test backward navigation
    - Test boundary conditions (first/last phrase)
    - Test looping behavior
    - _Requirements: 4.1, 4.5, 4.7, 4.8, 4.9_

- [ ] 12. Checkpoint - Ensure run phase component tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. Implement main Processing sketch (iCandySketch)
  - [ ] 13.1 Create iCandySketch class extending PApplet
    - Implement setup() to initialize all components
    - Load configuration and associations
    - Set up audio input and beat detection
    - Initialize display managers and sequencer
    - _Requirements: 7.1, 7.2, 7.5_

  - [ ] 13.2 Implement draw() loop
    - Update beat detection state
    - Check for automatic phrase advancement
    - Render current phrase via TextDisplayManager
    - Render current images via ImageDisplayManager
    - Swap images on beat detection
    - Maintain target frame rate
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.5, 7.3_

  - [ ] 13.3 Implement keyPressed() for keyboard navigation
    - Handle right arrow key to advance phrase
    - Handle left arrow key to go back to previous phrase
    - Reset phrase timer on manual navigation
    - _Requirements: 4.8, 4.9_

  - [ ]* 13.4 Write integration tests for iCandySketch
    - Test complete setup and initialization
    - Test draw loop execution
    - Test keyboard input handling
    - Test beat detection integration
    - Test phrase looping behavior
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.7, 4.8, 4.9, 5.1, 5.2, 5.5_

- [ ] 14. Implement error handling and logging
  - [ ] 14.1 Add error handling throughout build phase
    - Network failure handling with retry logic
    - API rate limiting detection and backoff
    - Invalid text file handling
    - File system error handling
    - _Requirements: 1.5, 2.5, 8.4, 8.5_

  - [ ]* 14.2 Write property test for download retry limit
    - **Property 18: Download Retry Limit**
    - **Validates: Requirements 8.4**

  - [ ] 14.3 Add error handling throughout run phase
    - Missing image file handling
    - Audio input failure fallback
    - Beat detection failure fallback
    - Invalid associations file handling
    - _Requirements: 8.1, 8.2, 8.3_

  - [ ] 14.4 Implement logging strategy
    - Create log file with timestamp
    - Log all errors with context
    - Log warnings for missing files
    - Log progress during build phase
    - _Requirements: 8.5_

  - [ ]* 14.5 Write unit tests for error handling
    - Test network failure retry logic
    - Test missing file handling
    - Test audio fallback behavior
    - Test logging output
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 15. Create sample data and documentation
  - [ ] 15.1 Create sample text scripts
    - Short sample (10-20 words)
    - Medium sample (50-100 words)
    - Long sample (200+ words)
    - _Requirements: 1.1_

  - [ ] 15.2 Create example configuration file
    - Document all configuration options
    - Provide sensible defaults
    - Include comments explaining each setting
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [ ] 15.3 Create README with usage instructions
    - Explain build phase usage (command-line: java BuildMain <textfile>)
    - Explain run phase usage (run Processing sketch)
    - Document keyboard controls (left/right arrows)
    - Document configuration options (simultaneousImageCount, loopPhrases, etc.)
    - Provide troubleshooting tips
    - Include Unsplash API key setup instructions

- [ ] 16. Final integration and testing
  - [ ] 16.1 Run complete build phase with sample text
    - Verify images are downloaded
    - Verify associations are saved
    - Verify error handling works
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [ ] 16.2 Run complete run phase with built associations
    - Verify phrases display correctly
    - Verify images display and swap
    - Verify keyboard navigation works
    - Verify phrase looping works
    - Verify beat detection works (if audio available)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.7, 4.8, 4.9, 5.1, 5.2, 5.3, 5.4, 5.5_

  - [ ]* 16.3 Run full property-based test suite
    - Execute all 18 property tests
    - Verify 100+ iterations per test
    - Fix any failures discovered
    - _All Requirements_

- [ ] 17. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- The build phase (tasks 1-6) can be completed independently before the run phase (tasks 7-13)
- Integration testing (task 16) validates the complete system end-to-end
