package com.ustaquery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustaquery.ui.theme.TennisGreen

@Composable
fun LevelBadge(level: String?, modifier: Modifier = Modifier) {
    val text = level ?: "?"
    val color = when {
        text.startsWith("L1") -> Color(0xFF7B1FA2)
        text.startsWith("L2") -> Color(0xFF303F9F)
        text.startsWith("L3") -> Color(0xFF1976D2)
        text.startsWith("L4") -> Color(0xFF388E3C)
        text.startsWith("L5") -> Color(0xFFFBC02D)
        text.startsWith("L6") -> Color(0xFFF57C00)
        text.startsWith("L7") -> Color(0xFFD32F2F)
        else -> TennisGreen
    }
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
