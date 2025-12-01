package com.nami.peace.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.GardenDao
import com.nami.peace.data.local.GardenTheme
import com.nami.peace.data.repository.GardenRepositoryImpl
import com.nami.peace.domain.model.GardenState
import com.nami.peace.domain.repository.GardenRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

/**
 * Property-based tests for streak tracking.
 * 
 * **Feature: peace-app-enhancement, Property 32: Streak calculation**
 * **Feature: peace-app-enhancement, Property 34: Streak reset**
 * **Validates: Requirements 18.5, 18.8**
 * 
 * Property 32: For any sequence of task completions, if completions occur on consecutive days,
 * the streak counter should increment; if a day is skipped, it should reset to 1.
 * 
 * Property 34: For any streak break (no completions for 24+ hours), the streak counter should reset to 0.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StreakTrackingPropertyTest {
    
    private lateinit var database: AppDatabase
    private lateinit var gardenDao: GardenDao
    private lateinit var repository: GardenRepository
    private lateinit var updateStreakUseCase: UpdateStreakUseCase
    private lateinit var checkStreakStatusUseCase: CheckStreakStatusUseCase
    private lateinit var getStreakInfoUseCase: GetStreakInfoUseCase
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        gardenDao = database.gardenDao()
        repository = GardenRepositoryImpl(gardenDao, org.mockito.Mockito.mock(com.nami.peace.widget.WidgetUpdateManager::class.java))
        updateStreakUseCase = UpdateStreakUseCase(repository)
        checkStreakStatusUseCase = CheckStreakStatusUseCase(repository)
        getStreakInfoUseCase = GetStreakInfoUseCase(repository)
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun `Property 32 - Streak calculation - first completion starts streak at 1`() = runTest {
        // Initialize with empty state
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 0,
            longestStreak = 0,
            lastCompletionDate = null,
            totalTasksCompleted = 0
        )
        repository.insertGardenState(initialState)
        
        // Complete first task
        val (streakIncremented, newStreak) = updateStreakUseCase()
        
        // Assert
        assertTrue("Streak should be incremented on first completion", streakIncremented)
        assertEquals("First completion should set streak to 1", 1, newStreak)
        
        // Verify state
        val updatedState = repository.getGardenStateOnce()
        assertNotNull("Updated state should not be null", updatedState)
        assertEquals("Current streak should be 1", 1, updatedState!!.currentStreak)
        assertEquals("Longest streak should be 1", 1, updatedState.longestStreak)
        assertNotNull("Last completion date should be set", updatedState.lastCompletionDate)
    }
    
    @Test
    fun `Property 32 - Streak calculation - consecutive day completions increment streak`() = runTest {
        // Test 10 consecutive days
        val startTime = System.currentTimeMillis()
        
        // Initialize with empty state
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 0,
            longestStreak = 0,
            lastCompletionDate = null,
            totalTasksCompleted = 0
        )
        repository.insertGardenState(initialState)
        
        // Complete tasks on consecutive days
        for (day in 0 until 10) {
            val completionTime = startTime + TimeUnit.DAYS.toMillis(day.toLong())
            val (streakIncremented, newStreak) = updateStreakUseCase(completionTime)
            
            // First day or consecutive day should increment
            if (day == 0) {
                assertTrue("First day should increment streak", streakIncremented)
                assertEquals("First day streak should be 1", 1, newStreak)
            } else {
                assertTrue("Consecutive day should increment streak", streakIncremented)
                assertEquals("Streak should be ${day + 1}", day + 1, newStreak)
            }
        }
        
        // Verify final state
        val finalState = repository.getGardenStateOnce()
        assertNotNull("Final state should not be null", finalState)
        assertEquals("Final streak should be 10", 10, finalState!!.currentStreak)
        assertEquals("Longest streak should be 10", 10, finalState.longestStreak)
    }
    
    @Test
    fun `Property 32 - Streak calculation - same day completions maintain streak`() = runTest {
        // Initialize with a streak
        val baseTime = System.currentTimeMillis()
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 5,
            longestStreak = 5,
            lastCompletionDate = baseTime,
            totalTasksCompleted = 5
        )
        repository.insertGardenState(initialState)
        
        // Complete multiple tasks on the same day
        for (hour in 1..5) {
            val completionTime = baseTime + TimeUnit.HOURS.toMillis(hour.toLong())
            val (streakIncremented, newStreak) = updateStreakUseCase(completionTime)
            
            // Same day should not increment streak
            assertFalse("Same day completion should not increment streak", streakIncremented)
            assertEquals("Streak should remain at 5", 5, newStreak)
        }
        
        // Verify state
        val finalState = repository.getGardenStateOnce()
        assertNotNull("Final state should not be null", finalState)
        assertEquals("Streak should still be 5", 5, finalState!!.currentStreak)
    }
    
    @Test
    fun `Property 32 - Streak calculation - skipped day resets streak to 1`() = runTest {
        // Initialize with a streak
        val baseTime = System.currentTimeMillis()
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 7,
            longestStreak = 10,
            lastCompletionDate = baseTime,
            totalTasksCompleted = 7
        )
        repository.insertGardenState(initialState)
        
        // Skip 2 days and complete a task
        val completionTime = baseTime + TimeUnit.DAYS.toMillis(3)
        val (streakIncremented, newStreak) = updateStreakUseCase(completionTime)
        
        // Assert
        assertTrue("Streak should be reset (incremented from 0 to 1)", streakIncremented)
        assertEquals("Streak should reset to 1 after skipping days", 1, newStreak)
        
        // Verify state
        val finalState = repository.getGardenStateOnce()
        assertNotNull("Final state should not be null", finalState)
        assertEquals("Current streak should be 1", 1, finalState!!.currentStreak)
        assertEquals("Longest streak should remain 10", 10, finalState.longestStreak)
    }
    
    @Test
    fun `Property 32 - Streak calculation - longest streak is tracked correctly`() = runTest {
        val baseTime = System.currentTimeMillis()
        
        // Initialize with empty state
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 0,
            longestStreak = 0,
            lastCompletionDate = null,
            totalTasksCompleted = 0
        )
        repository.insertGardenState(initialState)
        
        // Build a streak of 5 days
        for (day in 0 until 5) {
            val completionTime = baseTime + TimeUnit.DAYS.toMillis(day.toLong())
            updateStreakUseCase(completionTime)
        }
        
        var state = repository.getGardenStateOnce()
        assertEquals("Streak should be 5", 5, state!!.currentStreak)
        assertEquals("Longest streak should be 5", 5, state.longestStreak)
        
        // Break the streak
        val breakTime = baseTime + TimeUnit.DAYS.toMillis(7)
        updateStreakUseCase(breakTime)
        
        state = repository.getGardenStateOnce()
        assertEquals("Streak should reset to 1", 1, state!!.currentStreak)
        assertEquals("Longest streak should still be 5", 5, state.longestStreak)
        
        // Build a longer streak of 8 days
        for (day in 1 until 8) {
            val completionTime = breakTime + TimeUnit.DAYS.toMillis(day.toLong())
            updateStreakUseCase(completionTime)
        }
        
        state = repository.getGardenStateOnce()
        assertEquals("Streak should be 8", 8, state!!.currentStreak)
        assertEquals("Longest streak should be updated to 8", 8, state.longestStreak)
    }
    
    @Test
    fun `Property 34 - Streak reset - streak resets to 0 when checking after 2+ days`() = runTest {
        // Initialize with a streak
        val baseTime = System.currentTimeMillis()
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 5,
            longestStreak = 10,
            lastCompletionDate = baseTime,
            totalTasksCompleted = 5
        )
        repository.insertGardenState(initialState)
        
        // Check streak status after 3 days
        val checkTime = baseTime + TimeUnit.DAYS.toMillis(3)
        val (streakIsValid, currentStreak) = checkStreakStatusUseCase(checkTime)
        
        // Assert
        assertFalse("Streak should be invalid after 3 days", streakIsValid)
        assertEquals("Streak should be reset to 0", 0, currentStreak)
        
        // Verify state was updated
        val finalState = repository.getGardenStateOnce()
        assertNotNull("Final state should not be null", finalState)
        assertEquals("Current streak should be 0", 0, finalState!!.currentStreak)
        assertEquals("Longest streak should remain 10", 10, finalState.longestStreak)
    }
    
    @Test
    fun `Property 34 - Streak reset - streak remains valid on same day`() = runTest {
        // Initialize with a streak
        val baseTime = System.currentTimeMillis()
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 5,
            longestStreak = 5,
            lastCompletionDate = baseTime,
            totalTasksCompleted = 5
        )
        repository.insertGardenState(initialState)
        
        // Check streak status on the same day
        val checkTime = baseTime + TimeUnit.HOURS.toMillis(12)
        val (streakIsValid, currentStreak) = checkStreakStatusUseCase(checkTime)
        
        // Assert
        assertTrue("Streak should be valid on same day", streakIsValid)
        assertEquals("Streak should remain 5", 5, currentStreak)
        
        // Verify state was not changed
        val finalState = repository.getGardenStateOnce()
        assertNotNull("Final state should not be null", finalState)
        assertEquals("Current streak should still be 5", 5, finalState!!.currentStreak)
    }
    
    @Test
    fun `Property 34 - Streak reset - streak remains valid on next day`() = runTest {
        // Initialize with a streak
        val baseTime = System.currentTimeMillis()
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 5,
            longestStreak = 5,
            lastCompletionDate = baseTime,
            totalTasksCompleted = 5
        )
        repository.insertGardenState(initialState)
        
        // Check streak status on the next day
        val checkTime = baseTime + TimeUnit.DAYS.toMillis(1)
        val (streakIsValid, currentStreak) = checkStreakStatusUseCase(checkTime)
        
        // Assert
        assertTrue("Streak should be valid on next day", streakIsValid)
        assertEquals("Streak should remain 5", 5, currentStreak)
        
        // Verify state was not changed
        val finalState = repository.getGardenStateOnce()
        assertNotNull("Final state should not be null", finalState)
        assertEquals("Current streak should still be 5", 5, finalState!!.currentStreak)
    }
    
    @Test
    fun `Property 32 and 34 - Random streak sequences behave correctly`() = runTest {
        // Test 20 random streak sequences
        repeat(20) {
            database.clearAllTables()
            
            // Initialize with empty state
            val initialState = GardenState(
                theme = GardenTheme.ZEN,
                growthStage = 0,
                currentStreak = 0,
                longestStreak = 0,
                lastCompletionDate = null,
                totalTasksCompleted = 0
            )
            repository.insertGardenState(initialState)
            
            val baseTime = System.currentTimeMillis()
            var expectedStreak = 0
            var maxStreak = 0
            var lastDay = -1
            
            // Generate random completion pattern over 30 days
            val completionDays = (0 until 30).filter { (0..100).random() > 30 }.sorted()
            
            completionDays.forEach { day ->
                val completionTime = baseTime + TimeUnit.DAYS.toMillis(day.toLong())
                val (_, newStreak) = updateStreakUseCase(completionTime)
                
                // Calculate expected streak
                expectedStreak = when {
                    lastDay == -1 -> 1 // First completion
                    day == lastDay -> expectedStreak // Same day
                    day == lastDay + 1 -> expectedStreak + 1 // Consecutive day
                    else -> 1 // Gap - reset
                }
                
                maxStreak = maxOf(maxStreak, expectedStreak)
                lastDay = day
                
                // Verify streak matches expectation
                assertEquals(
                    "Streak should match expected value for day $day",
                    expectedStreak,
                    newStreak
                )
            }
            
            // Verify final state
            val finalState = repository.getGardenStateOnce()
            assertNotNull("Final state should not be null", finalState)
            assertEquals("Final streak should match expected", expectedStreak, finalState!!.currentStreak)
            assertEquals("Longest streak should match max", maxStreak, finalState.longestStreak)
        }
    }
    
    @Test
    fun `Property 32 - Streak calculation - handles edge case of exactly 24 hours`() = runTest {
        // Initialize with a streak
        val baseTime = System.currentTimeMillis()
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 3,
            longestStreak = 3,
            lastCompletionDate = baseTime,
            totalTasksCompleted = 3
        )
        repository.insertGardenState(initialState)
        
        // Complete task exactly 24 hours later
        val completionTime = baseTime + TimeUnit.HOURS.toMillis(24)
        val (streakIncremented, newStreak) = updateStreakUseCase(completionTime)
        
        // Should be treated as next day and increment
        assertTrue("Streak should increment after 24 hours", streakIncremented)
        assertEquals("Streak should be 4", 4, newStreak)
    }
    
    @Test
    fun `Property 34 - Streak reset - handles edge case of exactly 48 hours`() = runTest {
        // Initialize with a streak
        val baseTime = System.currentTimeMillis()
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 5,
            longestStreak = 5,
            lastCompletionDate = baseTime,
            totalTasksCompleted = 5
        )
        repository.insertGardenState(initialState)
        
        // Check streak status exactly 48 hours later
        val checkTime = baseTime + TimeUnit.HOURS.toMillis(48)
        val (streakIsValid, currentStreak) = checkStreakStatusUseCase(checkTime)
        
        // Should be invalid and reset
        assertFalse("Streak should be invalid after 48 hours", streakIsValid)
        assertEquals("Streak should be reset to 0", 0, currentStreak)
    }
    
    @Test
    fun `GetStreakInfoUseCase returns correct information`() = runTest {
        // Initialize with specific streak data
        val baseTime = System.currentTimeMillis()
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 7,
            longestStreak = 15,
            lastCompletionDate = baseTime,
            totalTasksCompleted = 20
        )
        repository.insertGardenState(initialState)
        
        // Get streak info
        val streakInfo = getStreakInfoUseCase()
        
        // Assert
        assertEquals("Current streak should be 7", 7, streakInfo.currentStreak)
        assertEquals("Longest streak should be 15", 15, streakInfo.longestStreak)
        assertEquals("Last completion date should match", baseTime, streakInfo.lastCompletionDate)
    }
    
    @Test
    fun `GetStreakInfoUseCase handles null state`() = runTest {
        // Don't initialize any state
        
        // Get streak info
        val streakInfo = getStreakInfoUseCase()
        
        // Assert
        assertEquals("Current streak should be 0", 0, streakInfo.currentStreak)
        assertEquals("Longest streak should be 0", 0, streakInfo.longestStreak)
        assertNull("Last completion date should be null", streakInfo.lastCompletionDate)
    }
}
