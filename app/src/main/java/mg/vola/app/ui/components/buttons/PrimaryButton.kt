package com.vola.app.ui.components.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vola.app.ui.theme.PrimaryGreen
import com.vola.app.ui.theme.White

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.ButtonWithIconContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f),
            contentColor = White,
            disabledContentColor = White.copy(alpha = 0.5f)
        ),
        contentPadding = contentPadding
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = White,
                strokeWidth = 2.dp
            )
        } else {
            content()
        }
    }
}