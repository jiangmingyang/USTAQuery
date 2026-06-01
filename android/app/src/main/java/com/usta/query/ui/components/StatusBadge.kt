package com.usta.query.ui.components

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
import com.usta.query.ui.theme.LossRed
import com.usta.query.ui.theme.TennisGreen
import com.usta.query.ui.theme.WinGreen

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val s = status.lowercase()
    val (bg, fg) = when {
        s.contains("open") -> Color(0xFFE8F5E9) to WinGreen
        s.contains("closed") -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        s.contains("completed") -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        s.contains("cancelled") -> Color(0xFFFFEBEE) to LossRed
        s.contains("accept") -> Color(0xFFE8F5E9) to TennisGreen
        s.contains("alternate") -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        s.contains("withdrawn") -> Color(0xFFFFEBEE) to LossRed
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = status,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = fg,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
