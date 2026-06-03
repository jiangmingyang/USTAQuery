package com.ustaquery.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
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
                },
                windowInsets = WindowInsets.statusBars,
                modifier = Modifier.height(44.dp)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Filter bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterDropdown(
                    label = "List",
                    options = RankingsViewModel.listTypes.map { it.second },
                    selectedIndex = RankingsViewModel.listTypes.indexOfFirst { it.first == viewModel.listKey }.takeIf { it >= 0 } ?: 0,
                    onSelected = { idx -> viewModel.updateFilter(list = RankingsViewModel.listTypes[idx].first) },
                    modifier = Modifier.width(180.dp)
                )
                FilterDropdown(
                    label = "Gender",
                    options = RankingsViewModel.genders.map { it.second },
                    selectedIndex = RankingsViewModel.genders.indexOfFirst { it.first == viewModel.gender }.takeIf { it >= 0 } ?: 0,
                    onSelected = { idx -> viewModel.updateFilter(g = RankingsViewModel.genders[idx].first) },
                    modifier = Modifier.width(100.dp)
                )
                FilterDropdown(
                    label = "Age",
                    options = RankingsViewModel.ageRestrictions.map { RankingsViewModel.ageGroupLabels[it] ?: it },
                    selectedIndex = RankingsViewModel.ageRestrictions.indexOf(viewModel.ageRestriction).takeIf { it >= 0 } ?: 0,
                    onSelected = { idx -> viewModel.updateFilter(age = RankingsViewModel.ageRestrictions[idx]) },
                    modifier = Modifier.width(120.dp)
                )
                FilterDropdown(
                    label = "Version",
                    options = viewModel.versions.ifEmpty { listOf("Latest") },
                    selectedIndex = viewModel.versions.indexOf(viewModel.publishDate).takeIf { it >= 0 } ?: 0,
                    onSelected = { idx ->
                        val selected = viewModel.versions.getOrElse(idx) { "" }
                        viewModel.updateVersion(selected)
                    },
                    modifier = Modifier.width(140.dp)
                )
                FilterDropdown(
                    label = "Section",
                    options = listOf("All Sections") + viewModel.sections,
                    selectedIndex = if (viewModel.sectionFilter.isEmpty()) 0
                        else (viewModel.sections.indexOf(viewModel.sectionFilter).takeIf { it >= 0 } ?: 0) + 1,
                    onSelected = { idx ->
                        val selected = if (idx == 0) "" else viewModel.sections.getOrElse(idx - 1) { "" }
                        viewModel.updateFilter(section = selected)
                    },
                    modifier = Modifier.width(180.dp)
                )
            }

            when {
                viewModel.isLoading -> LoadingView()
                viewModel.error != null -> EmptyStateView(title = "Error", description = viewModel.error)
                viewModel.data.isNullOrEmpty() -> EmptyStateView(title = "No rankings", description = "No data for selected filters")
                else -> {
                    // Fixed table header (outside LazyColumn)
                    RankingsHeader()

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(viewModel.data!!) { ranking ->
                            RankingRow(ranking = ranking, onClick = { onPlayerClick(ranking.playerUaid) })
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                        if (viewModel.totalPages > 1) {
                            item {
                                PaginationControls(
                                    currentPage = viewModel.page,
                                    totalPages = viewModel.totalPages,
                                    onPageChange = { viewModel.goToPage(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("", modifier = Modifier.width(32.dp), fontSize = 11.sp)
        Text("Player", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Dist", modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Sect", modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Natl", modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Pts", modifier = Modifier.width(44.dp), textAlign = TextAlign.End, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("", modifier = Modifier.width(20.dp), fontSize = 11.sp)
    }
}

@Composable
private fun RankingRow(ranking: Ranking, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Green circle with national rank
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(TennisGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                ranking.nationalRank?.toString() ?: "\u2014",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TennisGreen
            )
        }
        Spacer(Modifier.width(4.dp))

        // Player name + section/district
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${ranking.playerFirstName} ${ranking.playerLastName}",
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 1
            )
            val loc = listOfNotNull(ranking.section, ranking.district).filter { it.isNotBlank() }.joinToString(" \u00b7 ")
            if (loc.isNotBlank()) {
                Text(loc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }

        // District rank
        Text(
            ranking.districtRank?.toString() ?: "\u2014",
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Section rank
        Text(
            ranking.sectionRank?.toString() ?: "\u2014",
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )

        // National rank
        val isTopThree = ranking.nationalRank != null && ranking.nationalRank <= 3
        Text(
            ranking.nationalRank?.toString() ?: "\u2014",
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (isTopThree) TennisGreen else MaterialTheme.colorScheme.onSurface
        )

        // Points (number only, no "pts" suffix)
        Text(
            ranking.points?.toString() ?: "\u2014",
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )


        // Trend icon
        Text(
            when (ranking.trendDirection) {
                "down" -> "\u25B2"
                "up" -> "\u25BC"
                "no change" -> "\u2014"
                else -> ""
            },
            modifier = Modifier.width(20.dp),
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            color = when (ranking.trendDirection) {
                "down" -> MaterialTheme.colorScheme.onSurfaceVariant
                "up" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
