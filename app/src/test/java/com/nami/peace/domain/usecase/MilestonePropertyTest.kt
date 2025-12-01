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
 * Property-based tests for milestone detection.
 * 
 * **Feature: peace-app-enhancement, Property 33: Milestone detection**
 * **Validates: Requirements 18.6**
 * 
 * Property 33: For any streak reaching a milestone value (7, 30, 100, 365),
 * an achievement notification should be displayed.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MilestonePropertyTest {
    
    private lateinit var database: AppDatabase
    private lateinit var gardenDao: GardenDao
    private lateinit var repository: GardenRepository
    private lateinit var updateStreakUseCase: UpdateStreakUseCase
    private lateinit var checkMilestoneUseCase: CheckMilestoneUseCase
    
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
        checkMilestoneUseCase = CheckMilestoneUseCase(repository)
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun `Property 33 - Milestone detection - detects 7 day milestone`() = runTest {
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
        
        // Complete tasks for 7 consecutive days
        for (day in 0 until 7) {
            val completionTime = baseTime + TimeUnit.DAYS.toMillis(day.toLong())
            updateStreakUseCase(completionTime)
            
            // Check milestone after each completion
            val milestone = checkMilestoneUseCase()
            
            if (day == 6) {
                // On day 7, milestone should be detected
                assertNotNull("Milestone should be detected on day 7", milestone)
                assertEquals("Milestone should be 7", 7, milestone)
            } else {
                // Before day 7, no milestone
                assertNull("No milestone should be detected before day 7", milestone)
            }
        }
    }
    
    @Test
    fun `Property 33 - Milestone detection - detects 30 day milestone`() = runTest {
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
        
        // Complete tasks for 30 consecutive days
        for (day in 0 until 30) {
            val completionTime = baseTime + TimeUnit.DAYS.toMillis(day.toLong())
            updateStreakUseCase(completionTime)
            
            // Check milestone after each completion
            val milestone = checkMilestoneUseCase()
            
            when (day) {
                6 -> {
                    // Day 7 milestone
                    assertNotNull("Milestone should be detected on day 7", milestone)
                    assertEquals("Milestone should be 7", 7, milestone)
                }
                29 -> {
                    // Day 30 milestone
                    assertNotNull("Milestone should be detected on day 30", milestone)
                    assertEquals("Milestone should be 30", 30, milestone)
                }
                else -> {
                    // No milestone on other days
                    if (day != 6 && day != 29) {
                        assertNull("No milestone should be detected on day ${day + 1}", milestone)
                    }
                }
            }
        }
    }
    
    @Test
    fun `Property 33 - Milestone detection - detects 100 day milestone`() = runTest {
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
        
        // Complete tasks for 100 consecutive days
        for (day in 0 until 100) {
            val completionTime = baseTime + TimeUnit.DAYS.toMillis(day.toLong())
            updateStreakUseCase(completionTime)
        }
        
        // Check milestone
        val milestone = checkMilestoneUseCase()
        assertNotNull("Milestone should be detected on day 100", milestone)
        assertEquals("Milestone should be 100", 100, milestone)
    }
    
    @Test
    fun `Property 33 - Milestone detection - detects 365 day milestone`() = runTest {
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
        
        // Complete tasks for 365 consecutive days
        for (day in 0 until 365) {
            val completionTime = baseTime + TimeUnit.DAYS.toMillis(day.toLong())
            updateStreakUseCase(completionTime)
        }
        
        // Check milestone
        val milestone = checkMilestoneUseCase()
        assertNotNull("Milestone should be detected on day 365", milestone)
        assertEquals("Milestone should be 365", 365, milestone)
    }
    
    @Test
    fun `Property 33 - Milestone detection - no milestone detected for non-milestone streaks`() = runTest {
        // Test various non-milestone streak values
        val nonMilestoneStreaks = listOf(1, 2, 3, 4, 5, 6, 8, 9, 10, 15, 20, 25, 29, 31, 50, 99, 101, 200, 364, 366)
        
        nonMilestoneStreaks.forEach { streakValue ->
            database.clearAllTables()
            
            // Initialize with specific streak
            val initialState = GardenState(
                theme = GardenTheme.ZEN,
                growthStage = 0,
                currentStreak = streakValue,
                longestStreak = streakValue,
                lastCompletionDate = System.currentTimeMillis(),
                totalTasksCompleted = streakValue
            )
            repository.insertGardenState(initialState)
            
            // Check milestone
            val milestone = checkMilestoneUseCase()
            assertNull("No milestone should be detected for streak $streakValue", milestone)
        }
    }
    
    @Test
    fun `Property 33 - Milestone detection - all milestones are detected correctly`() = runTest {
        val milestones = listOf(7, 30, 100, 365)
        
        milestones.forEach { milestoneValue ->
            database.clearAllTables()
            
            // Initialize with specific streak
            val initialState = GardenState(
                theme = GardenTheme.ZEN,
                growthStage = 0,
                currentStreak = milestoneValue,
                longestStreak = milestoneValue,
                lastCompletionDate = System.currentTimeMillis(),
                totalTasksCompleted = milestoneValue
            )
            repository.insertGardenState(initialState)
            
            // Check milestone
            val milestone = checkMilestoneUseCase()
            assertNotNull("Milestone should be detected for streak $milestoneValue", milestone)
            assertEquals("Detected milestone should match streak value", milestoneValue, milestone)
        }
    }
    
    @Test
    fun `Property 33 - Milestone detection - getNextMilestone returns correct value`() = runTest {
        // Test next milestone for various current streaks
        val testCases = mapOf(
            0 to 7,
            1 to 7,
            6 to 7,
            7 to 30,
            8 to 30,
            29 to 30,
            30 to 100,
            50 to 100,
            99 to 100,
            100 to 365,
            200 to 365,
            364 to 365,
            365 to null, // All milestones achieved
            400 to null  // Beyond all milestones
        )
        
        testCases.forEach { (currentStreak, expectedNext) ->
            database.clearAllTables()
            
            // Initialize with specific streak
            val initialState = GardenState(
                theme = GardenTheme.ZEN,
                growthStage = 0,
                currentStreak = currentStreak,
                longestStreak = currentStreak,
                lastCompletionDate = System.currentTimeMillis(),
                totalTasksCompleted = currentStreak
            )
            repository.insertGardenState(initialState)
            
            // Get next milestone
            val nextMilestone = checkMilestoneUseCase.getNextMilestone()
            assertEquals(
                "Next milestone for streak $currentStreak should be $expectedNext",
                expectedNext,
                nextMilestone
            )
        }
    }
    
    @Test
    fun `Property 33 - Milestone detection - getAchievedMilestones returns correct list`() = runTest {
        // Test achieved milestones for various longest streaks
        val testCases = mapOf(
            0 to emptyList<Int>(),
            6 to emptyList<Int>(),
            7 to listOf(7),
            29 to listOf(7),
            30 to listOf(7, 30),
            99 to listOf(7, 30),
            100 to listOf(7, 30, 100),
            364 to listOf(7, 30, 100),
            365 to listOf(7, 30, 100, 365),
            400 to listOf(7, 30, 100, 365)
        )
        
        testCases.forEach { (longestStreak, expectedAchieved) ->
            database.clearAllTables()
            
            // Initialize with specific longest streak
            val initialState = GardenState(
                theme = GardenTheme.ZEN,
                growthStage = 0,
                currentStreak = 0, // Current streak doesn't matter for achieved milestones
                longestStreak = longestStreak,
                lastCompletionDate = System.currentTimeMillis(),
                totalTasksCompleted = longestStreak
            )
            repository.insertGardenState(initialState)
            
            // Get achieved milestones
            val achievedMilestones = checkMilestoneUseCase.getAchievedMilestones()
            assertEquals(
                "Achieved milestones for longest streak $longestStreak should be $expectedAchieved",
                expectedAchieved,
                achievedMilestones
            )
        }
    }
    
    @Test
    fun `Property 33 - Milestone detection - milestone detected only at exact streak value`() = runTest {
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
        
        // Build up to 10 days
        for (day in 0 until 10) {
            val completionTime = baseTime + TimeUnit.DAYS.toMillis(day.toLong())
            updateStreakUseCase(completionTime)
            
            val milestone = checkMilestoneUseCase()
            
            // Milestone should only be detected on day 7
            if (day == 6) { // Day 7 (0-indexed)
                assertNotNull("Milestone should be detected on day 7", milestone)
                assertEquals("Milestone should be 7", 7, milestone)
            } else {
                assertNull("No milestone should be detected on day ${day + 1}", milestone)
            }
        }
    }
    
    @Test
    fun `Property 33 - Milestone detection - handles null garden state`() = runTest {
        // Don't initialize any state
        
        // Check milestone
        val milestone = checkMilestoneUseCase()
        assertNull("No milestone should be detected with null state", milestone)
        
        // Get next milestone
        val nextMilestone = checkMilestoneUseCase.getNextMilestone()
        assertEquals("Next milestone should be 7 with null state", 7, nextMilestone)
        
        // Get achieved milestones
        val achievedMilestones = checkMilestoneUseCase.getAchievedMilestones()
        assertTrue("Achieved milestones should be empty with null state", achievedMilestones.isEmpty())
    }
    
    @Test
    fun `Property 33 - Milestone detection - milestone not detected after streak break`() = runTest {
        // Initialize with a 7-day streak
        val baseTime = System.currentTimeMillis()
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            currentStreak = 7,
            longestStreak = 7,
            lastCompletionDate = baseTime,
            totalTasksCompleted = 7
        )
        repository.insertGardenState(initialState)
        
        // Break the streak by skipping 3 days
        val breakTime = baseTime + TimeUnit.DAYS.toMillis(4)
        updateStreakUseCase(breakTime)
        
        // Current streak should be reset to 1
        val state = repository.getGardenStateOnce()
        assertEquals("Streak should be reset to 1", 1, state!!.currentStreak)
        
        // No milestone should be detected
        val milestone = checkMilestoneUseCase()
        assertNull("No milestone should be detected after streak break", milestone)
    }
    
    @Test
    fun `Property 33 - Milestone detection - random streak sequences detect milestones correctly`() = runTest {
        // Test 10 random sequences
        repeat(10) {
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
            val milestones = listOf(7, 30, 100, 365)
            val detectedMilestones = mutableListOf<Int>()
            
            // Build a random streak up to 50 days
            val targetDays = (10..50).random()
            for (day in 0 until targetDays) {
                val completionTime = baseTime + TimeUnit.DAYS.toMillis(day.toLong())
                updateStreakUseCase(completionTime)
                
                val milestone = checkMilestoneUseCase()
                if (milestone != null) {
                    detectedMilestones.add(milestone)
                }
            }
            
            // Verify detected milestones match expected
            val expectedMilestones = milestones.filter { it <= targetDays }
            assertEquals(
                "Detected milestones should match expected for $targetDays days",
                expectedMilestones,
                detectedMilestones
            )
        }
    }
}
