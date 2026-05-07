import Foundation

struct PlayerStats: Codable {
    let uaid: String
    let totalWins: Int
    let totalLosses: Int
    let winPercentage: Double
    let tournamentsPlayed: Int
}
