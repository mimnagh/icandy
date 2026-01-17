#!/bin/bash

# iCandy Build Phase Runner
# Downloads images and creates associations for a text file

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
    echo "iCandy Build Phase - Visual Text Processor"
    echo ""
    echo "Usage:"
    echo "  bin/run-build.sh <text-file> [config-file]"
    echo ""
    echo "Arguments:"
    echo "  text-file    Path to the text script file to process (required)"
    echo "  config-file  Path to the configuration JSON file"
    echo "               (optional, default: ~/.icandy/config.json)"
    echo ""
    echo "Examples:"
    echo "  bin/run-build.sh data/Maryhadalittlelamb.txt"
    echo "  bin/run-build.sh my_script.txt custom_config.json"
    echo "  bin/run-build.sh ~/Documents/poem.txt ~/.icandy/config.json"
    echo ""
    echo "Configuration:"
    echo "  The configuration file should be a JSON file with build settings."
    echo "  See config.json.example for a template."
    echo ""
    echo "  Required configuration:"
    echo "    - Unsplash API credentials (in unsplash.properties file)"
    echo "    - Stop words file (data/stopwords.txt)"
    echo ""
    echo "Output:"
    echo "  - Downloaded images: data/images/"
    echo "  - Word-image associations: data/associations.json"
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

# Check if JAR exists
JAR_FILE="$PROJECT_ROOT/target/icandy-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found: $JAR_FILE"
    echo "Please run bin/build.sh first to compile the project"
    exit 1
fi

# Change to project root directory
cd "$PROJECT_ROOT"

# Check if config file exists (warn but don't fail)
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Warning: Configuration file not found: $CONFIG_FILE"
    echo "The application will attempt to use default settings."
    echo ""
fi

echo "=========================================="
echo "  iCandy - Build Phase"
echo "=========================================="
echo ""
echo "Text file:   $TEXT_FILE"
echo "Config file: $CONFIG_FILE"
echo ""

# Run the build phase
java -jar "$JAR_FILE" "$TEXT_FILE" "$CONFIG_FILE"

echo ""
echo "=========================================="
echo "  Build Phase Complete!"
echo "=========================================="
echo ""
echo "Next step:"
echo "  Run the visual display: bin/run-sketch.sh $TEXT_FILE"
echo ""
