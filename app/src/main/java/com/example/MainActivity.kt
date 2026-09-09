package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ScreenDestination
import com.example.ui.TradeGuardViewModel
import com.example.ui.screens.AiSettingsScreen
import com.example.ui.screens.ChartScannerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DisciplineWeeklyScreen
import com.example.ui.screens.GatekeeperScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.PaperTradingScreen
import com.example.ui.screens.PricingScreen
import com.example.ui.screens.TradeGuardHeader
import com.example.ui.screens.ValueFirstPaywallDialog
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TradeGuardTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TradeGuardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TradeGuardTheme {
                TradeGuardApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TradeGuardApp(viewModel: TradeGuardViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val aiConfig by viewModel.aiConfig.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val showPaywall by viewModel.showPaywall.collectAsState()
    val paywallReason by viewModel.paywallReason.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TradeGuardHeader(
                currentProviderName = aiConfig.provider.displayName,
                subscriptionTier = userProfile.subscriptionTier,
                onProviderClick = { viewModel.navigateTo(ScreenDestination.AI_SETTINGS) },
                onUpgradeClick = { viewModel.navigateTo(ScreenDestination.PRICING) }
            )
        },
        bottomBar = {
            TradeGuardNavigationBar(
                currentScreen = currentScreen,
                onSelectScreen = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBg)
        ) {
            when (currentScreen) {
                ScreenDestination.DASHBOARD -> DashboardScreen(viewModel)
                ScreenDestination.GATEKEEPER -> GatekeeperScreen(viewModel)
                ScreenDestination.CHART_SCANNER -> ChartScannerScreen(viewModel)
                ScreenDestination.PAPER_TRADES -> PaperTradingScreen(viewModel)
                ScreenDestination.JOURNAL -> JournalScreen(viewModel)
                ScreenDestination.DISCIPLINE_WEEKLY -> DisciplineWeeklyScreen(viewModel)
                ScreenDestination.AI_SETTINGS -> AiSettingsScreen(viewModel)
                ScreenDestination.PRICING -> PricingScreen(viewModel)
            }
        }

        if (showPaywall) {
            ValueFirstPaywallDialog(
                reason = paywallReason,
                onDismiss = { viewModel.dismissPaywall() },
                onUpgrade = { tier -> viewModel.upgradeSubscription(tier) }
            )
        }
    }
}

@Composable
fun TradeGuardNavigationBar(
    currentScreen: ScreenDestination,
    onSelectScreen: (ScreenDestination) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        contentColor = TextPrimary,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple(ScreenDestination.DASHBOARD, "Home", Icons.Default.Home),
            Triple(ScreenDestination.GATEKEEPER, "Gatekeeper", Icons.Default.Security),
            Triple(ScreenDestination.CHART_SCANNER, "Scanner", Icons.Default.ShowChart),
            Triple(ScreenDestination.PAPER_TRADES, "Paper", Icons.Default.TrendingUp),
            Triple(ScreenDestination.JOURNAL, "Journal", Icons.AutoMirrored.Filled.MenuBook),
            Triple(ScreenDestination.AI_SETTINGS, "AI", Icons.Default.Memory)
        )

        navItems.forEach { (destination, label, icon) ->
            val isSelected = currentScreen == destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(destination) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EmeraldLight,
                    selectedTextColor = EmeraldLight,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = Color(0xFF064E3B)
                )
            )
        }
    }
}

