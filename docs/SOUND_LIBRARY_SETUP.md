# Processing Sound Library Setup

This document explains how to set up the Processing Sound library for beat detection in iCandy.

## Overview

The Processing Sound library enables audio beat detection, allowing images to swap in sync with music or audio beats. The library is **optional** - iCandy will work without it, but beat detection features will not be available.

## Quick Setup

Run the setup script:

```bash
./scripts/setup-sound-library.sh
```

This will:
1. Download the Processing Sound library (v2.4.0) from GitHub releases
2. Extract `sound.jar` to the `lib/` directory
3. Provide instructions for using it

## Manual Setup

If you prefer to set up the library manually:

1. Download the sound library from the [Processing Sound releases](https://github.com/processing/processing-sound/releases)
2. Extract the ZIP file
3. Copy all JAR files from `sound/library/` to `lib/` in your iCandy project directory:
   - sound.jar
   - jsyn-17.1.0.jar
   - jportaudio.jar
   - jlayer-1.0.1.4.jar
   - jorbis-0.0.17.4.jar
   - mp3spi-1.9.5.4.jar
   - tritonus-share-0.3.7.4.jar
   - vorbisspi-1.0.3.3.jar

## Using the Sound Library

### With Maven

Add the library to your local Maven repository:

```bash
mvn install:install-file \
  -Dfile=lib/sound.jar \
  -DgroupId=org.processing \
  -DartifactId=sound \
  -Dversion=2.4.0 \
  -Dpackaging=jar
```

Then add it as a dependency in `pom.xml`:

```xml
<dependency>
    <groupId>org.processing</groupId>
    <artifactId>sound</artifactId>
    <version>2.4.0</version>
</dependency>
```

### With Java Classpath

When running the application, include all sound library JARs in your classpath:

```bash
java -cp "target/icandy-1.0.0.jar:lib/*:target/lib/*" \
  com.icandy.run.iCandySketch \
  data/Maryhadalittlelamb.txt
```

**Important**: Use `lib/*` to include all JAR files, not just `lib/sound.jar`. The sound library has dependencies on JSyn and other libraries.

## How It Works

The `BeatDetectorWrapper` class uses Java reflection to load the Processing Sound library at runtime:

1. **Library Available**: If `sound.jar` is in the classpath, the wrapper will:
   - Initialize `AudioIn` for microphone input
   - Create a `BeatDetector` instance
   - Analyze audio for beat detection
   - Trigger image swaps on detected beats

2. **Library Not Available**: If `sound.jar` is not in the classpath, the wrapper will:
   - Log a warning: "Processing Sound library not found"
   - Continue running without beat detection
   - Fall back to timed image transitions

This design allows the application to compile and run without the sound library, while still supporting beat detection when the library is available.

## Verifying Installation

After running the setup script, verify the libraries were installed:

```bash
ls -lh lib/*.jar
```

You should see 8 JAR files:
- sound.jar (~55KB)
- jsyn-17.1.0.jar (~404KB)
- jportaudio.jar (~5KB)
- jlayer-1.0.1.4.jar (~138KB)
- jorbis-0.0.17.4.jar (~98KB)
- mp3spi-1.9.5.4.jar (~33KB)
- tritonus-share-0.3.7.4.jar (~130KB)
- vorbisspi-1.0.3.3.jar (~19KB)

Check that sound.jar contains the required classes:

```bash
jar tf lib/sound.jar | grep -E "(AudioIn|BeatDetector)\.class"
```

You should see:
```
processing/sound/AudioIn.class
processing/sound/BeatDetector.class
```

## Troubleshooting

### "Processing Sound library not found" Warning

This warning appears when:
- The sound library is not installed
- The sound library is not in the classpath
- The JAR file is corrupted

**Solution**: Run `./scripts/setup-sound-library.sh` and ensure you include `lib/*` in your classpath (not just `lib/sound.jar`).

### Missing JSyn or Other Dependencies

If you see errors like `NoClassDefFoundError: com/jsyn/unitgen/UnitSource`:

**Cause**: The sound library has dependencies that aren't in the classpath.

**Solution**: 
- Make sure you ran the setup script which downloads all 8 JAR files
- Use `lib/*` in your classpath, not individual JAR files
- Verify all JARs are present: `ls lib/*.jar` should show 8 files

### Audio Input Fails

If the library is loaded but audio input fails:

1. **Check Microphone Permissions**:
   - macOS: System Preferences → Security & Privacy → Privacy → Microphone
   - Windows: Settings → Privacy → Microphone
   - Linux: Check PulseAudio/ALSA settings

2. **Verify Microphone is Working**:
   - Test your microphone in another application
   - Check that it's not muted
   - Ensure it's set as the default input device

3. **Check Logs**:
   - Look for error messages in the console output
   - Check `logs/icandy_YYYY-MM-DD.log` for details

### Library Version Mismatch

The setup script downloads version 2.4.0 of the Processing Sound library. If you need a different version:

1. Edit `scripts/setup-sound-library.sh`
2. Change the `SOUND_VERSION` variable
3. Run the script again

## Beat Detection Configuration

Once the sound library is installed, you can configure beat detection in `~/.icandy/config.json`:

```json
{
  "run": {
    "beatSensitivity": 100,
    "audioSource": "microphone"
  }
}
```

- `beatSensitivity`: Minimum time between detected beats in milliseconds (default: 100)
  - Lower values: More frequent beat detection
  - Higher values: Less frequent beat detection
- `audioSource`: Audio input source (currently only "microphone" is supported)

## Technical Details

### Reflection-Based Loading

The `BeatDetectorWrapper` uses reflection to avoid compile-time dependency on the sound library:

```java
// Load classes dynamically
Class<?> audioInClass = Class.forName("processing.sound.AudioIn");
Class<?> beatDetectorClass = Class.forName("processing.sound.BeatDetector");

// Create instances using reflection
audioInput = audioInConstructor.newInstance(parent, 0);
beatDetector = beatDetectorConstructor.newInstance(parent);
```

This approach allows:
- Compilation without the sound library
- Graceful fallback when library is not available
- Runtime loading when library is present

### Beat Detection Algorithm

The Processing Sound library's `BeatDetector` uses energy-based beat detection:

1. Analyzes audio input from the microphone
2. Calculates energy levels in frequency bands
3. Detects sudden increases in energy (beats)
4. Returns `true` from `isBeat()` when a beat is detected

The `BeatDetectorWrapper` adds sensitivity enforcement to prevent detecting beats too frequently.

## References

- [Processing Sound Library Documentation](https://processing.org/reference/libraries/sound/index.html)
- [Processing Sound GitHub Repository](https://github.com/processing/processing-sound)
- [Processing Sound Releases](https://github.com/processing/processing-sound/releases)

