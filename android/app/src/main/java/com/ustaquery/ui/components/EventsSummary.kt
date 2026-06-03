package com.ustaquery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustaquery.data.model.TournamentEvent

private val EVENT_GENDER_MAP = mapOf(
    "M" to "Boys", "Male" to "Boys", "male" to "Boys", "boys" to "Boys", "Boys" to "Boys",
    "F" to "Girls", "Female" to "Girls", "female" to "Girls", "girls" to "Girls", "Girls" to "Girls",
    "Coed" to "Coed", "coed" to "Coed",
    "Mixed" to "Mixed", "mixed" to "Mixed"
)

private val BALL_COLOR_MAP = mapOf(
    "Yellow" to Color(0xFFEAB308), "yellow" to Color(0xFFEAB308),
    "Green" to Color(0xFF22C55E), "green" to Color(0xFF22C55E),
    "Orange" to Color(0xFFF97316), "orange" to Color(0xFFF97316),
    "Red" to Color(0xFFEF4444), "red" to Color(0xFFEF4444)
)

private fun compactEventLabel(event: TournamentEvent): String {
    val age = event.ageCategory ?: ""
    val type = when {
        event.eventType?.lowercase()?.startsWith("s") == true -> "S"
        event.eventType?.lowercase()?.startsWith("d") == true -> "D"
        else -> ""
    }
    return listOf(age, type).filter { it.isNotBlank() }.joinToString(" ")
}

fun compactPlayerEventLabel(eventGender: String?, eventAgeCategory: String?, eventType: String?): String {
    val gender = EVENT_GENDER_MAP[eventGender ?: ""] ?: eventGender ?: ""
    val age = eventAgeCategory ?: ""
    val type = when {
        eventType?.lowercase()?.startsWith("s") == true -> "S"
        eventType?.lowercase()?.startsWith("d") == true -> "D"
        else -> eventType ?: ""
    }
    return listOf(gender, age, type).filter { it.isNotBlank() }.joinToString(" ")
}

private val GENDER_ORDER = listOf("Boys", "Girls", "Coed", "Mixed")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventsSummary(events: List<TournamentEvent>, modifier: Modifier = Modifier) {
    if (events.isEmpty()) return

    val grouped = events.groupBy { EVENT_GENDER_MAP[it.gender ?: ""] ?: it.gender ?: "Other" }
    val sortedGenders = GENDER_ORDER.filter { grouped.containsKey(it) } +
        (grouped.keys - GENDER_ORDER.toSet())

    Column(modifier = modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        for (gender in sortedGenders) {
            val genderEvents = grouped[gender] ?: continue
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    gender,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (event in genderEvents) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(compactEventLabel(event), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            BALL_COLOR_MAP[event.ballColor ?: ""]?.let { color ->
                                Canvas(modifier = Modifier.size(5.dp)) {
                                    drawCircle(color = color)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
