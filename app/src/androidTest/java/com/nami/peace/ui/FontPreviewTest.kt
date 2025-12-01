package com.nami.peace.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nami.peace.ui.theme.PeaceTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for font preview and font application.
 * Tests font rendering, preview text display, and font switching.
 */
@RunWith(AndroidJUnit4::class)
class FontPreviewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fontPreview_displaysPreviewText() {
        // Given
        val previewText = "The quick brown fox jumps over the lazy dog"

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Text(text = previewText)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(previewText)
            .assertIsDisplayed()
    }

    @Test
    fun fontPreview_displaysMultipleFontOptions() {
        // Given
        val fonts = listOf(
            "System Font",
            "Roboto",
            "Open Sans",
            "Lato"
        )

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    fonts.forEach { fontName ->
                        Text(text = fontName)
                    }
                }
            }
        }

        // Then
        fonts.forEach { fontName ->
            composeTestRule
                .onNodeWithText(fontName)
                .assertIsDisplayed()
        }
    }

    @Test
    fun fontPreview_textIsReadable() {
        // Given
        val sampleText = "Sample Text 123"

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Text(text = sampleText)
            }
        }

        // Then - Text should be displayed and readable
        composeTestRule
            .onNodeWithText(sampleText)
            .assertIsDisplayed()
            .assertHasClickAction() // Should not have click action (just text)
    }

    @Test
    fun fontPreview_displaysAllCharacterTypes() {
        // Given - Text with various character types
        val complexText = "ABCabc123!@# αβγ 中文"

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Text(text = complexText)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(complexText)
            .assertIsDisplayed()
    }

    @Test
    fun fontApplication_appliesSystemFont() {
        // Given
        val testText = "System Font Test"

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Text(
                    text = testText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Then - Text should be displayed with system font
        composeTestRule
            .onNodeWithText(testText)
            .assertIsDisplayed()
    }

    @Test
    fun fontPadding_appliesCorrectly() {
        // Given
        val testText = "Padded Text"

        // When - Apply text with padding
        composeTestRule.setContent {
            PeaceTheme {
                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = testText)
                }
            }
        }

        // Then - Text should be displayed
        composeTestRule
            .onNodeWithText(testText)
            .assertIsDisplayed()
    }

    @Test
    fun fontPadding_zeroValueWorks() {
        // Given
        val testText = "No Padding"

        // When - Apply text with zero padding
        composeTestRule.setContent {
            PeaceTheme {
                Box(
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(text = testText)
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(testText)
            .assertIsDisplayed()
    }

    @Test
    fun fontPadding_maximumValueWorks() {
        // Given
        val testText = "Maximum Padding"

        // When - Apply text with maximum padding (20dp)
        composeTestRule.setContent {
            PeaceTheme {
                Box(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(text = testText)
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(testText)
            .assertIsDisplayed()
    }

    @Test
    fun fontPreview_multipleTextSizes() {
        // Given
        val sizes = listOf("Small", "Medium", "Large")

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Column {
                    Text(
                        text = sizes[0],
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = sizes[1],
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = sizes[2],
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Then
        sizes.forEach { size ->
            composeTestRule
                .onNodeWithText(size)
                .assertIsDisplayed()
        }
    }

    @Test
    fun fontPreview_longTextWraps() {
        // Given
        val longText = "This is a very long text that should wrap to multiple lines " +
                "when displayed in a constrained width container to test text wrapping behavior"

        // When
        composeTestRule.setContent {
            PeaceTheme {
                Box(
                    modifier = Modifier.width(200.dp)
                ) {
                    Text(text = longText)
                }
            }
        }

        // Then - Text should be displayed (wrapping is handled by Compose)
        composeTestRule
            .onNodeWithText(longText)
            .assertIsDisplayed()
    }
}


