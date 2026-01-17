#!/bin/bash

# iCandy Quick Start
# Builds the project and runs both phases with the sample text

set -e  # Exit on error

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

SAMPLE_TEXT="$PROJECT_ROOT/data/Maryhadalittlelamb.txt"

echo "=========================================="
echo "  iCandy - Quick Start"
echo "=========================================="
echo ""
echo "This script will:"
echo "  1. Build the project"
echo "  2. Run the build phase (download images)"
echo "  3. Run the visual display"
echo ""
echo "Using sample text: $SAMPLE_TEXT"
echo ""

# Check if sample text exists
if [ ! -f "$SAMPLE_TEXT" ]; then
    echo "Error: Sample text file not found: $SAMPLE_TEXT"
    exit 1
fi

# Check if configuration exists
CONFIG_FILE="$HOME/.icandy/config.json"
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Warning: Configuration file not found: $CONFIG_FILE"
    echo ""
    echo "Please set up your configuration first:"
    echo "  1. Copy config.json.example to ~/.icandy/config.json"
    echo "  2. Set up Unsplash API credentials in ~/.icandy/unsplash.properties"
    echo ""
    echo "Run the setup script:"
    echo "  ./scripts/setup-config.sh"
    echo ""
    exit 1
fi

# Change to project root
cd "$PROJECT_ROOT"

# Step 1: Build
echo "Step 1: Building project..."
echo ""
"$SCRIPT_DIR/build.sh"

# Step 2: Build phase (download images)
echo ""
echo "Step 2: Running build phase (downloading images)..."
echo ""
read -p "Press Enter to continue or Ctrl+C to cancel..."
echo ""
"$SCRIPT_DIR/run-build.sh" "$SAMPLE_TEXT"

# Step 3: Run phase (visual display)
echo ""
echo "Step 3: Running visual display..."
echo ""
read -p "Press Enter to start the visual display or Ctrl+C to cancel..."
echo ""
"$SCRIPT_DIR/run-sketch.sh" "$SAMPLE_TEXT"
