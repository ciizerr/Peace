package com.nami.peace.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.ui.theme.AccentRed
import com.nami.peace.ui.theme.White

@Composable
fun AlarmScreen(
    onFinish: () -> Unit,
    viewModel: AlarmViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.loadActiveReminders()
    }

    LaunchedEffect(uiState.shouldFinish) {
        if (uiState.shouldFinish) {
            onFinish()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Hero Section
        uiState.heroReminder?.let { hero ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(AccentRed)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.nami.peace.R.string.alarm_hero_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = hero.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = White
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.markDone(hero) },
                    colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = AccentRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.nami.peace.R.string.alarm_btn_done))
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { viewModel.snooze(hero) },
                    colors = ButtonDefaults.textButtonColors(contentColor = White.copy(alpha = 0.7f))
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.nami.peace.R.string.alarm_btn_snooze))
                }
            }
        }

        // Bundle Section
        if (uiState.bundledReminders.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().weight(0.5f),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.nami.peace.R.string.alarm_bundle_header),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn {
                        items(uiState.bundledReminders) { reminder ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(reminder.title, style = MaterialTheme.typography.bodyLarge)
                                    Text(reminder.priority.name, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
