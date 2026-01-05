package com.vola.app.ui.screens.goal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vola.app.ui.components.indicators.ProgressBar
import com.vola.app.ui.theme.*
import com.vola.app.viewmodels.GoalViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onBack: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Goals") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showCreateDialog(true) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Goal")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog(true) },
                containerColor = PrimaryGreen,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Goals Overview Card
            GoalsOverviewCard(
                totalGoals = state.totalGoals,
                activeGoals = state.activeGoals,
                completedGoals = state.completedGoals,
                totalSaved = state.totalSaved,
                totalTarget = state.totalTarget,
                overallProgress = state.overallProgress,
                modifier = Modifier.padding(16.dp)
            )
            
            // Filters
            GoalFilters(
                filterActive = state.filterActive,
                filterCompleted = state.filterCompleted,
                filterPinned = state.filterPinned,
                onActiveChanged = { viewModel.setFilterActive(it) },
                onCompletedChanged = { viewModel.setFilterCompleted(it) },
                onPinnedChanged = { viewModel.setFilterPinned(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Goals List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.goals.isEmpty()) {
                    item {
                        EmptyGoalsState(
                            onAddGoal = { viewModel.showCreateDialog(true) },
                            onGenerateSuggestions = { viewModel.generateGoalSuggestions() }
                        )
                    }
                } else {
                    items(state.goalProgresses) { goalProgress ->
                        GoalItem(
                            goalProgress = goalProgress,
                            onEdit = { viewModel.selectGoal(goalProgress.goal) },
                            onDelete = {
                                viewModel.selectGoal(goalProgress.goal)
                                viewModel.showDeleteDialog(true)
                            },
                            onAddFunds = {
                                viewModel.selectGoal(goalProgress.goal)
                                // Show add funds dialog
                            }
                        )
                    }
                }
                
                // Goal Suggestions
                if (state.goalSuggestions.isNotEmpty()) {
                    item {
                        GoalSuggestionsCard(
                            suggestions = state.goalSuggestions,
                            onCreateGoal = { goal ->
                                // Create goal from suggestion
                                viewModel.showCreateDialog(false)
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Create Goal Dialog
    if (state.showCreateDialog) {
        CreateGoalDialog(
            onDismiss = { viewModel.showCreateDialog(false) },
            onCreate = { goal ->
                // Create goal
                viewModel.showCreateDialog(false)
            }
        )
    }
    
    // Delete Goal Dialog
    if (state.showDeleteDialog && state.selectedGoal != null) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteDialog(false) },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Delete goal
                        viewModel.showDeleteDialog(false)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.showDeleteDialog(false) }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Celebration Dialog
    if (state.showCelebration && state.completedGoal != null) {
        CelebrationDialog(
            goal = state.completedGoal!!,
            onDismiss = { viewModel.showCelebration(false) }
        )
    }
}

@Composable
private fun GoalsOverviewCard(
    totalGoals: Int,
    activeGoals: Int,
    completedGoals: Int,
    totalSaved: Double,
    totalTarget: Double,
    overallProgress: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Goals Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            ProgressBar(
                current = totalSaved,
                total = totalTarget,
                label = "Overall Progress",
                showAmounts = false,
                showPercentage = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GoalStatItem(
                    label = "Total",
                    value = totalGoals.toString(),
                    icon = Icons.Default.Flag
                )
                GoalStatItem(
                    label = "Active",
                    value = activeGoals.toString(),
                    icon = Icons.Default.TrendingUp
                )
                GoalStatItem(
                    label = "Completed",
                    value = completedGoals.toString(),
                    icon = Icons.Default.CheckCircle
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Saved",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = formatMGA(totalSaved),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Target Amount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = formatMGA(totalTarget),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
            }
        }
    }
}

@Composable
private fun GoalStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun GoalFilters(
    filterActive: Boolean,
    filterCompleted: Boolean,
    filterPinned: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    onCompletedChanged: (Boolean) -> Unit,
    onPinnedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = filterActive,
            onClick = { onActiveChanged(!filterActive) },
            label = { Text("Active") },
            leadingIcon = if (filterActive) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
        
        FilterChip(
            selected = filterCompleted,
            onClick = { onCompletedChanged(!filterCompleted) },
            label = { Text("Completed") },
            leadingIcon = if (filterCompleted) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
        
        FilterChip(
            selected = filterPinned,
            onClick = { onPinnedChanged(!filterPinned) },
            label = { Text("Pinned") },
            leadingIcon = if (filterPinned) {
                {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
    }
}

@Composable
private fun GoalItem(
    goalProgress: com.vola.app.data.models.GoalProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddFunds: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(android.graphics.Color.parseColor(goalProgress.goal.color)),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = goalProgress.goal.icon,
                            fontSize = 24.sp
                        )
                    }
                    
                    Column {
                        Text(
                            text = goalProgress.goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${goalProgress.monthsRemaining} months remaining",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Actions Menu
                DropdownMenu(
                    expanded = false,
                    onDismissRequest = { /* Handle menu dismiss */ }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = onEdit,
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add Funds") },
                        onClick = onAddFunds,
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = onDelete,
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
            
            ProgressBar(
                current = goalProgress.goal.currentAmount,
                total = goalProgress.goal.targetAmount,
                label = "${goalProgress.progressPercentage.toInt()}% complete",
                showAmounts = true,
                showPercentage = false,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Monthly Contribution",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatMGA(goalProgress.monthlyRequired),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Target Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = goalProgress.projectedCompletion.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Milestones
            if (goalProgress.milestones.isNotEmpty()) {
                MilestoneRow(milestones = goalProgress.milestones)
            }
        }
    }
}

@Composable
private fun MilestoneRow(
    milestones: List<com.vola.app.data.models.Milestone>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        milestones.forEach { milestone ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (milestone.achieved) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (milestone.achieved) SuccessGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${milestone.percentage.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (milestone.achieved) SuccessGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EmptyGoalsState(
    onAddGoal: () -> Unit,
    onGenerateSuggestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            
            Text(
                text = "No Goals Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Create a financial goal to start saving for your dreams",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Button(
                onClick = onAddGoal,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Goal")
            }
            
            TextButton(
                onClick = onGenerateSuggestions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Suggestions")
            }
        }
    }
}

@Composable
private fun GoalSuggestionsCard(
    suggestions: List<com.vola.app.data.models.Goal>,
    onCreateGoal: (com.vola.app.data.models.Goal) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryGreen.copy(alpha = 0.1f)
        ),
        border = CardDefaults.outlinedCardBorder.copy(
            color = PrimaryGreen.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = PrimaryGreen
                )
                Text(
                    text = "Goal Suggestions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen
                )
            }
            
            suggestions.forEach { suggestion ->
                GoalSuggestionItem(
                    goal = suggestion,
                    onCreate = { onCreateGoal(suggestion) }
                )
            }
        }
    }
}

@Composable
private fun GoalSuggestionItem(
    goal: com.vola.app.data.models.Goal,
    onCreate: () -> Unit
) {
    Card(
        onClick = onCreate,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(android.graphics.Color.parseColor(goal.color)),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = goal.icon,
                    fontSize = 20.sp
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatMGA(goal.targetAmount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Goal",
                tint = PrimaryGreen
            )
        }
    }
}

@Composable
private fun CreateGoalDialog(
    onDismiss: () -> Unit,
    onCreate: (com.vola.app.data.models.Goal) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Goal") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* Handle name change */ },
                    label = { Text("Goal Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* Handle target amount change */ },
                    label = { Text("Target Amount (MGA)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Text("MGA")
                    }
                )
                
                // Add more fields as needed
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Create and save goal
                    onDismiss()
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CelebrationDialog(
    goal: com.vola.app.data.models.Goal,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = WarningYellow
            )
        },
        title = { Text("Congratulations! 🎉") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "You've achieved your goal!",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Text(
                    text = "You saved ${formatMGA(goal.targetAmount)}!",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Celebrate!")
            }
        }
    )
}

private fun formatMGA(amount: Double): String {
    return String.format("%.0f", amount) + " MGA"
}