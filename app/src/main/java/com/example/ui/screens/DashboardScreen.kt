package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.TradeDirection
import com.example.data.PaperTrade
import com.example.data.TradeStatus
import com.example.ui.ScreenDestination
import com.example.ui.TradeGuardViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(viewModel: TradeGuardViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val discipline by viewModel.disciplineBreakdown.collectAsState()
    val trades by viewModel.paperTrades.collectAsState()
    val journals by viewModel.journals.collectAsState()
    val aiConfig by viewModel.aiConfig.collectAsState()

    val totalTrades = trades.size
    val winningTrades = trades.count { it.status == TradeStatus.WON }
    val winRate = if (totalTrades > 0) ((winningTrades.toDouble() / totalTrades) * 100).roundToInt() else 0
    val totalPnl = trades.sumOf { it.pnl }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Welcome Header
        item {
            Column {
                Text(
                    text = "Welcome, ${userProfile.displayName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Protect capital first. Growth follows discipline.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Discipline Score Card
        item {
            DisciplineScoreGauge(
                score = discipline.score,
                rating = discipline.riskManagementRating
            )
        }

        // Quick Strengths / Focus
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BEHAVIORAL AUDIT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    discipline.strengths.take(2).forEach { strength ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = strength, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                    discipline.areasToImprove.take(1).forEach { area ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = area, fontSize = 12.sp, color = AmberWarning)
                        }
                    }
                }
            }
        }

        // Key Performance Stats
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Paper P&L",
                    value = (if (totalPnl >= 0) "+$" else "-$") + "%.2f".format(kotlin.math.abs(totalPnl)),
                    subtitle = "Account: $${"%,.0f".format(userProfile.accountBalance + totalPnl)}",
                    icon = Icons.Default.MonetizationOn,
                    iconTint = if (totalPnl >= 0) EmeraldPrimary else RoseDanger,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Win Rate",
                    value = "$winRate%",
                    subtitle = "$winningTrades of $totalTrades closed",
                    icon = Icons.Default.TrendingUp,
                    iconTint = SkyAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    title = "Avg R : R",
                    value = "1 : 2.5",
                    subtitle = "Target min 1 : 1.5",
                    icon = Icons.Default.Assessment,
                    iconTint = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Streak",
                    value = "3 Days",
                    subtitle = "Zero stop adjustments",
                    icon = Icons.Default.Security,
                    iconTint = EmeraldLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // AI Provider Status Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(ScreenDestination.AI_SETTINGS) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Memory, null, tint = SkyAccent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Active AI: ${aiConfig.provider.displayName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Model: ${aiConfig.model} • Fallback: ${aiConfig.fallbackProvider?.name ?: "None"}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Switch Provider",
                        tint = SkyAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Signature Action: AI Trade Gatekeeper
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(ScreenDestination.GATEKEEPER) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, null, tint = Color(0xFF0B0F19), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "AI TRADE GATEKEEPER",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Check risk, position size & emotions before trading",
                                fontSize = 12.sp,
                                color = EmeraldLight
                            )
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Quick Navigation Grid
        item {
            Text(
                text = "QUICK ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionTile(
                    title = "Chart Scanner",
                    subtitle = "AI Market Risk",
                    icon = Icons.Default.ShowChart,
                    color = CyanAccent,
                    modifier = Modifier.weight(1f)
                ) { viewModel.navigateTo(ScreenDestination.CHART_SCANNER) }

                ActionTile(
                    title = "Paper Trading",
                    subtitle = "Track Plans",
                    icon = Icons.Default.TrendingUp,
                    color = SkyAccent,
                    modifier = Modifier.weight(1f)
                ) { viewModel.navigateTo(ScreenDestination.PAPER_TRADES) }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionTile(
                    title = "Trade Journal",
                    subtitle = "Log Emotions",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    color = AmberWarning,
                    modifier = Modifier.weight(1f)
                ) { viewModel.navigateTo(ScreenDestination.JOURNAL) }

                ActionTile(
                    title = "Weekly Review",
                    subtitle = "AI Habit Audit",
                    icon = Icons.Default.Psychology,
                    color = EmeraldLight,
                    modifier = Modifier.weight(1f)
                ) { viewModel.navigateTo(ScreenDestination.DISCIPLINE_WEEKLY) }
            }
        }

        // Recent Paper Trades
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT PAPER TRADES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    color = SkyAccent,
                    modifier = Modifier.clickable { viewModel.navigateTo(ScreenDestination.PAPER_TRADES) }
                )
            }
        }

        items(trades.take(3)) { trade ->
            RecentTradeItem(trade)
        }

        item {
            EducationalDisclaimerBanner()
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun RecentTradeItem(trade: PaperTrade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (trade.direction == TradeDirection.LONG) EmeraldPrimary.copy(alpha = 0.15f)
                            else RoseDanger.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = trade.direction.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (trade.direction == TradeDirection.LONG) EmeraldPrimary else RoseDanger
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = trade.asset, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(
                        text = "Entry: $${trade.entryPrice} • Stop: $${trade.stopLoss}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val pnlColor = when {
                    trade.status == TradeStatus.WON -> EmeraldPrimary
                    trade.status == TradeStatus.LOST -> RoseDanger
                    trade.pnl >= 0 -> EmeraldLight
                    else -> RoseDanger
                }
                Text(
                    text = (if (trade.pnl >= 0) "+$" else "-$") + "%.2f".format(kotlin.math.abs(trade.pnl)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = pnlColor
                )
                Text(
                    text = trade.status.name,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}
