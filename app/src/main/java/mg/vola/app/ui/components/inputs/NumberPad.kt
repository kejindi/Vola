package com.vola.app.ui.components.inputs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vola.app.ui.theme.Neutral900
import com.vola.app.ui.theme.PrimaryGreen

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onDecimalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Number pad grid
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: 1, 2, 3
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberPadButton("1", onClick = { onNumberClick("1") })
                NumberPadButton("2", onClick = { onNumberClick("2") })
                NumberPadButton("3", onClick = { onNumberClick("3") })
            }
            
            // Row 2: 4, 5, 6
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberPadButton("4", onClick = { onNumberClick("4") })
                NumberPadButton("5", onClick = { onNumberClick("5") })
                NumberPadButton("6", onClick = { onNumberClick("6") })
            }
            
            // Row 3: 7, 8, 9
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberPadButton("7", onClick = { onNumberClick("7") })
                NumberPadButton("8", onClick = { onNumberClick("8") })
                NumberPadButton("9", onClick = { onNumberClick("9") })
            }
            
            // Row 4: ., 0, ⌫
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberPadButton(
                    text = ".",
                    onClick = onDecimalClick,
                    modifier = Modifier.weight(1f)
                )
                NumberPadButton(
                    text = "0",
                    onClick = { onNumberClick("0") },
                    modifier = Modifier.weight(1f)
                )
                NumberPadButton(
                    text = "⌫",
                    isBackspace = true,
                    onClick = onBackspaceClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NumberPadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBackspace: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .defaultMinSize(64.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (isBackspace) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = Neutral900,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Neutral900
                )
            }
        }
    }
}

@Composable
fun CurrencyDisplay(
    amount: String,
    currency: String = "MGA",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (amount.isEmpty()) "0" else amount,
            fontSize = if (amount.length > 10) 32.sp else 48.sp,
            fontWeight = FontWeight.Bold,
            color = Neutral900
        )
        Text(
            text = currency,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun NumberPadDialog(
    amount: String,
    currency: String = "MGA",
    onAmountChange: (String) -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Enter Amount",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                CurrencyDisplay(
                    amount = amount,
                    currency = currency,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                NumberPad(
                    onNumberClick = { digit ->
                        if (amount == "0") {
                            onAmountChange(digit)
                        } else {
                            onAmountChange(amount + digit)
                        }
                    },
                    onBackspaceClick = {
                        if (amount.isNotEmpty()) {
                            onAmountChange(amount.dropLast(1))
                        }
                    },
                    onDecimalClick = {
                        if (!amount.contains(".")) {
                            onAmountChange("$amount.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    )
}