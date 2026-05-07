import Foundation

struct Ranking: Codable, Identifiable, Hashable {
    let id: Int
    let playerUaid: String
    let playerFirstName: String
    let playerLastName: String
    let catalogId: String
    let displayLabel: String?
    let playerType: String?
    let ageRestriction: String?
    let ageRestrictionModifier: String?
    let rankListGender: String?
    let listType: String?
    let matchFormat: String?
    let matchFormatType: String?
    let familyCategory: String?
    let nationalRank: Int?
    let sectionRank: Int?
    let districtRank: Int?
    let points: Int?
    let singlesPoints: Int?
    let doublesPoints: Int?
    let bonusPoints: Int?
    let wins: Int?
    let losses: Int?
    let trendDirection: String?
    let publishDate: String?
    let section: String?
    let district: String?
    let state: String?
}
