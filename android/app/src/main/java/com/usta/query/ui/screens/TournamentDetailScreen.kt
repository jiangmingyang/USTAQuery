package com.usta.query.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.usta.query.data.model.TournamentEntry
import com.usta.query.data.model.TournamentEvent
import com.usta.query.ui.components.EmptyStateView
import com.usta.query.ui.components.LevelBadge
import com.usta.query.ui.components.LoadingView
import com.usta.query.ui.components.StatusBadge
import com.usta.query.ui.theme.TennisGreen
import com.usta.query.viewmodel.DisplayRow
import com.usta.query.viewmodel.TournamentDetailViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TournamentDetailScreen(
    tournamentId: Int,
    onBack: () -> Unit,
    viewModel: TournamentDetailViewModel = viewModel(key = "$tournamentId") { TournamentDetailViewModel(tournamentId) }
) {
    LaunchedEffect(tournamentId) {
        viewModel.loadTournament()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.tournament?.name ?: "Tournament") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                viewModel.isLoading -> LoadingView()
                viewModel.error != null -> EmptyStateView(title = "Error", description = viewModel.error)
                viewModel.tournament == null -> EmptyStateView(title = "Not found", description = "Tournament not found")
                else -> {
                    val tournament = viewModel.tournament!!
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header info
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tournament.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    tournament.code?.let {
                                        Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                LevelBadge(level = tournament.level)
                            }
                            Spacer(Modifier.height(6.dp))

                            // Info grid (2 columns)
                            val loc = listOfNotNull(tournament.city, tournament.state).filter { it.isNotBlank() }.joinToString(", ")
                            val dates = listOfNotNull(tournament.startDate, tournament.endDate).filter { it.isNotBlank() }.joinToString(" - ")
                            val eventsNum = tournament.eventsCount ?: tournament.events?.size ?: 0

                            InfoRow {
                                if (dates.isNotBlank()) InfoItem(icon = Icons.Default.CalendarMonth, label = "Dates", value = dates)
                                if (loc.isNotBlank()) InfoItem(icon = Icons.Default.LocationOn, label = "Location", value = loc)
                            }
                            InfoRow {
                                tournament.venueName?.let { InfoItem(label = "Venue", value = it) }
                                tournament.section?.let { InfoItem(label = "Section", value = it) }
                            }
                            InfoRow {
                                tournament.surface?.let { InfoItem(label = "Surface", value = it) }
                                tournament.directorName?.let { InfoItem(label = "Director", value = it) }
                            }
                            InfoRow {
                                InfoItem(icon = Icons.Default.EmojiEvents, label = "Events", value = "$eventsNum")
                                if (tournament.status == "cancelled") {
                                    InfoItem(label = "Status", value = "Cancelled", valueColor = Color(0xFFEF4444))
                                } else {
                                    tournament.registrationStatus?.let {
                                        val color = when {
                                            it.contains("open", ignoreCase = true) -> Color(0xFF22C55E)
                                            it.contains("closed", ignoreCase = true) -> Color(0xFFF97316)
                                            else -> Color.Unspecified
                                        }
                                        InfoItem(label = "Status", value = it, valueColor = color)
                                    }
                                }
                            }
                        }

                        // Events filter - horizontal scrollable chips
                        if (!tournament.events.isNullOrEmpty()) {
                            val labelCounts = tournament.events.groupingBy { ev ->
                                buildEventLabel(ev)
                            }.eachCount()

                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = viewModel.selectedEventId == null,
                                    onClick = { viewModel.selectEvent(null) },
                                    label = { Text("All", fontSize = 12.sp) }
                                )
                                tournament.events.forEach { event ->
                                    val baseLabel = buildEventLabel(event)
                                    val label = if ((labelCounts[baseLabel] ?: 0) > 1) {
                                        "$baseLabel (${event.eventId})"
                                    } else {
                                        baseLabel
                                    }
                                    FilterChip(
                                        selected = viewModel.selectedEventId == event.eventId,
                                        onClick = { viewModel.selectEvent(event.eventId) },
                                        label = { Text(label, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }

                        // Entries grouped
                        if (viewModel.entriesLoading) {
                            LoadingView()
                        } else {
                            val grouped = viewModel.getGroupedDisplayRows()
                            if (grouped.isEmpty()) {
                                EmptyStateView(title = "No entries", description = "No entries found for this tournament")
                            } else {
                                val statusOrder = listOf("Acceptance", "Alternates", "Withdrawn", "Other")
                                val statusColors = mapOf(
                                    "Acceptance" to TennisGreen,
                                    "Alternates" to Color(0xFFF97316),
                                    "Withdrawn" to Color(0xFFEF4444),
                                    "Other" to MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (status in statusOrder) {
                                        val rows = grouped[status] ?: continue
                                        item {
                                            Text(
                                                "$status (${rows.size})",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = statusColors[status] ?: TennisGreen
                                            )
                                        }
                                        items(rows.size) { index ->
                                            DisplayRowCard(row = rows[index], index = index + 1)
                                        }
                                        item { Spacer(Modifier.height(8.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayRowCard(row: DisplayRow, index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$index.",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TennisGreen,
                modifier = Modifier.width(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                if (row.isPair) {
                    Text(
                        "${playerName(row.entries[0])} / ${playerName(row.entries[1])}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    val loc1 = playerLocation(row.entries[0])
                    val loc2 = playerLocation(row.entries[1])
                    val locText = when {
                        loc1.isNotBlank() && loc2.isNotBlank() -> "$loc1 / $loc2"
                        loc1.isNotBlank() -> loc1
                        loc2.isNotBlank() -> loc2
                        else -> ""
                    }
                    if (locText.isNotBlank()) {
                        Text(locText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Text(playerName(row.entries[0]), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    val loc = playerLocation(row.entries[0])
                    if (loc.isNotBlank()) {
                        Text(loc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (row.isPair) {
                Text(
                    "${row.entries[0].rankingPoints ?: "—"} / ${row.entries[1].rankingPoints ?: "—"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                row.entries[0].rankingPoints?.let {
                    Text("$it pts", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun playerName(entry: TournamentEntry): String {
    return if (entry.firstName != null && entry.lastName != null) {
        "${entry.firstName} ${entry.lastName}".trim().ifBlank { entry.playerName ?: "Unknown" }
    } else {
        entry.playerName ?: "${entry.firstName ?: ""} ${entry.lastName ?: ""}".trim().ifBlank { "Unknown" }
    }
}

private fun playerLocation(entry: TournamentEntry): String {
    return listOfNotNull(entry.city, entry.state).filter { it.isNotBlank() }.joinToString(", ")
}

private val GENDER_MAP = mapOf(
    "M" to "Boys", "Male" to "Boys", "male" to "Boys",
    "F" to "Girls", "Female" to "Girls", "female" to "Girls",
    "Coed" to "Coed", "coed" to "Coed",
    "Mixed" to "Mixed", "mixed" to "Mixed",
    "Boys" to "Boys", "Girls" to "Girls"
)

private fun buildEventLabel(event: TournamentEvent): String {
    val gender = GENDER_MAP[event.gender ?: ""] ?: event.gender ?: ""
    val age = event.ageCategory ?: ""
    val type = event.eventType ?: ""
    return listOf(gender, age, type).filter { it.isNotBlank() }.joinToString(" ")
}

@Composable
private fun InfoRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun RowScope.InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface)
        }
    }
}
