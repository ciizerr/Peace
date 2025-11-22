package com.nami.peace.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.nami.peace.data.Frequency
import com.nami.peace.data.Reminder
import com.nami.peace.ui.PeaceViewModel
import com.nami.peace.ui.navigation.Screen
import com.nami.peace.ui.theme.PeaceCardBlue
import com.nami.peace.ui.theme.PeaceCardGreen
import com.nami.peace.ui.theme.PeaceCardPurple
import com.nami.peace.ui.theme.PeaceCardRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: PeaceViewModel,
    onFabClick: () -> Unit
) {
    val reminders by viewModel.allReminders.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val profileUri by viewModel.profileImageUri.collectAsState()
    val gardenProgress by viewModel.gardenProgress.collectAsState()
    val coachQuote = viewModel.coachQuote
    
    // Greeting logic
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        in 18..22 -> "Good Evening"
        else -> "Good Night"
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header Section
            HeaderSection(
                userName = userName,
                greeting = greeting,
                profileUri = profileUri,
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Peace Garden (Visual Progress)
            PeaceGarden(
                progress = gardenProgress,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // AI Insight Card
            AiInsightCard(quote = coachQuote)

            Spacer(modifier = Modifier.height(24.dp))

            // Reminders List
            Text(
                "Your Focus",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(reminders) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onClick = { viewModel.onEditClick(reminder); onFabClick() },
                        onToggleCompletion = { viewModel.toggleReminderCompletion(reminder) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    userName: String,
    greeting: String,
    profileUri: String?,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting, $userName! ☀️",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Let's find your peace today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        // Profile Picture
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            if (profileUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(profileUri),
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AiInsightCard(quote: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(listOf(Color(0xFF7C4DFF), Color(0xFF536DFE)))) // Purple to Blue gradient
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("PEACE COACH", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = quote,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    onToggleCompletion: () -> Unit
) {
    // Map dummy integers to our colors (or use category color if available)
    // For now, using static colors based on type/random for variety
    val cardColor = when(reminder.id.toInt() % 4) {
        0 -> PeaceCardBlue
        1 -> PeaceCardPurple
        2 -> PeaceCardGreen
        3 -> PeaceCardRed
        else -> PeaceCardBlue
    }

    val containerColor = if (reminder.isCompleted) MaterialTheme.colorScheme.surfaceVariant else cardColor
    val contentColor = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else Color.White

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val timeString = timeFormat.format(Date(reminder.timeInMillis))
                    
                    Text(
                        timeString,
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        style = if (reminder.isCompleted) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle()
                    )
                    if (reminder.frequency != Frequency.Once) {
                        Text(" ↻", color = contentColor.copy(alpha = 0.7f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    reminder.title,
                    color = contentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    style = if (reminder.isCompleted) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle()
                )
            }

            // Checkbox for completion
            androidx.compose.material3.Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = { onToggleCompletion() },
                colors = androidx.compose.material3.CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = contentColor.copy(alpha = 0.7f),
                    checkmarkColor = Color.White
                )
            )
        }
    }
}
