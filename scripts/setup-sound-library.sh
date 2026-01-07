#!/bin/bash

# Script to download Processing Sound library from GitHub releases
# This script downloads the sound.jar file and its dependencies,
# placing them in the lib directory for use with the iCandy application.

set -e

# Configuration
SOUND_VERSION="v2.4.0"
GITHUB_REPO="processing/processing-sound"
RELEASE_URL="https://github.com/${GITHUB_REPO}/releases/download/${SOUND_VERSION}/sound.zip"
LIB_DIR="lib"
TEMP_DIR=$(mktemp -d)

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "================================================"
echo "Processing Sound Library Setup"
echo "================================================"
echo ""

# Check if lib directory exists, create if not
if [ ! -d "$LIB_DIR" ]; then
    echo "Creating lib directory..."
    mkdir -p "$LIB_DIR"
fi

# Check if sound.jar already exists
if [ -f "$LIB_DIR/sound.jar" ]; then
    echo -e "${YELLOW}Warning: sound.jar already exists in $LIB_DIR${NC}"
    read -p "Do you want to re-download it? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Skipping download."
        exit 0
    fi
    rm -f "$LIB_DIR"/sound*.jar
    rm -f "$LIB_DIR"/jsyn*.jar
fi

# Download the sound library
echo "Downloading Processing Sound library ${SOUND_VERSION}..."
echo "URL: ${RELEASE_URL}"

if command -v curl &> /dev/null; then
    curl -L -o "$TEMP_DIR/sound.zip" "$RELEASE_URL"
elif command -v wget &> /dev/null; then
    wget -O "$TEMP_DIR/sound.zip" "$RELEASE_URL"
else
    echo -e "${RED}Error: Neither curl nor wget is available. Please install one of them.${NC}"
    exit 1
fi

# Check if download was successful
if [ ! -f "$TEMP_DIR/sound.zip" ]; then
    echo -e "${RED}Error: Failed to download sound.zip${NC}"
    exit 1
fi

echo "Download complete."

# Extract the ZIP file
echo "Extracting sound library..."
unzip -q "$TEMP_DIR/sound.zip" -d "$TEMP_DIR"

# Find and copy all JAR files from the library directory
# The structure is typically: sound/library/*.jar
LIBRARY_DIR=$(find "$TEMP_DIR" -type d -name "library" | head -n 1)

if [ -z "$LIBRARY_DIR" ]; then
    echo -e "${RED}Error: library directory not found in the downloaded archive${NC}"
    echo "Contents of archive:"
    unzip -l "$TEMP_DIR/sound.zip"
    rm -rf "$TEMP_DIR"
    exit 1
fi

# Copy all JAR files
JAR_COUNT=0
for jar_file in "$LIBRARY_DIR"/*.jar; do
    if [ -f "$jar_file" ]; then
        cp "$jar_file" "$LIB_DIR/"
        echo "Copied: $(basename "$jar_file")"
        JAR_COUNT=$((JAR_COUNT + 1))
    fi
done

# Clean up
rm -rf "$TEMP_DIR"

if [ $JAR_COUNT -eq 0 ]; then
    echo -e "${RED}Error: No JAR files found in the library directory${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}Success!${NC} Processing Sound library installed to $LIB_DIR/"
echo "Installed $JAR_COUNT JAR file(s):"
ls -1 "$LIB_DIR"/*.jar | xargs -n 1 basename
echo ""
echo "To use the library with Maven, add all JARs to your classpath:"
echo "  java -cp \"target/icandy-1.0.0.jar:$LIB_DIR/*:target/lib/*\" \\"
echo "    com.icandy.run.iCandySketch <textfile>"
echo ""
echo "Or install them to your local Maven repository:"
echo "  mvn install:install-file -Dfile=$LIB_DIR/sound.jar \\"
echo "    -DgroupId=org.processing -DartifactId=sound \\"
echo "    -Dversion=$SOUND_VERSION -Dpackaging=jar"
echo ""
echo "================================================"
