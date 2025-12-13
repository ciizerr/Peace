package com.nami.peace.ui.settings.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import com.nami.peace.ui.components.GlassyDialogSurface

import com.nami.peace.ui.components.GlassyDialog

@Composable
fun ColorPickerDialog(
    show: Boolean,
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    hazeState: HazeState? = null,
    blurEnabled: Boolean = true,
    blurStrength: Float = 15f,
    blurTintAlpha: Float = 0.2f
) {
    // Initial HSV values
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var value by remember { mutableStateOf(1f) }

    // Initialize state from initialColor
    LaunchedEffect(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
    }

    val currentColor = remember(hue, saturation, value) {
        Color.hsv(hue, saturation, value)
    }

    GlassyDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        hazeState = hazeState,
        blurEnabled = blurEnabled,
        blurStrength = blurStrength.toInt(),
        blurTintAlpha = blurTintAlpha,
        modifier = Modifier.padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Color Compass",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Preview Circle
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))
                
                 // Hex Input
                var hexText by remember(currentColor) { mutableStateOf(String.format("#%08X", currentColor.toArgb())) }
                
                OutlinedTextField(
                    value = hexText.replace("#FF", "#"), 
                    onValueChange = { newValue ->
                        hexText = newValue
                        try {
                            val cleanHex = if (newValue.startsWith("#")) newValue else "#$newValue"
                            if (cleanHex.length == 7 || cleanHex.length == 9) {
                                val parseColor = android.graphics.Color.parseColor(cleanHex)
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(parseColor, hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                            }
                        } catch (e: Exception) { }
                    },
                    label = { Text("Hex Code") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.width(160.dp)
                )

            Spacer(modifier = Modifier.height(24.dp))

            // Sliders
            LabelledSlider(label = "Hue", value = hue, range = 0f..360f, onValueChange = { hue = it })
            LabelledSlider(label = "Saturation", value = saturation, range = 0f..1f, onValueChange = { saturation = it })
            LabelledSlider(label = "Lightness", value = value, range = 0f..1f, onValueChange = { value = it })

            Spacer(modifier = Modifier.height(16.dp))

            // Warning
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Note: Ensure your color is readable against light and dark backgrounds.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onColorSelected(currentColor) }) {
                    Text("Set Color")
                }
            }
        }
    }
}

@Composable
private fun LabelledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}
