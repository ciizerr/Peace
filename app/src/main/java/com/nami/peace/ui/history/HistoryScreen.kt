package com.nami.peace.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.data.local.HistoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.nami.peace.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateUp: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val iconManager: IconManager = remember { IoniconsManager(context) }
    val historyList by viewModel.history.collectAsState()
    var itemToDelete by remember { mutableStateOf<HistoryEntity?>(null) }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.delete_history_title)) },
            text = { Text(stringResource(R.string.delete_history_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let { viewModel.deleteHistoryItem(it) }
                        itemToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_log)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        PeaceIcon(
                            iconName = "arrow_back",
                            contentDescription = stringResource(R.string.cd_back),
                            iconManager = iconManager
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_history_yet),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyList) { item ->
                    HistoryItem(
                        item = item,
                        onClick = { onNavigateToDetail(item.id) },
                        onDelete = { itemToDelete = item },
                        iconManager = iconManager
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    item: HistoryEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    iconManager: IconManager
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.originalTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.completed_format, formatDateTime(item.completedTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDelete) {
                PeaceIcon(
                    iconName = "trash",
                    contentDescription = stringResource(R.string.cd_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconManager = iconManager
                )
            }
        }
    }
}

private fun formatDateTime(millis: Long): String {
    val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}
