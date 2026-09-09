package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ChartAnalysisResult
import com.example.data.DemoData
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

@Composable
fun ChartScannerScreen(viewModel: TradeGuardViewModel) {
    val sampleCharts = remember { DemoData.getSampleCharts() }
    var selectedSample by remember { mutableStateOf(sampleCharts.first()) }

    val isAnalyzing by viewModel.isAnalyzingChart.collectAsState()
    val scanStage by viewModel.scanStage.collectAsState()
    val analysis by viewModel.chartAnalysis.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI CHART RISK SCANNER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Detect structure, key support/resistance zones, and risk factors without biased buy/sell signals.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Sample Chart Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SELECT TECHNICAL SETUP TO ANALYZE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sampleCharts.forEach { sample ->
                            val isSelected = sample.asset == selectedSample.asset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyanAccent else DarkSurfaceVariant)
                                    .clickable { selectedSample = sample }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = sample.asset.replace("/USDT", ""),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DarkBg else TextPrimary
                                    )
                                    Text(
                                        text = sample.timeframe,
                                        fontSize = 10.sp,
                                        color = if (isSelected) DarkBg.copy(alpha = 0.8f) else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Pattern: ${selectedSample.pattern}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldLight
                    )
                    Text(
                        text = selectedSample.description,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Visual Candlestick Preview Box
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedSample.asset} • ${selectedSample.timeframe} Candlestick Preview",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("1H Vol: 4.2k", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated Financial Candlestick Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070B14))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle grid lines
                            drawLine(Color(0xFF1E293B), Offset(0f, h * 0.33f), Offset(w, h * 0.33f), 1f)
                            drawLine(Color(0xFF1E293B), Offset(0f, h * 0.66f), Offset(w, h * 0.66f), 1f)

                            // Candlestick coordinates (normalized 0-1)
                            val candleData = listOf(
                                Triple(0.55f, 0.45f, Pair(0.60f, 0.40f)), // Green
                                Triple(0.48f, 0.52f, Pair(0.55f, 0.45f)), // Red
                                Triple(0.53f, 0.42f, Pair(0.56f, 0.38f)), // Green
                                Triple(0.44f, 0.50f, Pair(0.52f, 0.41f)), // Red
                                Triple(0.50f, 0.35f, Pair(0.53f, 0.32f)), // Green
                                Triple(0.36f, 0.40f, Pair(0.42f, 0.33f)), // Red
                                Triple(0.41f, 0.28f, Pair(0.44f, 0.25f)), // Green breakout
                                Triple(0.29f, 0.22f, Pair(0.32f, 0.18f))  // Strong green
                            )

                            val step = w / (candleData.size + 1)
                            val barWidth = step * 0.45f

                            candleData.forEachIndexed { index, (open, close, wicks) ->
                                val cx = (index + 1) * step
                                val isBullish = close < open
                                val color = if (isBullish) EmeraldPrimary else RoseDanger

                                // Wick
                                drawLine(
                                    color = color,
                                    start = Offset(cx, wicks.first * h),
                                    end = Offset(cx, wicks.second * h),
                                    strokeWidth = 2f
                                )

                                // Body
                                val topY = minOf(open, close) * h
                                val bottomY = maxOf(open, close) * h
                                val bodyHeight = maxOf(bottomY - topY, 4f)
                                drawRect(
                                    color = color,
                                    topLeft = Offset(cx - barWidth / 2, topY),
                                    size = Size(barWidth, bodyHeight)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scan Action Button
                    Button(
                        onClick = {
                            viewModel.runChartAnalysis(
                                asset = selectedSample.asset,
                                timeframe = selectedSample.timeframe,
                                description = selectedSample.description
                            )
                        },
                        enabled = !isAnalyzing,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(scanStage, color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.Visibility, null, tint = DarkBg, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Chart with AI Engine", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Structured Report Result
        item {
            AnimatedVisibility(visible = analysis != null) {
                analysis?.let { rep ->
                    ChartAnalysisReportCard(
                        report = rep,
                        onSendToGatekeeper = {
                            viewModel.gatekeeperAsset.value = rep.asset
                            viewModel.navigateTo(ScreenDestination.GATEKEEPER)
                        },
                        onUnlockDeepReport = {
                            viewModel.triggerPaywall("Unlock Institutional Heatmap & Multi-Timeframe Order Flow")
                        }
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
fun ChartAnalysisReportCard(
    report: ChartAnalysisResult,
    onSendToGatekeeper: () -> Unit,
    onUnlockDeepReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, CyanAccent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${report.asset} • ${report.timeframe}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Structure: ${report.marketStructure}",
                        fontSize = 12.sp,
                        color = EmeraldLight,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF064E3B))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Risk: ${report.riskScore}/100", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Key Price Zones
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("SUPPORT ZONES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    report.supportZones.forEach { zone ->
                        Text(text = "• $zone", fontSize = 11.sp, color = TextPrimary)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("RESISTANCE ZONES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoseDanger)
                    report.resistanceZones.forEach { zone ->
                        Text(text = "• $zone", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Observations
            Text("AI STRUCTURAL OBSERVATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            report.observations.forEach { obs ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text("•", color = CyanAccent, modifier = Modifier.padding(end = 6.dp))
                    Text(text = obs, fontSize = 12.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Risk Factors
            Text("POTENTIAL RISK FACTORS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberWarning, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            report.riskFactors.forEach { risk ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, null, tint = AmberWarning, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = risk, fontSize = 12.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Educational explanation (Safety compliant)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(10.dp)
            ) {
                Text(
                    text = report.educationalExplanation,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Analyzed by: ${report.providerUsed}", fontSize = 10.sp, color = TextMuted)

            Spacer(modifier = Modifier.height(14.dp))

            // Send to Gatekeeper Action
            Button(
                onClick = onSendToGatekeeper,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Security, null, tint = DarkBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Send Setup to AI Gatekeeper", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Value-First Paywall Teaser
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUnlockDeepReport() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = SkyAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Deep Order Flow & Volume Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Unlock institutional liquidity footprint with Pro", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = SkyAccent, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}
