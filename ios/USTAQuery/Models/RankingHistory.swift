import Foundation

struct RankingHistory: Codable {
    let playerUaid: String
    let catalogId: String
    let displayLabel: String?
    let dataPoints: [Ranking]
}
