# iCandy Visual Text Processor

A Processing-based application that creates dynamic visual experiences by associating images with text scripts and displaying them in sync with audio beat detection.

## Project Structure

```
icandy/
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

### Build Phase

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

**Option 1: Run with Maven (Recommended for Development)**

```bash
mvn exec:java -Dexec.mainClass="com.icandy.run.iCandySketch" \
  -Dexec.args="<path-to-text-file> [path-to-config.json]"
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

**Option 2: Run with Java directly**

First, ensure all dependencies are in the classpath:

```bash
# Build the project with dependencies
mvn clean package

# Run the sketch with increased heap size
java -Xmx2g -cp "target/icandy-1.0.0.jar:target/lib/*" \
  com.icandy.run.iCandySketch \
  <path-to-text-file> [path-to-config.json]
```

**Option 3: Quick Start (using default config)**

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
7. Respond to keyboard navigation (arrow keys)

**Note:** Beat detection (audio-synchronized image swapping) will be available after Task 10 is completed.

### Keyboard Controls

- **Right Arrow**: Advance to next phrase
- **Left Arrow**: Go back to previous phrase

## Complete Example Workflow

Here's a complete example from start to finish:

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
- Allow navigation with arrow keys

### 4. Interact

- Watch phrases advance automatically (timing based on word count)
- Press **Right Arrow** to skip ahead
- Press **Left Arrow** to go back
- Close the window to exit

## Using Your Own Text

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

## Configuration Options

### Build Phase

- `imagesPerWord`: Number of images to download per word (default: 5)
- `unsplashPropertiesFile`: Path to Unsplash credentials file (default: ~/.icandy/unsplash.properties)
- `imageStorageDir`: Directory for storing images (default: data/images)
- `associationsFile`: Path to associations file (default: data/associations.json)
- `stopWordsFile`: Path to stop words file (default: data/stopwords.txt)
- `maxRetries`: Maximum retry attempts for failed downloads (default: 3)

### Run Phase

- `beatSensitivity`: Minimum time between beats in ms (default: 100)
- `minPhraseDuration`: Minimum phrase display time in ms (default: 2000)
- `maxPhraseDuration`: Maximum phrase display time in ms (default: 10000)
- `msPerWord`: Milliseconds per word for duration calculation (default: 300)
- `frameRate`: Target frame rate (default: 30)
- `textSize`: Font size for phrases (default: 48)
- `textColor`: Text color in hex (default: #FFFFFF)
- `backgroundColor`: Background color in hex (default: #000000)
- `enableKeyboardNavigation`: Enable arrow key navigation (default: true)
- `simultaneousImageCount`: Number of images to display at once (default: 3)
- `loopPhrases`: Loop back to first phrase after last (default: true)
- `audioSource`: Audio input source (default: microphone)

## Testing

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

**"No phrases found" error:**
- Verify your text file contains actual text
- Check that the text has sentence-ending punctuation (. ! ?)
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

### Audio Input Issues

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
