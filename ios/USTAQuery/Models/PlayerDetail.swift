import Foundation

struct PlayerDetail: Codable, Identifiable, Hashable {
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
    // Extended fields
    let sectionCode: String?
    let districtCode: String?
    let nationality: String?
    let itfTennisId: String?
    let ageCategory: String?
    let wheelchair: Bool?
    let wtnSinglesConfidence: Int?
    let wtnSinglesLastPlayed: String?
    let wtnSinglesGameZoneUpper: Double?
    let wtnSinglesGameZoneLower: Double?
    let wtnDoublesConfidence: Int?
    let wtnDoublesLastPlayed: String?
    let wtnDoublesGameZoneUpper: Double?
    let wtnDoublesGameZoneLower: Double?
    let utrId: String?
    let utrDoubles: Double?
    let profileImageUrl: String?
    let membershipType: String?
    let membershipExpiry: String?
}
