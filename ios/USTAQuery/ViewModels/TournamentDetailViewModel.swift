import SwiftUI

// MARK: - Display Row Types

enum StatusGroup: String, CaseIterable {
    case accepted, alternate, withdrawn, other

    var label: String {
        switch self {
        case .accepted: return "Acceptance"
        case .alternate: return "Alternates"
        case .withdrawn: return "Withdrawn"
        case .other: return "Other"
        }
    }

    var color: Color {
        switch self {
        case .accepted: return AppTheme.tennisGreen
        case .alternate: return .orange
        case .withdrawn: return AppTheme.lossRed
        case .other: return .secondary
        }
    }
}

struct DisplayRow: Identifiable {
    let id = UUID()
    var entries: [TournamentEntry]
    var eventId: String
    var entryPosition: Int?
    var entryStatus: String?
}

// MARK: - ViewModel

@Observable
final class TournamentDetailViewModel {
    let tournamentId: Int
    var tournament: Tournament?
    var entries: [TournamentEntry] = []
    var selectedEventId: String?
    var isLoading = false
    var entriesLoading = false
    var error: String?

    init(tournamentId: Int) {
        self.tournamentId = tournamentId
    }

    func loadTournament() async {
        isLoading = true
        error = nil
        do {
            tournament = try await APIClient.getTournament(id: tournamentId)
            await loadEntries()
        } catch {
            self.error = error.localizedDescription
        }
        isLoading = false
    }

    func loadEntries() async {
        entriesLoading = true
        do {
            entries = try await APIClient.getTournamentEntries(id: tournamentId, eventId: selectedEventId)
        } catch {
            entries = []
        }
        entriesLoading = false
    }

    func selectEvent(_ eventId: String?) async {
        selectedEventId = eventId
        await loadEntries()
    }

    // MARK: - Build Display Rows (ported from TournamentDetailPage.tsx)

    var displayRows: [DisplayRow] {
        buildDisplayRows(entries)
    }

    var groupedByStatus: [(StatusGroup, [DisplayRow])] {
        let rows = displayRows
        var groups: [StatusGroup: [DisplayRow]] = [:]
        for row in rows {
            let g = classifyStatus(row.entryStatus)
            groups[g, default: []].append(row)
        }
        return StatusGroup.allCases.compactMap { g in
            guard let rows = groups[g], !rows.isEmpty else { return nil }
            return (g, rows)
        }
    }

    private func classifyStatus(_ status: String?) -> StatusGroup {
        guard let s = status?.uppercased() else { return .other }
        if s.contains("DIRECT") || s == "REGISTERED" { return .accepted }
        if s.contains("ALTERNATE") { return .alternate }
        if s.contains("WITHDRAWN") { return .withdrawn }
        return .other
    }

    private func buildDisplayRows(_ entries: [TournamentEntry]) -> [DisplayRow] {
        var rows: [DisplayRow] = []
        var teamEntries: [TournamentEntry] = []
        var individualsByKey: [String: [TournamentEntry]] = [:]
        var singlesOrOther: [TournamentEntry] = []

        // Classify entries
        for e in entries {
            let isDoubles = (e.eventType ?? "").uppercased().contains("DOUBLES")
            let isTeam = (e.playerName ?? "").contains("/") && (e.firstName?.trimmingCharacters(in: .whitespaces).isEmpty ?? true)

            if isDoubles && isTeam {
                teamEntries.append(e)
            } else if isDoubles && !(e.firstName?.trimmingCharacters(in: .whitespaces).isEmpty ?? true) {
                let ln = (e.lastName ?? "").trimmingCharacters(in: .whitespaces).lowercased()
                let key = "\(e.eventId)::\(e.drawId ?? "")::\(ln)"
                individualsByKey[key, default: []].append(e)
            } else {
                singlesOrOther.append(e)
            }
        }

        // Pair doubles using team entries as lookup
        var pairedIds: Set<String> = []

        for team in teamEntries {
            let names = (team.playerName ?? "").split(separator: "/").map { $0.trimmingCharacters(in: .whitespaces).lowercased() }
            guard names.count == 2 else { continue }

            let drawId = team.drawId ?? ""
            var pair: [TournamentEntry] = []

            for ln in names {
                let key = "\(team.eventId)::\(drawId)::\(ln)"
                if let candidates = individualsByKey[key] {
                    let unused = candidates.filter { !pairedIds.contains($0.participantId ?? "") }
                    let pick = unused.first { $0.entryPosition == team.entryPosition } ?? unused.first
                    if let pick {
                        pair.append(pick)
                        if let pid = pick.participantId { pairedIds.insert(pid) }
                    }
                }
            }

            if pair.count == 2 {
                rows.append(DisplayRow(entries: pair, eventId: team.eventId, entryPosition: team.entryPosition, entryStatus: team.entryStatus))
            } else {
                rows.append(DisplayRow(entries: [team], eventId: team.eventId, entryPosition: team.entryPosition, entryStatus: team.entryStatus))
            }
        }

        // Add unmatched individual doubles entries
        for list in individualsByKey.values {
            for e in list {
                if !pairedIds.contains(e.participantId ?? "") {
                    rows.append(DisplayRow(entries: [e], eventId: e.eventId, entryPosition: e.entryPosition, entryStatus: e.entryStatus))
                }
            }
        }

        // Add singles entries
        for e in singlesOrOther {
            rows.append(DisplayRow(entries: [e], eventId: e.eventId, entryPosition: e.entryPosition, entryStatus: e.entryStatus))
        }

        // Sort by entryPosition when available, otherwise preserve order
        let hasPositions = rows.contains { $0.entryPosition != nil }
        if hasPositions {
            rows.sort { a, b in
                (a.entryPosition ?? Int.max) < (b.entryPosition ?? Int.max)
            }
        }

        return rows
    }
}
