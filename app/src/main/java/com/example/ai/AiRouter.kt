package com.example.ai

import com.example.ai.providers.AnthropicAiProvider
import com.example.ai.providers.GeminiAiProvider
import com.example.ai.providers.GroqAiProvider
import com.example.ai.providers.MockAiProvider
import com.example.ai.providers.OpenAiProvider
import com.example.ai.providers.OpenRouterAiProvider
import com.example.data.JournalEntry
import com.example.data.PaperTrade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AiRouter(
    private val configProvider: () -> AiProviderConfig = { AiProviderConfig() },
    private val apiKeyResolver: (AiProviderType) -> String? = { null }
) {

    private val mockProvider = MockAiProvider()
    private val geminiProvider = GeminiAiProvider(
        apiKeyProvider = { apiKeyResolver(AiProviderType.GEMINI) }
    )
    private val openAiProvider = OpenAiProvider(
        apiKeyProvider = { apiKeyResolver(AiProviderType.OPENAI) }
    )
    private val anthropicProvider = AnthropicAiProvider(
        apiKeyProvider = { apiKeyResolver(AiProviderType.ANTHROPIC) }
    )
    private val openRouterProvider = OpenRouterAiProvider(
        apiKeyProvider = { apiKeyResolver(AiProviderType.OPENROUTER) }
    )
    private val groqProvider = GroqAiProvider(
        apiKeyProvider = { apiKeyResolver(AiProviderType.GROQ) }
    )

    // Usage Tracking
    private val _usageRecords = MutableStateFlow<List<AiUsageRecord>>(emptyList())
    val usageRecords: StateFlow<List<AiUsageRecord>> = _usageRecords.asStateFlow()

    fun getProvider(type: AiProviderType): AiProvider {
        return when (type) {
            AiProviderType.MOCK -> mockProvider
            AiProviderType.GEMINI -> geminiProvider
            AiProviderType.OPENAI -> openAiProvider
            AiProviderType.ANTHROPIC -> anthropicProvider
            AiProviderType.OPENROUTER -> openRouterProvider
            AiProviderType.GROQ -> groqProvider
        }
    }

    suspend fun analyzeChart(input: ChartAnalysisInput): Result<ChartAnalysisResult> {
        val config = configProvider()
        val primary = getProvider(config.provider)
        val startTime = System.currentTimeMillis()

        // Capability check: Vision
        if (!primary.capabilities.supportsVision) {
            // Check fallback provider for vision capability
            val fallbackType = config.fallbackProvider ?: AiProviderType.MOCK
            val fallback = getProvider(fallbackType)
            if (fallback.capabilities.supportsVision) {
                return executeWithTracking(
                    provider = fallback,
                    requestType = "Chart Analysis (Fallback for Vision)",
                    startTime = startTime
                ) { fallback.analyzeChart(input) }
            }
        }

        val primaryResult = primary.analyzeChart(input)
        if (primaryResult.isSuccess || !config.enableFallback) {
            recordUsage(
                provider = primary.name,
                model = config.model,
                requestType = "Chart Analysis",
                tokens = 850,
                durationMs = System.currentTimeMillis() - startTime,
                success = primaryResult.isSuccess,
                costUsd = calculateCost(config.provider, 850)
            )
            return primaryResult
        }

        // Fallback execution
        val fallbackType = config.fallbackProvider ?: AiProviderType.MOCK
        val fallback = getProvider(fallbackType)
        val fallbackStart = System.currentTimeMillis()
        val fallbackResult = fallback.analyzeChart(input)

        recordUsage(
            provider = "${fallback.name} [Fallback from ${primary.name}]",
            model = fallbackType.defaultModel,
            requestType = "Chart Analysis (Fallback)",
            tokens = 850,
            durationMs = System.currentTimeMillis() - fallbackStart,
            success = fallbackResult.isSuccess,
            costUsd = calculateCost(fallbackType, 850)
        )

        return fallbackResult
    }

    suspend fun analyzeTradePlan(plan: TradePlan): Result<TradeGatekeeperResult> {
        val config = configProvider()
        val primary = getProvider(config.provider)
        val startTime = System.currentTimeMillis()

        val primaryResult = primary.analyzeTradePlan(plan)
        if (primaryResult.isSuccess || !config.enableFallback) {
            recordUsage(
                provider = primary.name,
                model = config.model,
                requestType = "Trade Gatekeeper",
                tokens = 450,
                durationMs = System.currentTimeMillis() - startTime,
                success = primaryResult.isSuccess,
                costUsd = calculateCost(config.provider, 450)
            )
            return primaryResult
        }

        // Fallback
        val fallbackType = config.fallbackProvider ?: AiProviderType.MOCK
        val fallback = getProvider(fallbackType)
        return fallback.analyzeTradePlan(plan)
    }

    suspend fun generateWeeklyReview(
        trades: List<PaperTrade>,
        journals: List<JournalEntry>
    ): Result<WeeklyReviewResult> {
        val config = configProvider()
        val primary = getProvider(config.provider)
        val startTime = System.currentTimeMillis()

        val result = primary.generateWeeklyReview(trades, journals)
        recordUsage(
            provider = primary.name,
            model = config.model,
            requestType = "Weekly AI Review",
            tokens = 1200,
            durationMs = System.currentTimeMillis() - startTime,
            success = result.isSuccess,
            costUsd = calculateCost(config.provider, 1200)
        )
        return result
    }

    private suspend fun <T> executeWithTracking(
        provider: AiProvider,
        requestType: String,
        startTime: Long,
        action: suspend () -> Result<T>
    ): Result<T> {
        val res = action()
        val duration = System.currentTimeMillis() - startTime
        recordUsage(
            provider = provider.name,
            model = provider.providerType.defaultModel,
            requestType = requestType,
            tokens = 800,
            durationMs = duration,
            success = res.isSuccess,
            costUsd = calculateCost(provider.providerType, 800)
        )
        return res
    }

    private fun recordUsage(
        provider: String,
        model: String,
        requestType: String,
        tokens: Int,
        durationMs: Long,
        success: Boolean,
        costUsd: Double
    ) {
        val record = AiUsageRecord(
            id = UUID.randomUUID().toString(),
            provider = provider,
            model = model,
            requestType = requestType,
            estimatedTokens = tokens,
            durationMs = durationMs,
            success = success,
            estimatedCostUsd = costUsd
        )
        _usageRecords.value = listOf(record) + _usageRecords.value.take(24)
    }

    private fun calculateCost(providerType: AiProviderType, tokens: Int): Double {
        val pricePer1k = when (providerType) {
            AiProviderType.MOCK -> 0.0
            AiProviderType.GEMINI -> 0.00015
            AiProviderType.OPENAI -> 0.005
            AiProviderType.ANTHROPIC -> 0.003
            AiProviderType.OPENROUTER -> 0.0008
            AiProviderType.GROQ -> 0.0002
        }
        return (tokens / 1000.0) * pricePer1k
    }
}
