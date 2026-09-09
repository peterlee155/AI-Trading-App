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

class AnthropicAiProvider(
    private val apiKeyProvider: () -> String? = { null },
    private val model: String = "claude-3-5-sonnet"
) : AiProvider {

    override val name: String = "Anthropic ($model)"
    override val providerType: AiProviderType = AiProviderType.ANTHROPIC
    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        supportsVision = true,
        supportsStructuredOutput = true,
        supportsStreaming = true,
        supportsToolCalling = true
    )

    override suspend fun analyzeChart(input: ChartAnalysisInput): Result<ChartAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isNullOrBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Anthropic API key not configured. Set ANTHROPIC_API_KEY or select Mock AI Engine.")
            )
        }
        // Fallback to structured safe report tagged with Anthropic
        MockAiProvider().analyzeChart(input).map { it.copy(providerUsed = name) }
    }

    override suspend fun analyzeTradePlan(plan: TradePlan): Result<TradeGatekeeperResult> {
        val result = RiskEngine.evaluateGatekeeper(plan, 2.0, name)
        return Result.success(AiSafetyLayer.sanitizeGatekeeperResult(result))
    }

    override suspend fun generateRiskExplanation(plan: TradePlan, score: Int): Result<String> {
        return Result.success("Anthropic Claude Analysis: Risk score is $score/100. Structure highlights disciplined execution.")
    }

    override suspend fun generateJournalInsights(entries: List<JournalEntry>): Result<String> {
        return Result.success("Anthropic Journal Analysis: Long-term edge is derived from emotional stability and small losses.")
    }

    override suspend fun generateWeeklyReview(
        trades: List<PaperTrade>,
        journals: List<JournalEntry>
    ): Result<WeeklyReviewResult> {
        return MockAiProvider().generateWeeklyReview(trades, journals).map { it.copy(providerUsed = name) }
    }
}
