package com.nami.peace.util.accessibility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Accessibility helper utilities for the Peace app.
 * Ensures all interactive elements meet WCAG 2.1 Level AA standards.
 */
object AccessibilityHelper {
    
    /**
     * Minimum touch target size as per Material Design guidelines (48dp x 48dp)
     */
    val MIN_TOUCH_TARGET_SIZE: Dp = 48.dp
    
    /**
     * Recommended touch target size for better accessibility (56dp x 56dp)
     */
    val RECOMMENDED_TOUCH_TARGET_SIZE: Dp = 56.dp
    
    /**
     * Creates a modifier with minimum touch target size and proper semantics.
     * 
     * @param contentDescription Accessibility description for screen readers
     * @param role Semantic role of the element (Button, Checkbox, etc.)
     * @param onClick Click handler
     * @param enabled Whether the element is enabled
     * @param minSize Minimum touch target size (defaults to 48dp)
     * @return Modifier with accessibility properties
     */
    @Composable
    fun Modifier.accessibleClickable(
        contentDescription: String,
        role: Role = Role.Button,
        onClick: () -> Unit,
        enabled: Boolean = true,
        minSize: Dp = MIN_TOUCH_TARGET_SIZE
    ): Modifier {
        return this
            .size(minSize)
            .semantics {
                this.contentDescription = contentDescription
                this.role = role
            }
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = role,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            )
    }
    
    /**
     * Adds semantic content description to a composable.
     * 
     * @param description Accessibility description for screen readers
     * @return Modifier with content description
     */
    fun Modifier.withContentDescription(description: String): Modifier {
        return this.semantics {
            contentDescription = description
        }
    }
    
    /**
     * Validates if a color contrast ratio meets WCAG AA standards.
     * WCAG AA requires:
     * - 4.5:1 for normal text
     * - 3:1 for large text (18pt+ or 14pt+ bold)
     * 
     * @param foreground Foreground color luminance (0.0 to 1.0)
     * @param background Background color luminance (0.0 to 1.0)
     * @return Contrast ratio
     */
    fun calculateContrastRatio(foreground: Float, background: Float): Float {
        val lighter = maxOf(foreground, background)
        val darker = minOf(foreground, background)
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * Checks if contrast ratio meets WCAG AA standards for normal text.
     * 
     * @param contrastRatio The contrast ratio to check
     * @return True if meets WCAG AA standards (4.5:1 or higher)
     */
    fun meetsWCAGAANormalText(contrastRatio: Float): Boolean {
        return contrastRatio >= 4.5f
    }
    
    /**
     * Checks if contrast ratio meets WCAG AA standards for large text.
     * 
     * @param contrastRatio The contrast ratio to check
     * @return True if meets WCAG AA standards (3:1 or higher)
     */
    fun meetsWCAGAALargeText(contrastRatio: Float): Boolean {
        return contrastRatio >= 3.0f
    }
    
    /**
     * Calculates relative luminance of an RGB color.
     * Formula from WCAG 2.1 specification.
     * 
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     * @return Relative luminance (0.0 to 1.0)
     */
    fun calculateLuminance(r: Int, g: Int, b: Int): Float {
        val rsRGB = r / 255f
        val gsRGB = g / 255f
        val bsRGB = b / 255f
        
        val rLinear = if (rsRGB <= 0.03928f) rsRGB / 12.92f else Math.pow((rsRGB + 0.055) / 1.055, 2.4).toFloat()
        val gLinear = if (gsRGB <= 0.03928f) gsRGB / 12.92f else Math.pow((gsRGB + 0.055) / 1.055, 2.4).toFloat()
        val bLinear = if (bsRGB <= 0.03928f) bsRGB / 12.92f else Math.pow((bsRGB + 0.055) / 1.055, 2.4).toFloat()
        
        return 0.2126f * rLinear + 0.7152f * gLinear + 0.0722f * bLinear
    }
}
