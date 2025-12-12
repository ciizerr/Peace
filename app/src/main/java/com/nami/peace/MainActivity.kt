package com.nami.peace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.nami.peace.ui.theme.PeaceTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost

import androidx.activity.enableEdgeToEdge

import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontFamily

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var userPreferencesRepository: com.nami.peace.data.repository.UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request Notifications Permission (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val requestPermissionLauncher = registerForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    com.nami.peace.util.DebugLogger.log(getString(R.string.notification_permission_granted))
                } else {
                    com.nami.peace.util.DebugLogger.log(getString(R.string.notification_permission_denied))
                }
            }
            
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check Exact Alarm Permission (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.app.AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                com.nami.peace.util.DebugLogger.log(getString(R.string.exact_alarm_permission_missing))
            }
        }

        setContent {
            val fontFamilyName = userPreferencesRepository.fontFamily
                .collectAsState(initial = "System").value
            
            val fontFamily = when (fontFamilyName) {
                "Poppins" -> androidx.compose.ui.text.font.FontFamily(
                    androidx.compose.ui.text.font.Font(R.font.poppins_regular)
                )
                "Lato" -> androidx.compose.ui.text.font.FontFamily(
                    androidx.compose.ui.text.font.Font(R.font.lato_regular)
                )
                "Bodoni" -> androidx.compose.ui.text.font.FontFamily(
                    androidx.compose.ui.text.font.Font(R.font.bodoni_moda)
                )
                "Loves" -> androidx.compose.ui.text.font.FontFamily(
                    androidx.compose.ui.text.font.Font(R.font.loves)
                )
                "Serif" -> FontFamily.Serif
                "Monospace" -> FontFamily.Monospace
                "Cursive" -> FontFamily.Cursive
                else -> FontFamily.Default
            }

            val isBoldText = userPreferencesRepository.isBoldText
                .collectAsState(initial = false).value

            PeaceTheme(fontFamily = fontFamily, isBoldText = isBoldText) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val startDestination = if (intent?.getBooleanExtra("NAVIGATE_TO_ALARM", false) == true) {
                        "alarm"
                    } else {
                        "home"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("home") {
                            com.nami.peace.ui.main.MainScreen(
                                onAddReminder = { navController.navigate("add_edit") },
                                onEditReminder = { id -> navController.navigate("add_edit?reminderId=$id") }
                            )
                        }
                        composable(
                            route = "add_edit?reminderId={reminderId}",
                            arguments = listOf(
                                androidx.navigation.navArgument("reminderId") {
                                    type = androidx.navigation.NavType.IntType
                                    defaultValue = -1
                                }
                            )
                        ) {
                            com.nami.peace.ui.reminder.AddEditReminderScreen(
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        
                        composable("alarm") {
                            com.nami.peace.ui.alarm.AlarmScreen(
                                onFinish = {
                                    if (intent?.getBooleanExtra("NAVIGATE_TO_ALARM", false) == true) {
                                        finish()
                                    } else {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
