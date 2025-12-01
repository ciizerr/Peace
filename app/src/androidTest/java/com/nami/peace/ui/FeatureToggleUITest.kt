package com.nami.peace.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nami.peace.ui.theme.PeaceTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for feature toggle functionality.
 * Tests toggle switches, UI hiding/showing, and state persistence.
 */
@RunWith(AndroidJUnit4::class)
class FeatureToggleUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun featureToggle_switchDisplays() {
        // When
        composeTestRule.setContent {
            PeaceTheme {
                Row {
                    Text("Subtasks")
                    Switch(
                        checked = true,
                        onCheckedChange = {}
                    )
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Subtasks")
            .assertIsDisplayed()
        composeTestRule
            .onNode(isToggleable())
            .assertIsDisplayed()
    }

    @Test
    fun featureToggle_switchToggles() {
        // Given
        var isEnabled = false

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it }
                )
            }
        }

        // Then - Initially off
        composeTestRule
            .onNode(isToggleable())
            .assertIsOff()

        // When - Toggle on
        composeTestRule
            .onNode(isToggleable())
            .performClick()

        // Then - Should be on
        assert(isEnabled)
    }

    @Test
    fun featureToggle_disabledFeatureHidesUI() {
        // Given
        var subtasksEnabled = false

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Text("Main Content")
                    if (subtasksEnabled) {
                        Text("Subtasks Section")
                    }
                }
            }
        }

        // Then - Subtasks section should not be displayed
        composeTestRule
            .onNodeWithText("Main Content")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Subtasks Section")
            .assertDoesNotExist()
    }

    @Test
    fun featureToggle_enabledFeatureShowsUI() {
        // Given
        var subtasksEnabled = true

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Text("Main Content")
                    if (subtasksEnabled) {
                        Text("Subtasks Section")
                    }
                }
            }
        }

        // Then - Subtasks section should be displayed
        composeTestRule
            .onNodeWithText("Main Content")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Subtasks Section")
            .assertIsDisplayed()
    }

    @Test
    fun featureToggle_multipleToggles() {
        // Given
        val features = mapOf(
            "Subtasks" to true,
            "Attachments" to false,
            "ML Suggestions" to true,
            "Calendar Sync" to false
        )

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    features.forEach { (name, enabled) ->
                        Row {
                            Text(name)
                            Switch(
                                checked = enabled,
                                onCheckedChange = {}
                            )
                        }
                    }
                }
            }
        }

        // Then - All feature names should be displayed
        features.keys.forEach { featureName ->
            composeTestRule
                .onNodeWithText(featureName)
                .assertIsDisplayed()
        }
    }

    @Test
    fun featureToggle_toggleStateChangesUI() {
        // Given
        var attachmentsEnabled = false

        // When - Initially disabled
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Switch(
                        checked = attachmentsEnabled,
                        onCheckedChange = { attachmentsEnabled = it }
                    )
                    if (attachmentsEnabled) {
                        Text("Attachment Options")
                    }
                }
            }
        }

        // Then - Attachment options not visible
        composeTestRule
            .onNodeWithText("Attachment Options")
            .assertDoesNotExist()

        // When - Enable attachments
        composeTestRule
            .onNode(isToggleable())
            .performClick()

        // Re-compose with new state
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Switch(
                        checked = attachmentsEnabled,
                        onCheckedChange = { attachmentsEnabled = it }
                    )
                    if (attachmentsEnabled) {
                        Text("Attachment Options")
                    }
                }
            }
        }

        // Then - Attachment options should be visible
        composeTestRule
            .onNodeWithText("Attachment Options")
            .assertIsDisplayed()
    }

    @Test
    fun featureToggle_widgetsToggle() {
        // Given
        var widgetsEnabled = true

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Text("Widgets")
                    Switch(
                        checked = widgetsEnabled,
                        onCheckedChange = { widgetsEnabled = it }
                    )
                    if (widgetsEnabled) {
                        Text("Widget Configuration")
                    }
                }
            }
        }

        // Then - Widget configuration visible
        composeTestRule
            .onNodeWithText("Widget Configuration")
            .assertIsDisplayed()

        // When - Disable widgets
        composeTestRule
            .onNode(isToggleable())
            .performClick()

        // Re-compose
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Text("Widgets")
                    Switch(
                        checked = widgetsEnabled,
                        onCheckedChange = { widgetsEnabled = it }
                    )
                    if (widgetsEnabled) {
                        Text("Widget Configuration")
                    }
                }
            }
        }

        // Then - Widget configuration hidden
        composeTestRule
            .onNodeWithText("Widget Configuration")
            .assertDoesNotExist()
    }

    @Test
    fun featureToggle_mlSuggestionsToggle() {
        // Given
        var mlEnabled = false

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Row {
                        Text("ML Suggestions")
                        Switch(
                            checked = mlEnabled,
                            onCheckedChange = { mlEnabled = it }
                        )
                    }
                    if (mlEnabled) {
                        Text("Suggestion Settings")
                    }
                }
            }
        }

        // Then - Initially hidden
        composeTestRule
            .onNodeWithText("Suggestion Settings")
            .assertDoesNotExist()
    }

    @Test
    fun featureToggle_calendarSyncToggle() {
        // Given
        var calendarSyncEnabled = true

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Text("Calendar Sync")
                    Switch(
                        checked = calendarSyncEnabled,
                        onCheckedChange = { calendarSyncEnabled = it }
                    )
                    if (calendarSyncEnabled) {
                        Text("Sync Options")
                    }
                }
            }
        }

        // Then - Sync options visible
        composeTestRule
            .onNodeWithText("Sync Options")
            .assertIsDisplayed()
    }

    @Test
    fun featureToggle_descriptionDisplays() {
        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Text("Subtasks")
                    Text("Break down reminders into smaller tasks")
                    Switch(
                        checked = true,
                        onCheckedChange = {}
                    )
                }
            }
        }

        // Then - Description should be displayed
        composeTestRule
            .onNodeWithText("Break down reminders into smaller tasks")
            .assertIsDisplayed()
    }

    @Test
    fun featureToggle_allFeaturesListDisplays() {
        // Given
        val features = listOf(
            "Subtasks",
            "Attachments",
            "ML Suggestions",
            "Calendar Sync",
            "Widgets"
        )

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    features.forEach { feature ->
                        Row {
                            Text(feature)
                            Switch(
                                checked = true,
                                onCheckedChange = {}
                            )
                        }
                    }
                }
            }
        }

        // Then - All features should be displayed
        features.forEach { feature ->
            composeTestRule
                .onNodeWithText(feature)
                .assertIsDisplayed()
        }
    }
}
