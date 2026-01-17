#!/bin/bash

# iCandy Build Script
# Compiles the project and creates an executable JAR

set -e  # Exit on error

# Get the directory where this script is located and the project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Change to project root
cd "$PROJECT_ROOT"

echo "=========================================="
echo "  iCandy - Building Application"
echo "=========================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven: https://maven.apache.org/install.html"
    exit 1
fi

# Clean and compile
echo "Step 1: Cleaning previous builds..."
mvn clean

echo ""
echo "Step 2: Compiling and packaging..."
mvn package -DskipTests

echo ""
echo "=========================================="
echo "  Build Complete!"
echo "=========================================="
echo ""
echo "Executable JAR created at:"
echo "  target/icandy-1.0.0.jar"
echo ""
echo "Next steps:"
echo "  1. Build phase: bin/run-build.sh <text-file>"
echo "  2. Run phase:   bin/run-sketch.sh <text-file>"
echo ""
