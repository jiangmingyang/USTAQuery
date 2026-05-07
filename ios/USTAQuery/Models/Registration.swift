import Foundation

struct Registration: Codable, Identifiable {
    let id: Int
    let tournament: Tournament
    let matchType: String
    let divisionName: String
    let player1: PlayerSummary
    let player2: PlayerSummary?
    let seed: Int?
    let status: String
    let registrationDate: String?
}
