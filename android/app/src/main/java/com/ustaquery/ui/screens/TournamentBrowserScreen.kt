package com.ustaquery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ustaquery.ui.components.EmptyStateView
import com.ustaquery.ui.components.FilterDropdown
import com.ustaquery.ui.components.LoadingView
import com.ustaquery.ui.components.MultiSelectDropdown
import com.ustaquery.ui.components.PaginationControls
import com.ustaquery.ui.components.SearchBar
import com.ustaquery.ui.components.TournamentCard
import com.ustaquery.viewmodel.TournamentBrowserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TournamentBrowserScreen(
    onBack: () -> Unit,
    onTournamentClick: (Int) -> Unit,
    viewModel: TournamentBrowserViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tournaments") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets.statusBars,
                modifier = Modifier.height(44.dp)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            SearchBar(
                query = viewModel.searchText,
                onQueryChange = { viewModel.searchText = it },
                placeholder = "Search tournaments...",
                onSearch = { viewModel.search() }
            )

            FlowRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val years = listOf(null, 2024, 2025, 2026)
                val yearLabels = listOf("All Years", "2024", "2025", "2026")
                FilterDropdown(
                    label = "Year",
                    options = yearLabels,
                    selectedIndex = years.indexOf(viewModel.selectedYear).takeIf { it >= 0 } ?: 0,
                    onSelected = { idx -> viewModel.selectedYear = years[idx]; viewModel.search() }
                )

                val levelOptions = viewModel.filterOptions?.levels?.map { it to it }
                    ?: listOf("L1", "L2", "L3", "L4", "L5", "L6", "L7").map { it to it }
                MultiSelectDropdown(
                    label = "Level",
                    options = levelOptions,
                    selectedValues = viewModel.selectedLevels,
                    onToggle = { value ->
                        viewModel.selectedLevels = if (viewModel.selectedLevels.contains(value)) viewModel.selectedLevels - value else viewModel.selectedLevels + value
                        viewModel.search()
                    }
                )

                val genderMap = mapOf(
                    "M" to "Boys", "Male" to "Boys", "male" to "Boys",
                    "F" to "Girls", "Female" to "Girls", "female" to "Girls",
                    "Coed" to "Coed", "coed" to "Coed",
                    "Mixed" to "Mixed", "mixed" to "Mixed"
                )
                val genderOptions = viewModel.filterOptions?.genders?.map {
                    (genderMap[it] ?: it) to it
                } ?: listOf("Boys" to "M", "Girls" to "F")
                MultiSelectDropdown(
                    label = "Gender",
                    options = genderOptions,
                    selectedValues = viewModel.selectedGenders,
                    onToggle = { value ->
                        viewModel.selectedGenders = if (viewModel.selectedGenders.contains(value)) viewModel.selectedGenders - value else viewModel.selectedGenders + value
                        viewModel.search()
                    }
                )

                val ageOptions = TournamentBrowserViewModel.AGE_FILTER_OPTIONS.map { it to it }
                MultiSelectDropdown(
                    label = "Age",
                    options = ageOptions,
                    selectedValues = viewModel.selectedAgeFilters,
                    onToggle = { value ->
                        viewModel.toggleAgeFilter(value)
                        viewModel.search()
                    }
                )

                val sectionOptions = viewModel.filterOptions?.sections?.map { it to it }
                    ?: listOf("Northeast", "Southern", "Midwest", "Texas", "Northern California", "Southern California", "Florida", "Eastern", "Middle States", "New England", "Pacific Northwest", "Southwest", "Missouri Valley", "Intermountain", "Hawaii", "Caribbean").map { it to it }
                MultiSelectDropdown(
                    label = "Section",
                    options = sectionOptions,
                    selectedValues = viewModel.selectedSections,
                    onToggle = { value ->
                        viewModel.selectedSections = if (viewModel.selectedSections.contains(value)) viewModel.selectedSections - value else viewModel.selectedSections + value
                        viewModel.search()
                    }
                )

                val eventTypeOptions = viewModel.filterOptions?.eventTypes?.map { it to it }
                    ?: listOf("Singles", "Doubles", "Compass Draw", "Team").map { it to it }
                MultiSelectDropdown(
                    label = "Event",
                    options = eventTypeOptions,
                    selectedValues = viewModel.selectedEventTypes,
                    onToggle = { value ->
                        viewModel.selectedEventTypes = if (viewModel.selectedEventTypes.contains(value)) viewModel.selectedEventTypes - value else viewModel.selectedEventTypes + value
                        viewModel.search()
                    }
                )

                TextButton(
                    onClick = { viewModel.clearFilters(); viewModel.selectedYear = null; viewModel.search() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Clear", fontSize = 11.sp)
                }
            }

            when {
                viewModel.isLoading -> LoadingView()
                viewModel.error != null -> EmptyStateView(title = "Error", description = viewModel.error)
                viewModel.results.isEmpty() -> EmptyStateView(title = "No tournaments", description = "Try adjusting filters")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(viewModel.results) { tournament ->
                            TournamentCard(tournament = tournament, onClick = { onTournamentClick(tournament.id) })
                        }
                        if (viewModel.totalPages > 1) {
                            item {
                                PaginationControls(currentPage = viewModel.currentPage, totalPages = viewModel.totalPages, onPageChange = { viewModel.loadPage(it) })
                            }
                        }
                    }
                }
            }
        }
    }
}
