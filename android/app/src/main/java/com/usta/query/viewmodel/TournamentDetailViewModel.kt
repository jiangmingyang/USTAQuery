package com.usta.query.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usta.query.data.api.ApiClient
import com.usta.query.data.model.Tournament
import com.usta.query.data.model.TournamentEntry
import com.usta.query.data.model.TournamentEvent
import kotlinx.coroutines.launch

data class DisplayRow(
    val entries: List<TournamentEntry>,
    val eventId: String,
    val entryPosition: Int?,
    val entryStatus: String?
) {
    val totalPoints: Int get() = entries.sumOf { it.rankingPoints ?: 0 }
    val isPair: Boolean get() = entries.size == 2
}

class TournamentDetailViewModel(private val tournamentId: Int) : ViewModel() {
    var tournament by mutableStateOf<Tournament?>(null)
    var entries by mutableStateOf<List<TournamentEntry>>(emptyList())
    var selectedEventId by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var entriesLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun loadTournament() {
        isLoading = true
        viewModelScope.launch {
            try {
                tournament = ApiClient.api.getTournament(tournamentId)
                loadEntries()
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun loadEntries() {
        entriesLoading = true
        viewModelScope.launch {
            try {
                entries = ApiClient.api.getTournamentEntries(tournamentId, selectedEventId)
            } catch (_: Exception) {
                entries = emptyList()
            } finally {
                entriesLoading = false
            }
        }
    }

    fun selectEvent(eventId: String?) {
        selectedEventId = eventId
        loadEntries()
    }

    fun getDisplayRows(): List<DisplayRow> {
        val rows = mutableListOf<DisplayRow>()
        val teamEntries = mutableListOf<TournamentEntry>()
        val individualsByKey = mutableMapOf<String, MutableList<TournamentEntry>>()
        val singlesOrOther = mutableListOf<TournamentEntry>()

        for (e in entries) {
            val evType = (e.eventType ?: "").uppercase()
            val isDoubles = evType.contains("DOUBLES")
            val isTeamEvent = evType.contains("TEAM")
            val hasSlash = (e.playerName ?: "").contains("/")
            val hasFirstName = (e.firstName ?: "").isNotBlank()

            when {
                isTeamEvent && !hasFirstName -> {
                    // TEAM event synthetic entry: pair summary or team name — skip both
                }
                isDoubles && hasSlash && !hasFirstName -> {
                    // Doubles pair summary (e.g. "Racic/Yamamoto")
                    teamEntries.add(e)
                }
                isDoubles && hasFirstName -> {
                    val ln = (e.lastName ?: "").trim().lowercase()
                    val key = "${e.eventId}::${e.drawId ?: ""}::$ln"
                    individualsByKey.getOrPut(key) { mutableListOf() }.add(e)
                }
                else -> singlesOrOther.add(e)
            }
        }

        val pairedIds = mutableSetOf<String>()

        for (team in teamEntries) {
            val names = (team.playerName ?: "").split("/").map { it.trim().lowercase() }
            if (names.size != 2) continue

            val drawId = team.drawId ?: ""
            val pair = mutableListOf<TournamentEntry>()

            for (ln in names) {
                val key = "${team.eventId}::${drawId}::$ln"
                val candidates = individualsByKey[key]
                if (candidates != null) {
                    val unused = candidates.filter { pairedIds.isEmpty() || !pairedIds.contains(it.participantId ?: "") }
                    val pick = unused.find { it.entryPosition == team.entryPosition } ?: unused.firstOrNull()
                    if (pick != null) {
                        pair.add(pick)
                        pick.participantId?.let { pairedIds.add(it) }
                    }
                }
            }

            if (pair.size == 2) {
                rows.add(DisplayRow(pair, team.eventId, team.entryPosition, team.entryStatus))
            } else {
                rows.add(DisplayRow(listOf(team), team.eventId, team.entryPosition, team.entryStatus))
            }
        }

        for (list in individualsByKey.values) {
            for (e in list) {
                if (!pairedIds.contains(e.participantId ?: "")) {
                    rows.add(DisplayRow(listOf(e), e.eventId, e.entryPosition, e.entryStatus))
                }
            }
        }

        for (e in singlesOrOther) {
            rows.add(DisplayRow(listOf(e), e.eventId, e.entryPosition, e.entryStatus))
        }

        rows.sortWith(compareBy<DisplayRow> { it.entryPosition == null }
            .thenBy { it.entryPosition ?: Int.MAX_VALUE }
            .thenByDescending { it.totalPoints })

        return rows
    }

    fun getGroupedDisplayRows(): Map<String, List<DisplayRow>> {
        return getDisplayRows().groupBy { classifyStatus(it.entryStatus) }
    }

    private fun classifyStatus(status: String?): String {
        val s = status?.uppercase() ?: return "Other"
        return when {
            s.contains("DIRECT") || s == "REGISTERED" -> "Acceptance"
            s.contains("ALTERNATE") || s.contains("UNGROUPED") -> "Alternates"
            s.contains("WITHDRAWN") -> "Withdrawn"
            else -> "Other"
        }
    }
}
