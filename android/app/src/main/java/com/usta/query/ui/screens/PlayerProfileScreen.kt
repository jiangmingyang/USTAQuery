package com.usta.query.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.usta.query.data.model.PlayerTournamentEntry
import com.usta.query.data.model.Ranking
import com.usta.query.ui.components.EmptyStateView
import com.usta.query.ui.components.LevelBadge
import com.usta.query.ui.components.LoadingView
import com.usta.query.ui.components.PaginationControls
import com.usta.query.ui.components.StatusBadge
import com.usta.query.ui.components.compactPlayerEventLabel
import com.usta.query.ui.theme.TennisGreen
import com.usta.query.viewmodel.PlayerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    uaid: String,
    onBack: () -> Unit,
    onTournamentClick: (Int) -> Unit,
    viewModel: PlayerProfileViewModel = viewModel(key = uaid) { PlayerProfileViewModel(uaid) }
) {
    LaunchedEffect(uaid) {
        viewModel.loadProfile()
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Info", "Tournaments", "Registrations", "Matches", "Rankings")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.player?.let { "${it.firstName} ${it.lastName}" } ?: "Player") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (viewModel.isLoading) {
                LoadingView()
            } else if (viewModel.error != null) {
                EmptyStateView(title = "Error", description = viewModel.error)
            } else {
                viewModel.player?.let { player ->
                    // Header
                    PlayerHeader(player = player, stats = viewModel.stats)

                    SecondaryTabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = {
                                    selectedTab = index
                                    when (index) {
                                        1 -> viewModel.loadTournaments()
                                        2 -> viewModel.loadRegistrations()
                                        3 -> viewModel.loadMatches()
                                        4 -> viewModel.loadRankings()
                                    }
                                },
                                text = { Text(title, fontSize = 12.sp) }
                            )
                        }
                    }

                    when (selectedTab) {
                        0 -> PlayerInfoTab(player = player)
                        1 -> TournamentsTab(
                            entries = viewModel.tournamentEntries.filter { it.registrationStatus == "Completed" },
                            isLoading = viewModel.tournamentsLoading,
                            onTournamentClick = onTournamentClick
                        )
                        2 -> RegistrationsTab(
                            entries = viewModel.tournamentEntries.filter { it.registrationStatus != "Completed" },
                            isLoading = viewModel.registrationsLoading || viewModel.tournamentsLoading,
                            onTournamentClick = onTournamentClick
                        )
                        3 -> MatchesTab(
                            matches = viewModel.matches,
                            isLoading = viewModel.matchesLoading,
                            currentPage = viewModel.matchesPage,
                            uaid = uaid,
                            onPageChange = { viewModel.loadMatches(it) }
                        )
                        4 -> RankingsTab(
                            rankings = viewModel.rankings,
                            isLoading = viewModel.rankingsLoading,
                            selectedAge = viewModel.selectedAgeRestriction,
                            onAgeChange = {
                                viewModel.selectedAgeRestriction = it
                                viewModel.loadRankings()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerHeader(player: com.usta.query.data.model.PlayerDetail, stats: com.usta.query.data.model.PlayerStats?) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(TennisGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${player.firstName.first()}${player.lastName.first()}",
                color = TennisGreen, fontWeight = FontWeight.Bold, fontSize = 28.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("${player.firstName} ${player.lastName}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        val loc = listOfNotNull(player.city, player.state).filter { it.isNotBlank() }.joinToString(", ")
        if (loc.isNotBlank()) {
            Text(loc, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatPill("WTN", player.wtnSingles?.let { String.format("%.2f", it) } ?: "—")
            StatPill("NTRP", player.ratingNtrp ?: "—")
            stats?.let { StatPill("Win%", "${String.format("%.0f", it.winPercentage)}%") }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun PlayerInfoTab(player: com.usta.query.data.model.PlayerDetail) {
    val infoItems = listOfNotNull(
        "Gender" to player.gender,
        "Age Category" to player.ageCategory,
        "Section" to player.section,
        "District" to player.district,
        "Nationality" to player.nationality,
        "Membership" to player.membershipType,
        "UTR Singles" to player.utrSingles?.toString(),
        "UTR Doubles" to player.utrDoubles?.toString()
    )
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        infoItems.forEach { (label, value) ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value ?: "", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TournamentsTab(
    entries: List<PlayerTournamentEntry>,
    isLoading: Boolean,
    onTournamentClick: (Int) -> Unit
) {
    if (isLoading) { LoadingView(); return }
    if (entries.isEmpty()) { EmptyStateView(title = "No tournaments", description = "No completed tournament entries found"); return }

    val grouped = entries.groupBy { it.tournamentInternalId }
    val sortedKeys = grouped.keys.sortedByDescending { grouped[it]?.firstOrNull()?.startDate ?: "" }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sortedKeys.forEach { key ->
            val items = grouped[key] ?: return@forEach
            val first = items.first()
            val status = classifyPlayerEntryStatus(items)
            val isMainDraw = status == PlayerEntryStatus.ACCEPTED
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (!isMainDraw) Modifier.border(1.dp, Color.Gray.copy(alpha = 0.35f), RoundedCornerShape(12.dp)) else Modifier)
                    .clickable { onTournamentClick(first.tournamentInternalId) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isMainDraw) 0.5f else 0.25f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            first.tournamentName,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            color = if (isMainDraw) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        EntryStatusBadge(status)
                        Spacer(Modifier.width(6.dp))
                        LevelBadge(level = first.tournamentLevel)
                    }
                    first.startDate?.let { Text(it.take(10), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items.forEach { entry ->
                            Text(compactPlayerEventLabel(entry.eventGender, entry.eventAgeCategory, entry.eventType).ifBlank { entry.eventType ?: "Event" }, fontSize = 12.sp, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RegistrationsTab(
    entries: List<PlayerTournamentEntry>,
    isLoading: Boolean,
    onTournamentClick: (Int) -> Unit
) {
    if (isLoading) { LoadingView(); return }
    if (entries.isEmpty()) { EmptyStateView(title = "No registrations", description = "No active registrations found"); return }

    val grouped = entries.groupBy { it.tournamentInternalId }
    val sortedKeys = grouped.keys.sortedByDescending { grouped[it]?.firstOrNull()?.startDate ?: "" }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sortedKeys.forEach { key ->
            val items = grouped[key] ?: return@forEach
            val first = items.first()
            val status = classifyPlayerEntryStatus(items)
            val isMainDraw = status == PlayerEntryStatus.ACCEPTED
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (!isMainDraw) Modifier.border(1.dp, Color.Gray.copy(alpha = 0.35f), RoundedCornerShape(12.dp)) else Modifier)
                    .clickable { onTournamentClick(first.tournamentInternalId) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isMainDraw) 0.5f else 0.25f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            first.tournamentName,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            color = if (isMainDraw) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        EntryStatusBadge(status)
                        Spacer(Modifier.width(6.dp))
                        LevelBadge(level = first.tournamentLevel)
                    }
                    first.startDate?.let { Text(it.take(10), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    first.registrationStatus?.let { StatusBadge(it, Modifier.padding(top = 6.dp)) }
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items.forEach { entry ->
                            Text(compactPlayerEventLabel(entry.eventGender, entry.eventAgeCategory, entry.eventType).ifBlank { entry.eventType ?: "Event" }, fontSize = 12.sp, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchesTab(
    matches: com.usta.query.data.model.PagedResponse<com.usta.query.data.model.Match>?,
    isLoading: Boolean,
    currentPage: Int,
    uaid: String,
    onPageChange: (Int) -> Unit
) {
    if (isLoading) { LoadingView(); return }
    if (matches == null || matches.content.isEmpty()) { EmptyStateView(title = "No matches", description = "No match records found"); return }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(matches.content) { match ->
                MatchCard(match = match, uaid = uaid)
            }
        }
        if (matches.totalPages > 1) {
            PaginationControls(currentPage = currentPage, totalPages = matches.totalPages, onPageChange = onPageChange)
        }
    }
}

@Composable
private fun MatchCard(match: com.usta.query.data.model.Match, uaid: String) {
    val isWin = when (match.winnerSide) {
        "SIDE1" -> match.player1.uaid == uaid
        "SIDE2" -> match.player2?.uaid == uaid
        else -> false
    }
    val opponentName = if (match.player1.uaid == uaid) {
        match.opponent1Name?.takeIf { it.isNotBlank() } ?: match.player2?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown"
    } else {
        "${match.player1.firstName} ${match.player1.lastName}"
    }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(match.tournamentName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                match.matchDate?.let { Text(it.take(10), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isWin) com.usta.query.ui.theme.WinGreen else com.usta.query.ui.theme.LossRed))
                Spacer(Modifier.width(6.dp))
                Text("vs $opponentName", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("${match.round} - ${match.divisionName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RankingsTab(
    rankings: List<Ranking>,
    isLoading: Boolean,
    selectedAge: String,
    onAgeChange: (String) -> Unit
) {
    if (isLoading) { LoadingView(); return }
    if (rankings.isEmpty()) { EmptyStateView(title = "No rankings", description = "No ranking data found"); return }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            listOf("All", "Y12", "Y14", "Y16", "Y18").forEach { age ->
                FilterChip(
                    selected = selectedAge == age,
                    onClick = { onAgeChange(age) },
                    label = { Text(age) }
                )
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rankings) { r ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r.displayLabel ?: r.catalogId, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Nat: ${r.nationalRank ?: "—"} · Sect: ${r.sectionRank ?: "—"} · Dist: ${r.districtRank ?: "—"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${r.points ?: "—"} pts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// Entry status classification for player profiles

private enum class PlayerEntryStatus { ACCEPTED, ALTERNATE, WITHDRAWN }

private fun classifyPlayerEntryStatus(entries: List<PlayerTournamentEntry>): PlayerEntryStatus {
    for (e in entries) {
        val s = (e.entryStatus ?: "").uppercase()
        if (s.contains("DIRECT") || s == "REGISTERED") return PlayerEntryStatus.ACCEPTED
    }
    for (e in entries) {
        val s = (e.entryStatus ?: "").uppercase()
        if (s.contains("ALTERNATE") || s.contains("UNGROUPED")) return PlayerEntryStatus.ALTERNATE
    }
    for (e in entries) {
        val s = (e.entryStatus ?: "").uppercase()
        if (s.contains("WITHDRAWN")) return PlayerEntryStatus.WITHDRAWN
    }
    return PlayerEntryStatus.ACCEPTED
}

@Composable
private fun EntryStatusBadge(status: PlayerEntryStatus) {
    val (label, bgColor, fgColor) = when (status) {
        PlayerEntryStatus.ACCEPTED -> Triple("Accepted", TennisGreen.copy(alpha = 0.15f), TennisGreen)
        PlayerEntryStatus.ALTERNATE -> Triple("Alternate", Color(0xFFF97316).copy(alpha = 0.15f), Color(0xFFF97316))
        PlayerEntryStatus.WITHDRAWN -> Triple("Withdrawn", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444))
    }
    Text(
        label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = fgColor,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(bgColor).padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
