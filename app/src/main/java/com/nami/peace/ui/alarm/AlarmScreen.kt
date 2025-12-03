package com.nami.peace.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.ui.theme.*

@Composable
fun AlarmScreen(
    onFinish: () -> Unit,
    viewModel: AlarmViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.loadActiveReminders()
    }

    LaunchedEffect(uiState.shouldFinish) {
        if (uiState.shouldFinish) {
            onFinish()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Hero Section
            uiState.heroReminder?.let { hero ->
                val priorityColor = when (hero.priority) {
                    PriorityLevel.HIGH -> PriorityHigh
                    PriorityLevel.MEDIUM -> PriorityMedium
                    PriorityLevel.LOW -> PriorityLow
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    priorityColor,
                                    priorityColor.copy(alpha = 0.9f),
                                    priorityColor.copy(alpha = 0.85f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 32.dp, vertical = 48.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon with glow
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = CircleShape,
                                    ambientColor = Color.White.copy(alpha = 0.3f),
                                    spotColor = Color.White.copy(alpha = 0.3f)
                                )
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "🔔",
                                style = MaterialTheme.typography.displayLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(40.dp))
                        
                        Text(
                            text = "It's time to",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White.copy(alpha = 0.95f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = hero.title,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.W600
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(56.dp))
                        
                        // Action Buttons
                        Button(
                            onClick = { viewModel.markDone(hero) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = priorityColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = MaterialTheme.shapes.small,
                                    ambientColor = Color.White.copy(alpha = 0.3f),
                                    spotColor = Color.White.copy(alpha = 0.3f)
                                ),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Mark Done",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.W600
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.snooze(hero) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Color.White.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(
                                Icons.Outlined.Snooze,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Snooze",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.W600
                                )
                            )
                        }
                    }
                }
            }

            // Bundle Section
            if (uiState.bundledReminders.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Also due",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.W600
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.bundledReminders) { reminder ->
                                val stripColor = when (reminder.priority) {
                                    PriorityLevel.HIGH -> PriorityHigh
                                    PriorityLevel.MEDIUM -> PriorityMedium
                                    PriorityLevel.LOW -> PriorityLow
                                }
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = MaterialTheme.shapes.medium,
                                            ambientColor = ShadowLight,
                                            spotColor = ShadowLight
                                        ),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.height(IntrinsicSize.Min),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(4.dp)
                                                .background(stripColor)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                reminder.title,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.W500
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Surface(
                                                color = stripColor.copy(alpha = 0.15f),
                                                shape = MaterialTheme.shapes.extraSmall
                                            ) {
                                                Text(
                                                    reminder.priority.name,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.W600
                                                    ),
                                                    color = stripColor,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
