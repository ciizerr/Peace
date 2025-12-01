package com.nami.peace.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.GardenDao
import com.nami.peace.data.local.GardenTheme
import com.nami.peace.data.repository.GardenRepositoryImpl
import com.nami.peace.domain.model.GardenState
import com.nami.peace.domain.repository.GardenRepository
import com.nami.peace.ui.theme.getGardenThemeConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random
import org.junit.Assert.*

/**
 * Feature: peace-app-enhancement
 * Property 30: Garden theme application
 * 
 * Validates: Requirements 18.2
 * 
 * Property: For any garden theme selection, the Peace Garden should immediately 
 * display theme-specific icons and colors.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GardenThemePropertyTest {
    
    private lateinit var database: AppDatabase
    private lateinit var gardenDao: GardenDao
    private lateinit var repository: GardenRepository
    private lateinit var updateGardenThemeUseCase: UpdateGardenThemeUseCase
    private lateinit var getGardenThemeConfigUseCase: GetGardenThemeConfigUseCase
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        gardenDao = database.gardenDao()
        repository = GardenRepositoryImpl(gardenDao, org.mockito.Mockito.mock(com.nami.peace.widget.WidgetUpdateManager::class.java))
        updateGardenThemeUseCase = UpdateGardenThemeUseCase(repository)
        getGardenThemeConfigUseCase = GetGardenThemeConfigUseCase(repository)
        
        // Initialize garden state with default values
        runBlocking {
            val initialState = GardenState(
                theme = GardenTheme.ZEN,
                growthStage = 0,
                currentStreak = 0,
                longestStreak = 0,
                lastCompletionDate = null,
                totalTasksCompleted = 0
            )
            repository.insertGardenState(initialState)
        }
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun `Property 30 - Garden theme application - for any theme selection, theme-specific config should be applied`() = runTest {
        // Run 100 iterations with random theme selections
        repeat(100) {
            val randomTheme = GardenTheme.values().random()
            
            // Update the garden theme
            updateGardenThemeUseCase(randomTheme)
            
            // Get the theme configuration
            val themeConfig = getGardenThemeConfigUseCase().first()
            
            // Verify the theme was applied
            assertNotNull("Theme config should not be null", themeConfig)
            assertEquals("Theme should match the selected theme", randomTheme, themeConfig!!.theme)
            
            // Verify theme-specific colors are present
            assertNotNull("Theme colors should not be null", themeConfig.colors)
            
            // Verify theme-specific icons are present
            assertNotNull("Theme icons should not be null", themeConfig.icons)
            
            // Verify the theme has a display name
            assertTrue("Theme display name should not be empty", themeConfig.displayName.isNotEmpty())
            
            // Verify the theme has a description
            assertTrue("Theme description should not be empty", themeConfig.description.isNotEmpty())
        }
    }
    
    @Test
    fun `Property 30 - Garden theme application - theme change should preserve other garden state`() = runTest {
        // Set up initial garden state with specific values
        val initialState = GardenState(
            theme = GardenTheme.ZEN,
            growthStage = 5,
            currentStreak = 10,
            longestStreak = 15,
            lastCompletionDate = System.currentTimeMillis(),
            totalTasksCompleted = 50
        )
        repository.updateGardenState(initialState)
        
        // Run 50 iterations with random theme changes
        repeat(50) {
            val randomTheme = GardenTheme.values().random()
            
            // Update the garden theme
            updateGardenThemeUseCase(randomTheme)
            
            // Get the updated state
            val updatedState = repository.getGardenStateOnce()
            
            // Verify the theme was changed
            assertNotNull("Garden state should not be null", updatedState)
            assertEquals("Theme should be updated", randomTheme, updatedState!!.theme)
            
            // Verify other state was preserved
            assertEquals("Growth stage should be preserved", initialState.growthStage, updatedState.growthStage)
            assertEquals("Current streak should be preserved", initialState.currentStreak, updatedState.currentStreak)
            assertEquals("Longest streak should be preserved", initialState.longestStreak, updatedState.longestStreak)
            assertEquals("Last completion date should be preserved", initialState.lastCompletionDate, updatedState.lastCompletionDate)
            assertEquals("Total tasks completed should be preserved", initialState.totalTasksCompleted, updatedState.totalTasksCompleted)
        }
    }
    
    @Test
    fun `Property 30 - Garden theme application - each theme should have unique colors`() = runTest {
        val themes = GardenTheme.values()
        val colorSets = mutableSetOf<String>()
        
        // Verify each theme has unique color configuration
        themes.forEach { theme ->
            updateGardenThemeUseCase(theme)
            val themeConfig = getGardenThemeConfigUseCase().first()
            
            assertNotNull("Theme config should not be null", themeConfig)
            
            // Create a unique identifier for the color set
            val colorId = "${themeConfig!!.colors.primary}-${themeConfig.colors.secondary}-${themeConfig.colors.accent}"
            
            // Verify this color set is unique
            assertFalse("Each theme should have unique colors", colorSets.contains(colorId))
            colorSets.add(colorId)
        }
        
        // Verify we have as many unique color sets as themes
        assertEquals("Should have unique colors for each theme", themes.size, colorSets.size)
    }
    
    @Test
    fun `Property 30 - Garden theme application - each theme should have unique icons`() = runTest {
        val themes = GardenTheme.values()
        val iconSets = mutableSetOf<String>()
        
        // Verify each theme has unique icon configuration
        themes.forEach { theme ->
            updateGardenThemeUseCase(theme)
            val themeConfig = getGardenThemeConfigUseCase().first()
            
            assertNotNull("Theme config should not be null", themeConfig)
            
            // Create a unique identifier for the icon set
            val iconId = "${themeConfig!!.icons.themeIcon}-${themeConfig.icons.growthStage0}-${themeConfig.icons.growthStage9}"
            
            // Verify this icon set is unique
            assertFalse("Each theme should have unique icons", iconSets.contains(iconId))
            iconSets.add(iconId)
        }
        
        // Verify we have as many unique icon sets as themes
        assertEquals("Should have unique icons for each theme", themes.size, iconSets.size)
    }
    
    @Test
    fun `Property 30 - Garden theme application - all growth stages should have icons`() = runTest {
        val themes = GardenTheme.values()
        
        // Verify each theme has icons for all 10 growth stages
        themes.forEach { theme ->
            updateGardenThemeUseCase(theme)
            val themeConfig = getGardenThemeConfigUseCase().first()
            
            assertNotNull("Theme config should not be null", themeConfig)
            
            // Verify all growth stage icons are defined
            assertNotNull("Growth stage 0 icon should be defined", themeConfig!!.icons.growthStage0)
            assertNotNull("Growth stage 1 icon should be defined", themeConfig.icons.growthStage1)
            assertNotNull("Growth stage 2 icon should be defined", themeConfig.icons.growthStage2)
            assertNotNull("Growth stage 3 icon should be defined", themeConfig.icons.growthStage3)
            assertNotNull("Growth stage 4 icon should be defined", themeConfig.icons.growthStage4)
            assertNotNull("Growth stage 5 icon should be defined", themeConfig.icons.growthStage5)
            assertNotNull("Growth stage 6 icon should be defined", themeConfig.icons.growthStage6)
            assertNotNull("Growth stage 7 icon should be defined", themeConfig.icons.growthStage7)
            assertNotNull("Growth stage 8 icon should be defined", themeConfig.icons.growthStage8)
            assertNotNull("Growth stage 9 icon should be defined", themeConfig.icons.growthStage9)
            
            // Verify icons are not empty strings
            assertTrue("Growth stage icons should not be empty", themeConfig.icons.growthStage0.isNotEmpty())
            assertTrue("Growth stage icons should not be empty", themeConfig.icons.growthStage9.isNotEmpty())
        }
    }
    
    @Test
    fun `Property 30 - Garden theme application - theme config should match getGardenThemeConfig function`() = runTest {
        val themes = GardenTheme.values()
        
        // Verify the use case returns the same config as the direct function
        themes.forEach { theme ->
            updateGardenThemeUseCase(theme)
            
            // Get config from use case
            val useCaseConfig = getGardenThemeConfigUseCase().first()
            
            // Get config from direct function
            val directConfig = getGardenThemeConfig(theme)
            
            // Verify they match
            assertNotNull("Use case config should not be null", useCaseConfig)
            assertEquals("Configs should match", directConfig.theme, useCaseConfig!!.theme)
            assertEquals("Display names should match", directConfig.displayName, useCaseConfig.displayName)
            assertEquals("Descriptions should match", directConfig.description, useCaseConfig.description)
            assertEquals("Colors should match", directConfig.colors, useCaseConfig.colors)
            assertEquals("Icons should match", directConfig.icons, useCaseConfig.icons)
        }
    }
    
    @Test
    fun `Property 30 - Garden theme application - rapid theme changes should all be applied`() = runTest {
        // Simulate rapid theme changes (like user quickly switching themes)
        repeat(20) {
            val theme1 = GardenTheme.values().random()
            val theme2 = GardenTheme.values().random()
            val theme3 = GardenTheme.values().random()
            
            // Rapidly change themes
            updateGardenThemeUseCase(theme1)
            updateGardenThemeUseCase(theme2)
            updateGardenThemeUseCase(theme3)
            
            // Verify the final theme is applied
            val finalConfig = getGardenThemeConfigUseCase().first()
            assertNotNull("Final config should not be null", finalConfig)
            assertEquals("Final theme should be theme3", theme3, finalConfig!!.theme)
        }
    }
}
