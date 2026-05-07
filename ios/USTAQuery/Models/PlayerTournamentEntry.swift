import Foundation

struct PlayerTournamentEntry: Codable, Identifiable, Hashable {
    var id: String { "\(tournamentInternalId)-\(eventId)" }
    let tournamentInternalId: Int
    let tournamentName: String
    let tournamentLevel: String?
    let tournamentCategory: String?
    let startDate: String?
    let endDate: String?
    let city: String?
    let state: String?
    let section: String?
    let eventId: String
    let eventType: String?
    let entryStatus: String?
    let entryStage: String?
    let entryPosition: Int?
}
