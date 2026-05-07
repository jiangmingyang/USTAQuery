import SwiftUI

struct ErrorAlertView: View {
    let message: String
    var onRetry: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 8) {
            HStack(spacing: 8) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .foregroundStyle(AppTheme.lossRed)
                Text(message)
                    .font(.subheadline)
                    .foregroundStyle(.primary)
            }
            if let onRetry {
                Button("Retry", action: onRetry)
                    .font(.subheadline.weight(.medium))
            }
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(AppTheme.lossRed.opacity(0.08))
        .clipShape(RoundedRectangle(cornerRadius: AppTheme.badgeRadius))
    }
}
