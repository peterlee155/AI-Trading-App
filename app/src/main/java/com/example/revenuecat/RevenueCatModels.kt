package com.example.revenuecat

/**
 * Standard RevenueCat package types matching the SDK specification:
 * - MONTHLY: standard recurring monthly subscription
 * - YEARLY: annual recurring subscription (often featuring free trial and annual discount)
 * - LIFETIME: non-renewing one-time lifetime unlock
 */
enum class RevenueCatPackageType(
    val identifier: String,
    val defaultBadge: String?
) {
    MONTHLY(
        identifier = "\$rc_monthly",
        defaultBadge = "FLEXIBLE"
    ),
    YEARLY(
        identifier = "\$rc_annual",
        defaultBadge = "MOST POPULAR • SAVE 40%"
    ),
    LIFETIME(
        identifier = "\$rc_lifetime",
        defaultBadge = "ONE-TIME PAYMENT"
    )
}

/**
 * Encapsulates a RevenueCat Package item from an Offering.
 */
data class RevenueCatPackage(
    val id: String,
    val packageType: RevenueCatPackageType,
    val title: String,
    val subtitle: String,
    val priceString: String,
    val pricePerMonthString: String? = null,
    val billingPeriodText: String,
    val trialPeriodText: String? = null,
    val savingsBadge: String? = null,
    val ctaText: String,
    val isRecommended: Boolean = false,
    val features: List<String> = emptyList(),
    val entitlementId: String = "tradeguard_pro"
)

/**
 * Encapsulates the current RevenueCat Offering containing available packages.
 */
data class RevenueCatOffering(
    val identifier: String = "default",
    val description: String = "TradeGuard Pro Offerings",
    val availablePackages: List<RevenueCatPackage>
)

/**
 * Customer entitlement status retrieved from RevenueCat.
 */
data class RevenueCatCustomerInfo(
    val originalAppUserId: String = "anon_trader_7894",
    val activeEntitlements: Set<String> = emptySet(),
    val latestPurchaseDate: Long? = null,
    val managementUrl: String? = "https://play.google.com/store/account/subscriptions"
) {
    fun hasEntitlement(entitlementId: String): Boolean = activeEntitlements.contains(entitlementId)
}

/**
 * Factory providing standard RevenueCat offerings configured with the
 * Lifetime, Yearly, and Monthly packages.
 */
object RevenueCatOfferingsProvider {
    val defaultOffering = RevenueCatOffering(
        identifier = "default_offering",
        description = "TradeGuard Pro Systematic Gatekeeper Access",
        availablePackages = listOf(
            // Yearly Plan (Best Value, Free Trial, Recommended)
            RevenueCatPackage(
                id = "yearly_pro_pass",
                packageType = RevenueCatPackageType.YEARLY,
                title = "Annual Pro Pass",
                subtitle = "Includes 7-day risk-free trial",
                priceString = "RM 149.90 / yr",
                pricePerMonthString = "RM 12.49 / month",
                billingPeriodText = "Billed annually after 7-day trial. Cancel anytime.",
                trialPeriodText = "7 Days Free",
                savingsBadge = "SAVE 40%",
                ctaText = "Start 7-Day Free Trial",
                isRecommended = true,
                features = listOf(
                    "7-Day Free Trial with instant full access",
                    "Multi-Model AI (Gemini 2.5, GPT-4o, Claude 3.5)",
                    "Unlimited pre-trade Gatekeeper evaluations",
                    "Automated fallback routing on rate limits",
                    "Weekly AI behavioral audit & psychology journal",
                    "Live Candlestick market structure analysis"
                )
            ),
            // Monthly Plan (Standard Flexibility)
            RevenueCatPackage(
                id = "monthly_pro_pass",
                packageType = RevenueCatPackageType.MONTHLY,
                title = "Monthly Pro Pass",
                subtitle = "Pay month-to-month, cancel anytime",
                priceString = "RM 19.90 / mo",
                pricePerMonthString = "RM 19.90 / month",
                billingPeriodText = "Billed monthly. Renews automatically.",
                trialPeriodText = null,
                savingsBadge = null,
                ctaText = "Subscribe Monthly",
                isRecommended = false,
                features = listOf(
                    "Multi-Model AI (Gemini 2.5, OpenAI GPT-4o)",
                    "Unlimited pre-trade Gatekeeper evaluations",
                    "Automated fallback routing on rate limits",
                    "Weekly AI behavioral audit & psychology journal",
                    "Live Candlestick market structure analysis"
                )
            ),
            // Lifetime Plan (VIP / Pay Once, Own Forever)
            RevenueCatPackage(
                id = "lifetime_pro_pass",
                packageType = RevenueCatPackageType.LIFETIME,
                title = "Lifetime VIP Pass",
                subtitle = "Pay once, own forever with zero recurring fees",
                priceString = "RM 389.00",
                pricePerMonthString = "One-time purchase",
                billingPeriodText = "Pay once. Never pay again. All future AI upgrades included.",
                trialPeriodText = null,
                savingsBadge = "ONE-TIME PAYMENT",
                ctaText = "Get Lifetime Access",
                isRecommended = false,
                features = listOf(
                    "All Pro & Premium features unlocked forever",
                    "Priority Groq ultra-low latency inference",
                    "Zero recurring subscription charges",
                    "Custom BYOK API key integration support",
                    "VIP Discord discipline community invite",
                    "Direct access to all future model iterations"
                )
            )
        )
    )
}
