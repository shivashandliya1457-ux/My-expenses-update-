package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Translations
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Note100Yellow
import com.example.ui.theme.Note200Orange
import com.example.ui.theme.RedDanger
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.Calendar
import kotlin.math.min

@Composable
fun SummaryCards(
    monthSum: Double,
    totalSum: Double,
    yearlySum: Double,
    budget: Double,
    currency: String,
    sumType: String,
    language: String,
    onSumTypeChange: (String) -> Unit,
    onOpenTrustCenter: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val displaySum = when (sumType) {
        "total" -> totalSum
        "yearly" -> yearlySum
        else -> monthSum
    }

    val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
    val dailyBurnAvg = monthSum / todayDay
    val budgetPercent = if (budget > 0) (monthSum / budget) * 100 else 0.0

    val (healthLabel, healthColor) = when {
        budgetPercent < 60.0 -> Pair("Healthy Pace 🟢", EmeraldSuccess)
        budgetPercent < 85.0 -> Pair("Moderate 🟡", Note100Yellow)
        else -> Pair("High Spending 🔴", RedDanger)
    }

    val colors = AppTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        // 3 Cards Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Monthly Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .padding(10.dp)
                    .testTag("summary_card_monthly")
            ) {
                Column {
                    Text(
                        text = Translations.get("monthly", language),
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currency${"%,.0f".format(monthSum)}",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Progress Bar towards budget
                    val progressRatio = if (budget > 0) min((monthSum / budget).toFloat(), 1f) else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.surfaceSecondary)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressRatio)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (monthSum > budget) RedDanger else BlueAccent)
                        )
                    }
                }
            }

            // Total Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .padding(10.dp)
                    .testTag("summary_card_total")
            ) {
                Column {
                    Text(
                        text = Translations.get("total", language),
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currency${"%,.0f".format(totalSum)}",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Yearly Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .padding(10.dp)
                    .testTag("summary_card_yearly")
            ) {
                Column {
                    Text(
                        text = Translations.get("yearly", language),
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$currency${"%,.0f".format(yearlySum)}",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Financial Health & Spending Velocity Aesthetic Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            colors.surface,
                            if (colors.isDark) Color(0xFF1E3A8A).copy(alpha = 0.35f) else Color(0xFFDBEAFE).copy(alpha = 0.6f)
                        )
                    )
                )
                .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Pace",
                        tint = healthColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = healthLabel,
                        color = colors.textPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Velocity: $currency${"%,.0f".format(dailyBurnAvg)} / day avg",
                    color = colors.textSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val options = listOf("monthly", "total", "yearly")
            options.forEach { opt ->
                val isSelected = sumType == opt
                val label = Translations.get(opt, language)
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) BlueAccent else colors.surface)
                        .border(1.dp, if (isSelected) BlueAccent else colors.border, RoundedCornerShape(16.dp))
                        .clickable { onSumTypeChange(opt) }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                        .testTag("filter_chip_$opt")
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${Translations.get("budget", language)}: $currency${"%,.0f".format(budget)} | $currency${"%,.0f".format(displaySum)} ${Translations.get("showing", language)}",
                color = colors.textMuted,
                fontSize = 10.sp
            )
        }
    }
}
