#!/bin/bash

# iCandy Run Phase Runner
# Displays text phrases with images and beat-synchronized transitions

set -e  # Exit on error

# Get the directory where this script is located and the project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Default configuration path
DEFAULT_CONFIG="$HOME/.icandy/config.json"

# Parse arguments
TEXT_FILE="$1"
CONFIG_FILE="${2:-$DEFAULT_CONFIG}"

# Display usage if no arguments
if [ -z "$TEXT_FILE" ]; then
    echo ""
    echo "iCandy Run Phase - Visual Text Processor"
    echo ""
    echo "Usage:"
    echo "  bin/run-sketch.sh <text-file> [config-file]"
    echo ""
    echo "Arguments:"
    echo "  text-file    Path to the text script file to display (required)"
    echo "  config-file  Path to the configuration JSON file"
    echo "               (optional, default: ~/.icandy/config.json)"
    echo ""
    echo "Examples:"
    echo "  bin/run-sketch.sh data/Maryhadalittlelamb.txt"
    echo "  bin/run-sketch.sh my_script.txt custom_config.json"
    echo ""
    echo "Keyboard Controls:"
    echo "  Right Arrow  - Advance to next phrase"
    echo "  Left Arrow   - Go back to previous phrase"
    echo "  ESC          - Exit application"
    echo ""
    echo "Prerequisites:"
    echo "  1. Run the build phase first: bin/run-build.sh <text-file>"
    echo "  2. Ensure associations file exists: data/associations.json"
    echo "  3. Ensure images are downloaded: data/images/"
    echo ""
    echo "Audio:"
    echo "  - Beat detection uses system microphone (if available)"
    echo "  - Falls back to timed transitions if audio unavailable"
    echo ""
    exit 1
fi

# Check if text file exists
if [ ! -f "$TEXT_FILE" ]; then
    # Try relative to project root
    if [ -f "$PROJECT_ROOT/$TEXT_FILE" ]; then
        TEXT_FILE="$PROJECT_ROOT/$TEXT_FILE"
    else
        echo "Error: Text file not found: $TEXT_FILE"
        exit 1
    fi
fi

# Convert to absolute path
TEXT_FILE="$(cd "$(dirname "$TEXT_FILE")" && pwd)/$(basename "$TEXT_FILE")"

# Check if associations file exists
ASSOCIATIONS_FILE="$PROJECT_ROOT/data/associations.json"
if [ ! -f "$ASSOCIATIONS_FILE" ]; then
    echo "Error: Associations file not found: $ASSOCIATIONS_FILE"
    echo ""
    echo "Please run the build phase first:"
    echo "  bin/run-build.sh $TEXT_FILE"
    echo ""
    exit 1
fi

# Check if config file exists (warn but don't fail)
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Warning: Configuration file not found: $CONFIG_FILE"
    echo "The application will attempt to use default settings."
    echo ""
fi

echo "=========================================="
echo "  iCandy - Run Phase"
echo "=========================================="
echo ""
echo "Text file:   $TEXT_FILE"
echo "Config file: $CONFIG_FILE"
echo ""
echo "Keyboard Controls:"
echo "  Right Arrow  - Next phrase"
echo "  Left Arrow   - Previous phrase"
echo "  ESC          - Exit"
echo ""
echo "Starting visual display..."
echo ""

# Change to project root directory so relative paths work
cd "$PROJECT_ROOT"

# Run the Processing sketch
# Note: We need to run it with the compiled classes, not the JAR
# because Processing needs access to the sketch class directly

CLASSPATH="target/classes"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/github/micycle1/processing-core-4/4.4.4/processing-core-4-4.4.4.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/squareup/okhttp3/okhttp/4.12.0/okhttp-4.12.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/squareup/okio/okio-jvm/3.6.0/okio-jvm-3.6.0.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.9.10/kotlin-stdlib-1.9.10.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-common/1.9.10/kotlin-stdlib-common-1.9.10.jar"

# Add Processing Sound library if available
SOUND_LIB="lib/sound.jar"
if [ -f "$SOUND_LIB" ]; then
    CLASSPATH="$CLASSPATH:$SOUND_LIB"
    CLASSPATH="$CLASSPATH:lib/jsyn-17.1.0.jar"
    CLASSPATH="$CLASSPATH:lib/jportaudio.jar"
fi

# Run with Processing's PApplet main method
# We call our main method directly which then calls PApplet.main()
java -cp "$CLASSPATH" com.icandy.run.iCandySketch "$TEXT_FILE" "$CONFIG_FILE"
