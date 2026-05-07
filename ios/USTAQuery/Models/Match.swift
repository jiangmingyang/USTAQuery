import Foundation

struct SetScore: Codable, Hashable {
    let setNumber: Int
    let playerGames: Int
    let opponentGames: Int
    let tiebreakPlayer: Int?
    let tiebreakOpponent: Int?
}

struct Match: Codable, Identifiable {
    let id: Int
    let tournamentName: String
    let tournamentId: Int
    let divisionName: String
    let round: String
    let matchType: String
    let player1: PlayerSummary
    let player2: PlayerSummary?
    let opponent1Name: String?
    let opponent1Uaid: String?
    let opponent2Name: String?
    let opponent2Uaid: String?
    let winnerSide: String?
    let winType: String?
    let matchDate: String?
    let scoreSummary: String?
    let durationMinutes: Int?
    let sets: [SetScore]
}
