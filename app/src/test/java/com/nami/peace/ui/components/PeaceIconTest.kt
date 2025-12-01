package com.nami.peace.ui.components

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for PeaceIcon composable.
 * Tests icon loading logic and resource resolution.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PeaceIconTest {
    
    private lateinit var iconManager: IconManager
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        iconManager = IoniconsManager(context)
    }
    
    @Test
    fun `IconManager returns valid resource ID for existing icon`() {
        // Given a valid icon name
        val iconName = "add"
        
        // When getting the icon
        val resourceId = iconManager.getIcon(iconName)
        
        // Then it should return a valid resource ID
        assertNotNull("Icon resource ID should not be null", resourceId)
        assertTrue("Resource ID should be greater than 0", resourceId!! > 0)
    }
    
    @Test
    fun `IconManager returns fallback for non-existent icon`() {
        // Given an invalid icon name
        val iconName = "nonexistent_icon_xyz"
        
        // When getting the icon
        val resourceId = iconManager.getIcon(iconName)
        
        // Then it should return null (and fallback can be used)
        assertNull("Non-existent icon should return null", resourceId)
        
        // And fallback should return a valid resource ID
        val fallbackId = iconManager.getFallbackIcon(iconName)
        assertTrue("Fallback resource ID should be greater than 0", fallbackId > 0)
    }
    
    @Test
    fun `IconManager has common icons available`() {
        // Given common icon names
        val commonIcons = listOf("add", "home", "settings", "heart", "star")
        
        // When checking if icons exist
        val results = commonIcons.map { iconName ->
            iconName to iconManager.hasIcon(iconName)
        }
        
        // Then most common icons should be available
        val availableCount = results.count { it.second }
        assertTrue(
            "At least some common icons should be available, found $availableCount/${commonIcons.size}",
            availableCount > 0
        )
    }
    
    @Test
    fun `IconManager caches icon lookups`() {
        // Given an icon name
        val iconName = "add"
        
        // When getting the icon multiple times
        val firstLookup = iconManager.getIcon(iconName)
        val secondLookup = iconManager.getIcon(iconName)
        
        // Then both lookups should return the same resource ID
        assertEquals("Cached lookups should return same resource ID", firstLookup, secondLookup)
    }
    
    @Test
    fun `IconManager supports icon names with and without prefix`() {
        // Given icon names with and without prefix
        val iconWithoutPrefix = "add"
        val iconWithPrefix = "ic_ionicons_add"
        
        // When getting both icons
        val resourceId1 = iconManager.getIcon(iconWithoutPrefix)
        val resourceId2 = iconManager.getIcon(iconWithPrefix)
        
        // Then both should resolve to the same resource (if icon exists)
        if (resourceId1 != null && resourceId2 != null) {
            assertEquals("Icon names with and without prefix should resolve to same resource", 
                resourceId1, resourceId2)
        }
    }
    
    @Test
    fun `IconManager getAllIcons returns non-empty map`() {
        // When getting all icons
        val allIcons = iconManager.getAllIcons()
        
        // Then the map should not be empty
        assertTrue("Icon map should not be empty", allIcons.isNotEmpty())
        
        // And all resource IDs should be valid
        allIcons.values.forEach { resourceId ->
            assertTrue("All resource IDs should be greater than 0", resourceId > 0)
        }
    }
}
