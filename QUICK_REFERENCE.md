# iCandy Quick Reference

## Shell Scripts

### Build & Run
```bash
bin/build.sh                            # Compile project
bin/run-build.sh <text-file>            # Download images
bin/run-sketch.sh <text-file>           # Display visual
bin/quick-start.sh                      # Do everything
```

### Testing
```bash
bin/test.sh                             # Run all tests
bin/test.sh <TestClassName>             # Run specific test
```

### Maintenance
```bash
bin/clean-data.sh                       # Remove images & associations
```

## Common Workflows

### First Time Setup
```bash
./scripts/setup-config.sh
nano ~/.icandy/unsplash.properties      # Add API key
```

### Process New Text
```bash
bin/build.sh                            # If not built yet
bin/run-build.sh mytext.txt
bin/run-sketch.sh mytext.txt
```

### Quick Demo
```bash
bin/quick-start.sh
```

## Keyboard Controls

- **Right Arrow** - Next phrase
- **Left Arrow** - Previous phrase
- **ESC** - Exit

## File Locations

- Config: `~/.icandy/config.json`
- API Key: `~/.icandy/unsplash.properties`
- Images: `data/images/`
- Associations: `data/associations.json`
- Logs: `logs/`
