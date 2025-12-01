package com.nami.peace.util.feature

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID
import org.junit.Assert.*

/**
 * Unit tests for FeatureToggleManager
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class FeatureToggleManagerTest {
    
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var testScope: TestScope
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var featureToggleManager: FeatureToggleManager
    private lateinit var testDir: File
    
    @Before
    fun setup() {
        val testId = "${System.currentTimeMillis()}_${UUID.randomUUID()}"
        val dispatcher = UnconfinedTestDispatcher()
        testScope = TestScope(dispatcher)
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        testDir = File(context.cacheDir, "test_feature_toggle_unit_$testId")
        
        // Clean up any existing directory first
        if (testDir.exists()) {
            testDir.deleteRecursively()
        }
        testDir.mkdirs()
        
        val testFile = File(testDir, "test.preferences_pb")
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        
        userPreferencesRepository = UserPreferencesRepository(testDataStore)
        featureToggleManager = FeatureToggleManager(userPreferencesRepository)
    }
    
    @After
    fun teardown() {
        runBlocking {
            // Cancel the scope and wait for completion
            testScope.cancel()
            delay(100)
        }
        
        // Try multiple times to delete with delays (Windows file locking)
        repeat(3) { attempt ->
            try {
                if (testDir.exists()) {
                    testDir.deleteRecursively()
                }
                return // Success
            } catch (e: Exception) {
                if (attempt < 2) {
                    Thread.sleep(200)
                } else {
                    // Final attempt failed, just log it
                    System.err.println("Warning: Could not delete test directory after 3 attempts: ${e.message}")
                }
            }
        }
    }
    
    @Ignore("Windows file locking issue with DataStore - to be refactored with in-memory implementation")
    @Test
    fun `setFeatureEnabled should update feature state`() = testScope.runTest {
        // Enable a feature
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.SUBTASKS, true)
        
        // Verify it's enabled
        assertTrue(featureToggleManager.isFeatureEnabled(FeatureToggleManager.Feature.SUBTASKS))
        
        // Disable the feature
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.SUBTASKS, false)
        
        // Verify it's disabled
        assertFalse(featureToggleManager.isFeatureEnabled(FeatureToggleManager.Feature.SUBTASKS))
    }
    
    @Ignore("Windows file locking issue with DataStore - to be refactored with in-memory implementation")
    @Test
    fun `getFeatureFlow should return Flow of feature state`() = testScope.runTest {
        // Set a feature state
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.ATTACHMENTS, true)
        
        // Get the Flow and verify
        val flow = featureToggleManager.getFeatureFlow(FeatureToggleManager.Feature.ATTACHMENTS)
        assertTrue(flow.first())
        
        // Change the state
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.ATTACHMENTS, false)
        
        // Verify Flow reflects the change
        assertFalse(flow.first())
    }
    
    @Ignore("Windows file locking issue with DataStore - to be refactored with in-memory implementation")
    @Test
    fun `getAllFeatureStates should return all feature states`() = testScope.runTest {
        // Set some features
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.SUBTASKS, true)
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.ATTACHMENTS, false)
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.WIDGETS, true)
        
        // Get all states
        val allStates = featureToggleManager.getAllFeatureStates()
        
        // Verify
        assertEquals(5, allStates.size) // Should have all 5 features
        assertTrue(allStates[FeatureToggleManager.Feature.SUBTASKS] == true)
        assertTrue(allStates[FeatureToggleManager.Feature.ATTACHMENTS] == false)
        assertTrue(allStates[FeatureToggleManager.Feature.WIDGETS] == true)
    }
    
    @Ignore("Windows file locking issue with DataStore - to be refactored with in-memory implementation")
    @Test
    fun `resetToDefaults should set all features to default states`() = testScope.runTest {
        // Set all features to non-default states
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.SUBTASKS, false)
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.ATTACHMENTS, false)
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.WIDGETS, false)
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.ML_SUGGESTIONS, false)
        featureToggleManager.setFeatureEnabled(FeatureToggleManager.Feature.CALENDAR_SYNC, true)
        
        // Reset to defaults
        featureToggleManager.resetToDefaults()
        
        // Verify defaults
        assertTrue(featureToggleManager.isFeatureEnabled(FeatureToggleManager.Feature.SUBTASKS))
        assertTrue(featureToggleManager.isFeatureEnabled(FeatureToggleManager.Feature.ATTACHMENTS))
        assertTrue(featureToggleManager.isFeatureEnabled(FeatureToggleManager.Feature.WIDGETS))
        assertTrue(featureToggleManager.isFeatureEnabled(FeatureToggleManager.Feature.ML_SUGGESTIONS))
        assertFalse(featureToggleManager.isFeatureEnabled(FeatureToggleManager.Feature.CALENDAR_SYNC))
    }
    
    @Test
    fun `Feature enum should contain all expected features`() {
        val features = FeatureToggleManager.Feature.values()
        
        assertEquals(5, features.size)
        assertTrue(features.contains(FeatureToggleManager.Feature.SUBTASKS))
        assertTrue(features.contains(FeatureToggleManager.Feature.ATTACHMENTS))
        assertTrue(features.contains(FeatureToggleManager.Feature.WIDGETS))
        assertTrue(features.contains(FeatureToggleManager.Feature.ML_SUGGESTIONS))
        assertTrue(features.contains(FeatureToggleManager.Feature.CALENDAR_SYNC))
    }
}
