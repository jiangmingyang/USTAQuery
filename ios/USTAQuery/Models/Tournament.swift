import Foundation

struct Tournament: Codable, Identifiable, Hashable {
    let id: Int
    let tournamentId: String?
    let code: String?
    let name: String
    let level: String?
    let category: String?
    let startDate: String?
    let endDate: String?
    let entryDeadline: String?
    let acceptingEntries: Bool?
    let venueName: String?
    let city: String?
    let state: String?
    let section: String?
    let organization: String?
    let orgSlug: String?
    let status: String?
    let eventsCount: Int?
    let surface: String?
    let url: String?
    let directorName: String?
    let totalDraws: Int?
    let events: [TournamentEvent]?
}
