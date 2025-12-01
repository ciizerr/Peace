package com.nami.peace.ui.reminder

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.nami.peace.R
import com.nami.peace.ui.components.AddNoteDialog
import com.nami.peace.ui.components.AttachmentGrid
import com.nami.peace.ui.components.BackgroundWrapper
import com.nami.peace.ui.components.FullScreenImageViewer
import com.nami.peace.ui.components.ImagePickerDialog
import com.nami.peace.ui.components.NoteList
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.ui.settings.SettingsViewModel
import com.nami.peace.util.background.BackgroundImageManager
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.domain.model.PriorityLevel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    onNavigateUp: () -> Unit,
    onEditReminder: (Int) -> Unit,
    viewModel: ReminderDetailViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    backgroundImageManager: BackgroundImageManager? = null
) {
    val context = LocalContext.current
    val iconManager: IconManager = remember { IoniconsManager(context) }
    val uiState by viewModel.uiState
    
    // Background settings
    val blurIntensity by settingsViewModel.blurIntensity.collectAsState()
    val slideshowEnabled by settingsViewModel.slideshowEnabled.collectAsState()
    
    // Feature toggles - get from settings view model
    val attachmentsEnabled by settingsViewModel.attachmentsEnabled.collectAsState()

    // Handle share confirmation toast
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(uiState.showShareConfirmation) {
        if (uiState.showShareConfirmation) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.share_confirmation),
                duration = SnackbarDuration.Short
            )
            viewModel.hideShareConfirmation()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reminder_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        PeaceIcon(
                            iconName = "arrow_back",
                            contentDescription = stringResource(R.string.back),
                            iconManager = iconManager
                        )
                    }
                },
                actions = {
                    // Share button
                    if (uiState.reminder != null) {
                        IconButton(
                            onClick = {
                                val shareLink = viewModel.generateShareLink()
                                if (shareLink != null) {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareLink)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(
                                        sendIntent,
                                        context.getString(R.string.share_reminder)
                                    )
                                    context.startActivity(shareIntent)
                                    viewModel.showShareConfirmation()
                                }
                            }
                        ) {
                            PeaceIcon(
                                iconName = "share",
                                contentDescription = stringResource(R.string.share),
                                iconManager = iconManager
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.reminder != null) {
                FloatingActionButton(onClick = { onEditReminder(uiState.reminder!!.id) }) {
                    PeaceIcon(
                        iconName = "create",
                        contentDescription = "Edit",
                        iconManager = iconManager
                    )
                }
            }
        }
    ) { paddingValues ->
        BackgroundWrapper(
            attachments = uiState.attachments,
            blurIntensity = blurIntensity,
            slideshowEnabled = slideshowEnabled,
            backgroundImageManager = backgroundImageManager,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    val reminder = uiState.reminder
                    if (reminder != null) {
                        Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Box
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = getPriorityColor(reminder.priority)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = reminder.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = reminder.category.iconResId),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = reminder.category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        // Info Cards
                        DetailCard(
                            label = "Original Time",
                            value = formatTime(reminder.originalStartTimeInMillis)
                        )

                        DetailCard(
                            label = "Recurrence",
                            value = when (reminder.recurrenceType) {
                                com.nami.peace.domain.model.RecurrenceType.ONE_TIME -> {
                                    if (reminder.dateInMillis != null) {
                                        "One Time: " + SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(reminder.dateInMillis))
                                    } else {
                                        "One Time"
                                    }
                                }
                                com.nami.peace.domain.model.RecurrenceType.WEEKLY -> {
                                    val days = reminder.daysOfWeek.sorted().joinToString(", ") { day ->
                                        when (day) {
                                            1 -> "Sun"
                                            2 -> "Mon"
                                            3 -> "Tue"
                                            4 -> "Wed"
                                            5 -> "Thu"
                                            6 -> "Fri"
                                            7 -> "Sat"
                                            else -> ""
                                        }
                                    }
                                    "Weekly on $days"
                                }
                                else -> reminder.recurrenceType.name
                            }
                        )

                        DetailCard(
                            label = "Schedule Mode",
                            value = if (reminder.isStrictSchedulingEnabled) "Strict (Anchored)" else "Flexible (Drift)"
                        )

                        DetailCard(
                            label = "Nag Mode",
                            value = if (reminder.isNagModeEnabled) "Enabled" else "Disabled"
                        )

                        if (reminder.isNagModeEnabled) {
                            Text(
                                text = "Sequence Progress",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    val interval = reminder.nagIntervalInMillis ?: (15 * 60 * 1000L)
                                    
                                    for (i in 0 until reminder.nagTotalRepetitions) {
                                        val repTime = reminder.originalStartTimeInMillis + (i * interval)
                                        val status = when {
                                            i < reminder.currentRepetitionIndex -> "Done"
                                            i == reminder.currentRepetitionIndex -> "Next"
                                            else -> "Upcoming"
                                        }
                                        
                                        val color = when (status) {
                                            "Done" -> Color.Gray
                                            "Next" -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                        
                                        val fontWeight = if (status == "Next") FontWeight.Bold else FontWeight.Normal
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Rep ${i + 1}: ${formatTime(repTime)}",
                                                color = color,
                                                fontWeight = fontWeight
                                            )
                                            Text(
                                                text = status,
                                                color = color,
                                                fontWeight = fontWeight,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        if (i < reminder.nagTotalRepetitions - 1) {
                                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Notes Section (only show if attachments feature is enabled)
                        if (attachmentsEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Notes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Button(
                                    onClick = { viewModel.showAddNoteDialog() },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    PeaceIcon(
                                        iconName = "add",
                                        contentDescription = "Add note",
                                        iconManager = iconManager,
                                        size = 20.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Note")
                                }
                            }
                            
                            NoteList(
                                notes = uiState.notes,
                                onDelete = { note -> viewModel.deleteNote(note) },
                                iconManager = iconManager,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // Attachments Section
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Attachments",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Button(
                                    onClick = { viewModel.showImagePickerDialog() },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    PeaceIcon(
                                        iconName = "image",
                                        contentDescription = "Add image",
                                        iconManager = iconManager,
                                        size = 20.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Image")
                                }
                            }
                            
                            AttachmentGrid(
                                attachments = uiState.attachments,
                                onDelete = { attachment -> viewModel.deleteAttachment(attachment) },
                                onAttachmentClick = { attachment -> viewModel.showFullScreenImage(attachment) },
                                iconManager = iconManager,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    } else {
                        Text("Reminder not found", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
    
    // Dialogs (only show if attachments feature is enabled)
    if (attachmentsEnabled) {
        if (uiState.showAddNoteDialog) {
            AddNoteDialog(
                onDismiss = { viewModel.hideAddNoteDialog() },
                onConfirm = { content -> viewModel.addNote(content) },
                iconManager = iconManager
            )
        }
        
        if (uiState.showImagePickerDialog) {
            ImagePickerDialog(
                onDismiss = { viewModel.hideImagePickerDialog() },
                onImageSelected = { uri -> viewModel.addAttachment(uri) },
                iconManager = iconManager
            )
        }
        
        uiState.selectedAttachment?.let { attachment ->
            FullScreenImageViewer(
                attachment = attachment,
                onDismiss = { viewModel.hideFullScreenImage() },
                iconManager = iconManager
            )
        }
    }
}

@Composable
fun DetailCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun getPriorityColor(priority: PriorityLevel): Color {
    return when (priority) {
        PriorityLevel.HIGH -> Color(0xFFFFCDD2) // Red-ish
        PriorityLevel.MEDIUM -> Color(0xFFFFF9C4) // Yellow-ish
        PriorityLevel.LOW -> Color(0xFFC8E6C9) // Green-ish
    }
}

fun formatTime(timeInMillis: Long): String {
    val calendar = Calendar.getInstance().apply { this.timeInMillis = timeInMillis }
    return String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
}
