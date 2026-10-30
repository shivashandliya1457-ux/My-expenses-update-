package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseRepository
import com.example.model.Translations
import com.example.ui.theme.AppTheme
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.Note200Orange
import com.example.ui.theme.Note500Green
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.model.Expense

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    repository: ExpenseRepository,
    initialDate: Calendar,
    currency: String,
    language: String,
    expenseToEdit: Expense? = null,
    onBack: () -> Unit,
    onExpenseSaved: () -> Unit
) {
    val context = LocalContext.current
    val isEditing = expenseToEdit != null

    var amount by remember {
        mutableStateOf(
            if (expenseToEdit != null && !expenseToEdit.category.equals("Udhar", ignoreCase = true)) {
                if (expenseToEdit.amount % 1.0 == 0.0) expenseToEdit.amount.toLong().toString() else expenseToEdit.amount.toString()
            } else ""
        )
    }
    var category by remember { mutableStateOf(expenseToEdit?.category ?: "Food") }
    var note by remember { mutableStateOf(expenseToEdit?.note ?: "") }
    var paymentMethod by remember { mutableStateOf(expenseToEdit?.paymentMethod ?: "Cash") }
    var person by remember { mutableStateOf(expenseToEdit?.person ?: "") }
    var given by remember {
        mutableStateOf(
            if (expenseToEdit != null && expenseToEdit.given > 0) {
                if (expenseToEdit.given % 1.0 == 0.0) expenseToEdit.given.toLong().toString() else expenseToEdit.given.toString()
            } else ""
        )
    }
    var returned by remember {
        mutableStateOf(
            if (expenseToEdit != null && expenseToEdit.returned > 0) {
                if (expenseToEdit.returned % 1.0 == 0.0) expenseToEdit.returned.toLong().toString() else expenseToEdit.returned.toString()
            } else ""
        )
    }

    var selectedCalendar by remember {
        mutableStateOf(
            if (expenseToEdit != null) {
                Calendar.getInstance().apply { timeInMillis = expenseToEdit.timestamp }
            } else {
                initialDate.clone() as Calendar
            }
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = listOf(
        "Food", "Travel", "Rent", "Investment", "Self Transfer", "Sent to Home", "Udhar", "Shopping", "Bills"
    )
    val paymentMethods = listOf("Cash", "UPI", "Card", "Net Banking")

    val isUdhar = category.equals("Udhar", ignoreCase = true)
    val remainingCalculated by remember {
        derivedStateOf {
            val g = given.toDoubleOrNull() ?: 0.0
            val r = returned.toDoubleOrNull() ?: 0.0
            g - r
        }
    }

    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val colors = AppTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(18.dp)
            .testTag("add_expense_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Back Button
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
                text = Translations.get(if (isEditing) "edit_expense_title" else "add_expense_title", language),
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Amount Input (If not Udhar)
            if (!isUdhar) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("${Translations.get("amount", language)} ($currency)", color = colors.textSecondary) },
                    placeholder = { Text("0.00", color = colors.textMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BlueAccent,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.inputContainer,
                        unfocusedContainerColor = colors.inputContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_amount")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Category Selection Chips
            Text(
                text = Translations.get("category", language),
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = category.equals(cat, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) BlueAccent else colors.surface)
                            .border(1.dp, if (isSelected) BlueAccent else colors.border, RoundedCornerShape(16.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("chip_cat_$cat")
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color.White else colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Udhar Specific Fields
            if (isUdhar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, Note200Orange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Udhar Debt Ledger (उधार खाता)",
                            color = Note200Orange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = person,
                            onValueChange = { person = it },
                            label = { Text(Translations.get("person_name", language), color = colors.textSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BlueAccent,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedContainerColor = colors.surfaceSecondary,
                                unfocusedContainerColor = colors.surfaceSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = given,
                                onValueChange = { given = it },
                                label = { Text(Translations.get("amount_given", language), color = colors.textSecondary, fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BlueAccent,
                                    unfocusedBorderColor = colors.border,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedContainerColor = colors.surfaceSecondary,
                                    unfocusedContainerColor = colors.surfaceSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = returned,
                                onValueChange = { returned = it },
                                label = { Text(Translations.get("amount_returned", language), color = colors.textSecondary, fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BlueAccent,
                                    unfocusedBorderColor = colors.border,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary,
                                    focusedContainerColor = colors.surfaceSecondary,
                                    unfocusedContainerColor = colors.surfaceSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${Translations.get("remaining_balance", language)}: $currency${"%,.0f".format(remainingCalculated)}",
                                color = Note500Green,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Translations.get("udhar_note", language),
                            color = colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Payment Method Chips
            Text(
                text = Translations.get("payment_method", language),
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paymentMethods.forEach { method ->
                    val isSelected = paymentMethod == method
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) BlueAccent else colors.surface)
                            .border(1.dp, if (isSelected) BlueAccent else colors.border, RoundedCornerShape(10.dp))
                            .clickable { paymentMethod = method }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = method,
                            color = if (isSelected) Color.White else colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note / Description Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(Translations.get("note_description", language), color = colors.textSecondary) },
                singleLine = false,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueAccent,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.inputContainer,
                    unfocusedContainerColor = colors.inputContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_note")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .clickable { showDatePicker = true }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Date (तारीख)", color = colors.textSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dateFormat.format(selectedCalendar.time),
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = BlueAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    val parsedAmount = if (isUdhar) {
                        val g = given.toDoubleOrNull() ?: 0.0
                        val r = returned.toDoubleOrNull() ?: 0.0
                        g - r
                    } else {
                        amount.toDoubleOrNull() ?: -1.0
                    }

                    if (!isUdhar && parsedAmount <= 0) {
                        Toast.makeText(context, "Enter valid Amount", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (isUdhar && person.isBlank()) {
                        Toast.makeText(context, "Enter Person Name for Udhar", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val gVal = given.toDoubleOrNull() ?: 0.0
                    val rVal = returned.toDoubleOrNull() ?: 0.0

                    if (isEditing && expenseToEdit != null) {
                        val updated = expenseToEdit.copy(
                            amount = if (isUdhar) parsedAmount else parsedAmount,
                            category = category,
                            note = note,
                            paymentMethod = paymentMethod,
                            dateIso = selectedCalendar.time.toInstant().toString(),
                            timestamp = selectedCalendar.timeInMillis,
                            person = person.trim(),
                            given = gVal,
                            returned = rVal,
                            remaining = if (isUdhar) gVal - rVal else 0.0
                        )
                        repository.updateExpense(updated)
                        Toast.makeText(context, "Expense Updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        repository.addOrUpdateExpense(
                            amount = if (isUdhar) parsedAmount else parsedAmount,
                            category = category,
                            note = note,
                            paymentMethod = paymentMethod,
                            dateIso = selectedCalendar.time.toInstant().toString(),
                            timestamp = selectedCalendar.timeInMillis,
                            person = person.trim(),
                            given = gVal,
                            returned = rVal
                        )
                        Toast.makeText(context, "Expense Added!", Toast.LENGTH_SHORT).show()
                    }

                    onExpenseSaved()
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_expense")
            ) {
                Text(
                    text = Translations.get(if (isEditing) "update_expense" else "save_expense", language),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedCalendar.timeInMillis
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedCalendar.timeInMillis = it
                        }
                        showDatePicker = false
                    }) {
                        Text("OK", color = BlueAccent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = Slate400)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
