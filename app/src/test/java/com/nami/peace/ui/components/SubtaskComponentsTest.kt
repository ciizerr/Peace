package com.nami.peace.ui.components

import com.nami.peace.domain.model.Subtask
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Subtask UI components logic.
 * Tests the helper functions and data transformations.
 */
class SubtaskComponentsTest {

    @Test
    fun subtask_hasCorrectProperties() {
        // Given
        val subtask = Subtask(
            id = 1,
            reminderId = 100,
            title = "Test Subtask",
            isCompleted = false,
            order = 0,
            createdAt = 1234567890L
        )

        // Then
        assertEquals(1, subtask.id)
        assertEquals(100, subtask.reminderId)
        assertEquals("Test Subtask", subtask.title)
        assertFalse(subtask.isCompleted)
        assertEquals(0, subtask.order)
        assertEquals(1234567890L, subtask.createdAt)
    }

    @Test
    fun subtask_canBeCompleted() {
        // Given
        val subtask = Subtask(
            id = 1,
            reminderId = 1,
            title = "Test Subtask",
            isCompleted = false,
            order = 0
        )

        // When
        val completedSubtask = subtask.copy(isCompleted = true)

        // Then
        assertTrue(completedSubtask.isCompleted)
        assertFalse(subtask.isCompleted) // Original unchanged
    }

    @Test
    fun subtask_canBeReordered() {
        // Given
        val subtask = Subtask(
            id = 1,
            reminderId = 1,
            title = "Test Subtask",
            isCompleted = false,
            order = 0
        )

        // When
        val reorderedSubtask = subtask.copy(order = 5)

        // Then
        assertEquals(5, reorderedSubtask.order)
        assertEquals(0, subtask.order) // Original unchanged
    }

    @Test
    fun subtaskList_canBeFiltered() {
        // Given
        val subtasks = listOf(
            Subtask(id = 1, reminderId = 1, title = "Subtask 1", isCompleted = false, order = 0),
            Subtask(id = 2, reminderId = 1, title = "Subtask 2", isCompleted = true, order = 1),
            Subtask(id = 3, reminderId = 1, title = "Subtask 3", isCompleted = false, order = 2)
        )

        // When
        val completedSubtasks = subtasks.filter { it.isCompleted }
        val incompleteSubtasks = subtasks.filter { !it.isCompleted }

        // Then
        assertEquals(1, completedSubtasks.size)
        assertEquals(2, incompleteSubtasks.size)
        assertEquals("Subtask 2", completedSubtasks.first().title)
    }

    @Test
    fun subtaskList_canBeSortedByOrder() {
        // Given
        val subtasks = listOf(
            Subtask(id = 1, reminderId = 1, title = "Subtask 1", isCompleted = false, order = 2),
            Subtask(id = 2, reminderId = 1, title = "Subtask 2", isCompleted = true, order = 0),
            Subtask(id = 3, reminderId = 1, title = "Subtask 3", isCompleted = false, order = 1)
        )

        // When
        val sortedSubtasks = subtasks.sortedBy { it.order }

        // Then
        assertEquals("Subtask 2", sortedSubtasks[0].title)
        assertEquals("Subtask 3", sortedSubtasks[1].title)
        assertEquals("Subtask 1", sortedSubtasks[2].title)
    }

    @Test
    fun subtaskProgress_calculatesCorrectly() {
        // Given
        val subtasks = listOf(
            Subtask(id = 1, reminderId = 1, title = "Subtask 1", isCompleted = true, order = 0),
            Subtask(id = 2, reminderId = 1, title = "Subtask 2", isCompleted = true, order = 1),
            Subtask(id = 3, reminderId = 1, title = "Subtask 3", isCompleted = false, order = 2),
            Subtask(id = 4, reminderId = 1, title = "Subtask 4", isCompleted = false, order = 3)
        )

        // When
        val completedCount = subtasks.count { it.isCompleted }
        val totalCount = subtasks.size
        val progress = if (totalCount > 0) (completedCount * 100) / totalCount else 0

        // Then
        assertEquals(2, completedCount)
        assertEquals(4, totalCount)
        assertEquals(50, progress)
    }

    @Test
    fun subtaskProgress_handlesEmptyList() {
        // Given
        val subtasks = emptyList<Subtask>()

        // When
        val completedCount = subtasks.count { it.isCompleted }
        val totalCount = subtasks.size
        val progress = if (totalCount > 0) (completedCount * 100) / totalCount else 0

        // Then
        assertEquals(0, completedCount)
        assertEquals(0, totalCount)
        assertEquals(0, progress)
    }

    @Test
    fun subtaskProgress_handlesAllCompleted() {
        // Given
        val subtasks = listOf(
            Subtask(id = 1, reminderId = 1, title = "Subtask 1", isCompleted = true, order = 0),
            Subtask(id = 2, reminderId = 1, title = "Subtask 2", isCompleted = true, order = 1)
        )

        // When
        val completedCount = subtasks.count { it.isCompleted }
        val totalCount = subtasks.size
        val progress = if (totalCount > 0) (completedCount * 100) / totalCount else 0

        // Then
        assertEquals(2, completedCount)
        assertEquals(2, totalCount)
        assertEquals(100, progress)
    }

    @Test
    fun subtaskTitle_canBeTrimmed() {
        // Given
        val title = "  Test Subtask  "

        // When
        val trimmedTitle = title.trim()

        // Then
        assertEquals("Test Subtask", trimmedTitle)
    }

    @Test
    fun subtaskTitle_canBeValidated() {
        // Given
        val validTitle = "Valid Subtask"
        val emptyTitle = ""
        val blankTitle = "   "

        // When/Then
        assertTrue(validTitle.isNotBlank())
        assertFalse(emptyTitle.isNotBlank())
        assertFalse(blankTitle.isNotBlank())
    }
}
