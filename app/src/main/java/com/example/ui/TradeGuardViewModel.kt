package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiProviderConfig
import com.example.ai.AiProviderType
import com.example.ai.AiRouter
import com.example.ai.AiUsageRecord
import com.example.ai.ChartAnalysisInput
import com.example.ai.ChartAnalysisResult
import com.example.ai.GatekeeperStatus
import com.example.ai.TradeDirection
import com.example.ai.TradeGatekeeperResult
import com.example.ai.TradePlan
import com.example.ai.WeeklyReviewResult
import com.example.data.DemoData
import com.example.data.DisciplineScoreBreakdown
import com.example.data.JournalEntry
import com.example.data.PaperTrade
import com.example.data.SubscriptionTier
import com.example.revenuecat.RevenueCatCustomerInfo
import com.example.revenuecat.RevenueCatOffering
import com.example.revenuecat.RevenueCatOfferingsProvider
import com.example.revenuecat.RevenueCatPackage
import com.example.revenuecat.RevenueCatPackageType
import com.example.data.TradeStatus
import com.example.data.UserProfile
import com.example.risk.RiskEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class ScreenDestination(val title: String) {
    DASHBOARD("Dashboard"),
    GATEKEEPER("AI Gatekeeper"),
    CHART_SCANNER("Chart Scanner"),
    PAPER_TRADES("Paper Trading"),
    JOURNAL("Journal"),
    DISCIPLINE_WEEKLY("Discipline & Review"),
    AI_SETTINGS("AI Provider & Settings"),
    PRICING("Subscription Plans")
}

class TradeGuardViewModel : ViewModel() {

    // Active Screen
    private val _currentScreen = MutableStateFlow(ScreenDestination.DASHBOARD)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    // User Profile
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // API Keys storage (by provider)
    private val apiKeys = mutableMapOf<AiProviderType, String>()

    // AI Provider Configuration
    private val _aiConfig = MutableStateFlow(
        AiProviderConfig(
            provider = AiProviderType.MOCK,
            model = AiProviderType.MOCK.defaultModel,
            fallbackProvider = AiProviderType.GEMINI,
            enableFallback = true
        )
    )
    val aiConfig: StateFlow<AiProviderConfig> = _aiConfig.asStateFlow()

    // Central AI Router
    private val aiRouter = AiRouter(
        configProvider = { _aiConfig.value },
        apiKeyResolver = { apiKeys[it] }
    )

    val aiUsageRecords: StateFlow<List<AiUsageRecord>> = aiRouter.usageRecords

    // Paper Trades & Journal
    private val _paperTrades = MutableStateFlow(DemoData.getInitialTrades())
    val paperTrades: StateFlow<List<PaperTrade>> = _paperTrades.asStateFlow()

    private val _journals = MutableStateFlow(DemoData.getInitialJournals())
    val journals: StateFlow<List<JournalEntry>> = _journals.asStateFlow()

    // Discipline Score (derived reactively)
    val disciplineBreakdown: StateFlow<DisciplineScoreBreakdown> = combine(_paperTrades, _journals) { trades, jrnls ->
        RiskEngine.calculateDisciplineScore(trades, jrnls)
    }.stateIn(viewModelScope, SharingStarted.Lazily, RiskEngine.calculateDisciplineScore(emptyList(), emptyList()))

    // Gatekeeper Form State
    var gatekeeperAsset = MutableStateFlow("BTC/USDT")
    var gatekeeperDirection = MutableStateFlow(TradeDirection.LONG)
    var gatekeeperEntry = MutableStateFlow("64500.0")
    var gatekeeperStop = MutableStateFlow("63200.0")
    var gatekeeperTakeProfit = MutableStateFlow("67800.0")
    var gatekeeperRiskPercent = MutableStateFlow("1.5")
    var gatekeeperEmotionalState = MutableStateFlow("Calm")
    var gatekeeperStyle = MutableStateFlow("Breakout")
    var gatekeeperNotes = MutableStateFlow("Retest of horizontal shelf with decreasing volume.")

    private val _gatekeeperResult = MutableStateFlow<TradeGatekeeperResult?>(null)
    val gatekeeperResult: StateFlow<TradeGatekeeperResult?> = _gatekeeperResult.asStateFlow()

    private val _isEvaluatingGatekeeper = MutableStateFlow(false)
    val isEvaluatingGatekeeper: StateFlow<Boolean> = _isEvaluatingGatekeeper.asStateFlow()

    // Chart Scanner State
    private val _isAnalyzingChart = MutableStateFlow(false)
    val isAnalyzingChart: StateFlow<Boolean> = _isAnalyzingChart.asStateFlow()

    private val _scanStage = MutableStateFlow("")
    val scanStage: StateFlow<String> = _scanStage.asStateFlow()

    private val _chartAnalysis = MutableStateFlow<ChartAnalysisResult?>(null)
    val chartAnalysis: StateFlow<ChartAnalysisResult?> = _chartAnalysis.asStateFlow()

    // Weekly Review State
    private val _weeklyReview = MutableStateFlow<WeeklyReviewResult?>(null)
    val weeklyReview: StateFlow<WeeklyReviewResult?> = _weeklyReview.asStateFlow()

    private val _isGeneratingWeeklyReview = MutableStateFlow(false)
    val isGeneratingWeeklyReview: StateFlow<Boolean> = _isGeneratingWeeklyReview.asStateFlow()

    // RevenueCat Offerings & State
    private val _revenueCatOffering = MutableStateFlow(RevenueCatOfferingsProvider.defaultOffering)
    val revenueCatOffering: StateFlow<RevenueCatOffering> = _revenueCatOffering.asStateFlow()

    private val _selectedPackage = MutableStateFlow<RevenueCatPackage?>(
        RevenueCatOfferingsProvider.defaultOffering.availablePackages.firstOrNull { it.isRecommended }
            ?: RevenueCatOfferingsProvider.defaultOffering.availablePackages.firstOrNull()
    )
    val selectedPackage: StateFlow<RevenueCatPackage?> = _selectedPackage.asStateFlow()

    private val _isPurchasing = MutableStateFlow(false)
    val isPurchasing: StateFlow<Boolean> = _isPurchasing.asStateFlow()

    private val _purchaseFeedback = MutableStateFlow<String?>(null)
    val purchaseFeedback: StateFlow<String?> = _purchaseFeedback.asStateFlow()

    private val _customerInfo = MutableStateFlow(RevenueCatCustomerInfo())
    val customerInfo: StateFlow<RevenueCatCustomerInfo> = _customerInfo.asStateFlow()

    fun selectPackage(pkg: RevenueCatPackage) {
        _selectedPackage.value = pkg
    }

    fun purchasePackage(pkg: RevenueCatPackage) {
        viewModelScope.launch {
            _isPurchasing.value = true
            _purchaseFeedback.value = null
            delay(1200) // Simulate secure Google Play & RevenueCat billing handshake

            val newTier = when (pkg.packageType) {
                RevenueCatPackageType.LIFETIME -> SubscriptionTier.PREMIUM
                else -> SubscriptionTier.PRO
            }

            _userProfile.value = _userProfile.value.copy(subscriptionTier = newTier)
            _customerInfo.value = _customerInfo.value.copy(
                activeEntitlements = setOf("tradeguard_pro", "agentic_cenima_pro"),
                latestPurchaseDate = System.currentTimeMillis()
            )
            _showPaywall.value = false
            _isPurchasing.value = false
            _purchaseFeedback.value = "Welcome to ${pkg.title}! Entitlement verified."
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _isPurchasing.value = true
            delay(1000)
            _isPurchasing.value = false
            if (_userProfile.value.subscriptionTier != SubscriptionTier.FREE) {
                _purchaseFeedback.value = "Purchases restored: Active ${_userProfile.value.subscriptionTier.title}"
            } else {
                _purchaseFeedback.value = "No previous active purchases found on this Google account."
            }
        }
    }

    fun clearPurchaseFeedback() {
        _purchaseFeedback.value = null
    }

    // Value-First Paywall Dialog
    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    private val _paywallReason = MutableStateFlow("Unlock Full Institutional Order Flow & Deep Analytics")
    val paywallReason: StateFlow<String> = _paywallReason.asStateFlow()

    fun triggerPaywall(reason: String) {
        _paywallReason.value = reason
        _showPaywall.value = true
    }

    fun dismissPaywall() {
        _showPaywall.value = false
    }

    fun upgradeSubscription(tier: SubscriptionTier) {
        _userProfile.value = _userProfile.value.copy(subscriptionTier = tier)
        if (tier != SubscriptionTier.FREE) {
            _customerInfo.value = _customerInfo.value.copy(
                activeEntitlements = setOf("tradeguard_pro", "agentic_cenima_pro")
            )
        } else {
            _customerInfo.value = _customerInfo.value.copy(
                activeEntitlements = emptySet()
            )
        }
        _showPaywall.value = false
    }

    // AI Provider Config Actions
    fun updateAiProvider(providerType: AiProviderType) {
        _aiConfig.value = _aiConfig.value.copy(
            provider = providerType,
            model = providerType.defaultModel
        )
    }

    fun updateFallbackProvider(providerType: AiProviderType?) {
        _aiConfig.value = _aiConfig.value.copy(fallbackProvider = providerType)
    }

    fun toggleFallback(enabled: Boolean) {
        _aiConfig.value = _aiConfig.value.copy(enableFallback = enabled)
    }

    fun setProviderApiKey(providerType: AiProviderType, key: String) {
        apiKeys[providerType] = key
    }

    fun getApiKey(providerType: AiProviderType): String {
        return apiKeys[providerType] ?: ""
    }

    // Gatekeeper Execution
    fun runGatekeeper() {
        viewModelScope.launch {
            _isEvaluatingGatekeeper.value = true
            val entry = gatekeeperEntry.value.toDoubleOrNull() ?: 0.0
            val stop = gatekeeperStop.value.toDoubleOrNull() ?: 0.0
            val tp = gatekeeperTakeProfit.value.toDoubleOrNull() ?: 0.0
            val riskPct = gatekeeperRiskPercent.value.toDoubleOrNull() ?: 1.0
            val account = _userProfile.value.accountBalance

            val plan = TradePlan(
                asset = gatekeeperAsset.value,
                direction = gatekeeperDirection.value,
                entryPrice = entry,
                stopLoss = stop,
                takeProfit = tp,
                accountSize = account,
                riskPercent = riskPct,
                emotionalState = gatekeeperEmotionalState.value,
                tradingStyle = gatekeeperStyle.value,
                notes = gatekeeperNotes.value
            )

            val result = aiRouter.analyzeTradePlan(plan)
            _gatekeeperResult.value = result.getOrNull() ?: RiskEngine.evaluateGatekeeper(
                plan,
                _userProfile.value.maxRiskPercent,
                "Fallback Risk Engine"
            )
            _isEvaluatingGatekeeper.value = false
        }
    }

    fun saveGatekeeperPlanAsTrade() {
        val res = _gatekeeperResult.value ?: return
        val entry = gatekeeperEntry.value.toDoubleOrNull() ?: return
        val stop = gatekeeperStop.value.toDoubleOrNull() ?: return
        val tp = gatekeeperTakeProfit.value.toDoubleOrNull() ?: return

        val newTrade = PaperTrade(
            id = UUID.randomUUID().toString(),
            asset = gatekeeperAsset.value,
            direction = gatekeeperDirection.value,
            entryPrice = entry,
            stopLoss = stop,
            takeProfit = tp,
            positionSizeUnits = res.positionSizeUnits,
            riskAmount = res.riskAmount,
            status = TradeStatus.OPEN,
            strategy = gatekeeperStyle.value,
            notes = "Gatekeeper status: ${res.status}. ${res.primaryReason}"
        )
        _paperTrades.value = listOf(newTrade) + _paperTrades.value
        navigateTo(ScreenDestination.PAPER_TRADES)
    }

    // Chart Scanner Execution
    fun runChartAnalysis(asset: String, timeframe: String, description: String = "") {
        viewModelScope.launch {
            _isAnalyzingChart.value = true
            _chartAnalysis.value = null

            val stages = listOf(
                "Reading chart image resolution...",
                "Detecting market structure & trendlines...",
                "Evaluating volatility & liquidity shelves...",
                "Synthesizing educational risk discipline report..."
            )

            for (stage in stages) {
                _scanStage.value = stage
                delay(350)
            }

            val input = ChartAnalysisInput(
                asset = asset,
                timeframe = timeframe,
                chartDescription = description
            )

            val result = aiRouter.analyzeChart(input)
            _chartAnalysis.value = result.getOrNull()
            _isAnalyzingChart.value = false
        }
    }

    // Weekly Review Execution
    fun generateWeeklyReview() {
        viewModelScope.launch {
            _isGeneratingWeeklyReview.value = true
            val res = aiRouter.generateWeeklyReview(_paperTrades.value, _journals.value)
            _weeklyReview.value = res.getOrNull()
            _isGeneratingWeeklyReview.value = false
        }
    }

    // Paper Trading Actions
    fun closeTrade(tradeId: String, exitPrice: Double, status: TradeStatus) {
        _paperTrades.value = _paperTrades.value.map { trade ->
            if (trade.id == tradeId) {
                val pnl = when (trade.direction) {
                    TradeDirection.LONG -> (exitPrice - trade.entryPrice) * trade.positionSizeUnits
                    TradeDirection.SHORT -> (trade.entryPrice - exitPrice) * trade.positionSizeUnits
                }
                trade.copy(
                    status = status,
                    exitPrice = exitPrice,
                    pnl = pnl,
                    exitDate = System.currentTimeMillis()
                )
            } else trade
        }
    }

    fun deleteTrade(tradeId: String) {
        _paperTrades.value = _paperTrades.value.filterNot { it.id == tradeId }
    }

    // Journal Actions
    fun addJournalEntry(entry: JournalEntry) {
        _journals.value = listOf(entry) + _journals.value
    }

    fun deleteJournalEntry(entryId: String) {
        _journals.value = _journals.value.filterNot { it.id == entryId }
    }

    // Reset & Demo Data
    fun resetToDemoData() {
        _paperTrades.value = DemoData.getInitialTrades()
        _journals.value = DemoData.getInitialJournals()
        _gatekeeperResult.value = null
        _chartAnalysis.value = null
        _weeklyReview.value = null
    }

    fun clearAllData() {
        _paperTrades.value = emptyList()
        _journals.value = emptyList()
        _gatekeeperResult.value = null
        _chartAnalysis.value = null
        _weeklyReview.value = null
    }
}
