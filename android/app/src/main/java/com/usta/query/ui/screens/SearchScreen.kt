package com.usta.query.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.usta.query.ui.components.EmptyStateView
import com.usta.query.ui.components.LoadingView
import com.usta.query.ui.components.PaginationControls
import com.usta.query.ui.components.PlayerCard
import com.usta.query.ui.components.SearchBar
import com.usta.query.ui.components.TournamentCard
import com.usta.query.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    initialQuery: String = "",
    onPlayerClick: (String) -> Unit,
    onTournamentClick: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank() && viewModel.query != initialQuery) {
            viewModel.query = initialQuery
            viewModel.search()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            query = viewModel.query,
            onQueryChange = { viewModel.query = it },
            placeholder = "Search players or tournaments...",
            onSearch = { viewModel.search() }
        )

        when {
            viewModel.isLoading -> LoadingView()
            viewModel.error != null -> EmptyStateView(title = "Error", description = viewModel.error)
            viewModel.results == null -> EmptyStateView(title = "Search", description = "Enter a name or UAID to find players and tournaments")
            viewModel.results?.players?.content?.isEmpty() == true && viewModel.results?.tournaments?.content?.isEmpty() == true ->
                EmptyStateView(title = "No results", description = "Try a different search term")
            else -> {
                val players = viewModel.results?.players
                val tournaments = viewModel.results?.tournaments
                val hasResults = (players?.content?.isNotEmpty() == true) || (tournaments?.content?.isNotEmpty() == true)

                if (hasResults) {
                    Text(
                        "${players?.totalElements ?: 0} players · ${tournaments?.totalElements ?: 0} tournaments found",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    players?.content?.let { list ->
                        if (list.isNotEmpty()) {
                            item { Text("Players", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                            items(list) { player ->
                                PlayerCard(player = player, onClick = { onPlayerClick(player.uaid) })
                            }
                            players.totalPages.takeIf { it > 1 }?.let {
                                item {
                                    PaginationControls(
                                        currentPage = viewModel.currentPage,
                                        totalPages = it,
                                        onPageChange = { viewModel.loadPage(it) }
                                    )
                                }
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                    tournaments?.content?.let { list ->
                        if (list.isNotEmpty()) {
                            item { Text("Tournaments", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                            items(list) { tournament ->
                                TournamentCard(tournament = tournament, onClick = { onTournamentClick(tournament.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}
