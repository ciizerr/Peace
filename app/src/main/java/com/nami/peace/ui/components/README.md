# PeaceIcon Composable

## Overview

`PeaceIcon` is a custom Compose icon component that integrates with the IconManager to load Ionicons vector drawables. It provides theme-aware tinting, accessibility support, and fallback handling for missing icons.

## Features

- **Ionicons Integration**: Loads icons from the Ionicons pack via IconManager
- **Theme-Aware Tinting**: Automatically adapts to theme colors or accepts custom tints
- **Accessibility**: Requires content descriptions for screen readers
- **Fallback Support**: Gracefully handles missing icons with fallback icons
- **Flexible Sizing**: Supports custom icon sizes
- **Performance**: Leverages IconManager's caching for efficient icon loading

## Usage

### Basic Usage

```kotlin
@Composable
fun MyScreen(iconManager: IconManager) {
    PeaceIcon(
        iconName = "add",
        contentDescription = "Add new item",
        iconManager = iconManager
    )
}
```

### Custom Tint Color

```kotlin
PeaceIcon(
    iconName = "heart",
    contentDescription = "Favorite",
    tint = Color.Red,
    iconManager = iconManager
)
```

### Custom Size

```kotlin
PeaceIcon(
    iconName = "star",
    contentDescription = "Star rating",
    size = 48.dp,
    iconManager = iconManager
)
```

### Theme-Aware Tinting

```kotlin
PeaceIcon(
    iconName = "settings",
    contentDescription = "Settings",
    tint = MaterialTheme.colorScheme.primary,
    iconManager = iconManager
)
```

### Using Resource ID Directly

When you already have the resource ID resolved:

```kotlin
val iconResourceId = iconManager.getIcon("add") ?: iconManager.getFallbackIcon("add")

PeaceIcon(
    iconResourceId = iconResourceId,
    contentDescription = "Add button"
)
```

## Parameters

### PeaceIcon (with IconManager)

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `iconName` | String | Yes | - | Name of the icon from Ionicons (without "ic_ionicons_" prefix) |
| `contentDescription` | String | Yes | - | Accessibility description for screen readers |
| `modifier` | Modifier | No | Modifier | Modifier for the icon |
| `tint` | Color | No | LocalContentColor.current | Color to tint the icon |
| `size` | Dp | No | 24.dp | Size of the icon |
| `iconManager` | IconManager | Yes | - | IconManager instance for loading icons |

### PeaceIcon (with Resource ID)

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `iconResourceId` | Int | Yes | - | Resource ID of the icon drawable |
| `contentDescription` | String | Yes | - | Accessibility description for screen readers |
| `modifier` | Modifier | No | Modifier | Modifier for the icon |
| `tint` | Color | No | LocalContentColor.current | Color to tint the icon |
| `size` | Dp | No | 24.dp | Size of the icon |

## Icon Naming

Icons can be referenced by name without the "ic_ionicons_" prefix:

- ✅ `"add"` → resolves to `ic_ionicons_add`
- ✅ `"add_circle"` → resolves to `ic_ionicons_add_circle`
- ✅ `"ic_ionicons_add"` → also works (with prefix)

## Fallback Behavior

If an icon is not found in the Ionicons pack:

1. `iconManager.getIcon()` returns `null`
2. `iconManager.getFallbackIcon()` returns a default icon (help_circle)
3. The fallback icon is displayed instead
4. A warning is logged for debugging

## Accessibility

All PeaceIcon instances **must** include a `contentDescription` parameter. This ensures the app is accessible to users with screen readers.

### Good Examples

```kotlin
// ✅ Clear, descriptive content description
PeaceIcon(
    iconName = "trash",
    contentDescription = "Delete item",
    iconManager = iconManager
)

// ✅ Context-specific description
PeaceIcon(
    iconName = "heart",
    contentDescription = "Add to favorites",
    iconManager = iconManager
)
```

### Bad Examples

```kotlin
// ❌ Generic description
PeaceIcon(
    iconName = "trash",
    contentDescription = "Icon",
    iconManager = iconManager
)

// ❌ Missing context
PeaceIcon(
    iconName = "heart",
    contentDescription = "Heart",
    iconManager = iconManager
)
```

## Dependency Injection

IconManager should be injected via Hilt:

```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel()
) {
    val iconManager = LocalIconManager.current
    
    PeaceIcon(
        iconName = "add",
        contentDescription = "Add",
        iconManager = iconManager
    )
}
```

Or passed as a parameter:

```kotlin
@Composable
fun MyComponent(iconManager: IconManager) {
    PeaceIcon(
        iconName = "settings",
        contentDescription = "Settings",
        iconManager = iconManager
    )
}
```

## Performance Considerations

- **Caching**: IconManager caches loaded icons, so repeated lookups are fast
- **Lazy Loading**: Icons are loaded on-demand, not at app startup
- **Memory**: Icon cache is kept in memory for the app lifetime
- **Recomposition**: Icon lookups don't trigger recomposition

## Testing

See `PeaceIconTest.kt` for unit tests covering:

- Valid icon loading
- Fallback behavior for missing icons
- Icon caching
- Resource ID resolution
- Icon name normalization

## Migration from Material Icons

To migrate from Material Icons to PeaceIcon:

### Before (Material Icons)

```kotlin
Icon(
    imageVector = Icons.Default.Add,
    contentDescription = "Add"
)
```

### After (PeaceIcon)

```kotlin
PeaceIcon(
    iconName = "add",
    contentDescription = "Add",
    iconManager = iconManager
)
```

## Related Components

- `IconManager`: Interface for loading icons
- `IoniconsManager`: Implementation that loads Ionicons
- `IconMapper`: Maps semantic names to icon names

## Requirements

This component satisfies the following requirements:

- **Requirement 3.2**: Uses icons from Ionicons pack instead of Material Icons
- **Requirement 3.6**: Adds content descriptions for accessibility
- **Requirement 3.4**: Provides fallback icons for missing resources

## See Also

- [PeaceIconExamples.kt](./PeaceIconExamples.kt) - Example usage patterns
- [IconManager.kt](../../util/icon/IconManager.kt) - Icon loading interface
- [IoniconsManager.kt](../../util/icon/IoniconsManager.kt) - Icon loading implementation
