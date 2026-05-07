import Foundation

struct PlayerSummary: Codable, Identifiable, Hashable {
    var id: String { uaid }
    let uaid: String
    let firstName: String
    let lastName: String
    let gender: String?
    let city: String?
    let state: String?
    let section: String?
    let district: String?
    let ratingNtrp: String?
    let wtnSingles: Double?
    let wtnDoubles: Double?
    let utrSingles: Double?
}
