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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubscriptionTier
import com.example.revenuecat.RevenueCatPackage
import com.example.revenuecat.RevenueCatPackageType
import com.example.ui.TradeGuardViewModel
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Subscription Upsell Screen
 * Clean, institutional-grade subscription upsell interface presenting Lifetime,
 * Yearly, and Monthly offerings retrieved from RevenueCat with individual calls-to-action.
 */
@Composable
fun PricingScreen(viewModel: TradeGuardViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val offering by viewModel.revenueCatOffering.collectAsState()
    val selectedPackage by viewModel.selectedPackage.collectAsState()
    val isPurchasing by viewModel.isPurchasing.collectAsState()
    val purchaseFeedback by viewModel.purchaseFeedback.collectAsState()
    val customerInfo by viewModel.customerInfo.collectAsState()

    var showLegalDialog by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header
        item {
            SubscriptionUpsellHeader(
                currentTier = userProfile.subscriptionTier,
                hasActiveEntitlement = customerInfo.hasEntitlement("tradeguard_pro") ||
                        customerInfo.hasEntitlement("agentic_cenima_pro")
            )
        }

        // Purchase Feedback Banner
        item {
            AnimatedVisibility(visible = purchaseFeedback != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(EmeraldPrimary, CyanAccent)))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = purchaseFeedback ?: "",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearPurchaseFeedback() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, "Dismiss", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Section Title: Offerings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CHOOSE YOUR PLAN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real-time offerings via RevenueCat SDK",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                // Restore Purchase Button
                TextButton(
                    onClick = { viewModel.restorePurchases() },
                    enabled = !isPurchasing
                ) {
                    Icon(Icons.Default.Refresh, null, tint = SkyAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore", fontSize = 12.sp, color = SkyAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Offerings List (Yearly, Monthly, Lifetime)
        items(offering.availablePackages) { pkg ->
            val isSelected = selectedPackage?.id == pkg.id
            RevenueCatOfferingCard(
                pkg = pkg,
                isSelected = isSelected,
                isPurchasing = isPurchasing,
                onSelect = { viewModel.selectPackage(pkg) },
                onPurchase = { viewModel.purchasePackage(pkg) }
            )
        }

        // Entitlement Benefits Matrix
        item {
            SubscriptionBenefitsCard()
        }

        // Trust, Guarantee & Legal Footer
        item {
            SubscriptionUpsellFooter(
                onShowTerms = { showLegalDialog = "Terms of Service" },
                onShowPrivacy = { showLegalDialog = "Privacy Policy" },
                onRestore = { viewModel.restorePurchases() }
            )
        }
    }
}

/**
 * Hero Header for Subscription Upsell
 */
@Composable
private fun SubscriptionUpsellHeader(
    currentTier: SubscriptionTier,
    hasActiveEntitlement: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, Color(0xFF1E3A8A))))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF059669), Color(0xFF0284C7))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF03151E),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TRADEGUARD PRO",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Institutional Risk & AI Discipline",
                            fontSize = 12.sp,
                            color = EmeraldLight
                        )
                    }
                }

                // Current Tier Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (hasActiveEntitlement) Color(0xFF064E3B) else DarkSurfaceVariant)
                        .border(
                            1.dp,
                            if (hasActiveEntitlement) EmeraldPrimary else DarkBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (hasActiveEntitlement) "PRO ACTIVE" else currentTier.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasActiveEntitlement) EmeraldLight else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Eliminate revenge trades, size positions systematically, and analyze charts with multi-model AI.",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Micro Value Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeaderMicroBadge("✓ Zero FOMO Leaks")
                HeaderMicroBadge("✓ Pre-Trade Gatekeeper")
                HeaderMicroBadge("✓ Cancel Anytime")
            }
        }
    }
}

@Composable
private fun HeaderMicroBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(DarkSurfaceVariant)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Individual Offering Card displaying a RevenueCat package (Yearly, Monthly, or Lifetime)
 * with a distinct, prominent Call-To-Action button.
 */
@Composable
private fun RevenueCatOfferingCard(
    pkg: RevenueCatPackage,
    isSelected: Boolean,
    isPurchasing: Boolean,
    onSelect: () -> Unit,
    onPurchase: () -> Unit
) {
    val borderColor = when {
        isSelected && pkg.packageType == RevenueCatPackageType.LIFETIME -> SkyAccent
        isSelected -> EmeraldPrimary
        pkg.isRecommended -> Color(0xFF059669).copy(alpha = 0.6f)
        else -> DarkBorder
    }

    val cardBg = if (isSelected) {
        when (pkg.packageType) {
            RevenueCatPackageType.LIFETIME -> Color(0xFF0A192F)
            else -> Color(0xFF071F1A)
        }
    } else DarkSurface

    val accentColor = when (pkg.packageType) {
        RevenueCatPackageType.LIFETIME -> SkyAccent
        RevenueCatPackageType.YEARLY -> EmeraldLight
        RevenueCatPackageType.MONTHLY -> CyanAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(borderColor, borderColor.copy(alpha = 0.7f)))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Badges & Radio Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onSelect,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = accentColor,
                            unselectedColor = TextMuted
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pkg.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Savings / Type Badge
                val badgeText = pkg.savingsBadge ?: pkg.packageType.defaultBadge
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (pkg.packageType == RevenueCatPackageType.LIFETIME)
                                    Color(0xFF0284C7).copy(alpha = 0.25f)
                                else
                                    Color(0xFF059669).copy(alpha = 0.25f)
                            )
                            .border(
                                1.dp,
                                accentColor.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = pkg.priceString,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    if (pkg.pricePerMonthString != null) {
                        Text(
                            text = pkg.pricePerMonthString,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = accentColor
                        )
                    }
                }

                if (pkg.trialPeriodText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF064E3B))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = pkg.trialPeriodText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = pkg.billingPeriodText,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Features List
            pkg.features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = feature,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Individual Call-To-Action Button for this Offering
            Button(
                onClick = onPurchase,
                enabled = !isPurchasing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (pkg.packageType) {
                        RevenueCatPackageType.LIFETIME -> Color(0xFF0284C7)
                        RevenueCatPackageType.YEARLY -> EmeraldPrimary
                        RevenueCatPackageType.MONTHLY -> Color(0xFF1E293B)
                    },
                    contentColor = when (pkg.packageType) {
                        RevenueCatPackageType.MONTHLY -> TextPrimary
                        else -> Color(0xFF03151E)
                    }
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                if (isPurchasing && isSelected) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing with Google Play...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = pkg.ctaText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Benefits Checklist Card
 */
@Composable
private fun SubscriptionBenefitsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkBorder, DarkBorder)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = SkyAccent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WHAT YOU UNLOCK WITH PRO",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            BenefitRow(
                title = "Deterministic AI Gatekeeper",
                desc = "Stop revenge trades, enforces R:R > 1.5, blocks trade if risk > account rules."
            )
            BenefitRow(
                title = "Multi-Model AI Flexibility",
                desc = "Switch seamlessly between Google Gemini 2.5, OpenAI GPT-4o, and Claude 3.5."
            )
            BenefitRow(
                title = "Automatic Fallback Protection",
                desc = "Zero rate-limit lockouts. If your primary AI is busy, fallback takes over instantly."
            )
            BenefitRow(
                title = "Live Chart Scanner Vision",
                desc = "Identifies break of structure, liquidity sweep, and order block confluence."
            )
            BenefitRow(
                title = "Weekly Behavioral Psychology Audits",
                desc = "Pinpoint emotional patterns (FOMO, hesitation, overconfidence) with actionable advice."
            )
        }
    }
}

@Composable
private fun BenefitRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF064E3B)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = EmeraldLight, modifier = Modifier.size(12.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}

/**
 * Footer with Restore Purchases, Terms, Privacy and Google Play Billing details.
 */
@Composable
private fun SubscriptionUpsellFooter(
    onShowTerms: () -> Unit,
    onShowPrivacy: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Restore Purchase Button
        OutlinedButton(
            onClick = onRestore,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Refresh, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Restore Google Play Purchases", fontSize = 12.sp, color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Legal Links
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Terms of Service",
                fontSize = 11.sp,
                color = SkyAccent,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onShowTerms() }
            )
            Text(
                text = "  •  ",
                fontSize = 11.sp,
                color = TextMuted
            )
            Text(
                text = "Privacy Policy",
                fontSize = 11.sp,
                color = SkyAccent,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onShowPrivacy() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Recurring subscriptions automatically renew unless cancelled at least 24 hours before the end of the current period. Subscriptions can be managed or cancelled at any time in Google Play Store Settings. Protected by RevenueCat SDK.",
            fontSize = 10.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
