# PeaceIcon Integration Guide

## Quick Start

### 1. Import the Component

```kotlin
import com.nami.peace.ui.components.PeaceIcon
```

### 2. Inject IconManager

In your ViewModel or Screen:

```kotlin
@Composable
fun MyScreen(
    iconManager: IconManager // Inject via Hilt or pass as parameter
) {
    // Use PeaceIcon here
}
```

### 3. Use PeaceIcon

```kotlin
PeaceIcon(
    iconName = "add",
    contentDescription = "Add new item",
    iconManager = iconManager
)
```

## Integration Examples

### Example 1: Button with Icon

```kotlin
@Composable
fun AddButton(iconManager: IconManager, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        PeaceIcon(
            iconName = "add_circle",
            contentDescription = "Add new reminder",
            tint = MaterialTheme.colorScheme.primary,
            iconManager = iconManager
        )
    }
}
```

### Example 2: Navigation Bar Icons

```kotlin
@Composable
fun BottomNavigationBar(iconManager: IconManager) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                PeaceIcon(
                    iconName = "home",
                    contentDescription = "Home",
                    iconManager = iconManager
                )
            },
            label = { Text("Home") },
            selected = true,
            onClick = { /* Navigate to home */ }
        )
        
        NavigationBarItem(
            icon = {
                PeaceIcon(
                    iconName = "calendar",
                    contentDescription = "Calendar",
                    iconManager = iconManager
                )
            },
            label = { Text("Calendar") },
            selected = false,
            onClick = { /* Navigate to calendar */ }
        )
    }
}
```

### Example 3: List Item with Icon

```kotlin
@Composable
fun ReminderListItem(
    reminder: Reminder,
    iconManager: IconManager,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(reminder.title) },
        leadingContent = {
            PeaceIcon(
                iconName = getCategoryIcon(reminder.category),
                contentDescription = "${reminder.category} category",
                tint = getCategoryColor(reminder.category),
                iconManager = iconManager
            )
        },
        trailingContent = {
            PeaceIcon(
                iconName = "chevron_forward",
                contentDescription = "View details",
                iconManager = iconManager
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

private fun getCategoryIcon(category: Category): String {
    return when (category) {
        Category.WORK -> "briefcase"
        Category.PERSONAL -> "person"
        Category.HEALTH -> "fitness"
        Category.STUDY -> "book"
        Category.HOME -> "home"
        else -> "apps"
    }
}
```

### Example 4: Floating Action Button

```kotlin
@Composable
fun AddReminderFAB(iconManager: IconManager, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        PeaceIcon(
            iconName = "add",
            contentDescription = "Add new reminder",
            size = 24.dp,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            iconManager = iconManager
        )
    }
}
```

### Example 5: Top App Bar Actions

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(iconManager: IconManager) {
    TopAppBar(
        title = { Text("Peace") },
        navigationIcon = {
            IconButton(onClick = { /* Navigate back */ }) {
                PeaceIcon(
                    iconName = "arrow_back",
                    contentDescription = "Navigate back",
                    iconManager = iconManager
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Open search */ }) {
                PeaceIcon(
                    iconName = "search",
                    contentDescription = "Search",
                    iconManager = iconManager
                )
            }
            IconButton(onClick = { /* Open settings */ }) {
                PeaceIcon(
                    iconName = "settings",
                    contentDescription = "Settings",
                    iconManager = iconManager
                )
            }
        }
    )
}
```

### Example 6: Card with Status Icon

```kotlin
@Composable
fun ReminderCard(
    reminder: Reminder,
    iconManager: IconManager
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PeaceIcon(
                iconName = if (reminder.isCompleted) "checkmark_circle" else "time",
                contentDescription = if (reminder.isCompleted) "Completed" else "Pending",
                tint = if (reminder.isCompleted) Color.Green else MaterialTheme.colorScheme.primary,
                size = 32.dp,
                iconManager = iconManager
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
```

### Example 7: Priority Indicator

```kotlin
@Composable
fun PriorityIndicator(
    priority: Priority,
    iconManager: IconManager
) {
    val (iconName, color) = when (priority) {
        Priority.HIGH -> "alert_circle" to Color.Red
        Priority.MEDIUM -> "warning" to Color(0xFFFFA500) // Orange
        Priority.LOW -> "information_circle" to Color.Blue
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PeaceIcon(
            iconName = iconName,
            contentDescription = "$priority priority",
            tint = color,
            size = 16.dp,
            iconManager = iconManager
        )
        Text(
            text = priority.name,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
```

## Dependency Injection Setup

### Option 1: Hilt Module (Recommended)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object IconModule {
    
    @Provides
    @Singleton
    fun provideIconManager(
        @ApplicationContext context: Context
    ): IconManager {
        return IoniconsManager(context)
    }
}
```

### Option 2: CompositionLocal

```kotlin
val LocalIconManager = compositionLocalOf<IconManager> {
    error("IconManager not provided")
}

@Composable
fun PeaceApp() {
    val context = LocalContext.current
    val iconManager = remember { IoniconsManager(context) }
    
    CompositionLocalProvider(LocalIconManager provides iconManager) {
        // Your app content
        NavHost(...)
    }
}

// Usage in any composable
@Composable
fun MyScreen() {
    val iconManager = LocalIconManager.current
    
    PeaceIcon(
        iconName = "add",
        contentDescription = "Add",
        iconManager = iconManager
    )
}
```

## Common Icon Names

Here are commonly used Ionicons names:

### Navigation
- `home`, `home_outline`
- `arrow_back`, `arrow_forward`
- `chevron_back`, `chevron_forward`
- `menu`, `close`

### Actions
- `add`, `add_circle`
- `remove`, `remove_circle`
- `create`, `trash`
- `save`, `share`
- `search`, `filter`

### Status
- `checkmark`, `checkmark_circle`
- `close_circle`, `alert_circle`
- `information_circle`, `help_circle`
- `warning`, `alert`

### Content
- `heart`, `heart_outline`
- `star`, `star_outline`
- `bookmark`, `bookmark_outline`
- `calendar`, `time`

### Settings
- `settings`, `cog`
- `notifications`, `notifications_outline`
- `person`, `person_circle`
- `lock_closed`, `lock_open`

### Categories
- `briefcase` (Work)
- `home` (Home)
- `fitness` (Health)
- `book` (Study)
- `apps` (General)

## Best Practices

### 1. Always Provide Content Descriptions

```kotlin
// ✅ Good
PeaceIcon(
    iconName = "trash",
    contentDescription = "Delete reminder",
    iconManager = iconManager
)

// ❌ Bad - Missing context
PeaceIcon(
    iconName = "trash",
    contentDescription = "Icon",
    iconManager = iconManager
)
```

### 2. Use Theme Colors When Possible

```kotlin
// ✅ Good - Adapts to theme
PeaceIcon(
    iconName = "settings",
    contentDescription = "Settings",
    tint = MaterialTheme.colorScheme.primary,
    iconManager = iconManager
)

// ⚠️ Acceptable - Specific color needed
PeaceIcon(
    iconName = "heart",
    contentDescription = "Favorite",
    tint = Color.Red,
    iconManager = iconManager
)
```

### 3. Use Appropriate Sizes

```kotlin
// Standard sizes
val smallIcon = 16.dp    // For inline text
val normalIcon = 24.dp   // Default, most common
val largeIcon = 32.dp    // For emphasis
val extraLargeIcon = 48.dp // For hero elements
```

### 4. Cache IconManager Instance

```kotlin
// ✅ Good - Cached in ViewModel or remember
@Composable
fun MyScreen() {
    val iconManager = remember { IoniconsManager(LocalContext.current) }
    // Use iconManager
}

// ❌ Bad - Creates new instance on every recomposition
@Composable
fun MyScreen() {
    val iconManager = IoniconsManager(LocalContext.current)
    // Use iconManager
}
```

## Troubleshooting

### Icon Not Displaying

1. Check if icon name is correct (without "ic_ionicons_" prefix)
2. Verify IconManager is properly injected
3. Check if icon exists in Ionicons pack
4. Look for fallback icon being used (check logs)

### Icon Color Not Changing

1. Ensure `tint` parameter is set
2. Check if theme colors are properly configured
3. Verify icon is a vector drawable (not bitmap)

### Performance Issues

1. Ensure IconManager is singleton or cached
2. Check if icons are being loaded repeatedly
3. Verify icon cache is working (check logs)

## Migration Checklist

When migrating from Material Icons to PeaceIcon:

- [ ] Replace `Icon(imageVector = Icons.Default.X)` with `PeaceIcon(iconName = "x")`
- [ ] Add `iconManager` parameter to composables
- [ ] Update content descriptions to be more specific
- [ ] Test with different themes (light/dark)
- [ ] Verify accessibility with TalkBack
- [ ] Check icon sizes are appropriate
- [ ] Ensure fallback icons work for missing icons

## Support

For issues or questions:

1. Check the main README.md for detailed documentation
2. Review PeaceIconExamples.kt for usage patterns
3. Run PeaceIconTest.kt to verify IconManager functionality
4. Check logs for icon loading warnings/errors
