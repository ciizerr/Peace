# Peace User Guide 🌿

Welcome to Peace! This comprehensive guide will help you master all features of your new productivity companion.

## Table of Contents

1. [Getting Started](#getting-started)
2. [Basic Task Management](#basic-task-management)
3. [Advanced Features](#advanced-features)
4. [Customization](#customization)
5. [Peace Garden](#peace-garden)
6. [ML Suggestions](#ml-suggestions)
7. [Widgets](#widgets)
8. [Sharing & Collaboration](#sharing--collaboration)
9. [Settings & Preferences](#settings--preferences)
10. [Tips & Best Practices](#tips--best-practices)

---

## Getting Started

### First Launch

When you first open Peace, you'll see:
- A clean, minimalist home screen
- A **+** button to create your first reminder
- Access to **Settings** and **Peace Garden**

### Setting Up Your API Key

To enable AI-powered natural language parsing:

1. Tap the **Settings** icon (gear) on the home screen
2. Scroll to **Gemini API Key**
3. Enter your Google Gemini API key
4. Tap **Save**

Alternatively, you can add it to `local.properties` during development.

---

## Basic Task Management

### Creating a Simple Reminder

1. Tap the **+** button on the home screen
2. Type naturally in the title field:
   - *"Dentist appointment tomorrow at 3pm"*
   - *"Call mom on Friday at 7pm"*
   - *"Gym workout every Monday at 6am"*
3. Gemini AI will automatically parse:
   - Date and time
   - Recurrence patterns
   - Task details
4. Review the parsed information
5. Tap **Save**

### Setting Priority

Reminders can have three priority levels:

- **🔴 High**: Urgent, important tasks
- **🟡 Medium**: Regular tasks (default)
- **🟢 Low**: Nice-to-have tasks

Priority affects:
- Visual indicators in the list
- Notification styling
- ML suggestion algorithms

### Choosing a Category

Organize reminders by category:

- **💼 Work**: Professional tasks
- **📚 Study**: Learning and education
- **💪 Health**: Fitness and wellness
- **🏠 Home**: Household tasks
- **📋 General**: Everything else (default)

Each category has a unique icon from the Ionicons pack.

### Using Nag Mode

Nag Mode lets you create multiple repetitions of a task:

**Example**: "Drink water 5 times every 2 hours"

1. Enable **Nag Mode** toggle
2. Set **Total Repetitions**: 5
3. Set **Interval**: 2 hours
4. Choose mode:
   - **Flex Mode**: Each repetition schedules from when you complete the previous one
   - **Strict Mode**: All repetitions anchored to the original start time

**Panic Loop**: If you snooze instead of completing, Peace enters "panic loop" mode:
- Reminds you every 2 minutes
- Continues for up to 30 minutes
- Helps you stay accountable

---

## Advanced Features

### Subtasks & Checklists

Break down complex tasks into manageable steps:

1. Open any reminder
2. Scroll to **Subtasks** section
3. Tap **Add Subtask**
4. Enter subtask title
5. Tap **Add**

**Features**:
- Unlimited subtasks per reminder
- Visual progress bar shows completion percentage
- Check off subtasks as you complete them
- Reorder subtasks by dragging (long-press)
- Delete subtasks with swipe gesture

**Progress Calculation**:
```
Progress = (Completed Subtasks / Total Subtasks) × 100%
```

### Notes

Add context and details to any reminder:

1. Open a reminder
2. Scroll to **Notes** section
3. Tap **Add Note**
4. Type your note
5. Tap **Save**

**Features**:
- Unlimited notes per reminder
- Automatic timestamps
- Chronological ordering
- Delete with swipe gesture
- Markdown-style formatting (coming soon)

### Image Attachments

Attach images for visual context:

1. Open a reminder
2. Scroll to **Attachments** section
3. Tap **Add Image**
4. Select image(s) from gallery
5. Images are automatically compressed and thumbnailed

**Features**:
- Unlimited images per reminder
- Automatic thumbnail generation (200×200px)
- Full-screen image viewer (tap any image)
- Delete with long-press
- Use as background images

**Storage**:
- Images stored in app-private directory
- Maximum 5MB per image (auto-compressed)
- Thumbnails cached for performance

### Custom Alarm Sounds

Personalize alarm sounds for each reminder:

1. Open a reminder (or create new)
2. Tap **Alarm Sound** field
3. Browse system sounds or custom sounds
4. Tap any sound to preview
5. Select your preferred sound
6. Tap **Save**

**Sound Sources**:
- System notification sounds
- System alarm sounds
- Custom audio files (import from storage)

**Preview**: Tap the play button next to any sound to hear it before selecting.

---

## Customization

### Language Selection

Change the app language without restarting:

1. Open **Settings**
2. Tap **Language**
3. Choose from 7+ languages:
   - English
   - Spanish (Español)
   - French (Français)
   - German (Deutsch)
   - Hindi (हिन्दी)
   - Japanese (日本語)
   - Portuguese (Português)
   - Chinese (中文)
4. Language applies immediately

**System Default**: Select "System Default" to match your device language.

### Custom Fonts

Personalize text appearance:

1. Open **Settings**
2. Tap **Font Settings**
3. Browse 20+ custom fonts
4. Tap any font to see live preview
5. Select your preferred font
6. Adjust **Font Padding** (0-20dp) with slider
7. Changes apply immediately

**Font Padding**: Adds extra spacing around text for better readability.

**System Font**: Select "System Font" to use Android's default typeface.

### Background Images

Create a personalized visual experience:

1. Open **Settings**
2. Tap **Background Settings**
3. Enable **Background Images** toggle
4. Adjust **Blur Intensity** (0-100):
   - 0 = No blur (sharp image)
   - 50 = Medium blur (recommended)
   - 100 = Maximum blur (very soft)
5. Enable **Slideshow** to cycle through multiple images
6. Slideshow transitions every 5 seconds

**Setting Background Images**:
- Attach images to any reminder
- Images automatically become available as backgrounds
- Background applies to Home, Detail, and Settings screens

**Performance**: Background images are cached for smooth performance.

---

## Peace Garden

The Peace Garden is your visual progress tracker that grows as you complete tasks.

### Garden Themes

Choose from 4 unique themes:

1. **🧘 Zen Garden**: Minimalist, peaceful aesthetic
   - Colors: Soft greens, whites, grays
   - Icons: Stones, bamboo, water

2. **🌲 Forest Garden**: Lush, natural environment
   - Colors: Deep greens, browns, earth tones
   - Icons: Trees, leaves, wildlife

3. **🏜️ Desert Garden**: Warm, resilient landscape
   - Colors: Oranges, yellows, sandy tones
   - Icons: Cacti, sun, sand

4. **🌊 Ocean Garden**: Calm, flowing seascape
   - Colors: Blues, teals, aqua
   - Icons: Waves, shells, coral

**Switching Themes**:
1. Open **Peace Garden**
2. Tap theme selector at top
3. Choose your preferred theme
4. Garden updates immediately

### Growth Stages

Your garden progresses through 10 growth stages:

| Stage | Name | Tasks Required | Visual |
|-------|------|----------------|--------|
| 1 | Seed | 0 | Tiny sprout |
| 2 | Sprout | 5 | Small plant |
| 3 | Seedling | 15 | Growing plant |
| 4 | Young Plant | 30 | Healthy plant |
| 5 | Mature Plant | 50 | Full plant |
| 6 | Flowering | 75 | Flowers appear |
| 7 | Blooming | 100 | Full bloom |
| 8 | Thriving | 150 | Abundant growth |
| 9 | Flourishing | 200 | Peak beauty |
| 10 | Masterpiece | 300 | Complete garden |

**Advancement**: Complete tasks to advance through stages. Each stage has a unique celebration animation!

### Streak Tracking

Track consecutive days of task completion:

- **Current Streak**: Days in a row with at least one completed task
- **Longest Streak**: Your personal best
- **Streak Reset**: Missing a day resets to 0

**Viewing Streak**:
- Displayed prominently in Peace Garden
- Shows days until next milestone
- Encourages daily consistency

### Milestones

Celebrate significant achievements:

- **🥉 7 Days**: One week of consistency
- **🥈 30 Days**: One month of dedication
- **🥇 100 Days**: Exceptional commitment
- **💎 365 Days**: One full year!

**Milestone Notifications**: Receive a special notification when you reach each milestone.

**Recent Achievements**: View your milestone history in the Peace Garden.

---

## ML Suggestions

Peace learns from your behavior and provides intelligent suggestions.

### How It Works

1. **Data Collection**: Peace tracks completion patterns (last 90 days)
2. **Pattern Analysis**: On-device ML analyzes your behavior
3. **Suggestion Generation**: Creates personalized recommendations
4. **Learning**: Adapts based on your acceptance/dismissal

**Privacy**: All analysis happens on your device. No data is sent to external servers.

### Types of Suggestions

#### 1. Optimal Time Suggestions
*"You usually complete 'Gym workout' at 7am. Consider scheduling it then."*

- Analyzes when you typically complete tasks
- Suggests better scheduling times
- Confidence based on consistency

#### 2. Priority Adjustments
*"You often complete 'Team standup' first. Consider marking it High priority."*

- Identifies tasks you prioritize in practice
- Suggests matching priority levels
- Helps align settings with behavior

#### 3. Recurring Pattern Detection
*"You've created 'Water plants' 4 times this month. Make it recurring?"*

- Detects manually repeated tasks
- Suggests converting to recurring reminders
- Saves time on task creation

#### 4. Break Reminders
*"You've been focused for 2 hours. Consider a 10-minute break."*

- Monitors focus session duration
- Suggests breaks to prevent burnout
- Promotes healthy work habits

#### 5. Habit Formation
*"You've completed 'Morning meditation' 7 days in a row. Keep it up!"*

- Recognizes emerging habits
- Provides encouragement
- Suggests making habits permanent

#### 6. Template Creation
*"You often create similar 'Client meeting' tasks. Create a template?"*

- Identifies repeated task patterns
- Suggests creating reusable templates
- Streamlines task creation

#### 7. Focus Session Optimization
*"Your most productive sessions are 90 minutes. Try that duration."*

- Analyzes focus session effectiveness
- Suggests optimal work durations
- Personalizes to your rhythm

### Using Suggestions

1. Open **Suggestions** from home screen
2. Review each suggestion:
   - **Title**: What the suggestion is about
   - **Description**: Detailed explanation
   - **Confidence Score**: 0-100% reliability
3. Choose action:
   - **Apply**: Accept the suggestion (updates reminder)
   - **Dismiss**: Decline the suggestion (system learns)

**Minimum Data**: Suggestions require at least 7 days of usage data.

**Daily Analysis**: Peace analyzes patterns once per day in the background.

---

## Widgets

Add Peace to your home screen for quick access.

### Today's Reminders Widget

Displays all reminders scheduled for today.

**Setup**:
1. Long-press on home screen
2. Tap **Widgets**
3. Find **Peace** → **Today's Reminders**
4. Drag to home screen
5. Resize as needed

**Features**:
- Shows all today's tasks
- Priority indicators
- Category icons
- Tap any reminder to open in app
- Auto-updates when data changes

### Peace Garden Widget

Shows your garden progress and streak.

**Setup**:
1. Long-press on home screen
2. Tap **Widgets**
3. Find **Peace** → **Peace Garden**
4. Drag to home screen
5. Resize as needed

**Features**:
- Current growth stage visual
- Streak counter
- Next milestone progress
- Tap to open Peace Garden
- Theme-aware styling

### Quick-Add Widget

Create reminders instantly without opening the app.

**Setup**:
1. Long-press on home screen
2. Tap **Widgets**
3. Find **Peace** → **Quick-Add**
4. Drag to home screen
5. Resize as needed

**Usage**:
1. Type in the text field: *"Lunch meeting at noon"*
2. Tap **Add** button
3. Gemini AI parses and creates reminder
4. Confirmation toast appears
5. Reminder added to your list

**Features**:
- Natural language parsing
- Instant creation
- No app launch required
- Perfect for quick captures

---

## Sharing & Collaboration

Share reminders with other Peace users via deep links.

### Sharing a Reminder

1. Open any reminder
2. Tap **Share** button (top-right)
3. Choose sharing method:
   - SMS
   - WhatsApp
   - Email
   - Any messaging app
4. Deep link is automatically generated
5. Send to recipient

**What Gets Shared**:
- Title
- Date and time
- Priority
- Category
- Recurrence settings
- Nag mode settings
- Notes (text only)
- Subtasks

**What Doesn't Get Shared**:
- Image attachments
- Custom alarm sounds
- Completion status

### Receiving a Shared Reminder

**With Peace Installed**:
1. Tap the deep link
2. Peace opens automatically
3. Review reminder details
4. Tap **Import** to add to your list
5. Reminder is created as an independent copy

**Without Peace Installed**:
1. Tap the deep link
2. Android prompts to install Peace
3. Redirects to Play Store
4. Install Peace
5. Open link again to import

### Deep Link Format

Deep links use this format:
```
peace://share?data=<base64_encoded_reminder_data>
```

**Security**: Data is Base64-encoded JSON. No sensitive information is exposed.

**Independence**: Imported reminders are independent copies. Changes to the original don't sync.

---

## Settings & Preferences

### General Settings

- **Language**: Change app language
- **Theme**: Light/Dark mode (system default)
- **Gemini API Key**: Configure AI features

### Customization Settings

- **Font Settings**: Choose font and padding
- **Background Settings**: Configure background images and blur
- **Icon Pack**: Ionicons (built-in, no configuration needed)

### Feature Settings

Toggle advanced features on/off:

- **Subtasks**: Enable/disable subtask functionality
- **Attachments**: Enable/disable image attachments
- **ML Suggestions**: Enable/disable intelligent suggestions
- **Calendar Sync**: Enable/disable Google Calendar integration
- **Widgets**: Enable/disable home screen widgets

**Effect**: Disabled features are completely hidden from the UI.

### Calendar Sync Settings

- **Enable Sync**: Toggle calendar integration
- **Manual Sync**: Trigger immediate sync
- **Last Sync**: View last sync timestamp
- **Sync Statistics**: See number of synced reminders
- **Sync Errors**: View and resolve sync issues

### Notification Settings

- **Default Alarm Sound**: Set default sound for new reminders
- **Vibration**: Enable/disable vibration
- **LED**: Enable/disable notification LED
- **Bundling**: Enable/disable notification bundling

---

## Tips & Best Practices

### Task Management

1. **Use Natural Language**: Let Gemini do the parsing work
   - ✅ "Team standup every weekday at 9am"
   - ❌ Manually setting each field

2. **Leverage Subtasks**: Break down complex projects
   - Example: "Launch Product" → 10 subtasks for each phase

3. **Set Appropriate Priorities**: Use the priority system effectively
   - High: Urgent deadlines, critical tasks
   - Medium: Regular daily tasks
   - Low: Nice-to-have, flexible tasks

4. **Use Categories**: Organize by life area for better overview

5. **Enable Nag Mode**: For tasks requiring multiple repetitions
   - Drinking water throughout the day
   - Taking medication at intervals
   - Regular stretch breaks

### Customization

1. **Choose Readable Fonts**: Select fonts that work for you
   - Adjust padding if text feels cramped
   - Preview before committing

2. **Moderate Blur Intensity**: Too much blur can reduce readability
   - Recommended: 40-60 for good balance

3. **Curate Background Images**: Use calming, non-distracting images
   - Nature scenes work well
   - Avoid busy or high-contrast images

4. **Match Garden Theme to Mood**: Switch themes seasonally or by preference

### Productivity

1. **Check Suggestions Weekly**: Review ML suggestions regularly
   - Apply helpful ones
   - Dismiss irrelevant ones (helps learning)

2. **Maintain Your Streak**: Complete at least one task daily
   - Builds consistency
   - Unlocks milestones
   - Grows your garden

3. **Use Widgets**: Add Quick-Add widget for rapid task capture
   - Reduces friction
   - Captures ideas immediately

4. **Review Regularly**: Check Today's Reminders widget each morning
   - Plan your day
   - Adjust priorities

5. **Share Thoughtfully**: Share reminders for collaboration
   - Team projects
   - Family tasks
   - Event coordination

### Performance

1. **Limit Attachments**: While unlimited, keep attachments reasonable
   - Large images are auto-compressed
   - Too many can slow loading

2. **Clean Up Completed**: Archive or delete old completed reminders
   - Keeps database lean
   - Improves performance

3. **Disable Unused Features**: Toggle off features you don't use
   - Reduces UI clutter
   - Slightly improves performance

### Privacy

1. **Use BYOK**: Bring your own Gemini API key
   - Full control over AI usage
   - No shared API limits

2. **Disable ML if Concerned**: Turn off ML suggestions
   - No pattern analysis
   - No data collection

3. **Control Calendar Sync**: Only enable if you need it
   - One-way sync (Peace → Calendar)
   - Can disable anytime

---

## Troubleshooting

### AI Parsing Not Working

- Verify Gemini API key is set correctly
- Check internet connection
- Try simpler phrasing
- Manually fill fields if needed

### Widgets Not Updating

- Check that widgets are enabled in Feature Settings
- Force refresh by opening the app
- Remove and re-add widget
- Restart device if persistent

### Calendar Sync Failing

- Verify Google Calendar permissions granted
- Check internet connection
- Review sync errors in Calendar Sync Settings
- Try manual sync
- Check Google Calendar API quota

### Notifications Not Appearing

- Check notification permissions
- Verify Do Not Disturb is off
- Check battery optimization settings
- Ensure alarm is scheduled correctly

### Images Not Loading

- Check storage permissions
- Verify images are under 5MB
- Try re-attaching image
- Clear app cache if persistent

---

## Getting Help

### In-App Support

- Check this User Guide
- Review tooltips and hints in the app
- Explore Settings for configuration options

### Community

- GitHub Issues: Report bugs or request features
- Discussions: Ask questions and share tips

### Contact

- Email: [support email]
- GitHub: [@ciizerr](https://github.com/ciizerr)

---

**Enjoy your journey with Peace! 🌿**
