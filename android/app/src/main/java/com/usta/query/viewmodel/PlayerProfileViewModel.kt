package com.usta.query.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usta.query.data.api.ApiClient
import com.usta.query.data.model.Match
import com.usta.query.data.model.PagedResponse
import com.usta.query.data.model.PlayerDetail
import com.usta.query.data.model.PlayerStats
import com.usta.query.data.model.PlayerTournamentEntry
import com.usta.query.data.model.Ranking
import com.usta.query.data.model.Registration
import kotlinx.coroutines.launch

class PlayerProfileViewModel(private val uaid: String) : ViewModel() {
    var player by mutableStateOf<PlayerDetail?>(null)
    var stats by mutableStateOf<PlayerStats?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    var tournamentEntries by mutableStateOf<List<PlayerTournamentEntry>>(emptyList())
    var tournamentsLoading by mutableStateOf(false)

    var registrations by mutableStateOf<PagedResponse<Registration>?>(null)
    var registrationsPage by mutableIntStateOf(0)
    var registrationsLoading by mutableStateOf(false)

    var matches by mutableStateOf<PagedResponse<Match>?>(null)
    var matchesPage by mutableIntStateOf(0)
    var matchesLoading by mutableStateOf(false)

    var rankings by mutableStateOf<List<Ranking>>(emptyList())
    var rankingsLoading by mutableStateOf(false)
    var selectedAgeRestriction by mutableStateOf("All")
    var selectedListType by mutableStateOf("")

    fun loadProfile() {
        isLoading = true
        viewModelScope.launch {
            try {
                player = ApiClient.api.getPlayer(uaid)
                stats = ApiClient.api.getPlayerStats(uaid)
                // Auto-load tournaments and registrations
                try {
                    tournamentEntries = ApiClient.api.getPlayerTournamentEntries(uaid)
                } catch (_: Exception) {
                    tournamentEntries = emptyList()
                }
                try {
                    registrations = ApiClient.api.getPlayerRegistrations(uaid)
                } catch (_: Exception) {
                    registrations = null
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun loadTournaments() {
        tournamentsLoading = true
        viewModelScope.launch {
            try {
                tournamentEntries = ApiClient.api.getPlayerTournamentEntries(uaid)
            } catch (_: Exception) {
                tournamentEntries = emptyList()
            } finally {
                tournamentsLoading = false
            }
        }
    }

    fun loadRegistrations(page: Int = 0) {
        registrationsLoading = true
        registrationsPage = page
        viewModelScope.launch {
            try {
                registrations = ApiClient.api.getPlayerRegistrations(uaid, page = page)
            } catch (_: Exception) {
                registrations = null
            } finally {
                registrationsLoading = false
            }
        }
    }

    fun loadMatches(page: Int = 0) {
        matchesLoading = true
        matchesPage = page
        viewModelScope.launch {
            try {
                matches = ApiClient.api.getPlayerMatches(uaid, page = page)
            } catch (_: Exception) {
                matches = null
            } finally {
                matchesLoading = false
            }
        }
    }

    fun loadRankings() {
        rankingsLoading = true
        viewModelScope.launch {
            try {
                val age = selectedAgeRestriction.takeIf { it != "All" }
                val lt = selectedListType.takeIf { it.isNotBlank() }
                rankings = ApiClient.api.getPlayerRankings(uaid, listType = lt, ageRestriction = age)
            } catch (_: Exception) {
                rankings = emptyList()
            } finally {
                rankingsLoading = false
            }
        }
    }
}
