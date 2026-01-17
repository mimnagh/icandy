#!/bin/bash

# iCandy Data Cleaner
# Removes downloaded images and associations to start fresh

# Get the directory where this script is located and the project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Change to project root
cd "$PROJECT_ROOT"

echo "=========================================="
echo "  iCandy - Clean Data"
echo "=========================================="
echo ""
echo "This will remove:"
echo "  - Downloaded images (data/images/*.jpg)"
echo "  - Associations file (data/associations.json)"
echo "  - Log files (logs/*.log)"
echo ""
echo "Configuration files will NOT be removed:"
echo "  - ~/.icandy/config.json"
echo "  - ~/.icandy/unsplash.properties"
echo ""

read -p "Are you sure you want to continue? (y/N) " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Cancelled."
    exit 0
fi

echo ""
echo "Cleaning data..."

# Remove images (but keep .gitkeep)
if [ -d "data/images" ]; then
    find data/images -type f -name "*.jpg" -delete
    echo "✓ Removed downloaded images"
fi

# Remove associations file
if [ -f "data/associations.json" ]; then
    rm data/associations.json
    echo "✓ Removed associations file"
fi

# Remove log files
if [ -d "logs" ]; then
    find logs -type f -name "*.log" -delete
    echo "✓ Removed log files"
fi

echo ""
echo "=========================================="
echo "  Data Cleaned!"
echo "=========================================="
echo ""
echo "You can now run a fresh build:"
echo "  bin/run-build.sh <text-file>"
echo ""
