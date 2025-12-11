package com.nami.peace.ui.main

import androidx.compose.foundation.shape.RoundedCornerShape

import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.nami.peace.ui.components.CategoryCarouselBar
import com.nami.peace.ui.components.FloatingBottomBar
import com.nami.peace.ui.components.MainTab
import com.nami.peace.ui.components.PlaceholderScreen
import com.nami.peace.ui.components.SettingsCategory
import com.nami.peace.ui.home.HomeScreen
import com.nami.peace.ui.settings.SettingsContent
import com.nami.peace.ui.alarm.AlarmsListScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    onAddReminder: () -> Unit,
    onEditReminder: (Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    settingsViewModel: com.nami.peace.ui.settings.SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { MainTab.values().size })
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    
    val blurEnabled by settingsViewModel.blurEnabled.collectAsState()
    val shadowsEnabled by settingsViewModel.shadowsEnabled.collectAsState()
    val shadowStyle by settingsViewModel.shadowStyle.collectAsState()
    val blurStrength by settingsViewModel.blurStrength.collectAsState()
    val blurTintAlpha by settingsViewModel.blurTintAlpha.collectAsState()
    
    // Bottom Bar Visibility Logic
    var isBottomBarVisible by remember { mutableStateOf(true) }
    var scrollAccumulator by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val scrollThreshold = 100f

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Reset accumulator if direction changes
                if ((available.y < 0 && scrollAccumulator > 0) || (available.y > 0 && scrollAccumulator < 0)) {
                   scrollAccumulator = 0f
                }
                
                scrollAccumulator += available.y

                if (scrollAccumulator < -scrollThreshold) {
                    isBottomBarVisible = false
                } else if (scrollAccumulator > scrollThreshold) {
                    isBottomBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // Settings Category State
    val settingsPagerState = rememberPagerState(pageCount = { SettingsCategory.values().size })
    val selectedCategory by remember { derivedStateOf { SettingsCategory.values()[settingsPagerState.currentPage] } }

    // Sync Main Tab Selection
    val selectedTab by remember { derivedStateOf { MainTab.values()[pagerState.currentPage] } }

    // Swipe Redirect: Settings -> Dashboard (Skip Tasks and Alarms)
    LaunchedEffect(pagerState.currentPage, pagerState.targetPage) {
        if (pagerState.currentPage == MainTab.Settings.ordinal && pagerState.targetPage == MainTab.Tasks.ordinal) {
             pagerState.scrollToPage(MainTab.Dashboard.ordinal)
        }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(nestedScrollConnection),
        bottomBar = {
            Box(contentAlignment = Alignment.BottomCenter) {
                // Main Bottom Bar
                FloatingBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        scope.launch { pagerState.animateScrollToPage(tab.ordinal, animationSpec = tween(300)) }
                    },
                    isVisible = isBottomBarVisible && selectedTab != MainTab.Settings,
                    hazeState = hazeState,
                    blurEnabled = blurEnabled,
                    blurStrength = blurStrength,
                    blurTintAlpha = blurTintAlpha,
                    shadowsEnabled = shadowsEnabled,
                    shadowStyle = shadowStyle
                )
                
                // Settings Category Carousel
                CategoryCarouselBar(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        scope.launch { settingsPagerState.animateScrollToPage(category.ordinal) }
                    },
                    isVisible = isBottomBarVisible && selectedTab == MainTab.Settings,
                    hazeState = hazeState,
                    blurEnabled = blurEnabled,
                    blurStrength = blurStrength,
                    blurTintAlpha = blurTintAlpha,
                    shadowsEnabled = shadowsEnabled,
                    shadowStyle = shadowStyle
                )
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 0.dp), // We handle padding internally or let content go behind
            userScrollEnabled = selectedTab != MainTab.Settings || settingsPagerState.currentPage == 0
            // If we are in Settings (and not on the first page), we want the swipe to control the Categories.
            // If on first page (Appearance), swipe back should go to Dashboard (via Redirect logic).
        ) { page ->
            val tab = MainTab.values()[page]
            
            Box(modifier = Modifier.fillMaxSize()) {
                when (tab) {
                    MainTab.Dashboard -> {
                        HomeScreen(
                            onAddReminder = onAddReminder,
                            onEditReminder = onEditReminder,
                            onProfileClick = { 
                                // TODO: Implement Profile Feature later
                            },
                            bottomPadding = 90.dp,
                            hazeState = hazeState,
                            blurEnabled = blurEnabled,
                            blurStrength = blurStrength,
                            blurTintAlpha = blurTintAlpha,
                            isFABVisible = isBottomBarVisible
                        )
                    }
                    MainTab.Alarms -> {
                        AlarmsListScreen(
                            hazeState = hazeState,
                            blurEnabled = blurEnabled,
                            blurStrength = blurStrength,
                            blurTintAlpha = blurTintAlpha,
                            onEditReminder = onEditReminder,
                            onAddReminder = onAddReminder,
                            isFABVisible = isBottomBarVisible
                        )
                    }
                    MainTab.Tasks -> {
                        PlaceholderScreen(
                            title = "Tasks",
                            subtitle = "Manage your daily tasks efficiently.",
                            onBack = { scope.launch { pagerState.animateScrollToPage(MainTab.Dashboard.ordinal) } }
                        )
                    }
                    MainTab.Settings -> {
                        // Settings Sub-Pager
                        HorizontalPager(
                            state = settingsPagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { categoryPage ->
                            SettingsContent(
                                category = SettingsCategory.values()[categoryPage],
                                onNavigateToHistory = onNavigateToHistory,
                                onNavigateToDashboard = {
                                    scope.launch { pagerState.animateScrollToPage(MainTab.Dashboard.ordinal) }
                                },
                                onNavigateToCategory = { category ->
                                    scope.launch { settingsPagerState.animateScrollToPage(category.ordinal) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
