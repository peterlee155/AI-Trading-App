package com.example.ai

/**
 * Supported AI Providers in TradeGuard AI.
 * The application communicates through a provider abstraction
 * and never couples directly to vendor-specific APIs.
 */
enum class AiProviderType(val displayName: String, val defaultModel: String) {
    MOCK("Mock AI Engine", "tradeguard-v1-deterministic"),
    GEMINI("Google Gemini", "gemini-2.5-flash"),
    OPENAI("OpenAI", "gpt-4o"),
    ANTHROPIC("Anthropic", "claude-3-5-sonnet"),
    OPENROUTER("OpenRouter", "deepseek/deepseek-chat"),
    GROQ("Groq Llama", "llama-3.3-70b-versatile")
}

data class ProviderCapabilities(
    val supportsVision: Boolean,
    val supportsStructuredOutput: Boolean,
    val supportsStreaming: Boolean,
    val supportsToolCalling: Boolean
)

data class AiProviderConfig(
    val provider: AiProviderType = AiProviderType.MOCK,
    val model: String = AiProviderType.MOCK.defaultModel,
    val apiKey: String? = null,
    val temperature: Double = 0.2,
    val maxTokens: Int = 1500,
    val fallbackProvider: AiProviderType? = AiProviderType.GEMINI,
    val enableFallback: Boolean = true
)

data class ChartAnalysisInput(
    val asset: String,
    val timeframe: String,
    val chartBase64: String? = null,
    val chartDescription: String? = null
)

data class ChartAnalysisResult(
    val asset: String,
    val timeframe: String,
    val marketStructure: String, // Bullish, Bearish, Mixed / Consolidation
    val trend: String,           // Strong Uptrend, Moderate, Sideways, Downtrend
    val supportZones: List<String>,
    val resistanceZones: List<String>,
    val volatility: String,      // Low, Moderate, High
    val structureQuality: Int,   // 0-100
    val riskScore: Int,          // 0-100
    val confidence: String,      // Low, Medium, High
    val observations: List<String>,
    val riskFactors: List<String>,
    val educationalExplanation: String,
    val providerUsed: String
)

enum class TradeDirection {
    LONG, SHORT
}

enum class GatekeeperStatus {
    APPROVED, WARNING, REJECTED
}

data class TradePlan(
    val asset: String,
    val direction: TradeDirection,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val accountSize: Double = 10000.0,
    val riskPercent: Double = 1.0,
    val emotionalState: String = "Calm", // Calm, FOMO, Anxious, Confident, Frustrated, Impulsive
    val tradingStyle: String = "Breakout", // Breakout, Pullback, Mean Reversion, Range, Scalp
    val notes: String = ""
)

data class TradeGatekeeperResult(
    val status: GatekeeperStatus,
    val summary: String,
    val primaryReason: String,
    val riskAmount: Double,
    val potentialReward: Double,
    val riskRewardRatio: Double,
    val positionSizeUnits: Double,
    val stopDistancePercent: Double,
    val accountExposurePercent: Double,
    val riskCheckPassed: Boolean,
    val emotionalWarning: String?,
    val suggestions: List<String>,
    val providerUsed: String
)

data class WeeklyReviewResult(
    val weeklySummary: String,
    val whatYouDidWell: List<String>,
    val riskProblems: List<String>,
    val behaviorPatterns: List<String>,
    val mostCommonMistake: String,
    val bestHabit: String,
    val suggestedLearningFocus: String,
    val nextWeekDisciplineGoal: String,
    val providerUsed: String
)

data class AiUsageRecord(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String,
    val model: String,
    val requestType: String,
    val estimatedTokens: Int,
    val durationMs: Long,
    val success: Boolean,
    val estimatedCostUsd: Double
)
