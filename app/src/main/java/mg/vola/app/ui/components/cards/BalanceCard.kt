package com.vola.app.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vola.app.ui.theme.CobaltBlue
import com.vola.app.ui.theme.PrimaryGreen
import com.vola.app.ui.theme.White

@Composable
fun BalanceCard(
    balance: String,
    changePercentage: Double,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(PrimaryGreen, CobaltBlue),
                    start = androidx.compose.ui.geometry.Offset.Zero,
                    end = androidx.compose.ui.geometry.Offset.Infinite
                )
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "BALANCE",
                style = MaterialTheme.typography.labelMedium,
                color = White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = balance,
                style = androidx.compose.material3.MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (changePercentage >= 0) {
                    "↑ ${String.format("%.1f", changePercentage)}% from last month"
                } else {
                    "↓ ${String.format("%.1f", -changePercentage)}% from last month"
                },
                style = MaterialTheme.typography.labelMedium,
                color = White.copy(alpha = 0.9f)
            )
        }
    }
}