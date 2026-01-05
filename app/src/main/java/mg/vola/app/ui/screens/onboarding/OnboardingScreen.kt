package com.vola.app.ui.screens.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vola.app.R
import com.vola.app.ui.components.buttons.PrimaryButton
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    navController: NavController
) {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to Vola",
            description = "Take control of your finances in Malagasy Ariary",
            imageRes = R.drawable.ic_onboarding_welcome,
            backgroundColor = MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            title = "Track Every Ariary",
            description = "Monitor expenses, set budgets, and achieve your goals",
            imageRes = R.drawable.ic_onboarding_track,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        OnboardingPage(
            title = "Secure & Private",
            description = "Your financial data stays on your device",
            imageRes = R.drawable.ic_onboarding_security,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicators
        OnboardingProgressIndicators(
            pageCount = pages.size,
            currentPage = 0,
            modifier = Modifier.padding(top = 48.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Content
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            OnboardingPageContent(page = pages[0])
        }
        
        // Action buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(
                onClick = { navController.navigate("dashboard") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            Button(
                onClick = { navController.navigate("dashboard") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Skip Onboarding",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun OnboardingProgressIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val animatedProgress = remember { Animatable(0f) }
            
            LaunchedEffect(isActive) {
                if (isActive) {
                    animatedProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 300)
                    )
                } else {
                    animatedProgress.animateTo(0f)
                }
            }
            
            Box(
                modifier = Modifier
                    .size(if (isActive) 24.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    .graphicsLayer {
                        scaleX = if (isActive) animatedProgress.value * 0.5f + 1f else 1f
                        scaleY = if (isActive) animatedProgress.value * 0.5f + 1f else 1f
                    }
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(page.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(page.imageRes),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val backgroundColor: androidx.compose.ui.graphics.Color
)