package com.nami.peace.ui.deeplink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.nami.peace.MainActivity
import com.nami.peace.R
import com.nami.peace.domain.usecase.ImportReminderUseCase
import com.nami.peace.ui.theme.PeaceTheme
import com.nami.peace.util.deeplink.DeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity that handles deep link intents for reminder sharing.
 * 
 * This activity:
 * - Receives deep link intents with the format: peace://share?data=<encoded_data>
 * - Parses and validates the deep link data
 * - Imports the reminder into the database
 * - Navigates to the main app
 * 
 * Requirements: 9.3, 9.4
 */
@AndroidEntryPoint
class DeepLinkActivity : ComponentActivity() {
    
    @Inject
    lateinit var deepLinkHandler: DeepLinkHandler
    
    @Inject
    lateinit var importReminderUseCase: ImportReminderUseCase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the intent data
        val uri = intent?.data
        
        if (uri == null) {
            // No URI provided, show error and close
            showErrorAndFinish(getString(R.string.deep_link_error_no_data))
            return
        }
        
        // Validate the deep link format
        if (!deepLinkHandler.isValidDeepLink(uri)) {
            showErrorAndFinish(getString(R.string.deep_link_error_invalid_format))
            return
        }
        
        // Show loading UI while processing
        setContent {
            PeaceTheme {
                DeepLinkLoadingScreen()
            }
        }
        
        // Process the deep link
        lifecycleScope.launch {
            try {
                // Parse the deep link
                val reminder = deepLinkHandler.parseShareLink(uri)
                
                if (reminder == null) {
                    showErrorAndFinish(getString(R.string.deep_link_error_invalid_data))
                    return@launch
                }
                
                // Import the reminder
                val reminderId = importReminderUseCase(reminder)
                
                // Show success message
                Toast.makeText(
                    this@DeepLinkActivity,
                    getString(R.string.deep_link_success, reminder.title),
                    Toast.LENGTH_LONG
                ).show()
                
                // Navigate to the main app and show the imported reminder
                val mainIntent = Intent(this@DeepLinkActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("IMPORTED_REMINDER_ID", reminderId.toInt())
                }
                startActivity(mainIntent)
                finish()
                
            } catch (e: IllegalArgumentException) {
                // Validation error
                showErrorAndFinish(getString(R.string.deep_link_error_validation, e.message ?: "Unknown error"))
            } catch (e: Exception) {
                // General error
                showErrorAndFinish(getString(R.string.deep_link_error_general))
            }
        }
    }
    
    /**
     * Shows an error message and finishes the activity.
     */
    private fun showErrorAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        // Navigate to main app anyway
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(mainIntent)
        finish()
    }
}

/**
 * Loading screen shown while processing the deep link.
 */
@Composable
fun DeepLinkLoadingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.deep_link_loading),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
