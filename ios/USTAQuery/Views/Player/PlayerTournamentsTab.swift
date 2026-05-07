import SwiftUI

struct PlayerTournamentsTab: View {
    let entries: [PlayerTournamentEntry]
    let isLoading: Bool

    var body: some View {
        if isLoading {
            LoadingView()
        } else if entries.isEmpty {
            EmptyStateView(title: "No tournament entries", description: "No tournament participation data found")
        } else {
            // Group by tournament
            let grouped = Dictionary(grouping: entries) { $0.tournamentInternalId }
            let sortedKeys = grouped.keys.sorted { a, b in
                let dateA = grouped[a]?.first?.startDate ?? ""
                let dateB = grouped[b]?.first?.startDate ?? ""
                return dateA > dateB
            }

            LazyVStack(spacing: 12) {
                ForEach(sortedKeys, id: \.self) { key in
                    if let items = grouped[key], let first = items.first {
                        TournamentEntryCard(name: first.tournamentName, level: first.tournamentLevel, startDate: first.startDate, endDate: first.endDate, city: first.city, state: first.state, entries: items)
                    }
                }
            }
        }
    }
}

private struct TournamentEntryCard: View {
    let name: String
    let level: String?
    let startDate: String?
    let endDate: String?
    let city: String?
    let state: String?
    let entries: [PlayerTournamentEntry]

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                NavigationLink(value: TournamentRoute(id: entries.first?.tournamentInternalId ?? 0)) {
                    Text(name)
                        .font(.subheadline.weight(.semibold))
                        .multilineTextAlignment(.leading)
                }
                Spacer()
                LevelBadge(level: level)
            }

            HStack(spacing: 12) {
                if let date = startDate {
                    Label(DateFormatting.format(date), systemImage: "calendar")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                if let loc = [city, state].compactMap({ $0 }).filter({ !$0.isEmpty }).joined(separator: ", ") as String?, !loc.isEmpty {
                    Label(loc, systemImage: "mappin")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }

            // Event entries
            ForEach(entries, id: \.id) { entry in
                HStack(spacing: 8) {
                    Text(entry.eventType ?? "Event")
                        .font(.caption2.weight(.medium))
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color(.systemGray5))
                        .clipShape(Capsule())
                    if let status = entry.entryStatus {
                        StatusBadge(status: status)
                    }
                }
            }
        }
        .padding()
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: AppTheme.cornerRadius))
    }
}
