package com.nami.peace.ui.suggestions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nami.peace.R
import com.nami.peace.data.local.SuggestionType
import com.nami.peace.domain.model.Suggestion
import com.nami.peace.ui.components.PeaceIcon
import com.nami.peace.util.icon.IconManager
import com.nami.peace.util.icon.IoniconsManager

/**
 * Screen displaying ML-generated suggestions for the user.
 * 
 * Features:
 * - Display pending suggestions with confidence scores
 * - Apply/dismiss buttons for each suggestion
 * - Detailed explanations for each suggestion
 * - Empty state when insufficient data
 * 
 * Requirements: 12.2, 12.9, 12.10, 12.11
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SuggestionsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val iconManager: IconManager = remember { IoniconsManager(context) }
    
    val suggestions by viewModel.suggestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ml_suggestions_title)) },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                suggestions.isEmpty() -> {
                    EmptyStateContent(iconManager = iconManager)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        item {
                            Text(
                                text = stringResource(R.string.ml_suggestions_header),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Suggestions
                        items(suggestions, key = { it.id }) { suggestion ->
                            SuggestionCard(
                                suggestion = suggestion,
                                isActionInProgress = actionInProgress == suggestion.id,
                                onApply = { viewModel.applySuggestion(suggestion) },
                                onDismiss = { viewModel.dismissSuggestion(suggestion) },
                                iconManager = iconManager
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty state displayed when there are no suggestions.
 * Shows when insufficient data is available for ML analysis.
 */
@Composable
private fun EmptyStateContent(
    iconManager: IconManager
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PeaceIcon(
            iconName = "bulb_outline",
            contentDescription = "",
            iconManager = iconManager,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.ml_suggestions_empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.ml_suggestions_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Card displaying a single suggestion with confidence score and actions.
 */
@Composable
private fun SuggestionCard(
    suggestion: Suggestion,
    isActionInProgress: Boolean,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    iconManager: IconManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header with icon and confidence score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    PeaceIcon(
                        iconName = getSuggestionIcon(suggestion.type),
                        contentDescription = "",
                        iconManager = iconManager,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = getSuggestionTypeLabel(suggestion.type),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                ConfidenceScoreBadge(score = suggestion.confidenceScore)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description/Explanation
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isActionInProgress
                ) {
                    Text(stringResource(R.string.ml_suggestions_dismiss))
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onApply,
                    enabled = !isActionInProgress
                ) {
                    if (isActionInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.ml_suggestions_apply))
                    }
                }
            }
        }
    }
}

/**
 * Badge displaying the confidence score as a percentage.
 */
@Composable
private fun ConfidenceScoreBadge(score: Int) {
    val color = when {
        score >= 80 -> MaterialTheme.colorScheme.tertiary
        score >= 60 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = "$score%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Get the icon name for a suggestion type.
 */
private fun getSuggestionIcon(type: SuggestionType): String {
    return when (type) {
        SuggestionType.OPTIMAL_TIME -> "time_outline"
        SuggestionType.PRIORITY_ADJUSTMENT -> "flag_outline"
        SuggestionType.RECURRING_PATTERN -> "repeat_outline"
        SuggestionType.BREAK_REMINDER -> "cafe_outline"
        SuggestionType.HABIT_FORMATION -> "fitness_outline"
        SuggestionType.TEMPLATE_CREATION -> "document_outline"
        SuggestionType.FOCUS_SESSION -> "eye_outline"
    }
}

/**
 * Get the display label for a suggestion type.
 */
@Composable
private fun getSuggestionTypeLabel(type: SuggestionType): String {
    return when (type) {
        SuggestionType.OPTIMAL_TIME -> stringResource(R.string.ml_suggestion_type_optimal_time)
        SuggestionType.PRIORITY_ADJUSTMENT -> stringResource(R.string.ml_suggestion_type_priority)
        SuggestionType.RECURRING_PATTERN -> stringResource(R.string.ml_suggestion_type_recurring)
        SuggestionType.BREAK_REMINDER -> stringResource(R.string.ml_suggestion_type_break)
        SuggestionType.HABIT_FORMATION -> stringResource(R.string.ml_suggestion_type_habit)
        SuggestionType.TEMPLATE_CREATION -> stringResource(R.string.ml_suggestion_type_template)
        SuggestionType.FOCUS_SESSION -> stringResource(R.string.ml_suggestion_type_focus)
    }
}
