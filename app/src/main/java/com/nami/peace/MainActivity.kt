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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PeaceTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val startDestination = if (intent?.getBooleanExtra("NAVIGATE_TO_ALARM", false) == true) {
                        "alarm"
                    } else {
                        "home"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("home") {
                            com.nami.peace.ui.home.HomeScreen(
                                onAddReminder = { navController.navigate("add_edit") }
                            )
                        }
                        composable("add_edit") {
                            com.nami.peace.ui.reminder.AddEditReminderScreen(
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        composable("alarm") {
                            com.nami.peace.ui.alarm.AlarmScreen(
                                onFinish = {
                                    // If started from intent, finish activity. Else pop.
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
