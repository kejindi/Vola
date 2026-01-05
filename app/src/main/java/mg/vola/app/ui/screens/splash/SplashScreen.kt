package com.vola.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vola.app.R
import com.vola.app.ui.theme.PrimaryGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val scale = remember { Animatable(0.8f) }
    val rotation = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        // Animate logo
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 800, easing = LinearEasing)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )
        
        rotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
        
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
        
        // Wait and navigate
        delay(1500)
        onTimeout()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryGreen),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_vola_logo),
            contentDescription = "Vola Logo",
            modifier = Modifier
                .size(120.dp)
                .scale(scale.value)
                .graphicsLayer {
                    rotationZ = rotation.value
                    alpha = alpha.value
                }
                .clip(MaterialTheme.shapes.extraLarge)
        )
    }
}