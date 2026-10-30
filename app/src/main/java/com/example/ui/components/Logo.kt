package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Note100Yellow
import com.example.ui.theme.Note200Orange
import com.example.ui.theme.Note500Green

@Composable
fun AppLogo(
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val scaleFactor = size / 80.dp

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 16.dp * scaleFactor,
                shape = RoundedCornerShape(18.dp * scaleFactor),
                ambientColor = Color(0x663B82F6),
                spotColor = Color(0x668B5CF6)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6))
                ),
                shape = RoundedCornerShape(18.dp * scaleFactor)
            )
            .border(
                width = 1.dp * scaleFactor,
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp * scaleFactor)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating Notes Behind Clasp
        // 100 Yellow Note
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-16).dp * scaleFactor, y = 10.dp * scaleFactor)
                .rotate(4f)
                .size(width = 18.dp * scaleFactor, height = 24.dp * scaleFactor)
                .background(Note100Yellow, RoundedCornerShape(2.dp * scaleFactor))
                .shadow(2.dp * scaleFactor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "100",
                fontSize = (7f * scaleFactor).sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        // 200 Orange Note
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp * scaleFactor, y = 4.dp * scaleFactor)
                .rotate(8f)
                .size(width = 20.dp * scaleFactor, height = 28.dp * scaleFactor)
                .background(Note200Orange, RoundedCornerShape(2.5.dp * scaleFactor))
                .shadow(3.dp * scaleFactor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "200",
                fontSize = (8f * scaleFactor).sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        // 500 Green Note
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-3).dp * scaleFactor, y = (-4).dp * scaleFactor)
                .rotate(12f)
                .size(width = 24.dp * scaleFactor, height = 32.dp * scaleFactor)
                .background(Note500Green, RoundedCornerShape(3.dp * scaleFactor))
                .shadow(4.dp * scaleFactor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "500",
                fontSize = (9f * scaleFactor).sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        // Main Wallet Icon
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = "Wallet Icon",
            tint = Color.White,
            modifier = Modifier.size(36.dp * scaleFactor)
        )
    }
}
