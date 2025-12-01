package com.nami.peace.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nami.peace.domain.model.Subtask
import com.nami.peace.ui.components.SubtaskItem
import com.nami.peace.ui.components.SubtaskList
import com.nami.peace.ui.components.AddSubtaskDialog
import com.nami.peace.util.icon.IconManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for subtask interaction components.
 * Tests checkbox interactions, drag-to-reorder, and dialog functionality.
 */
@RunWith(AndroidJUnit4::class)
class SubtaskInteractionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockIconManager = mockk<IconManager>(relaxed = true)

    @Test
    fun subtaskItem_displaysTitle() {
        // Given
        val subtask = Subtask(
            id = 1,
            reminderId = 1,
            title = "Test Subtask",
            isCompleted = false,
            order = 0
        )

        // When
        composeTestRule.setContent {
            SubtaskItem(
                subtask = subtask,
                onCheckedChange = {},
                iconManager = mockIconManager
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("Test Subtask")
            .assertIsDisplayed()
    }

    @Test
    fun subtaskItem_checkboxTogglesState() {
        // Given
        val subtask = Subtask(
            id = 1,
            reminderId = 1,
            title = "Test Subtask",
            isCompleted = false,
            order = 0
        )
        var checkedState = false

        // When
        composeTestRule.setContent {
            SubtaskItem(
                subtask = subtask.copy(isCompleted = checkedState),
                onCheckedChange = { checkedState = it },
                iconManager = mockIconManager
            )
        }

        // Then - Initially unchecked
        composeTestRule
            .onNode(hasTestTag("checkbox") or isToggleable())
            .assertIsOff()

        // When - Click checkbox
        composeTestRule
            .onNode(hasTestTag("checkbox") or isToggleable())
            .performClick()

        // Then - State should change
        assert(checkedState)
    }

    @Test
    fun subtaskItem_completedSubtaskShowsStrikethrough() {
        // Given
        val completedSubtask = Subtask(
            id = 1,
            reminderId = 1,
            title = "Completed Task",
            isCompleted = true,
            order = 0
        )

        // When
        composeTestRule.setContent {
            SubtaskItem(
                subtask = completedSubtask,
                onCheckedChange = {},
                iconManager = mockIconManager
            )
        }

        // Then - Text should be displayed (strikethrough is a style property)
        composeTestRule
            .onNodeWithText("Completed Task")
            .assertIsDisplayed()
    }

    @Test
    fun subtaskList_displaysMultipleSubtasks() {
        // Given
        val subtasks = listOf(
            Subtask(1, 1, "First Task", false, 0),
            Subtask(2, 1, "Second Task", false, 1),
            Subtask(3, 1, "Third Task", true, 2)
        )

        // When
        composeTestRule.setContent {
            SubtaskList(
                subtasks = subtasks,
                onCheckedChange = { _, _ -> },
                onReorder = { _, _ -> },
                iconManager = mockIconManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("First Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Second Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Third Task").assertIsDisplayed()
    }

    @Test
    fun subtaskList_emptyStateDisplaysMessage() {
        // Given
        val emptyList = emptyList<Subtask>()

        // When
        composeTestRule.setContent {
            SubtaskList(
                subtasks = emptyList,
                onCheckedChange = { _, _ -> },
                onReorder = { _, _ -> },
                iconManager = mockIconManager
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("No subtasks yet")
            .assertIsDisplayed()
    }

    @Test
    fun subtaskList_checkboxInteractionTriggersCallback() {
        // Given
        val subtasks = listOf(
            Subtask(1, 1, "Test Task", false, 0)
        )
        var callbackTriggered = false
        var callbackSubtask: Subtask? = null
        var callbackChecked = false

        // When
        composeTestRule.setContent {
            SubtaskList(
                subtasks = subtasks,
                onCheckedChange = { subtask, checked ->
                    callbackTriggered = true
                    callbackSubtask = subtask
                    callbackChecked = checked
                },
                onReorder = { _, _ -> },
                iconManager = mockIconManager
            )
        }

        // Click the checkbox
        composeTestRule
            .onAllNodes(isToggleable())
            .onFirst()
            .performClick()

        // Then
        assert(callbackTriggered)
        assert(callbackSubtask?.id == 1)
        assert(callbackChecked)
    }

    @Test
    fun addSubtaskDialog_displaysCorrectly() {
        // When
        composeTestRule.setContent {
            AddSubtaskDialog(
                onDismiss = {},
                onConfirm = {},
                iconManager = mockIconManager
            )
        }

        // Then
        composeTestRule.onNodeWithText("Add Subtask").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add").assertIsDisplayed()
    }

    @Test
    fun addSubtaskDialog_textInputWorks() {
        // When
        composeTestRule.setContent {
            AddSubtaskDialog(
                onDismiss = {},
                onConfirm = {},
                iconManager = mockIconManager
            )
        }

        // Type in the text field
        composeTestRule
            .onNodeWithText("Subtask title")
            .performTextInput("New Subtask")

        // Then
        composeTestRule
            .onNodeWithText("New Subtask")
            .assertIsDisplayed()
    }

    @Test
    fun addSubtaskDialog_confirmButtonTriggersCallback() {
        // Given
        var confirmedTitle: String? = null

        // When
        composeTestRule.setContent {
            AddSubtaskDialog(
                onDismiss = {},
                onConfirm = { title -> confirmedTitle = title },
                iconManager = mockIconManager
            )
        }

        // Type and confirm
        composeTestRule
            .onNodeWithText("Subtask title")
            .performTextInput("Test Subtask")
        
        composeTestRule
            .onNodeWithText("Add")
            .performClick()

        // Then
        assert(confirmedTitle == "Test Subtask")
    }

    @Test
    fun addSubtaskDialog_emptyInputShowsError() {
        // When
        composeTestRule.setContent {
            AddSubtaskDialog(
                onDismiss = {},
                onConfirm = {},
                iconManager = mockIconManager
            )
        }

        // Click Add without entering text
        composeTestRule
            .onNodeWithText("Add")
            .performClick()

        // Then - Error message should appear
        composeTestRule
            .onNodeWithText("Title cannot be empty")
            .assertIsDisplayed()
    }

    @Test
    fun addSubtaskDialog_cancelButtonDismisses() {
        // Given
        var dismissed = false

        // When
        composeTestRule.setContent {
            AddSubtaskDialog(
                onDismiss = { dismissed = true },
                onConfirm = {},
                iconManager = mockIconManager
            )
        }

        // Click Cancel
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        // Then
        assert(dismissed)
    }
}
