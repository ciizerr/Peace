package com.nami.peace.ui.settings.identity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nami.peace.R
import com.nami.peace.ui.profile.ProfileSheet
import com.nami.peace.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import com.nami.peace.ui.settings.components.*
import com.nami.peace.ui.theme.GlassyBlack
import com.nami.peace.ui.theme.GlassyWhite
import androidx.compose.ui.graphics.luminance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    hazeState: HazeState? = null,
    sheetHazeState: HazeState? = null
) {
    val scrollState = rememberScrollState()
    val state = hazeState ?: remember { HazeState() }
    
    val userProfile by viewModel.userProfile.collectAsState(initial = com.nami.peace.data.repository.UserProfile())
    var showProfileSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state)
                    .verticalScroll(scrollState)
                    .padding(top = padding.calculateTopPadding() + 80.dp, bottom = padding.calculateBottomPadding() + 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- 1. HERO SECTION ---
                Spacer(modifier = Modifier.height(24.dp))
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(4.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
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
                             modifier = Modifier.size(72.dp),
                             tint = MaterialTheme.colorScheme.onSurfaceVariant
                         )
                     }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name
                Text(
                    text = userProfile.name.ifEmpty { stringResource(R.string.title_your_profile) },
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Occupation Badge
                if (userProfile.occupation.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = CircleShape,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = userProfile.occupation,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))

                // --- 2. ABOUT ME SECTION ---
                GlassySettingSection(title = "About Me") {
                    Text(
                        text = userProfile.bio.ifEmpty { "No bio provided yet. Tap 'Edit Profile' to add a little about yourself." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                // --- 3. PROFILE MANAGEMENT SECTION ---
                GlassySettingSection(title = "Management") {
                    GlassyButtonRow(
                        title = "Edit Profile",
                        subtitle = "Update your photo, name, and bio",
                        icon = Icons.Default.Edit,
                        onClick = { showProfileSheet = true }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Floating Glassy Top Bar
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
            
            // Profile Sheet
            if (showProfileSheet) {
                val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                
                ModalBottomSheet(
                    onDismissRequest = { showProfileSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.Transparent,
                    scrimColor = Color.Transparent,
                    dragHandle = null
                ) {
                      val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                      val borderColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
                      
                      Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderColor, shape)
                            .background(Color.Transparent)
                             .hazeChild(
                                 state = sheetHazeState ?: state,
                                 shape = shape,
                                 style = dev.chrisbanes.haze.HazeStyle(
                                     blurRadius = 15.dp, 
                                     tint = if (isDark) GlassyBlack.copy(alpha = 0.5f) else GlassyWhite.copy(alpha = 0.5f)
                                 )
                             )
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
}
