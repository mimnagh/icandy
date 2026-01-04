#!/bin/bash
# Setup script for iCandy configuration

set -e

echo "Setting up iCandy configuration..."

# Create config directory
echo "Creating ~/.icandy directory..."
mkdir -p ~/.icandy

# Copy config files if they don't exist
if [ ! -f ~/.icandy/config.json ]; then
    echo "Copying config.json.example to ~/.icandy/config.json..."
    cp config.json.example ~/.icandy/config.json
    echo "✓ Created ~/.icandy/config.json"
else
    echo "✓ ~/.icandy/config.json already exists"
fi

if [ ! -f ~/.icandy/unsplash.properties ]; then
    echo "Copying unsplash.properties.example to ~/.icandy/unsplash.properties..."
    cp unsplash.properties.example ~/.icandy/unsplash.properties
    echo "✓ Created ~/.icandy/unsplash.properties"
    echo ""
    echo "⚠️  IMPORTANT: Edit ~/.icandy/unsplash.properties and add your Unsplash API credentials"
    echo "   Get credentials from: https://unsplash.com/developers"
else
    echo "✓ ~/.icandy/unsplash.properties already exists"
fi

# Set secure permissions
echo "Setting secure permissions..."
chmod 600 ~/.icandy/unsplash.properties
chmod 644 ~/.icandy/config.json

echo ""
echo "✓ Setup complete!"
echo ""
echo "Configuration files:"
echo "  - ~/.icandy/config.json"
echo "  - ~/.icandy/unsplash.properties"
echo ""
echo "Next steps:"
echo "  1. Edit ~/.icandy/unsplash.properties with your Unsplash credentials"
echo "  2. (Optional) Customize ~/.icandy/config.json"
echo "  3. Run: mvn clean install"
