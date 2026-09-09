package com.example.data

import com.example.ai.TradeDirection

enum class TradeStatus {
    OPEN, WON, LOST, BREAKEVEN
}

data class PaperTrade(
    val id: String,
    val asset: String,
    val direction: TradeDirection,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val positionSizeUnits: Double,
    val riskAmount: Double,
    val status: TradeStatus = TradeStatus.OPEN,
    val exitPrice: Double? = null,
    val pnl: Double = 0.0,
    val entryDate: Long = System.currentTimeMillis(),
    val exitDate: Long? = null,
    val strategy: String = "Breakout",
    val notes: String = ""
)

data class JournalEntry(
    val id: String,
    val date: Long = System.currentTimeMillis(),
    val asset: String,
    val setup: String,
    val whyEntered: String,
    val whatExpected: String,
    val emotionalState: String, // Calm, FOMO, Anxious, Confident, Frustrated, Impulsive
    val confidence: String,     // High, Medium, Low
    val result: String = "Open", // Win, Loss, Breakeven, Open
    val whatWentWell: String = "",
    val whatWentWrong: String = "",
    val lessonLearned: String = ""
)

enum class SubscriptionTier(val title: String, val monthlyPrice: String, val features: List<String>) {
    FREE(
        title = "Free Starter",
        monthlyPrice = "$0",
        features = listOf(
            "Basic chart risk checks",
            "Simulated position sizing",
            "Up to 5 paper trades",
            "Basic trade journaling",
            "Mock AI engine access"
        )
    ),
    PRO(
        title = "TradeGuard Pro",
        monthlyPrice = "RM19.90/mo",
        features = listOf(
            "Unlimited AI Gatekeeper checks",
            "Multi-model AI switching (Gemini, OpenAI)",
            "Full structured chart vision reports",
            "Unlimited paper trading & analytics",
            "Weekly AI discipline habit reviews",
            "Automatic revenge-trade alerts"
        )
    ),
    PREMIUM(
        title = "TradeGuard Premium",
        monthlyPrice = "RM49.90/mo",
        features = listOf(
            "Everything in Pro",
            "Priority Groq & Anthropic Claude 3.5",
            "Institutional risk modeling",
            "Deep order flow structure notes",
            "Custom BYOK key management",
            "Advanced multi-timeframe correlation"
        )
    )
}

data class UserProfile(
    val displayName: String = "Disciplined Trader",
    val accountBalance: Double = 10000.0,
    val maxRiskPercent: Double = 2.0,
    val tradingStyle: String = "Swing / Intraday",
    val preferredTimeframe: String = "1H / 4H",
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE
)

data class DisciplineScoreBreakdown(
    val score: Int, // 0-100
    val riskManagementRating: String, // Excellent, Good, Fair, Needs Work
    val strengths: List<String>,
    val areasToImprove: List<String>
)
