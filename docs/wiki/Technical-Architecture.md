# ⚙️ Technical Architecture

This page outlines the structure of the Peace app. We adhere strictly to modern Android standards to ensure the code is clean, scalable, and easy to maintain.

## 1. The MVVM Pattern
The app uses the MVVM (Model-View-ViewModel) architecture to ensure a clean separation between the user interface and business logic.

- **View (UI):** Handled entirely by Jetpack Compose. The Composables only observe the state provided by the ViewModel and send user events back (e.g., button clicks).
- **ViewModel (Logic Orchestration):** The "Brain" of the app. It holds and manages all UI state (e.g., `isAiLoading`, `reminderTitle`) and acts as the middleman, directing data requests to the Repository layer.
- **Model/Data (Repository):** Decides the source of truth. A request for the task list goes to the Repository, which fetches it from the Room Database. An AI request is routed to the `GeminiRepository`.

## 2. Asynchronous Data Flow (Flow & Coroutines)
All data operations (database reads, API calls, settings changes) are handled asynchronously using Kotlin Coroutines and Flow.

**Flow is essential for persistence:** The `PeaceViewModel` receives the list of reminders as a `Flow<List<Reminder>>`. When a user clicks "Set Peace" and the data is inserted into the database, the Flow automatically emits the new list, and the Home Screen instantly updates without needing a manual refresh.

## 3. Data Persistence Stack
The app uses two different systems for storing data:

| System | What It Stores | Why We Use It |
| :--- | :--- | :--- |
| **Room Database** | The primary Reminder List (complex objects). | Scalability, query capability, and integrity for transactional data. |
| **DataStore Preferences** | Small settings: User's Name, Theme preference, and the User's Custom API Key (BYOK). | Simple, safe, and asynchronous storage for user preferences. |
