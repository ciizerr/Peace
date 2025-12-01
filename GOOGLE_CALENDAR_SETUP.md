# Google Calendar API Setup Guide

This document provides instructions for configuring OAuth 2.0 credentials for Google Calendar integration in the Peace app.

## Prerequisites

1. A Google Cloud Platform (GCP) account
2. Android Studio with the Peace project open

## Step 1: Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click "Select a project" → "New Project"
3. Enter project name: "Peace App"
4. Click "Create"

## Step 2: Enable Google Calendar API

1. In the Google Cloud Console, select your project
2. Navigate to "APIs & Services" → "Library"
3. Search for "Google Calendar API"
4. Click on it and press "Enable"

## Step 3: Configure OAuth Consent Screen

1. Navigate to "APIs & Services" → "OAuth consent screen"
2. Select "External" user type (unless you have a Google Workspace account)
3. Click "Create"
4. Fill in the required fields:
   - App name: "Peace"
   - User support email: Your email
   - Developer contact information: Your email
5. Click "Save and Continue"
6. On the "Scopes" page, click "Add or Remove Scopes"
7. Add the following scope:
   - `https://www.googleapis.com/auth/calendar`
8. Click "Update" and then "Save and Continue"
9. Add test users (your email addresses for testing)
10. Click "Save and Continue"

## Step 4: Create OAuth 2.0 Credentials

### For Android App

1. Navigate to "APIs & Services" → "Credentials"
2. Click "Create Credentials" → "OAuth client ID"
3. Select "Android" as the application type
4. Enter a name: "Peace Android App"
5. Get your SHA-1 fingerprint:

#### Debug SHA-1 (for development):
```bash
# On Windows (PowerShell)
cd $env:USERPROFILE\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android

# On macOS/Linux
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### Release SHA-1 (for production):
```bash
# Replace with your keystore path
keytool -list -v -keystore /path/to/your/release.keystore -alias your_alias_name
```

6. Copy the SHA-1 fingerprint (looks like: `AA:BB:CC:DD:...`)
7. Paste it in the "SHA-1 certificate fingerprint" field
8. Enter package name: `com.nami.peace`
9. Click "Create"

### For Web (Optional - for testing in emulator)

1. Click "Create Credentials" → "OAuth client ID"
2. Select "Web application"
3. Enter a name: "Peace Web Client"
4. Click "Create"
5. Note down the Client ID (you may need this for testing)

## Step 5: Download Configuration (Optional)

While not strictly necessary for this implementation (we're using Google Sign-In), you can download the configuration:

1. In "Credentials", find your OAuth 2.0 Client ID
2. Click the download icon to get `google-services.json`
3. Place it in `app/` directory (optional)

## Step 6: Verify Setup

The app is now configured to use Google Calendar API. The OAuth flow will:

1. Request calendar permissions from the user
2. Authenticate via Google Sign-In
3. Request Calendar API scope
4. Create a "Peace Reminders" calendar
5. Sync reminders as calendar events

## Testing

### Test Users

During development (while the app is in "Testing" mode):
- Only test users added in the OAuth consent screen can sign in
- Add your test email addresses in the OAuth consent screen

### Publishing

To make the app available to all users:
1. Navigate to "OAuth consent screen"
2. Click "Publish App"
3. Submit for verification (required for production)

## Troubleshooting

### "Sign-in failed" error
- Verify SHA-1 fingerprint matches your keystore
- Check that package name is correct: `com.nami.peace`
- Ensure Calendar API is enabled
- Verify test users are added (in Testing mode)

### "Access blocked" error
- App needs to be verified by Google for production use
- Use test users during development
- Ensure OAuth consent screen is properly configured

### "Invalid client" error
- SHA-1 fingerprint doesn't match
- Package name doesn't match
- Using wrong keystore (debug vs release)

## Security Notes

1. **Never commit credentials to version control**
   - Add `google-services.json` to `.gitignore` if used
   - Keep OAuth client secrets secure

2. **Use different credentials for debug and release builds**
   - Create separate OAuth clients for debug and release
   - Use different SHA-1 fingerprints

3. **Rotate credentials if compromised**
   - Delete old credentials in Google Cloud Console
   - Create new ones with different SHA-1

## Additional Resources

- [Google Calendar API Documentation](https://developers.google.com/calendar/api)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [OAuth 2.0 for Mobile Apps](https://developers.google.com/identity/protocols/oauth2/native-app)

## Implementation Status

✅ Dependencies added (Google Play Services, Calendar API)
✅ Permissions configured in AndroidManifest.xml
✅ CalendarManager interface created
✅ CalendarManagerImpl with OAuth flow
✅ Permission helper for Compose UI
✅ Use cases for calendar sync
✅ Hilt module for dependency injection

## Next Steps

1. Configure OAuth 2.0 credentials in Google Cloud Console (follow steps above)
2. Test authentication flow
3. Implement UI for calendar sync in Settings
4. Test calendar event creation
5. Implement sync error handling
