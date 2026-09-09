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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun PaperTradingScreen(viewModel: TradeGuardViewModel) {
    val trades by viewModel.paperTrades.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var filterStatus by remember { mutableStateOf<TradeStatus?>(null) }
    var tradeToClose by remember { mutableStateOf<PaperTrade?>(null) }
    var exitPriceInput by remember { mutableStateOf("") }
    var closeStatusChoice by remember { mutableStateOf(TradeStatus.WON) }

    val filteredTrades = if (filterStatus == null) trades else trades.filter { it.status == filterStatus }

    val totalPnl = trades.sumOf { it.pnl }
    val wonCount = trades.count { it.status == TradeStatus.WON }
    val lostCount = trades.count { it.status == TradeStatus.LOST }
    val openCount = trades.count { it.status == TradeStatus.OPEN }
    val closedTotal = wonCount + lostCount
    val winRate = if (closedTotal > 0) ((wonCount.toDouble() / closedTotal) * 100).roundToInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title & Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, null, tint = SkyAccent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PAPER TRADING",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text("Zero financial risk • Pure discipline rehearsal", fontSize = 12.sp, color = TextSecondary)
                }

                Button(
                    onClick = { viewModel.navigateTo(ScreenDestination.GATEKEEPER) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = DarkBg, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Plan", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Simulated Net P&L", fontSize = 11.sp, color = TextMuted)
                            val pnlColor = if (totalPnl >= 0) EmeraldPrimary else RoseDanger
                            Text(
                                text = (if (totalPnl >= 0) "+$" else "-$") + "%.2f".format(kotlin.math.abs(totalPnl)),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = pnlColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Simulated Balance", fontSize = 11.sp, color = TextMuted)
                            Text(
                                text = "$${"%,.2f".format(userProfile.accountBalance + totalPnl)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Win Rate: $winRate%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SkyAccent)
                        Text("Won: $wonCount", fontSize = 12.sp, color = EmeraldLight)
                        Text("Lost: $lostCount", fontSize = 12.sp, color = RoseDanger)
                        Text("Active: $openCount", fontSize = 12.sp, color = AmberWarning)
                    }
                }
            }
        }

        // Filter chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "All (${trades.size})", TradeStatus.OPEN to "Open ($openCount)", TradeStatus.WON to "Won ($wonCount)", TradeStatus.LOST to "Lost ($lostCount)").forEach { (status, label) ->
                    val isSelected = filterStatus == status
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SkyAccent else DarkSurfaceVariant)
                            .clickable { filterStatus = status }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) DarkBg else TextPrimary
                        )
                    }
                }
            }
        }

        // Empty state or Trades list
        if (filteredTrades.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.TrendingUp, null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No paper trades in this view", fontSize = 14.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Run a trade plan through the Gatekeeper to simulate execution.", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(filteredTrades, key = { it.id }) { trade ->
                PaperTradeCard(
                    trade = trade,
                    onCloseClick = {
                        tradeToClose = trade
                        exitPriceInput = trade.takeProfit.toString()
                    },
                    onDeleteClick = { viewModel.deleteTrade(trade.id) }
                )
            }
        }

        item {
            EducationalDisclaimerBanner()
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Close Trade Dialog
    tradeToClose?.let { trade ->
        AlertDialog(
            onDismissRequest = { tradeToClose = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Close Trade: ${trade.asset}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Direction: ${trade.direction} • Entry: $${trade.entryPrice}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = exitPriceInput,
                        onValueChange = { exitPriceInput = it },
                        label = { Text("Exit Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Text("Mark Outcome:", fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(TradeStatus.WON to EmeraldPrimary, TradeStatus.LOST to RoseDanger, TradeStatus.BREAKEVEN to AmberWarning).forEach { (status, color) ->
                            val isSelected = closeStatusChoice == status
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) color else DarkSurfaceVariant)
                                    .clickable { closeStatusChoice = status }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = status.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DarkBg else TextPrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val exitPrice = exitPriceInput.toDoubleOrNull() ?: trade.entryPrice
                        viewModel.closeTrade(trade.id, exitPrice, closeStatusChoice)
                        tradeToClose = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Confirm Exit", color = DarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { tradeToClose = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
fun PaperTradeCard(
    trade: PaperTrade,
    onCloseClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isLong = trade.direction == TradeDirection.LONG
    val directionColor = if (isLong) EmeraldPrimary else RoseDanger

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(directionColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = trade.direction.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = directionColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trade.asset,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (trade.status) {
                        TradeStatus.OPEN -> AmberWarning
                        TradeStatus.WON -> EmeraldPrimary
                        TradeStatus.LOST -> RoseDanger
                        TradeStatus.BREAKEVEN -> SkyAccent
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = trade.status.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Entry Price", fontSize = 10.sp, color = TextMuted)
                    Text("$${trade.entryPrice}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column {
                    Text("Stop Loss", fontSize = 10.sp, color = TextMuted)
                    Text("$${trade.stopLoss}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RoseDanger)
                }
                Column {
                    Text("Take Profit", fontSize = 10.sp, color = TextMuted)
                    Text("$${trade.takeProfit}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EmeraldLight)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Position", fontSize = 10.sp, color = TextMuted)
                    Text("${"%.3f".format(trade.positionSizeUnits)} u", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CyanAccent)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notes / Gatekeeper attribution
            if (trade.notes.isNotBlank()) {
                Text(
                    text = trade.notes,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Bottom P&L & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("P&L: ", fontSize = 12.sp, color = TextMuted)
                    val pnlColor = if (trade.pnl >= 0) EmeraldPrimary else RoseDanger
                    Text(
                        text = (if (trade.pnl >= 0) "+$" else "-$") + "%.2f".format(kotlin.math.abs(trade.pnl)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = pnlColor
                    )
                }

                if (trade.status == TradeStatus.OPEN) {
                    Button(
                        onClick = onCloseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Close Position", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
