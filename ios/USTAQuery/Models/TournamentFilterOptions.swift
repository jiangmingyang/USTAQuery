import Foundation

struct TournamentFilterOptions: Codable {
    let sections: [String]
    let levels: [String]
    let genders: [String]
    let ageCategories: [String]
    let eventTypes: [String]
}
