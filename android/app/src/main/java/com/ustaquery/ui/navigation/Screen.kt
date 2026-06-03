package com.ustaquery.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search?query={query}") {
        fun createRoute(query: String = "") = "search?query=$query"
    }
    data object PlayerProfile : Screen("player/{uaid}") {
        fun createRoute(uaid: String) = "player/$uaid"
    }
    data object TournamentBrowser : Screen("tournaments")
    data object TournamentDetail : Screen("tournament/{id}") {
        fun createRoute(id: Int) = "tournament/{id}"
    }
    data object Rankings : Screen("rankings/{gender}/{age}") {
        fun createRoute(gender: String = "M", age: String = "Y12") = "rankings/$gender/$age"
    }
    data object RankingsDefault : Screen("rankings_tab")
}
