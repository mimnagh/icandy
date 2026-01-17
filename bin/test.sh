#!/bin/bash

# iCandy Test Runner
# Runs all unit tests and property-based tests

set -e  # Exit on error

# Get the directory where this script is located and the project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Change to project root
cd "$PROJECT_ROOT"

echo "=========================================="
echo "  iCandy - Running Tests"
echo "=========================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven: https://maven.apache.org/install.html"
    exit 1
fi

# Parse arguments
TEST_CLASS="$1"

if [ -z "$TEST_CLASS" ]; then
    # Run all tests
    echo "Running all tests..."
    echo ""
    mvn test
else
    # Run specific test class
    echo "Running test class: $TEST_CLASS"
    echo ""
    mvn test -Dtest="$TEST_CLASS"
fi

echo ""
echo "=========================================="
echo "  Tests Complete!"
echo "=========================================="
echo ""
echo "Coverage report available at:"
echo "  target/site/jacoco/index.html"
echo ""
