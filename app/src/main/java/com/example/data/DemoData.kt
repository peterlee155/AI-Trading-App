package com.example.data

import com.example.ai.TradeDirection
import java.util.UUID

object DemoData {

    fun getInitialTrades(): List<PaperTrade> {
        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L
        val day = 24 * hour

        return listOf(
            PaperTrade(
                id = UUID.randomUUID().toString(),
                asset = "BTC/USDT",
                direction = TradeDirection.LONG,
                entryPrice = 64200.0,
                stopLoss = 63100.0,
                takeProfit = 67200.0,
                positionSizeUnits = 0.18,
                riskAmount = 198.0,
                status = TradeStatus.WON,
                exitPrice = 67200.0,
                pnl = 540.0,
                entryDate = now - (3 * day),
                exitDate = now - (2 * day),
                strategy = "Bullish Flag Breakout",
                notes = "Waited for 1H candle close above resistance. Followed rules and hit target 2.7R."
            ),
            PaperTrade(
                id = UUID.randomUUID().toString(),
                asset = "ETH/USDT",
                direction = TradeDirection.SHORT,
                entryPrice = 3480.0,
                stopLoss = 3560.0,
                takeProfit = 3280.0,
                positionSizeUnits = 2.5,
                riskAmount = 200.0,
                status = TradeStatus.LOST,
                exitPrice = 3560.0,
                pnl = -200.0,
                entryDate = now - (2 * day),
                exitDate = now - (1 * day),
                strategy = "Supply Zone Rejection",
                notes = "Stopped out mechanically. Accepted small predetermined loss without adjusting stop."
            ),
            PaperTrade(
                id = UUID.randomUUID().toString(),
                asset = "SOL/USDT",
                direction = TradeDirection.LONG,
                entryPrice = 142.50,
                stopLoss = 138.00,
                takeProfit = 154.00,
                positionSizeUnits = 44.4,
                riskAmount = 200.0,
                status = TradeStatus.WON,
                exitPrice = 153.80,
                pnl = 501.72,
                entryDate = now - (36 * hour),
                exitDate = now - (12 * hour),
                strategy = "Demand Zone Pullback",
                notes = "High-volume reclaim of 20 EMA on 4H. Exited near upper resistance band."
            ),
            PaperTrade(
                id = UUID.randomUUID().toString(),
                asset = "NVDA",
                direction = TradeDirection.LONG,
                entryPrice = 124.00,
                stopLoss = 121.50,
                takeProfit = 130.00,
                positionSizeUnits = 80.0,
                riskAmount = 200.0,
                status = TradeStatus.OPEN,
                exitPrice = null,
                pnl = 144.0, // Floating P&L at $125.80
                entryDate = now - (6 * hour),
                exitDate = null,
                strategy = "Morning Opening Range Breakout",
                notes = "Trade Gatekeeper APPROVED: R:R = 1:2.4. Trailing stop to breakeven upon reaching $127."
            )
        )
    }

    fun getInitialJournals(): List<JournalEntry> {
        val now = System.currentTimeMillis()
        val day = 24 * 3600 * 1000L

        return listOf(
            JournalEntry(
                id = UUID.randomUUID().toString(),
                date = now - (3 * day),
                asset = "BTC/USDT",
                setup = "Ascending Triangle Breakout on 1H with above-average volume",
                whyEntered = "Price broke horizontal resistance and retested support shelf cleanly.",
                whatExpected = "Expansion toward $67,500 liquidity cluster.",
                emotionalState = "Calm",
                confidence = "High",
                result = "Win (+2.7R)",
                whatWentWell = "Remained patient and did not front-run the breakout candle.",
                whatWentWrong = "Slight slippage on market execution.",
                lessonLearned = "Patience pays. Waiting for the confirmation candle reduced fake-out risk."
            ),
            JournalEntry(
                id = UUID.randomUUID().toString(),
                date = now - (2 * day),
                asset = "ETH/USDT",
                setup = "Bearish pinbar rejection at 4H supply level",
                whyEntered = "Wick rejection and bearish divergence on hourly RSI.",
                whatExpected = "Downward move toward lower range boundary at $3,280.",
                emotionalState = "Confident",
                confidence = "Medium",
                result = "Loss (-1.0R)",
                whatWentWell = "Accepted the stop loss mechanically. Did not revenge trade or increase size.",
                whatWentWrong = "Macro crypto market was in strong momentum, counter-trend short was low probability.",
                lessonLearned = "Avoid shorting when higher timeframe market structure remains firmly bullish."
            ),
            JournalEntry(
                id = UUID.randomUUID().toString(),
                date = now - (1 * day),
                asset = "SOL/USDT",
                setup = "Liquidity sweep below $140 followed by aggressive reclaim",
                whyEntered = "Clear bear trap and volume expansion back inside the trading range.",
                whatExpected = "Mean reversion to range value high.",
                emotionalState = "Calm",
                confidence = "High",
                result = "Win (+2.5R)",
                whatWentWell = "AI Gatekeeper checked and approved position size before entry.",
                whatWentWrong = "Took partial profit slightly early at 1.8R.",
                lessonLearned = "Letting winners run to the predefined Gatekeeper target maximized expectancy."
            )
        )
    }

    data class SampleChartSetup(
        val asset: String,
        val timeframe: String,
        val description: String,
        val pattern: String
    )

    fun getSampleCharts(): List<SampleChartSetup> {
        return listOf(
            SampleChartSetup(
                asset = "BTC/USDT",
                timeframe = "1H",
                description = "Ascending Triangle consolidation pressing against $65,000 resistance with rising lows.",
                pattern = "Ascending Triangle Breakout"
            ),
            SampleChartSetup(
                asset = "ETH/USDT",
                timeframe = "4H",
                description = "Horizontal range between $3,320 support and $3,580 supply zone.",
                pattern = "Range Bound Channel"
            ),
            SampleChartSetup(
                asset = "SOL/USDT",
                timeframe = "1H",
                description = "Clean pullback to 50 EMA with bullish divergence on 1H RSI.",
                pattern = "Trend Continuation Pullback"
            ),
            SampleChartSetup(
                asset = "NVDA",
                timeframe = "15M",
                description = "Opening range consolidation breaking out above yesterday's high.",
                pattern = "Opening Range Breakout"
            )
        )
    }
}
