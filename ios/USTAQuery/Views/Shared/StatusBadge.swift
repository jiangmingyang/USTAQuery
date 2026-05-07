import SwiftUI

struct StatusBadge: View {
    let status: String

    var body: some View {
        Text(status.capitalized)
            .font(.system(size: 10, weight: .medium))
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .foregroundStyle(foregroundColor)
            .background(backgroundColor)
            .clipShape(Capsule())
    }

    private var foregroundColor: Color {
        let upper = status.uppercased()
        if upper.contains("DIRECT") || upper == "REGISTERED" || upper == "CONFIRMED" || upper == "ENTERED" {
            return AppTheme.tennisGreen
        } else if upper.contains("ALTERNATE") {
            return .orange
        } else if upper.contains("WITHDRAWN") {
            return AppTheme.lossRed
        } else {
            return .secondary
        }
    }

    private var backgroundColor: Color {
        foregroundColor.opacity(0.12)
    }
}
