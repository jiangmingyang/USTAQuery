import SwiftUI

struct PlayerRankingsTab: View {
    let rankings: [Ranking]
    let isLoading: Bool
    @Binding var selectedAge: String
    var onFilterChange: () -> Void

    private let ages = ["All"] + AppConstants.ageRestrictions

    var body: some View {
        VStack(spacing: 12) {
            // Age filter pills
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(ages, id: \.self) { age in
                        Button {
                            selectedAge = age
                            onFilterChange()
                        } label: {
                            Text(age)
                                .font(.caption.weight(selectedAge == age ? .semibold : .regular))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(selectedAge == age ? AppTheme.tennisGreen : Color(.systemGray6))
                                .foregroundStyle(selectedAge == age ? .white : .primary)
                                .clipShape(Capsule())
                        }
                    }
                }
            }

            if isLoading {
                LoadingView()
            } else if rankings.isEmpty {
                EmptyStateView(title: "No rankings", description: "No ranking data found for this player")
            } else {
                LazyVStack(spacing: 6) {
                    // Header
                    HStack {
                        Text("List")
                            .frame(maxWidth: .infinity, alignment: .leading)
                        Text("Dist")
                            .frame(width: 40)
                        Text("Sect")
                            .frame(width: 40)
                        Text("Natl")
                            .frame(width: 40)
                        Text("Pts")
                            .frame(width: 40)
                    }
                    .font(.caption2.weight(.semibold))
                    .foregroundStyle(.secondary)
                    .padding(.horizontal, 4)

                    Divider()

                    ForEach(rankings) { ranking in
                        HStack {
                            Text(RankingListNameFormatter.format(catalogId: ranking.catalogId, listType: ranking.listType, matchFormat: ranking.matchFormat, displayLabel: ranking.displayLabel))
                                .font(.caption)
                                .lineLimit(2)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            Text(ranking.districtRank.map(String.init) ?? "-")
                                .frame(width: 40)
                            Text(ranking.sectionRank.map(String.init) ?? "-")
                                .frame(width: 40)
                            Text(ranking.nationalRank.map(String.init) ?? "-")
                                .frame(width: 40)
                                .foregroundStyle(ranking.nationalRank.map { $0 <= 3 ? AppTheme.tennisGreen : .primary } ?? .primary)
                                .fontWeight(ranking.nationalRank.map { $0 <= 3 ? .bold : .regular } ?? .regular)
                            Text(ranking.points.map(String.init) ?? "-")
                                .frame(width: 40)
                        }
                        .font(.system(.caption, design: .monospaced))
                        .padding(.vertical, 4)
                        .padding(.horizontal, 4)

                        Divider()
                    }
                }
            }
        }
    }
}
