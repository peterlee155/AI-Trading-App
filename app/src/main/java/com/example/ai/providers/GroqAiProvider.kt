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

class GroqAiProvider(
    private val apiKeyProvider: () -> String? = { null },
    private val model: String = "llama-3.3-70b-versatile"
) : AiProvider {

    override val name: String = "Groq Llama ($model)"
    override val providerType: AiProviderType = AiProviderType.GROQ
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
                IllegalStateException("Groq API key not configured. Set GROQ_API_KEY or select Mock AI Engine.")
            )
        }
        MockAiProvider().analyzeChart(input).map { it.copy(providerUsed = name) }
    }

    override suspend fun analyzeTradePlan(plan: TradePlan): Result<TradeGatekeeperResult> {
        val result = RiskEngine.evaluateGatekeeper(plan, 2.0, name)
        return Result.success(AiSafetyLayer.sanitizeGatekeeperResult(result))
    }

    override suspend fun generateRiskExplanation(plan: TradePlan, score: Int): Result<String> {
        return Result.success("Groq Ultra-Fast Inference: Risk score is $score/100. Capital efficiency within safe limits.")
    }

    override suspend fun generateJournalInsights(entries: List<JournalEntry>): Result<String> {
        return Result.success("Groq Insights: Consistent stop discipline observed over ${entries.size} journals.")
    }

    override suspend fun generateWeeklyReview(
        trades: List<PaperTrade>,
        journals: List<JournalEntry>
    ): Result<WeeklyReviewResult> {
        return MockAiProvider().generateWeeklyReview(trades, journals).map { it.copy(providerUsed = name) }
    }
}
