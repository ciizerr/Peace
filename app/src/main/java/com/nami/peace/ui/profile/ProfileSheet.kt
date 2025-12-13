package com.nami.peace.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import java.io.File
import java.io.FileOutputStream
import android.net.Uri


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    userProfile: com.nami.peace.data.repository.UserProfile,
    onSave: (com.nami.peace.data.repository.UserProfile) -> Unit,
    onClose: () -> Unit
) {
    var name: String by remember { mutableStateOf(userProfile.name) }
    var bio: String by remember { mutableStateOf(userProfile.bio) }
    var occupation: String by remember { mutableStateOf(userProfile.occupation) }
    var wakeTime: String by remember { mutableStateOf(userProfile.wakeTime) }
    var bedTime: String by remember { mutableStateOf(userProfile.bedTime) }
    var photoUri: String? by remember { mutableStateOf(userProfile.photoUri) }

    val context = LocalContext.current

    // Launcher for picking image from gallery/camera
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val intent = android.content.Intent(context, ProfileCropActivity::class.java).apply {
                data = it
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // Launch custom cropper
            // We need a separate launcher for the result
            // Or we can use a single launcher if we define a refined contract, but simpler to just chain.
            // Wait, we need to launch the activity.
        }
    }

    // Launcher for Custom Crop Activity Result
    val cropActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
             result.data?.data?.let { uri ->
                 // Save to internal storage (logic from before)
                 val existingFile = File(context.filesDir, "profile_photo.jpg")
                 if (existingFile.exists()) existingFile.delete()
                 
                 val newFile = File(context.filesDir, "profile_photo_${System.currentTimeMillis()}.jpg")
                 try {
                     context.contentResolver.openInputStream(uri)?.use { inputStream ->
                         FileOutputStream(newFile).use { outputStream ->
                             inputStream.copyTo(outputStream)
                         }
                     }
                     photoUri = Uri.fromFile(newFile).toString()
                 } catch (e: Exception) {
                     e.printStackTrace()
                 }
             }
        }
    }
    
    // Intermediate launcher to pick then crop
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
             val intent = android.content.Intent(context, ProfileCropActivity::class.java).apply {
                data = uri
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            cropActivityLauncher.launch(intent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .imePadding(), // Handle keyboard
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        )

        Text(
            text = stringResource(com.nami.peace.R.string.title_your_profile),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Photo
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { 
                    imagePickerLauncher.launch("image/*")
                }
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = stringResource(com.nami.peace.R.string.cd_profile_photo),
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
            
            // Edit Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Scrollable Fields
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(com.nami.peace.R.string.lbl_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            
            Text(
                text = stringResource(com.nami.peace.R.string.lbl_profile_help),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text(stringResource(com.nami.peace.R.string.lbl_bio_optional)) },
                placeholder = { Text(stringResource(com.nami.peace.R.string.hint_bio)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            OutlinedTextField(
                value = occupation,
                onValueChange = { occupation = it },
                label = { Text(stringResource(com.nami.peace.R.string.lbl_occupation_optional)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = wakeTime,
                    onValueChange = { wakeTime = it },
                    label = { Text(stringResource(com.nami.peace.R.string.lbl_wake_time)) },
                    placeholder = { Text("07:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = bedTime,
                    onValueChange = { bedTime = it },
                    label = { Text(stringResource(com.nami.peace.R.string.lbl_bed_time)) },
                    placeholder = { Text("23:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(com.nami.peace.R.string.cancel))
            }

            Button(
                onClick = {
                    onSave(
                        userProfile.copy(
                            name = name,
                            photoUri = photoUri,
                            bio = bio,
                            occupation = occupation,
                            wakeTime = wakeTime,
                            bedTime = bedTime
                        )
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(com.nami.peace.R.string.btn_save_profile))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
