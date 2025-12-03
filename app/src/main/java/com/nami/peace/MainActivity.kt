package com.nami.peace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.nami.peace.ui.theme.PeaceTheme
import dagger.hilt.android.AndroidEntryPoint

import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set window background to white immediately
        window.setBackgroundDrawableResource(android.R.color.white)
        
        setContent {
            PeaceTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            com.nami.peace.ui.home.HomeScreen(
                                onAddReminder = { navController.navigate("add_edit") },
                                onEditReminder = { id -> navController.navigate("detail/$id") },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToPeaceGarden = { navController.navigate("peace_garden") },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToFocus = { }
                            )
                        }
                        composable("add_edit") {
                            com.nami.peace.ui.reminder.AddEditReminderScreen(
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "detail/{reminderId}",
                            arguments = listOf(
                                androidx.navigation.navArgument("reminderId") {
                                    type = androidx.navigation.NavType.IntType
                                }
                            )
                        ) {
                            com.nami.peace.ui.reminder.ReminderDetailScreen(
                                onNavigateUp = { navController.popBackStack() },
                                onEditReminder = { id -> navController.navigate("add_edit?reminderId=$id") }
                            )
                        }
                        composable("settings") {
                            com.nami.peace.ui.settings.SettingsScreen(
                                onNavigateUp = { navController.popBackStack() },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToFontSettings = { },
                                onNavigateToBackgroundSettings = { },
                                onNavigateToLanguageSettings = { },
                                onNavigateToCalendarSync = { },
                                onNavigateToPeaceGarden = { navController.navigate("peace_garden") },
                                onNavigateToMLSuggestions = { },
                                onNavigateToFeatureSettings = { }
                            )
                        }
                        composable("peace_garden") {
                            com.nami.peace.ui.garden.PeaceGardenScreen(
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                        composable("history") {
                            com.nami.peace.ui.history.HistoryScreen(
                                onNavigateUp = { navController.popBackStack() },
                                onNavigateToDetail = { }
                            )
                        }
                    }
                }
            }
        }
    }
}
