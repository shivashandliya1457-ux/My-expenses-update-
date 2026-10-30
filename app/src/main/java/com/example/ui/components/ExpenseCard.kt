package com.example.ui.components

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Expense
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseCard(
    expense: Expense,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val isUdhar = expense.category.equals("Udhar", ignoreCase = true)
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(expense.timestamp))

    // Determine category icon and color
    val (catIcon, catBg, catColor) = when (expense.category.lowercase()) {
        "food" -> Triple(Icons.Default.Restaurant, Color(0x3322C55E), Note500Green)
        "travel" -> Triple(Icons.Default.DirectionsCar, Color(0x333B82F6), BlueAccent)
        "rent" -> Triple(Icons.Default.Home, Color(0x338B5CF6), PurpleAccent)
        "investment" -> Triple(Icons.Default.TrendingUp, Color(0x3310B981), EmeraldSuccess)
        "self transfer" -> Triple(Icons.Default.SyncAlt, Color(0x3306B6D4), Color(0xFF06B6D4))
        "sent to home" -> Triple(Icons.Default.FamilyRestroom, Color(0x33EC4899), Color(0xFFEC4899))
        "udhar" -> Triple(Icons.Default.Handshake, Color(0x33FB923C), Note200Orange)
        "shopping" -> Triple(Icons.Default.ShoppingBag, Color(0x33EAB308), Note100Yellow)
        "bills" -> Triple(Icons.Default.ReceiptLong, Color(0x33F97316), Color(0xFFF97316))
        else -> Triple(Icons.Default.Wallet, Color(0x333B82F6), BlueAccent)
    }

    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isHighlighted) colors.surfaceSecondary else colors.surface)
            .border(
                width = if (isHighlighted) 1.5.dp else 1.dp,
                color = when {
                    isHighlighted -> Color(0xFFEF4444)
                    isUdhar -> Note200Orange.copy(alpha = 0.4f)
                    else -> colors.border
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable {
                if (expense.note.isNotBlank()) {
                    isExpanded = !isExpanded
                }
            }
            .padding(12.dp)
            .testTag("expense_card_${expense.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (isHighlighted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "★ Selected Date Entry",
                            color = Color(0xFFEF4444),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(catBg)
                        .border(1.dp, catColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = catIcon,
                        contentDescription = expense.category,
                        tint = catColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Details Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${expense.category} • $currency${"%,.0f".format(if (isUdhar) expense.remaining else expense.amount)}",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (isUdhar) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Note200Orange.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Due: $currency${"%,.0f".format(expense.remaining)}",
                                    color = Note200Orange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    val detailParts = mutableListOf<String>()
                    if (expense.person.isNotBlank()) detailParts.add(expense.person)
                    detailParts.add(expense.paymentMethod)
                    detailParts.add(formattedDate)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = detailParts.joinToString(" • "),
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                        if (expense.note.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isExpanded) "▲ Hide" else "▼ Note",
                                color = BlueAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isUdhar) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Given: $currency${"%,.0f".format(expense.given)} | Returned: $currency${"%,.0f".format(expense.returned)}",
                            color = colors.textMuted,
                            fontSize = 10.sp
                        )
                    }
                }

            // Action Buttons
            Row(
                modifier = Modifier.padding(start = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If Udhar: Share Payment Slip / Polite WhatsApp Reminder Button
                if (isUdhar) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceSecondary)
                            .border(1.dp, Note200Orange.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                val slipText = """
                                🧾 Payment Reminder
                                Hi ${expense.person.ifBlank { "Friend" }},
                                Here is the transaction summary from My Expenses Report:
                                • Amount Given: $currency${"%,.0f".format(expense.given)}
                                • Amount Returned: $currency${"%,.0f".format(expense.returned)}
                                • Balance Remaining: $currency${"%,.0f".format(expense.remaining)}
                                
                                Recorded on: $formattedDate
                                Thank you!
                                """.trimIndent()
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, slipText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Udhar Reminder"))
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Reminder Slip",
                                tint = Note200Orange,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Edit Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceSecondary)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Expense",
                            tint = BlueAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Delete Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceSecondary)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Expense",
                            tint = RedDanger.copy(alpha = 0.85f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // Expanded Description Block (shown only on card click)
        if (isExpanded && expense.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceSecondary)
                    .border(1.dp, BlueAccent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Description",
                        tint = BlueAccent,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Description (विवरण):",
                            color = BlueAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = expense.note,
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
}


