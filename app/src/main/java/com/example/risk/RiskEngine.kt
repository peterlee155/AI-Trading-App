package com.example.risk

import com.example.ai.GatekeeperStatus
import com.example.ai.TradeDirection
import com.example.ai.TradeGatekeeperResult
import com.example.ai.TradePlan
import com.example.data.DisciplineScoreBreakdown
import com.example.data.JournalEntry
import com.example.data.PaperTrade
import com.example.data.TradeStatus
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object RiskEngine {

    fun calculateRiskAmount(accountSize: Double, riskPercent: Double): Double {
        if (accountSize <= 0 || riskPercent <= 0) return 0.0
        return accountSize * (riskPercent / 100.0)
    }

    fun calculateStopDistance(entryPrice: Double, stopLoss: Double, direction: TradeDirection): Double {
        return when (direction) {
            TradeDirection.LONG -> entryPrice - stopLoss
            TradeDirection.SHORT -> stopLoss - entryPrice
        }
    }

    fun calculatePotentialReward(entryPrice: Double, takeProfit: Double, direction: TradeDirection): Double {
        return when (direction) {
            TradeDirection.LONG -> takeProfit - entryPrice
            TradeDirection.SHORT -> entryPrice - takeProfit
        }
    }

    fun calculatePositionSize(riskAmount: Double, stopDistance: Double): Double {
        if (stopDistance <= 0.0) return 0.0
        return riskAmount / stopDistance
    }

    fun calculateRiskRewardRatio(potentialReward: Double, stopDistance: Double): Double {
        if (stopDistance <= 0.0 || potentialReward <= 0.0) return 0.0
        return potentialReward / stopDistance
    }

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validateTradePlan(plan: TradePlan): ValidationResult {
        if (plan.asset.isBlank()) return ValidationResult.Invalid("Asset ticker cannot be empty.")
        if (plan.entryPrice <= 0.0) return ValidationResult.Invalid("Entry price must be greater than 0.")
        if (plan.stopLoss <= 0.0) return ValidationResult.Invalid("Stop loss must be greater than 0.")
        if (plan.takeProfit <= 0.0) return ValidationResult.Invalid("Take profit must be greater than 0.")
        if (plan.accountSize <= 0.0) return ValidationResult.Invalid("Account size must be greater than 0.")
        if (plan.riskPercent <= 0.0) return ValidationResult.Invalid("Risk percentage must be greater than 0.")

        if (plan.entryPrice == plan.stopLoss) {
            return ValidationResult.Invalid("Stop loss cannot equal the entry price.")
        }
        if (plan.entryPrice == plan.takeProfit) {
            return ValidationResult.Invalid("Take profit cannot equal the entry price.")
        }

        when (plan.direction) {
            TradeDirection.LONG -> {
                if (plan.stopLoss >= plan.entryPrice) {
                    return ValidationResult.Invalid("For a LONG trade, stop loss must be BELOW entry price.")
                }
                if (plan.takeProfit <= plan.entryPrice) {
                    return ValidationResult.Invalid("For a LONG trade, take profit must be ABOVE entry price.")
                }
            }
            TradeDirection.SHORT -> {
                if (plan.stopLoss <= plan.entryPrice) {
                    return ValidationResult.Invalid("For a SHORT trade, stop loss must be ABOVE entry price.")
                }
                if (plan.takeProfit >= plan.entryPrice) {
                    return ValidationResult.Invalid("For a SHORT trade, take profit must be BELOW entry price.")
                }
            }
        }
        return ValidationResult.Valid
    }

    fun calculateRiskScore(plan: TradePlan): Int {
        var score = 15

        // Factor 1: Risk Percentage (Max 35 pts)
        when {
            plan.riskPercent <= 1.0 -> score += 5
            plan.riskPercent <= 2.0 -> score += 15
            plan.riskPercent <= 3.0 -> score += 25
            else -> score += 40
        }

        // Factor 2: Risk / Reward Ratio (Max 25 pts)
        val stopDist = calculateStopDistance(plan.entryPrice, plan.stopLoss, plan.direction)
        val rewardDist = calculatePotentialReward(plan.entryPrice, plan.takeProfit, plan.direction)
        val rr = calculateRiskRewardRatio(rewardDist, stopDist)

        when {
            rr >= 2.5 -> score -= 10
            rr >= 1.8 -> score -= 5
            rr >= 1.2 -> score += 10
            else -> score += 25
        }

        // Factor 3: Emotional state (Max 25 pts)
        when (plan.emotionalState.lowercase()) {
            "calm" -> score -= 5
            "confident" -> score += 0
            "anxious" -> score += 10
            "fomo" -> score += 25
            "frustrated" -> score += 30
            "impulsive" -> score += 35
            else -> score += 10
        }

        // Factor 4: Stop Distance Percentage (Tightness risk)
        val stopDistancePct = (stopDist / plan.entryPrice) * 100.0
        if (stopDistancePct < 0.4) {
            score += 15 // dangerously tight stop
        } else if (stopDistancePct > 15.0) {
            score += 10 // extremely wide stop
        }

        return max(5, min(95, score))
    }

    fun evaluateGatekeeper(
        plan: TradePlan,
        maxAllowedRiskPercent: Double = 2.0,
        providerUsed: String = "TradeGuard Gatekeeper Engine"
    ): TradeGatekeeperResult {
        val validation = validateTradePlan(plan)
        if (validation is ValidationResult.Invalid) {
            return TradeGatekeeperResult(
                status = GatekeeperStatus.REJECTED,
                summary = "Plan validation failed",
                primaryReason = validation.reason,
                riskAmount = 0.0,
                potentialReward = 0.0,
                riskRewardRatio = 0.0,
                positionSizeUnits = 0.0,
                stopDistancePercent = 0.0,
                accountExposurePercent = 0.0,
                riskCheckPassed = false,
                emotionalWarning = null,
                suggestions = listOf("Check entry, stop loss, and take profit prices for correctness."),
                providerUsed = providerUsed
            )
        }

        val riskAmount = calculateRiskAmount(plan.accountSize, plan.riskPercent)
        val stopDistance = calculateStopDistance(plan.entryPrice, plan.stopLoss, plan.direction)
        val potentialRewardUnit = calculatePotentialReward(plan.entryPrice, plan.takeProfit, plan.direction)
        val positionSize = calculatePositionSize(riskAmount, stopDistance)
        val rrRatio = calculateRiskRewardRatio(potentialRewardUnit, stopDistance)
        val potentialRewardTotal = positionSize * potentialRewardUnit
        val stopDistancePercent = (stopDistance / plan.entryPrice) * 100.0
        val totalExposure = positionSize * plan.entryPrice
        val exposurePercent = (totalExposure / plan.accountSize) * 100.0

        val suggestions = mutableListOf<String>()
        var emotionalWarning: String? = null
        var isApproved = true
        var isWarning = false
        var rejectionReason: String? = null

        // Rule 1: Risk % ceiling check
        if (plan.riskPercent > maxAllowedRiskPercent) {
            isApproved = false
            rejectionReason = "Planned risk of ${"%.1f".format(plan.riskPercent)}% exceeds maximum allowed risk threshold (${"%.1f".format(maxAllowedRiskPercent)}%)."
            suggestions.add("Lower your risk percentage to at or below ${"%.1f".format(maxAllowedRiskPercent)}% of account equity.")
        }

        // Rule 2: Minimum R:R check
        if (rrRatio < 1.2) {
            if (isApproved) {
                isWarning = true
            }
            suggestions.add("Risk/Reward ratio is only 1:${"%.2f".format(rrRatio)}. Aim for setups with at least 1:1.5 or 1:2.0.")
        }

        // Rule 3: Emotional discipline check
        when (plan.emotionalState.lowercase()) {
            "fomo" -> {
                isWarning = true
                emotionalWarning = "FOMO detected: Waiting for pullback confirmation reduces emotional stop-outs."
                suggestions.add("Verify if this trade meets your pre-planned setup criteria or if you are chasing green candles.")
            }
            "frustrated", "impulsive" -> {
                isApproved = false
                rejectionReason = rejectionReason ?: "Impulsive or frustrated state detected. High risk of revenge trading."
                emotionalWarning = "Revenge trading safeguard triggered: Step away from the screen for at least 30 minutes."
                suggestions.add("Take a mandatory mental break before committing any capital to this setup.")
            }
            "anxious" -> {
                isWarning = true
                emotionalWarning = "Anxiety detected: Consider reducing position size by 50% to achieve mental comfort."
                suggestions.add("Downsize your position until you feel completely detached from the outcome.")
            }
        }

        // Rule 4: Exposure check
        if (exposurePercent > 100.0) {
            isWarning = true
            suggestions.add("Position size requires leverage or high capital concentration (${"%.0f".format(exposurePercent)}% of account).")
        }

        // Rule 5: Ultra-tight stop check
        if (stopDistancePercent < 0.5) {
            isWarning = true
            suggestions.add("Stop loss is within 0.5% of entry. Ordinary market noise or spread may stop you out prematurely.")
        }

        val status = when {
            rejectionReason != null -> GatekeeperStatus.REJECTED
            isWarning -> GatekeeperStatus.WARNING
            else -> GatekeeperStatus.APPROVED
        }

        val summary = when (status) {
            GatekeeperStatus.APPROVED -> "Plan passed risk discipline checks. Position size matches your 1%–2% risk parameters."
            GatekeeperStatus.WARNING -> "Plan acceptable with warnings. Review Risk/Reward or emotional conditions before entering."
            GatekeeperStatus.REJECTED -> rejectionReason ?: "Plan violated fundamental risk management rules."
        }

        return TradeGatekeeperResult(
            status = status,
            summary = summary,
            primaryReason = rejectionReason ?: (if (isWarning) "Caution: Sub-optimal R:R or emotional state detected" else "Plan adheres strictly to your risk profile"),
            riskAmount = riskAmount,
            potentialReward = potentialRewardTotal,
            riskRewardRatio = rrRatio,
            positionSizeUnits = positionSize,
            stopDistancePercent = stopDistancePercent,
            accountExposurePercent = exposurePercent,
            riskCheckPassed = status != GatekeeperStatus.REJECTED,
            emotionalWarning = emotionalWarning,
            suggestions = suggestions.ifEmpty { listOf("Trade plan satisfies all predefined risk criteria. Execute mechanically.") },
            providerUsed = providerUsed
        )
    }

    fun calculateDisciplineScore(
        trades: List<PaperTrade>,
        journals: List<JournalEntry>
    ): DisciplineScoreBreakdown {
        if (trades.isEmpty()) {
            return DisciplineScoreBreakdown(
                score = 82,
                riskManagementRating = "Strong Baseline",
                strengths = listOf("Clean slate ready for disciplined execution", "Pre-set risk ceiling enabled"),
                areasToImprove = listOf("Log at least 5 paper trades with stop loss to measure real consistency")
            )
        }

        var score = 100
        val strengths = mutableListOf<String>()
        val areasToImprove = mutableListOf<String>()

        // Check 1: Stop losses defined on all trades
        val missingStopLoss = trades.count { it.stopLoss <= 0.0 }
        if (missingStopLoss > 0) {
            score -= (missingStopLoss * 15)
            areasToImprove.add("$missingStopLoss trade(s) executed without a strict stop-loss defined.")
        } else {
            strengths.add("100% of trades had a predefined stop-loss order.")
        }

        // Check 2: Journaling adherence
        val journalRatio = if (trades.isNotEmpty()) journals.size.toDouble() / trades.size else 1.0
        if (journalRatio >= 0.7) {
            strengths.add("High journaling consistency (${(journalRatio * 100).toInt()}% of trades journaled).")
        } else {
            score -= 15
            areasToImprove.add("Journal completion is below 70%. Document lessons immediately after exiting.")
        }

        // Check 3: Emotional discipline
        val emotionalMistakes = journals.count {
            it.emotionalState.equals("fomo", ignoreCase = true) ||
            it.emotionalState.equals("frustrated", ignoreCase = true) ||
            it.emotionalState.equals("impulsive", ignoreCase = true)
        }
        if (emotionalMistakes > 0) {
            score -= (emotionalMistakes * 8)
            areasToImprove.add("$emotionalMistakes journal entry(ies) recorded FOMO or impulsive entries.")
        } else {
            strengths.add("Maintained emotional composure (Calm/Confident) across recorded entries.")
        }

        // Check 4: Win/Loss execution
        val winningTrades = trades.count { it.status == TradeStatus.WON }
        val closedTrades = trades.count { it.status != TradeStatus.OPEN }
        if (closedTrades >= 3) {
            strengths.add("Consistent trade execution over $closedTrades closed paper trades.")
        }

        val finalScore = max(35, min(98, score))
        val rating = when {
            finalScore >= 85 -> "Excellent Discipline"
            finalScore >= 70 -> "Good Discipline"
            finalScore >= 55 -> "Fair - Needs Focus"
            else -> "High Behavioral Risk"
        }

        return DisciplineScoreBreakdown(
            score = finalScore,
            riskManagementRating = rating,
            strengths = strengths.ifEmpty { listOf("Regular tracking of trade entries and outcomes") },
            areasToImprove = areasToImprove.ifEmpty { listOf("Maintain current stop-loss discipline as volume increases") }
        )
    }
}
