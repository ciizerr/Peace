package com.nami.peace.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nami.peace.ui.PeaceViewModel
import com.nami.peace.ui.components.PeaceButton
import com.nami.peace.ui.components.PeaceChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: PeaceViewModel
) {
    val userName by viewModel.userName.collectAsState()
    val wakeUpTime by viewModel.wakeUpTime.collectAsState()
    val bedTime by viewModel.bedTime.collectAsState()
    val focusAreas by viewModel.focusAreas.collectAsState()

    var nameInput by remember { mutableStateOf(userName) }
    var wakeUpInput by remember { mutableStateOf(wakeUpTime) }
    var bedTimeInput by remember { mutableStateOf(bedTime) }

    val availableFocusAreas = listOf("Better Sleep", "Productivity", "Physical Health", "Mental Peace", "Hydration")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Personal Details", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Daily Rhythm", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = wakeUpInput,
                onValueChange = { wakeUpInput = it },
                label = { Text("Wake Up Time (HH:mm)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = bedTimeInput,
                onValueChange = { bedTimeInput = it },
                label = { Text("Bed Time (HH:mm)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Focus Areas", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Simple FlowRow alternative for chips
            Column {
                availableFocusAreas.chunked(3).forEach { rowItems ->
                    androidx.compose.foundation.layout.Row(modifier = Modifier.padding(bottom = 8.dp)) {
                        rowItems.forEach { area ->
                            val isSelected = focusAreas.contains(area)
                            PeaceChip(
                                text = area,
                                color = MaterialTheme.colorScheme.primary,
                                selected = isSelected,
                                onClick = {
                                    val newSet = if (isSelected) {
                                        focusAreas - area
                                    } else {
                                        focusAreas + area
                                    }
                                    viewModel.saveFocusAreas(newSet)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            PeaceButton(
                onClick = {
                    viewModel.saveUserName(nameInput)
                    viewModel.saveWakeUpTime(wakeUpInput)
                    viewModel.saveBedTime(bedTimeInput)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }
        }
    }
}
