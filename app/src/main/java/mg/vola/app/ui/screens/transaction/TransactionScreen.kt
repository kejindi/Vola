package com.vola.app.ui.screens.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavController
import com.vola.app.data.models.Transaction
import com.vola.app.data.models.TransactionType
import com.vola.app.ui.theme.*
import com.vola.app.viewmodels.TransactionViewModel
import kotlinx.datetime.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showFilterDialog(true) }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_transaction") },
                containerColor = PrimaryGreen,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quick Filters
            if (state.filters.category != null || state.filters.type != null) {
                FilterChipsRow(
                    filters = state.filters,
                    onClearFilters = { viewModel.clearFilters() }
                )
            }
            
            // Transactions List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.filteredTransactions.groupBy { it.date }) { (date, transactions) ->
                    DateSection(
                        date = date,
                        transactions = transactions,
                        onTransactionClick = { viewModel.selectTransaction(it) }
                    )
                }
            }
        }
    }
    
    // Filter Dialog
    if (state.showFilterDialog) {
        FilterDialog(
            filters = state.filters,
            onFilterChange = { category, type ->
                viewModel.setCategoryFilter(category)
                viewModel.setTypeFilter(type)
            },
            onDismiss = { viewModel.showFilterDialog(false) }
        )
    }
}

@Composable
private fun FilterChipsRow(
    filters: com.vola.app.data.models.TransactionFilters,
    onClearFilters: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.category?.let { category ->
            FilterChip(
                selected = true,
                onClick = {},
                label = { Text(category.replaceFirstChar { it.uppercase() }) },
                trailingIcon = {
                    Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                }
            )
        }
        
        filters.type?.let { type ->
            FilterChip(
                selected = true,
                onClick = {},
                label = { Text(type.name) },
                trailingIcon = {
                    Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                }
            )
        }
        
        AssistChip(
            onClick = onClearFilters,
            label = { Text("Clear All") },
            leadingIcon = {
                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
            }
        )
    }
}

@Composable
private fun DateSection(
    date: kotlinx.datetime.LocalDate,
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    Column {
        Text(
            text = when {
                date == kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date -> "Today"
                date == kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.minusDays(1) -> "Yesterday"
                else -> date.toString()
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Neutral700,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        transactions.forEach { transaction ->
            TransactionItem(
                transaction = transaction,
                onClick = { onTransactionClick(transaction) }
            )
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        when (transaction.category) {
                            "food" -> CategoryFood
                            "transport" -> CategoryTransport
                            "utilities" -> CategoryUtilities
                            else -> PrimaryGreen.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (transaction.category) {
                        "food" -> "🍽️"
                        "transport" -> "🚌"
                        "utilities" -> "💡"
                        "shopping" -> "🛍️"
                        "health" -> "🏥"
                        "salary" -> "💰"
                        else -> "💸"
                    },
                    fontSize = 20.sp
                )
            }
            
            // Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transaction.merchant ?: transaction.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = Neutral600
                )
            }
            
            // Amount
            Text(
                text = if (transaction.type == TransactionType.EXPENSE) {
                    "-${String.format("%.0f", transaction.amount)} MGA"
                } else {
                    "+${String.format("%.0f", transaction.amount)} MGA"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.type == TransactionType.EXPENSE) ErrorRed else SuccessGreen
            )
        }
    }
}

@Composable
private fun FilterDialog(
    filters: com.vola.app.data.models.TransactionFilters,
    onFilterChange: (String?, TransactionType?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Transactions") },
        text = {
            Column {
                Text("Category", style = MaterialTheme.typography.labelLarge)
                // Category filter options would go here
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Type", style = MaterialTheme.typography.labelLarge)
                // Type filter options would go here
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onFilterChange(null, null)
                onDismiss()
            }) {
                Text("Clear")
            }
        }
    )
}