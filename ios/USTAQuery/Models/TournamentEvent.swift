import Foundation

struct TournamentEvent: Codable, Identifiable, Hashable {
    var id: String { eventId }
    let eventId: String
    let gender: String?
    let eventType: String?
    let ageCategory: String?
    let minAge: Int?
    let maxAge: Int?
    let surface: String?
    let courtLocation: String?
    let entryFee: Double?
    let currency: String?
    let level: String?
    let ballColor: String?
}
