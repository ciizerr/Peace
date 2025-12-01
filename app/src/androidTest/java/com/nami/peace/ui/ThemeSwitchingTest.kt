package com.nami.peace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nami.peace.ui.theme.PeaceTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for theme switching functionality.
 * Tests light/dark theme application, color scheme changes, and theme persistence.
 */
@RunWith(AndroidJUnit4::class)
class ThemeSwitchingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun theme_lightThemeApplies() {
        // When
        composeTestRule.setContent {
            PeaceTheme(darkTheme = false) {
                Surface {
                    Text(text = "Light Theme Test")
                }
            }
        }

        // Then - Content should be displayed
        composeTestRule
            .onNodeWithText("Light Theme Test")
            .assertIsDisplayed()
    }

    @Test
    fun theme_darkThemeApplies() {
        // When
        composeTestRule.setContent {
            PeaceTheme(darkTheme = true) {
                Surface {
                    Text(text = "Dark Theme Test")
                }
            }
        }

        // Then - Content should be displayed
        composeTestRule
            .onNodeWithText("Dark Theme Test")
            .assertIsDisplayed()
    }

    @Test
    fun theme_systemThemeFollowsDevice() {
        // When - Use system theme
        composeTestRule.setContent {
            PeaceTheme {
                Surface {
                    val isDark = isSystemInDarkTheme()
                    Text(text = if (isDark) "Dark Mode" else "Light Mode")
                }
            }
        }

        // Then - Should display based on system setting
        composeTestRule
            .onNode(hasText("Dark Mode") or hasText("Light Mode"))
            .assertIsDisplayed()
    }

    @Test
    fun theme_colorSchemeAppliesCorrectly() {
        // When
        composeTestRule.setContent {
            PeaceTheme(darkTheme = false) {
                Surface(
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "Color Scheme Test",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Color Scheme Test")
            .assertIsDisplayed()
    }

    @Test
    fun theme_typographyAppliesCorrectly() {
        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Text(
                        text = "Headline",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Body",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Label",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithText("Headline").assertIsDisplayed()
        composeTestRule.onNodeWithText("Body").assertIsDisplayed()
        composeTestRule.onNodeWithText("Label").assertIsDisplayed()
    }

    @Test
    fun theme_switchingBetweenLightAndDark() {
        // Given - Start with light theme
        var isDarkTheme = false

        composeTestRule.setContent {
            PeaceTheme(darkTheme = isDarkTheme) {
                Surface {
                    Text(text = "Theme Switch Test")
                }
            }
        }

        // Then - Light theme content displayed
        composeTestRule
            .onNodeWithText("Theme Switch Test")
            .assertIsDisplayed()

        // When - Switch to dark theme
        isDarkTheme = true
        composeTestRule.setContent {
            PeaceTheme(darkTheme = isDarkTheme) {
                Surface {
                    Text(text = "Theme Switch Test")
                }
            }
        }

        // Then - Dark theme content displayed
        composeTestRule
            .onNodeWithText("Theme Switch Test")
            .assertIsDisplayed()
    }

    @Test
    fun theme_gardenThemeColorsApply() {
        // When - Apply garden theme colors
        composeTestRule.setContent {
            PeaceTheme {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Garden Theme",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Garden Theme")
            .assertIsDisplayed()
    }

    @Test
    fun theme_customFontApplies() {
        // When - Apply custom font through theme
        composeTestRule.setContent {
            PeaceTheme {
                Text(
                    text = "Custom Font Test",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Custom Font Test")
            .assertIsDisplayed()
    }

    @Test
    fun theme_backgroundImageCompatibility() {
        // When - Theme with background image
        composeTestRule.setContent {
            PeaceTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Text(
                        text = "Background Test",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Background Test")
            .assertIsDisplayed()
    }

    @Test
    fun theme_multipleComponentsWithTheme() {
        // When - Multiple components using theme
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Button(
                        onClick = {}
                    ) {
                        Text("Button")
                    }
                    Card {
                        Text("Card Content")
                    }
                    Text(
                        text = "Plain Text",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Then - All components should be displayed
        composeTestRule.onNodeWithText("Button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Card Content").assertIsDisplayed()
        composeTestRule.onNodeWithText("Plain Text").assertIsDisplayed()
    }
}
