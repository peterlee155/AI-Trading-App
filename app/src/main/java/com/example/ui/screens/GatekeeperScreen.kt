package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GatekeeperStatus
import com.example.ai.TradeDirection
import com.example.ai.TradeGatekeeperResult
import com.example.risk.RiskEngine
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

@Composable
fun GatekeeperScreen(viewModel: TradeGuardViewModel) {
    val asset by viewModel.gatekeeperAsset.collectAsState()
    val direction by viewModel.gatekeeperDirection.collectAsState()
    val entryStr by viewModel.gatekeeperEntry.collectAsState()
    val stopStr by viewModel.gatekeeperStop.collectAsState()
    val tpStr by viewModel.gatekeeperTakeProfit.collectAsState()
    val riskPctStr by viewModel.gatekeeperRiskPercent.collectAsState()
    val emotion by viewModel.gatekeeperEmotionalState.collectAsState()
    val style by viewModel.gatekeeperStyle.collectAsState()
    val notes by viewModel.gatekeeperNotes.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()
    val result by viewModel.gatekeeperResult.collectAsState()
    val isRunning by viewModel.isEvaluatingGatekeeper.collectAsState()

    // Real-time live calculations
    val entry = entryStr.toDoubleOrNull() ?: 0.0
    val stop = stopStr.toDoubleOrNull() ?: 0.0
    val tp = tpStr.toDoubleOrNull() ?: 0.0
    val riskPct = riskPctStr.toDoubleOrNull() ?: 1.0

    val stopDistance = RiskEngine.calculateStopDistance(entry, stop, direction)
    val potentialReward = RiskEngine.calculatePotentialReward(entry, tp, direction)
    val riskAmount = RiskEngine.calculateRiskAmount(userProfile.accountBalance, riskPct)
    val positionSizeUnits = RiskEngine.calculatePositionSize(riskAmount, stopDistance)
    val rrRatio = RiskEngine.calculateRiskRewardRatio(potentialReward, stopDistance)
    val stopDistancePct = if (entry > 0) (stopDistance / entry) * 100.0 else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI TRADE GATEKEEPER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A behavioral discipline shield between your trading plan and execution.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Live Risk Engine Card (Instant feedback)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LIVE RISK ENGINE METRICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Planned Risk", fontSize = 11.sp, color = TextMuted)
                            Text("$${"%.2f".format(riskAmount)} ($riskPct%)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("Target R : R", fontSize = 11.sp, color = TextMuted)
                            val rrColor = if (rrRatio >= 1.5) EmeraldPrimary else AmberWarning
                            Text("1 : ${"%.2f".format(rrRatio)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = rrColor)
                        }
                        Column {
                            Text("Position Size", fontSize = 11.sp, color = TextMuted)
                            Text("${"%.3f".format(positionSizeUnits)} units", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Stop Distance: ${"%.2f".format(stopDistancePct)}%",
                            fontSize = 11.sp,
                            color = if (stopDistancePct in 0.5..15.0) TextSecondary else AmberWarning
                        )
                        Text(
                            text = "Max Allowed Risk: ${"%.1f".format(userProfile.maxRiskPercent)}%",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Setup Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TRADE PLAN DETAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )

                    // Asset quick selector
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "NVDA").forEach { quickAsset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (asset == quickAsset) EmeraldPrimary else DarkSurfaceVariant)
                                    .clickable { viewModel.gatekeeperAsset.value = quickAsset }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = quickAsset,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (asset == quickAsset) DarkBg else TextPrimary
                                )
                            }
                        }
                    }

                    // Direction Selector (Long vs Short)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (direction == TradeDirection.LONG) EmeraldPrimary else DarkSurfaceVariant)
                                .clickable { viewModel.gatekeeperDirection.value = TradeDirection.LONG }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LONG (BUY)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (direction == TradeDirection.LONG) DarkBg else TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (direction == TradeDirection.SHORT) RoseDanger else DarkSurfaceVariant)
                                .clickable { viewModel.gatekeeperDirection.value = TradeDirection.SHORT }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SHORT (SELL)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (direction == TradeDirection.SHORT) DarkBg else TextPrimary
                            )
                        }
                    }

                    // Price Inputs: Entry, Stop Loss, Take Profit
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = entryStr,
                            onValueChange = { viewModel.gatekeeperEntry.value = it },
                            label = { Text("Entry Price", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = stopStr,
                            onValueChange = { viewModel.gatekeeperStop.value = it },
                            label = { Text("Stop Loss", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoseDanger,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = tpStr,
                            onValueChange = { viewModel.gatekeeperTakeProfit.value = it },
                            label = { Text("Take Profit", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // Risk % and Strategy
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = riskPctStr,
                            onValueChange = { viewModel.gatekeeperRiskPercent.value = it },
                            label = { Text("Risk % (e.g. 1.5)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = style,
                            onValueChange = { viewModel.gatekeeperStyle.value = it },
                            label = { Text("Strategy", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // Emotional State Selector (Crucial Gatekeeper input)
                    Column {
                        Text("Current Emotional State", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Calm", "Confident", "Anxious", "FOMO", "Impulsive").forEach { emotionOption ->
                                val isSelected = emotion.equals(emotionOption, ignoreCase = true)
                                val chipColor = when (emotionOption) {
                                    "Calm" -> EmeraldPrimary
                                    "Confident" -> SkyAccent
                                    "Anxious" -> AmberWarning
                                    "FOMO", "Impulsive" -> RoseDanger
                                    else -> TextSecondary
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) chipColor else DarkSurfaceVariant)
                                        .clickable { viewModel.gatekeeperEmotionalState.value = emotionOption }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = emotionOption,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) DarkBg else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Reason / Plan Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { viewModel.gatekeeperNotes.value = it },
                        label = { Text("Trade thesis / confirmation factors", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Primary Action: Run Gatekeeper
                    Button(
                        onClick = { viewModel.runGatekeeper() },
                        enabled = !isRunning && entry > 0 && stop > 0 && tp > 0,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking Risk Rules...", color = DarkBg, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Security, null, tint = DarkBg, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run AI Trade Gatekeeper", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Gatekeeper Result Card
        item {
            AnimatedVisibility(visible = result != null) {
                result?.let { res ->
                    GatekeeperResultCard(
                        result = res,
                        onSaveAsPaperTrade = { viewModel.saveGatekeeperPlanAsTrade() }
                    )
                }
            }
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
fun GatekeeperResultCard(
    result: TradeGatekeeperResult,
    onSaveAsPaperTrade: () -> Unit
) {
    val (statusColor, statusBg, statusIcon) = when (result.status) {
        GatekeeperStatus.APPROVED -> Triple(EmeraldPrimary, Color(0xFF064E3B), Icons.Default.CheckCircle)
        GatekeeperStatus.WARNING -> Triple(AmberWarning, Color(0xFF451A03), Icons.Default.Warning)
        GatekeeperStatus.REJECTED -> Triple(RoseDanger, Color(0xFF4C0519), Icons.Default.Warning)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, statusColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GATEKEEPER STATUS: ${result.status.name}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = result.summary,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Diagnostic Checklist
            Text("GATEKEEPER RISK AUDIT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))

            ChecklistItem(
                title = "Risk/Reward Ratio (1 : ${"%.2f".format(result.riskRewardRatio)})",
                isPassed = result.riskRewardRatio >= 1.5,
                detail = if (result.riskRewardRatio >= 1.5) "Favorable risk asymmetry" else "Sub-optimal expectancy (< 1:1.5)"
            )
            ChecklistItem(
                title = "Max Risk Ceiling (Risk: $${"%.2f".format(result.riskAmount)})",
                isPassed = result.riskCheckPassed,
                detail = if (result.riskCheckPassed) "Adheres to predefined equity risk ceiling" else "Exceeds maximum allowable risk percentage"
            )
            ChecklistItem(
                title = "Stop Distance (${"%.2f".format(result.stopDistancePercent)}%)",
                isPassed = result.stopDistancePercent in 0.5..12.0,
                detail = if (result.stopDistancePercent in 0.5..12.0) "Sensible buffer outside noise" else "Check for overly tight or loose stop distance"
            )

            result.emotionalWarning?.let { warning ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberWarning.copy(alpha = 0.15f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = warning, fontSize = 12.sp, color = AmberWarning)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Suggestions
            Text("GATEKEEPER RECOMMENDATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            result.suggestions.forEach { suggestion ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text("•", color = CyanAccent, modifier = Modifier.padding(end = 6.dp))
                    Text(text = suggestion, fontSize = 12.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Provider attribution
            Text("Evaluated by: ${result.providerUsed}", fontSize = 10.sp, color = TextMuted)

            Spacer(modifier = Modifier.height(10.dp))

            // Convert to paper trade
            Button(
                onClick = onSaveAsPaperTrade,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = DarkBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save as Paper Trade", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ChecklistItem(title: String, isPassed: Boolean, detail: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isPassed) EmeraldPrimary else AmberWarning,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = detail, fontSize = 11.sp, color = TextMuted)
        }
    }
}
