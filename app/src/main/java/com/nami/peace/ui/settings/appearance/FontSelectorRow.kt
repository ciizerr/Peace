package com.nami.peace.ui.settings.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun FontSelectorRow(
    currentFont: String,
    onFontSelected: (String) -> Unit
) {
    val fonts = listOf("System", "Poppins", "Lato", "Bodoni", "Loves", "Serif", "Monospace", "Cursive")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // ... (Header Text remains same, but easier to just replace whole function body if needed, but chunking is safer)
        Text(
            text = "Font Family",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(fonts) { fontName ->
                val fontFamily = when (fontName) {
                    "Poppins" -> androidx.compose.ui.text.font.FontFamily(
                        androidx.compose.ui.text.font.Font(com.nami.peace.R.font.poppins_regular)
                    )
                    "Lato" -> androidx.compose.ui.text.font.FontFamily(
                        androidx.compose.ui.text.font.Font(com.nami.peace.R.font.lato_regular)
                    )
                    "Bodoni" -> androidx.compose.ui.text.font.FontFamily(
                        androidx.compose.ui.text.font.Font(com.nami.peace.R.font.bodoni_moda)
                    )
                    "Loves" -> androidx.compose.ui.text.font.FontFamily(
                        androidx.compose.ui.text.font.Font(com.nami.peace.R.font.loves)
                    )
                    "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                    "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
                    "Cursive" -> androidx.compose.ui.text.font.FontFamily.Cursive
                    else -> androidx.compose.ui.text.font.FontFamily.Default
                }
                
                val isSelected = currentFont == fontName
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onFontSelected(fontName) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                            else MaterialTheme.colorScheme.surfaceContainer
                        )
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "Aa",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = fontFamily,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = fontName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
