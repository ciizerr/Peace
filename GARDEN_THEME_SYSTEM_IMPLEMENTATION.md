# Garden Theme System Implementation

## Overview
Implemented a comprehensive garden theme system for the Peace app that provides 4 distinct visual themes (Zen, Forest, Desert, Ocean) with unique color palettes and icon sets for the Peace Garden feature.

## Implementation Summary

### 1. Theme Color Palettes (`GardenThemeColors.kt`)
Created theme-specific color configurations for all 4 garden themes:

- **Zen Garden**: Calming grays, whites, and subtle greens inspired by Japanese zen gardens
- **Forest Garden**: Rich greens and earth tones inspired by lush forests
- **Desert Garden**: Warm sandy tones and terracotta inspired by desert landscapes
- **Ocean Garden**: Cool blues and aqua tones inspired by the ocean

Each theme includes:
- Primary, secondary, and accent colors
- Background and surface colors
- Text colors (onPrimary, onSecondary, onBackground)

### 2. Theme Icon Sets (`GardenThemeIcons.kt`)
Created theme-specific icon configurations using Ionicons:

Each theme includes:
- Main theme icon
- 10 growth stage icons (stages 0-9) representing progression
- Streak icon for displaying completion streaks
- Milestone icon for achievement notifications

**Theme-Specific Icons:**
- **Zen**: Minimalist nature icons (leaf, flower, rose, sparkles)
- **Forest**: Tree and forest icons (leaf, branches, network, bonfire)
- **Desert**: Cactus and desert icons (triangle, star, sunny)
- **Ocean**: Water and marine icons (water, fish, boat, planet)

### 3. Theme Configuration (`GardenThemeConfig.kt`)
Created a unified configuration system that combines colors, icons, and metadata:

- `GardenThemeConfig` data class with theme, colors, icons, display name, and description
- `getGardenThemeConfig()` function to retrieve complete theme configuration
- `getAllGardenThemeConfigs()` function to get all available themes
- Helper functions for display names and descriptions

### 4. Use Cases
Created domain layer use cases for theme management:

**UpdateGardenThemeUseCase** (`UpdateGardenThemeUseCase.kt`):
- Updates the garden theme while preserving all other garden state
- Validates that garden state exists before updating
- Persists changes to the database

**GetGardenThemeConfigUseCase** (`GetGardenThemeConfigUseCase.kt`):
- Observes the current garden state
- Maps the theme to its complete configuration
- Returns a Flow that updates when the theme changes

### 5. Property-Based Tests (`GardenThemePropertyTest.kt`)
Implemented comprehensive property-based tests validating:

1. **Theme Application**: For any theme selection, theme-specific config is applied
2. **State Preservation**: Theme changes preserve other garden state (streak, growth stage, etc.)
3. **Unique Colors**: Each theme has unique color configuration
4. **Unique Icons**: Each theme has unique icon configuration
5. **Complete Icon Sets**: All 10 growth stages have icons for each theme
6. **Config Consistency**: Use case returns same config as direct function
7. **Rapid Changes**: Rapid theme changes are all applied correctly

All tests passed successfully with 100+ iterations per property.

## Requirements Validated

✅ **Requirement 18.1**: WHEN the user opens Peace Garden settings THEN the Peace System SHALL display all available themes
- Implemented `getAllGardenThemeConfigs()` to provide all themes

✅ **Requirement 18.2**: WHEN the user selects a garden theme THEN the Peace System SHALL apply the theme immediately with theme-specific icons and colors
- Implemented `UpdateGardenThemeUseCase` for immediate theme application
- Property tests validate immediate application

✅ **Requirement 18.9**: WHEN the Peace Garden theme is changed THEN the Peace System SHALL persist the selection across app restarts
- Theme is stored in GardenEntity and persisted via Room database

## Files Created

1. `app/src/main/java/com/nami/peace/ui/theme/GardenThemeColors.kt` - Color palettes
2. `app/src/main/java/com/nami/peace/ui/theme/GardenThemeIcons.kt` - Icon configurations
3. `app/src/main/java/com/nami/peace/ui/theme/GardenThemeConfig.kt` - Unified configuration
4. `app/src/main/java/com/nami/peace/domain/usecase/UpdateGardenThemeUseCase.kt` - Theme update use case
5. `app/src/main/java/com/nami/peace/domain/usecase/GetGardenThemeConfigUseCase.kt` - Theme config use case
6. `app/src/test/java/com/nami/peace/domain/usecase/GardenThemePropertyTest.kt` - Property tests

## Architecture

```
UI Layer (Composables)
    ↓
GetGardenThemeConfigUseCase / UpdateGardenThemeUseCase
    ↓
GardenRepository
    ↓
GardenDao (Room)
    ↓
GardenEntity (Database)

Theme Configuration:
GardenThemeConfig
    ├── GardenThemeColors (color palettes)
    └── GardenThemeIcons (icon sets)
```

## Usage Example

```kotlin
// Get current theme configuration
val themeConfig by getGardenThemeConfigUseCase().collectAsState(initial = null)

// Display theme colors
themeConfig?.let { config ->
    Box(backgroundColor = config.colors.primary) {
        // Use theme-specific colors
    }
}

// Update theme
updateGardenThemeUseCase(GardenTheme.FOREST)

// Get growth stage icon
val iconName = getGrowthStageIcon(GardenTheme.ZEN, stage = 5)
```

## Next Steps

The theme system is now ready for UI integration. The next task (42) will implement the growth stage system that uses these themes to visualize user progress.

## Testing

All property-based tests passed:
- 7 test methods
- 100+ iterations per property test
- Validates theme application, state preservation, uniqueness, and consistency
- Tests run in ~10 seconds

**Property 30: Garden theme application** ✅ PASSED
- Validates: Requirements 18.2
