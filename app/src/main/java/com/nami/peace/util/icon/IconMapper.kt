package com.nami.peace.util.icon

/**
 * Utility object for mapping common icon names to Ionicons names.
 * Provides semantic mappings for frequently used icons in the Peace app.
 */
object IconMapper {
    
    /**
     * Map of semantic icon names to Ionicons names.
     * This allows using descriptive names like "add_task" instead of "add_circle".
     */
    private val semanticMappings = mapOf(
        // Task-related icons
        "add_task" to "add_circle",
        "complete_task" to "checkmark_circle",
        "delete_task" to "trash",
        "edit_task" to "create",
        
        // Navigation icons
        "back" to "arrow_back",
        "forward" to "arrow_forward",
        "menu" to "menu",
        "close" to "close",
        
        // Settings icons
        "settings" to "settings",
        "notifications" to "notifications",
        "calendar" to "calendar",
        "time" to "time",
        
        // Category icons
        "general" to "apps",
        "work" to "briefcase",
        "home" to "home",
        "health" to "fitness",
        "study" to "school",
        
        // Peace Garden icons
        "garden" to "leaf",
        "growth" to "sparkles",
        "achievement" to "trophy",
        "streak" to "flame",
        
        // Attachment icons
        "image" to "image",
        "note" to "document_text",
        "attach" to "attach",
        
        // Alarm icons
        "alarm" to "alarm",
        "sound" to "musical_notes",
        "volume" to "volume_high",
        
        // Misc icons
        "help" to "help_circle",
        "info" to "information_circle",
        "warning" to "warning",
        "error" to "alert_circle",
        "success" to "checkmark_circle"
    )
    
    /**
     * Get the Ionicons name for a semantic icon name.
     * If no mapping exists, returns the original name.
     */
    fun getIconName(semanticName: String): String {
        return semanticMappings[semanticName] ?: semanticName
    }
    
    /**
     * Get the Ionicons name with a specific variant (outline, sharp, or default).
     * @param semanticName The semantic name of the icon
     * @param variant The variant: "outline", "sharp", or null for default
     */
    fun getIconNameWithVariant(semanticName: String, variant: String? = null): String {
        val baseName = getIconName(semanticName)
        return when (variant) {
            "outline" -> "${baseName}_outline"
            "sharp" -> "${baseName}_sharp"
            else -> baseName
        }
    }
    
    /**
     * Check if a semantic mapping exists for the given name.
     */
    fun hasSemanticMapping(semanticName: String): Boolean {
        return semanticMappings.containsKey(semanticName)
    }
    
    /**
     * Get all available semantic mappings.
     */
    fun getAllMappings(): Map<String, String> {
        return semanticMappings.toMap()
    }
}
