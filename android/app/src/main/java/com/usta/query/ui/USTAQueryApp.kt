package com.usta.query.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.usta.query.ui.navigation.Screen
import com.usta.query.ui.screens.HomeScreen
import com.usta.query.ui.screens.PlayerProfileScreen
import com.usta.query.ui.screens.RankingsScreen
import com.usta.query.ui.screens.SearchScreen
import com.usta.query.ui.screens.TournamentBrowserScreen
import com.usta.query.ui.screens.TournamentDetailScreen

@Composable
fun USTAQueryApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSearch = { query ->
                    navController.navigate(Screen.Search.createRoute(query))
                },
                onNavigateToRankings = { gender, age ->
                    navController.navigate(Screen.Rankings.createRoute(gender, age))
                },
                onNavigateToTournaments = {
                    navController.navigate(Screen.TournamentBrowser.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.createRoute())
                }
            )
        }

        composable(
            route = Screen.Search.route,
            arguments = listOf(navArgument("query") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchScreen(
                initialQuery = query,
                onPlayerClick = { uaid ->
                    navController.navigate(Screen.PlayerProfile.createRoute(uaid))
                },
                onTournamentClick = { id ->
                    navController.navigate(Screen.TournamentDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.PlayerProfile.route,
            arguments = listOf(navArgument("uaid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uaid = backStackEntry.arguments?.getString("uaid") ?: ""
            PlayerProfileScreen(
                uaid = uaid,
                onBack = { navController.popBackStack() },
                onTournamentClick = { id ->
                    navController.navigate(Screen.TournamentDetail.createRoute(id))
                }
            )
        }

        composable(Screen.TournamentBrowser.route) {
            TournamentBrowserScreen(
                onBack = { navController.popBackStack() },
                onTournamentClick = { id ->
                    navController.navigate(Screen.TournamentDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.TournamentDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            TournamentDetailScreen(
                tournamentId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Rankings.route,
            arguments = listOf(
                navArgument("gender") { type = NavType.StringType; defaultValue = "M" },
                navArgument("age") { type = NavType.StringType; defaultValue = "Y12" }
            )
        ) { backStackEntry ->
            val gender = backStackEntry.arguments?.getString("gender") ?: "M"
            val age = backStackEntry.arguments?.getString("age") ?: "Y12"
            RankingsScreen(
                initialGender = gender,
                initialAge = age,
                onBack = { navController.popBackStack() },
                onPlayerClick = { uaid ->
                    navController.navigate(Screen.PlayerProfile.createRoute(uaid))
                }
            )
        }
    }
}
