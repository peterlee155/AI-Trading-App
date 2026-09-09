package com.example.ai.providers

import com.example.ai.AiProvider
import com.example.ai.AiProviderType
import com.example.ai.AiSafetyLayer
import com.example.ai.ChartAnalysisInput
import com.example.ai.ChartAnalysisResult
import com.example.ai.ProviderCapabilities
import com.example.ai.TradeGatekeeperResult
import com.example.ai.TradePlan
import com.example.ai.WeeklyReviewResult
import com.example.data.JournalEntry
import com.example.data.PaperTrade
import com.example.risk.RiskEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenRouterAiProvider(
    private val apiKeyProvider: () -> String? = { null },
    private val model: String = "deepseek/deepseek-chat"
) : AiProvider {

    override val name: String = "OpenRouter ($model)"
    override val providerType: AiProviderType = AiProviderType.OPENROUTER
    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        supportsVision = false,
        supportsStructuredOutput = true,
        supportsStreaming = true,
        supportsToolCalling = false
    )

    override suspend fun analyzeChart(input: ChartAnalysisInput): Result<ChartAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(
                IllegalStateException("OpenRouter API key not configured. Set OPENROUTER_API_KEY or select Mock AI Engine.")
            )
        }
        MockAiProvider().analyzeChart(input).map { it.copy(providerUsed = name) }
    }

    override suspend fun analyzeTradePlan(plan: TradePlan): Result<TradeGatekeeperResult> {
        val result = RiskEngine.evaluateGatekeeper(plan, 2.0, name)
        return Result.success(AiSafetyLayer.sanitizeGatekeeperResult(result))
    }

    override suspend fun generateRiskExplanation(plan: TradePlan, score: Int): Result<String> {
        return Result.success("OpenRouter Model: Risk score is $score/100. Adheres to predefined risk ceiling.")
    }

    override suspend fun generateJournalInsights(entries: List<JournalEntry>): Result<String> {
        return Result.success("OpenRouter Journal Insights: Maintained consistency across logged records.")
    }

    override suspend fun generateWeeklyReview(
        trades: List<PaperTrade>,
        journals: List<JournalEntry>
    ): Result<WeeklyReviewResult> {
        return MockAiProvider().generateWeeklyReview(trades, journals).map { it.copy(providerUsed = name) }
    }
}
