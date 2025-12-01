package com.nami.peace.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nami.peace.util.icon.IconManager

/**
 * Custom Icon composable that uses IconManager to load Ionicons.
 * Supports icon tinting based on theme and provides accessibility content descriptions.
 *
 * @param iconName The name of the icon from Ionicons (without "ic_ionicons_" prefix)
 * @param contentDescription Accessibility description for the icon
 * @param modifier Modifier for the icon
 * @param tint Color to tint the icon. Defaults to LocalContentColor
 * @param size Size of the icon. Defaults to 24.dp
 */
@Composable
fun PeaceIcon(
    iconName: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    iconManager: IconManager
) {
    val context = LocalContext.current
    
    // Get the icon resource ID from IconManager
    val iconResourceId = iconManager.getIcon(iconName) ?: iconManager.getFallbackIcon(iconName)
    
    Icon(
        painter = painterResource(id = iconResourceId),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}

/**
 * Overload that accepts a direct resource ID instead of icon name.
 * Useful when the resource ID is already known.
 *
 * @param iconResourceId The resource ID of the icon
 * @param contentDescription Accessibility description for the icon
 * @param modifier Modifier for the icon
 * @param tint Color to tint the icon. Defaults to LocalContentColor
 * @param size Size of the icon. Defaults to 24.dp
 */
@Composable
fun PeaceIcon(
    iconResourceId: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp
) {
    Icon(
        painter = painterResource(id = iconResourceId),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}
