import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            NavigationStack {
                HomeView()
            }
            .tabItem {
                Label("Home", systemImage: "magnifyingglass")
            }

            NavigationStack {
                TournamentBrowserView()
            }
            .tabItem {
                Label("Tournaments", systemImage: "trophy")
            }

            NavigationStack {
                RankingsView()
            }
            .tabItem {
                Label("Rankings", systemImage: "chart.bar")
            }
        }
        .tint(AppTheme.tennisGreen)
    }
}
