import SwiftUI

struct MatchScoreDisplay: View {
    let sets: [SetScore]
    let winnerSide: String?
    let playerUaid: String? // to determine if this player won

    var body: some View {
        HStack(spacing: 6) {
            ForEach(sets, id: \.setNumber) { set in
                Text(formatSet(set))
                    .font(.system(.caption, design: .monospaced, weight: .medium))
                    .padding(.horizontal, 4)
                    .padding(.vertical, 2)
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 4))
            }
        }
    }

    private func formatSet(_ set: SetScore) -> String {
        var s = "\(set.playerGames)-\(set.opponentGames)"
        if let tp = set.tiebreakPlayer, let to = set.tiebreakOpponent {
            let loserTb = min(tp, to)
            s += "(\(loserTb))"
        }
        return s
    }
}
