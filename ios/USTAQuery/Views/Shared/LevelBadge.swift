import SwiftUI

struct LevelBadge: View {
    let level: String?

    var body: some View {
        if let level, !level.isEmpty {
            Text(level)
                .font(.system(size: 10, weight: .bold))
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .foregroundStyle(.white)
                .background(AppTheme.levelColor(level))
                .clipShape(Capsule())
        }
    }
}
