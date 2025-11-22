package com.nami.peace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.rememberLauncherForActivityResult
import com.nami.peace.ui.SettingsScreen
import com.nami.peace.ui.SplashScreen
import com.nami.peace.ui.home.HomeScreen
import com.nami.peace.ui.navigation.PeaceBottomBar
import com.nami.peace.ui.navigation.Screen
import com.nami.peace.ui.theme.PeaceTheme
import com.nami.peace.ui.home.UpcomingScreen

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: com.nami.peace.ui.PeaceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val isDarkMode = viewModel.isDarkMode.collectAsState(initial = true)
            val themeAccent = viewModel.themeAccent.collectAsState(initial = "Purple")
            val navController = rememberNavController()

            val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
            var showSheet by remember { mutableStateOf(false) }
            val scope = androidx.compose.runtime.rememberCoroutineScope()

            // Permission Request Logic
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    // Handle permission result if needed
                }
            )

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            PeaceTheme(
                darkTheme = isDarkMode.value,
                themeAccent = themeAccent.value
            ) {
                Scaffold(
                    bottomBar = {
                        PeaceBottomBar(navController = navController)
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(navController = navController)
                        }
                        composable(Screen.Home.route) {
                            HomeScreen(
                                navController = navController,
                                viewModel = viewModel,
                                onFabClick = { 
                                    viewModel.resetReminderState()
                                    showSheet = true 
                                }
                            )
                        }
                        composable("upcoming") { // Matches BottomNavItem.Upcoming.route
                            UpcomingScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(navController = navController, viewModel = viewModel)
                        }
                        composable(Screen.Profile.route) {
                            com.nami.peace.ui.settings.ProfileScreen(navController = navController, viewModel = viewModel)
                        }
                    }

                    if (showSheet) {
                        androidx.compose.material3.ModalBottomSheet(
                            onDismissRequest = { showSheet = false },
                            sheetState = sheetState
                        ) {
                            com.nami.peace.ui.AddReminderSheet(
                                viewModel = viewModel,
                                onDismiss = {
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            showSheet = false
                                            viewModel.resetReminderState()
                                        }
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