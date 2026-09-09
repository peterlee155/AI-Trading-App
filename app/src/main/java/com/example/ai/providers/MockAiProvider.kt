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
import com.example.data.TradeStatus
import com.example.risk.RiskEngine
import kotlinx.coroutines.delay

class MockAiProvider : AiProvider {

    override val name: String = "Mock AI Engine (Deterministic V1)"
    override val providerType: AiProviderType = AiProviderType.MOCK
    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        supportsVision = true,
        supportsStructuredOutput = true,
        supportsStreaming = false,
        supportsToolCalling = false
    )

    override suspend fun analyzeChart(input: ChartAnalysisInput): Result<ChartAnalysisResult> {
        // Simulate realistic AI model processing latency
        delay(650)

        val assetUpper = input.asset.uppercase()
        val isBtc = assetUpper.contains("BTC")
        val isEth = assetUpper.contains("ETH")
        val isSol = assetUpper.contains("SOL")

        val marketStructure = when {
            isBtc -> "Bullish Breakout (Ascending Triangle)"
            isEth -> "Range Bound Consolidation"
            isSol -> "Expanding Bullish Channel"
            else -> "Healthy Pullback to Daily Support"
        }

        val trend = when {
            isBtc -> "Strong Uptrend (Above 20 & 50 EMA)"
            isEth -> "Neutral / Sideways Chop"
            else -> "Moderate Uptrend with Lower Volume"
        }

        val supportZones = when {
            isBtc -> listOf("$64,200 – $64,800", "$62,500 (200 EMA)")
            isEth -> listOf("$3,320 – $3,360", "$3,180 (Key Swing Low)")
            isSol -> listOf("$142 – $145", "$135 (Demand Block)")
            else -> listOf("Recent swing low zone", "20-day moving average test")
        }

        val resistanceZones = when {
            isBtc -> listOf("$67,800 – $68,500 (Macro Highs)", "$70,000 Psychological")
            isEth -> listOf("$3,550 – $3,600", "$3,720 (Major Supply)")
            isSol -> listOf("$160 – $164", "$175 Resistance Pivot")
            else -> listOf("Prior swing high level", "Upper Bollinger Band edge")
        }

        val observations = listOf(
            "Price is holding above the local value area low with decreasing seller volume.",
            "Higher-low sequence intact across the $ {input.timeframe} timeframe.",
            "Momentum oscillators (RSI / MACD) show mild bullish divergence but approach overbought territory.",
            "Liquidity resting above the nearest resistance band may attract stop runs."
        )

        val riskFactors = listOf(
            "High volatility environment: sudden macro news or funding rate spikes could cause wick invalidations.",
            "Distance to logical invalidation level is significant; require strict position sizing.",
            "Volume declining on up-moves indicates potential momentum exhaustion if resistance rejects price."
        )

        val explanation =
            "The chart structure appears to show a structured price expansion with identifiable horizontal consolidation. " +
            "From an educational risk perspective, an entry in this area requires a clear stop loss below the nearest support shelf. " +
            "Traders often look for confirmation rather than anticipating breaks into major resistance. " +
            "This market observation is for educational study and does not forecast future price direction."

        val result = ChartAnalysisResult(
            asset = input.asset,
            timeframe = input.timeframe,
            marketStructure = marketStructure,
            trend = trend,
            supportZones = supportZones,
            resistanceZones = resistanceZones,
            volatility = if (isSol) "High" else "Moderate",
            structureQuality = if (isBtc) 88 else 76,
            riskScore = if (isBtc) 42 else 58,
            confidence = "Medium-High",
            observations = observations,
            riskFactors = riskFactors,
            educationalExplanation = explanation,
            providerUsed = "Mock AI Engine (Deterministic)"
        )

        return Result.success(AiSafetyLayer.sanitizeChartAnalysis(result))
    }

    override suspend fun analyzeTradePlan(plan: TradePlan): Result<TradeGatekeeperResult> {
        delay(400)
        val gatekeeperResult = RiskEngine.evaluateGatekeeper(
            plan = plan,
            maxAllowedRiskPercent = 2.0,
            providerUsed = "Mock AI Gatekeeper (Rules + Heuristics)"
        )
        return Result.success(AiSafetyLayer.sanitizeGatekeeperResult(gatekeeperResult))
    }

    override suspend fun generateRiskExplanation(plan: TradePlan, score: Int): Result<String> {
        delay(300)
        val explanation = "Risk assessment score is $score/100. " +
                "The position sizing formula limits loss to ${"%.1f".format(plan.riskPercent)}% of capital. " +
                "Maintaining an invariant stop-loss discipline protects equity from tail-risk events."
        return Result.success(AiSafetyLayer.sanitizeText(explanation))
    }

    override suspend fun generateJournalInsights(entries: List<JournalEntry>): Result<String> {
        delay(400)
        if (entries.isEmpty()) {
            return Result.success("No journal entries logged yet. Consistent journaling builds systematic trading habits.")
        }
        val fomoCount = entries.count { it.emotionalState.equals("fomo", ignoreCase = true) }
        val calmCount = entries.count { it.emotionalState.equals("calm", ignoreCase = true) }
        val insight = "Across ${entries.size} recorded trades: $calmCount were entered with calm discipline, while $fomoCount involved FOMO. " +
                "Data indicates trades entered with calm patience produced significantly higher risk-adjusted outcomes."
        return Result.success(AiSafetyLayer.sanitizeText(insight))
    }

    override suspend fun generateWeeklyReview(
        trades: List<PaperTrade>,
        journals: List<JournalEntry>
    ): Result<WeeklyReviewResult> {
        delay(700)
        val totalTrades = trades.size
        val won = trades.count { it.status == TradeStatus.WON }
        val lost = trades.count { it.status == TradeStatus.LOST }
        val winRate = if (totalTrades > 0) ((won.toDouble() / totalTrades) * 100).roundToInt() else 60

        val whatYouDidWell = listOf(
            "Pre-defined stop loss orders on all recorded paper executions.",
            "Respected the 2.0% maximum risk threshold across setups.",
            "Documented emotional states immediately after trade planning."
        )

        val riskProblems = listOf(
            "Occasional impulse to move stop loss closer to breakeven before target 1R was reached.",
            "Temptation to enter on 5-minute noise rather than waiting for 1H candle close confirmation."
        )

        val patterns = listOf(
            "Higher win rate on morning swing setups compared to late afternoon scalp trades.",
            "Patience to let winners hit full take-profit targets improved overall expectancy."
        )

        val review = WeeklyReviewResult(
            weeklySummary = "Completed $totalTrades paper trades with a $winRate% disciplined execution rate. Core risk parameters remained protected throughout the cycle.",
            whatYouDidWell = whatYouDidWell,
            riskProblems = riskProblems,
            behaviorPatterns = patterns,
            mostCommonMistake = "Entering second-tier setups when prime setups did not trigger.",
            bestHabit = "Checking the AI Trade Gatekeeper before placing any simulated order.",
            suggestedLearningFocus = "Mastering multi-timeframe support/resistance alignment before entry.",
            nextWeekDisciplineGoal = "Zero trades taken outside predefined London & New York market open sessions.",
            providerUsed = "Mock AI Engine (Deterministic)"
        )

        return Result.success(review)
    }

    private fun Double.roundToInt(): Int = (this + 0.5).toInt()
}
