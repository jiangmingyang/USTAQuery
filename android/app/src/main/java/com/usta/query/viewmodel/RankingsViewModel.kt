package com.usta.query.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usta.query.data.api.ApiClient
import com.usta.query.data.model.Ranking
import kotlinx.coroutines.launch

class RankingsViewModel : ViewModel() {
    var listKey by mutableStateOf("STANDING")
    var gender by mutableStateOf("M")
    var ageRestriction by mutableStateOf("Y12")
    var page by mutableIntStateOf(0)
    var publishDate by mutableStateOf("")

    var data by mutableStateOf<List<Ranking>?>(null)
    var totalPages by mutableIntStateOf(0)
    var versions by mutableStateOf<List<String>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    val catalogId: String
        get() = buildCatalogId(listKey, gender, ageRestriction)

    fun loadIfNeeded() {
        if (data == null) fetchAll()
    }

    fun reload() {
        page = 0
        fetchAll()
    }

    fun updateFilter(list: String? = null, g: String? = null, age: String? = null) {
        list?.let { listKey = it }
        g?.let { gender = it }
        age?.let { ageRestriction = it }
        page = 0
        publishDate = ""
        fetchAll()
    }

    fun updateVersion(date: String) {
        publishDate = date
        page = 0
        fetchLeaderboard()
    }

    fun goToPage(p: Int) {
        page = p
        fetchLeaderboard()
    }

    private fun fetchAll() {
        viewModelScope.launch {
            try {
                versions = ApiClient.api.getRankingVersions(catalogId)
                if (versions.isNotEmpty() && publishDate.isEmpty()) {
                    publishDate = versions[0]
                }
            } catch (_: Exception) {
                versions = emptyList()
            }
            fetchLeaderboard()
        }
    }

    private fun fetchLeaderboard() {
        isLoading = true
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.getLeaderboard(
                    catalogId = catalogId,
                    page = page,
                    size = 50,
                    publishDate = publishDate.takeIf { it.isNotEmpty() }
                )
                data = resp.content
                totalPages = resp.totalPages
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }

    companion object {
        fun buildCatalogId(listKey: String, gender: String, age: String): String {
            val pattern = when (listKey) {
                "STANDING" -> "JUNIOR_NULL_{G}_STANDING_{A}_UNDER_NULL_NULL_NULL"
                "SEEDING_SINGLES" -> "JUNIOR_NULL_{G}_SEEDING_{A}_UNDER_SINGLES_NULL_NULL"
                "SEEDING_DOUBLES" -> "JUNIOR_NULL_{G}_SEEDING_{A}_UNDER_DOUBLES_INDIVIDUAL_NULL"
                "BONUS_POINTS" -> "JUNIOR_NULL_{G}_BONUS_POINTS_{A}_UNDER_NULL_NULL_NULL"
                "QUOTA" -> "JUNIOR_NULL_{G}_QUOTA_{A}_UNDER_NULL_NULL_S05"
                "YEAR_END_COMBINED" -> "JUNIOR_NULL_{G}_YEAR_END_{A}_UNDER_NULL_NULL_NULL"
                "YEAR_END_DOUBLES" -> "JUNIOR_NULL_{G}_YEAR_END_{A}_UNDER_DOUBLES_INDIVIDUAL_NULL"
                else -> "JUNIOR_NULL_{G}_STANDING_{A}_UNDER_NULL_NULL_NULL"
            }
            return pattern.replace("{G}", gender).replace("{A}", age)
        }

        val listTypes = listOf(
            Triple("STANDING", "Combined National Standing List", ""),
            Triple("SEEDING_SINGLES", "Singles Seeding List", ""),
            Triple("SEEDING_DOUBLES", "Doubles Seeding List", ""),
            Triple("BONUS_POINTS", "Bonus Points List", ""),
            Triple("QUOTA", "Quota List", ""),
            Triple("YEAR_END_COMBINED", "Final Year End Combined Rank List", ""),
            Triple("YEAR_END_DOUBLES", "Final Year End Doubles Rank List", "")
        )

        val genders = listOf("M" to "Boys", "F" to "Girls")
        val ageRestrictions = listOf("Y12", "Y14", "Y16", "Y18")
        val ageGroupLabels = mapOf("Y12" to "12 & Under", "Y14" to "14 & Under", "Y16" to "16 & Under", "Y18" to "18 & Under")
    }
}
