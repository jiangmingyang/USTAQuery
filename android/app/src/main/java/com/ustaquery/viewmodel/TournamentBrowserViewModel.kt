package com.ustaquery.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ustaquery.data.api.ApiClient
import com.ustaquery.data.model.Tournament
import com.ustaquery.data.model.TournamentFilterOptions
import kotlinx.coroutines.launch

class TournamentBrowserViewModel : ViewModel() {
    var searchText by mutableStateOf("")
    var selectedYear by mutableStateOf<Int?>(null)
    var selectedSections by mutableStateOf(setOf<String>())
    var selectedLevels by mutableStateOf(setOf<String>())
    var selectedGenders by mutableStateOf(setOf<String>())
    var selectedAgeFilters by mutableStateOf(setOf<String>())  // UI-level: may contain "Other"
    var selectedEventTypes by mutableStateOf(setOf<String>())

    var filterOptions by mutableStateOf<TournamentFilterOptions?>(null)

    var results by mutableStateOf<List<Tournament>>(emptyList())
    var currentPage by mutableIntStateOf(0)
    var totalPages by mutableIntStateOf(0)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    companion object {
        val MAIN_AGE_GROUPS = listOf("U8", "U10", "U12", "U14", "U16", "U18")
        val AGE_FILTER_OPTIONS = MAIN_AGE_GROUPS + "Other"
    }

    init {
        loadFilters()
        search()
    }

    private fun loadFilters() {
        viewModelScope.launch {
            try {
                filterOptions = ApiClient.api.getTournamentFilters()
            } catch (_: Exception) {
                filterOptions = null
            }
        }
    }

    fun toggleAgeFilter(value: String) {
        selectedAgeFilters = if (selectedAgeFilters.contains(value))
            selectedAgeFilters - value
        else
            selectedAgeFilters + value
    }

    /** Resolve "Other" to actual non-main age category values from the API. */
    private fun resolveAgeCategories(): String? {
        if (selectedAgeFilters.isEmpty()) return null
        val resolved = selectedAgeFilters.toMutableSet()
        if (resolved.remove("Other")) {
            filterOptions?.ageCategories?.forEach { age ->
                if (age !in MAIN_AGE_GROUPS) resolved.add(age)
            }
        }
        return resolved.joinToString(",").takeIf { it.isNotBlank() }
    }

    fun search() {
        currentPage = 0
        fetch()
    }

    fun loadPage(page: Int) {
        currentPage = page
        fetch()
    }

    private fun fetch() {
        isLoading = true
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.searchTournaments(
                    q = searchText.takeIf { it.isNotBlank() },
                    section = selectedSections.joinToString(",").takeIf { it.isNotBlank() },
                    level = selectedLevels.joinToString(",").takeIf { it.isNotBlank() },
                    year = selectedYear,
                    gender = selectedGenders.joinToString(",").takeIf { it.isNotBlank() },
                    ageCategory = resolveAgeCategories(),
                    eventType = selectedEventTypes.joinToString(",").takeIf { it.isNotBlank() },
                    page = currentPage
                )
                results = resp.content
                totalPages = resp.totalPages
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun clearFilters() {
        selectedSections = emptySet()
        selectedLevels = emptySet()
        selectedGenders = emptySet()
        selectedAgeFilters = emptySet()
        selectedEventTypes = emptySet()
    }
}
