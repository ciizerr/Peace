# ✨ Advanced Features

Peace's core value lies in its intelligent features, which are documented here.

## 1. 🧠 Gemini Integration: The "Sparkle" Function
The Sparkle button is powered by the Gemini API and serves as the app's primary input method.

- **Mechanism:** When the user types a sentence (e.g., "Take medicine every 8 hours starting at 9 AM"), the app sends a highly constrained prompt to the Gemini model.
- **Gemini's Role:** The model's sole job is to return a clean, structured string (e.g., `Title|HH:mm|Frequency|Repeat_Interval`) that the app can reliably parse to fill the fields in the `AddReminderSheet`.

### The BYOK (Bring Your Own Key) Strategy
To ensure app sustainability and provide unlimited access to power users, the app prioritizes key usage as follows:
1. The app checks DataStore for a User's Custom API Key.
2. If the user has provided a key, the app uses their key for all AI calls (saving the developer quota).
3. If no user key is present, the app securely falls back to the Default Key embedded in the `BuildConfig`.

## 2. ⏱️ Advanced Scheduling System
The app's scheduling logic is deliberately split between two Android components:

| Task Type | Android Component | Purpose |
| :--- | :--- | :--- |
| **Alarm Mode** | `AlarmManager` | Used for high-priority tasks requiring exact timing (e.g., Waking up, medication). This wakes the phone up even if the app is closed. |
| **Notification Mode** | `WorkManager` | Used for low-priority, general notifications (e.g., "Drink Water"). This is less precise but more battery-friendly. |

### Hourly Repeats
This is an advanced feature handled in the `AddReminderSheet`. Instead of just "Daily," users can set complex repeating rules:
- The user selects "Custom..." frequency.
- The system then uses the Room Database and WorkManager to schedule a sequence of tasks that repeat every X hours/minutes between a Start Time and an End Time (e.g., 09:00 to 17:00).

## 3. 👨‍💻 User Context and Personalization
The app uses the Profile Screen to gather necessary user context for the AI, enhancing the quality of the "Peace Coach."

- **Profile Data:** The user's dietary preferences, sleep schedule, and chosen Focus Area are all saved in DataStore.
- **AI Enhancement:** This data is automatically included in the Gemini prompt whenever the Peace Coach generates a motivational message. For example, if the user selects "Health," the coach's daily message will focus on gentle encouragement related to diet or sleep habits.
