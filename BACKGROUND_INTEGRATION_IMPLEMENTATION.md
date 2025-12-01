# Background System Integration Implementation

## Overview
This document describes the implementation of task 30: "Integrate background system into screens" from the Peace app enhancement specification.

## Implementation Summary

### Components Created/Modified

#### 1. BackgroundWrapper Component
**File**: `app/src/main/java/com/nami/peace/ui/components/BackgroundWrapper.kt`

A new composable that wraps screen content with a blurred background based on attachment images.

**Features**:
- Displays blurred background from attachment images
- Supports slideshow mode (cycles through images every 5 seconds)
- Falls back to theme background when no images are available
- Handles null BackgroundImageManager gracefully

**Key Logic**:
- Uses `LaunchedEffect` to load images when attachments change
- For slideshow mode with multiple images, uses `getSlideshowFlow()` to cycle through images
- For single image or non-slideshow mode, loads the first image only
- Wraps content with `BlurredBackground` component

#### 2. HomeScreen Integration
**File**: `app/src/main/java/com/nami/peace/ui/home/HomeScreen.kt`

**Changes**:
- Added `settingsViewModel` parameter to access blur and slideshow settings
- Added `backgroundImageManager` parameter (nullable for DI flexibility)
- Wrapped entire screen content with `BackgroundWrapper`
- Loads all attachments from `HomeViewModel.allAttachments`

#### 3. HomeViewModel Enhancement
**File**: `app/src/main/java/com/nami/peace/ui/home/HomeViewModel.kt`

**Changes**:
- Added `AttachmentRepository` dependency injection
- Added `allAttachments` StateFlow to provide all attachments for background
- Loads all attachments on initialization using `getAllAttachments()`

#### 4. ReminderDetailScreen Integration
**File**: `app/src/main/java/com/nami/peace/ui/reminder/ReminderDetailScreen.kt`

**Changes**:
- Added `settingsViewModel` parameter to access blur and slideshow settings
- Added `backgroundImageManager` parameter (nullable for DI flexibility)
- Wrapped entire screen content with `BackgroundWrapper`
- Uses attachments from the current reminder (`uiState.attachments`)

#### 5. SettingsScreen Integration
**File**: `app/src/main/java/com/nami/peace/ui/settings/SettingsScreen.kt`

**Changes**:
- Added `homeViewModel` parameter to access all attachments
- Added `backgroundImageManager` parameter (nullable for DI flexibility)
- Wrapped entire screen content with `BackgroundWrapper`
- Loads all attachments from `HomeViewModel.allAttachments`

#### 6. AttachmentRepository Enhancement
**Files**: 
- `app/src/main/java/com/nami/peace/domain/repository/AttachmentRepository.kt`
- `app/src/main/java/com/nami/peace/data/repository/AttachmentRepositoryImpl.kt`
- `app/src/main/java/com/nami/peace/data/local/AttachmentDao.kt`

**Changes**:
- Added `getAllAttachments()` method to retrieve all attachments across all reminders
- Implemented in DAO with query: `SELECT * FROM attachments ORDER BY timestamp DESC`
- Used for HomeScreen and SettingsScreen backgrounds

## Requirements Validation

### Requirement 6.1: Background Image Application
✅ **Implemented**: All three screens (HomeScreen, ReminderDetailScreen, SettingsScreen) now support background images from attachments.

### Requirement 6.6: Theme Fallback
✅ **Implemented**: When no attachments are available or BackgroundImageManager is null, the screens fall back to the default theme background. The `BackgroundWrapper` handles this gracefully by passing `null` bitmap to `BlurredBackground`, which simply doesn't render a background image.

## Architecture Decisions

### 1. Nullable BackgroundImageManager
The `backgroundImageManager` parameter is nullable in all screen composables. This allows:
- Easier testing without requiring DI setup
- Graceful degradation if the manager is not available
- Flexibility for future feature toggles

### 2. Centralized BackgroundWrapper
Instead of duplicating background logic in each screen, a single `BackgroundWrapper` component encapsulates all background functionality. This:
- Reduces code duplication
- Makes it easier to maintain and update background behavior
- Provides consistent behavior across all screens

### 3. Attachment Source Strategy
- **HomeScreen & SettingsScreen**: Use all attachments from all reminders
- **ReminderDetailScreen**: Uses only attachments from the current reminder

This provides contextual backgrounds while maintaining consistency.

### 4. Slideshow Management
The slideshow logic is handled by `BackgroundImageManager.getSlideshowFlow()`, which:
- Cycles through images every 5 seconds
- Runs in a coroutine that's automatically cancelled when the composable leaves composition
- Handles empty lists gracefully

## Testing Considerations

### Manual Testing Checklist
- [ ] HomeScreen displays background when attachments exist
- [ ] ReminderDetailScreen displays background from reminder attachments
- [ ] SettingsScreen displays background when attachments exist
- [ ] Blur intensity slider affects all screens in real-time
- [ ] Slideshow mode cycles through images every 5 seconds
- [ ] Screens fall back to theme when no attachments exist
- [ ] Screens work correctly when BackgroundImageManager is null

### Edge Cases Handled
1. **No attachments**: Falls back to theme background
2. **Null BackgroundImageManager**: Falls back to theme background
3. **Single attachment with slideshow enabled**: Loads single image (no cycling)
4. **Multiple attachments with slideshow disabled**: Loads first image only
5. **Empty file paths**: Handled by BackgroundImageManager returning null

## Performance Considerations

### Image Loading
- Images are loaded asynchronously using coroutines
- LRU cache in BackgroundImageManager prevents redundant loading
- Slideshow flow is cancelled when composable leaves composition

### Memory Management
- BackgroundImageManager uses LRU cache with 10% of available memory
- Bitmaps are automatically garbage collected when no longer referenced
- Cache can be cleared manually if needed

## Future Enhancements

### Potential Improvements
1. **Per-screen background preferences**: Allow users to set different backgrounds for different screens
2. **Background image selection**: Allow users to select specific attachments for backgrounds
3. **Transition animations**: Add smooth transitions when slideshow changes images
4. **Background dimming**: Add option to dim background for better text readability
5. **Custom blur algorithms**: Implement true Gaussian blur for API 31+ devices

## Conclusion

The background system has been successfully integrated into all three main screens (HomeScreen, ReminderDetailScreen, SettingsScreen). The implementation:
- Meets all requirements (6.1, 6.6)
- Provides graceful fallback to theme background
- Maintains consistent behavior across screens
- Handles edge cases appropriately
- Follows the existing architecture patterns
- Is performant and memory-efficient

The implementation is complete and ready for testing.
