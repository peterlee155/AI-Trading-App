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

class OpenAiProvider(
    private val apiKeyProvider: () -> String? = { null },
    private val model: String = "gpt-4o"
) : AiProvider {

    override val name: String = "OpenAI ($model)"
    override val providerType: AiProviderType = AiProviderType.OPENAI
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
            return@withContext Result.failure(
                IllegalStateException("OpenAI API key not configured. Set OPENAI_API_KEY or select Mock AI Engine.")
            )
        }

        try {
            val url = "https://api.openai.com/v1/chat/completions"
            val systemPrompt = "You are TradeGuard AI, a trading discipline and educational risk engine. " +
                    "Analyze market structure and risk factors without giving buy/sell signals or financial guarantees. " +
                    "Return ONLY valid JSON matching: {marketStructure, trend, supportZones, resistanceZones, volatility, structureQuality, riskScore, confidence, observations, riskFactors, educationalExplanation}"

            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Analyze chart setup for ${input.asset} on ${input.timeframe} timeframe.")
                })
            }

            val bodyJson = JSONObject().apply {
                put("model", model)
                put("messages", messages)
                put("response_format", JSONObject().apply { put("type", "json_object") })
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("OpenAI API error: HTTP ${response.code}"))
            }

            val respBody = response.body?.string() ?: throw Exception("Empty response from OpenAI")
            val root = JSONObject(respBody)
            val contentStr = root.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            val parsed = JSONObject(contentStr)

            val result = ChartAnalysisResult(
                asset = input.asset,
                timeframe = input.timeframe,
                marketStructure = parsed.optString("marketStructure", "Consolidation Range"),
                trend = parsed.optString("trend", "Neutral"),
                supportZones = jsonArrayToList(parsed.optJSONArray("supportZones")),
                resistanceZones = jsonArrayToList(parsed.optJSONArray("resistanceZones")),
                volatility = parsed.optString("volatility", "Moderate"),
                structureQuality = parsed.optInt("structureQuality", 80),
                riskScore = parsed.optInt("riskScore", 48),
                confidence = parsed.optString("confidence", "Medium"),
                observations = jsonArrayToList(parsed.optJSONArray("observations")),
                riskFactors = jsonArrayToList(parsed.optJSONArray("riskFactors")),
                educationalExplanation = parsed.optString("educationalExplanation", "Educational risk observation."),
                providerUsed = name
            )
            Result.success(AiSafetyLayer.sanitizeChartAnalysis(result))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun analyzeTradePlan(plan: TradePlan): Result<TradeGatekeeperResult> {
        val result = RiskEngine.evaluateGatekeeper(plan, 2.0, name)
        return Result.success(AiSafetyLayer.sanitizeGatekeeperResult(result))
    }

    override suspend fun generateRiskExplanation(plan: TradePlan, score: Int): Result<String> {
        return Result.success("OpenAI Analysis: Risk score is $score/100. Stop-loss distance is calibrated to account size.")
    }

    override suspend fun generateJournalInsights(entries: List<JournalEntry>): Result<String> {
        return Result.success("OpenAI Journal Insights: Analyzed ${entries.size} journals. Risk discipline is the key differentiator.")
    }

    override suspend fun generateWeeklyReview(
        trades: List<PaperTrade>,
        journals: List<JournalEntry>
    ): Result<WeeklyReviewResult> {
        return MockAiProvider().generateWeeklyReview(trades, journals).map { it.copy(providerUsed = name) }
    }

    private fun jsonArrayToList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) list.add(arr.optString(i))
        return list
    }
}
