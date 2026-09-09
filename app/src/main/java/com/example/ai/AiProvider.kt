package com.example.ai

import com.example.data.JournalEntry
import com.example.data.PaperTrade

/**
 * Common AI Provider Interface for TradeGuard AI.
 * The rest of the application ONLY speaks to this interface.
 * Neither the UI nor business logic ever calls vendor SDKs directly.
 */
interface AiProvider {
    val name: String
    val providerType: AiProviderType
    val capabilities: ProviderCapabilities

    suspend fun analyzeChart(input: ChartAnalysisInput): Result<ChartAnalysisResult>

    suspend fun analyzeTradePlan(plan: TradePlan): Result<TradeGatekeeperResult>

    suspend fun generateRiskExplanation(plan: TradePlan, score: Int): Result<String>

    suspend fun generateJournalInsights(entries: List<JournalEntry>): Result<String>

    suspend fun generateWeeklyReview(
        trades: List<PaperTrade>,
        journals: List<JournalEntry>
    ): Result<WeeklyReviewResult>
}
