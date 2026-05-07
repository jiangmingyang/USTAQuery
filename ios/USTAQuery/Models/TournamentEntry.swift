import Foundation

struct TournamentEntry: Codable, Identifiable, Hashable {
    var id: String { "\(eventId)-\(participantId ?? UUID().uuidString)" }
    let eventId: String
    let participantId: String?
    let playerUaid: String?
    let playerName: String?
    let firstName: String?
    let lastName: String?
    let gender: String?
    let city: String?
    let state: String?
    let eventType: String?
    let entryStage: String?
    let entryStatus: String?
    let entryPosition: Int?
    let statusDetail: String?
    let drawId: String?
    let rankingPoints: Int?
}
