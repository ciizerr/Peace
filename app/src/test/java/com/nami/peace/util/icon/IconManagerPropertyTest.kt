package com.nami.peace.util.icon

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random
import org.junit.Assert.*

/**
 * Feature: peace-app-enhancement, Property 4 & 5: Ionicons usage consistency and fallback handling
 * 
 * Property 4: For any UI component that renders an icon, the icon should come from the Ionicons pack, 
 * not Material Icons.
 * 
 * Property 5: For any requested icon name that doesn't exist in Ionicons, the system should return 
 * a fallback icon without crashing.
 * 
 * Validates: Requirements 3.2, 3.4
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class IconManagerPropertyTest {
    
    private lateinit var context: Context
    private lateinit var iconManager: IconManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        iconManager = IoniconsManager(context)
    }
    
    @Test
    fun `Property 4 - Ionicons usage consistency - all loaded icons should be from Ionicons pack`() {
        // Get all available icons
        val allIcons = iconManager.getAllIcons()
        
        // Verify we have icons loaded
        assertTrue("Icon cache should not be empty", allIcons.isNotEmpty())
        
        // Run 100 iterations checking random icons
        repeat(100) {
            // Pick a random icon from the loaded icons
            val randomIconEntry = allIcons.entries.random()
            val iconName = randomIconEntry.key
            val resourceId = randomIconEntry.value
            
            // Verify the resource ID is valid (non-zero)
            assertNotEquals("Resource ID should be non-zero for icon: $iconName", 0, resourceId)
            
            // Verify the icon name follows Ionicons naming convention
            // Either starts with "ic_ionicons_" or is a short name that maps to one
            val resourceName = context.resources.getResourceEntryName(resourceId)
            assertTrue(
                "Icon should be from Ionicons pack: $resourceName",
                resourceName.startsWith("ic_ionicons_")
            )
        }
    }
    
    @Test
    fun `Property 4 - Ionicons consistency - getIcon should return Ionicons resources`() {
        // Test with known Ionicons names
        val knownIcons = listOf(
            "add", "add_circle", "alarm", "calendar", "checkmark",
            "close", "home", "settings", "trash", "heart"
        )
        
        knownIcons.forEach { iconName ->
            val resourceId = iconManager.getIcon(iconName)
            
            // Verify icon was found
            assertNotNull("Icon should be found: $iconName", resourceId)
            
            // Verify it's from Ionicons
            resourceId?.let {
                val resourceName = context.resources.getResourceEntryName(it)
                assertTrue(
                    "Icon should be from Ionicons pack: $resourceName",
                    resourceName.startsWith("ic_ionicons_")
                )
            }
        }
    }
    
    @Test
    fun `Property 5 - Icon fallback handling - missing icons should return fallback without crashing`() {
        // Generate 100 random invalid icon names
        repeat(100) {
            val invalidIconName = generateRandomInvalidIconName()
            
            // Try to get the icon (should return null)
            val resourceId = iconManager.getIcon(invalidIconName)
            
            // Should return null for missing icons
            assertNull("Missing icon should return null: $invalidIconName", resourceId)
            
            // Get fallback icon (should not crash)
            val fallbackId = iconManager.getFallbackIcon(invalidIconName)
            
            // Verify fallback is valid
            assertNotEquals("Fallback icon should have valid resource ID", 0, fallbackId)
            
            // Verify fallback is from Ionicons
            val fallbackName = context.resources.getResourceEntryName(fallbackId)
            assertTrue(
                "Fallback icon should be from Ionicons pack: $fallbackName",
                fallbackName.startsWith("ic_ionicons_")
            )
        }
    }
    
    @Test
    fun `Property 5 - Fallback consistency - fallback icon should always be the same`() {
        val invalidNames = List(50) { generateRandomInvalidIconName() }
        
        // Get fallback for first invalid name
        val firstFallback = iconManager.getFallbackIcon(invalidNames[0])
        
        // Verify all other invalid names get the same fallback
        invalidNames.drop(1).forEach { invalidName ->
            val fallback = iconManager.getFallbackIcon(invalidName)
            assertEquals(
                "All fallback icons should be the same resource",
                firstFallback,
                fallback
            )
        }
    }
    
    @Test
    fun `Property 4 - Icon caching - repeated requests should return cached results`() {
        val testIconName = "add_circle"
        
        // First request - loads from resources
        val firstResult = iconManager.getIcon(testIconName)
        assertNotNull("Icon should be found", firstResult)
        
        // Run 100 iterations requesting the same icon
        repeat(100) {
            val cachedResult = iconManager.getIcon(testIconName)
            
            // Should return the same resource ID
            assertEquals(
                "Cached icon should match original",
                firstResult,
                cachedResult
            )
        }
    }
    
    @Test
    fun `Property 5 - hasIcon should correctly identify existing and missing icons`() {
        // Test with known existing icons
        val existingIcons = listOf("add", "calendar", "home", "settings")
        existingIcons.forEach { iconName ->
            assertTrue(
                "hasIcon should return true for existing icon: $iconName",
                iconManager.hasIcon(iconName)
            )
        }
        
        // Test with random invalid icons
        repeat(50) {
            val invalidName = generateRandomInvalidIconName()
            assertFalse(
                "hasIcon should return false for missing icon: $invalidName",
                iconManager.hasIcon(invalidName)
            )
        }
    }
    
    @Test
    fun `Property 4 - Icon variants - outline and sharp variants should be from Ionicons`() {
        val baseIcons = listOf("add", "calendar", "home", "heart", "settings")
        val variants = listOf("outline", "sharp")
        
        baseIcons.forEach { baseName ->
            variants.forEach { variant ->
                val variantName = "${baseName}_$variant"
                val resourceId = iconManager.getIcon(variantName)
                
                // If variant exists, verify it's from Ionicons
                resourceId?.let {
                    val resourceName = context.resources.getResourceEntryName(it)
                    assertTrue(
                        "Variant icon should be from Ionicons pack: $resourceName",
                        resourceName.startsWith("ic_ionicons_")
                    )
                }
            }
        }
    }
    
    @Test
    fun `Property 5 - Null safety - manager should handle null and empty strings gracefully`() {
        // Test with empty string
        val emptyResult = iconManager.getIcon("")
        assertNull("Empty string should return null", emptyResult)
        
        // Fallback should still work
        val fallback = iconManager.getFallbackIcon("")
        assertNotEquals("Fallback should be valid even for empty string", 0, fallback)
    }
    
    // Helper function to generate random invalid icon names
    private fun generateRandomInvalidIconName(): String {
        val length = Random.nextInt(5, 20)
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf('_', '-')
        val randomName = (1..length).map { chars.random() }.joinToString("")
        
        // Ensure it's not a valid icon by adding a unique prefix
        return "invalid_${randomName}_${Random.nextInt(10000)}"
    }
}
