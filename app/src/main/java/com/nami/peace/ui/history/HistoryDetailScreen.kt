package com.nami.peace.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
    val historyItem by viewModel.getHistoryItem(historyId).collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(androidx.compose.ui.res.stringResource(com.nami.peace.R.string.history_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = androidx.compose.ui.res.stringResource(com.nami.peace.R.string.back))
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
        DetailRow(androidx.compose.ui.res.stringResource(com.nami.peace.R.string.history_label_completed), formatDate(item.completedTime))
        DetailRow(androidx.compose.ui.res.stringResource(com.nami.peace.R.string.history_label_priority), item.priority.name)
        DetailRow(androidx.compose.ui.res.stringResource(com.nami.peace.R.string.history_label_category), item.category.name)
        
        item.nagInfo?.let {
            DetailRow(androidx.compose.ui.res.stringResource(com.nami.peace.R.string.history_label_nag_stats), it)
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
