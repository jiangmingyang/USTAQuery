import SwiftUI

struct HomeView: View {
    @State private var searchText = ""
    @State private var navigateToSearch = false

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                // Hero section
                ZStack {
                    AppTheme.heroGradient
                    VStack(spacing: 16) {
                        Text("USTA Query")
                            .font(.largeTitle.bold())
                            .foregroundStyle(.white)
                        Text("Search players, tournaments, and rankings")
                            .font(.subheadline)
                            .foregroundStyle(.white.opacity(0.85))

                        SearchBarView(text: $searchText, placeholder: "Search by name or UAID...") {
                            navigateToSearch = true
                        }
                        .padding(.horizontal)
                    }
                    .padding(.vertical, 40)
                }

                // Feature cards
                VStack(spacing: AppTheme.sectionSpacing) {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                        FeatureCard(icon: "person.2", title: "Players", description: "Search player profiles and stats")
                        FeatureCard(icon: "trophy", title: "Tournaments", description: "Browse upcoming and past events")
                        FeatureCard(icon: "sportscourt", title: "Matches", description: "View match results and scores")
                        FeatureCard(icon: "chart.bar", title: "Rankings", description: "National ranking leaderboards")
                    }
                    .padding(.horizontal)

                    // Quick access rankings
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Quick Rankings")
                            .font(.headline)
                            .padding(.horizontal)

                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                            ForEach(AppConstants.ageRestrictions, id: \.self) { age in
                                ForEach(AppConstants.genders, id: \.value) { gender in
                                    NavigationLink(value: RankingRoute(gender: gender.value, age: age)) {
                                        QuickRankingCard(gender: gender.label, age: age)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical, AppTheme.sectionSpacing)
            }
        }
        .navigationDestination(for: SearchRoute.self) { route in
            SearchResultsView(initialQuery: route.query)
        }
        .navigationDestination(for: PlayerRoute.self) { route in
            PlayerProfileView(uaid: route.uaid)
        }
        .navigationDestination(for: TournamentRoute.self) { route in
            TournamentDetailView(tournamentId: route.id)
        }
        .navigationDestination(for: RankingRoute.self) { route in
            RankingsView(initialGender: route.gender, initialAge: route.age)
        }
        .navigationDestination(isPresented: $navigateToSearch) {
            SearchResultsView(initialQuery: searchText)
        }
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Navigation Routes

struct SearchRoute: Hashable {
    let query: String
}

struct PlayerRoute: Hashable {
    let uaid: String
}

struct TournamentRoute: Hashable {
    let id: Int
}

struct RankingRoute: Hashable {
    let gender: String
    let age: String
}

// MARK: - Sub-views

private struct FeatureCard: View {
    let icon: String
    let title: String
    let description: String

    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundStyle(AppTheme.tennisGreen)
            Text(title)
                .font(.subheadline.weight(.semibold))
            Text(description)
                .font(.caption2)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: AppTheme.cornerRadius))
    }
}

private struct QuickRankingCard: View {
    let gender: String
    let age: String

    var body: some View {
        HStack {
            Text("\(gender) \(age)")
                .font(.caption.weight(.medium))
            Spacer()
            Image(systemName: "chevron.right")
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}
