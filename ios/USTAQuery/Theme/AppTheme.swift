import SwiftUI

enum AppTheme {
    // MARK: - Colors
    static let tennisGreen = Color(hex: 0x198C52)
    static let winGreen = Color(hex: 0x22C55E)
    static let lossRed = Color(hex: 0xEF4444)

    static let ballYellow = Color(hex: 0xEAB308)
    static let ballGreen = Color(hex: 0x22C55E)
    static let ballOrange = Color(hex: 0xF97316)
    static let ballRed = Color(hex: 0xEF4444)

    // MARK: - Spacing
    static let cardPadding: CGFloat = 16
    static let sectionSpacing: CGFloat = 24
    static let itemSpacing: CGFloat = 8
    static let cornerRadius: CGFloat = 12
    static let badgeRadius: CGFloat = 8
    static let pillRadius: CGFloat = 100

    // MARK: - Fonts
    static let statFont = Font.system(.body, design: .monospaced).weight(.bold)
    static let statLabel = Font.caption.weight(.semibold)

    // MARK: - Helpers
    static func ballColor(for name: String?) -> Color? {
        guard let name = name?.lowercased() else { return nil }
        switch name {
        case "yellow": return ballYellow
        case "green": return ballGreen
        case "orange": return ballOrange
        case "red": return ballRed
        default: return nil
        }
    }

    static func levelColor(_ level: String?) -> Color {
        switch level {
        case "L1", "L2": return tennisGreen
        case "L3", "L4": return .accentColor
        default: return .secondary
        }
    }

    static let heroGradient = LinearGradient(
        colors: [tennisGreen, tennisGreen.opacity(0.7)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
}
