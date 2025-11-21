package com.nami.peace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nami.peace.data.Frequency
import com.nami.peace.data.ReminderType
import com.nami.peace.ui.components.PeaceButton
import com.nami.peace.ui.components.PeaceChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(
    viewModel: PeaceViewModel,
    onDismiss: () -> Unit
) {
    // This is the text the user types for the AI (e.g. "Yoga at 7am")
    var aiInput by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Time Picker State
    val calendar = java.util.Calendar.getInstance()
    val timePickerDialog = android.app.TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            viewModel.reminderTime = String.format("%02d:%02d", hour, minute)
        },
        calendar.get(java.util.Calendar.HOUR_OF_DAY),
        calendar.get(java.util.Calendar.MINUTE),
        true
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        // --- 1. Drag Handle ---
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(20.dp))

        val titleText = if (viewModel.currentReminderId != null) "Edit Peace Reminder" else "New Peace Reminder"
        Text(titleText, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(20.dp))

        // --- 2. The "Magic" Input Field ---
        Text("WHAT NEEDS TO BE DONE?", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.reminderTitle.ifEmpty { aiInput },
            onValueChange = { 
                viewModel.reminderTitle = it
                aiInput = it 
            },
            placeholder = { Text("e.g., 'Yoga at 7am gently'", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            trailingIcon = {
                // THE SPARKLE BUTTON
                IconButton(onClick = { viewModel.onSparkleClick(aiInput) }) {
                    if (viewModel.isAiLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = "AI Magic", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.onSparkleClick(aiInput) })
        )

        Text("✨ Tip: Type a sentence like \"Run every morning\" and click the sparkle!", color = Color(0xFF9C27B0), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. Results (Auto-filled by AI or Manual) ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Time Display
            Column(modifier = Modifier.weight(1f)) {
                Text("TIME", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clickable { timePickerDialog.show() }, // Click to open Time Picker
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (viewModel.reminderTime.isEmpty()) "--:--" else viewModel.reminderTime,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Mode Display (Notification vs Alarm)
             Column(modifier = Modifier.weight(1f)) {
                Text("MODE", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clickable { 
                             // Toggle Logic
                             viewModel.reminderType = if (viewModel.reminderType == ReminderType.Notification) ReminderType.Alarm else ReminderType.Notification
                        },
                    contentAlignment = Alignment.Center
                ) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = if (viewModel.reminderType == ReminderType.Notification) Icons.Outlined.Notifications else Icons.Outlined.Alarm
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                 Text(if (viewModel.reminderType == ReminderType.Notification) "Notification" else "Alarm", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Frequency
        Text("REPEAT", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.padding(top = 8.dp)) {
            Frequency.values().forEach { freq ->
                val isSelected = viewModel.reminderFrequency == freq
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(bgColor, RoundedCornerShape(20.dp))
                        .clickable { viewModel.reminderFrequency = freq }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }, color = contentColor, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 4. Save Button ---
        val buttonText = if (viewModel.currentReminderId != null) "✓ Update Peace" else "✓ Set Peace"
        Row(modifier = Modifier.fillMaxWidth()) {
             Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    viewModel.saveReminder()
                    onDismiss()
                },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}