package com.vola.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vola.app.ui.theme.PrimaryGreen
import com.vola.app.ui.theme.White
import com.vola.app.viewmodels.MainViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Section
            ProfileSection(
                userName = uiState.userName,
                userEmail = uiState.userEmail,
                onEditProfile = { /* Navigate to edit profile */ },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Account Settings
            SettingsSection(
                title = "Account",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.AccountBalance,
                        title = "Bank Accounts",
                        subtitle = "2 connected",
                        onClick = { /* Navigate to bank accounts */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Security & Privacy",
                        subtitle = "Biometric, PIN",
                        onClick = { /* Navigate to security */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Budget alerts, reminders",
                        onClick = { /* Navigate to notifications */ }
                    )
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // App Settings
            SettingsSection(
                title = "App Settings",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = if (uiState.isDarkMode) "Dark" else "Light",
                        onClick = { /* Toggle theme */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = "Malagasy",
                        onClick = { /* Navigate to language selection */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.CurrencyExchange,
                        title = "Currency",
                        subtitle = uiState.currency,
                        onClick = { /* Navigate to currency selection */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Backup,
                        title = "Data & Backup",
                        subtitle = "Auto-backup enabled",
                        onClick = { /* Navigate to backup */ }
                    )
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Support
            SettingsSection(
                title = "Support",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Help & FAQ",
                        subtitle = "Get help using Vola",
                        onClick = { /* Navigate to help */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Feedback,
                        title = "Send Feedback",
                        subtitle = "Share your thoughts",
                        onClick = { /* Open feedback form */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = "Rate Vola",
                        subtitle = "Share your experience",
                        onClick = { /* Open app store */ }
                    )
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // About
            SettingsSection(
                title = "About",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About Vola",
                        subtitle = "Version 1.0.0",
                        onClick = { /* Show about dialog */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Policy,
                        title = "Terms & Privacy",
                        subtitle = "Legal information",
                        onClick = { /* Open terms */ }
                    )
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sign Out Button
            Button(
                onClick = {
                    // Sign out logic
                    navController.navigate("onboarding") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Vola - Personal Finance Manager",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Version 1.0.0 • Build 100",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
                Text(
                    text = "© $currentYear Vola App. All rights reserved.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(
    userName: String,
    userEmail: String,
    onEditProfile: () -> Unit,
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            PrimaryGreen,
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userName.ifEmpty { "Guest User" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userEmail.ifEmpty { "Not signed in" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                IconButton(onClick = onEditProfile) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                }
            }
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProfileStatItem(
                    label = "Transactions",
                    value = "42",
                    onClick = { /* Navigate to transactions */ }
                )
                ProfileStatItem(
                    label = "Goals",
                    value = "3",
                    onClick = { /* Navigate to goals */ }
                )
                ProfileStatItem(
                    label = "Days",
                    value = "28",
                    onClick = { /* Show streak info */ }
                )
            }
        }
    }
}

@Composable
private fun ProfileStatItem(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingsItem>,
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
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
            
            items.forEachIndexed { index, item ->
                SettingsItemRow(
                    item = item,
                    showDivider = index < items.size - 1
                )
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: SettingsItem,
    showDivider: Boolean = true
) {
    Column {
        androidx.compose.material3.ListItem(
            headlineContent = {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            supportingContent = {
                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            leadingContent = {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier.clickable(onClick = item.onClick)
        )
        
        if (showDivider) {
            Divider(
                modifier = Modifier.padding(start = 72.dp),
                thickness = 0.5.dp
            )
        }
    }
}

data class SettingsItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String = "",
    val onClick: () -> Unit
)