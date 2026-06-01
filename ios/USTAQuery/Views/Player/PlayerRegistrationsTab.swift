import SwiftUI

struct PlayerRegistrationsTab: View {
    let tournamentEntries: [PlayerTournamentEntry]
    let registrations: PagedResponse<Registration>?
    let isLoading: Bool
    let currentPage: Int
    var onPageChange: (Int) -> Void

    private var hasContent: Bool {
        !tournamentEntries.isEmpty || (registrations?.content.isEmpty == false)
    }

    var body: some View {
        if isLoading {
            LoadingView()
        } else if !hasContent {
            EmptyStateView(title: "No registrations", description: "No active registrations found")
        } else {
            LazyVStack(spacing: 8) {
                // Tournament entries (Registrations open / closed) grouped by tournament
                if !tournamentEntries.isEmpty {
                    let grouped = Dictionary(grouping: tournamentEntries) { $0.tournamentInternalId }
                    let sortedKeys = grouped.keys.sorted { a, b in
                        let dateA = grouped[a]?.first?.startDate ?? ""
                        let dateB = grouped[b]?.first?.startDate ?? ""
                        return dateA > dateB
                    }
                    ForEach(sortedKeys, id: \.self) { key in
                        if let items = grouped[key], let first = items.first {
                            ActiveTournamentEntryCard(entry: first, allEntries: items)
                        }
                    }
                }

                // Legacy registrations
                if let regs = registrations, !regs.content.isEmpty {
                    ForEach(regs.content) { reg in
                        RegistrationRow(reg: reg)
                    }
                    if regs.totalPages > 1 {
                        PaginationControls(currentPage: currentPage, totalPages: regs.totalPages, onPageChange: onPageChange)
                    }
                }
            }
        }
    }
}

private struct ActiveTournamentEntryCard: View {
    let entry: PlayerTournamentEntry
    let allEntries: [PlayerTournamentEntry]

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                NavigationLink(value: TournamentRoute(id: entry.tournamentInternalId)) {
                    Text(entry.tournamentName)
                        .font(.subheadline.weight(.semibold))
                        .multilineTextAlignment(.leading)
                }
                Spacer()
                LevelBadge(level: entry.tournamentLevel)
            }

            HStack(spacing: 12) {
                if let date = entry.startDate {
                    Label(DateFormatting.format(date), systemImage: "calendar")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                if let loc = [entry.city, entry.state].compactMap({ $0 }).filter({ !$0.isEmpty }).joined(separator: ", ") as String?, !loc.isEmpty {
                    Label(loc, systemImage: "mappin")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }

            if let regStatus = entry.registrationStatus {
                RegistrationStatusBadge(status: regStatus)
            }

            FlowLayout(spacing: 6) {
                ForEach(allEntries, id: \.id) { e in
                    Text(compactEventLabel(e))
                        .font(.caption2.weight(.medium))
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color(.systemGray5))
                        .clipShape(Capsule())
                }
            }
        }
        .padding()
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: AppTheme.cornerRadius))
    }
}

private struct RegistrationRow: View {
    let reg: Registration

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            NavigationLink(value: TournamentRoute(id: reg.tournament.id)) {
                Text(reg.tournament.name)
                    .font(.subheadline.weight(.medium))
                    .multilineTextAlignment(.leading)
            }
            HStack(spacing: 8) {
                Text(reg.divisionName)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(reg.matchType)
                    .font(.caption2.weight(.medium))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color(.systemGray5))
                    .clipShape(Capsule())
                Spacer()
                StatusBadge(status: reg.status)
            }
            if let p2 = reg.player2 {
                Text("Partner: \(p2.firstName) \(p2.lastName)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}
