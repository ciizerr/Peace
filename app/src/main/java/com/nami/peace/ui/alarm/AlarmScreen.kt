package com.nami.peace.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nami.peace.R
import com.nami.peace.ui.theme.AccentRed
import com.nami.peace.ui.theme.White
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AlarmScreen(
    onFinish: () -> Unit,
    viewModel: AlarmViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val hazeState = remember { HazeState() }

    // Live Clock State
    val currentTime by produceState(initialValue = LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            delay(1000)
        }
    }

    LaunchedEffect(true) {
        viewModel.loadActiveReminders()
    }

    LaunchedEffect(uiState.shouldFinish) {
        if (uiState.shouldFinish) {
            onFinish()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Serene Background Image
        AsyncImage(
            model = R.drawable.peace_alarm_bg,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Haze Blur Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(
                    state = hazeState,
                    style = HazeStyle(blurRadius = 40.dp, tint = Color.Black.copy(alpha = 0.15f))
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        // 3. Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper Section: Clock & Greeting
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 84.sp,
                    letterSpacing = (-2).sp
                ),
                color = White
            )
            
            Text(
                text = stringResource(R.string.alarm_hero_title).uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Center Section: Hero Reminder
            uiState.heroReminder?.let { hero ->
                GlassyCard(
                    hazeState = hazeState,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(AccentRed.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, AccentRed.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AccentRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = hero.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = White,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        if (!hero.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = hero.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = White.copy(alpha = 0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        
                        if (hero.priority.name.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                color = White.copy(alpha = 0.1f),
                                shape = CircleShape,
                                modifier = Modifier.border(0.5.dp, White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Text(
                                    text = hero.priority.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.2f))

            // Lower Section: Bundled Reminders (if any)
            if (uiState.bundledReminders.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.alarm_bundle_header),
                    style = MaterialTheme.typography.labelMedium,
                    color = White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.bundledReminders) { reminder ->
                        GlassySmallCard(hazeState = hazeState, title = reminder.title)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Bottom Section: Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassyActionButton(
                    onClick = { uiState.heroReminder?.let { viewModel.markDone(it) } },
                    icon = Icons.Default.Check,
                    label = stringResource(R.string.alarm_btn_done),
                    containerColor = White.copy(alpha = 0.9f),
                    contentColor = Color.Black,
                    hazeState = hazeState,
                    modifier = Modifier.weight(1.5f)
                )
                
                GlassyActionButton(
                    onClick = { uiState.heroReminder?.let { viewModel.snooze(it) } },
                    icon = Icons.Default.Snooze,
                    label = stringResource(R.string.alarm_btn_snooze),
                    containerColor = White.copy(alpha = 0.15f),
                    contentColor = White,
                    hazeState = hazeState,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GlassyCard(
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(32.dp)
    Box(
        modifier = modifier
            .hazeChild(
                state = hazeState,
                shape = shape,
                style = HazeStyle(blurRadius = 20.dp, tint = Color.White.copy(alpha = 0.05f))
            )
            .border(1.dp, Color.White.copy(alpha = 0.15f), shape)
            .background(Color.White.copy(alpha = 0.05f), shape)
    ) {
        content()
    }
}

@Composable
fun GlassySmallCard(
    hazeState: HazeState,
    title: String
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .widthIn(max = 160.dp)
            .hazeChild(
                state = hazeState,
                shape = shape,
                style = HazeStyle(blurRadius = 15.dp, tint = Color.White.copy(alpha = 0.05f))
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.1f), shape)
            .background(Color.White.copy(alpha = 0.05f), shape)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = White.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GlassyActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = shape,
        modifier = modifier
            .height(64.dp)
            .clip(shape)
            .hazeChild(
                state = hazeState,
                shape = shape,
                style = HazeStyle(blurRadius = 12.dp, tint = containerColor.copy(alpha = 0.1f))
            )
            .background(containerColor)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}
