import SwiftUI

struct TournamentDetailView: View {
    let tournamentId: Int
    @State private var viewModel: TournamentDetailViewModel

    init(tournamentId: Int) {
        self.tournamentId = tournamentId
        _viewModel = State(initialValue: TournamentDetailViewModel(tournamentId: tournamentId))
    }

    var body: some View {
        Group {
            if viewModel.isLoading {
                LoadingView()
            } else if let error = viewModel.error {
                ErrorAlertView(message: error) {
                    Task { await viewModel.loadTournament() }
                }
                .padding()
            } else if let tournament = viewModel.tournament {
                ScrollView {
                    VStack(spacing: 16) {
                        // Tournament info card
                        TournamentInfoCard(tournament: tournament)

                        // Event pills
                        if let events = tournament.events, !events.isEmpty {
                            EventPillSelector(
                                events: events,
                                selectedEventId: viewModel.selectedEventId,
                                totalCount: viewModel.entries.count
                            ) { eventId in
                                Task { await viewModel.selectEvent(eventId) }
                            }
                        }

                        // Entries
                        if viewModel.entriesLoading {
                            LoadingView()
                        } else if viewModel.entries.isEmpty {
                            EmptyStateView(title: "No entries found", description: "No player registrations are available for this tournament yet.")
                        } else {
                            let eventMap = Dictionary(uniqueKeysWithValues: (tournament.events ?? []).map { ($0.eventId, $0) })
                            EntriesGroupedView(
                                groups: viewModel.groupedByStatus,
                                eventMap: eventMap,
                                selectedEventId: viewModel.selectedEventId
                            )
                            .padding(.horizontal)
                        }
                    }
                    .padding(.bottom, 20)
                }
            }
        }
        .navigationTitle(viewModel.tournament?.name ?? "Tournament")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if let tournament = viewModel.tournament, let tid = tournament.tournamentId, let slug = tournament.orgSlug {
                ToolbarItem(placement: .topBarTrailing) {
                    Link(destination: URL(string: "https://playtennis.usta.com/Competitions/\(slug)/Tournaments/Overview/\(tid)")!) {
                        Image(systemName: "arrow.up.right.square")
                            .font(.subheadline)
                    }
                }
            }
        }
        .task { await viewModel.loadTournament() }
        .refreshable { await viewModel.loadTournament() }
    }
}

// MARK: - Tournament Info Card

private struct TournamentInfoCard: View {
    let tournament: Tournament

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(tournament.name)
                        .font(.headline)
                    if let code = tournament.code, !code.isEmpty {
                        Text(code)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                Spacer()
                LevelBadge(level: tournament.level)
            }

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                InfoItem(icon: "calendar", label: "Dates", value: dateRange)
                if let loc = locationText {
                    InfoItem(icon: "mappin", label: "Location", value: loc)
                }
                if let venue = tournament.venueName {
                    InfoItem(label: "Venue", value: venue)
                }
                if let section = tournament.section {
                    InfoItem(label: "Section", value: section)
                }
                if let surface = tournament.surface {
                    InfoItem(label: "Surface", value: surface)
                }
                if let director = tournament.directorName {
                    InfoItem(label: "Director", value: director)
                }
                InfoItem(icon: "trophy", label: "Events", value: "\(tournament.eventsCount ?? tournament.events?.count ?? 0)")
                InfoItem(label: "Status", value: statusText)
            }
        }
        .padding()
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: AppTheme.cornerRadius))
        .padding(.horizontal)
    }

    private var dateRange: String {
        let start = DateFormatting.format(tournament.startDate)
        if let end = tournament.endDate {
            return "\(start) - \(DateFormatting.format(end))"
        }
        return start
    }

    private var locationText: String? {
        let parts = [tournament.city, tournament.state].compactMap { $0 }.filter { !$0.isEmpty }
        return parts.isEmpty ? nil : parts.joined(separator: ", ")
    }

    private var statusText: String {
        if tournament.status == "cancelled" { return "Cancelled" }
        return (tournament.acceptingEntries ?? false) ? "Accepting Entries" : "Entries Closed"
    }
}

private struct InfoItem: View {
    var icon: String? = nil
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.caption2)
                .foregroundStyle(.secondary)
            HStack(spacing: 4) {
                if let icon {
                    Image(systemName: icon)
                        .font(.caption2)
                }
                Text(value)
                    .font(.subheadline.weight(.medium))
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
