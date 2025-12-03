package com.nami.peace.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        android.util.Log.d("SPLASH_DEBUG", "SplashScreen started")
        delay(2000)
        val isFirst = viewModel.isFirstLaunch.value
        android.util.Log.d("SPLASH_DEBUG", "isFirstLaunch = $isFirst")
        if (isFirst) {
            android.util.Log.d("SPLASH_DEBUG", "Navigating to onboarding")
            onNavigateToOnboarding()
        } else {
            android.util.Log.d("SPLASH_DEBUG", "Navigating to home")
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App icon from app_icon.jpg
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                contentDescription = "Peace App Icon",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            // App Name
            Text(
                text = "Peace",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF1A1A1A)
            )
            
            // Tagline
            Text(
                text = "Your calm companion",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
        }
    }
}
