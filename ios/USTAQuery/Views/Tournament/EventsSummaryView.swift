import SwiftUI

struct EventsSummaryView: View {
    let events: [TournamentEvent]

    var body: some View {
        HStack(spacing: 4) {
            ForEach(events.prefix(4)) { event in
                HStack(spacing: 3) {
                    if let color = AppTheme.ballColor(for: event.ballColor) {
                        Circle()
                            .fill(color)
                            .frame(width: 6, height: 6)
                    }
                    Text(eventLabel(event))
                        .font(.system(size: 9, weight: .medium))
                }
                .padding(.horizontal, 6)
                .padding(.vertical, 3)
                .background(Color(.systemGray6))
                .clipShape(Capsule())
            }
            if events.count > 4 {
                Text("+\(events.count - 4)")
                    .font(.system(size: 9, weight: .medium))
                    .foregroundStyle(.secondary)
            }
        }
    }

    private func eventLabel(_ event: TournamentEvent) -> String {
        let gender = AppConstants.genderMap[event.gender ?? ""] ?? event.gender ?? ""
        let age = event.ageCategory ?? ""
        let type = event.eventType ?? ""
        return [gender, age, type].filter { !$0.isEmpty }.joined(separator: " ")
    }
}
