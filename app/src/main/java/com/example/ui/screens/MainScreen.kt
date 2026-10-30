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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import com.example.ui.theme.AppTheme
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Note200Orange
import com.example.ui.theme.PurpleAccent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseRepository
import com.example.model.AppConfig
import com.example.model.Expense
import com.example.model.Translations
import com.example.ui.components.AppLogo
import com.example.ui.components.CalendarWidget
import com.example.ui.components.ExpenseCard
import com.example.ui.components.SummaryCards
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    repository: ExpenseRepository,
    expenses: List<Expense>,
    config: AppConfig,
    onNavigateToAdd: (Calendar) -> Unit,
    onNavigateToEdit: (Expense) -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTrustCenter: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var sumType by remember { mutableStateOf(config.summaryType) }
    var language by remember { mutableStateOf(config.language) }

    // Calendar states
    val todayCal = remember { Calendar.getInstance() }
    var calendarMonthView by remember { mutableStateOf(todayCal.clone() as Calendar) }
    var selectedCalendarDate by remember { mutableStateOf(todayCal.clone() as Calendar) }
    var filterByDay by remember { mutableStateOf(false) }

    // Calculate sums
    val totalSum = expenses.fold(0.0) { acc, e ->
        if (e.category.equals("Udhar", ignoreCase = true)) acc + e.remaining else acc + e.amount
    }

    val calNow = Calendar.getInstance()
    val curMonth = calNow.get(Calendar.MONTH)
    val curYear = calNow.get(Calendar.YEAR)

    val monthSum = expenses.filter {
        val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        c.get(Calendar.MONTH) == curMonth && c.get(Calendar.YEAR) == curYear
    }.fold(0.0) { acc, e ->
        if (e.category.equals("Udhar", ignoreCase = true)) acc + e.remaining else acc + e.amount
    }

    val yearlySum = expenses.filter {
        val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
        c.get(Calendar.YEAR) == curYear
    }.fold(0.0) { acc, e ->
        if (e.category.equals("Udhar", ignoreCase = true)) acc + e.remaining else acc + e.amount
    }

    // Search-filtered list
    val searchFilteredExpenses = expenses.filter { e ->
        if (search.isBlank()) true else {
            val q = search.trim().lowercase()
            e.category.lowercase().contains(q) ||
                    e.note.lowercase().contains(q) ||
                    e.person.lowercase().contains(q) ||
                    e.amount.toString().contains(q) ||
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(e.timestamp)).contains(q)
        }
    }

    // Partition when a specific date is selected so matching expenses are highlighted and pinned at top
    val (matchingDateExpenses, otherExpenses) = if (filterByDay) {
        val tempCal = Calendar.getInstance()
        val selYear = selectedCalendarDate.get(Calendar.YEAR)
        val selMonth = selectedCalendarDate.get(Calendar.MONTH)
        val selDay = selectedCalendarDate.get(Calendar.DAY_OF_MONTH)
        searchFilteredExpenses.partition { e ->
            tempCal.timeInMillis = e.timestamp
            tempCal.get(Calendar.YEAR) == selYear &&
                    tempCal.get(Calendar.MONTH) == selMonth &&
                    tempCal.get(Calendar.DAY_OF_MONTH) == selDay
        }
    } else {
        Pair(emptyList<Expense>(), searchFilteredExpenses)
    }

    val colors = AppTheme.colors

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAdd(selectedCalendarDate) },
                containerColor = BlueAccent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("fab_add_expense")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .border(1.dp, colors.border)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(size = 40.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = Translations.get("app_title", language),
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = Translations.get("designed_by", language),
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dark / Light Mode Quick Toggle Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceSecondary)
                                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                .clickable {
                                    repository.updateConfig(config.copy(isDarkMode = !config.isDarkMode))
                                }
                                .testTag("btn_dark_light_quick_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (config.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Dark / Light Mode",
                                tint = if (config.isDarkMode) PurpleAccent else Note200Orange,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // EN / HI Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surfaceSecondary)
                                .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                                .clickable {
                                    val newLang = if (language == "en") "hi" else "en"
                                    language = newLang
                                    repository.updateConfig(config.copy(language = newLang))
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("btn_lang_toggle")
                        ) {
                            Text(
                                text = if (language == "en") "HI" else "EN",
                                color = colors.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Settings Button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceSecondary)
                                .border(1.dp, colors.border, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = onNavigateToSettings,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Privacy & Trust Shield Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E3A8A).copy(alpha = 0.25f))
                    .border(1.dp, BlueAccent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .clickable { onNavigateToTrustCenter() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Zero Cloud Exposure • On-Device Biometric Safe",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Trust Center ➔",
                        color = BlueAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Search Bar & Reports Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TextField(
                            value = search,
                            onValueChange = { search = it },
                            placeholder = {
                                Text(
                                    text = Translations.get("search_placeholder", language),
                                    color = colors.textMuted,
                                    fontSize = 12.sp
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_input")
                        )
                        if (search.isNotBlank()) {
                            IconButton(
                                onClick = { search = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Reports Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onNavigateToReports,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Reports",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Summary Cards Component
            SummaryCards(
                monthSum = monthSum,
                totalSum = totalSum,
                yearlySum = yearlySum,
                budget = config.monthlyBudget,
                currency = config.currency,
                sumType = sumType,
                language = language,
                onSumTypeChange = {
                    sumType = it
                    repository.updateConfig(config.copy(summaryType = it))
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Calendar Widget (Centered 320-340px)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CalendarWidget(
                    currentCalendar = calendarMonthView,
                    selectedCalendar = selectedCalendarDate,
                    expenses = expenses,
                    isDateSelected = filterByDay,
                    language = language,
                    onDaySelected = { pickedCal ->
                        selectedCalendarDate = pickedCal
                        filterByDay = true
                    },
                    onMonthChange = { delta ->
                        val newCal = calendarMonthView.clone() as Calendar
                        newCal.add(Calendar.MONTH, delta)
                        calendarMonthView = newCal
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Expenses LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // If a date is clicked on calendar, show selected date focus banner & Add Expense button
                if (filterByDay) {
                    val dateHeaderFmt = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                    val dateShortFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                    item(key = "selected_date_header") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surface)
                                .border(
                                    1.dp,
                                    if (matchingDateExpenses.isNotEmpty()) Color(0xFFEF4444).copy(alpha = 0.5f) else colors.border,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                                .testTag("selected_date_focus_card")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Selected Date",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = dateHeaderFmt.format(selectedCalendarDate.time),
                                            color = colors.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = if (language == "hi") "सभी देखें (Show All)" else "Show All Dates",
                                        color = BlueAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clickable { filterByDay = false }
                                            .padding(4.dp)
                                            .testTag("btn_clear_date_filter")
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (matchingDateExpenses.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.surfaceSecondary)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "No expense on this specific date",
                                            color = Slate400,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                } else {
                                    Text(
                                        text = "★ ${matchingDateExpenses.size} ${if (matchingDateExpenses.size == 1) "entry" else "entries"} on this date (highlighted below):",
                                        color = Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Button(
                                    onClick = { onNavigateToAdd(selectedCalendarDate) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("btn_add_expense_for_selected_date")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+ Add Expense for ${dateShortFmt.format(selectedCalendarDate.time)}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Highlighted expenses for the selected date on top
                    items(matchingDateExpenses, key = { "match_${it.id}" }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            currency = config.currency,
                            onEdit = { onNavigateToEdit(expense) },
                            onDelete = { repository.deleteExpense(expense) },
                            isHighlighted = true
                        )
                    }

                    if (otherExpenses.isNotEmpty()) {
                        item(key = "other_expenses_divider") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Other Expenses (${otherExpenses.size})",
                                color = colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        items(otherExpenses, key = { "other_${it.id}" }) { expense ->
                            ExpenseCard(
                                expense = expense,
                                currency = config.currency,
                                onEdit = { onNavigateToEdit(expense) },
                                onDelete = { repository.deleteExpense(expense) },
                                isHighlighted = false
                            )
                        }
                    }
                } else {
                    // Normal list mode (all expenses)
                    if (searchFilteredExpenses.isEmpty()) {
                        item(key = "empty_state") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = Translations.get("no_expenses", language),
                                    color = Slate500,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Translations.get("add_first_expense", language),
                                    color = Slate500.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(searchFilteredExpenses, key = { it.id }) { expense ->
                            ExpenseCard(
                                expense = expense,
                                currency = config.currency,
                                onEdit = { onNavigateToEdit(expense) },
                                onDelete = { repository.deleteExpense(expense) },
                                isHighlighted = false
                            )
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}
