package com.example.ai

object AiSafetyLayer {

    const val MANDATORY_DISCLAIMER =
        "Educational Analysis Only: TradeGuard AI provides simulated risk analysis and educational market observations. " +
        "It does not provide financial advice, personalized recommendations, or guarantees of future market performance. " +
        "Trading involves substantial risk of loss. Always manage your risk responsibly."

    private val FORBIDDEN_PHRASES = listOf(
        "buy now",
        "sell now",
        "you should buy",
        "you should sell",
        "guaranteed profit",
        "guaranteed return",
        "easy money",
        "this will go up",
        "this will go down",
        "100% win rate",
        "cannot lose",
        "take this trade"
    )

    fun sanitizeText(input: String): String {
        var sanitized = input
        for (phrase in FORBIDDEN_PHRASES) {
            val regex = Regex("(?i)\\b${Regex.escape(phrase)}\\b")
            sanitized = regex.replace(sanitized) {
                "[Risk Observation: evaluate key price levels independently]"
            }
        }
        return sanitized
    }

    fun sanitizeChartAnalysis(result: ChartAnalysisResult): ChartAnalysisResult {
        return result.copy(
            educationalExplanation = sanitizeText(result.educationalExplanation),
            observations = result.observations.map { sanitizeText(it) },
            riskFactors = result.riskFactors.map { sanitizeText(it) }
        )
    }

    fun sanitizeGatekeeperResult(result: TradeGatekeeperResult): TradeGatekeeperResult {
        return result.copy(
            summary = sanitizeText(result.summary),
            primaryReason = sanitizeText(result.primaryReason),
            suggestions = result.suggestions.map { sanitizeText(it) }
        )
    }
}
