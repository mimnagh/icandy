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

- [x] 2. Implement text parsing and stop word filtering
  - [x] 2.1 Create TextParser class with phrase and word extraction
    - Implement parseIntoPhrases() to split text by sentences
    - Implement parseIntoWords() to extract individual words
    - Implement mapPhrasesToWords() to create phrase-to-content-words mapping
    - Handle punctuation and special characters appropriately
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [ ]* 2.2 Write property test for text parsing completeness
    - **Property 1: Text Parsing Completeness**
    - **Validates: Requirements 1.1, 1.3**

  - [x] 2.3 Implement stop word filtering
    - Load stop words from configuration file
    - Implement isStopWord() method
    - Implement filterStopWords() to remove stop words from word list
    - _Requirements: 2.2_

  - [x] 2.4 Create unit tests for TextParser
    - Test all parsing methods with various inputs
    - Test stop word filtering
    - Test edge cases (empty input, null input)
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.2_

  - [ ]* 2.5 Write property test for stop word filtering
    - **Property 2: Stop Word Filtering**
    - **Validates: Requirements 2.2**

  - [ ]* 2.6 Write property test for phrase structure preservation
    - **Property 3: Phrase Structure Preservation**
    - **Validates: Requirements 1.2**

- [x] 3. Implement image downloading and Unsplash API integration
  - [x] 3.1 Create ImageDownloader class
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

- [x] 4. Implement association management and persistence
  - [x] 4.1 Create AssociationManager class
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

- [x] 5. Implement build phase orchestration
  - [x] 5.1 Create BuildOrchestrator class
    - Coordinate TextParser, ImageDownloader, and AssociationManager
    - Implement runBuild() workflow
    - Add progress reporting
    - Handle partial failures gracefully
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [x] 5.2 Create BuildMain class for command-line interface
    - Implement main() method to accept command-line arguments
    - Parse text file path and optional config file path
    - Instantiate and invoke BuildOrchestrator
    - Display usage instructions on error
    - _Requirements: 2.1_

  - [ ]* 5.3 Write property test for unique word processing
    - **Property 4: Unique Word Processing**
    - **Validates: Requirements 2.2, 2.4**

  - [x] 5.4 Write unit tests for BuildOrchestrator
    - Test complete build workflow with sample text
    - Test error handling and recovery
    - Test progress reporting
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [ ]* 5.4.1 Enhance BuildOrchestrator tests with specific assertions
    - Add descriptive assertion messages to all tests
    - Replace generic assertTrue/assertFalse with specific assertEquals where applicable
    - Verify exact counts instead of just "greater than zero"
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [ ]* 5.4.2 Add test for associations file persistence
    - Test that associations file is created after build
    - Verify file can be loaded back into AssociationManager
    - Validate word count and image count match expected values
    - _Requirements: 2.6, 2.8, 3.2, 3.3_

  - [ ]* 5.4.3 Add test for special characters in words
    - Test words with punctuation, numbers, Unicode characters
    - Verify sanitizeFilename() handles special characters correctly
    - Ensure images are downloaded with sanitized filenames
    - _Requirements: 1.4, 2.4_

  - [ ]* 5.4.4 Extract magic numbers to constants
    - Create test constants for imagesPerWord, maxRetries
    - Use constants throughout test methods
    - Improve test readability and maintainability

  - [ ]* 5.4.5 Add test for configuration with extra fields
    - Test config file with comments and unknown fields
    - Verify system ignores unknown fields gracefully
    - Ensure backward compatibility with config changes
    - _Requirements: 6.4_

  - [ ]* 5.4.6 Add test for concurrent builds
    - Test multiple BuildOrchestrator instances running simultaneously
    - Verify no file conflicts or race conditions
    - Ensure each build maintains independent state
    - _Requirements: 2.1, 2.6_

- [x] 6. Checkpoint - Ensure build phase tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Implement configuration management
  - [x] 7.1 Create ConfigurationManager class
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

- [x] 8. Implement text display management
  - [x] 8.1 Create TextDisplayManager class
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

- [-] 9. Implement image display management
  - [x] 9.1 Create ImageDisplayManager class
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

  - [x] 9.4 Write unit tests for ImageDisplayManager
    - Test image loading and caching
    - Test image layout and rendering
    - Test image swapping logic
    - Test handling of missing images
    - _Requirements: 4.3, 5.2, 5.3, 5.4, 8.1_

- [x] 10. Implement beat detection
  - [x] 10.1 Create BeatDetectorWrapper class
    - Initialize Processing Sound library's BeatDetector
    - Implement setup() to configure microphone audio input
    - Implement isBeat() to check for beat detection
    - Implement setSensitivity() for configuration
    - Handle audio input failures gracefully with fallback
    - _Requirements: 5.1, 5.2, 7.2, 8.2, 8.3_

  - [ ]* 10.2 Write property test for beat and arrow key image swap
    - **Property 14: Beat and Arrow Key Image Swap**
    - **Validates: Requirements 5.3, 4.8, 4.9**

  - [ ]* 10.3 Write property test for image stability without beats
    - **Property 16: Image Stability Without Beats**
    - **Validates: Requirements 5.6**

  - [ ]* 10.4 Write unit tests for BeatDetectorWrapper
    - Test beat detection initialization
    - Test sensitivity configuration
    - Test fallback behavior when audio unavailable
    - _Requirements: 5.1, 5.2, 8.2, 8.3_

- [x] 11. Implement phrase sequencing with automatic timing
  - [x] 11.1 Create PhraseSequencer class
    - Implement getCurrentPhrase() and getWordsInCurrentPhrase()
    - Implement advance() to move to next phrase automatically
    - Implement hasNext() boundary checks
    - Implement setLooping() to configure looping behavior
    - Track current position in phrase sequence
    - Handle looping back to first phrase when reaching end
    - Note: Automatic timing is handled by TextDisplayManager.shouldAdvance()
    - _Requirements: 4.1, 4.5, 4.6, 4.7_

  - [ ]* 11.2 Write property test for sequential phrase display
    - **Property 9: Sequential Phrase Display**
    - **Validates: Requirements 4.1, 4.6**

  - [ ]* 11.3 Write property test for arrow key image swapping
    - **Property 10: Arrow Key Image Swapping**
    - **Validates: Requirements 4.8, 4.9**

  - [ ]* 11.4 Write property test for phrase looping
    - **Property 11: Phrase Looping**
    - **Validates: Requirements 4.7**

  - [ ]* 11.5 Write unit tests for PhraseSequencer
    - Test automatic phrase advancement based on timing
    - Test boundary conditions (first/last phrase)
    - Test looping behavior
    - _Requirements: 4.1, 4.5, 4.6, 4.7_

- [x] 12. Checkpoint - Ensure run phase component tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 13. Implement main Processing sketch (iCandySketch)
  - [x] 13.1 Create iCandySketch class extending PApplet
    - Implement setup() to initialize all components
    - Load configuration and associations
    - Set up audio input and beat detection
    - Initialize display managers and sequencer
    - _Requirements: 7.1, 7.2, 7.5_

  - [x] 13.2 Implement draw() loop
    - Update beat detection state
    - Check for automatic phrase advancement
    - Render current phrase via TextDisplayManager
    - Render current images via ImageDisplayManager
    - Swap images on beat detection
    - Maintain target frame rate
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.5, 7.3_

  - [x] 13.3 Implement keyPressed() for image swapping
    - Handle right arrow key to swap images within current phrase
    - Handle left arrow key to swap images within current phrase
    - Use same image swapping logic as beat detection
    - _Requirements: 4.8, 4.9_

  - [ ]* 13.4 Write integration tests for iCandySketch
    - Test complete setup and initialization
    - Test draw loop execution
    - Test keyboard input handling for image swapping
    - Test beat detection integration
    - Test phrase looping behavior
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.7, 4.8, 4.9, 5.1, 5.2, 5.5_

- [x] 14. Implement configurable layout engine
  - [x] 14.1 Create LayoutEngine and layout algorithm interfaces
    - Create LayoutAlgorithm interface with calculatePositions() and updatePositions()
    - Create LayoutEngine class to manage algorithm selection and configuration
    - Create LayoutConfig class for algorithm-specific parameters
    - Create ImageInfo and ImagePosition data models
    - _Requirements: 9.1, 9.6_

  - [x] 14.2 Implement GridLayoutAlgorithm
    - Calculate positions in regular rectangular grid
    - Support configurable rows, columns, spacing, padding
    - Implement alignment options (center, left, right, top, bottom)
    - Handle automatic grid sizing based on image count
    - _Requirements: 9.2_

  - [ ]* 14.3 Write property test for grid layout positioning
    - **Property 19: Layout Algorithm Positioning (Grid)**
    - **Validates: Requirements 9.1, 9.2**

  - [x] 14.4 Implement CollageLayoutAlgorithm
    - Generate random sizes within configured range
    - Generate random rotations within configured range
    - Implement controlled overlap with collision detection
    - Create organic, non-uniform positioning
    - _Requirements: 9.3_

  - [ ]* 14.5 Write property test for collage layout positioning
    - **Property 19: Layout Algorithm Positioning (Collage)**
    - **Validates: Requirements 9.1, 9.3**

  - [x] 14.6 Implement CircularLayoutAlgorithm
    - Arrange images in circular or spiral patterns
    - Support configurable radius and arc span
    - Implement rotation direction (clockwise/counterclockwise)
    - Support spiral mode with configurable pitch
    - _Requirements: 9.4_

  - [ ]* 14.7 Write property test for circular layout positioning
    - **Property 19: Layout Algorithm Positioning (Circular)**
    - **Validates: Requirements 9.1, 9.4**

  - [x] 14.8 Implement FlowingLayoutAlgorithm
    - Generate Bezier curve paths for image positioning
    - Support configurable flow direction and curvature
    - Implement organic spacing along paths
    - Support multiple flow paths for complex layouts
    - _Requirements: 9.5_

  - [ ]* 14.9 Write property test for flowing layout positioning
    - **Property 19: Layout Algorithm Positioning (Flowing)**
    - **Validates: Requirements 9.1, 9.5**

  - [x] 14.10 Implement layout algorithm configuration and switching
    - Add layout configuration loading to ConfigurationManager
    - Implement smooth transitions between layout algorithms
    - Add layout parameter validation and clamping
    - Support dynamic layout parameter updates
    - _Requirements: 9.6, 9.7, 9.8_

  - [ ]* 14.11 Write property test for layout algorithm configuration effect
    - **Property 20: Layout Algorithm Configuration Effect**
    - **Validates: Requirements 9.6, 9.8**

  - [ ]* 14.12 Write property test for layout algorithm transitions
    - **Property 21: Layout Algorithm Transitions**
    - **Validates: Requirements 9.7**

  - [ ]* 14.13 Write property test for layout parameter effects
    - **Property 25: Layout Parameter Effect**
    - **Validates: Requirements 13.1, 13.2, 13.3, 13.4, 13.5, 13.6**

  - [ ]* 14.14 Write unit tests for LayoutEngine and algorithms
    - Test each layout algorithm with various configurations
    - Test layout switching and transitions
    - Test parameter validation and error handling
    - Test edge cases (zero images, single image, many images)
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8_

- [x] 15. Implement configurable transition engine
  - [x] 15.1 Create TransitionEngine and transition effect interfaces
    - Create TransitionEffect interface with startTransition() and updateTransition()
    - Create TransitionEngine class to manage effect selection and timing
    - Create TransitionConfig class for effect-specific parameters
    - Create TransitionState and ImageState data models
    - Implement EasingFunction enum with linear, ease-in, ease-out, ease-in-out
    - _Requirements: 10.1, 10.7, 11.1, 11.2_

  - [x] 15.2 Implement FadeTransition effect
    - Gradually change image opacity during transitions
    - Support configurable fade duration and easing
    - Implement cross-fade between old and new images
    - Use alpha blending with Processing's tint() function
    - _Requirements: 10.2_

  - [ ]* 15.3 Write property test for fade transition behavior
    - **Property 22: Transition Effect Behavior (Fade)**
    - **Validates: Requirements 10.1, 10.2**

  - [x] 15.4 Implement SlideTransition effect
    - Move images in from specified directions during swaps
    - Support configurable slide direction (up, down, left, right, diagonal)
    - Implement bounce and overshoot effects
    - Support staggered timing for multiple images
    - _Requirements: 10.3_

  - [ ]* 15.5 Write property test for slide transition behavior
    - **Property 22: Transition Effect Behavior (Slide)**
    - **Validates: Requirements 10.1, 10.3**

  - [x] 15.6 Implement ZoomTransition effect
    - Scale images in or out during swaps
    - Support zoom in (scale from 0 to 1) and zoom out (scale from 1 to 0)
    - Support configurable zoom center point
    - Combine with fade for smooth effect
    - _Requirements: 10.4_

  - [ ]* 15.7 Write property test for zoom transition behavior
    - **Property 22: Transition Effect Behavior (Zoom)**
    - **Validates: Requirements 10.1, 10.4**

  - [x] 15.8 Implement RotateTransition effect
    - Rotate images during transitions with configurable angle
    - Support rotation direction and center point
    - Combine with scale and fade effects
    - Use Processing's rotate() and translate() functions
    - _Requirements: 10.5_

  - [ ]* 15.9 Write property test for rotate transition behavior
    - **Property 22: Transition Effect Behavior (Rotate)**
    - **Validates: Requirements 10.1, 10.5**

  - [x] 15.10 Implement MorphTransition effect
    - Blend between old and new images using shape interpolation
    - Implement color blending and morphing effects
    - Use Processing's blend() function with different blend modes
    - Support vertex manipulation for organic transitions
    - _Requirements: 10.6_

  - [ ]* 15.11 Write property test for morph transition behavior
    - **Property 22: Transition Effect Behavior (Morph)**
    - **Validates: Requirements 10.1, 10.6**

  - [x] 15.12 Implement transition coordination and stagger timing
    - Coordinate multiple simultaneous image transitions
    - Implement stagger timing with configurable delays
    - Support transition parameter configuration (duration, easing, stagger)
    - Add transition configuration loading to ConfigurationManager
    - _Requirements: 10.7, 10.8, 11.3, 11.4_

  - [ ]* 15.13 Write property test for transition parameter configuration
    - **Property 23: Transition Parameter Configuration**
    - **Validates: Requirements 10.7, 10.8, 11.1, 11.2, 11.3, 11.4, 11.5**

  - [x] 15.14 Write unit tests for TransitionEngine and effects
    - Test each transition effect with various configurations
    - Test transition timing and easing functions
    - Test stagger timing and coordination
    - Test parameter validation and error handling
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8_

- [x] 16. Implement visual effects manager
  - [x] 16.1 Create VisualEffectsManager class
    - Create VisualEffectsManager class to apply effects to images
    - Create VisualEffectsConfig class for effect parameters
    - Create VisualEffectsState data model
    - Add visual effects configuration loading to ConfigurationManager
    - _Requirements: 12.6_

  - [x] 16.2 Implement blur effects
    - Implement Gaussian blur with configurable radius
    - Use Processing's filter(BLUR) or custom blur shader
    - Support motion blur for moving images
    - Implement selective blur (blur background, keep foreground sharp)
    - _Requirements: 12.1_

  - [ ]* 16.3 Write property test for blur effects
    - **Property 24: Visual Effects Application (Blur)**
    - **Validates: Requirements 12.1, 12.6**

  - [x] 16.4 Implement color filter effects
    - Implement sepia tone effect using color matrix
    - Implement grayscale conversion
    - Implement color tinting with configurable hue/saturation
    - Implement vintage/retro color grading effects
    - Use Processing's tint() and colorMode() functions
    - _Requirements: 12.2_

  - [ ]* 16.5 Write property test for color filter effects
    - **Property 24: Visual Effects Application (Color Filters)**
    - **Validates: Requirements 12.2, 12.6**

  - [x] 16.6 Implement brightness and contrast adjustments
    - Implement configurable brightness levels (-100% to +100%)
    - Implement configurable contrast levels (-100% to +100%)
    - Implement gamma correction
    - Use Processing's tint() and custom pixel manipulation
    - _Requirements: 12.3_

  - [ ]* 16.7 Write property test for brightness/contrast effects
    - **Property 24: Visual Effects Application (Brightness/Contrast)**
    - **Validates: Requirements 12.3, 12.6**

  - [x] 16.8 Implement particle systems
    - Generate particles around images during transitions
    - Support configurable particle count, size, color, lifetime
    - Implement physics simulation (gravity, wind, collision)
    - Support different particle types (sparkles, smoke, fire, snow)
    - Use Processing's PVector for particle physics
    - _Requirements: 12.4_

  - [ ]* 16.9 Write property test for particle systems
    - **Property 24: Visual Effects Application (Particles)**
    - **Validates: Requirements 12.4, 12.6**

  - [x] 16.10 Implement border effects
    - Implement glow effect around image edges using blur and blend
    - Implement drop shadow with configurable offset and blur
    - Implement outline/stroke with configurable thickness and color
    - Implement vintage frame effects
    - _Requirements: 12.5_

  - [ ]* 16.11 Write property test for border effects
    - **Property 24: Visual Effects Application (Borders)**
    - **Validates: Requirements 12.5, 12.6**

  - [ ]* 16.12 Write property test for visual effects application
    - **Property 24: Visual Effects Application (General)**
    - **Validates: Requirements 12.6, 12.8**

  - [ ]* 16.13 Write unit tests for VisualEffectsManager
    - Test each visual effect with various configurations
    - Test effect combination and interaction
    - Test performance impact and quality reduction
    - Test parameter validation and error handling
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8_

- [x] 17. Enhance ImageDisplayManager with new engines
  - [x] 17.1 Update ImageDisplayManager to use LayoutEngine
    - Integrate LayoutEngine for image positioning
    - Replace hardcoded grid layout with configurable algorithms
    - Add layout algorithm switching support
    - Update displayCurrentImages() to use calculated positions
    - _Requirements: 9.1, 9.6, 9.7_

  - [x] 17.2 Update ImageDisplayManager to use TransitionEngine
    - Integrate TransitionEngine for image swapping
    - Replace instant image swaps with animated transitions
    - Add transition effect configuration support
    - Update swapImages() to use transition effects
    - _Requirements: 10.1, 10.7_

  - [x] 17.3 Update ImageDisplayManager to use VisualEffectsManager
    - Integrate VisualEffectsManager for image enhancement
    - Apply visual effects to all displayed images
    - Add visual effects configuration support
    - Update displayCurrentImages() to apply effects
    - _Requirements: 12.6_

  - [x] 17.4 Implement frame-based animation system
    - Add update() method with delta time parameter
    - Update all animations and transitions each frame
    - Maintain smooth 60 FPS performance
    - Handle animation state management
    - _Requirements: 7.3_

  - [ ]* 17.5 Write unit tests for enhanced ImageDisplayManager
    - Test integration with LayoutEngine
    - Test integration with TransitionEngine
    - Test integration with VisualEffectsManager
    - Test frame-based animation system
    - Test performance with multiple effects active
    - _Requirements: 9.1, 10.1, 12.6_

- [ ] 18. Update configuration management for new features
  - [ ] 18.1 Enhance ConfigurationManager for layout configuration
    - Add layout algorithm selection and parameters
    - Add layout parameter validation and defaults
    - Support dynamic layout parameter updates
    - _Requirements: 9.6, 9.8, 13.1, 13.2, 13.3, 13.4, 13.5, 13.6_

  - [ ] 18.2 Enhance ConfigurationManager for transition configuration
    - Add transition effect selection and parameters
    - Add transition timing and easing configuration
    - Support dynamic transition parameter updates
    - _Requirements: 10.7, 11.1, 11.2, 11.3, 11.5_

  - [ ] 18.3 Enhance ConfigurationManager for visual effects configuration
    - Add visual effects selection and parameters
    - Add effect intensity and quality configuration
    - Support dynamic visual effects parameter updates
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 12.6_

  - [ ]* 18.4 Write property test for invalid parameter handling
    - **Property 26: Invalid Parameter Handling**
    - **Validates: Requirements 11.6, 13.7**

  - [ ]* 18.5 Write property test for dynamic parameter updates
    - **Property 27: Dynamic Parameter Updates**
    - **Validates: Requirements 11.7, 13.8**

  - [ ]* 18.6 Write unit tests for enhanced ConfigurationManager
    - Test loading enhanced configuration file
    - Test parameter validation and clamping
    - Test dynamic parameter updates
    - Test error handling for invalid configurations
    - _Requirements: 9.6, 10.7, 11.6, 12.6, 13.7_

- [ ] 19. Update main Processing sketch for new features
  - [ ] 19.1 Update iCandySketch setup() for new engines
    - Initialize LayoutEngine with configured algorithm
    - Initialize TransitionEngine with configured effects
    - Initialize VisualEffectsManager with configured effects
    - Pass engines to enhanced ImageDisplayManager
    - _Requirements: 9.1, 10.1, 12.6_

  - [ ] 19.2 Update iCandySketch draw() loop for animations
    - Calculate delta time for smooth animations
    - Update LayoutEngine, TransitionEngine, and VisualEffectsManager each frame
    - Update enhanced ImageDisplayManager with delta time
    - Maintain target frame rate with new features
    - _Requirements: 7.3_

  - [ ] 19.3 Implement comprehensive keyboard controls for interactive mode
    - Add number keys (1-4) to switch layout algorithms with immediate visual feedback
    - Add letter keys (Q-W-E-R-T) to switch transition effects with immediate application
    - Add function keys (F1-F5) to toggle visual effects with immediate preview
    - Add space bar for manual image swapping using current transition
    - Add tab key to display settings overlay with current configuration and FPS
    - Implement smooth transitions when switching between modes
    - Display brief on-screen notifications when settings change
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

  - [ ] 19.4 Implement settings overlay and visual feedback system
    - Create settings overlay UI showing current layout, transition, and effects
    - Display performance metrics (FPS, effect quality level)
    - Add configurable overlay duration and styling
    - Implement fade-in/fade-out animations for overlay
    - Add brief notification system for setting changes
    - Ensure overlay doesn't interfere with main visual experience
    - _Requirements: 14.5, 14.7_

  - [ ]* 19.5 Write property test for interactive keyboard controls
    - **Property 28: Interactive Keyboard Controls**
    - **Validates: Requirements 14.1, 14.2, 14.3, 14.4, 14.6**

  - [ ]* 19.6 Write integration tests for enhanced iCandySketch
    - Test initialization with all new engines
    - Test frame-based animation system
    - Test comprehensive keyboard controls for all features
    - Test settings overlay display and timing
    - Test performance with all effects enabled
    - Test smooth transitions between different modes
    - _Requirements: 9.1, 10.1, 12.6, 14.1, 14.2, 14.3_

- [ ] 20. Checkpoint - Ensure enhanced features tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 21. Implement error handling and logging
  - [ ] 21.1 Add error handling throughout build phase
    - Network failure handling with retry logic
    - API rate limiting detection and backoff
    - Invalid text file handling
    - File system error handling
    - _Requirements: 1.5, 2.5, 8.4, 8.5_

- [ ] 21. Implement error handling and logging
  - [ ] 21.1 Add error handling throughout build phase
    - Network failure handling with retry logic
    - API rate limiting detection and backoff
    - Invalid text file handling
    - File system error handling
    - _Requirements: 1.5, 2.5, 8.4, 8.5_

  - [ ]* 21.2 Write property test for download retry limit
    - **Property 18: Download Retry Limit**
    - **Validates: Requirements 8.4**

  - [ ] 21.3 Add error handling throughout run phase
    - Missing image file handling
    - Audio input failure fallback
    - Beat detection failure fallback
    - Invalid associations file handling
    - Layout/transition/effects error handling
    - _Requirements: 8.1, 8.2, 8.3_

  - [ ] 21.4 Implement logging strategy
    - Create log file with timestamp
    - Log all errors with context
    - Log warnings for missing files
    - Log progress during build phase
    - Log performance metrics for new features
    - _Requirements: 8.5_

  - [ ]* 21.5 Write unit tests for error handling
    - Test network failure retry logic
    - Test missing file handling
    - Test audio fallback behavior
    - Test logging output
    - Test error handling for new features
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 22. Create enhanced sample data and documentation
  - [ ] 22.1 Create sample text scripts
    - Short sample (10-20 words)
    - Medium sample (50-100 words)
    - Long sample (200+ words)
    - _Requirements: 1.1_

  - [ ] 22.2 Create enhanced example configuration file
    - Document all configuration options including new features
    - Provide sensible defaults for layout, transition, and visual effects
    - Include comments explaining each setting
    - Provide multiple configuration presets (minimal, artistic, performance)
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 9.6, 10.7, 12.6_

  - [ ] 22.3 Update README with enhanced interactive features documentation
    - Document all keyboard controls with clear key mappings
    - Document layout algorithms and their visual characteristics
    - Document transition effects and their parameters
    - Document visual effects and their configurations
    - Document settings overlay and performance monitoring
    - Provide interactive usage examples and workflows
    - Include performance tuning tips for different hardware
    - Add troubleshooting section for keyboard controls
    - _Requirements: 9.1, 10.1, 12.1, 14.1, 14.2, 14.3_

- [ ] 23. Final integration and testing with enhanced features
  - [ ] 23.1 Run complete build phase with sample text
    - Verify images are downloaded
    - Verify associations are saved
    - Verify error handling works
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

  - [ ] 23.2 Run complete run phase with enhanced features
    - Verify all layout algorithms work correctly
    - Verify all transition effects work smoothly
    - Verify all visual effects apply correctly
    - Verify performance remains smooth (30+ FPS)
    - Verify keyboard controls for new features
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 10.1, 10.2, 10.3, 10.4, 10.5, 12.1, 12.2, 12.3, 12.4, 12.5_

  - [ ] 23.3 Test configuration combinations
    - Test different layout + transition + effects combinations
    - Verify no conflicts between features
    - Test performance with maximum effects enabled
    - Test graceful degradation when performance drops
    - _Requirements: 9.6, 10.7, 12.6, 12.7_

  - [ ]* 23.4 Run full enhanced property-based test suite
    - Execute all 27 property tests (original 18 + new 9)
    - Verify 100+ iterations per test
    - Fix any failures discovered
    - _All Requirements_

- [ ] 24. Final checkpoint - Ensure all enhanced tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties
- Unit tests validate specific examples and edge cases
- The build phase (tasks 1-6) can be completed independently before the run phase (tasks 7-13)
- Enhanced features (tasks 14-19) add configurable layouts, transitions, and visual effects
- New features leverage Processing.org's advanced graphics capabilities
- Integration testing (task 23) validates the complete enhanced system end-to-end
- Performance testing ensures smooth operation with all effects enabled
