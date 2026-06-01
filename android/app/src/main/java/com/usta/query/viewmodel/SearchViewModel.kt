package com.usta.query.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usta.query.data.api.ApiClient
import com.usta.query.data.model.PlayerSummary
import com.usta.query.data.model.UnifiedSearchResponse
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    var query by mutableStateOf("")
    var results by mutableStateOf<UnifiedSearchResponse?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var currentPage by mutableIntStateOf(0)

    fun search() {
        if (query.isBlank()) return
        currentPage = 0
        fetch()
    }

    fun loadPage(page: Int) {
        currentPage = page
        fetch()
    }

    private fun fetch() {
        isLoading = true
        error = null
        viewModelScope.launch {
            try {
                results = ApiClient.api.unifiedSearch(query, currentPage)
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
}
