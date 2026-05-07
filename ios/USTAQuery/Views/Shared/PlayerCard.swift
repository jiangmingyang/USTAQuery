import SwiftUI

struct PlayerCard: View {
    let player: PlayerSummary

    var body: some View {
        HStack(spacing: 12) {
            // Avatar initials
            Circle()
                .fill(AppTheme.tennisGreen.opacity(0.15))
                .frame(width: 44, height: 44)
                .overlay(
                    Text(initials)
                        .font(.system(.subheadline, design: .rounded, weight: .bold))
                        .foregroundStyle(AppTheme.tennisGreen)
                )

            VStack(alignment: .leading, spacing: 2) {
                Text("\(player.firstName) \(player.lastName)")
                    .font(.subheadline.weight(.semibold))
                    .lineLimit(1)
                if let loc = locationText {
                    Text(loc)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }

            Spacer()

            // Ratings
            VStack(alignment: .trailing, spacing: 2) {
                if let wtn = player.wtnSingles {
                    RatingPill(label: "WTN", value: String(format: "%.2f", wtn))
                }
                if let ntrp = player.ratingNtrp {
                    RatingPill(label: "NTRP", value: ntrp)
                }
            }
        }
        .padding(.vertical, 4)
    }

    private var initials: String {
        let f = player.firstName.prefix(1).uppercased()
        let l = player.lastName.prefix(1).uppercased()
        return f + l
    }

    private var locationText: String? {
        [player.city, player.state].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: ", ")
            .nilIfEmpty
    }
}

private struct RatingPill: View {
    let label: String
    let value: String

    var body: some View {
        HStack(spacing: 4) {
            Text(label)
                .font(.system(size: 9, weight: .medium))
                .foregroundStyle(.secondary)
            Text(value)
                .font(.system(size: 11, weight: .semibold, design: .monospaced))
        }
    }
}

private extension String {
    var nilIfEmpty: String? { isEmpty ? nil : self }
}
