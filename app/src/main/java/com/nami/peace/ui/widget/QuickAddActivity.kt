package com.nami.peace.ui.widget

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.nami.peace.R
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.ui.theme.PeaceTheme
import com.nami.peace.util.widget.ReminderParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Activity for quick reminder creation from widget.
 * Implements Requirements 17.6, 17.7, 17.8
 */
@AndroidEntryPoint
class QuickAddActivity : ComponentActivity() {
    
    @Inject
    lateinit var reminderRepository: ReminderRepository
    
    @Inject
    lateinit var reminderParser: ReminderParser
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            PeaceTheme {
                QuickAddScreen(
                    onAddReminder = { text ->
                        addReminder(text)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
    
    private fun addReminder(text: String) {
        if (text.isBlank()) {
            Toast.makeText(this, "Please enter a reminder", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                // Parse the text using AI/NLP parser
                val parsedReminder = reminderParser.parse(text)
                
                // Insert the reminder
                reminderRepository.insertReminder(parsedReminder)
                
                // Show confirmation toast
                Toast.makeText(
                    this@QuickAddActivity,
                    "Reminder created: ${parsedReminder.title}",
                    Toast.LENGTH_SHORT
                ).show()
                
                // Close the activity
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@QuickAddActivity,
                    "Failed to create reminder: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

@Composable
private fun QuickAddScreen(
    onAddReminder: (String) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Quick Add Reminder",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Input field
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("What do you need to do?") },
                placeholder = { Text("e.g., Buy milk at 5pm tomorrow") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_ionicons_create),
                        contentDescription = "Create"
                    )
                },
                singleLine = false,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Helper text
            Text(
                text = "Try natural language like:\n• \"Meeting tomorrow at 2pm\"\n• \"Call mom every day at 6pm\"\n• \"Buy groceries this Friday\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = { onAddReminder(text) },
                    modifier = Modifier.weight(1f),
                    enabled = text.isNotBlank()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_ionicons_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add")
                }
            }
        }
    }
}
