# 🛠️ Setup & Debugging

This is the essential guide for getting the project running and troubleshooting common issues.

## 1. Project Prerequisites
- **IDE:** Android Studio (latest stable version).
- **Language:** Kotlin (100% codebase).
- **Dependencies:** Ensure you run **Sync Now** after adding dependencies for Room, DataStore, and KSP.

## 2. Running the App (API Key Setup)
Because the app is secured with `BuildConfig`, a contributor must follow these steps:

1. **Clone the Repository.**
2. **Create `local.properties`:** In the root directory of the project, create this file (it is automatically ignored by Git).
3. **Add Your Key:** Paste your Gemini API key from Google AI Studio into the file:
   ```properties
   GEMINI_API_KEY="AIzaSyD...YourActualKeyHere..."
   ```
4. **Rebuild:** Go to **Build > Rebuild Project** to generate the necessary `BuildConfig` file that makes the key accessible to the app.

## 3. Debugging (Using Logcat)
If the AI feature fails or the app crashes, use Logcat to diagnose the problem quickly.

- **Filter:** In the Logcat window, set your filter to the tag: `GEMINI_DEBUG`
- **Expected Output:** A successful call will show:
  ```
  Using Key: [DEFAULT or USER CUSTOM]
  Gemini Replied: Title|HH:mm|DAILY
  ```

### Common Troubleshooting

| Issue | Cause | Solution |
| :--- | :--- | :--- |
| **CRASHED: 404 Not Found** | The app cannot resolve the model name (e.g., `gemini-2.5-flash`). | Check that the model name in `GeminiRepository.kt` is exactly correct, or temporarily switch to a stable model like `gemini-pro`. |
| **"Save Key" Doesn't Work** | The DataStore dependency was missed, or the SettingsSheet isn't fully updated. | Ensure the `datastore-preferences` dependency is added and synced. Verify the `PeaceViewModel` is correctly calling `settingsManager.saveUserApiKey()`. |
| **No Internet/Network Error** | Missing permission. | Ensure you have `<uses-permission android:name="android.permission.INTERNET" />` in your `AndroidManifest.xml`. |
