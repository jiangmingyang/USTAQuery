import SwiftUI

struct EntriesGroupedView: View {
    let groups: [(StatusGroup, [DisplayRow])]
    let eventMap: [String: TournamentEvent]
    let selectedEventId: String?

    var body: some View {
        LazyVStack(spacing: 8) {
            ForEach(groups, id: \.0) { group, rows in
                // Group header - standalone above the list, like Android
                Text("\(group.label) (\(rows.count))")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(group.color)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.top, 8)

                // Entry cards
                ForEach(Array(rows.enumerated()), id: \.element.id) { idx, row in
                    EntryRow(row: row, index: idx, eventMap: eventMap, showEvent: selectedEventId == nil)
                }
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
        HStack(spacing: 4) {
            // Position - green bold number like Android
            Text("\(row.entryPosition ?? (index + 1)).")
                .font(.system(size: 13, weight: .bold))
                .foregroundStyle(AppTheme.tennisGreen)
                .frame(width: 28, alignment: .leading)

            // Player name(s)
            VStack(alignment: .leading, spacing: 2) {
                if isPair {
                    Text("\(displayName(row.entries[0])) / \(displayName(row.entries[1]))")
                        .font(.system(size: 14, weight: .medium))
                        .lineLimit(1)
                    let loc1 = locationString(row.entries[0])
                    let loc2 = locationString(row.entries[1])
                    let locText = switch (loc1.isEmpty, loc2.isEmpty) {
                    case (false, false): "\(loc1) / \(loc2)"
                    case (false, true): loc1
                    case (true, false): loc2
                    default: ""
                    }
                    if !locText.isEmpty {
                        Text(locText)
                            .font(.system(size: 12))
                            .foregroundStyle(.secondary)
                            .lineLimit(1)
                    }
                } else {
                    if let uaid = row.entries[0].playerUaid, !uaid.isEmpty {
                        NavigationLink(value: PlayerRoute(uaid: uaid)) {
                            Text(displayName(row.entries[0]))
                                .font(.system(size: 14, weight: .medium))
                                .lineLimit(1)
                        }
                    } else {
                        Text(displayName(row.entries[0]))
                            .font(.system(size: 14, weight: .medium))
                            .lineLimit(1)
                    }
                    let loc = locationString(row.entries[0])
                    if !loc.isEmpty {
                        Text(loc)
                            .font(.system(size: 12))
                            .foregroundStyle(.secondary)
                            .lineLimit(1)
                    }
                }
            }

            Spacer(minLength: 4)

            // Points - right aligned, compact
            if isPair {
                let pts0 = row.entries[0].rankingPoints
                let pts1 = row.entries[1].rankingPoints
                Text("\(pts0?.description ?? "—") / \(pts1?.description ?? "—")")
                    .font(.system(size: 12, design: .monospaced))
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            } else {
                if let pts = row.entries[0].rankingPoints {
                    Text("\(pts) pts")
                        .font(.system(size: 12, design: .monospaced))
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }
        }
        .padding(.vertical, 10)
        .padding(.horizontal, 16)
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }

    private func locationString(_ entry: TournamentEntry) -> String {
        let parts = [entry.city, entry.state].compactMap { $0 }.filter { !$0.isEmpty }
        return parts.joined(separator: ", ")
    }

    private func displayName(_ entry: TournamentEntry) -> String {
        if let f = entry.firstName, let l = entry.lastName, !f.isEmpty, !l.isEmpty {
            return "\(f) \(l)"
        }
        return entry.playerName ?? "\(entry.firstName ?? "") \(entry.lastName ?? "")".trimmingCharacters(in: .whitespaces)
    }
}
