package com.nami.peace.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nami.peace.ui.components.ProgressBar
import com.nami.peace.ui.components.ProgressBarWithCounts
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for progress bar rendering.
 * Tests progress display, percentage calculation, and visual updates.
 */
@RunWith(AndroidJUnit4::class)
class ProgressBarRenderingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun progressBar_displaysZeroProgress() {
        // When
        composeTestRule.setContent {
            ProgressBar(progress = 0)
        }

        // Then - Should display 0%
        composeTestRule
            .onNodeWithText("0%")
            .assertIsDisplayed()
    }

    @Test
    fun progressBar_displaysPartialProgress() {
        // When
        composeTestRule.setContent {
            ProgressBar(progress = 50)
        }

        // Then - Should display 50%
        composeTestRule
            .onNodeWithText("50%")
            .assertIsDisplayed()
    }

    @Test
    fun progressBar_displaysFullProgress() {
        // When
        composeTestRule.setContent {
            ProgressBar(progress = 100)
        }

        // Then - Should display 100%
        composeTestRule
            .onNodeWithText("100%")
            .assertIsDisplayed()
    }

    @Test
    fun progressBar_handlesInvalidProgressValues() {
        // Test negative value (should be clamped to 0)
        composeTestRule.setContent {
            ProgressBar(progress = -10)
        }
        
        composeTestRule
            .onNodeWithText("0%")
            .assertIsDisplayed()

        // Test value over 100 (should be clamped to 100)
        composeTestRule.setContent {
            ProgressBar(progress = 150)
        }
        
        composeTestRule
            .onNodeWithText("100%")
            .assertIsDisplayed()
    }

    @Test
    fun progressBarWithCounts_displaysCorrectCounts() {
        // When
        composeTestRule.setContent {
            ProgressBarWithCounts(
                completedCount = 3,
                totalCount = 5
            )
        }

        // Then - Should display "3 of 5 complete"
        composeTestRule
            .onNodeWithText("3 of 5 complete")
            .assertIsDisplayed()
    }

    @Test
    fun progressBarWithCounts_displaysZeroCompleted() {
        // When
        composeTestRule.setContent {
            ProgressBarWithCounts(
                completedCount = 0,
                totalCount = 10
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("0 of 10 complete")
            .assertIsDisplayed()
    }

    @Test
    fun progressBarWithCounts_displaysAllCompleted() {
        // When
        composeTestRule.setContent {
            ProgressBarWithCounts(
                completedCount = 5,
                totalCount = 5
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("5 of 5 complete")
            .assertIsDisplayed()
    }

    @Test
    fun progressBarWithCounts_calculatesCorrectPercentage() {
        // When - 3 out of 5 = 60%
        composeTestRule.setContent {
            ProgressBarWithCounts(
                completedCount = 3,
                totalCount = 5
            )
        }

        // Then - Should display 60%
        composeTestRule
            .onNodeWithText("60%")
            .assertIsDisplayed()
    }

    @Test
    fun progressBarWithCounts_handlesZeroTotal() {
        // When - Edge case: 0 total items
        composeTestRule.setContent {
            ProgressBarWithCounts(
                completedCount = 0,
                totalCount = 0
            )
        }

        // Then - Should display 0% and "0 of 0 complete"
        composeTestRule
            .onNodeWithText("0%")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("0 of 0 complete")
            .assertIsDisplayed()
    }

    @Test
    fun progressBar_visualIndicatorExists() {
        // When
        composeTestRule.setContent {
            ProgressBar(progress = 75)
        }

        // Then - The progress bar component should be displayed
        // We can't directly test the visual indicator, but we can verify the component renders
        composeTestRule
            .onNodeWithText("75%")
            .assertExists()
    }

    @Test
    fun progressBarWithCounts_multipleProgressValues() {
        // Test various progress values
        val testCases = listOf(
            Triple(1, 10, 10),  // 1/10 = 10%
            Triple(2, 8, 25),   // 2/8 = 25%
            Triple(7, 10, 70),  // 7/10 = 70%
            Triple(9, 10, 90)   // 9/10 = 90%
        )

        testCases.forEach { (completed, total, expectedPercent) ->
            composeTestRule.setContent {
                ProgressBarWithCounts(
                    completedCount = completed,
                    totalCount = total
                )
            }

            composeTestRule
                .onNodeWithText("$expectedPercent%")
                .assertIsDisplayed()
        }
    }
}
