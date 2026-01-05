package com.vola.app.ui.screens.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vola.app.ui.components.cards.BalanceCard
import com.vola.app.ui.components.indicators.ProgressBar
import com.vola.app.ui.theme.PrimaryGreen
import com.vola.app.ui.theme.White
import com.vola.app.viewmodels.MainViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    Text(
                        text = "${now.month.name} ${now.year}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_transaction") },
                containerColor = PrimaryGreen,
                contentColor = White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Balance Card
                item {
                    BalanceCard(
                        balance = viewModel.formatCurrency(uiState.totalSavings),
                        changePercentage = 12.5, // This should come from viewModel
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                
                // Quick Stats
                item {
                    QuickStatsRow(
                        income = uiState.totalIncome,
                        expense = uiState.totalExpense,
                        savings = uiState.totalSavings,
                        formatCurrency = viewModel::formatCurrency
                    )
                }
                
                // Monthly Budget
                item {
                    BudgetSection(
                        budgets = uiState.budgetSummary.budgets,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                
                // Active Goals
                item {
                    GoalsSection(
                        goals = uiState.goalSummary,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                
                // Recent Transactions
                item {
                    RecentTransactionsSection(
                        transactions = uiState.recentTransactions,
                        onViewAll = { navController.navigate("transactions") }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(
    income: Double,
    expense: Double,
    savings: Double,
    formatCurrency: (Double) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickStatItem(
            title = "Income",
            value = formatCurrency(income),
            icon = Icons.Default.ArrowDownward,
            color = MaterialTheme.colorScheme.tertiary
        )
        
        QuickStatItem(
            title = "Expenses",
            value = formatCurrency(expense),
            icon = Icons.Default.ArrowUpward,
            color = MaterialTheme.colorScheme.error
        )
        
        QuickStatItem(
            title = "Savings",
            value = formatCurrency(savings),
            icon = Icons.Default.AccountBalance,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun QuickStatItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BudgetSection(
    budgets: List<com.vola.app.data.models.BudgetStatus>,
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
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "75%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            budgets.take(3).forEach { budget ->
                ProgressBar(
                    current = budget.spent,
                    total = budget.budget.amount,
                    label = budget.budget.name,
                    showAmounts = true,
                    showPercentage = true
                )
            }
        }
    }
}

@Composable
private fun GoalsSection(
    goals: com.vola.app.data.models.GoalSummary,
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
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${(goals.overallProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Sample goal progress - replace with actual goals
            ProgressBar(
                current = 150000.0,
                total = 500000.0,
                label = "🎓 Education Fund",
                showAmounts = true,
                showPercentage = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ProgressBar(
                current = 75000.0,
                total = 300000.0,
                label = "🏠 Emergency Fund",
                showAmounts = true,
                showPercentage = true
            )
        }
    }
}

@Composable
private fun RecentTransactionsSection(
    transactions: List<com.vola.app.data.models.Transaction>,
    onViewAll: () -> Unit,
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
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                androidx.compose.material3.TextButton(onClick = onViewAll) {
                    Text("View All")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (transactions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No transactions yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add your first transaction to get started",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                transactions.take(3).forEach { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: com.vola.app.data.models.Transaction,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#FF6B6B"))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🍽️",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Transaction Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.merchant ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "${transaction.date} • ${transaction.category}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Amount
        Text(
            text = if (transaction.isExpense) {
                "-${String.format("%.0f", transaction.amount)} MGA"
            } else {
                "+${String.format("%.0f", transaction.amount)} MGA"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (transaction.isExpense) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.tertiary
            }
        )
    }
}