package com.nami.peace.util.icon

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of IconManager that loads and manages Ionicons vector drawables.
 * Provides caching for performance and fallback icons for missing resources.
 * 
 * Performance optimizations:
 * - Lazy initialization of icon cache
 * - Thread-safe concurrent cache access
 * - Preloading of commonly used icons
 * - Efficient resource lookup with normalized names
 */
@Singleton
class IoniconsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : IconManager {
    
    // Thread-safe concurrent cache for better performance under concurrent access
    private val iconCache = java.util.concurrent.ConcurrentHashMap<String, Int>()
    private val TAG = "IoniconsManager"
    
    // Lazy initialization flag to avoid loading all icons at startup
    @Volatile
    private var isInitialized = false
    
    // Fallback icon - using a simple circle icon from Ionicons
    // Try multiple fallback options to ensure we always have a valid icon
    private val fallbackIconResourceId by lazy {
        val fallbackOptions = listOf(
            "ic_ionicons_help_circle",
            "ic_ionicons_ellipse",
            "ic_ionicons_radio_button_on",
            "ic_ionicons_checkmark_circle"
        )
        
        for (iconName in fallbackOptions) {
            val resourceId = context.resources.getIdentifier(
                iconName,
                "drawable",
                context.packageName
            )
            if (resourceId != 0) {
                return@lazy resourceId
            }
        }
        
        // If all else fails, use Android's built-in icon
        android.R.drawable.ic_menu_help
    }
    
    init {
        // Preload commonly used icons for instant access
        preloadCommonIcons()
    }
    
    /**
     * Preload commonly used icons to avoid lookup delays.
     * This runs on initialization to cache frequently accessed icons.
     */
    private fun preloadCommonIcons() {
        val commonIcons = listOf(
            "add", "close", "checkmark", "trash", "create", "settings",
            "calendar", "time", "alarm", "notifications", "home",
            "chevron_back", "chevron_forward", "ellipsis_vertical"
        )
        
        commonIcons.forEach { iconName ->
            getIcon(iconName) // This will cache the icon
        }
    }
    
    override fun getIcon(iconName: String): Int? {
        // Check cache first (fast path)
        iconCache[iconName]?.let { return it }
        
        // Try to load the icon (slow path)
        val resourceId = loadIconFromResources(iconName)
        
        return if (resourceId != 0) {
            // Cache the result for future access
            iconCache[iconName] = resourceId
            resourceId
        } else {
            // Cache negative results to avoid repeated lookups
            iconCache[iconName] = 0
            Log.w(TAG, "Icon not found: $iconName")
            null
        }
    }
    
    override fun getAllIcons(): Map<String, Int> {
        // If cache is empty, load all icons
        if (iconCache.isEmpty()) {
            loadAllIcons()
        }
        return iconCache.toMap()
    }
    
    override fun getFallbackIcon(iconName: String): Int {
        Log.w(TAG, "Using fallback icon for: $iconName")
        return fallbackIconResourceId
    }
    
    override fun hasIcon(iconName: String): Boolean {
        return getIcon(iconName) != null
    }
    
    /**
     * Load an icon from resources by name.
     * Supports multiple naming conventions:
     * - Direct name: "add" -> "ic_ionicons_add"
     * - With prefix: "ic_ionicons_add" -> "ic_ionicons_add"
     * - With variant: "add_outline" -> "ic_ionicons_add_outline"
     */
    private fun loadIconFromResources(iconName: String): Int {
        val normalizedName = normalizeIconName(iconName)
        
        return context.resources.getIdentifier(
            normalizedName,
            "drawable",
            context.packageName
        )
    }
    
    /**
     * Normalize icon name to match the resource naming convention.
     */
    private fun normalizeIconName(iconName: String): String {
        // If already has the prefix, return as is
        if (iconName.startsWith("ic_ionicons_")) {
            return iconName
        }
        
        // Add the prefix
        return "ic_ionicons_$iconName"
    }
    
    /**
     * Load all available Ionicons into the cache.
     * This is done lazily when getAllIcons() is called.
     * Uses double-checked locking for thread-safe lazy initialization.
     */
    private fun loadAllIcons() {
        if (isInitialized) return
        
        synchronized(this) {
            if (isInitialized) return
            
            try {
                val drawableClass = Class.forName("${context.packageName}.R\$drawable")
                val fields = drawableClass.declaredFields
                
                // Pre-allocate map capacity for better performance
                val tempCache = HashMap<String, Int>(fields.size)
                
                fields.forEach { field ->
                    val fieldName = field.name
                    if (fieldName.startsWith("ic_ionicons_")) {
                        try {
                            val resourceId = field.getInt(null)
                            // Store with both full name and short name
                            tempCache[fieldName] = resourceId
                            
                            // Also store without the prefix for easier access
                            val shortName = fieldName.removePrefix("ic_ionicons_")
                            if (!tempCache.containsKey(shortName)) {
                                tempCache[shortName] = resourceId
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error loading icon: $fieldName", e)
                        }
                    }
                }
                
                // Bulk update the concurrent cache
                iconCache.putAll(tempCache)
                isInitialized = true
                
                Log.d(TAG, "Loaded ${iconCache.size} icons into cache")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading all icons", e)
            }
        }
    }
}
