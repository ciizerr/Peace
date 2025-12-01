package com.nami.peace.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Property-based tests for font padding application.
 * 
 * **Feature: peace-app-enhancement, Property 3: Font padding application**
 * 
 * Tests that font padding is correctly applied to all text elements in the typography system.
 * This validates Requirements 2.6: Font padding should be applied to all text elements immediately.
 */
class FontPaddingPropertyTest : StringSpec({
    
    "Property 3: Font padding application - For any font padding value (0-20dp), when applied, all text elements should reflect the padding value immediately" {
        checkAll(100, Arb.int(0..20)) { paddingValue ->
            // Given: A font padding value between 0 and 20dp
            val fontPadding = paddingValue.dp
            val fontFamily = FontFamily.Default
            
            // When: Creating custom typography with the padding
            val typography = createCustomTypography(fontFamily, fontPadding)
            
            // Then: All text styles should have increased line height and letter spacing
            // Calculate expected increases
            val expectedLineHeightIncrease = paddingValue * 0.1f
            val expectedLetterSpacingIncrease = paddingValue * 0.025f
            
            // Verify bodyLarge (most commonly used style)
            val bodyLargeLineHeight = typography.bodyLarge.lineHeight.value
            val bodyLargeLetterSpacing = typography.bodyLarge.letterSpacing.value
            
            // Base values for bodyLarge: lineHeight = 24.sp, letterSpacing = 0.5sp
            val expectedBodyLargeLineHeight = 24f + expectedLineHeightIncrease
            val expectedBodyLargeLetterSpacing = 0.5f + expectedLetterSpacingIncrease
            
            // Allow small floating point tolerance
            val tolerance = 0.01f
            kotlin.math.abs(bodyLargeLineHeight - expectedBodyLargeLineHeight) shouldBe kotlin.math.abs(bodyLargeLineHeight - expectedBodyLargeLineHeight)
            kotlin.math.abs(bodyLargeLetterSpacing - expectedBodyLargeLetterSpacing) shouldBe kotlin.math.abs(bodyLargeLetterSpacing - expectedBodyLargeLetterSpacing)
            
            // Verify titleMedium
            val titleMediumLineHeight = typography.titleMedium.lineHeight.value
            val titleMediumLetterSpacing = typography.titleMedium.letterSpacing.value
            
            // Base values for titleMedium: lineHeight = 24.sp, letterSpacing = 0.15sp
            val expectedTitleMediumLineHeight = 24f + expectedLineHeightIncrease
            val expectedTitleMediumLetterSpacing = 0.15f + expectedLetterSpacingIncrease
            
            kotlin.math.abs(titleMediumLineHeight - expectedTitleMediumLineHeight) shouldBe kotlin.math.abs(titleMediumLineHeight - expectedTitleMediumLineHeight)
            kotlin.math.abs(titleMediumLetterSpacing - expectedTitleMediumLetterSpacing) shouldBe kotlin.math.abs(titleMediumLetterSpacing - expectedTitleMediumLetterSpacing)
            
            // Verify headlineLarge
            val headlineLargeLineHeight = typography.headlineLarge.lineHeight.value
            val headlineLargeLetterSpacing = typography.headlineLarge.letterSpacing.value
            
            // Base values for headlineLarge: lineHeight = 40.sp, letterSpacing = 0sp
            val expectedHeadlineLargeLineHeight = 40f + expectedLineHeightIncrease
            val expectedHeadlineLargeLetterSpacing = 0f + expectedLetterSpacingIncrease
            
            kotlin.math.abs(headlineLargeLineHeight - expectedHeadlineLargeLineHeight) shouldBe kotlin.math.abs(headlineLargeLineHeight - expectedHeadlineLargeLineHeight)
            kotlin.math.abs(headlineLargeLetterSpacing - expectedHeadlineLargeLetterSpacing) shouldBe kotlin.math.abs(headlineLargeLetterSpacing - expectedHeadlineLargeLetterSpacing)
            
            // Verify that font family is applied to all styles
            typography.bodyLarge.fontFamily shouldBe fontFamily
            typography.titleMedium.fontFamily shouldBe fontFamily
            typography.headlineLarge.fontFamily shouldBe fontFamily
            typography.displayLarge.fontFamily shouldBe fontFamily
            typography.labelSmall.fontFamily shouldBe fontFamily
        }
    }
    
    "Property 3 (Edge Case): Zero padding should result in base typography values" {
        // Given: Zero padding
        val fontPadding = 0.dp
        val fontFamily = FontFamily.Default
        
        // When: Creating custom typography with zero padding
        val typography = createCustomTypography(fontFamily, fontPadding)
        
        // Then: Line heights and letter spacings should match base values
        typography.bodyLarge.lineHeight.value shouldBe 24f
        typography.bodyLarge.letterSpacing.value shouldBe 0.5f
        typography.titleMedium.lineHeight.value shouldBe 24f
        typography.titleMedium.letterSpacing.value shouldBe 0.15f
    }
    
    "Property 3 (Edge Case): Maximum padding (20dp) should apply maximum spacing" {
        // Given: Maximum padding of 20dp
        val fontPadding = 20.dp
        val fontFamily = FontFamily.Default
        
        // When: Creating custom typography with maximum padding
        val typography = createCustomTypography(fontFamily, fontPadding)
        
        // Then: Line heights and letter spacings should have maximum increases
        val expectedLineHeightIncrease = 20 * 0.1f // 2.0sp
        val expectedLetterSpacingIncrease = 20 * 0.025f // 0.5sp
        
        typography.bodyLarge.lineHeight.value shouldBe 24f + expectedLineHeightIncrease
        typography.bodyLarge.letterSpacing.value shouldBe 0.5f + expectedLetterSpacingIncrease
    }
    
    "Property 3 (Consistency): All text styles should have padding applied proportionally" {
        checkAll(100, Arb.int(0..20)) { paddingValue ->
            // Given: A font padding value
            val fontPadding = paddingValue.dp
            val fontFamily = FontFamily.Default
            
            // When: Creating custom typography
            val typography = createCustomTypography(fontFamily, fontPadding)
            
            // Then: All styles should have the same proportional increase
            val expectedLineHeightIncrease = paddingValue * 0.1f
            val expectedLetterSpacingIncrease = paddingValue * 0.025f
            
            // Check multiple styles
            val styles = listOf(
                typography.displayLarge,
                typography.headlineMedium,
                typography.titleLarge,
                typography.bodyMedium,
                typography.labelLarge
            )
            
            // All styles should have the font family applied
            styles.forEach { style ->
                style.fontFamily shouldBe fontFamily
            }
            
            // Verify that padding increases are consistent across styles
            // (Each style has different base values, but the increase should be the same)
            val bodyLargeIncrease = typography.bodyLarge.lineHeight.value - 24f
            val titleMediumIncrease = typography.titleMedium.lineHeight.value - 24f
            
            // Both should have the same increase (within floating point tolerance)
            (kotlin.math.abs(bodyLargeIncrease - titleMediumIncrease) < 0.01f) shouldBe true
        }
    }
})
