package com.nami.peace.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.LocalContext
import com.nami.peace.R
import com.nami.peace.ui.widget.QuickAddActivity

/**
 * Main content composable for the Quick-Add widget.
 * Provides quick reminder creation functionality.
 * Implements Requirements 17.6, 17.7, 17.8
 */
@Composable
fun QuickAddWidgetContent() {
    val context = LocalContext.current
    
    // Intent to open Quick-Add activity
    val intent = Intent(context, QuickAddActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(16.dp)
            .cornerRadius(16.dp)
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_ionicons_add_circle),
                contentDescription = "Add",
                modifier = GlanceModifier.size(24.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "Quick Add",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // Input placeholder (tap to open activity)
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(8.dp)
                .padding(12.dp)
                .clickable(actionStartActivity(intent)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_ionicons_create),
                contentDescription = "Create reminder",
                modifier = GlanceModifier.size(32.dp)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Tap to add a reminder",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(12.dp))
        
        // Quick tips
        Text(
            text = "Try: \"Buy milk at 5pm\" or \"Meeting tomorrow 2pm\"",
            style = TextStyle(
                fontSize = 11.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
    }
}
