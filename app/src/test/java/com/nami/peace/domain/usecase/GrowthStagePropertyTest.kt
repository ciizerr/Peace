package com.nami.peace.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.GardenDao
import com.nami.peace.data.local.GardenTheme
import com.nami.peace.data.repository.GardenRepositoryImpl
import com.nami.peace.domain.model.GardenState
import com.nami.peace.domain.model.GrowthStage
import com.nami.peace.domain.repository.GardenRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

/**
 * Property-based tests for growth stage advancement.
 * 
 * **Feature: peace-app-enhancement, Property 31: Growth stage advancement**
 * **Validates: Requirements 18.3**
 * 
 * Property: For any task completion, if the completion count reaches a growth stage threshold,
 * the Peace Garden should advance to the next stage.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GrowthStagePropertyTest {
    
    private lateinit var database: AppDatabase
    private lateinit var gardenDao: GardenDao
    private lateinit var repository: GardenRepository
    private lateinit var advanceGrowthStageUseCase: AdvanceGrowthStageUseCase
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        gardenDao = database.gardenDao()
        repository = GardenRepositoryImpl(gardenDao, org.mockito.Mockito.mock(com.nami.peace.widget.WidgetUpdateManager::class.java))
        advanceGrowthStageUseCase = AdvanceGrowthStageUseCase(repository)
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun `Property 31 - Growth stage advancement - stage advances when threshold is reached`() = runTest {
        // Test all growth stage thresholds
        val thresholds = GrowthStage.values().map { it.tasksRequired }.filter { it > 0 }
        
        thresholds.forEach { threshold ->
            // Test task counts around each threshold
            for (offset in -2..1) {
                val initialCount = (threshold + offset).coerceAtLeast(0)
                
                // Reset database for each test
                database.clearAllTables()
                
                // Set up initial state
                val initialStage = GrowthStage.fromTaskCount(initialCount)
                val initialState = GardenState(
                    theme = GardenTheme.ZEN,
                    growthStage = initialStage.stage,
                    totalTasksCompleted = initialCount
                )
                repository.insertGardenState(initialState)
                
                // Act - advance the growth stage
                val (stageAdvanced, newStage) = advanceGrowthStageUseCase()
                
                // Assert
                val newTaskCount = initialCount + 1
                val expectedNewStage = GrowthStage.fromTaskCount(newTaskCount)
                val expectedAdvancement = expectedNewStage.stage > initialStage.stage
                
                assertEquals(
                    "Stage advancement should match expectation for count $initialCount -> $newTaskCount",
                    expectedAdvancement,
                    stageAdvanced
                )
                
                if (expectedAdvancement) {
                    assertNotNull("New stage should not be null when advancing", newStage)
                    assertEquals("New stage should match expected stage", expectedNewStage, newStage)
                } else {
                    assertNull("New stage should be null when not advancing", newStage)
                }
                
                // Verify the state was updated correctly
                val updatedState = repository.getGardenStateOnce()
                assertNotNull("Updated state should not be null", updatedState)
                assertEquals("Task count should be incremented", newTaskCount, updatedState!!.totalTasksCompleted)
                assertEquals("Growth stage should match expected", expectedNewStage.stage, updatedState.growthStage)
            }
        }
    }
    
    @Test
    fun `Property 31 - Growth stage advancement - correct stage calculation for all task counts`() {
        // Test 100 random task counts
        repeat(100) {
            val taskCount = (0..1000).random()
            
            // Test that GrowthStage.fromTaskCount always returns the correct stage
            val stage = GrowthStage.fromTaskCount(taskCount)
            
            // The stage should be the highest stage whose tasksRequired <= taskCount
            val expectedStage = GrowthStage.values()
                .lastOrNull { it.tasksRequired <= taskCount }
                ?: GrowthStage.SEED
            
            assertEquals("Stage should match expected for task count $taskCount", expectedStage, stage)
            
            // Verify stage is within valid range
            assertTrue("Stage should be in valid range 0-9", stage.stage in 0..9)
        }
    }
    
    @Test
    fun `Property 31 - Growth stage advancement - progress calculation is accurate`() {
        // Test 100 random task counts
        repeat(100) {
            val taskCount = (0..1000).random()
            val progress = GrowthStage.calculateProgressToNextStage(taskCount)
            
            // Progress should always be between 0 and 100
            assertTrue("Progress should be between 0 and 100", progress in 0..100)
            
            // If at max stage, progress should be 100
            val currentStage = GrowthStage.fromTaskCount(taskCount)
            if (currentStage == GrowthStage.TRANSCENDENT) {
                assertEquals("Progress should be 100 at max stage", 100, progress)
            }
        }
    }
    
    @Test
    fun `Property 31 - Growth stage advancement - stages are ordered correctly`() {
        // Verify that growth stages are in ascending order by tasksRequired
        val stages = GrowthStage.values()
        
        for (i in 0 until stages.size - 1) {
            val current = stages[i]
            val next = stages[i + 1]
            
            // Each stage should require more tasks than the previous
            assertTrue(
                "Stage ${next.displayName} should require more tasks than ${current.displayName}",
                next.tasksRequired > current.tasksRequired
            )
            
            // Stage numbers should be sequential
            assertEquals("Stage numbers should be sequential", current.stage + 1, next.stage)
        }
    }
    
    @Test
    fun `Property 31 - Growth stage advancement - all 10 stages are defined`() {
        val stages = GrowthStage.values()
        
        // Verify we have exactly 10 stages
        assertEquals("Should have exactly 10 growth stages", 10, stages.size)
        
        // Verify stages are numbered 0-9
        stages.forEachIndexed { index, stage ->
            assertEquals("Stage $index should have stage number $index", index, stage.stage)
        }
        
        // Verify first stage starts at 0 tasks
        assertEquals("First stage should start at 0 tasks", 0, GrowthStage.SEED.tasksRequired)
        
        // Verify last stage is TRANSCENDENT
        assertEquals("Last stage should be TRANSCENDENT", GrowthStage.TRANSCENDENT, stages.last())
    }
    
    @Test
    fun `Property 31 - Growth stage advancement - sequential completions advance through all stages`() = runTest {
        // Initialize with empty state
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 0,
            totalTasksCompleted = 0
        )
        repository.insertGardenState(initialState)
        
        var currentStageNumber = 0
        var advancementCount = 0
        
        // Complete tasks up to TRANSCENDENT threshold
        for (taskNum in 1..GrowthStage.TRANSCENDENT.tasksRequired) {
            val (stageAdvanced, newStage) = advanceGrowthStageUseCase()
            
            if (stageAdvanced) {
                advancementCount++
                assertNotNull("New stage should not be null when advancing", newStage)
                assertTrue("Stage should advance forward", newStage!!.stage > currentStageNumber)
                currentStageNumber = newStage.stage
            }
        }
        
        // Verify we advanced through all stages (0 to 9 = 9 advancements)
        assertEquals("Should advance through all 9 stage transitions", 9, advancementCount)
        
        // Verify we're at the final stage
        val finalState = repository.getGardenStateOnce()
        assertNotNull("Final state should not be null", finalState)
        assertEquals("Should be at TRANSCENDENT stage", GrowthStage.TRANSCENDENT.stage, finalState!!.growthStage)
    }
    
    @Test
    fun `Property 31 - Growth stage advancement - stage advancement preserves other garden state`() = runTest {
        // Set up initial state with specific values
        val initialState = GardenState(
            theme = GardenTheme.FOREST,
            growthStage = 0,
            currentStreak = 10,
            longestStreak = 15,
            lastCompletionDate = System.currentTimeMillis(),
            totalTasksCompleted = 0
        )
        repository.insertGardenState(initialState)
        
        // Advance the growth stage
        advanceGrowthStageUseCase()
        
        // Verify other state was preserved
        val updatedState = repository.getGardenStateOnce()
        assertNotNull("Updated state should not be null", updatedState)
        assertEquals("Theme should be preserved", GardenTheme.FOREST, updatedState!!.theme)
        assertEquals("Current streak should be preserved", 10, updatedState.currentStreak)
        assertEquals("Longest streak should be preserved", 15, updatedState.longestStreak)
        assertEquals("Last completion date should be preserved", initialState.lastCompletionDate, updatedState.lastCompletionDate)
    }
}
