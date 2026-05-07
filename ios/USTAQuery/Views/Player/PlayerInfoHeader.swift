import SwiftUI

struct PlayerInfoHeader: View {
    let player: PlayerDetail
    let stats: PlayerStats?

    var body: some View {
        VStack(spacing: 12) {
            // Avatar + Name
            HStack(spacing: 14) {
                Circle()
                    .fill(AppTheme.heroGradient)
                    .frame(width: 60, height: 60)
                    .overlay(
                        Text(initials)
                            .font(.title2.bold())
                            .foregroundStyle(.white)
                    )

                VStack(alignment: .leading, spacing: 2) {
                    Text("\(player.firstName) \(player.lastName)")
                        .font(.title3.bold())
                    if let loc = locationText {
                        Text(loc)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                    Text("UAID: \(player.uaid)")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
            }
            .padding(.horizontal)

            // Rating cards
            HStack(spacing: 10) {
                RatingCard(title: "WTN Singles", value: player.wtnSingles.map { String(format: "%.2f", $0) })
                RatingCard(title: "WTN Doubles", value: player.wtnDoubles.map { String(format: "%.2f", $0) })
                RatingCard(title: "NTRP", value: player.ratingNtrp)
            }
            .padding(.horizontal)

            // Stats card
            if let stats {
                HStack(spacing: 0) {
                    StatItem(label: "Wins", value: "\(stats.totalWins)", color: AppTheme.winGreen)
                    Divider().frame(height: 30)
                    StatItem(label: "Losses", value: "\(stats.totalLosses)", color: AppTheme.lossRed)
                    Divider().frame(height: 30)
                    StatItem(label: "Win %", value: String(format: "%.0f%%", stats.winPercentage))
                    Divider().frame(height: 30)
                    StatItem(label: "Events", value: "\(stats.tournamentsPlayed)")
                }
                .padding(.vertical, 10)
                .background(Color(.systemGroupedBackground))
                .clipShape(RoundedRectangle(cornerRadius: AppTheme.cornerRadius))
                .padding(.horizontal)
            }
        }
    }

    private var initials: String {
        "\(player.firstName.prefix(1))\(player.lastName.prefix(1))".uppercased()
    }

    private var locationText: String? {
        [player.city, player.state].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: ", ")
            .isEmpty ? nil : [player.city, player.state].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: ", ")
    }
}

private struct RatingCard: View {
    let title: String
    let value: String?

    var body: some View {
        VStack(spacing: 4) {
            Text(title)
                .font(.system(size: 9, weight: .medium))
                .foregroundStyle(.secondary)
                .textCase(.uppercase)
            Text(value ?? "-")
                .font(.system(.body, design: .monospaced, weight: .bold))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

private struct StatItem: View {
    let label: String
    let value: String
    var color: Color = .primary

    var body: some View {
        VStack(spacing: 2) {
            Text(value)
                .font(.system(.subheadline, design: .monospaced, weight: .bold))
                .foregroundStyle(color)
            Text(label)
                .font(.system(size: 9, weight: .medium))
                .foregroundStyle(.secondary)
                .textCase(.uppercase)
        }
        .frame(maxWidth: .infinity)
    }
}
