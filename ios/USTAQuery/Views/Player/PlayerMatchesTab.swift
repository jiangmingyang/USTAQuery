import SwiftUI

struct PlayerMatchesTab: View {
    let uaid: String
    let matches: PagedResponse<Match>?
    let isLoading: Bool
    let currentPage: Int
    var onPageChange: (Int) -> Void

    var body: some View {
        if isLoading {
            LoadingView()
        } else if let data = matches {
            if data.content.isEmpty {
                EmptyStateView(title: "No matches", description: "No match records found")
            } else {
                LazyVStack(spacing: 10) {
                    ForEach(data.content) { match in
                        MatchCard(match: match, uaid: uaid)
                    }
                }
                if data.totalPages > 1 {
                    PaginationControls(currentPage: currentPage, totalPages: data.totalPages, onPageChange: onPageChange)
                }
            }
        } else {
            EmptyStateView(title: "No matches")
        }
    }
}

private struct MatchCard: View {
    let match: Match
    let uaid: String

    private var isWin: Bool {
        if match.winnerSide == "SIDE1" { return match.player1.uaid == uaid }
        if match.winnerSide == "SIDE2" { return match.player2?.uaid == uaid }
        return false
    }

    private var opponentName: String {
        if match.player1.uaid == uaid {
            if let name = match.opponent1Name, !name.isEmpty { return name }
            if let p2 = match.player2 { return "\(p2.firstName) \(p2.lastName)" }
        } else {
            return "\(match.player1.firstName) \(match.player1.lastName)"
        }
        return "Unknown"
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(match.tournamentName)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
                Spacer()
                if let date = match.matchDate {
                    Text(DateFormatting.format(date))
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                }
            }

            HStack(spacing: 8) {
                WinLossIndicator(isWin: isWin)
                Text("vs \(opponentName)")
                    .font(.subheadline.weight(.medium))
                Spacer()
            }

            HStack {
                Text("\(match.round) - \(match.divisionName)")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                Spacer()
                MatchScoreDisplay(sets: match.sets, winnerSide: match.winnerSide, playerUaid: uaid)
            }

            if let partner = match.opponent2Name, match.matchType.uppercased().contains("DOUBLES") {
                Text("Partner: \(partner)")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: AppTheme.cornerRadius))
    }
}
