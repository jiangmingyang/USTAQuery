package com.ustaquery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.ustaquery.ui.navigation.Screen
import com.ustaquery.ui.screens.HomeScreen
import com.ustaquery.ui.screens.PlayerProfileScreen
import com.ustaquery.ui.screens.RankingsScreen
import com.ustaquery.ui.screens.SearchScreen
import com.ustaquery.ui.screens.TournamentBrowserScreen
import com.ustaquery.ui.screens.TournamentDetailScreen

data class BottomTab(val route: String, val icon: ImageVector, val label: String)

val bottomTabs = listOf(
    BottomTab(Screen.Home.route, Icons.Default.Home, "Home"),
    BottomTab(Screen.TournamentBrowser.route, Icons.Default.SportsTennis, "Tournaments"),
    BottomTab(Screen.RankingsDefault.route, Icons.Default.BarChart, "Rankings"),
)

@Composable
fun USTAQueryApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar) {
                Surface(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomTabs.forEach { tab ->
                            val isSelected = currentRoute == tab.route
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (currentRoute != tab.route) {
                                            navController.navigate(tab.route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    tab.label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
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

            composable(Screen.RankingsDefault.route) {
                RankingsScreen(
                    initialGender = "M",
                    initialAge = "Y12",
                    onBack = { navController.popBackStack() },
                    onPlayerClick = { uaid ->
                        navController.navigate(Screen.PlayerProfile.createRoute(uaid))
                    }
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
}
