# Requirements Document

## Introduction

iCandy is a Processing.org-based application that creates dynamic visual experiences by associating images with text scripts and displaying them in sync with audio beat detection. The system operates in two phases: a build phase that downloads and associates images with words, and a run phase that displays text phrases with images and swaps them based on audio beats.

## Glossary

- **iCandy**: The complete system that processes text scripts and generates visual output
- **Build_Phase**: The preprocessing stage where images are downloaded and associated with words
- **Run_Phase**: The execution stage where text phrases and images are displayed with beat-synchronized transitions
- **Text_Script**: The input text file containing text to be visualized
- **Image_Lookup**: The process of searching for and retrieving images associated with a word
- **Beat_Detection**: Audio analysis that identifies rhythmic beats to trigger image transitions
- **Text_Phrase**: A sentence or phrase displayed on screen like movie subtitles
- **Image_Association**: The mapping between words and their corresponding images
- **Processing_Application**: The main Processing.org sketch that renders the visual output
- **Stop_Words**: Common words (like "a", "the", "that") that are excluded from image lookup
- **Content_Words**: Meaningful words that have images associated with them
- **Layout_Algorithm**: The method used to position images on screen (grid, collage, circular, flowing)
- **Transition_Effect**: The visual animation used when swapping images (fade, slide, zoom, rotate, morph)
- **Animation_Parameters**: Configuration values that control transition timing and behavior
- **Visual_Effects**: Additional graphical enhancements like blur, color filters, or particle systems

## Requirements

### Requirement 1: Text Script Processing

**User Story:** As a user, I want to load text scripts into the system, so that I can prepare them for visual processing.

#### Acceptance Criteria

1. WHEN a text script file is provided, THE iCandy SHALL parse it into phrases and individual words
2. WHEN parsing text, THE iCandy SHALL organize text into displayable phrases by splitting on line breaks (one line per phrase)
3. WHEN parsing text, THE iCandy SHALL extract individual words for image association while preserving phrase structure
4. WHEN a text script contains special characters or punctuation, THE iCandy SHALL handle them appropriately for display
5. WHEN an invalid or empty text file is provided, THE iCandy SHALL return a descriptive error message

### Requirement 2: Image Lookup and Download (Build Phase)

**User Story:** As a user, I want the system to automatically find and download images for each word during the build phase, so that I have visual content ready for the run phase.

#### Acceptance Criteria

1. WHEN the build phase is invoked via command-line, THE iCandy SHALL accept a text file path as input
2. WHEN the build phase starts, THE iCandy SHALL iterate through all unique words in the text script
3. WHEN processing a word, THE iCandy SHALL skip stop words (common words like "a", "the", "that") and only process content words
4. WHEN processing a word, THE iCandy SHALL treat any word with length less than 3 characters as a stop word
5. WHEN processing a content word, THE iCandy SHALL perform an image lookup using an image search service
6. WHERE the number of images per word is configured, THE iCandy SHALL download exactly that number of images for each word
7. WHEN downloading images, THE iCandy SHALL store them locally with references to their associated words
8. WHEN an image download fails, THE iCandy SHALL log the error and continue processing remaining words
9. WHEN the build phase completes, THE iCandy SHALL persist all image associations for use in the run phase

### Requirement 3: Image Association Storage

**User Story:** As a developer, I want image associations to be stored persistently, so that the run phase can access them without rebuilding.

#### Acceptance Criteria

1. WHEN images are downloaded, THE iCandy SHALL create a mapping between each word and its associated image file paths
2. WHEN storing associations, THE iCandy SHALL use a format that allows efficient lookup during the run phase
3. WHEN the run phase starts, THE iCandy SHALL load all image associations from storage
4. WHEN loading associations, THE iCandy SHALL verify that referenced image files exist

### Requirement 4: Phrase Display with Images (Run Phase)

**User Story:** As a user, I want text phrases to be displayed on screen with their associated images like movie subtitles, so that I can experience the visual text presentation.

#### Acceptance Criteria

1. WHEN the run phase starts, THE Processing_Application SHALL display text phrases sequentially like movie subtitles
2. WHEN displaying a phrase, THE Processing_Application SHALL show it with subtitle-like styling at a readable position on screen
3. WHEN a phrase is displayed, THE Processing_Application SHALL show images associated with the content words in that phrase (excluding stop words)
4. WHERE the number of simultaneous images is configured, THE Processing_Application SHALL display that many images from the phrase's content words
5. WHEN calculating display duration, THE Processing_Application SHALL keep each phrase on screen long enough to be read comfortably
6. WHEN a phrase has finished displaying, THE Processing_Application SHALL transition to the next phrase in the sequence automatically
7. WHEN the last phrase is reached and finishes displaying, THE Processing_Application SHALL loop back to the first phrase
8. WHEN the user presses the right arrow key, THE Processing_Application SHALL swap the currently displayed images with different images from the same phrase's content words
9. WHEN the user presses the left arrow key, THE Processing_Application SHALL swap the currently displayed images with different images from the same phrase's content words

### Requirement 5: Beat Detection and Image Swapping

**User Story:** As a user, I want images to change in sync with audio beats, so that the visual experience is dynamic and rhythmically engaging.

#### Acceptance Criteria

1. WHEN the run phase starts, THE Processing_Application SHALL initialize audio input from the system microphone
2. WHEN audio is playing, THE Processing_Application SHALL continuously analyze it for beat detection
3. WHEN a beat is detected, THE Processing_Application SHALL swap the currently displayed images with different images from the same word's image set
4. WHEN swapping images, THE Processing_Application SHALL select images that have not been recently displayed for the current word
5. WHEN all images for a word have been displayed, THE Processing_Application SHALL cycle back through the available images
6. WHEN no beat is detected, THE Processing_Application SHALL maintain the current image display

### Requirement 6: Configuration Management

**User Story:** As a user, I want to configure system parameters like images per word and display timing, so that I can customize the visual experience.

#### Acceptance Criteria

1. THE iCandy SHALL provide a configuration mechanism for setting the number of images to download per word
2. THE iCandy SHALL provide configuration for phrase display duration parameters
3. THE iCandy SHALL provide configuration for beat detection sensitivity
4. WHEN configuration values are invalid, THE iCandy SHALL use sensible default values and log a warning
5. WHEN configuration is updated, THE iCandy SHALL apply changes without requiring code modifications

### Requirement 7: Processing.org Integration

**User Story:** As a developer, I want the application to use Processing.org APIs effectively, so that the visual rendering is smooth and performant.

#### Acceptance Criteria

1. THE Processing_Application SHALL use Processing.org drawing APIs for rendering text and images
2. THE Processing_Application SHALL use Processing.org audio libraries for beat detection
3. WHEN rendering frames, THE Processing_Application SHALL maintain a consistent frame rate for smooth visual output
4. WHEN loading images, THE Processing_Application SHALL use Processing's image loading capabilities
5. THE Processing_Application SHALL handle Processing's setup and draw loop appropriately

### Requirement 8: Error Handling and Resilience

**User Story:** As a user, I want the system to handle errors gracefully, so that issues don't crash the entire application.

#### Acceptance Criteria

1. WHEN an image file is missing during the run phase, THE Processing_Application SHALL display the phrase without images and log the error
2. WHEN beat detection fails, THE Processing_Application SHALL continue displaying phrases without beat-synchronized transitions
3. WHEN audio input is unavailable, THE Processing_Application SHALL operate in a fallback mode with timed image transitions
4. WHEN network errors occur during the build phase, THE iCandy SHALL retry failed downloads up to a configured limit
5. IF critical errors occur, THEN THE iCandy SHALL provide clear error messages indicating the problem and potential solutions

### Requirement 9: Configurable Image Layout Algorithms

**User Story:** As a user, I want to choose from different image layout algorithms, so that I can customize the visual arrangement of images on screen.

#### Acceptance Criteria

1. THE Processing_Application SHALL support multiple layout algorithms including grid, collage, circular, and flowing layouts
2. WHEN the grid layout is selected, THE Processing_Application SHALL arrange images in a regular rectangular grid with configurable rows and columns
3. WHEN the collage layout is selected, THE Processing_Application SHALL position images with random sizes, rotations, and overlapping for an artistic effect
4. WHEN the circular layout is selected, THE Processing_Application SHALL arrange images in a circle or spiral pattern around the screen center
5. WHEN the flowing layout is selected, THE Processing_Application SHALL position images along curved paths or organic shapes
6. WHERE a layout algorithm is configured, THE Processing_Application SHALL apply that algorithm to all image positioning
7. WHEN switching between layout algorithms, THE Processing_Application SHALL smoothly transition images to their new positions
8. WHEN a layout algorithm has configurable parameters, THE Processing_Application SHALL apply those parameters from the configuration

### Requirement 10: Configurable Transition Effects

**User Story:** As a user, I want to choose from different transition effects when images swap, so that I can create more engaging visual experiences.

#### Acceptance Criteria

1. THE Processing_Application SHALL support multiple transition effects including fade, slide, zoom, rotate, and morph transitions
2. WHEN the fade transition is selected, THE Processing_Application SHALL gradually change image opacity during swaps
3. WHEN the slide transition is selected, THE Processing_Application SHALL move images in from specified directions during swaps
4. WHEN the zoom transition is selected, THE Processing_Application SHALL scale images in or out during swaps
5. WHEN the rotate transition is selected, THE Processing_Application SHALL rotate images during swaps with configurable rotation angles
6. WHEN the morph transition is selected, THE Processing_Application SHALL blend between old and new images using shape interpolation
7. WHERE transition parameters are configured, THE Processing_Application SHALL use those parameters for timing, easing, and visual effects
8. WHEN multiple images swap simultaneously, THE Processing_Application SHALL coordinate transitions to create cohesive visual effects

### Requirement 11: Animation Parameter Configuration

**User Story:** As a user, I want to configure animation timing and behavior, so that I can fine-tune the visual experience to match my preferences.

#### Acceptance Criteria

1. THE Processing_Application SHALL provide configuration for transition duration with values between 100ms and 5000ms
2. THE Processing_Application SHALL provide configuration for easing functions including linear, ease-in, ease-out, and ease-in-out
3. THE Processing_Application SHALL provide configuration for stagger timing when multiple images transition simultaneously
4. WHEN stagger timing is configured, THE Processing_Application SHALL delay each image transition by the specified stagger amount
5. THE Processing_Application SHALL provide configuration for animation curves and interpolation methods
6. WHEN invalid animation parameters are provided, THE Processing_Application SHALL use sensible defaults and log a warning
7. WHEN animation parameters are updated, THE Processing_Application SHALL apply them to subsequent transitions without restart

### Requirement 12: Visual Effects and Enhancements

**User Story:** As a user, I want to apply visual effects to images, so that I can create more artistic and engaging presentations.

#### Acceptance Criteria

1. THE Processing_Application SHALL support optional blur effects with configurable blur radius
2. THE Processing_Application SHALL support color filter effects including sepia, grayscale, and color tinting
3. THE Processing_Application SHALL support brightness and contrast adjustments with configurable intensity
4. WHERE particle systems are enabled, THE Processing_Application SHALL generate particles around images during transitions
5. THE Processing_Application SHALL support image border effects including glow, shadow, and outline
6. WHEN visual effects are configured, THE Processing_Application SHALL apply them to all displayed images
7. WHEN visual effects impact performance, THE Processing_Application SHALL maintain target frame rate by reducing effect quality
8. WHEN visual effects are disabled, THE Processing_Application SHALL display images without additional processing

### Requirement 13: Layout Parameter Configuration

**User Story:** As a user, I want to configure layout-specific parameters, so that I can customize how images are positioned and sized within each layout algorithm.

#### Acceptance Criteria

1. WHERE grid layout is used, THE Processing_Application SHALL provide configuration for grid spacing, padding, and alignment
2. WHERE collage layout is used, THE Processing_Application SHALL provide configuration for size variation, rotation range, and overlap amount
3. WHERE circular layout is used, THE Processing_Application SHALL provide configuration for radius, arc span, and rotation direction
4. WHERE flowing layout is used, THE Processing_Application SHALL provide configuration for path curvature, flow direction, and spacing
5. THE Processing_Application SHALL provide configuration for global image scaling factors and aspect ratio handling
6. THE Processing_Application SHALL provide configuration for screen regions where images can be displayed
7. WHEN layout parameters are invalid, THE Processing_Application SHALL clamp values to valid ranges and log warnings
8. WHEN layout parameters are updated, THE Processing_Application SHALL recalculate positions and smoothly transition images

### Requirement 14: Interactive Keyboard Controls

**User Story:** As a user, I want to use keyboard shortcuts to switch between visual modes in real-time, so that I can experiment with different layouts and effects during playback.

#### Acceptance Criteria

1. WHEN the user presses number keys 1-4, THE Processing_Application SHALL switch to the corresponding layout algorithm (1=Grid, 2=Collage, 3=Circular, 4=Flowing)
2. WHEN the user presses letter keys Q-W-E-R-T, THE Processing_Application SHALL switch to the corresponding transition effect (Q=Fade, W=Slide, E=Zoom, R=Rotate, T=Morph)
3. WHEN the user presses function keys F1-F5, THE Processing_Application SHALL toggle the corresponding visual effect (F1=Blur, F2=Sepia, F3=Particles, F4=Glow, F5=Reset All)
4. WHEN the user presses the space bar, THE Processing_Application SHALL trigger an immediate image swap using the current transition effect
5. WHEN the user presses the tab key, THE Processing_Application SHALL display a settings overlay showing current layout, transition, and effects for a configurable duration
6. WHEN keyboard controls change settings, THE Processing_Application SHALL apply changes immediately without requiring restart
7. WHEN the settings overlay is displayed, THE Processing_Application SHALL show current FPS and performance information
8. WHERE keyboard navigation is disabled in configuration, THE Processing_Application SHALL ignore all keyboard input except arrow keys
