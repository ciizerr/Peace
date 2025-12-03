package com.nami.peace.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Premium Shape System - Smooth, rounded, Apple-inspired
val PeaceShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp), // Pills, chips
    small = RoundedCornerShape(16.dp), // Buttons, small cards
    medium = RoundedCornerShape(20.dp), // Standard cards
    large = RoundedCornerShape(28.dp), // Large cards, modals
    extraLarge = RoundedCornerShape(36.dp) // Hero cards, bottom sheets
)
