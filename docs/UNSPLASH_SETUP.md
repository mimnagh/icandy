# Unsplash API Setup Guide

## Overview

iCandy uses the Unsplash API to download images for words in your text scripts. To use this feature, you need to create a free Unsplash developer account and configure your credentials.

## Getting Unsplash Credentials

### Step 1: Create an Unsplash Account

1. Go to [https://unsplash.com/developers](https://unsplash.com/developers)
2. Sign up for a free account (or log in if you already have one)

### Step 2: Create a New Application

1. Click "New Application" or "Your Apps" → "New Application"
2. Accept the API Terms of Use
3. Fill in the application details:
   - **Application name**: iCandy (or any name you prefer)
   - **Description**: Visual text processor for personal use
4. Click "Create Application"

### Step 3: Get Your Credentials

After creating the application, you'll see three important credentials:

- **Application ID**: A unique identifier for your app
- **Secret Key**: A secret key for authentication
- **Access Key**: The key used for API requests

Copy all three values - you'll need them in the next step.

## Configuring iCandy

### Step 1: Create the Properties File

Create a directory for iCandy configuration in your home directory:

```bash
mkdir -p ~/.icandy
```

### Step 2: Create the Properties File

Create a file at `~/.icandy/unsplash.properties` with the following content:

```properties
# Unsplash API Credentials
application_id=YOUR_APPLICATION_ID_HERE
secret_key=YOUR_SECRET_KEY_HERE
access_key=YOUR_ACCESS_KEY_HERE
```

Replace the placeholder values with your actual credentials from the Unsplash dashboard.

### Step 3: Verify the Configuration

You can use the example file as a template:

```bash
cp unsplash.properties.example ~/.icandy/unsplash.properties
```

Then edit `~/.icandy/unsplash.properties` with your actual credentials.

### Step 4: Set Permissions (Optional but Recommended)

Protect your credentials file:

```bash
chmod 600 ~/.icandy/unsplash.properties
```

This ensures only you can read the file.

## File Location

By default, iCandy looks for the properties file at:
```
~/.icandy/unsplash.properties
```

If you want to store it elsewhere, update the `unsplashPropertiesFile` setting in `~/.icandy/config.json`:

```json
{
  "build": {
    "unsplashPropertiesFile": "/path/to/your/unsplash.properties"
  }
}
```

## API Limits

### Free Tier Limits

- **50 requests per hour**
- **50,000 requests per month**

For most personal use cases, this is more than sufficient. If you're processing large text files with many unique words, be mindful of these limits.

### Rate Limiting

If you exceed the rate limit, iCandy will:
1. Log a warning message
2. Wait and retry (up to `maxRetries` times)
3. Continue processing remaining words

## Troubleshooting

### "Invalid credentials" error

- Double-check that all three credentials are correct
- Make sure there are no extra spaces or quotes in the properties file
- Verify the file is at `~/.icandy/unsplash.properties`

### "File not found" error

- Ensure the directory exists: `mkdir -p ~/.icandy`
- Check the file path in `~/.icandy/config.json` matches where you saved the file
- On Windows, use the full path like `C:\Users\YourName\.icandy\unsplash.properties`

### "Rate limit exceeded" error

- Wait an hour for the limit to reset
- Reduce `imagesPerWord` in `config.json` to download fewer images
- Consider upgrading to a paid Unsplash plan if needed

### Testing Your Setup

You can test if your credentials work by running the build phase with a small text file:

```bash
echo "hello world" > test.txt
java -jar target/icandy-1.0.0.jar test.txt
```

If successful, you should see images being downloaded to `data/images/`.

## Security Best Practices

1. **Never commit credentials to Git**: The `.gitignore` file is configured to exclude `unsplash.properties`
2. **Use file permissions**: Set `chmod 600` on the properties file
3. **Don't share your credentials**: Each developer should use their own Unsplash account
4. **Rotate keys if exposed**: If you accidentally expose your keys, regenerate them in the Unsplash dashboard

## Additional Resources

- [Unsplash API Documentation](https://unsplash.com/documentation)
- [Unsplash API Guidelines](https://help.unsplash.com/en/articles/2511245-unsplash-api-guidelines)
- [Rate Limiting Information](https://unsplash.com/documentation#rate-limiting)
