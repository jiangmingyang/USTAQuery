import SwiftUI

struct WinLossIndicator: View {
    let isWin: Bool

    var body: some View {
        Text(isWin ? "W" : "L")
            .font(.system(size: 10, weight: .bold))
            .foregroundStyle(.white)
            .frame(width: 20, height: 20)
            .background(isWin ? AppTheme.winGreen : AppTheme.lossRed)
            .clipShape(Circle())
    }
}
