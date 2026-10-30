package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.Translations
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.Slate700
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import coil.imageLoader
import coil.request.CachePolicy
import com.example.model.Expense

@Composable
fun CalendarWidget(
    currentCalendar: Calendar,
    selectedCalendar: Calendar,
    expenses: List<Expense> = emptyList(),
    isDateSelected: Boolean = false,
    language: String,
    onDaySelected: (Calendar) -> Unit,
    onMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentYear = currentCalendar.get(Calendar.YEAR)
    val currentMonth = currentCalendar.get(Calendar.MONTH)
    val photo = remember(currentYear, currentMonth) {
        getCalendarPhoto(currentYear, currentMonth)
    }
    val photoTitle = if (language == "hi") photo.titleHi else photo.titleEn

    val monthFormat = SimpleDateFormat("MMMM yyyy", if (language == "hi") Locale("hi", "IN") else Locale.ENGLISH)
    val monthTitle = monthFormat.format(currentCalendar.time)

    val context = LocalContext.current

    // Preload adjacent months into memory cache so changing months is instant
    LaunchedEffect(currentYear, currentMonth) {
        val prevPhoto = getCalendarPhoto(
            if (currentMonth == 0) currentYear - 1 else currentYear,
            if (currentMonth == 0) 11 else currentMonth - 1
        )
        val nextPhoto = getCalendarPhoto(
            if (currentMonth == 11) currentYear + 1 else currentYear,
            if (currentMonth == 11) 0 else currentMonth + 1
        )
        val imageLoader = context.imageLoader
        val prevReq = ImageRequest.Builder(context)
            .data(prevPhoto.url)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        val nextReq = ImageRequest.Builder(context)
            .data(nextPhoto.url)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
        imageLoader.enqueue(prevReq)
        imageLoader.enqueue(nextReq)
    }

    val todayCalendar = Calendar.getInstance()
    val isCurrentRealMonth = todayCalendar.get(Calendar.YEAR) == currentYear &&
            todayCalendar.get(Calendar.MONTH) == currentMonth
    val todayDate = todayCalendar.get(Calendar.DAY_OF_MONTH)

    val calHelper = currentCalendar.clone() as Calendar
    calHelper.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calHelper.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
    val daysInMonth = calHelper.getActualMaximum(Calendar.DAY_OF_MONTH)

    val isSelMonth = selectedCalendar.get(Calendar.YEAR) == currentYear &&
            selectedCalendar.get(Calendar.MONTH) == currentMonth
    val selectedDay = if (isSelMonth) selectedCalendar.get(Calendar.DAY_OF_MONTH) else -1

    val expenseDaysInMonth = remember(expenses, currentYear, currentMonth) {
        val set = mutableSetOf<Int>()
        val tempCal = Calendar.getInstance()
        for (e in expenses) {
            if (!e.isDeleted) {
                tempCal.timeInMillis = e.timestamp
                if (tempCal.get(Calendar.YEAR) == currentYear && tempCal.get(Calendar.MONTH) == currentMonth) {
                    set.add(tempCal.get(Calendar.DAY_OF_MONTH))
                }
            }
        }
        set
    }

    Box(
        modifier = modifier
            .width(340.dp)
            .height(246.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, AppTheme.colors.border, RoundedCornerShape(16.dp))
            .testTag("calendar_card_widget")
    ) {
        // 1. Instant Themed Scenic Gradient Base (GUARANTEES never blank/black even offline or during transition)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(photo.gradient))
        )

        // 2. High-speed Cached Background Photo
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(photo.url)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = photoTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Contrast Gradient Overlay for crystal clear readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.78f),
                            Color.Black.copy(alpha = 0.94f)
                        )
                    )
                )
        )

        // Calendar Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Calendar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = monthTitle,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = photoTitle,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onMonthChange(-1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onMonthChange(1) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Weekday Headers
            val weekdays = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Calendar Days Grid (up to 5-6 rows)
            val totalCells = firstDayOfWeek + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNum = cellIndex - firstDayOfWeek + 1
                            if (dayNum in 1..daysInMonth) {
                                val isToday = isCurrentRealMonth && dayNum == todayDate
                                val isSelected = isDateSelected && isSelMonth && dayNum == selectedDay
                                val hasExpense = dayNum in expenseDaysInMonth

                                val bgColor = when {
                                    isToday -> BlueAccent
                                    isSelected -> Color.White.copy(alpha = 0.3f)
                                    else -> Color.Transparent
                                }
                                val textColor = when {
                                    isToday -> Color.White
                                    isSelected -> Color.White
                                    hasExpense -> Color(0xFFFF4D4D)
                                    else -> Color.White.copy(alpha = 0.9f)
                                }

                                Column(
                                    modifier = Modifier
                                        .size(width = 28.dp, height = 26.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(bgColor)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(1.dp, Color.White, RoundedCornerShape(6.dp))
                                            } else if (hasExpense && !isToday) {
                                                Modifier.border(1.dp, Color(0xFFEF4444).copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                                            } else Modifier
                                        )
                                        .clickable {
                                            val newCal = currentCalendar.clone() as Calendar
                                            newCal.set(Calendar.DAY_OF_MONTH, dayNum)
                                            onDaySelected(newCal)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        color = textColor,
                                        fontSize = 10.sp,
                                        fontWeight = if (isToday || isSelected || hasExpense) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center
                                    )
                                    if (hasExpense) {
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444))
                                        )
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.size(width = 28.dp, height = 26.dp))
                            }
                        }
                    }
                }
            }

            // Bottom Note
            Text(
                text = "🔴 " + (if (language == "hi") "लाल बिंदु = खर्च वाले दिन • $photoTitle" else "Red point = dates with expenses • $photoTitle"),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 9.sp,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
