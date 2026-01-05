package com.vola.app.ui.screens.transaction

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.vola.app.data.models.TransactionType
import com.vola.app.ui.components.buttons.PrimaryButton
import com.vola.app.ui.theme.*
import com.vola.app.viewmodels.TransactionViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("0") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("food") }
    var merchant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showNumberPad by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open camera */ }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showNumberPad = true },
                colors = CardDefaults.cardColors(
                    containerColor = Neutral50
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (amount == "0") "Tap to enter amount" else amount,
                        fontSize = if (amount.length > 6) 32.sp else 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (amount == "0") Neutral500 else Neutral900
                    )
                    Text(
                        text = "MGA",
                        fontSize = 16.sp,
                        color = Neutral700
                    )
                }
            }
            
            // Transaction Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionTypeButton(
                    type = TransactionType.INCOME,
                    isSelected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME }
                )
                TransactionTypeButton(
                    type = TransactionType.EXPENSE,
                    isSelected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE }
                )
            }
            
            // Category
            CategorySelector(
                selectedCategory = selectedCategory,
                onClick = { showCategoryDialog = true }
            )
            
            // Date
            DateSelector(
                selectedDate = selectedDate,
                onClick = { showDateDialog = true }
            )
            
            // Merchant
            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Store, contentDescription = "Merchant")
                }
            )
            
            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Note, contentDescription = "Notes")
                },
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save Button
            PrimaryButton(
                onClick = {
                    // Save transaction
                    onBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
                Text("Save Transaction")
            }
        }
    }
    
    // Number Pad Dialog
    if (showNumberPad) {
        NumberPadDialog(
            amount = amount,
            onAmountChange = { amount = it },
            onDismiss = { showNumberPad = false }
        )
    }
    
    // Category Dialog
    if (showCategoryDialog) {
        CategoryDialog(
            selectedCategory = selectedCategory,
            onCategorySelect = {
                selectedCategory = it
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
}

@Composable
private fun TransactionTypeButton(
    type: TransactionType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        label = "background"
    )
    
    val textColor by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.7f,
        label = "text"
    )
    
    val containerColor = when (type) {
        TransactionType.INCOME -> SuccessGreen
        TransactionType.EXPENSE -> ErrorRed
        TransactionType.TRANSFER -> InfoBlue
    }
    
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) containerColor else Color.Transparent
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = if (isSelected) 0.dp else 1.dp
        )
    ) {
        Icon(
            imageVector = when (type) {
                TransactionType.INCOME -> Icons.Default.ArrowDownward
                TransactionType.EXPENSE -> Icons.Default.ArrowUpward
                TransactionType.TRANSFER -> Icons.Default.SwapHoriz
            },
            contentDescription = null,
            tint = if (isSelected) White else Neutral700
        )
        Text(
            text = when (type) {
                TransactionType.INCOME -> "Income"
                TransactionType.EXPENSE -> "Expense"
                TransactionType.TRANSFER -> "Transfer"
            },
            color = if (isSelected) White else Neutral700
        )
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CategoryFood),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍽️", fontSize = 20.sp)
                }
                Text(
                    text = "Food",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Select Category",
                tint = Neutral500
            )
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Date",
                    tint = PrimaryGreen
                )
                Text(
                    text = selectedDate.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryGreen
            )
        }
    }
}

@Composable
private fun NumberPadDialog(
    amount: String,
    onAmountChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (amount.isEmpty()) "0" else amount,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "⌫")) { key ->
                        NumberPadKey(
                            key = key,
                            onClick = {
                                when (key) {
                                    "⌫" -> {
                                        if (amount.isNotEmpty()) {
                                            onAmountChange(amount.dropLast(1))
                                        }
                                    }
                                    "." -> {
                                        if (!amount.contains(".")) {
                                            onAmountChange("$amount.")
                                        }
                                    }
                                    else -> {
                                        if (amount == "0") {
                                            onAmountChange(key)
                                        } else {
                                            onAmountChange("$amount$key")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                
                PrimaryButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun NumberPadKey(
    key: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = Neutral100
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (key == "⌫") {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = Neutral700
                )
            } else {
                Text(
                    text = key,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Neutral900
                )
            }
        }
    }
}

@Composable
private fun CategoryDialog(
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf(
        "🍽️ Food" to "food",
        "🚌 Transport" to "transport",
        "💡 Utilities" to "utilities",
        "🛍️ Shopping" to "shopping",
        "🏥 Health" to "health",
        "🎉 Entertainment" to "entertainment",
        "📚 Education" to "education",
        "🏠 Housing" to "housing",
        "💝 Gifts" to "gifts",
        "✈️ Travel" to "travel",
        "💰 Salary" to "salary",
        "💼 Freelance" to "freelance"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(categories) { (emoji, categoryId) ->
                        CategoryItem(
                            emoji = emoji,
                            categoryId = categoryId,
                            isSelected = selectedCategory == categoryId,
                            onClick = { onCategorySelect(categoryId) }
                        )
                    }
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    emoji: String,
    categoryId: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryGreen.copy(alpha = 0.1f) else Neutral50
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder.copy(width = 2.dp, color = PrimaryGreen)
        } else {
            CardDefaults.outlinedCardBorder
        }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Text(
                text = categoryId.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}