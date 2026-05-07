import SwiftUI

struct PlayerInfoTab: View {
    let player: PlayerDetail

    var body: some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
            InfoRow(label: "Gender", value: player.gender)
            InfoRow(label: "Age Category", value: player.ageCategory)
            InfoRow(label: "Section", value: player.section)
            InfoRow(label: "District", value: player.district)
            InfoRow(label: "Nationality", value: player.nationality)
            InfoRow(label: "Membership", value: player.membershipType)
            if let expiry = player.membershipExpiry {
                InfoRow(label: "Expires", value: DateFormatting.format(expiry))
            }
            if let utr = player.utrSingles {
                InfoRow(label: "UTR Singles", value: String(format: "%.2f", utr))
            }
            if let utr = player.utrDoubles {
                InfoRow(label: "UTR Doubles", value: String(format: "%.2f", utr))
            }
        }
    }
}

private struct InfoRow: View {
    let label: String
    let value: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.caption2)
                .foregroundStyle(.secondary)
                .textCase(.uppercase)
            Text(value ?? "-")
                .font(.subheadline)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
