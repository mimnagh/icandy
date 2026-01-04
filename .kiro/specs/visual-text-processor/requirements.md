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
6. WHEN a phrase has finished displaying, THE Processing_Application SHALL transition to the next phrase in the sequence
7. WHEN the last phrase is reached and finishes displaying, THE Processing_Application SHALL loop back to the first phrase
8. WHEN the user presses the right arrow key, THE Processing_Application SHALL advance to the next phrase immediately
9. WHEN the user presses the left arrow key, THE Processing_Application SHALL go back to the previous phrase

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
