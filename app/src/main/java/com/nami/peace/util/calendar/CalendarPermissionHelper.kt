package com.nami.peace.util.calendar

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes

@OptIn(ExperimentalPermissionsApi::class)

/**
 * Helper class for managing calendar permissions and Google Sign-In flow.
 */
class CalendarPermissionHelper(
    private val permissionsState: MultiplePermissionsState,
    private val signInLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val onSignInSuccess: () -> Unit,
    private val onSignInFailure: (Exception) -> Unit
) {
    
    /**
     * Check if all required permissions are granted.
     */
    fun hasAllPermissions(): Boolean {
        return permissionsState.allPermissionsGranted
    }
    
    /**
     * Request calendar permissions.
     */
    fun requestPermissions() {
        permissionsState.launchMultiplePermissionRequest()
    }
    
    /**
     * Launch Google Sign-In flow.
     */
    fun requestGoogleSignIn(activity: Activity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(CalendarScopes.CALENDAR))
            .build()
        
        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }
    
    /**
     * Handle the result of Google Sign-In.
     */
    fun handleSignInResult(result: ActivityResult) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(Exception::class.java)
            
            if (account != null) {
                onSignInSuccess()
            } else {
                onSignInFailure(Exception("Sign-in failed: No account returned"))
            }
        } catch (e: Exception) {
            onSignInFailure(e)
        }
    }
}

/**
 * Composable function to create a CalendarPermissionHelper.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberCalendarPermissionHelper(
    onSignInSuccess: () -> Unit,
    onSignInFailure: (Exception) -> Unit
): CalendarPermissionHelper {
    val context = LocalContext.current
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )
    )
    
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(Exception::class.java)
            
            if (account != null) {
                onSignInSuccess()
            } else {
                onSignInFailure(Exception("Sign-in failed: No account returned"))
            }
        } catch (e: Exception) {
            onSignInFailure(e)
        }
    }
    
    return remember(permissionsState, signInLauncher) {
        CalendarPermissionHelper(
            permissionsState = permissionsState,
            signInLauncher = signInLauncher,
            onSignInSuccess = onSignInSuccess,
            onSignInFailure = onSignInFailure
        )
    }
}
