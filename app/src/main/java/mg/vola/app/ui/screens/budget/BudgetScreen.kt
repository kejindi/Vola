package com.vola.app.ui.screens.budget

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
import com.vola.app.data.models.Budget
import com.vola.app.data.models.BudgetPeriod
import com.vola.app.ui.components.indicators.ProgressBar
import com.vola.app.ui.theme.*
import com.vola.app.viewmodels.BudgetViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Budget") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showCreateDialog(true) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Budget")
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
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Budget Overview Card
            BudgetOverviewCard(
                totalBudget = state.totalBudget,
                totalSpent = state.totalSpent,
                totalRemaining = state.totalRemaining,
                overallPercentage = state.overallPercentage,
                modifier = Modifier.padding(16.dp)
            )
            
            // Budget Period Selector
            BudgetPeriodSelector(
                selectedPeriod = state.selectedPeriod,
                onPeriodSelected = { viewModel.setSelectedPeriod(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Budget List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.budgetStatuses.isEmpty()) {
                    item {
                        EmptyBudgetState(
                            onAddBudget = { viewModel.showCreateDialog(true) },
                            onGenerateSuggestions = { viewModel.generateBudgetSuggestions() }
                        )
                    }
                } else {
                    items(state.budgetStatuses) { budgetStatus ->
                        BudgetItem(
                            budgetStatus = budgetStatus,
                            onEdit = { viewModel.selectBudget(budgetStatus.budget) },
                            onDelete = {
                                viewModel.selectBudget(budgetStatus.budget)
                                viewModel.showDeleteDialog(true)
                            }
                        )
                    }
                }
                
                // Budget Tips
                if (state.budgetStatuses.isNotEmpty()) {
                    item {
                        BudgetTipsCard(
                            insights = viewModel.getBudgetInsights(),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Create Budget Dialog
    if (state.showCreateDialog) {
        CreateBudgetDialog(
            onDismiss = { viewModel.showCreateDialog(false) },
            onSave = { budget ->
                // Save budget
                viewModel.showCreateDialog(false)
            }
        )
    }
    
    // Delete Budget Dialog
    if (state.showDeleteDialog && state.selectedBudget != null) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteDialog(false) },
            title = { Text("Delete Budget") },
            text = { Text("Are you sure you want to delete this budget? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Delete budget
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
}

@Composable
private fun BudgetOverviewCard(
    totalBudget: Double,
    totalSpent: Double,
    totalRemaining: Double,
    overallPercentage: Double,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Budget Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            ProgressBar(
                current = totalSpent,
                total = totalBudget,
                label = "Overall Budget",
                showAmounts = false,
                showPercentage = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetStatItem(
                    label = "Budget",
                    value = formatMGA(totalBudget),
                    color = PrimaryGreen
                )
                BudgetStatItem(
                    label = "Spent",
                    value = formatMGA(totalSpent),
                    color = ErrorRed
                )
                BudgetStatItem(
                    label = "Remaining",
                    value = formatMGA(totalRemaining),
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun BudgetStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun BudgetPeriodSelector(
    selectedPeriod: BudgetPeriod,
    onPeriodSelected: (BudgetPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BudgetPeriod.values().forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.name) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BudgetItem(
    budgetStatus: com.vola.app.data.models.BudgetStatus,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
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
                Column {
                    Text(
                        text = budgetStatus.budget.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = budgetStatus.budget.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
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
                        text = { Text("Delete") },
                        onClick = onDelete,
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
            
            ProgressBar(
                current = budgetStatus.spent,
                total = budgetStatus.budget.amount,
                label = "${budgetStatus.percentage.toInt()}% spent",
                showAmounts = true,
                showPercentage = false,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${budgetStatus.daysRemaining} days remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (budgetStatus.isOverBudget) {
                    Badge(
                        containerColor = ErrorRed,
                        contentColor = White
                    ) {
                        Text("Over Budget")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBudgetState(
    onAddBudget: () -> Unit,
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
                imageVector = Icons.Default.PieChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            
            Text(
                text = "No Budgets Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Create a budget to track your spending and save money",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Button(
                onClick = onAddBudget,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Budget")
            }
            
            TextButton(
                onClick = onGenerateSuggestions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Suggestions")
            }
        }
    }
}

@Composable
private fun BudgetTipsCard(
    insights: List<String>,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    text = "Budget Tips",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryGreen
                )
            }
            
            insights.forEach { insight ->
                Text(
                    text = "• $insight",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun CreateBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Budget") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* Handle name change */ },
                    label = { Text("Budget Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* Handle amount change */ },
                    label = { Text("Budget Amount (MGA)") },
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
                    // Create and save budget
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

private fun formatMGA(amount: Double): String {
    return String.format("%.0f", amount) + " MGA"
}