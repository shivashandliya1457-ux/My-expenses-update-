package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Expense
import com.example.model.Translations
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Note100Yellow
import com.example.ui.theme.Note200Orange
import com.example.ui.theme.Note500Green
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RedDanger
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.Calendar
import kotlin.math.min

@Composable
fun ReportsScreen(
    expenses: List<Expense>,
    currency: String,
    budget: Double,
    language: String,
    onBack: () -> Unit
) {
    val totalSum = expenses.fold(0.0) { acc, e ->
        if (e.category.equals("Udhar", ignoreCase = true)) acc + e.remaining else acc + e.amount
    }

    val cal = Calendar.getInstance()
    val curMonth = cal.get(Calendar.MONTH)
    val curYear = cal.get(Calendar.YEAR)

    val monthSum = expenses.filter {
        val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        c.get(Calendar.MONTH) == curMonth && c.get(Calendar.YEAR) == curYear
    }.fold(0.0) { acc, e ->
        if (e.category.equals("Udhar", ignoreCase = true)) acc + e.remaining else acc + e.amount
    }

    // Category breakdown
    val categoryTotals = mutableMapOf<String, Double>()
    for (e in expenses) {
        val amt = if (e.category.equals("Udhar", ignoreCase = true)) e.remaining else e.amount
        categoryTotals[e.category] = (categoryTotals[e.category] ?: 0.0) + amt
    }

    // Payment methods breakdown
    val paymentTotals = mutableMapOf<String, Double>()
    for (e in expenses) {
        val amt = if (e.category.equals("Udhar", ignoreCase = true)) e.remaining else e.amount
        paymentTotals[e.paymentMethod] = (paymentTotals[e.paymentMethod] ?: 0.0) + amt
    }

    // Udhar Specifics
    val udharItems = expenses.filter { it.category.equals("Udhar", ignoreCase = true) }
    val totalGiven = udharItems.sumOf { it.given }
    val totalReturned = udharItems.sumOf { it.returned }
    val netUdharRemaining = udharItems.sumOf { it.remaining }

    val categoryColors = listOf(
        BlueAccent, PurpleAccent, Note500Green, Note200Orange,
        Note100Yellow, EmeraldSuccess, RedDanger, Color(0xFF06B6D4)
    )

    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(18.dp)
            .testTag("reports_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Translations.get("back_one_step", language),
                    color = colors.textPrimary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = Translations.get("reports", language),
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Monthly Budget Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Spending vs Budget",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$currency${"%,.0f".format(monthSum)} / $currency${"%,.0f".format(budget)}",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val percent = if (budget > 0) min((monthSum / budget).toFloat(), 1f) else 0f
                    val progressColor = when {
                        monthSum > budget -> RedDanger
                        monthSum > budget * 0.8 -> Note200Orange
                        else -> BlueAccent
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceSecondary)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percent)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(progressColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val remainingBudget = budget - monthSum
                    Text(
                        text = if (remainingBudget >= 0) "Budget Remaining: $currency${"%,.0f".format(remainingBudget)}"
                        else "Over Budget by: $currency${"%,.0f".format(-remainingBudget)}",
                        color = if (remainingBudget >= 0) EmeraldSuccess else RedDanger,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Distribution
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Categories",
                            tint = BlueAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Category Breakdown",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (categoryTotals.isEmpty()) {
                        Text(
                            text = Translations.get("no_expenses", language),
                            color = colors.textMuted,
                            fontSize = 12.sp
                        )
                    } else {
                        categoryTotals.entries.sortedByDescending { it.value }.forEachIndexed { idx, (cat, amt) ->
                            val color = categoryColors[idx % categoryColors.size]
                            val catRatio = if (totalSum > 0) (amt / totalSum).toFloat() else 0f

                            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = cat, color = colors.textPrimary, fontSize = 12.sp)
                                    Text(
                                        text = "$currency${"%,.0f".format(amt)} (${(catRatio * 100).toInt()}%)",
                                        color = colors.textSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(colors.surfaceSecondary)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(min(catRatio, 1f))
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Udhar Ledger Tracker Summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, Note200Orange.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = "Udhar",
                            tint = Note200Orange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Translations.get("udhar_summary", language),
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Given", color = colors.textSecondary, fontSize = 10.sp)
                            Text(
                                text = "$currency${"%,.0f".format(totalGiven)}",
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "Total Returned", color = colors.textSecondary, fontSize = 10.sp)
                            Text(
                                text = "$currency${"%,.0f".format(totalReturned)}",
                                color = EmeraldSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(text = "Pending / Due", color = colors.textSecondary, fontSize = 10.sp)
                            Text(
                                text = "$currency${"%,.0f".format(netUdharRemaining)}",
                                color = Note200Orange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (udharItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Person-wise Outstanding:", color = colors.textSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        udharItems.forEach { u ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = u.person.ifBlank { "Unknown" }, color = colors.textPrimary, fontSize = 12.sp)
                                Text(
                                    text = "Due: $currency${"%,.0f".format(u.remaining)}",
                                    color = if (u.remaining > 0) Note200Orange else EmeraldSuccess,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Methods Summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "Payment Modes Breakdown",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (paymentTotals.isEmpty()) {
                        Text(text = "No records yet", color = colors.textMuted, fontSize = 11.sp)
                    } else {
                        paymentTotals.forEach { (mode, amt) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = mode, color = colors.textSecondary, fontSize = 12.sp)
                                Text(
                                    text = "$currency${"%,.0f".format(amt)}",
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
