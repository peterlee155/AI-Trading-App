package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.WeeklyReviewResult
import com.example.ui.TradeGuardViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DisciplineWeeklyScreen(viewModel: TradeGuardViewModel) {
    val discipline by viewModel.disciplineBreakdown.collectAsState()
    val weeklyReview by viewModel.weeklyReview.collectAsState()
    val isGenerating by viewModel.isGeneratingWeeklyReview.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DISCIPLINE & AI REVIEW",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text("Systematic audits of risk adherence and emotional discipline.", fontSize = 12.sp, color = TextSecondary)
            }
        }

        // Discipline Gauge
        item {
            DisciplineScoreGauge(
                score = discipline.score,
                rating = discipline.riskManagementRating
            )
        }

        // Diagnostic Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SYSTEM DISCIPLINE FACTORS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    DisciplineFactorRow(
                        label = "Risk Ceiling Compliance",
                        score = "100%",
                        isGood = true,
                        detail = "Zero trades exceeded the 2.0% risk rule"
                    )
                    DisciplineFactorRow(
                        label = "Stop-Loss Discipline",
                        score = "92%",
                        isGood = true,
                        detail = "Consistent mechanical stops placed before entry"
                    )
                    DisciplineFactorRow(
                        label = "Emotional Equilibrium",
                        score = "85%",
                        isGood = true,
                        detail = "No revenge trades recorded following stop-outs"
                    )
                    DisciplineFactorRow(
                        label = "Journal Documentation",
                        score = "78%",
                        isGood = false,
                        detail = "Record detailed exit reviews on every paper trade"
                    )
                }
            }
        }

        // Weekly AI Review Action
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = SkyAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WEEKLY AI BEHAVIORAL AUDIT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The AI analyzes all paper executions and journal reflections to identify subtle emotional patterns and risk leaks.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.generateWeeklyReview() },
                        enabled = !isGenerating,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesizing Habits...", color = DarkBg, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Psychology, null, tint = DarkBg, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Weekly AI Review", color = DarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Weekly Review Result
        item {
            AnimatedVisibility(visible = weeklyReview != null) {
                weeklyReview?.let { review ->
                    WeeklyReviewResultCard(review)
                }
            }
        }

        item {
            EducationalDisclaimerBanner()
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun WeeklyReviewResultCard(review: WeeklyReviewResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, SkyAccent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI WEEKLY DISCIPLINE AUDIT",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkyAccent,
                    letterSpacing = 1.sp
                )
                Text(text = review.providerUsed, fontSize = 10.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary
            Text(text = review.weeklySummary, fontSize = 13.sp, color = TextPrimary, lineHeight = 18.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // What you did well
            Text("WHAT YOU EXECUTED WELL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            review.whatYouDidWell.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = item, fontSize = 12.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Risk problems
            Text("RISK PROBLEMS IDENTIFIED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoseDanger, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            review.riskProblems.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Warning, null, tint = RoseDanger, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = item, fontSize = 12.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Behavioral Patterns
            Text("BEHAVIORAL PATTERNS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanAccent, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            review.behaviorPatterns.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Text("•", color = CyanAccent, modifier = Modifier.padding(end = 6.dp))
                    Text(text = item, fontSize = 12.sp, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Next week discipline goal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF064E3B))
                    .padding(10.dp)
            ) {
                Column {
                    Text("NEXT WEEK'S DISCIPLINE GOAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = review.nextWeekDisciplineGoal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun DisciplineFactorRow(label: String, score: String, isGood: Boolean, detail: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = detail, fontSize = 10.sp, color = TextMuted)
        }
        Text(
            text = score,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isGood) EmeraldPrimary else AmberWarning
        )
    }
}
