package com.nami.peace.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

fun getTypography(fontFamily: FontFamily, isBold: Boolean = false): Typography {
    val weight = if (isBold) FontWeight.Bold else FontWeight.Normal
    val titleWeight = if (isBold) FontWeight.Black else FontWeight.Medium // Titles already boldish, make bolder

    return Typography(
        displayLarge = Typography.displayLarge.copy(fontFamily = fontFamily, fontWeight = weight),
        displayMedium = Typography.displayMedium.copy(fontFamily = fontFamily, fontWeight = weight),
        displaySmall = Typography.displaySmall.copy(fontFamily = fontFamily, fontWeight = weight),
        headlineLarge = Typography.headlineLarge.copy(fontFamily = fontFamily, fontWeight = weight),
        headlineMedium = Typography.headlineMedium.copy(fontFamily = fontFamily, fontWeight = weight),
        headlineSmall = Typography.headlineSmall.copy(fontFamily = fontFamily, fontWeight = weight),
        titleLarge = Typography.titleLarge.copy(fontFamily = fontFamily, fontWeight = titleWeight),
        titleMedium = Typography.titleMedium.copy(fontFamily = fontFamily, fontWeight = titleWeight),
        titleSmall = Typography.titleSmall.copy(fontFamily = fontFamily, fontWeight = titleWeight),
        bodyLarge = Typography.bodyLarge.copy(fontFamily = fontFamily, fontWeight = weight),
        bodyMedium = Typography.bodyMedium.copy(fontFamily = fontFamily, fontWeight = weight),
        bodySmall = Typography.bodySmall.copy(fontFamily = fontFamily, fontWeight = weight),
        labelLarge = Typography.labelLarge.copy(fontFamily = fontFamily, fontWeight = weight),
        labelMedium = Typography.labelMedium.copy(fontFamily = fontFamily, fontWeight = weight),
        labelSmall = Typography.labelSmall.copy(fontFamily = fontFamily, fontWeight = weight)
    )
}
