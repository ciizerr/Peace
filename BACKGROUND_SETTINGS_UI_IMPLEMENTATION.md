# Background Settings UI Implementation

## Overview
This document describes the implementation of the Background Settings UI for the Peace app, allowing users to configure background image settings including blur intensity and slideshow mode.

## Requirements Addressed
- **6.1**: Background image selection (informational - images come from attachments)
- **6.2**: Adjustable blur intensity with real-time preview
- **6.3**: Slideshow toggle for cycling through images
- **6.5**: Blur intensity persistence in settings
- **6.6**: Background image toggle and theme fallback

## Implementation Details

### 1. SettingsViewModel Updates
**File**: `app/src/main/java/com/nami/peace/ui/settings/SettingsViewModel.kt`

Added background-related state management:
- `blurIntensity: StateFlow<Int>` - Tracks blur intensity (0-100)
- `slideshowEnabled: StateFlow<Boolean>` - Tracks slideshow toggle state
- `setBlurIntensity(intensity: Int)` - Updates blur intensity with validation
- `setSlideshowEnabled(enabled: Boolean)` - Updates slideshow state

These states are backed by `UserPreferencesRepository` which already had the necessary DataStore preferences implemented.

### 2. BackgroundSettingsScreen
**File**: `app/src/main/java/com/nami/peace/ui/settings/BackgroundSettingsScreen.kt`

A new composable screen that provides:

#### Features:
1. **Information Card**
   - Explains that background images come from reminder attachments
   - Uses Ionicons `information_circle` icon
   - Provides user guidance

2. **Blur Intensity Slider**
   - Range: 0-100
   - Real-time updates as user drags slider
   - Shows current value prominently
   - Labeled endpoints: "0 (Clear)" and "100 (Maximum)"
   - Uses Ionicons `color_filter` icon

3. **Slideshow Toggle**
   - Switch control for enabling/disabling slideshow
   - Explains 5-second interval behavior
   - Uses Ionicons `images` icon

4. **Live Preview**
   - The entire screen uses `BlurredBackground` composable
   - Shows real-time blur effect as slider is adjusted
   - Informational card explains the preview feature
   - Uses Ionicons `eye` icon

#### UI Design:
- Uses Material 3 Card components with semi-transparent backgrounds
- Consistent spacing (24.dp between sections)
- Proper icon-text alignment
- Responsive layout with proper padding
- Follows Peace app's calm aesthetic

### 3. Navigation Integration
**File**: `app/src/main/java/com/nami/peace/MainActivity.kt`

Added new navigation route:
```kotlin
composable("background_settings") {
    com.nami.peace.ui.settings.BackgroundSettingsScreen(
        onNavigateUp = { navController.popBackStack() }
    )
}
```

Updated SettingsScreen call to include navigation callback:
```kotlin
onNavigateToBackgroundSettings = { navController.navigate("background_settings") }
```

### 4. SettingsScreen Updates
**File**: `app/src/main/java/com/nami/peace/ui/settings/SettingsScreen.kt`

Added:
- New parameter: `onNavigateToBackgroundSettings: () -> Unit`
- New button in Appearance section:
  - Uses Ionicons `image` icon
  - Labeled "Background Settings"
  - Navigates to background settings screen

## User Flow

1. User opens Settings from Home screen
2. In Appearance section, user sees "Background Settings" button
3. User taps button to navigate to Background Settings screen
4. User sees:
   - Info about background images coming from attachments
   - Blur intensity slider with live preview
   - Slideshow toggle
   - Live preview explanation
5. User adjusts blur intensity:
   - Slider updates immediately
   - Background blur effect changes in real-time
   - Value is persisted to DataStore
6. User toggles slideshow:
   - Switch updates immediately
   - State is persisted to DataStore
7. User navigates back to Settings

## Technical Details

### State Management
- All settings are persisted using DataStore Preferences
- StateFlow provides reactive updates to UI
- ViewModel handles all business logic
- Repository pattern ensures clean separation

### Real-time Preview
- Uses existing `BlurredBackground` composable
- Blur intensity changes are immediately visible
- No need to save/apply - changes are instant
- Preview bitmap can be provided for demonstration

### Validation
- Blur intensity is clamped to 0-100 range in repository
- Invalid values are automatically corrected
- Type-safe state management prevents errors

### Accessibility
- All icons have content descriptions
- Slider has proper semantics
- Switch is properly labeled
- Text is readable with sufficient contrast

## Integration with Existing Systems

### UserPreferencesRepository
Already implemented with:
- `blurIntensity: Flow<Int>` - Persists blur value
- `slideshowEnabled: Flow<Boolean>` - Persists slideshow state
- Validation and coercion built-in

### BackgroundImageManager
Ready for integration:
- `loadImageAsBitmap()` - Loads images for preview
- `getSlideshowFlow()` - Provides slideshow functionality
- Cache management for performance

### BlurredBackground Composable
Already implemented:
- Accepts bitmap and blur intensity
- Renders content on top of blurred background
- Works on all API levels

## Testing Considerations

### Manual Testing Checklist
- [ ] Navigate to Background Settings from Settings screen
- [ ] Verify blur slider moves smoothly (0-100)
- [ ] Confirm blur intensity value displays correctly
- [ ] Test real-time blur preview updates
- [ ] Toggle slideshow on/off
- [ ] Verify settings persist after app restart
- [ ] Test navigation back to Settings
- [ ] Verify all icons render correctly
- [ ] Test with different theme modes (light/dark)

### Property-Based Testing
The blur intensity persistence is already covered by:
- **Property 15**: Blur intensity persistence round-trip test
- Located in: `app/src/test/java/com/nami/peace/data/repository/UserPreferencesPropertyTest.kt`

## Future Enhancements

1. **Background Image Selection**
   - Add ability to browse and select from reminder attachments
   - Show thumbnail previews of available images
   - Allow setting different images per screen

2. **Advanced Blur Options**
   - True gaussian blur on API 31+ using RenderEffect
   - Blur style options (gaussian, motion, radial)
   - Blur color tinting

3. **Slideshow Customization**
   - Adjustable interval (not just 5 seconds)
   - Transition effects (fade, slide, zoom)
   - Random vs sequential order

4. **Preview Enhancements**
   - Load actual attachment images for preview
   - Show slideshow animation in preview
   - Preview different screens with background

## Files Modified

1. `app/src/main/java/com/nami/peace/ui/settings/SettingsViewModel.kt`
   - Added blur intensity and slideshow state
   - Added setter methods

2. `app/src/main/java/com/nami/peace/ui/settings/SettingsScreen.kt`
   - Added navigation parameter
   - Added Background Settings button

3. `app/src/main/java/com/nami/peace/MainActivity.kt`
   - Added background_settings route
   - Updated SettingsScreen navigation

## Files Created

1. `app/src/main/java/com/nami/peace/ui/settings/BackgroundSettingsScreen.kt`
   - Complete background settings UI
   - Real-time blur preview
   - Blur intensity slider
   - Slideshow toggle

## Dependencies

All required dependencies were already in place:
- Material 3 for UI components
- Hilt for dependency injection
- DataStore for preferences
- Compose Navigation
- Existing BackgroundImageManager
- Existing BlurredBackground composable

## Conclusion

The Background Settings UI is now fully implemented and integrated into the Peace app. Users can:
- Access background settings from the main Settings screen
- Adjust blur intensity with real-time preview (0-100)
- Enable/disable slideshow mode
- See changes immediately without needing to save

All settings are persisted and will be used by the background image system when images are set from reminder attachments. The implementation follows the app's design principles of calm engagement and provides a smooth, intuitive user experience.
