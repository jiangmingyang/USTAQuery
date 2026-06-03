import SwiftUI

enum AppTab { case home, tournaments, rankings }

struct ContentView: View {
    @State private var selectedTab: AppTab = .home

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationStack {
                HomeView(onSwitchToRankings: { selectedTab = .rankings })
            }
            .tabItem {
                Label("Home", systemImage: "house")
            }
            .tag(AppTab.home)

            NavigationStack {
                TournamentBrowserView()
            }
            .tabItem {
                Label("Tournaments", systemImage: "trophy")
            }
            .tag(AppTab.tournaments)

            NavigationStack {
                RankingsView()
            }
            .tabItem {
                Label("Rankings", systemImage: "chart.bar")
            }
            .tag(AppTab.rankings)
        }
        .tint(AppTheme.tennisGreen)
    }
}
