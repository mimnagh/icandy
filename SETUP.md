# iCandy Setup Guide

## Prerequisites Installation

### 1. Install Java Development Kit (JDK)

iCandy requires Java 11 or higher.

**Check if Java is installed:**
```bash
java -version
```

**Install Java (macOS):**
```bash
brew install openjdk@11
```

**Install Java (Linux):**
```bash
sudo apt-get update
sudo apt-get install openjdk-11-jdk
```

### 2. Install Maven

**Check if Maven is installed:**
```bash
mvn --version
```

**Install Maven (macOS):**
```bash
brew install maven
```

**Install Maven (Linux):**
```bash
sudo apt-get update
sudo apt-get install maven
```

**Install Maven (Windows):**
Download from https://maven.apache.org/download.cgi and follow installation instructions.

### 3. Get Unsplash API Credentials

1. Go to https://unsplash.com/developers
2. Create a free account
3. Create a new application
4. Copy your Application ID, Secret Key, and Access Key

### 4. Configure iCandy

1. Create the iCandy configuration directory:
   ```bash
   mkdir -p ~/.icandy
   ```

2. Copy the configuration template:
   ```bash
   cp config.json.example ~/.icandy/config.json
   ```

3. Copy the example Unsplash properties file:
   ```bash
   cp unsplash.properties.example ~/.icandy/unsplash.properties
   ```

4. Edit `~/.icandy/unsplash.properties` and add your Unsplash credentials:
   ```properties
   application_id=YOUR_APPLICATION_ID_HERE
   secret_key=YOUR_SECRET_KEY_HERE
   access_key=YOUR_ACCESS_KEY_HERE
   ```

5. (Optional) Customize `~/.icandy/config.json` for other settings like images per word, display duration, etc.

## Build the Project

```bash
mvn clean install
```

This will:
- Download all dependencies
- Compile the source code
- Run tests
- Create an executable JAR file

## Verify Installation

Check that the project structure is correct:

```bash
ls -la
```

You should see:
- `src/` directory with Java source files
- `data/` directory with images/, stopwords.txt
- `logs/` directory for log files
- `config.json.example` configuration template
- `unsplash.properties.example` credentials template
- `pom.xml` Maven build file
- `README.md` documentation

Check that your user configuration is set up:

```bash
ls -la ~/.icandy/
```

You should see:
- `config.json` - Your active configuration
- `unsplash.properties` - Your Unsplash credentials

## Next Steps

Once setup is complete, you can:

1. **Run the Build Phase**: Process a text file and download images
   ```bash
   java -jar target/icandy-1.0.0.jar sample.txt
   ```

2. **Run the Run Phase**: Display the visual experience (after implementation)

## Troubleshooting

### Maven not found
Make sure Maven is installed and in your PATH. Try closing and reopening your terminal after installation.

### Java version issues
Ensure you have Java 11 or higher. Check with `java -version`.

### Permission errors
On macOS/Linux, you may need to make scripts executable:
```bash
chmod +x scripts/*.sh
```

### API rate limiting
Unsplash free tier allows 50 requests per hour. If you exceed this, wait an hour or upgrade your API key.

### Missing Unsplash credentials
Make sure `~/.icandy/unsplash.properties` exists and contains valid credentials. Check the file path in `~/.icandy/config.json` if you stored it elsewhere.

### Configuration not found
Make sure `~/.icandy/config.json` exists. Copy it from `config.json.example` if needed.
