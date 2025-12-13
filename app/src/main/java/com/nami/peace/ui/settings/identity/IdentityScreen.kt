package com.nami.peace.ui.settings.identity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Save
import com.nami.peace.ui.components.GlassyFloatingActionButton
import com.nami.peace.ui.settings.appearance.SwitchSettingRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nami.peace.R
import com.nami.peace.ui.components.GlassySection
import com.nami.peace.ui.profile.ProfileSheet
import com.nami.peace.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    hazeState: HazeState? = null
) {
    val scrollState = rememberScrollState()
    val state = hazeState ?: remember { HazeState() }
    
    // -- Local State (UI Only for now) --
    // Profile
    // We can pull profile from ViewModel since it exists
    val userProfile by viewModel.userProfile.collectAsState(initial = com.nami.peace.data.repository.UserProfile())
    var showProfileSheet by remember { mutableStateOf(false) }

    // AI Config (Local Only)
    var isGeminiEnabled by remember { mutableStateOf(false) } // Master Toggle
    
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var accessTasks by remember { mutableStateOf(true) }
    var accessNotes by remember { mutableStateOf(true) }
    var accessHistory by remember { mutableStateOf(false) }
    var accessProfileData by remember { mutableStateOf(true) }
    
    // Granular Profile Permissions
    var accessName by remember { mutableStateOf(true) }
    var accessOccupation by remember { mutableStateOf(true) }
    var accessBio by remember { mutableStateOf(true) }
    var accessWakeTime by remember { mutableStateOf(true) }
    var accessBedTime by remember { mutableStateOf(true) }
    
    var systemPrompt by remember { mutableStateOf("") }
    
    // New Feature State
    var selectedModel by remember { mutableStateOf("Flash") } // "Flash" or "Pro"
    val personalityPresets = listOf(
        stringResource(R.string.preset_stoic) to stringResource(R.string.hint_system_prompt),
        stringResource(R.string.preset_friendly) to "You are a friendly and encouraging assistant...",
        stringResource(R.string.preset_concise) to "Answer as concisely as possible. No filler.",
        stringResource(R.string.preset_technical) to "You are a senior software engineer assistant..."
    )
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Profile Name Editing
    var isNameEditing by remember { mutableStateOf(false) }
    var editingName by remember(userProfile.name) { mutableStateOf(userProfile.name) }

    // Used for permissions layout
    @OptIn(ExperimentalLayoutApi::class)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state)
                    .verticalScroll(scrollState)
                     // Use specific padding values to ensure content starts below the floating header initially
                     // but scrolls behind it. 80.dp is roughly bar height + status bar.
                    .padding(top = padding.calculateTopPadding() + 80.dp, bottom = padding.calculateBottomPadding() + 100.dp) 
            ) {

                // --- 1. PROFILE SECTION ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Name & Details
                    Column(modifier = Modifier.weight(1f)) {
                        // Editable Name
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (isNameEditing) {
                                 androidx.compose.foundation.text.BasicTextField(
                                     value = editingName,
                                     onValueChange = { editingName = it },
                                     textStyle = MaterialTheme.typography.displaySmall.copy(
                                         color = MaterialTheme.colorScheme.onBackground,
                                         fontWeight = FontWeight.Bold
                                     ),
                                     modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 0.dp), // Zero vertical padding to match Text
                                     keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                         imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                     ),
                                     keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                         onDone = {
                                             viewModel.updateUserProfile(userProfile.copy(name = editingName))
                                             isNameEditing = false
                                         }
                                     ),
                                     cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
                                 )
                            } else {
                                Text(
                                    text = userProfile.name.ifEmpty { stringResource(R.string.title_your_profile) },
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.clickable { isNameEditing = true }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = userProfile.occupation.ifEmpty { "—" },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                         Text(
                            text = userProfile.bio.ifEmpty { "No bio yet." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                    }

                    // Right: Profile Picture
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showProfileSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                         if (userProfile.photoUri != null) {
                             AsyncImage(
                                 model = userProfile.photoUri,
                                 contentDescription = stringResource(R.string.cd_profile_photo),
                                 modifier = Modifier.fillMaxSize(),
                                 contentScale = ContentScale.Crop
                             )
                         } else {
                             Icon(
                                 Icons.Default.Person,
                                 contentDescription = null,
                                 modifier = Modifier.size(48.dp),
                                 tint = MaterialTheme.colorScheme.onSurfaceVariant
                             )
                         }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 2. AI CONFIGURATION SECTION ---
                GlassySection(title = stringResource(R.string.title_neural_config)) {
                    
                    // Master Toggle
                     SwitchSettingRow(
                        label = stringResource(R.string.lbl_enable_gemini),
                        checked = isGeminiEnabled,
                        onCheckedChange = { isGeminiEnabled = it },
                        // Could use a specific icon for Gemini if available, or a generic spark/AI icon
                        imageVector = Icons.Default.Face 
                    )
                    
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isGeminiEnabled,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        Column {
                             HorizontalDivider(
                                 modifier = Modifier.padding(vertical = 8.dp), 
                                 color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                             )
                            
                             // API Key Vault
                            OutlinedTextField(
                                value = apiKey,
                                onValueChange = { apiKey = it },
                                label = { Text(stringResource(R.string.lbl_neural_key)) },
                                placeholder = { Text(stringResource(R.string.hint_neural_key)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    val image = if (showApiKey) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    IconButton(onClick = { showApiKey = !showApiKey }) {
                                        Icon(image, contentDescription = null)
                                    }
                                }
                            )
                            
                            // Model Selection
                             Text(
                                text = stringResource(R.string.title_model_selection),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Flash" to stringResource(R.string.lbl_model_flash), "Pro" to stringResource(R.string.lbl_model_pro)).forEach { (key, label) ->
                                     FilterChip(
                                         selected = selectedModel == key,
                                         onClick = { selectedModel = key },
                                         label = { Text(label) },
                                         leadingIcon = if (selectedModel == key) { { Icon(Icons.Default.Check, null) } } else null,
                                         modifier = Modifier.weight(1f)
                                     )
                                }
                            }
        
                            
                            Text(
                                text = stringResource(R.string.title_data_permissions),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
        
                            // Data Permissions (Chips in FlowRow)
                             FlowRow(
                                 modifier = Modifier.fillMaxWidth(),
                                 horizontalArrangement = Arrangement.spacedBy(8.dp),
                                 verticalArrangement = Arrangement.spacedBy(8.dp)
                             ) {
                                 // Tasks
                                 FilterChip(
                                     selected = accessTasks,
                                     onClick = { accessTasks = !accessTasks; if(!accessTasks) accessNotes = false },
                                     label = { Text(stringResource(R.string.lbl_access_tasks)) },
                                     leadingIcon = if (accessTasks) { { Icon(Icons.Default.Check, null) } } else null
                                 )
                                 
                                 // Notes (Dependent on Tasks)
                                 if (accessTasks) {
                                      FilterChip(
                                         selected = accessNotes,
                                         onClick = { accessNotes = !accessNotes },
                                         label = { Text(stringResource(R.string.lbl_access_notes)) },
                                         leadingIcon = if (accessNotes) { { Icon(Icons.Default.Check, null) } } else null
                                     )
                                 }
                                 
                                 // History
                                 FilterChip(
                                     selected = accessHistory,
                                     onClick = { accessHistory = !accessHistory },
                                     label = { Text(stringResource(R.string.lbl_access_history)) },
                                     leadingIcon = if (accessHistory) { { Icon(Icons.Default.Check, null) } } else null
                                 )

                                 // Profile (Master)
                                 FilterChip(
                                     selected = accessProfileData,
                                     onClick = { accessProfileData = !accessProfileData },
                                     label = { Text(stringResource(R.string.lbl_access_profile_data)) },
                                     leadingIcon = if (accessProfileData) { { Icon(Icons.Default.Check, null) } } else null
                                 )
                             }
                             
                             // Granular Profile Fields (Only if Profile is enabled)
                             androidx.compose.animation.AnimatedVisibility(visible = accessProfileData) {
                                 Column {
                                     Text(
                                         text = "Profile Fields", // Could extract string
                                         style = MaterialTheme.typography.labelMedium,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant,
                                         modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                     )
                                     FlowRow(
                                         modifier = Modifier.fillMaxWidth(),
                                         horizontalArrangement = Arrangement.spacedBy(8.dp),
                                         verticalArrangement = Arrangement.spacedBy(8.dp)
                                     ) {
                                         val profileChips = listOf(
                                              Triple(accessName, { accessName = !accessName }, R.string.lbl_access_name),
                                              Triple(accessOccupation, { accessOccupation = !accessOccupation }, R.string.lbl_access_occupation),
                                              Triple(accessBio, { accessBio = !accessBio }, R.string.lbl_access_bio),
                                              Triple(accessWakeTime, { accessWakeTime = !accessWakeTime }, R.string.lbl_access_wake_time),
                                              Triple(accessBedTime, { accessBedTime = !accessBedTime }, R.string.lbl_access_bed_time),
                                         )
                                         
                                         profileChips.forEach { (checked, onToggle, labelRes) ->
                                             FilterChip(
                                                 selected = checked,
                                                 onClick = onToggle,
                                                 label = { Text(stringResource(labelRes)) },
                                                 leadingIcon = if (checked) {
                                                     { Icon(Icons.Default.Check, contentDescription = null) }
                                                 } else null
                                             )
                                         }
                                     }
                                 }
                             }
                            
                            // Personality Presets
                            Text(
                                text = stringResource(R.string.title_personality),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(personalityPresets) { (label, prompt) ->
                                    SuggestionChip(
                                        onClick = { systemPrompt = prompt },
                                        label = { Text(label) }
                                    )
                                }
                            }
                            
                            // System Prompt
                            OutlinedTextField(
                                value = systemPrompt,
                                onValueChange = { systemPrompt = it },
                                label = { Text(stringResource(R.string.lbl_system_prompt)) },
                                placeholder = { Text(stringResource(R.string.hint_system_prompt)) },
                                modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 10
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Space for FAB
            }
            
            // Floating Glassy Top Bar (Using Box Alignment)
            com.nami.peace.ui.components.GlassyTopAppBar(
                 title = { 
                     Text(
                         stringResource(R.string.title_identity),
                         style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                     ) 
                 },
                 navigationIcon = {
                     IconButton(onClick = onNavigateBack) {
                         Icon(
                             imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                             contentDescription = stringResource(R.string.back)
                         )
                     }
                 },
                 modifier = Modifier.align(Alignment.TopCenter),
                 hazeState = state,
                 blurEnabled = true
             )
            
            // Floating Save Button (Only show if Gemini is enabled, or allow saving toggle state too?)
            // Show always to save toggle state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                GlassyFloatingActionButton(
                    onClick = {
                        android.widget.Toast.makeText(context, R.string.msg_config_saved, android.widget.Toast.LENGTH_SHORT).show()
                    },
                    icon = Icons.Default.Save,
                    contentDescription = stringResource(R.string.btn_save_config),
                    hazeState = state
                )
            }
            
            // Profile Sheet
            if (showProfileSheet) {
               com.nami.peace.ui.components.GlassyBottomSheet(
                   onDismissRequest = { showProfileSheet = false },
                   show = showProfileSheet,
                   hazeState = state
               ) {
                   ProfileSheet(
                       userProfile = userProfile,
                       onSave = { newProfile ->
                           viewModel.updateUserProfile(newProfile)
                           showProfileSheet = false
                       },
                       onClose = { showProfileSheet = false }
                   )
               }
            }
        }
    }
}
