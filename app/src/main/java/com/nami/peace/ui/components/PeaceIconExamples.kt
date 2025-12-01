package com.nami.peace.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nami.peace.util.icon.IconManager

/**
 * Example usage of PeaceIcon composable.
 * This file demonstrates various ways to use the custom icon component.
 */

/**
 * Example: Basic icon usage with default settings
 */
@Composable
fun BasicIconExample(iconManager: IconManager) {
    PeaceIcon(
        iconName = "add",
        contentDescription = "Add new item",
        iconManager = iconManager
    )
}

/**
 * Example: Icon with custom tint color
 */
@Composable
fun TintedIconExample(iconManager: IconManager) {
    PeaceIcon(
        iconName = "heart",
        contentDescription = "Favorite",
        tint = Color.Red,
        iconManager = iconManager
    )
}

/**
 * Example: Icon with custom size
 */
@Composable
fun LargeIconExample(iconManager: IconManager) {
    PeaceIcon(
        iconName = "star",
        contentDescription = "Star rating",
        size = 48.dp,
        iconManager = iconManager
    )
}

/**
 * Example: Icon that adapts to theme colors
 */
@Composable
fun ThemedIconExample(iconManager: IconManager) {
    PeaceIcon(
        iconName = "settings",
        contentDescription = "Settings",
        tint = MaterialTheme.colorScheme.primary,
        iconManager = iconManager
    )
}

/**
 * Example: Multiple icons in a row
 */
@Composable
fun IconRowExample(iconManager: IconManager) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PeaceIcon(
            iconName = "home",
            contentDescription = "Home",
            iconManager = iconManager
        )
        PeaceIcon(
            iconName = "search",
            contentDescription = "Search",
            iconManager = iconManager
        )
        PeaceIcon(
            iconName = "notifications",
            contentDescription = "Notifications",
            iconManager = iconManager
        )
        PeaceIcon(
            iconName = "person",
            contentDescription = "Profile",
            iconManager = iconManager
        )
    }
}

/**
 * Example: Icon with label
 */
@Composable
fun IconWithLabelExample(iconManager: IconManager) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        PeaceIcon(
            iconName = "calendar",
            contentDescription = "Calendar",
            size = 32.dp,
            tint = MaterialTheme.colorScheme.primary,
            iconManager = iconManager
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Calendar",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Example: Using resource ID directly (when already resolved)
 */
@Composable
fun DirectResourceIdExample(iconManager: IconManager) {
    val iconResourceId = iconManager.getIcon("add") ?: iconManager.getFallbackIcon("add")
    
    PeaceIcon(
        iconResourceId = iconResourceId,
        contentDescription = "Add button"
    )
}
