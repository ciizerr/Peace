# Background Image System

This package provides background image management for the Peace app, including image loading, caching, and slideshow functionality.

## Components

### BackgroundImageManager

Interface for background image operations:
- `loadImageAsBitmap(filePath)`: Loads a single image as Bitmap
- `loadImagesForSlideshow(filePaths)`: Loads multiple images for slideshow
- `getSlideshowFlow(filePaths)`: Returns a Flow that emits images every 5 seconds
- `clearCache()`: Clears the image cache
- `getCachedBitmap(filePath)`: Gets cached bitmap if available

### BackgroundImageManagerImpl

Implementation with LRU caching:
- **Cache Size**: 10% of available memory (approximately 2-3 full-resolution images)
- **Slideshow Interval**: 5 seconds
- **Thread Safety**: All operations use Dispatchers.IO

## Usage

### Basic Background Image

```kotlin
@Composable
fun MyScreen(
    backgroundImageManager: BackgroundImageManager,
    attachment: Attachment,
    blurIntensity: Int
) {
    val bitmap by remember(attachment.filePath) {
        mutableStateOf<Bitmap?>(null)
    }
    
    LaunchedEffect(attachment.filePath) {
        bitmap = backgroundImageManager.loadImageAsBitmap(attachment.filePath)
    }
    
    BlurredBackground(
        bitmap = bitmap,
        blurIntensity = blurIntensity
    ) {
        // Your screen content here
    }
}
```

### Slideshow Background

```kotlin
@Composable
fun MyScreenWithSlideshow(
    backgroundImageManager: BackgroundImageManager,
    attachments: List<Attachment>,
    blurIntensity: Int
) {
    val filePaths = attachments.map { it.filePath }
    val slideshowFlow = remember(filePaths) {
        backgroundImageManager.getSlideshowFlow(filePaths)
    }
    
    BlurredBackgroundWithSlideshow(
        slideshowFlow = slideshowFlow,
        blurIntensity = blurIntensity
    ) {
        // Your screen content here
    }
}
```

## Blur Effect

The blur effect uses Android's RenderEffect API (Android 12+):
- **Blur Intensity**: 0-100 scale
  - 0 = No blur
  - 100 = Maximum blur (25px radius)
- **Fallback**: On Android < 12, images display without blur

## Performance Considerations

### Caching
- Images are cached in memory using LruCache
- Cache size is 10% of available memory
- Least recently used images are evicted automatically

### Memory Management
- Bitmaps are loaded on IO dispatcher
- Cache can be cleared manually with `clearCache()`
- Slideshow preloads all images to avoid loading delays

### Slideshow
- Images cycle every 5 seconds
- All images are preloaded into cache
- Flow continues indefinitely until cancelled

## Integration with Preferences

The background system integrates with UserPreferencesRepository:
- `blurIntensity`: 0-100 value for blur effect
- `slideshowEnabled`: Boolean to enable/disable slideshow

## Error Handling

- **File Not Found**: Returns null bitmap
- **Loading Failure**: Catches exceptions and returns null
- **Empty Slideshow**: Emits null if no images available
- **Cache Full**: Automatically evicts oldest entries

## Testing

See `BackgroundImagePropertyTest.kt` for property-based tests validating:
- Background image application (Property 14)
- Blur intensity persistence (Property 15)
- Slideshow cycling behavior
- Cache management

## Requirements Validation

This implementation validates:
- **Requirement 6.1**: Background image application
- **Requirement 6.2**: Adjustable blur intensity
- **Requirement 6.3**: Slideshow with 5-second intervals
- **Requirement 6.4**: Background image caching
