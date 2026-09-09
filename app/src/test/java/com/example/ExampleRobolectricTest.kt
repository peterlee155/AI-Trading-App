package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiSafetyLayer
import com.example.ai.TradeDirection
import com.example.ai.TradePlan
import com.example.risk.RiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TradeGuard AI", appName)
  }

  @Test
  fun `risk engine calculates position sizing accurately`() {
    val riskAmount = RiskEngine.calculateRiskAmount(10000.0, 1.5) // $150
    assertEquals(150.0, riskAmount, 0.001)

    val stopDistance = RiskEngine.calculateStopDistance(64500.0, 63000.0, TradeDirection.LONG) // 1500
    assertEquals(1500.0, stopDistance, 0.001)

    val units = RiskEngine.calculatePositionSize(riskAmount, stopDistance) // 0.1 units
    assertEquals(0.1, units, 0.001)
  }

  @Test
  fun `ai safety layer filters forbidden hype and profit guarantees`() {
    val rawText = "You must buy now for guaranteed profit and sure win signals!"
    val sanitized = AiSafetyLayer.sanitizeText(rawText)
    assertFalse(sanitized.contains("buy now", ignoreCase = true))
    assertFalse(sanitized.contains("guaranteed profit", ignoreCase = true))
  }
}

