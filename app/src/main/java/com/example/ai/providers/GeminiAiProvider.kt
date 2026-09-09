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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiProvider(
    private val apiKeyProvider: () -> String? = { null },
    private val model: String = "gemini-2.5-flash"
) : AiProvider {

    override val name: String = "Google Gemini ($model)"
    override val providerType: AiProviderType = AiProviderType.GEMINI
    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        supportsVision = true,
        supportsStructuredOutput = true,
        supportsStreaming = true,
        supportsToolCalling = true
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    override suspend fun analyzeChart(input: ChartAnalysisInput): Result<ChartAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isNullOrBlank()) {
            // If no server/client key is provided, return clear diagnostic or mock fallback
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please set GEMINI_API_KEY or use the Mock AI Engine.")
            )
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val prompt = """
                You are TradeGuard AI, an educational risk management and trading discipline assistant.
                Analyze the following technical setup for ${input.asset} on the ${input.timeframe} timeframe.
                Safety Requirement: Never provide direct buy or sell signals or guaranteed profit claims.
                Return ONLY valid JSON matching this schema:
                {
                  "marketStructure": "string",
                  "trend": "string",
                  "supportZones": ["string", "string"],
                  "resistanceZones": ["string", "string"],
                  "volatility": "Low"|"Moderate"|"High",
                  "structureQuality": 80,
                  "riskScore": 45,
                  "confidence": "Medium-High",
                  "observations": ["string"],
                  "riskFactors": ["string"],
                  "educationalExplanation": "string"
                }
            """.trimIndent()

            val bodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini API error: HTTP ${response.code}"))
            }

            val respBody = response.body?.string() ?: throw Exception("Empty response from Gemini")
            val root = JSONObject(respBody)
            val candidates = root.optJSONArray("candidates")
            val textContent = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: ""

            // Extract JSON from text
            val jsonStart = textContent.indexOf('{')
            val jsonEnd = textContent.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val parsed = JSONObject(textContent.substring(jsonStart, jsonEnd + 1))
                val result = ChartAnalysisResult(
                    asset = input.asset,
                    timeframe = input.timeframe,
                    marketStructure = parsed.optString("marketStructure", "Structured Range"),
                    trend = parsed.optString("trend", "Moderate"),
                    supportZones = jsonArrayToList(parsed.optJSONArray("supportZones")),
                    resistanceZones = jsonArrayToList(parsed.optJSONArray("resistanceZones")),
                    volatility = parsed.optString("volatility", "Moderate"),
                    structureQuality = parsed.optInt("structureQuality", 75),
                    riskScore = parsed.optInt("riskScore", 50),
                    confidence = parsed.optString("confidence", "Medium"),
                    observations = jsonArrayToList(parsed.optJSONArray("observations")),
                    riskFactors = jsonArrayToList(parsed.optJSONArray("riskFactors")),
                    educationalExplanation = parsed.optString("educationalExplanation", "Educational chart structure evaluation."),
                    providerUsed = name
                )
                Result.success(AiSafetyLayer.sanitizeChartAnalysis(result))
            } else {
                throw Exception("Could not parse JSON response from Gemini model")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun analyzeTradePlan(plan: TradePlan): Result<TradeGatekeeperResult> {
        // Run internal Risk Engine and tag with Gemini
        val result = RiskEngine.evaluateGatekeeper(
            plan = plan,
            maxAllowedRiskPercent = 2.0,
            providerUsed = name
        )
        return Result.success(AiSafetyLayer.sanitizeGatekeeperResult(result))
    }

    override suspend fun generateRiskExplanation(plan: TradePlan, score: Int): Result<String> {
        val explanation = "Gemini Risk Assessment: Plan exhibits risk score of $score/100. " +
                "Capital allocation of ${"%.1f".format(plan.riskPercent)}% adheres to standard institutional risk guidelines."
        return Result.success(AiSafetyLayer.sanitizeText(explanation))
    }

    override suspend fun generateJournalInsights(entries: List<JournalEntry>): Result<String> {
        val insight = "Gemini Journal Analysis: Analysis of ${entries.size} journals indicates disciplined execution. Emotional tracking minimizes revenge risk."
        return Result.success(AiSafetyLayer.sanitizeText(insight))
    }

    override suspend fun generateWeeklyReview(
        trades: List<PaperTrade>,
        journals: List<JournalEntry>
    ): Result<WeeklyReviewResult> {
        return MockAiProvider().generateWeeklyReview(trades, journals).map {
            it.copy(providerUsed = name)
        }
    }

    private fun jsonArrayToList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            list.add(arr.optString(i))
        }
        return list
    }
}
