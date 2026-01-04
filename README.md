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

### Run Phase

Run the Processing sketch to display the visual experience:

```bash
# Instructions will be added after implementation
```

### Keyboard Controls

- **Right Arrow**: Advance to next phrase
- **Left Arrow**: Go back to previous phrase

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

## Troubleshooting

### Audio Input Issues

If beat detection fails, the system will fall back to timed image transitions.

### Missing Images

If images are missing during run phase, phrases will display without images. Check logs for details.

### API Rate Limiting

Unsplash free tier allows 50 requests per hour. If you hit the limit, wait or upgrade your API key.

## License

MIT License
