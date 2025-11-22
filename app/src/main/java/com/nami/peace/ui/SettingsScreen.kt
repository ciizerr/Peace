package com.nami.peace.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.nami.peace.ui.navigation.Screen

enum class SettingsCategory(val title: String) {
    General("General"),
    Database("Database"),
    AI("AI"),
    About("About")
}

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: PeaceViewModel
) {
    var selectedCategory by remember { mutableStateOf(SettingsCategory.General) }
    
    // Use MaterialTheme colors instead of hardcoded values
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val accentColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Observe Data
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isHapticsEnabled by viewModel.isHapticsEnabled.collectAsState()
    val currentSound by viewModel.currentSound.collectAsState()
    val currentKey by viewModel.userApiKeyFlow.collectAsState(initial = "")
    val userName by viewModel.userName.collectAsState()
    val profileUri by viewModel.profileImageUri.collectAsState()
    val isPrivacyModeEnabled by viewModel.isPrivacyModeEnabled.collectAsState()
    val themeAccent by viewModel.themeAccent.collectAsState()

    Scaffold(
        containerColor = backgroundColor,
        contentColor = contentColor
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Left Navigation Rail
            SettingsNavigationRail(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                onBackClick = { navController.popBackStack() },
                accentColor = accentColor,
                contentColor = contentColor
            )

            // Right Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
                SettingsContent(
                    category = selectedCategory,
                    viewModel = viewModel,
                    accentColor = accentColor,
                    contentColor = contentColor,
                    userName = userName,
                    currentKey = currentKey,
                    isDarkMode = isDarkMode,
                    isHapticsEnabled = isHapticsEnabled,
                    currentSound = currentSound,
                    isPrivacyModeEnabled = isPrivacyModeEnabled,
                    themeAccent = themeAccent,
                    profileUri = profileUri,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun SettingsNavigationRail(
    selectedCategory: SettingsCategory,
    onCategorySelected: (SettingsCategory) -> Unit,
    onBackClick: () -> Unit,
    accentColor: Color,
    contentColor: Color
) {
    Column(
        modifier = Modifier
            .width(60.dp) // Fixed width for the rail
            .fillMaxHeight()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button at the top
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = contentColor.copy(alpha = 0.7f)
            )
        }

        // Categories
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SettingsCategory.values().forEach { category ->
                val isSelected = category == selectedCategory
                val textColor = if (isSelected) accentColor else contentColor.copy(alpha = 0.5f)
                
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .clickable { onCategorySelected(category) },
                    contentAlignment = Alignment.Center
                ) {
                    // Rotated Text
                    Text(
                        text = category.title,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .rotate(-90f)
                            .padding(vertical = 8.dp) // Padding becomes horizontal when rotated
                    )
                }
            }
        }
        
        // Bottom spacer to balance layout
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun SettingsContent(
    category: SettingsCategory,
    viewModel: PeaceViewModel,
    accentColor: Color,
    contentColor: Color,
    userName: String,
    currentKey: String?,
    isDarkMode: Boolean,
    isHapticsEnabled: Boolean,
    currentSound: String,
    isPrivacyModeEnabled: Boolean,
    themeAccent: String,
    profileUri: String?,
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header
        Text(
            text = category.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 32.dp, top = 8.dp).align(Alignment.End)
        )

        AnimatedContent(
            targetState = category,
            transitionSpec = {
                // Slide logic: if moving down the list (ordinal increases), content slides up (enters from bottom).
                // If moving up the list (ordinal decreases), content slides down (enters from top).
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                } else {
                    (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> height } + fadeOut())
                }.using(
                    SizeTransform(clip = false)
                )
            },
            label = "SettingsContent"
        ) { targetCategory ->
            Column(modifier = Modifier.fillMaxWidth()) {
                when (targetCategory) {
                    SettingsCategory.General -> GeneralSettings(
                        viewModel = viewModel,
                        accentColor = accentColor,
                        contentColor = contentColor,
                        isDarkMode = isDarkMode,
                        isHapticsEnabled = isHapticsEnabled,
                        currentSound = currentSound,
                        isPrivacyModeEnabled = isPrivacyModeEnabled,
                        themeAccent = themeAccent
                    )
                    SettingsCategory.Database -> DatabaseSettings(
                        accentColor = accentColor,
                        contentColor = contentColor,
                        storageUsage = viewModel.getStorageUsage(),
                        onBackup = { viewModel.backupData() },
                        onRestore = { viewModel.restoreData() },
                        onExport = { viewModel.exportData() },
                        onNuke = { viewModel.nukeData() }
                    )
                    SettingsCategory.AI -> AISettings(
                        accentColor = accentColor,
                        contentColor = contentColor,
                        currentKey = currentKey ?: "",
                        onKeyChange = { viewModel.saveApiKey(it) }
                    )
                    SettingsCategory.About -> AboutSettings(
                        accentColor = accentColor,
                        contentColor = contentColor,
                        appVersion = viewModel.getAppVersion()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- Category Specific Content ---

@Composable
fun GeneralSettings(
    viewModel: PeaceViewModel,
    accentColor: Color,
    contentColor: Color,
    isDarkMode: Boolean,
    isHapticsEnabled: Boolean,
    currentSound: String,
    isPrivacyModeEnabled: Boolean,
    themeAccent: String
) {
    val context = LocalContext.current
    
    SettingsSectionHeader("Appearance", accentColor)
    SettingsToggleItem(
        icon = Icons.Outlined.DarkMode,
        title = "Dark Mode",
        subtitle = "Easier on the eyes at night",
        checked = isDarkMode,
        onCheckedChange = { viewModel.toggleDarkMode(it) },
        iconColor = Color(0xFF3D5AFE),
        accentColor = accentColor,
        contentColor = contentColor
    )
    
    // Theme Accent Picker
    Text(
        text = "Theme Accent",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = contentColor,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Row(modifier = Modifier.padding(bottom = 24.dp)) {
        val colors = listOf("Purple", "Blue", "Teal", "Green")
        val colorValues = listOf(Color(0xFF6200EE), Color(0xFF2196F3), Color(0xFF009688), Color(0xFF4CAF50))
        
        colors.forEachIndexed { index, name ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp)
                    .background(colorValues[index], androidx.compose.foundation.shape.CircleShape)
                    .clickable { viewModel.updateThemeAccent(name) }
                    .then(if (themeAccent == name) Modifier.border(2.dp, contentColor, androidx.compose.foundation.shape.CircleShape) else Modifier)
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    SettingsSectionHeader("Audio & Notifications", accentColor)
    SettingsActionItem(
        icon = Icons.Outlined.Notifications,
        title = "Notification Settings",
        subtitle = "Manage system notifications",
        onClick = {
            val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        },
        iconColor = Color(0xFFFF5722),
        accentColor = accentColor,
        contentColor = contentColor
    )
    SettingsActionItem(
        icon = Icons.Outlined.VolumeUp,
        title = "Default Sound",
        subtitle = currentSound,
        onClick = {
            val newSound = if (currentSound == "Calm Breeze") "Gentle Rain" else "Calm Breeze"
            viewModel.updateSound(newSound)
            Toast.makeText(context, "Sound changed to $newSound", Toast.LENGTH_SHORT).show()
        },
        iconColor = Color(0xFF9C27B0),
        accentColor = accentColor,
        contentColor = contentColor
    )

    Spacer(modifier = Modifier.height(24.dp))

    SettingsSectionHeader("Privacy & Interaction", accentColor)
    SettingsToggleItem(
        icon = Icons.Outlined.VisibilityOff,
        title = "Privacy Mode",
        subtitle = "Hide content on lock screen",
        checked = isPrivacyModeEnabled,
        onCheckedChange = { viewModel.togglePrivacyMode(it) },
        iconColor = Color(0xFF607D8B),
        accentColor = accentColor,
        contentColor = contentColor
    )
    SettingsToggleItem(
        icon = Icons.Outlined.Vibration,
        title = "Haptics",
        subtitle = "Vibrate on interaction",
        checked = isHapticsEnabled,
        onCheckedChange = { viewModel.toggleHaptics(it) },
        iconColor = Color(0xFF2E7D32),
        accentColor = accentColor,
        contentColor = contentColor
    )
}

@Composable
fun DatabaseSettings(
    accentColor: Color,
    contentColor: Color,
    storageUsage: String,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onExport: () -> Unit,
    onNuke: () -> Unit
) {
    Column {
        SettingsSectionHeader("Database", contentColor)
        SettingsActionItem(
            icon = Icons.Outlined.Storage,
            title = "Storage Usage",
            subtitle = storageUsage,
            onClick = { /* TODO: Clear cache? */ },
            iconColor = Color(0xFF795548),
            accentColor = accentColor,
            contentColor = contentColor
        )
        SettingsActionItem(
            icon = Icons.Outlined.Backup,
            title = "Backup",
            subtitle = "Save your data",
            onClick = onBackup,
            iconColor = Color(0xFFFF9800),
            accentColor = accentColor,
            contentColor = contentColor
        )
        SettingsActionItem(
            icon = Icons.Outlined.Restore,
            title = "Restore",
            subtitle = "Load from backup",
            onClick = onRestore,
            iconColor = Color(0xFF4CAF50),
            accentColor = accentColor,
            contentColor = contentColor
        )
        SettingsActionItem(
            icon = Icons.Outlined.FileDownload,
            title = "Export Data",
            subtitle = "Save as JSON",
            onClick = onExport,
            iconColor = Color(0xFF00BCD4),
            accentColor = accentColor,
            contentColor = contentColor
        )
        SettingsActionItem(
            icon = Icons.Outlined.DeleteForever,
            title = "Clear Data",
            subtitle = "Delete all reminders and categories",
            onClick = onNuke,
            iconColor = Color(0xFFF44336),
            accentColor = accentColor,
            contentColor = contentColor
        )
    }
}

@Composable
fun AISettings(
    accentColor: Color,
    contentColor: Color,
    currentKey: String,
    onKeyChange: (String) -> Unit
) {
    var showGuide by remember { mutableStateOf(false) }

    Column {
        SettingsSectionHeader("AI", contentColor)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gemini API Key", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Paste your Gemini API key here to use your own quota.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = currentKey,
                    onValueChange = onKeyChange,
                    placeholder = { Text("AIzaSy...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        cursorColor = accentColor
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(onClick = { showGuide = !showGuide }) {
                    Text(if (showGuide) "Hide Guide" else "How to get a key?", color = accentColor)
                }
                
                AnimatedVisibility(visible = showGuide) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text("1. Go to aistudio.google.com", fontSize = 12.sp, color = contentColor)
                        Text("2. Create a new project or select one.", fontSize = 12.sp, color = contentColor)
                        Text("3. Click 'Get API key' -> 'Create API key'.", fontSize = 12.sp, color = contentColor)
                        Text("4. Copy and paste it above.", fontSize = 12.sp, color = contentColor)
                    }
                }
            }
        }
    }
}

@Composable
fun AboutSettings(
    accentColor: Color,
    contentColor: Color,
    appVersion: String
) {
    val context = LocalContext.current
    
    Text(
        text = "$appVersion by ciizerr",
        color = contentColor.copy(alpha = 0.5f),
        fontSize = 24.sp,
        modifier = Modifier.padding(bottom = 24.dp).fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.End
    )

    SettingsSectionHeader("Social", accentColor)
    SettingsActionItem(
        icon = Icons.Outlined.Code,
        title = "GitHub",
        subtitle = "View the source code",
        onClick = { 
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ciizerr"))
            context.startActivity(intent)
        },
        iconColor = Color(0xFF607D8B),
        accentColor = accentColor,
        contentColor = contentColor
    )

    Spacer(modifier = Modifier.height(24.dp))

    SettingsSectionHeader("Contact", accentColor)
    SettingsActionItem(
        icon = Icons.Outlined.BugReport,
        title = "Report an issue",
        subtitle = "If you need help with a bug you can file an issue on GitHub",
        onClick = { /* TODO */ },
        iconColor = Color(0xFFF44336),
        accentColor = accentColor,
        contentColor = contentColor
    )
    SettingsActionItem(
        icon = Icons.Outlined.Lightbulb,
        title = "Request a feature or suggest an idea",
        subtitle = "You will be redirected to GitHub",
        onClick = { /* TODO */ },
        iconColor = Color(0xFFFFC107),
        accentColor = accentColor,
        contentColor = contentColor
    )

    Spacer(modifier = Modifier.height(24.dp))

    SettingsSectionHeader("Version", accentColor)
    SettingsActionItem(
        icon = Icons.Outlined.Update,
        title = "Check for updates",
        subtitle = "You're currently running version $appVersion",
        onClick = { Toast.makeText(context, "Checking...", Toast.LENGTH_SHORT).show() },
        iconColor = Color(0xFF4CAF50),
        accentColor = accentColor,
        contentColor = contentColor
    )
    SettingsToggleItem(
        icon = Icons.Outlined.Autorenew,
        title = "Automatically check for updates",
        subtitle = "Off",
        checked = false,
        onCheckedChange = { /* TODO */ },
        iconColor = Color(0xFF2196F3),
        accentColor = accentColor,
        contentColor = contentColor
    )
}

// --- Reusable Components ---

@Composable
fun SettingsSectionHeader(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color,
    accentColor: Color,
    contentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = contentColor
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = contentColor.copy(alpha = 0.6f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector? = null,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color = Color.Unspecified,
    accentColor: Color,
    contentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = contentColor.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = contentColor.copy(alpha = 0.5f),
                uncheckedTrackColor = contentColor.copy(alpha = 0.1f)
            )
        )
    }
}
