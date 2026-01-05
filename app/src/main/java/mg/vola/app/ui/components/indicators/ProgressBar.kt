package com.vola.app.ui.components.indicators

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vola.app.ui.theme.ErrorRed
import com.vola.app.ui.theme.PrimaryGreen
import com.vola.app.ui.theme.WarningYellow
import kotlinx.coroutines.launch

@Composable
fun ProgressBar(
    current: Double,
    total: Double,
    label: String,
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true,
    showAmounts: Boolean = true
) {
    val progress = remember { Animatable(0f) }
    val percentage = (current / total).coerceIn(0f, 1f)
    
    LaunchedEffect(percentage) {
        launch {
            progress.animateTo(
                targetValue = percentage.toFloat(),
                animationSpec = tween(durationMillis = 800)
            )
        }
    }
    
    val barColor = when {
        percentage <= 0.7f -> PrimaryGreen
        percentage <= 0.9f -> WarningYellow
        else -> ErrorRed
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Labels row
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (showAmounts) {
                Text(
                    text = "${formatMGA(current)} / ${formatMGA(total)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        // Progress bar
        LinearProgressIndicator(
            progress = { progress.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        // Percentage indicator
        if (showPercentage) {
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun formatMGA(amount: Double): String {
    return if (amount >= 1000000) {
        String.format("%.1fM", amount / 1000000)
    } else if (amount >= 1000) {
        String.format("%.0fK", amount / 1000)
    } else {
        String.format("%.0f", amount)
    } + " MGA"
}