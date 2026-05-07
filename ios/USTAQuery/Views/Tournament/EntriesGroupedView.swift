import SwiftUI

struct EntriesGroupedView: View {
    let groups: [(StatusGroup, [DisplayRow])]
    let eventMap: [String: TournamentEvent]
    let selectedEventId: String?

    var body: some View {
        LazyVStack(spacing: 20) {
            ForEach(groups, id: \.0) { group, rows in
                VStack(alignment: .leading, spacing: 8) {
                    // Group header
                    HStack(spacing: 6) {
                        Text(group.label)
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(group.color)
                        Text("\(rows.count)")
                            .font(.caption2.weight(.medium))
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(group.color.opacity(0.12))
                            .foregroundStyle(group.color)
                            .clipShape(Capsule())
                    }

                    // Entries
                    ForEach(Array(rows.enumerated()), id: \.element.id) { idx, row in
                        EntryRow(row: row, index: idx, eventMap: eventMap, showEvent: selectedEventId == nil)
                        if idx < rows.count - 1 {
                            Divider()
                        }
                    }
                }
                .padding()
                .background(Color(.systemGroupedBackground))
                .clipShape(RoundedRectangle(cornerRadius: AppTheme.cornerRadius))
            }
        }
    }
}

private struct EntryRow: View {
    let row: DisplayRow
    let index: Int
    let eventMap: [String: TournamentEvent]
    let showEvent: Bool

    private var isPair: Bool { row.entries.count == 2 }

    var body: some View {
        HStack(spacing: 8) {
            // Position
            Text("\(row.entryPosition ?? (index + 1))")
                .font(.system(.caption, design: .monospaced))
                .foregroundStyle(.secondary)
                .frame(width: 28, alignment: .trailing)

            // Player name(s)
            VStack(alignment: .leading, spacing: 2) {
                if isPair {
                    HStack(spacing: 4) {
                        playerLink(row.entries[0])
                        Text("/")
                            .foregroundStyle(.secondary)
                            .font(.caption)
                        playerLink(row.entries[1])
                    }
                } else {
                    playerLink(row.entries[0])
                }

                // Location
                if isPair {
                    HStack(spacing: 4) {
                        locationText(row.entries[0])
                        Text("/")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                        locationText(row.entries[1])
                    }
                } else {
                    locationText(row.entries[0])
                }
            }

            Spacer()

            // Event label (when showing all events)
            if showEvent, let event = eventMap[row.eventId] {
                Text(eventLabel(event))
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            }
        }
        .padding(.vertical, 2)
    }

    @ViewBuilder
    private func playerLink(_ entry: TournamentEntry) -> some View {
        let name = displayName(entry)
        if let uaid = entry.playerUaid, !uaid.isEmpty {
            NavigationLink(value: PlayerRoute(uaid: uaid)) {
                Text(name)
                    .font(.subheadline.weight(.medium))
            }
        } else {
            Text(name)
                .font(.subheadline.weight(.medium))
        }
    }

    @ViewBuilder
    private func locationText(_ entry: TournamentEntry) -> some View {
        let parts = [entry.city, entry.state].compactMap { $0 }.filter { !$0.isEmpty }
        Text(parts.isEmpty ? "-" : parts.joined(separator: ", "))
            .font(.caption2)
            .foregroundStyle(.secondary)
    }

    private func displayName(_ entry: TournamentEntry) -> String {
        if let f = entry.firstName, let l = entry.lastName, !f.isEmpty, !l.isEmpty {
            return "\(f) \(l)"
        }
        return entry.playerName ?? "\(entry.firstName ?? "") \(entry.lastName ?? "")".trimmingCharacters(in: .whitespaces)
    }

    private func eventLabel(_ event: TournamentEvent) -> String {
        let gender = AppConstants.genderMap[event.gender ?? ""] ?? event.gender ?? ""
        let age = event.ageCategory ?? ""
        let type = event.eventType ?? ""
        return [gender, age, type].filter { !$0.isEmpty }.joined(separator: " ")
    }
}
