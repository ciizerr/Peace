package com.nami.peace.ui.settings.appearance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.zIndex
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.ui.theme.AccentBlue
import com.nami.peace.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    hazeState: HazeState? = null
) {
    val scrollState = rememberScrollState()
    
    // UI State
    val themeMode by viewModel.themeMode.collectAsState()
    val moodColor by viewModel.moodColor.collectAsState()
    val isBoldText by viewModel.isBoldText.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    
    // Immersion State
    val blurEnabled by viewModel.blurEnabled.collectAsState()
    val shadowsEnabled by viewModel.shadowsEnabled.collectAsState()
    val blurStrength by viewModel.blurStrength.collectAsState()
    val blurTintAlpha by viewModel.blurTintAlpha.collectAsState()
    val shadowStrength by viewModel.shadowStrength.collectAsState()
    
    // Haze Preview State
    val effectiveHazeState = hazeState ?: remember { HazeState() }

    // Map float strength to string style for GlassyTopAppBar
    val shadowStyle = when {
        shadowStrength == 0f -> "None"
        shadowStrength <= 0.33f -> "Subtle"
        shadowStrength <= 0.66f -> "Medium"
        else -> "Heavy"
    }

    // Color Picker State
    var showColorPicker by remember { mutableStateOf(false) }
    
    // Language Picker State
    var showLanguagePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(effectiveHazeState) // Source for preview
                    .verticalScroll(scrollState)
                    .contentPadding(
                        padding = PaddingValues(
                            top = padding.calculateTopPadding() + 80.dp, // Add extra padding for floating bar
                            bottom = padding.calculateBottomPadding() + 16.dp,
                            start = 0.dp,
                            end = 0.dp
                        )
                    )
            ) {
                // 1. Theme Section
                GlassySection(title = stringResource(R.string.section_theme)) {
                    ThemeSelector(
                        currentTheme = themeMode,
                        onThemeSelected = viewModel::setThemeMode
                    )
                }
                
                // 2. Mood (Energy) Section
                GlassySection(title = stringResource(R.string.section_mood)) {
                    MoodSelector(
                        currentMood = moodColor,
                        onMoodSelected = viewModel::setMoodColor,
                        onShowColorPicker = { showColorPicker = true }
                    )
                }

                // 3. Readability Section
                GlassySection(title = stringResource(R.string.section_readability)) {
                    val currentFontFamily by viewModel.fontFamily.collectAsState()
                    
                    FontSelectorRow(
                        currentFont = currentFontFamily,
                        onFontSelected = viewModel::setFontFamily
                    )
                    
                    SwitchSettingRow(
                        label = stringResource(R.string.lbl_bold_text),
                        checked = isBoldText,
                        onCheckedChange = viewModel::setBoldText
                    )
                    
                    val currentLanguageCode by viewModel.currentLanguageCode.collectAsState()
                    val currentLanguageLabel = when {
                        currentLanguageCode.startsWith("es") -> stringResource(R.string.lang_spanish)
                        currentLanguageCode.startsWith("en") -> stringResource(R.string.lang_english)
                        else -> stringResource(R.string.lang_system_default)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguagePicker = true }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.lbl_app_language),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentLanguageLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp).rotate(180f)
                            )
                        }
                    }
                }
                
                // 4. Immersion Section (Shadow & Blur)
                GlassySection(title = stringResource(R.string.section_immersion)) {
                     // Blur Control
                     SwitchSettingRow(
                         label = stringResource(R.string.lbl_glass_effects),
                         subtitle = stringResource(R.string.lbl_android_12_plus),
                         checked = blurEnabled,
                         onCheckedChange = viewModel::setBlurEnabled
                     )
        
                     AnimatedVisibility(
                         visible = blurEnabled,
                         enter = expandVertically() + fadeIn(),
                         exit = shrinkVertically() + fadeOut()
                     ) {
                         Column {
                             SliderSettingRow(
                                 label = stringResource(R.string.lbl_glass_blur),
                                 value = blurStrength,
                                 valueRange = 5f..30f,
                                 onValueChange = viewModel::setBlurStrength
                             )
                             
                             SliderSettingRow(
                                 label = stringResource(R.string.lbl_tint_opacity),
                                 value = blurTintAlpha,
                                 valueRange = 0f..0.5f,
                                 onValueChange = viewModel::setBlurTintAlpha
                             )
                         }
                     }
                     
                     // Shadow Control
                     SwitchSettingRow(
                         label = "Shadows",
                         checked = shadowsEnabled,
                         onCheckedChange = viewModel::setShadowsEnabled
                     )
                     
                     AnimatedVisibility(
                         visible = shadowsEnabled,
                         enter = expandVertically() + fadeIn(),
                         exit = shrinkVertically() + fadeOut()
                     ) {
                          SliderSettingRow(
                             label = stringResource(R.string.lbl_shadows),
                             value = shadowStrength,
                             valueRange = 0f..1f,
                             onValueChange = viewModel::setShadowStrength
                         )
                     }
                }

    
                Spacer(modifier = Modifier.height(100.dp))
            }
            
            // Floating Glassy Top Bar
            com.nami.peace.ui.components.GlassyTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.title_atmosphere),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                modifier = Modifier.align(Alignment.TopCenter),
                hazeState = effectiveHazeState,
                blurEnabled = blurEnabled,
                blurStrength = blurStrength,
                blurTintAlpha = blurTintAlpha,
                shadowsEnabled = shadowsEnabled,
                shadowStyle = shadowStyle
            )
            
                val initialColor = try {
                    if (moodColor.startsWith("#")) Color(android.graphics.Color.parseColor(moodColor)) else Color(0xFF42A5F5)
                } catch (e: Exception) {
                    Color(0xFF42A5F5)
                }
                
                ColorPickerDialog(
                    show = showColorPicker,
                    initialColor = initialColor,
                    onColorSelected = { color ->
                        val hexBox = "#" + Integer.toHexString(color.toArgb()).uppercase()
                        viewModel.setMoodColor(hexBox)
                        showColorPicker = false
                    },
                    onDismissRequest = { showColorPicker = false },
                    hazeState = effectiveHazeState,
                    blurEnabled = blurEnabled,
                    blurStrength = blurStrength,
                    blurTintAlpha = blurTintAlpha
                )
                
                // Language Picker
                val currentLangCode by viewModel.currentLanguageCode.collectAsState()
                LanguagePickerDialog(
                    show = showLanguagePicker,
                    currentLanguageCode = currentLangCode,
                    onLanguageSelected = { 
                        viewModel.setLanguage(it)
                        showLanguagePicker = false
                    },
                    onDismissRequest = { showLanguagePicker = false },
                    hazeState = effectiveHazeState
                )
        }
    }
}

// -----------------------------------------------------------------------------
// Sub-Components
// -----------------------------------------------------------------------------

@Composable
fun GlassySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f))
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun ThemeSelector(currentTheme: String, onThemeSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        VisualThemeCard(
            label = stringResource(R.string.theme_auto),
            mode = "Auto",
            isSelected = currentTheme == "Auto",
            onClick = { onThemeSelected("Auto") },
            modifier = Modifier.weight(1f)
        )
        VisualThemeCard(
            label = stringResource(R.string.theme_light),
            mode = "Light",
            isSelected = currentTheme == "Light",
            onClick = { onThemeSelected("Light") },
            modifier = Modifier.weight(1f)
        )
        VisualThemeCard(
            label = stringResource(R.string.theme_dark),
            mode = "Dark",
            isSelected = currentTheme == "Dark",
            onClick = { onThemeSelected("Dark") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun VisualThemeCard(
    label: String,
    mode: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = when(mode) {
        "Light" -> Color(0xFFFAFAFA) // Fixed Light
        "Dark" -> Color(0xFF1E1E1E) // Fixed Dark
        else -> MaterialTheme.colorScheme.surface // Follow System/App
    }
    val contentColor = when(mode) {
        "Light" -> Color.Black
        "Dark" -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor)
                .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Mini UI Representation
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Mini App Bar
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(contentColor.copy(alpha = 0.2f))
                )
                // Mini Content Lines
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.height(4.dp).fillMaxWidth(0.6f).clip(RoundedCornerShape(2.dp)).background(contentColor.copy(alpha = 0.1f)))
                    Box(modifier = Modifier.height(4.dp).fillMaxWidth(0.8f).clip(RoundedCornerShape(2.dp)).background(contentColor.copy(alpha = 0.1f)))
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MoodSelector(
    currentMood: String, 
    onMoodSelected: (String) -> Unit,
    onShowColorPicker: () -> Unit
) {
    // State logic moved to parent

    val presets = listOf(
         "Default" to Color(0xFF42A5F5), 
         "Forest" to Color(0xFF66BB6A), 
         "Sunset" to Color(0xFFEF5350),
         "Lavender" to Color(0xFFAB47BC)
    )
     
    // Check if current mood is custom
    val isCustom = currentMood.startsWith("#")
    val customColor = if (isCustom) {
        try {
            Color(android.graphics.Color.parseColor(currentMood))
        } catch (e: Exception) {
            null
        }
    } else null

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), // Reduced padding to fit more items
        horizontalArrangement = Arrangement.Center
    ) {
         // Presets
         presets.forEach { (name, color) -> 
             Column(
                 horizontalAlignment = Alignment.CenterHorizontally,
                 modifier = Modifier.padding(horizontal = 8.dp)
             ) {
                 Box(
                     modifier = Modifier
                         .size(48.dp)
                         .clip(CircleShape)
                         .background(color)
                         .clickable { onMoodSelected(name) }
                         .then(if (currentMood == name) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier),
                     contentAlignment = Alignment.Center
                 ) {
                     if (currentMood == name) {
                         Icon(Icons.Default.Check, null, tint = Color.White)
                     }
                 }
                 Spacer(modifier = Modifier.height(4.dp))
                 Text(
                     text = name,
                     style = MaterialTheme.typography.labelSmall,
                     color = if (currentMood == name) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                 )
             }
         }
         
         // Custom Button
         Column(
             horizontalAlignment = Alignment.CenterHorizontally,
             modifier = Modifier.padding(horizontal = 8.dp)
         ) {
             Box(
                 modifier = Modifier
                     .size(48.dp)
                     .clip(CircleShape)
                     .background(customColor ?: MaterialTheme.colorScheme.surfaceContainerHigh) // Show custom color if active
                     .clickable { onShowColorPicker() }
                     .then(if (isCustom) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier),
                 contentAlignment = Alignment.Center
             ) {
                 if (isCustom) {
                      Icon(Icons.Default.Check, null, tint = Color.White)
                 } else {
                      // Plus Icon
                      Icon(
                          imageVector = androidx.compose.material.icons.Icons.Default.Add,
                          contentDescription = "Custom Color",
                          tint = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                 }
             }
             Spacer(modifier = Modifier.height(4.dp))
             Text(
                 text = if (isCustom) stringResource(R.string.lbl_custom) else stringResource(R.string.lbl_new),
                 style = MaterialTheme.typography.labelSmall,
                 color = if (isCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
             )
         }
    }
}

@Composable
fun SwitchSettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconRes: Int = 0,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        if (imageVector != null) {
             Icon(
                 imageVector = imageVector,
                 contentDescription = null,
                 tint = MaterialTheme.colorScheme.primary,
                 modifier = Modifier.padding(end = 16.dp)
             )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SliderSettingRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            // Just display value for debugging or context if needed, or remove details for cleaner look
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
fun PreviewCard() {
    // Optional: Could be removed if VisualThemeCards provide enough preview
}


// Helper for modifiers
fun Modifier.contentPadding(padding: PaddingValues): Modifier {
    return this.padding(padding)
}

const val NULL_ICON = 0
