# iCandy Visual Text Processor

A Processing-based application that creates dynamic visual experiences by associating images with text scripts and displaying them in sync with audio beat detection.

## Project Structure

```
icandy/
├── bin/                                # Shell scripts
│   ├── build.sh                        # Compile project
│   ├── run-build.sh                    # Download images
│   ├── run-sketch.sh                   # Run visual display
│   ├── quick-start.sh                  # All-in-one script
│   ├── test.sh                         # Run tests
│   └── clean-data.sh                   # Clean data
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── icandy/
│   │               ├── build/          # Build phase components
│   │               ├── run/            # Run phase components
│   │               └── common/         # Shared utilities
│   └── test/
│       └── java/
│           └── com/
│               └── icandy/
│                   ├── properties/     # Property-based tests
│                   └── unit/           # Unit tests
├── data/
│   ├── images/                         # Downloaded images
│   ├── associations.json               # Word-to-image mappings
│   └── stopwords.txt                   # Stop words list
├── logs/                               # Application logs
├── scripts/                            # Setup scripts
│   ├── setup-config.sh                 # Configuration setup
│   └── setup-sound-library.sh          # Sound library setup
├── config.json.example                 # Configuration template
├── unsplash.properties.example         # Unsplash credentials template
└── pom.xml                             # Maven build configuration

~/.icandy/                              # User configuration directory
├── config.json                         # Active configuration
└── unsplash.properties                 # Unsplash API credentials
```

## Setup

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Unsplash API key (get one at https://unsplash.com/developers)

### Configuration

**Quick Setup (Recommended):**

```bash
./scripts/setup-config.sh
```

This will create `~/.icandy/config.json` and `~/.icandy/unsplash.properties` from the example files.

**Optional: Setup Processing Sound Library (for Beat Detection):**

To enable audio beat detection, download the Processing Sound library:

```bash
./scripts/setup-sound-library.sh
```

This will:
- Download the Processing Sound library from GitHub releases
- Extract and place all JAR files (sound.jar, jsyn.jar, and dependencies) in the `lib/` directory
- Provide instructions for adding them to your classpath

The sound library is optional. If not installed, the application will run without beat detection features and fall back to timed image transitions.

**Manual Setup:**

1. Create your iCandy configuration directory:
   ```bash
   mkdir -p ~/.icandy
   ```

2. Copy the configuration template:
   ```bash
   cp config.json.example ~/.icandy/config.json
   ```

3. Create your Unsplash properties file:
   ```bash
   cp unsplash.properties.example ~/.icandy/unsplash.properties
   ```

4. Edit `~/.icandy/unsplash.properties` and add your Unsplash API credentials:
   ```properties
   application_id=YOUR_APPLICATION_ID_HERE
   secret_key=YOUR_SECRET_KEY_HERE
   access_key=YOUR_ACCESS_KEY_HERE
   ```

5. (Optional) Customize settings in `~/.icandy/config.json` as needed (images per word, display duration, etc.)

### Build

```bash
mvn clean install
```

## Usage

### Quick Start with Shell Scripts

The easiest way to use iCandy is with the provided shell scripts in the `bin/` directory:

```bash
# 1. Build the project
bin/build.sh

# 2. Run build phase (download images)
bin/run-build.sh data/Maryhadalittlelamb.txt

# 3. Run visual display
bin/run-sketch.sh data/Maryhadalittlelamb.txt
```

Or use the all-in-one quick start:

```bash
bin/quick-start.sh
```

### Shell Scripts Reference

All scripts are located in the `bin/` directory.

#### `bin/build.sh`
Compiles the project and creates an executable JAR.

```bash
bin/build.sh
```

#### `bin/run-build.sh`
Runs the build phase to download images and create associations.

```bash
bin/run-build.sh <text-file> [config-file]

# Examples:
bin/run-build.sh data/Maryhadalittlelamb.txt
bin/run-build.sh my_script.txt custom_config.json
```

#### `bin/run-sketch.sh`
Runs the visual display (Processing sketch).

```bash
bin/run-sketch.sh <text-file> [config-file]

# Examples:
bin/run-sketch.sh data/Maryhadalittlelamb.txt
bin/run-sketch.sh my_script.txt custom_config.json
```

#### `bin/test.sh`
Runs unit tests and property-based tests.

```bash
# Run all tests
bin/test.sh

# Run specific test class
bin/test.sh ImageDisplayManagerBasicTest
```

#### `bin/quick-start.sh`
Runs the complete workflow: build, download images, and display.

```bash
bin/quick-start.sh
```

#### `bin/clean-data.sh`
Removes downloaded images and associations to start fresh.

```bash
bin/clean-data.sh
```

### Manual Usage (Advanced)

#### Build Phase

Process a text script and download images:

```bash
java -jar target/icandy-1.0.0.jar <path-to-text-file>
```

Or with Maven:

```bash
mvn exec:java -Dexec.mainClass="com.icandy.build.BuildMain" -Dexec.args="<path-to-text-file>"
```

**Incremental Builds:**

The build phase is incremental - it will skip downloading images for words that already have sufficient images. This means:
- Running the build phase multiple times on the same text is fast (only new words are processed)
- You can add new text to your script and only download images for new words
- Existing associations are preserved and merged with new ones
- **Associations are saved after each successful word download**, so you can safely interrupt the build process (Ctrl+C) without losing progress

Example:
```bash
# First build: downloads images for "hello" and "world"
mvn exec:java -Dexec.mainClass="com.icandy.build.BuildMain" \
  -Dexec.args="data/text1.txt"

# Second build: skips "hello" and "world", only downloads for "test"
mvn exec:java -Dexec.mainClass="com.icandy.build.BuildMain" \
  -Dexec.args="data/text2.txt"

# If you interrupt the build (Ctrl+C), all successfully downloaded words are saved
# Just run the build again to continue from where you left off
```

This saves API requests and time when iterating on your text scripts.

### Run Phase

After completing the build phase, run the Processing sketch to display the visual experience.

**Prerequisites:**
- Build phase must be completed first (associations.json must exist)
- Text file used in build phase

**Option 1: Use Shell Script (Recommended)**

```bash
bin/run-sketch.sh <path-to-text-file> [path-to-config.json]

# Examples:
bin/run-sketch.sh data/Maryhadalittlelamb.txt
bin/run-sketch.sh my_script.txt ~/.icandy/config.json
```

**Option 2: Run with Maven**

```bash
mvn exec:java -Dexec.mainClass="com.icandy.run.iCandySketch" \
  -Dexec.args="<path-to-text-file> [path-to-config.json]"
```

**Note**: When specifying paths with `~`, use the full expanded path or let the shell expand it:

```bash
# Option A: Use full path
mvn exec:java -Dexec.mainClass="com.icandy.run.iCandySketch" \
  -Dexec.args="data/Maryhadalittlelamb.txt $HOME/.icandy/config.json"

# Option B: Let the application expand ~ (works now)
mvn exec:java -Dexec.mainClass="com.icandy.run.iCandySketch" \
  -Dexec.args="data/Maryhadalittlelamb.txt ~/.icandy/config.json"
```

For large text files or many images, you may need to increase the Java heap size:

```bash
export MAVEN_OPTS="-Xmx2g"
mvn exec:java -Dexec.mainClass="com.icandy.run.iCandySketch" \
  -Dexec.args="<path-to-text-file> [path-to-config.json]"
```

Example:
```bash
mvn exec:java -Dexec.mainClass="com.icandy.run.iCandySketch" \
  -Dexec.args="data/Maryhadalittlelamb.txt ~/.icandy/config.json"
```

**Option 3: Run with Java directly**

First, ensure all dependencies are in the classpath:

```bash
# Build the project with dependencies
mvn clean package

# Run the sketch with increased heap size
java -Xmx2g -cp "target/icandy-1.0.0.jar:target/lib/*" \
  com.icandy.run.iCandySketch \
  <path-to-text-file> [path-to-config.json]
```

**Option 3a: Run with Sound Library (for Beat Detection)**

If you've installed the Processing Sound library using `./scripts/setup-sound-library.sh`:

```bash
# Run with sound library and all dependencies in classpath
java -Xmx2g -cp "target/icandy-1.0.0.jar:lib/*:target/lib/*" \
  com.icandy.run.iCandySketch \
  <path-to-text-file> [path-to-config.json]
```

**Note**: The sound library requires multiple JAR files (sound.jar, jsyn.jar, and others), so use `lib/*` to include all of them.

With the sound library, the application will:
- Initialize audio input from your microphone
- Detect beats in the audio
- Swap images in sync with detected beats
- Fall back to timed transitions if audio input fails

**Option 4: Quick Start (using default config)**

If you've set up `~/.icandy/config.json`, you can omit the config path:

```bash
mvn exec:java -Dexec.mainClass="com.icandy.run.iCandySketch" \
  -Dexec.args="data/Maryhadalittlelamb.txt"
```

The sketch will:
1. Load configuration from `config.json` (or use defaults)
2. Load word-image associations from `data/associations.json`
3. Parse the text file into phrases
4. Open a 1280x720 window
5. Display phrases with associated images
6. Automatically advance through phrases
7. Respond to keyboard input (arrow keys swap images)

**Note:** Beat detection (audio-synchronized image swapping) will be available after Task 10 is completed.

### Interactive Keyboard Controls

iCandy provides comprehensive keyboard controls for real-time interaction with layouts, transitions, and visual effects:

#### Basic Navigation
- **Right Arrow**: Swap images within current phrase (text stays the same)
- **Left Arrow**: Swap images within current phrase (text stays the same)
- **Space Bar**: Trigger manual image swap using current transition effect

#### Layout Algorithm Controls
- **1**: Switch to Grid Layout - Regular rectangular arrangement
- **2**: Switch to Collage Layout - Artistic arrangement with varied sizes and rotations
- **3**: Switch to Circular Layout - Circular or spiral patterns around screen center
- **4**: Switch to Flowing Layout - Images positioned along curved paths

#### Transition Effect Controls
- **Q**: Switch to Fade Transition - Gradual opacity changes
- **W**: Switch to Slide Transition - Images slide in from specified directions
- **E**: Switch to Zoom Transition - Images scale in or out during swaps
- **R**: Switch to Rotate Transition - Images rotate during transitions
- **T**: Switch to Morph Transition - Advanced blending between image states

#### Visual Effects Controls
- **F1**: Toggle Blur Effect - Gaussian blur with configurable radius
- **F2**: Toggle Sepia Effect - Vintage sepia tone color filter
- **F3**: Toggle Particle System - Animated particles around images
- **F4**: Toggle Glow Effect - Glowing outline around images
- **F5**: Reset All Effects - Disable all visual effects

#### Information Display
- **Tab**: Show Settings Overlay - Display current configuration and performance metrics

**Notes**: 
- Text phrases advance automatically based on timing
- Keyboard controls apply changes immediately without restart
- Settings overlay shows current layout, transition, effects, and FPS
- All changes are temporary - restart to return to configuration defaults

## Complete Example Workflow

Here's a complete example from start to finish using the shell scripts:

### Quick Method (Recommended)

```bash
# 1. Setup (one-time)
./scripts/setup-config.sh
nano ~/.icandy/unsplash.properties  # Add your API credentials

# 2. Run everything
bin/quick-start.sh
```

### Step-by-Step Method

```bash
# 1. Setup (one-time)
./scripts/setup-config.sh
nano ~/.icandy/unsplash.properties  # Add your API credentials

# 2. Build the project
bin/build.sh

# 3. Run build phase (download images)
bin/run-build.sh data/Maryhadalittlelamb.txt

# 4. Run visual display
bin/run-sketch.sh data/Maryhadalittlelamb.txt
```

### Manual Method (Advanced)

Here's a complete example from start to finish using Maven directly:

### 1. Setup (One-time)

```bash
# Run the setup script
./scripts/setup-config.sh

# Edit your Unsplash credentials
nano ~/.icandy/unsplash.properties
# Add your application_id, secret_key, and access_key
```

### 2. Build Phase (Process Text and Download Images)

```bash
# Build the project
mvn clean install

# Run build phase on sample text
mvn exec:java -Dexec.mainClass="com.icandy.build.BuildMain" \
  -Dexec.args="data/Maryhadalittlelamb.txt"
```

This will:
- Parse the text into phrases and words
- Filter out stop words
- Download 5 images per content word (configurable)
- Save associations to `data/associations.json`
- Store images in `data/images/`

### 3. Run Phase (Display Visual Experience)

```bash
# Run the Processing sketch
mvn exec:java -Dexec.mainClass="com.icandy.run.iCandySketch" \
  -Dexec.args="data/Maryhadalittlelamb.txt ~/.icandy/config.json"
```

This will:
- Open a 1280x720 window
- Display phrases like movie subtitles
- Show associated images for each phrase
- Automatically advance through phrases
- Allow image swapping with arrow keys

### 4. Interact

- Watch phrases advance automatically (timing based on word count)
- Press **Right Arrow** to swap images within current phrase
- Press **Left Arrow** to swap images within current phrase
- Close the window to exit

## Using Your Own Text

### With Shell Scripts (Recommended)

Create a text file with your content:

```bash
echo "The quick brown fox jumps over the lazy dog." > mytext.txt
echo "This is a test of the visual text processor." >> mytext.txt
```

Run the build and display:

```bash
bin/build.sh                    # Compile (if not already done)
bin/run-build.sh mytext.txt     # Download images
bin/run-sketch.sh mytext.txt    # Display visual experience
```

### With Maven (Advanced)

Create a text file with your content:

```bash
echo "The quick brown fox jumps over the lazy dog." > mytext.txt
echo "This is a test of the visual text processor." >> mytext.txt
```

Run the build phase:

```bash
mvn exec:java -Dexec.mainClass="com.icandy.build.BuildMain" \
  -Dexec.args="mytext.txt"
```

Run the visual display:

```bash
mvn exec:java -Dexec.mainClass="com.icandy.run.iCandySketch" \
  -Dexec.args="mytext.txt"
```

## Layout Algorithms

iCandy supports four different layout algorithms for positioning images on screen:

### Grid Layout (Key: 1)
Arranges images in a regular rectangular grid pattern.

**Visual Characteristics:**
- Uniform spacing between images
- Predictable, organized appearance
- Good for presentations and formal displays
- Automatic grid sizing based on image count

**Configuration Parameters:**
- `spacing`: Space between images in pixels (0-100)
- `padding`: Padding around the grid in pixels (0-200)
- `alignment`: Grid alignment - "center", "left", "right", "top", "bottom"

**Best For:** Professional presentations, educational content, structured displays

### Collage Layout (Key: 2)
Creates artistic collage-style arrangements with varied sizes and rotations.

**Visual Characteristics:**
- Random sizes within configured range
- Random rotations for organic feel
- Controlled overlap for artistic effect
- Non-uniform, creative positioning

**Configuration Parameters:**
- `minSize`/`maxSize`: Size variation range (0.1-2.0)
- `minRotation`/`maxRotation`: Rotation angle range (-180 to 180 degrees)
- `overlapAmount`: Amount of overlap allowed (0.0-0.5)

**Best For:** Artistic presentations, creative displays, mood boards

### Circular Layout (Key: 3)
Arranges images in circular or spiral patterns around the screen center.

**Visual Characteristics:**
- Images positioned along circular paths
- Configurable radius and arc span
- Spiral mode for dynamic arrangements
- Rotation direction control

**Configuration Parameters:**
- `radius`: Circle radius in pixels (100-800)
- `arcSpan`: Arc span in degrees (90-360)
- `rotationDirection`: "clockwise" or "counterclockwise"

**Best For:** Dynamic presentations, focal point displays, radial compositions

### Flowing Layout (Key: 4)
Positions images along curved Bezier paths for organic arrangements.

**Visual Characteristics:**
- Images follow curved paths
- Organic, natural spacing
- Multiple flow paths for complex layouts
- Configurable curvature and direction

**Configuration Parameters:**
- `pathCurvature`: Path curvature amount (0.0-2.0)
- `flowDirection`: "horizontal", "vertical", "diagonal"
- `pathSpacing`: Spacing along paths in pixels (50-300)

**Best For:** Organic presentations, storytelling, natural compositions

## Transition Effects

iCandy provides five different transition effects for smooth image swapping:

### Fade Transition (Key: Q)
Gradually changes image opacity during swaps.

**Visual Characteristics:**
- Smooth opacity transitions
- Cross-fade between old and new images
- Gentle, non-intrusive effect
- Works well with all layout algorithms

**Configuration Parameters:**
- `duration`: Transition duration in milliseconds (100-5000)
- `easingFunction`: Timing curve - "linear", "ease-in", "ease-out", "ease-in-out"

**Best For:** Subtle transitions, professional presentations, gentle mood changes

### Slide Transition (Key: W)
Moves images in from specified directions during swaps.

**Visual Characteristics:**
- Images slide in from edges
- Directional movement creates energy
- Bounce and overshoot effects available
- Staggered timing for multiple images

**Configuration Parameters:**
- `slideDirection`: "up", "down", "left", "right", "diagonal"
- `staggerDelay`: Delay between multiple transitions (0-1000ms)
- `enableStagger`: Enable staggered timing

**Best For:** Dynamic presentations, energetic displays, directional storytelling

### Zoom Transition (Key: E)
Scales images in or out during swaps.

**Visual Characteristics:**
- Images scale from/to center point
- Zoom in (0 to 1) or zoom out (1 to 0)
- Combined with fade for smooth effect
- Configurable zoom center point

**Configuration Parameters:**
- `zoomMode`: "in" (scale from 0 to 1) or "out" (scale from 1 to 0)
- `duration`: Transition duration
- Combined with fade opacity changes

**Best For:** Dramatic reveals, focus changes, impact moments

### Rotate Transition (Key: R)
Rotates images during transitions with configurable angles.

**Visual Characteristics:**
- Images rotate around center point
- Configurable rotation angle and direction
- Combined with scale and fade effects
- 3D rotation effects possible

**Configuration Parameters:**
- `rotationAngle`: Rotation angle in degrees (0-360)
- Can be combined with other effects
- Uses Processing's transform functions

**Best For:** Dynamic presentations, playful displays, attention-grabbing effects

### Morph Transition (Key: T)
Advanced blending between image shapes and positions.

**Visual Characteristics:**
- Shape interpolation between states
- Color blending and morphing effects
- Organic, fluid transitions
- Advanced vertex manipulation

**Configuration Parameters:**
- `blendMode`: "normal", "multiply", "screen", "overlay"
- `duration`: Morph duration
- Advanced Processing blend functions

**Best For:** Artistic presentations, creative displays, experimental visuals

## Visual Effects

iCandy offers comprehensive visual effects to enhance image appearance:

### Blur Effects (Key: F1)
Applies Gaussian blur with configurable radius.

**Types Available:**
- **Gaussian Blur**: Standard blur with configurable radius
- **Motion Blur**: For moving images (future enhancement)
- **Selective Blur**: Blur background, keep foreground sharp (future enhancement)

**Configuration:**
- `blurRadius`: Blur radius in pixels (0.5-10.0)
- `enableBlur`: Toggle blur on/off

**Performance Impact:** Medium - may reduce frame rate with large radius

### Color Filters (Key: F2)
Applies various color transformations to images.

**Available Filters:**
- **Sepia**: Vintage sepia tone effect
- **Grayscale**: Black and white conversion
- **Vintage**: Retro color grading
- **Custom Tint**: Configurable hue/saturation

**Configuration:**
- `colorFilter`: "none", "sepia", "grayscale", "vintage"
- `colorIntensity`: Filter intensity (0.0-2.0)
- `tintColor`: Custom tint color in hex format

**Performance Impact:** Low - minimal frame rate impact

### Particle Systems (Key: F3)
Generates animated particles around images during transitions.

**Particle Types:**
- **Sparkles**: Golden sparkle effects
- **Smoke**: Wispy smoke particles (future enhancement)
- **Fire**: Fire-like particles (future enhancement)
- **Snow**: Falling snow particles (future enhancement)

**Configuration:**
- `particleCount`: Number of particles (10-200)
- `particleType`: Type of particles
- `particleColor`: Particle color in hex format
- `particleLifetime`: Particle duration in milliseconds (500-5000)

**Performance Impact:** High - may significantly impact frame rate

### Border Effects (Key: F4)
Adds various border and outline effects to images.

**Available Effects:**
- **Glow**: Soft glow around image edges
- **Drop Shadow**: Shadow with configurable offset
- **Outline**: Stroke with configurable thickness
- **Vintage Frame**: Decorative frame effects (future enhancement)

**Configuration:**
- `glowRadius`: Glow radius in pixels (2-50)
- `glowColor`: Glow color in hex format
- `shadowOffset`: Shadow offset in pixels (1-20)

**Performance Impact:** Medium - moderate frame rate impact

### Effect Reset (Key: F5)
Disables all visual effects and returns to clean image display.

**Function:**
- Instantly disables all active visual effects
- Returns to original image appearance
- Useful for performance optimization
- Can be re-enabled individually

## Configuration Options

## Configuration Options

iCandy uses a comprehensive JSON configuration system with support for multiple presets:

### Configuration Files

- **Main Configuration**: `config.json.example` - Full-featured configuration with all options
- **Minimal Preset**: `config_minimal.json.example` - Basic functionality, best performance
- **Artistic Preset**: `config_artistic.json.example` - Maximum visual impact and creativity
- **Performance Preset**: `config_performance.json.example` - Balanced settings for reliable performance

Copy your preferred preset to `~/.icandy/config.json` and customize as needed.

### Build Phase Configuration

- `imagesPerWord`: Number of images to download per word (1-10, default: 5)
- `unsplashPropertiesFile`: Path to Unsplash credentials file (default: ~/.icandy/unsplash.properties)
- `imageStorageDir`: Directory for storing images (default: data/images)
- `associationsFile`: Path to associations file (default: data/associations.json)
- `stopWordsFile`: Path to stop words file (default: data/stopwords.txt)
- `maxRetries`: Maximum retry attempts for failed downloads (1-10, default: 3)

### Run Phase Configuration

#### Basic Display Settings
- `beatSensitivity`: Minimum time between beats in ms (50-500, default: 100)
- `minPhraseDuration`: Minimum phrase display time in ms (default: 2000)
- `maxPhraseDuration`: Maximum phrase display time in ms (default: 10000)
- `msPerWord`: Milliseconds per word for duration calculation (200-500, default: 300)
- `frameRate`: Target frame rate (15-60, default: 30)
- `textSize`: Font size for phrases (24-72, default: 48)
- `textColor`: Text color in hex format (default: #FFFFFF)
- `backgroundColor`: Background color in hex format (default: #000000)

#### Interaction Settings
- `enableKeyboardNavigation`: Enable interactive keyboard controls (default: true)
- `simultaneousImageCount`: Number of images to display at once (1-8, default: 3)
- `loopPhrases`: Loop back to first phrase after last (default: true)
- `audioSource`: Audio input source - "microphone" or "none" (default: microphone)
- `showSettingsOverlay`: Show settings overlay when Tab is pressed (default: true)
- `settingsOverlayDuration`: Settings overlay display duration in ms (default: 3000)

### Layout Configuration

#### Global Layout Settings
- `algorithm`: Layout algorithm - "grid", "collage", "circular", "flowing" (default: grid)
- `displayRegion`: Screen region for image display (x, y, width, height)
- `globalScale`: Global scaling factor for all images (0.1-3.0, default: 1.0)
- `aspectRatioMode`: Aspect ratio handling - "preserve", "stretch", "crop" (default: preserve)

#### Grid Layout Parameters
- `spacing`: Space between images in pixels (0-100, default: 20)
- `padding`: Padding around the grid in pixels (0-200, default: 40)
- `alignment`: Grid alignment - "center", "left", "right", "top", "bottom" (default: center)

#### Collage Layout Parameters
- `minSize`: Minimum image size as fraction (0.1-1.0, default: 0.3)
- `maxSize`: Maximum image size as fraction (0.1-2.0, default: 0.8)
- `minRotation`: Minimum rotation angle in degrees (-180 to 180, default: -15)
- `maxRotation`: Maximum rotation angle in degrees (-180 to 180, default: 15)
- `overlapAmount`: Amount of overlap allowed (0.0-0.5, default: 0.1)

#### Circular Layout Parameters
- `radius`: Circle radius in pixels (100-800, default: 300)
- `arcSpan`: Arc span in degrees (90-360, default: 360)
- `rotationDirection`: Rotation direction - "clockwise" or "counterclockwise" (default: clockwise)

#### Flowing Layout Parameters
- `pathCurvature`: Path curvature amount (0.0-2.0, default: 0.5)
- `flowDirection`: Flow direction - "horizontal", "vertical", "diagonal" (default: horizontal)
- `pathSpacing`: Spacing along paths in pixels (50-300, default: 100)

### Transition Configuration

#### Basic Transition Settings
- `effect`: Transition effect - "fade", "slide", "zoom", "rotate", "morph" (default: fade)
- `duration`: Transition duration in milliseconds (100-5000, default: 800)
- `easingFunction`: Easing function - "linear", "ease-in", "ease-out", "ease-in-out" (default: ease-out)
- `staggerDelay`: Delay between multiple transitions in ms (0-1000, default: 100)
- `enableStagger`: Enable staggered timing for multiple transitions (default: true)

#### Effect-Specific Parameters
- `slideDirection`: Slide direction - "up", "down", "left", "right", "diagonal" (default: up)
- `zoomMode`: Zoom mode - "in" (scale from 0 to 1) or "out" (scale from 1 to 0) (default: in)
- `rotationAngle`: Rotation angle in degrees (0-360, default: 45)
- `blendMode`: Blend mode for morph - "normal", "multiply", "screen", "overlay" (default: normal)

### Visual Effects Configuration

#### Blur Effects
- `enableBlur`: Enable blur effects (default: false)
- `blurRadius`: Blur radius in pixels (0.5-10.0, default: 2.0)

#### Color Filters
- `colorFilter`: Color filter - "none", "sepia", "grayscale", "vintage" (default: none)
- `colorIntensity`: Color filter intensity (0.0-2.0, default: 1.0)
- `tintColor`: Tint color in hex format (default: #FFFFFF)

#### Brightness/Contrast
- `brightness`: Brightness adjustment (-1.0 to 1.0, default: 0.0)
- `contrast`: Contrast adjustment (-1.0 to 1.0, default: 0.0)
- `gamma`: Gamma correction (0.1-3.0, default: 1.0)

#### Particle Systems
- `enableParticles`: Enable particle systems (default: false)
- `particleCount`: Number of particles (10-200, default: 50)
- `particleType`: Particle type - "sparkles", "smoke", "fire", "snow" (default: sparkles)
- `particleColor`: Particle color in hex format (default: #FFD700)
- `particleLifetime`: Particle lifetime in milliseconds (500-5000, default: 2000)

#### Border Effects
- `enableGlow`: Enable glow effect (default: false)
- `glowRadius`: Glow radius in pixels (2-50, default: 10)
- `glowColor`: Glow color in hex format (default: #FFFFFF)
- `enableShadow`: Enable drop shadow (default: false)
- `shadowOffset`: Shadow offset in pixels (1-20, default: 5)

### Keyboard Control Mappings

The keyboard controls can be customized in the configuration:

#### Layout Keys (Number Keys)
- `"1"`: "grid" - Switch to grid layout
- `"2"`: "collage" - Switch to collage layout
- `"3"`: "circular" - Switch to circular layout
- `"4"`: "flowing" - Switch to flowing layout

#### Transition Keys (Letter Keys)
- `"q"`: "fade" - Switch to fade transition
- `"w"`: "slide" - Switch to slide transition
- `"e"`: "zoom" - Switch to zoom transition
- `"r"`: "rotate" - Switch to rotate transition
- `"t"`: "morph" - Switch to morph transition

#### Effect Keys (Function Keys)
- `"F1"`: "blur" - Toggle blur effect
- `"F2"`: "sepia" - Toggle sepia effect
- `"F3"`: "particles" - Toggle particle system
- `"F4"`: "glow" - Toggle glow effect
- `"F5"`: "reset" - Reset all effects

## Testing

### Using Shell Script (Recommended)

Run all tests:

```bash
bin/test.sh
```

Run specific test class:

```bash
bin/test.sh ImageDisplayManagerBasicTest
```

### Using Maven (Advanced)

### Automated Tests

Run all tests:

```bash
mvn test
```

Run only unit tests:

```bash
mvn test -Dtest="*Test"
```

Run only property-based tests:

```bash
mvn test -Dtest="*PropertiesTest"
```

### Manual Testing

#### Test ImageDownloader

Verify that the ImageDownloader can connect to Unsplash and download images:

```bash
mvn exec:java -Dexec.mainClass="com.icandy.build.ImageDownloaderManualTest"
```

This will:
1. Load your Unsplash credentials
2. Search for sample images
3. Download a test image to `data/images/test_sunset.jpg`
4. Verify error handling

You can view the downloaded test image:

```bash
open data/images/test_sunset.jpg
```

**Note**: Make sure you've set up your Unsplash credentials in `~/.icandy/unsplash.properties` before running this test.

## Performance Tuning

iCandy's performance can be optimized based on your hardware and requirements:

### Hardware Recommendations

#### Minimum Requirements
- **CPU**: Dual-core 2.0GHz or equivalent
- **RAM**: 4GB (2GB for Java heap)
- **Graphics**: Integrated graphics with OpenGL support
- **Configuration**: Use `config_minimal.json.example`

#### Recommended Requirements
- **CPU**: Quad-core 2.5GHz or equivalent
- **RAM**: 8GB (4GB for Java heap)
- **Graphics**: Dedicated graphics card
- **Configuration**: Use `config_performance.json.example`

#### High-End Setup
- **CPU**: Multi-core 3.0GHz+ processor
- **RAM**: 16GB+ (8GB+ for Java heap)
- **Graphics**: Modern dedicated graphics card
- **Configuration**: Use `config_artistic.json.example` or custom settings

### Performance Optimization Tips

#### For Better Frame Rate
1. **Reduce Visual Complexity**:
   - Lower `simultaneousImageCount` (try 2-3 instead of 4-5)
   - Disable expensive effects: particles, blur, glow
   - Use simpler transitions: fade instead of morph
   - Choose grid layout over collage for better performance

2. **Adjust Rendering Settings**:
   - Lower `frameRate` to 24 or 15 if needed
   - Reduce `globalScale` to display smaller images
   - Use `config_minimal.json.example` as starting point

3. **Java Memory Settings**:
   ```bash
   # Increase heap size for large text files
   export MAVEN_OPTS="-Xmx4g -XX:+UseG1GC"
   
   # Or when running directly with Java
   java -Xmx4g -XX:+UseG1GC -cp "target/icandy-1.0.0.jar:lib/*:target/lib/*" \
     com.icandy.run.iCandySketch mytext.txt
   ```

#### For Lower Memory Usage
1. **Reduce Image Count**:
   - Lower `imagesPerWord` during build phase (try 3 instead of 5)
   - Use smaller `simultaneousImageCount` during run phase
   - Process shorter text files or split long texts

2. **Optimize Configuration**:
   - Disable particle systems (`enableParticles: false`)
   - Reduce `particleCount` if particles are needed
   - Use simpler visual effects

#### Performance Monitoring
- Press **Tab** to show performance overlay with current FPS
- Monitor Java memory usage with system tools
- Watch for frame drops during transitions
- Check logs for performance warnings

### Effect Performance Impact

#### Low Impact (Minimal frame rate reduction)
- **Fade transitions**: Very efficient
- **Grid layout**: Fastest positioning algorithm
- **Color filters**: Sepia, grayscale (GPU accelerated)
- **Drop shadows**: Simple offset rendering

#### Medium Impact (Moderate frame rate reduction)
- **Slide/Zoom/Rotate transitions**: More complex calculations
- **Circular/Flowing layouts**: Curved path calculations
- **Blur effects**: Depends on radius size
- **Glow effects**: Multiple rendering passes

#### High Impact (Significant frame rate reduction)
- **Morph transitions**: Complex blending calculations
- **Collage layout**: Collision detection and random positioning
- **Particle systems**: Many animated objects
- **Large blur radius**: Expensive filter operations

### Troubleshooting Performance Issues

#### Frame Rate Drops
1. **Check Current Settings**: Press Tab to see active effects and FPS
2. **Disable Effects Gradually**: Use F5 to reset all effects, then enable one by one
3. **Switch to Simpler Layout**: Press 1 for grid layout
4. **Use Fade Transitions**: Press Q for fade transitions
5. **Reduce Image Count**: Lower `simultaneousImageCount` in config

#### Memory Issues
1. **Increase Heap Size**: Use `-Xmx4g` or higher
2. **Reduce Image Count**: Lower `imagesPerWord` and rebuild
3. **Use Garbage Collection**: Add `-XX:+UseG1GC` flag
4. **Monitor Memory**: Use system tools to watch Java memory usage

#### Startup Performance
1. **Preload Images**: Build phase creates optimized associations
2. **Use SSD Storage**: Faster image loading from solid-state drives
3. **Local Images**: Avoid network-mounted image directories
4. **Incremental Builds**: Only new words require image downloads

## Interactive Usage Examples

### Example 1: Real-time Layout Experimentation
```bash
# Start with a text file
bin/run-sketch.sh data/sample_medium.txt

# Try different layouts:
# Press 1 - See organized grid arrangement
# Press 2 - Switch to artistic collage style
# Press 3 - Arrange in circular pattern
# Press 4 - Flow along curved paths
# Press Tab - Check performance impact
```

### Example 2: Transition Effect Showcase
```bash
# Start the visual display
bin/run-sketch.sh data/sample_long.txt

# Experiment with transitions:
# Press Q - Gentle fade transitions
# Press W - Dynamic slide effects
# Press E - Dramatic zoom effects
# Press R - Rotating image swaps
# Press T - Advanced morphing (high-end hardware)
# Press Space - Trigger manual transitions
```

### Example 3: Visual Effects Exploration
```bash
# Start with artistic preset
cp config_artistic.json.example ~/.icandy/config.json
bin/run-sketch.sh data/sample_short.txt

# Try different effects:
# Press F1 - Add blur for dreamy effect
# Press F2 - Apply sepia for vintage look
# Press F3 - Enable sparkle particles
# Press F4 - Add glowing outlines
# Press F5 - Reset to clean images
# Press Tab - Monitor performance impact
```

### Example 4: Performance Optimization Workflow
```bash
# Start with minimal config for best performance
cp config_minimal.json.example ~/.icandy/config.json
bin/run-sketch.sh data/sample_long.txt

# Gradually add features:
# Press Tab - Check baseline FPS
# Press 2 - Try collage layout, check FPS
# Press W - Try slide transitions, check FPS
# Press F1 - Add blur, monitor performance
# Press F5 - Reset if frame rate drops too low
```

## Troubleshooting

### Build Phase Issues

**"Associations file not found" error:**
- Make sure you've run the build phase first
- Check that `data/associations.json` exists
- Verify the path in your config.json matches the actual file location

**"Text file not found" error:**
- Verify the text file path is correct
- Use absolute paths or paths relative to where you run the command

**Unsplash API errors:**
- Verify your credentials in `~/.icandy/unsplash.properties`
- Check you haven't exceeded the rate limit (50 requests/hour for free tier)
- Ensure you have internet connectivity

### Run Phase Issues

**Window doesn't open:**
- Ensure Processing core library is in the classpath
- Try running with Maven instead of direct Java command
- Check that Java has permission to create windows on your system

**NullPointerException about "url" during startup:**
- This is a harmless warning from Processing's icon loading
- The message "Cannot invoke java.net.URL.toString() because url is null" can be safely ignored
- The application will continue to run normally and display the visual experience
- This is a known Processing framework issue when running without a custom icon

**"No phrases found" error:**
- Verify your text file contains actual text
- Check that the text has line breaks (each line becomes a phrase)
- Ensure the file encoding is UTF-8

**Images not displaying:**
- Check that the build phase completed successfully
- Verify images exist in `data/images/`
- Check logs for missing image warnings
- Ensure image paths in `data/associations.json` are correct

**Phrases advancing too quickly/slowly:**
- Adjust `msPerWord` in config.json (default: 300ms per word)
- Adjust `minPhraseDuration` and `maxPhraseDuration` for bounds
- Formula: duration = (wordCount × msPerWord) + 1000ms

**Keyboard navigation not working:**
- Check that `enableKeyboardNavigation` is true in config.json
- Ensure the Processing window has focus (click on it)
- Try clicking in the window before pressing arrow keys
- Remember: arrow keys only swap images, they don't change text phrases

### Interactive Features Issues

**Keyboard controls not responding:**
- Ensure the Processing window has focus (click on it)
- Check that `enableKeyboardNavigation` is true in config.json
- Try clicking in the window before pressing keys
- Verify the window is not minimized or hidden behind other windows

**Layout switching not working (keys 1-4):**
- Confirm the window has focus
- Check logs for layout algorithm errors
- Verify all layout algorithms are properly configured
- Try switching to grid layout (key 1) first as a baseline

**Transition effects not changing (keys Q-W-E-R-T):**
- Trigger a manual image swap (Space bar) to see the current transition
- Check that images are available for the current phrase
- Verify transition configuration in config.json
- Look for transition engine errors in logs

**Visual effects not applying (keys F1-F5):**
- Check performance - effects may be disabled automatically if FPS is too low
- Verify visual effects configuration in config.json
- Monitor system resources - effects require additional GPU/CPU power
- Try effects individually to identify problematic ones

**Settings overlay not showing (Tab key):**
- Check that `showSettingsOverlay` is true in config.json
- Verify `settingsOverlayDuration` is set to a reasonable value (2000-5000ms)
- Look for overlay rendering errors in logs
- Try pressing Tab multiple times with different timing

**Performance drops with interactive features:**
- Use Tab to monitor FPS in real-time
- Disable expensive effects: Press F5 to reset all effects
- Switch to grid layout (key 1) for better performance
- Use fade transitions (key Q) for minimal performance impact
- Check the Performance Tuning section above for optimization tips

**Effects not persisting between sessions:**
- Interactive changes are temporary and reset on restart
- To make changes permanent, update your config.json file
- Use the configuration presets as starting points
- Copy settings from the overlay display (Tab key) to your config file

### Audio Input Issues

**Beat detection not working:**
- Ensure the Processing Sound library is installed: `./scripts/setup-sound-library.sh`
- Add `lib/sound.jar` to your classpath when running
- Check that your microphone is connected and working
- Verify microphone permissions are granted to Java
- Check logs for "Processing Sound library not found" warnings

**"Processing Sound library not found" warning:**
- This is normal if you haven't installed the sound library
- The application will continue without beat detection
- To enable beat detection, run `./scripts/setup-sound-library.sh`
- Then add `lib/sound.jar` to your classpath

**Audio input fails but library is installed:**
- Check microphone permissions in System Preferences (macOS) or Settings (Windows/Linux)
- Verify your microphone is working in other applications
- Try a different audio input device
- The system will fall back to timed image transitions

If beat detection fails, the system will fall back to timed image transitions.

### Missing Images

If images are missing during run phase, phrases will display without images. Check logs for details.

### API Rate Limiting

Unsplash free tier allows 50 requests per hour. If you hit the limit, wait or upgrade your API key.

### Performance Issues

**Low frame rate:**
- Reduce `simultaneousImageCount` in config.json
- Reduce `frameRate` (default: 30)
- Use smaller images or fewer images per word

**High memory usage / OutOfMemoryError:**
- Increase Java heap size: `export MAVEN_OPTS="-Xmx2g"` before running
- Or use: `java -Xmx2g -cp ...` when running directly
- Reduce `imagesPerWord` in build phase (fewer images per word)
- Reduce `simultaneousImageCount` in run phase (fewer images displayed at once)
- Close other applications
- The system now caches images intelligently to avoid reloading, but large text files with many unique words may still require more memory

## License

MIT License
