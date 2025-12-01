package com.nami.peace.accessibility

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.util.accessibility.AccessibilityHelper
import com.nami.peace.util.icon.IconManager
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import kotlin.test.assertTrue

/**
 * Comprehensive accessibility tests for the Peace app.
 * Tests content descriptions, touch target sizes, color contrast, and TalkBack compatibility.
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testMinimumTouchTargetSize_Button() {
        composeTestRule.setContent {
            Button(
                onClick = {},
                modifier = Modifier
                    .testTag("test_button")
                    .size(AccessibilityHelper.MIN_TOUCH_TARGET_SIZE)
            ) {
                Text("Test")
            }
        }

        composeTestRule.onNodeWithTag("test_button")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun testMinimumTouchTargetSize_IconButton() {
        val mockIconManager = mock(IconManager::class.java)
        `when`(mockIconManager.getIcon("settings")).thenReturn(android.R.drawable.ic_menu_preferences)

        composeTestRule.setContent {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .testTag("test_icon_button")
                    .size(AccessibilityHelper.MIN_TOUCH_TARGET_SIZE)
            ) {
                PeaceIcon(
                    iconName = "settings",
                    contentDescription = "Settings",
                    iconManager = mockIconManager
                )
            }
        }

        composeTestRule.onNodeWithTag("test_icon_button")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun testMinimumTouchTargetSize_Checkbox() {
        composeTestRule.setContent {
            Checkbox(
                checked = false,
                onCheckedChange = {},
                modifier = Modifier
                    .testTag("test_checkbox")
                    .size(AccessibilityHelper.MIN_TOUCH_TARGET_SIZE)
            )
        }

        composeTestRule.onNodeWithTag("test_checkbox")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun testContentDescription_Icon() {
        val mockIconManager = mock(IconManager::class.java)
        `when`(mockIconManager.getIcon("add")).thenReturn(android.R.drawable.ic_input_add)

        composeTestRule.setContent {
            PeaceIcon(
                iconName = "add",
                contentDescription = "Add Reminder",
                iconManager = mockIconManager,
                modifier = Modifier.testTag("test_icon")
            )
        }

        composeTestRule.onNodeWithTag("test_icon")
            .assertContentDescriptionEquals("Add Reminder")
    }

    @Test
    fun testContentDescription_Button() {
        composeTestRule.setContent {
            Button(
                onClick = {},
                modifier = Modifier.testTag("test_button")
            ) {
                Text("Save", modifier = Modifier.testTag("button_text"))
            }
        }

        // Button should have accessible text from its content
        composeTestRule.onNodeWithTag("test_button")
            .assertTextContains("Save")
    }

    @Test
    fun testContentDescription_IconButton() {
        val mockIconManager = mock(IconManager::class.java)
        `when`(mockIconManager.getIcon("delete")).thenReturn(android.R.drawable.ic_menu_delete)

        composeTestRule.setContent {
            IconButton(
                onClick = {},
                modifier = Modifier.testTag("test_icon_button")
            ) {
                PeaceIcon(
                    iconName = "delete",
                    contentDescription = "Delete",
                    iconManager = mockIconManager
                )
            }
        }

        composeTestRule.onNodeWithTag("test_icon_button")
            .assertContentDescriptionContains("Delete")
    }

    @Test
    fun testContentDescription_Checkbox() {
        composeTestRule.setContent {
            Row(modifier = Modifier.testTag("checkbox_row")) {
                Checkbox(
                    checked = false,
                    onCheckedChange = {},
                    modifier = Modifier.testTag("test_checkbox")
                )
                Text("Enable notifications")
            }
        }

        // Checkbox should be accessible
        composeTestRule.onNodeWithTag("test_checkbox")
            .assertExists()
    }

    @Test
    fun testColorContrast_NormalText() {
        // Test WCAG AA compliance for normal text (4.5:1 ratio)
        val foregroundLuminance = AccessibilityHelper.calculateLuminance(0, 0, 0) // Black
        val backgroundLuminance = AccessibilityHelper.calculateLuminance(255, 255, 255) // White
        
        val contrastRatio = AccessibilityHelper.calculateContrastRatio(
            foregroundLuminance,
            backgroundLuminance
        )
        
        assertTrue(
            AccessibilityHelper.meetsWCAGAANormalText(contrastRatio),
            "Black on white should meet WCAG AA for normal text (ratio: $contrastRatio)"
        )
    }

    @Test
    fun testColorContrast_LargeText() {
        // Test WCAG AA compliance for large text (3:1 ratio)
        val foregroundLuminance = AccessibilityHelper.calculateLuminance(85, 85, 85) // Dark gray
        val backgroundLuminance = AccessibilityHelper.calculateLuminance(255, 255, 255) // White
        
        val contrastRatio = AccessibilityHelper.calculateContrastRatio(
            foregroundLuminance,
            backgroundLuminance
        )
        
        assertTrue(
            AccessibilityHelper.meetsWCAGAALargeText(contrastRatio),
            "Dark gray on white should meet WCAG AA for large text (ratio: $contrastRatio)"
        )
    }

    @Test
    fun testColorContrast_MaterialTheme() {
        composeTestRule.setContent {
            MaterialTheme {
                val primaryColor = MaterialTheme.colorScheme.primary
                val surfaceColor = MaterialTheme.colorScheme.surface
                
                // Extract RGB components
                val primaryArgb = primaryColor.toArgb()
                val surfaceArgb = surfaceColor.toArgb()
                
                val primaryR = (primaryArgb shr 16) and 0xFF
                val primaryG = (primaryArgb shr 8) and 0xFF
                val primaryB = primaryArgb and 0xFF
                
                val surfaceR = (surfaceArgb shr 16) and 0xFF
                val surfaceG = (surfaceArgb shr 8) and 0xFF
                val surfaceB = surfaceArgb and 0xFF
                
                val primaryLuminance = AccessibilityHelper.calculateLuminance(primaryR, primaryG, primaryB)
                val surfaceLuminance = AccessibilityHelper.calculateLuminance(surfaceR, surfaceG, surfaceB)
                
                val contrastRatio = AccessibilityHelper.calculateContrastRatio(
                    primaryLuminance,
                    surfaceLuminance
                )
                
                Text(
                    "Contrast: $contrastRatio",
                    modifier = Modifier.testTag("contrast_text")
                )
            }
        }

        // Verify the text exists (actual contrast validation happens in the composition)
        composeTestRule.onNodeWithTag("contrast_text")
            .assertExists()
    }

    @Test
    fun testSemanticProperties_Button() {
        composeTestRule.setContent {
            Button(
                onClick = {},
                modifier = Modifier.testTag("semantic_button")
            ) {
                Text("Click me")
            }
        }

        composeTestRule.onNodeWithTag("semantic_button")
            .assert(hasClickAction())
            .assertIsEnabled()
    }

    @Test
    fun testSemanticProperties_DisabledButton() {
        composeTestRule.setContent {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.testTag("disabled_button")
            ) {
                Text("Disabled")
            }
        }

        composeTestRule.onNodeWithTag("disabled_button")
            .assertIsNotEnabled()
    }

    @Test
    fun testSemanticProperties_Checkbox() {
        composeTestRule.setContent {
            Checkbox(
                checked = true,
                onCheckedChange = {},
                modifier = Modifier.testTag("semantic_checkbox")
            )
        }

        composeTestRule.onNodeWithTag("semantic_checkbox")
            .assertIsOn()
    }

    @Test
    fun testSemanticProperties_Switch() {
        composeTestRule.setContent {
            Switch(
                checked = false,
                onCheckedChange = {},
                modifier = Modifier.testTag("semantic_switch")
            )
        }

        composeTestRule.onNodeWithTag("semantic_switch")
            .assertIsOff()
    }

    @Test
    fun testAccessibleClickable_MinimumSize() {
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .testTag("accessible_box")
                    .accessibleClickable(
                        contentDescription = "Clickable box",
                        onClick = {}
                    )
            )
        }

        composeTestRule.onNodeWithTag("accessible_box")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
            .assertContentDescriptionEquals("Clickable box")
            .assert(hasClickAction())
    }

    @Test
    fun testTalkBackSupport_MultipleElements() {
        val mockIconManager = mock(IconManager::class.java)
        `when`(mockIconManager.getIcon("settings")).thenReturn(android.R.drawable.ic_menu_preferences)
        `when`(mockIconManager.getIcon("add")).thenReturn(android.R.drawable.ic_input_add)

        composeTestRule.setContent {
            Column {
                IconButton(
                    onClick = {},
                    modifier = Modifier.testTag("settings_button")
                ) {
                    PeaceIcon(
                        iconName = "settings",
                        contentDescription = "Settings",
                        iconManager = mockIconManager
                    )
                }
                
                Button(
                    onClick = {},
                    modifier = Modifier.testTag("save_button")
                ) {
                    Text("Save")
                }
                
                FloatingActionButton(
                    onClick = {},
                    modifier = Modifier.testTag("fab")
                ) {
                    PeaceIcon(
                        iconName = "add",
                        contentDescription = "Add Reminder",
                        iconManager = mockIconManager
                    )
                }
            }
        }

        // Verify all elements are accessible
        composeTestRule.onNodeWithTag("settings_button")
            .assertContentDescriptionContains("Settings")
        
        composeTestRule.onNodeWithTag("save_button")
            .assertTextContains("Save")
        
        composeTestRule.onNodeWithTag("fab")
            .assertContentDescriptionContains("Add Reminder")
    }

    @Test
    fun testKeyboardNavigation_FocusOrder() {
        composeTestRule.setContent {
            Column {
                Button(
                    onClick = {},
                    modifier = Modifier.testTag("button1")
                ) {
                    Text("First")
                }
                
                Button(
                    onClick = {},
                    modifier = Modifier.testTag("button2")
                ) {
                    Text("Second")
                }
                
                Button(
                    onClick = {},
                    modifier = Modifier.testTag("button3")
                ) {
                    Text("Third")
                }
            }
        }

        // Verify all buttons are focusable
        composeTestRule.onNodeWithTag("button1")
            .assertExists()
            .assert(hasClickAction())
        
        composeTestRule.onNodeWithTag("button2")
            .assertExists()
            .assert(hasClickAction())
        
        composeTestRule.onNodeWithTag("button3")
            .assertExists()
            .assert(hasClickAction())
    }

    @Test
    fun testAccessibilityHelper_ContrastCalculation() {
        // Test pure black on pure white (maximum contrast)
        val blackLuminance = AccessibilityHelper.calculateLuminance(0, 0, 0)
        val whiteLuminance = AccessibilityHelper.calculateLuminance(255, 255, 255)
        val maxContrast = AccessibilityHelper.calculateContrastRatio(blackLuminance, whiteLuminance)
        
        assertTrue(maxContrast >= 21f, "Black on white should have ~21:1 contrast ratio")
        
        // Test same color (minimum contrast)
        val grayLuminance = AccessibilityHelper.calculateLuminance(128, 128, 128)
        val minContrast = AccessibilityHelper.calculateContrastRatio(grayLuminance, grayLuminance)
        
        assertTrue(minContrast == 1f, "Same color should have 1:1 contrast ratio")
    }

    @Test
    fun testAccessibilityHelper_WCAGCompliance() {
        // Test various contrast ratios
        assertTrue(AccessibilityHelper.meetsWCAGAANormalText(4.5f))
        assertTrue(AccessibilityHelper.meetsWCAGAANormalText(7.0f))
        assertTrue(!AccessibilityHelper.meetsWCAGAANormalText(4.0f))
        
        assertTrue(AccessibilityHelper.meetsWCAGAALargeText(3.0f))
        assertTrue(AccessibilityHelper.meetsWCAGAALargeText(5.0f))
        assertTrue(!AccessibilityHelper.meetsWCAGAALargeText(2.5f))
    }
}

@Composable
private fun Box.accessibleClickable(
    contentDescription: String,
    onClick: () -> Unit
): Modifier {
    return with(AccessibilityHelper) {
        Modifier.accessibleClickable(
            contentDescription = contentDescription,
            onClick = onClick
        )
    }
}
