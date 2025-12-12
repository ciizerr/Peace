package com.nami.peace.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageView
import com.nami.peace.R
import com.nami.peace.ui.theme.PeaceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProfileCropActivity : AppCompatActivity() {

    private lateinit var cropImageView: CropImageView
    private lateinit var loadingSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_crop)

        val sourceUri = intent.data
        if (sourceUri == null) {
            finish()
            return
        }

        cropImageView = findViewById(R.id.cropImageView)
        loadingSpinner = findViewById(R.id.loadingSpinner)
        val controlBar = findViewById<ComposeView>(R.id.controlBar)

        // Setup Cropper
        cropImageView.apply {
            scaleType = CropImageView.ScaleType.FIT_CENTER
            cropShape = CropImageView.CropShape.OVAL
            isShowCropOverlay = true
            isAutoZoomEnabled = true
            setAspectRatio(1, 1)
            setFixedAspectRatio(true)
        }

        // Setup Controls
        controlBar.setContent {
            PeaceTheme {
                ProfileCropControls(
                    onRotateLeft = { cropImageView.rotateImage(-90) },
                    onRotateRight = { cropImageView.rotateImage(90) },
                    onFlipHorizontal = { cropImageView.flipImageHorizontally() },
                    onCancel = { finish() },
                    onSave = { saveCroppedImage() }
                )
            }
        }

        // Load Image Manually
        loadImage(sourceUri)
    }

    private fun loadImage(uri: Uri) {
        loadingSpinner.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        cropImageView.setImageBitmap(bitmap)
                    } else {
                        // Handle error (maybe show toast)
                    }
                    loadingSpinner.visibility = View.GONE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    loadingSpinner.visibility = View.GONE
                }
            }
        }
    }

    private fun saveCroppedImage() {
        loadingSpinner.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = cropImageView.getCroppedImage()
            if (bitmap != null) {
                val tempFile = File(cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg")
                try {
                    FileOutputStream(tempFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    val resultUri = Uri.fromFile(tempFile)
                    withContext(Dispatchers.Main) {
                        setResult(Activity.RESULT_OK, Intent().apply { data = resultUri })
                        finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                     withContext(Dispatchers.Main) {
                        loadingSpinner.visibility = View.GONE
                    }
                }
            } else {
                 withContext(Dispatchers.Main) {
                    loadingSpinner.visibility = View.GONE
                }
            }
        }
    }
}

@Composable
fun ProfileCropControls(
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
            .navigationBarsPadding()
    ) {
        // Tools Row
        Row(
           modifier = Modifier.fillMaxWidth(),
           horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onRotateLeft) {
                Icon(Icons.AutoMirrored.Filled.RotateLeft, "Rotate Left", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onRotateRight) {
                Icon(Icons.AutoMirrored.Filled.RotateRight, "Rotate Right", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onFlipHorizontal) {
                Icon(Icons.Default.Flip, "Flip Horizontal", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Actions Row
        Row(
           modifier = Modifier.fillMaxWidth(),
           horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        onSave()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Photo")
                }
            }
        }
    }
}
