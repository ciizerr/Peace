package com.nami.peace.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.data.local.HistoryEntity
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.ReminderCategory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    historyId: Int,
    onNavigateUp: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val iconManager: IconManager = remember { IoniconsManager(context) }
    val historyItem by viewModel.getHistoryItem(historyId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        PeaceIcon(
                            iconName = "arrow_back",
                            contentDescription = "Back",
                            iconManager = iconManager
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            historyItem?.let { item ->
                HistoryDetailContent(item)
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun HistoryDetailContent(item: HistoryEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Badge
        Surface(
            color = if (item.status == "Done") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = item.status.uppercase(),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (item.status == "Done") Color(0xFF2E7D32) else Color(0xFFC62828),
                fontWeight = FontWeight.Bold
            )
        }

        // Title
        Text(
            text = item.originalTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Divider()

        // Details Grid
        DetailRow("Completed On", formatDate(item.completedTime))
        DetailRow("Priority", item.priority.name)
        DetailRow("Category", item.category.name)
        
        item.nagInfo?.let {
            DetailRow("Nag Stats", it)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
