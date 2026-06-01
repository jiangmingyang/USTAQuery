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

/// Determine the overall entry status for a group of entries.
/// Priority: accepted > alternate > withdrawn
func classifyEntryStatus(_ entries: [PlayerTournamentEntry]) -> EntryStatusGroup {
    for e in entries {
        let s = (e.entryStatus ?? "").uppercased()
        if s.contains("DIRECT") || s == "REGISTERED" { return .accepted }
    }
    for e in entries {
        let s = (e.entryStatus ?? "").uppercased()
        if s.contains("ALTERNATE") || s.contains("UNGROUPED") { return .alternate }
    }
    for e in entries {
        let s = (e.entryStatus ?? "").uppercased()
        if s.contains("WITHDRAWN") { return .withdrawn }
    }
    return .accepted
}

enum EntryStatusGroup {
    case accepted, alternate, withdrawn

    var label: String {
        switch self {
        case .accepted: return "Accepted"
        case .alternate: return "Alternate"
        case .withdrawn: return "Withdrawn"
        }
    }

    var color: Color {
        switch self {
        case .accepted: return AppTheme.tennisGreen
        case .alternate: return .orange
        case .withdrawn: return AppTheme.lossRed
        }
    }

    var isMainDraw: Bool { self == .accepted }
}

private struct TournamentEntryCard: View {
    let name: String
    let level: String?
    let startDate: String?
    let endDate: String?
    let city: String?
    let state: String?
    let entries: [PlayerTournamentEntry]

    private var status: EntryStatusGroup { classifyEntryStatus(entries) }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                NavigationLink(value: TournamentRoute(id: entries.first?.tournamentInternalId ?? 0)) {
                    Text(name)
                        .font(.subheadline.weight(.semibold))
                        .multilineTextAlignment(.leading)
                }
                Spacer()
                Text(status.label)
                    .font(.caption2.weight(.medium))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(status.color.opacity(0.15))
                    .foregroundStyle(status.color)
                    .clipShape(Capsule())
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
            FlowLayout(spacing: 6) {
                ForEach(entries, id: \.id) { entry in
                    Text(compactEventLabel(entry))
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
        .opacity(status.isMainDraw ? 1.0 : 0.6)
        .overlay(
            RoundedRectangle(cornerRadius: AppTheme.cornerRadius)
                .strokeBorder(status.isMainDraw ? Color.clear : Color(.systemGray3).opacity(0.5), lineWidth: 1)
        )
    }
}

func compactEventLabel(_ entry: PlayerTournamentEntry) -> String {
    let gender = AppConstants.genderMap[entry.eventGender ?? ""] ?? entry.eventGender ?? ""
    let age = entry.eventAgeCategory ?? ""
    let type: String
    if let et = entry.eventType {
        let lower = et.lowercased()
        if lower.hasPrefix("s") { type = "S" }
        else if lower.hasPrefix("d") { type = "D" }
        else { type = et }
    } else {
        type = ""
    }
    return [gender, age, type].filter { !$0.isEmpty }.joined(separator: " ").ifEmpty("Event")
}

private extension String {
    func ifEmpty(_ fallback: String) -> String { self.isEmpty ? fallback : self }
}
