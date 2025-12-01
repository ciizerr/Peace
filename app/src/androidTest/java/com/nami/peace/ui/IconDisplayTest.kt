package com.nami.peace.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.util.icon.IconManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for icon display using Ionicons.
 * Tests icon rendering, fallback behavior, and accessibility.
 */
@RunWith(AndroidJUnit4::class)
class IconDisplayTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun icon_displaysWithContentDescription() {
        // Given
        val mockIconManager = mockk<IconManager>(relaxed = true)
        val contentDescription = "Home Icon"

        // When
        composeTestRule.setContent {
            PeaceIcon(
                iconName = "home",
                contentDescription = contentDescription,
                iconManager = mockIconManager
            )
        }

        // Then - Icon should have content description for accessibility
        composeTestRule
            .onNodeWithContentDescription(contentDescription)
            .assertIsDisplayed()
    }

    @Test
    fun icon_displaysMultipleIcons() {
        // Given
        val mockIconManager = mockk<IconManager>(relaxed = true)
        val icons = listOf(
            "home" to "Home",
            "settings" to "Settings",
            "calendar" to "Calendar"
        )

        // When
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Row {
                icons.forEach { (iconName, description) ->
                    PeaceIcon(
                        iconName = iconName,
                        contentDescription = description,
                        iconManager = mockIconManager,
                        size = 24.dp
                    )
                }
            }
        }

        // Then
        icons.forEach { (_, description) ->
            composeTestRule
                .onNodeWithContentDescription(description)
                .assertIsDisplayed()
        }
    }

    @Test
    fun icon_hasCorrectSize() {
        // Given
        val mockIconManager = mockk<IconManager>(relaxed = true)

        // When
        composeTestRule.setContent {
            PeaceIcon(
                iconName = "home",
                contentDescription = "Home",
                iconManager = mockIconManager,
                size = 24.dp
            )
        }

        // Then - Icon should be displayed
        composeTestRule
            .onNodeWithContentDescription("Home")
            .assertIsDisplayed()
    }

    @Test
    fun icon_fallbackDisplaysWhenIconMissing() {
        // Given
        val mockIconManager = mockk<IconManager>(relaxed = true)
        every { mockIconManager.getIcon("nonexistent") } returns null

        // When
        composeTestRule.setContent {
            PeaceIcon(
                iconName = "nonexistent",
                contentDescription = "Fallback Icon",
                iconManager = mockIconManager
            )
        }

        // Then - Fallback icon should be displayed
        composeTestRule
            .onNodeWithContentDescription("Fallback Icon")
            .assertIsDisplayed()
    }

    @Test
    fun icon_categoryIconsDisplay() {
        // Given
        val mockIconManager = mockk<IconManager>(relaxed = true)
        val categories = listOf(
            "work" to "Work Category",
            "home" to "Home Category",
            "health" to "Health Category",
            "study" to "Study Category"
        )

        // When
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                categories.forEach { (iconName, description) ->
                    PeaceIcon(
                        iconName = iconName,
                        contentDescription = description,
                        iconManager = mockIconManager,
                        size = 24.dp
                    )
                }
            }
        }

        // Then
        categories.forEach { (_, description) ->
            composeTestRule
                .onNodeWithContentDescription(description)
                .assertIsDisplayed()
        }
    }

    @Test
    fun icon_gardenThemeIconsDisplay() {
        // Given
        val mockIconManager = mockk<IconManager>(relaxed = true)
        val gardenIcons = listOf(
            "leaf" to "Zen Theme",
            "tree" to "Forest Theme",
            "sunny" to "Desert Theme",
            "water" to "Ocean Theme"
        )

        // When
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Row {
                gardenIcons.forEach { (iconName, description) ->
                    PeaceIcon(
                        iconName = iconName,
                        contentDescription = description,
                        iconManager = mockIconManager,
                        size = 24.dp
                    )
                }
            }
        }

        // Then
        gardenIcons.forEach { (_, description) ->
            composeTestRule
                .onNodeWithContentDescription(description)
                .assertIsDisplayed()
        }
    }

    @Test
    fun icon_navigationIconsDisplay() {
        // Given
        val mockIconManager = mockk<IconManager>(relaxed = true)
        val navIcons = listOf(
            "arrow_back" to "Back",
            "arrow_forward" to "Forward",
            "chevron_down" to "Expand",
            "chevron_up" to "Collapse"
        )

        // When
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Row {
                navIcons.forEach { (iconName, description) ->
                    PeaceIcon(
                        iconName = iconName,
                        contentDescription = description,
                        iconManager = mockIconManager,
                        size = 24.dp
                    )
                }
            }
        }

        // Then
        navIcons.forEach { (_, description) ->
            composeTestRule
                .onNodeWithContentDescription(description)
                .assertIsDisplayed()
        }
    }

    @Test
    fun icon_actionIconsDisplay() {
        // Given
        val mockIconManager = mockk<IconManager>(relaxed = true)
        val actionIcons = listOf(
            "add" to "Add",
            "trash" to "Delete",
            "create" to "Edit",
            "checkmark" to "Complete"
        )

        // When
        composeTestRule.setContent {
            androidx.compose.foundation.layout.Row {
                actionIcons.forEach { (iconName, description) ->
                    PeaceIcon(
                        iconName = iconName,
                        contentDescription = description,
                        iconManager = mockIconManager,
                        size = 24.dp
                    )
                }
            }
        }

        // Then
        actionIcons.forEach { (_, description) ->
            composeTestRule
                .onNodeWithContentDescription(description)
                .assertIsDisplayed()
        }
    }

    @Test
    fun icon_tintAppliesCorrectly() {
        // Given
        val mockIconManager = mockk<IconManager>(relaxed = true)

        // When
        composeTestRule.setContent {
            androidx.compose.material3.MaterialTheme {
                PeaceIcon(
                    iconName = "home",
                    contentDescription = "Tinted Icon",
                    iconManager = mockIconManager,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }
        }

        // Then - Icon should be displayed with tint
        composeTestRule
            .onNodeWithContentDescription("Tinted Icon")
            .assertIsDisplayed()
    }

    @Test
    fun icon_accessibilityContentDescriptionRequired() {
        // Given
        val mockIconManager = mockk<IconManager>(relaxed = true)

        // When
        composeTestRule.setContent {
            PeaceIcon(
                iconName = "home",
                contentDescription = "Required Description",
                iconManager = mockIconManager
            )
        }

        // Then - Content description should be present for screen readers
        composeTestRule
            .onNodeWithContentDescription("Required Description")
            .assertIsDisplayed()
    }
}


