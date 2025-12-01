# FontManager System

## Overview

The FontManager system provides a centralized way to manage custom fonts in the Peace application. It allows loading, caching, and retrieving custom fonts bundled with the app, with automatic fallback to the system font.

## Components

### 1. CustomFont Data Class

Represents a custom font with its metadata:

```kotlin
data class CustomFont(
    val name: String,
    val fontFamily: FontFamily,
    val previewText: String = "The quick brown fox jumps over the lazy dog"
)
```

### 2. FontManager Interface

Defines the contract for font management:

```kotlin
interface FontManager {
    fun getAllFonts(): List<CustomFont>
    fun getFont(fontName: String): FontFamily?
    fun getSystemFont(): FontFamily
}
```

### 3. FontManagerImpl

Implementation that loads fonts from `res/font` directory:

- Loads fonts at initialization
- Caches loaded fonts in memory
- Provides system font fallback
- Handles errors gracefully

## Available Fonts

The following custom fonts are bundled with the application:

1. **Poppins** - Modern geometric sans-serif
   - Regular (400)
   - Medium (500)
   - Bold (700)

2. **Lato** - Humanist sans-serif
   - Regular (400)
   - Bold (700)
   - Italic

3. **Bodoni Moda** - Classic serif
   - Regular (400)
   - Italic

4. **Loves** - Decorative font
   - Regular (400)

## Usage

### Dependency Injection

The FontManager is provided via Hilt:

```kotlin
@Inject
lateinit var fontManager: FontManager
```

### Getting All Fonts

```kotlin
val fonts = fontManager.getAllFonts()
fonts.forEach { font ->
    println("${font.name}: ${font.previewText}")
}
```

### Getting a Specific Font

```kotlin
val poppins = fontManager.getFont("Poppins")
if (poppins != null) {
    // Use the font
} else {
    // Font not found, use fallback
}
```

### Getting System Font

```kotlin
val systemFont = fontManager.getSystemFont()
// Always returns FontFamily.Default
```

## Integration with UserPreferencesRepository

The selected font is persisted using DataStore:

```kotlin
// Save selected font
userPreferencesRepository.setSelectedFont("Poppins")

// Retrieve selected font
val selectedFont = userPreferencesRepository.selectedFont.first()
val fontFamily = fontManager.getFont(selectedFont) ?: fontManager.getSystemFont()
```

## Error Handling

- If a font file fails to load, it's skipped and logged
- If a requested font doesn't exist, `getFont()` returns `null`
- System font is always available as fallback
- Font loading errors don't crash the app

## Performance Considerations

- Fonts are loaded lazily on first access
- Loaded fonts are cached in memory
- Font loading happens on the main thread (fonts are small)
- No disk I/O after initial load

## Testing

The FontManager system can be tested by:

1. Verifying all expected fonts are loaded
2. Checking font retrieval by name
3. Confirming system font fallback works
4. Testing case-sensitive font name matching

## Future Enhancements

- Support for downloading additional fonts
- Font preview generation
- Font metrics and characteristics
- Variable font support
