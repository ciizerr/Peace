package com.nami.peace.util.icon

/**
 * Interface for managing icon resources in the Peace app.
 * Provides access to Ionicons vector drawables with caching and fallback support.
 */
interface IconManager {
    /**
     * Get an icon by name from the Ionicons pack.
     * @param iconName The name of the icon (without the "ic_ionicons_" prefix)
     * @return The resource ID of the icon, or null if not found
     */
    fun getIcon(iconName: String): Int?
    
    /**
     * Get all available icons as a map of name to resource ID.
     * @return Map of icon names to resource IDs
     */
    fun getAllIcons(): Map<String, Int>
    
    /**
     * Get a fallback icon when the requested icon is not found.
     * @param iconName The name of the icon that was not found
     * @return The resource ID of a fallback icon
     */
    fun getFallbackIcon(iconName: String): Int
    
    /**
     * Check if an icon exists in the Ionicons pack.
     * @param iconName The name of the icon to check
     * @return true if the icon exists, false otherwise
     */
    fun hasIcon(iconName: String): Boolean
}
