package com.ustaquery.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ustaquery.data.model.Ranking
import com.ustaquery.ui.components.EmptyStateView
import com.ustaquery.ui.components.FilterDropdown
import com.ustaquery.ui.components.LoadingView
import com.ustaquery.ui.components.PaginationControls
import com.ustaquery.ui.theme.TennisGreen
import com.ustaquery.viewmodel.RankingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingsScreen(
    initialGender: String = "M",
    initialAge: String = "Y12",
    onBack: () -> Unit,
    onPlayerClick: (String) -> Unit,
    viewModel: RankingsViewModel = viewModel()
) {
    LaunchedEffect(initialGender, initialAge) {
        viewModel.updateFilter(g = initialGender, age = initialAge)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rankings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterDropdown(
                    label = "List",
                    options = RankingsViewModel.listTypes.map { it.second },
                    selectedIndex = RankingsViewModel.listTypes.indexOfFirst { it.first == viewModel.listKey }.takeIf { it >= 0 } ?: 0,
                    onSelected = { idx -> viewModel.updateFilter(list = RankingsViewModel.listTypes[idx].first) },
                    modifier = Modifier.weight(1.5f)
                )

                FilterDropdown(
                    label = "Gender",
                    options = RankingsViewModel.genders.map { it.second },
                    selectedIndex = RankingsViewModel.genders.indexOfFirst { it.first == viewModel.gender }.takeIf { it >= 0 } ?: 0,
                    onSelected = { idx -> viewModel.updateFilter(g = RankingsViewModel.genders[idx].first) },
                    modifier = Modifier.weight(1.2f)
                )

                FilterDropdown(
                    label = "Age",
                    options = RankingsViewModel.ageRestrictions.map { RankingsViewModel.ageGroupLabels[it] ?: it },
                    selectedIndex = RankingsViewModel.ageRestrictions.indexOf(viewModel.ageRestriction).takeIf { it >= 0 } ?: 0,
                    onSelected = { idx -> viewModel.updateFilter(age = RankingsViewModel.ageRestrictions[idx]) },
                    modifier = Modifier.weight(1f)
                )

                FilterDropdown(
                    label = "Version",
                    options = viewModel.versions.ifEmpty { listOf("Latest") },
                    selectedIndex = viewModel.versions.indexOf(viewModel.publishDate).takeIf { it >= 0 } ?: 0,
                    onSelected = { idx ->
                        val selected = viewModel.versions.getOrElse(idx) { "" }
                        viewModel.updateVersion(selected)
                    },
                    modifier = Modifier.weight(1.3f)
                )
            }

            when {
                viewModel.isLoading -> LoadingView()
                viewModel.error != null -> EmptyStateView(title = "Error", description = viewModel.error)
                viewModel.data.isNullOrEmpty() -> EmptyStateView(title = "No rankings", description = "No data for selected filters")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.data!!) { ranking ->
                            RankingRow(ranking = ranking, onClick = { onPlayerClick(ranking.playerUaid) })
                        }
                        if (viewModel.totalPages > 1) {
                            item {
                                PaginationControls(currentPage = viewModel.page, totalPages = viewModel.totalPages, onPageChange = { viewModel.goToPage(it) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingRow(ranking: Ranking, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(TennisGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${ranking.nationalRank ?: "—"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TennisGreen
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${ranking.playerFirstName} ${ranking.playerLastName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                val loc = listOfNotNull(ranking.section, ranking.district).filter { it.isNotBlank() }.joinToString(" · ")
                if (loc.isNotBlank()) {
                    Text(loc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("${ranking.points ?: "—"} pts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
