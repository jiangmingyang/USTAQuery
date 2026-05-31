import SwiftUI

struct TournamentCard: View {
    let tournament: Tournament

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .top) {
                Text(tournament.name)
                    .font(.subheadline.weight(.semibold))
                    .lineLimit(2)
                Spacer()
                LevelBadge(level: tournament.level)
            }

            if let code = tournament.code, !code.isEmpty {
                Text(code)
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }

            HStack(spacing: 12) {
                Label(dateRange, systemImage: "calendar")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                if let loc = locationText {
                    Label(loc, systemImage: "mappin")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }

            if let regStatus = tournament.registrationStatus {
                RegistrationStatusBadge(status: regStatus)
            }

            if let events = tournament.events, !events.isEmpty {
                EventsSummaryView(events: events)
            }
        }
        .padding(.vertical, 4)
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
}

struct RegistrationStatusBadge: View {
    let status: String

    private var color: Color {
        let lower = status.lowercased()
        if lower.contains("open")   { return .green }
        if lower.contains("closed") { return Color.orange }
        return Color(.systemGray3)
    }

    private var textColor: Color {
        let lower = status.lowercased()
        if lower.contains("open")   { return .green }
        if lower.contains("closed") { return Color.orange }
        return .secondary
    }

    var body: some View {
        Text(status)
            .font(.system(size: 11, weight: .medium))
            .foregroundStyle(textColor)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(color.opacity(0.12))
            .clipShape(Capsule())
    }
}
