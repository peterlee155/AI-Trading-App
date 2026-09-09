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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AiProviderType
import com.example.ai.AiUsageRecord
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
fun AiSettingsScreen(viewModel: TradeGuardViewModel) {
    val aiConfig by viewModel.aiConfig.collectAsState()
    val usageRecords by viewModel.aiUsageRecords.collectAsState()

    var editingApiKeyProvider by remember { mutableStateOf<AiProviderType?>(null) }
    var apiKeyInputValue by remember { mutableStateOf("") }

    val totalCost = usageRecords.sumOf { it.estimatedCostUsd }
    val totalTokens = usageRecords.sumOf { it.estimatedTokens }
    val totalRequests = usageRecords.size

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
                    Icon(Icons.Default.Memory, null, tint = SkyAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI PROVIDER ABSTRACTION",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TradeGuard AI supports multiple AI engines with zero vendor lock-in and seamless fallback.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Active Provider Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ACTIVE AI ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLight,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = aiConfig.provider.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Default Model: ${aiConfig.model}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF064E3B))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Capabilities check
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val capVision = aiConfig.provider in listOf(AiProviderType.MOCK, AiProviderType.GEMINI, AiProviderType.OPENAI, AiProviderType.ANTHROPIC)
                        CapabilityChip(label = "Vision", isSupported = capVision)
                        CapabilityChip(label = "Structured JSON", isSupported = true)
                        CapabilityChip(label = "Safety Layer", isSupported = true)
                    }
                }
            }
        }

        // Provider Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CHOOSE AI PROVIDER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AiProviderType.entries.forEach { providerType ->
                        val isSelected = aiConfig.provider == providerType
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DarkSurfaceVariant else Color.Transparent)
                                .clickable { viewModel.updateAiProvider(providerType) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, if (isSelected) EmeraldPrimary else DarkBorder, CircleShape)
                                        .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = providerType.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TextPrimary else TextSecondary
                                    )
                                    Text(
                                        text = providerType.defaultModel,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            if (providerType != AiProviderType.MOCK) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .clickable {
                                            editingApiKeyProvider = providerType
                                            apiKeyInputValue = viewModel.getApiKey(providerType)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    val hasKey = viewModel.getApiKey(providerType).isNotBlank()
                                    Text(
                                        text = if (hasKey) "Key Set" else "Set Key",
                                        fontSize = 10.sp,
                                        color = if (hasKey) EmeraldLight else SkyAccent
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF064E3B))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("No Key Needed", fontSize = 10.sp, color = EmeraldLight)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fallback Configuration
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
                        Column {
                            Text(
                                text = "AUTOMATIC MODEL FALLBACK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Switch provider if rate limited or timeout occurs",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = aiConfig.enableFallback,
                            onCheckedChange = { viewModel.toggleFallback(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DarkBg,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }

                    if (aiConfig.enableFallback) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Fallback Target: ${aiConfig.fallbackProvider?.displayName ?: "Mock Engine"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SkyAccent
                        )
                    }
                }
            }
        }

        // Cost & Usage Tracking Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "AI USAGE & COST CONTROL",
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
                            Text("Total Requests", fontSize = 10.sp, color = TextMuted)
                            Text("$totalRequests", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("Est. Tokens", fontSize = 10.sp, color = TextMuted)
                            Text("$totalTokens", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Est. Cost", fontSize = 10.sp, color = TextMuted)
                            Text("$${"%.4f".format(totalCost)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                        }
                    }

                    if (usageRecords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Recent Inference Calls:", fontSize = 11.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        usageRecords.take(4).forEach { record ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${record.requestType} (${record.provider.take(16)})",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${record.durationMs}ms",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Security & Architectural Standards Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TRADEGUARD ARCHITECTURE STANDARDS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Provider Abstraction: UI never calls OpenAI, Gemini or Anthropic directly.\n" +
                                "• Safety Layer: Content sanitized against buy/sell hype and profit claims.\n" +
                                "• Zero Key Leaks: Keys are stored securely and never transmitted client-side.\n" +
                                "• V1 Offline Guarantee: Fully operable via local Mock AI without API keys.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Demo Data Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("DATA & DEMO CONTROLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.resetToDemoData() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Demo", fontSize = 11.sp, color = EmeraldLight)
                        }

                        Button(
                            onClick = { viewModel.clearAllData() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear All Data", fontSize = 11.sp, color = RoseDanger)
                        }
                    }
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

    // Edit API Key Dialog
    editingApiKeyProvider?.let { providerType ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { editingApiKeyProvider = null },
            containerColor = DarkSurface,
            title = {
                Text("Set ${providerType.displayName} API Key", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text("Enter your BYOK (Bring Your Own Key) for ${providerType.displayName}:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKeyInputValue,
                        onValueChange = { apiKeyInputValue = it },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setProviderApiKey(providerType, apiKeyInputValue)
                        editingApiKeyProvider = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Save Key", color = DarkBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { editingApiKeyProvider = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun CapabilityChip(label: String, isSupported: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSupported) Color(0xFF064E3B) else DarkSurfaceVariant)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isSupported) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (isSupported) EmeraldLight else TextMuted,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSupported) EmeraldLight else TextMuted
            )
        }
    }
}
