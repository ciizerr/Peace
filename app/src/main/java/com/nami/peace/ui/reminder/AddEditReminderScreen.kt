package com.nami.peace.ui.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.ui.components.GlassyTopAppBar
import com.nami.peace.ui.components.GlassySheetSurface
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddEditReminderViewModel = hiltViewModel(),
    settingsViewModel: com.nami.peace.ui.settings.SettingsViewModel = hiltViewModel(),
    hazeState: HazeState? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Global Settings
    val blurEnabled by settingsViewModel.blurEnabled.collectAsState()
    val blurStrength by settingsViewModel.blurStrength.collectAsState()
    val blurTintAlpha by settingsViewModel.blurTintAlpha.collectAsState()
    val shadowsEnabled by settingsViewModel.shadowsEnabled.collectAsState()
    val shadowStyleString by settingsViewModel.shadowStyle.collectAsState()
    
    // Map String -> Int for GlassySheetSurface
    val shadowStyleInt = remember(shadowStyleString) {
        when (shadowStyleString) {
            "None" -> 0
            "Subtle" -> 1
            "Medium" -> 2
            "Hard" -> 3
            else -> 1
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val localHazeState = hazeState ?: remember { HazeState() }
    
    // Bottom Sheet State
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    // Helpers
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    fun showDatePicker() {
        val initialMillis = uiState.dateInMillis ?: System.currentTimeMillis()
        val c = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(context, { _, y, m, d ->
            val sel = Calendar.getInstance()
            sel.set(y, m, d)
            viewModel.onEvent(AddEditReminderEvent.DateChanged(sel.timeInMillis))
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun showTimePicker() {
        val c = Calendar.getInstance().apply { timeInMillis = uiState.startTimeInMillis }
        TimePickerDialog(context, { _, h, m ->
            val newCal = Calendar.getInstance()
            newCal.set(Calendar.HOUR_OF_DAY, h)
            newCal.set(Calendar.MINUTE, m)
            viewModel.onEvent(AddEditReminderEvent.StartTimeChanged(newCal.timeInMillis))
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
    }

    fun onSave() {
        if (uiState.title.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.err_title_required)) }
        } else {
            viewModel.onEvent(AddEditReminderEvent.SaveReminder)
            onNavigateUp()
        }
    }

    // Advanced Preview String (Improved Description)
    val advancedPreview = remember(uiState) {
        if (uiState.isNagModeEnabled) {
             val strictPart = if (uiState.isStrictSchedulingEnabled) " • Strict" else ""
             "Nag Mode: On • ${uiState.nagIntervalValue}${uiState.nagIntervalUnit.name.take(1).lowercase()} × ${uiState.nagTotalRepetitions}$strictPart"
        } else {
            null
        }
    }

    // Layout: Root Box to handle floating overlays
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent, // For Haze
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                GlassyTopAppBar(
                    title = { Text(stringResource(if (uiState.id != 0) R.string.edit_reminder else R.string.new_reminder)) },
                    hazeState = localHazeState,
                    blurEnabled = blurEnabled,
                    blurStrength = blurStrength, // Float for GlassyTopAppBar
                    blurTintAlpha = blurTintAlpha,
                    shadowsEnabled = shadowsEnabled,
                    shadowStyle = shadowStyleString // String for GlassyTopAppBar
                )
            },
            // Bottom Bar removed to handle as floating overlay
        ) { padding ->
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 16.dp,
                    bottom = 120.dp, // Space for floating bottom bar
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .haze(
                        state = localHazeState,
                        style = dev.chrisbanes.haze.HazeStyle(blurRadius = if (blurEnabled) blurStrength.dp else 0.dp, tint = Color.Transparent)
                    ) 
            ) {
                item {
                    TitleSection(
                        title = uiState.title,
                        onTitleChanged = { viewModel.onEvent(AddEditReminderEvent.TitleChanged(it)) },
                        categoryName = uiState.category.name.lowercase().capitalize(Locale.getDefault()),
                        recurrenceName = uiState.recurrenceType.name.lowercase().split("_").joinToString(" ") { it.capitalize(Locale.getDefault()) },
                        timeText = timeFormat.format(Date(uiState.startTimeInMillis))
                    )
                }

                item {
                    DateTimeCard(
                        dateText = uiState.dateInMillis?.let { dateFormat.format(Date(it)) } ?: stringResource(R.string.today),
                        timeText = timeFormat.format(Date(uiState.startTimeInMillis)),
                        onDateClick = { showDatePicker() },
                        onTimeClick = { showTimePicker() }
                    )
                }

                item {
                    CategoryPriorityCard(
                        category = uiState.category,
                        priority = uiState.priority,
                        onCategoryChanged = { viewModel.onEvent(AddEditReminderEvent.CategoryChanged(it)) },
                        onPriorityChanged = { viewModel.onEvent(AddEditReminderEvent.PriorityChanged(it)) }
                    )
                }

                item {
                    RecurrenceCard(
                        recurrenceType = uiState.recurrenceType,
                        daysOfWeek = uiState.daysOfWeek,
                        onRecurrenceChanged = { viewModel.onEvent(AddEditReminderEvent.RecurrenceChanged(it)) },
                        onDayToggled = { viewModel.onEvent(AddEditReminderEvent.DayToggled(it)) }
                    )
                }
                
                if (uiState.showPermissionBanner) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(stringResource(R.string.exact_alarm_permission_required), style = MaterialTheme.typography.titleMedium)
                                Button(onClick = { 
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    context.startActivity(intent)
                                }) { Text("Grant") }
                            }
                        }
                    }
                }

                item {
                    AdvancedBottomSheetTrigger(
                        onClick = { showBottomSheet = true },
                        previewText = advancedPreview
                    )
                }
            }
        }

        // Floating Action Bar (Hides when BottomSheet is open)
        androidx.compose.animation.AnimatedVisibility(
            visible = !showBottomSheet,
            enter = androidx.compose.animation.slideInVertically { it } + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically { it } + androidx.compose.animation.fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            GlassyActionBar(
                onSave = { onSave() },
                onCancel = onNavigateUp,
                modifier = Modifier
            )
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.Transparent, // Glassy
                scrimColor = Color.Transparent, // Transparent scrim (matched HistoryScreen)
                dragHandle = null // We draw our own or none for glassy effect
            ) {
                GlassySheetSurface(
                    hazeState = localHazeState,
                    blurEnabled = blurEnabled,
                    blurStrength = blurStrength.toInt(), // Int for GlassySheetSurface
                    blurTintAlpha = blurTintAlpha,
                    shadowsEnabled = shadowsEnabled,
                    shadowStyle = shadowStyleInt // Int for GlassySheetSurface
                ) {
                    AdvancedBottomSheetContent(
                        isNagModeEnabled = uiState.isNagModeEnabled,
                        onNagToggled = { viewModel.onEvent(AddEditReminderEvent.NagModeToggled(it)) },
                        nagIntervalValue = uiState.nagIntervalValue,
                        onNagIntervalValueChanged = { viewModel.onEvent(AddEditReminderEvent.NagIntervalValueChanged(it)) },
                        nagIntervalUnit = uiState.nagIntervalUnit,
                        onNagIntervalUnitChanged = { viewModel.onEvent(AddEditReminderEvent.NagIntervalUnitChanged(it)) },
                        nagTotalRepetitions = uiState.nagTotalRepetitions,
                        maxAllowedRepetitions = uiState.maxAllowedRepetitions,
                        onNagRepetitionsChanged = { viewModel.onEvent(AddEditReminderEvent.NagRepetitionsChanged(it)) },
                        isStrictSchedulingEnabled = uiState.isStrictSchedulingEnabled,
                        onStrictModeToggled = { viewModel.onEvent(AddEditReminderEvent.StrictModeToggled(it)) },
                        onClose = { scope.launch { sheetState.hide(); showBottomSheet = false } }
                    )
                }
            }
        }
    }
}
