import SwiftUI

struct EventPillSelector: View {
    let events: [TournamentEvent]
    let selectedEventId: String?
    let totalCount: Int
    var onSelect: (String?) -> Void

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                // "All" pill
                Button {
                    onSelect(nil)
                } label: {
                    HStack(spacing: 4) {
                        Image(systemName: "person.2")
                            .font(.system(size: 10))
                        Text("All (\(totalCount))")
                            .font(.caption.weight(selectedEventId == nil ? .semibold : .regular))
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(selectedEventId == nil ? AppTheme.tennisGreen : Color(.systemGray6))
                    .foregroundStyle(selectedEventId == nil ? .white : .primary)
                    .clipShape(Capsule())
                }

                ForEach(events) { event in
                    Button {
                        onSelect(event.eventId)
                    } label: {
                        HStack(spacing: 3) {
                            if let color = AppTheme.ballColor(for: event.ballColor) {
                                Circle().fill(color).frame(width: 6, height: 6)
                            }
                            Text(eventLabel(event))
                                .font(.caption.weight(selectedEventId == event.eventId ? .semibold : .regular))
                        }
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(selectedEventId == event.eventId ? AppTheme.tennisGreen : Color(.systemGray6))
                        .foregroundStyle(selectedEventId == event.eventId ? .white : .primary)
                        .clipShape(Capsule())
                    }
                }
            }
            .padding(.horizontal)
        }
    }

    private func eventLabel(_ event: TournamentEvent) -> String {
        let gender = AppConstants.genderMap[event.gender ?? ""] ?? event.gender ?? ""
        let age = event.ageCategory ?? ""
        let type = event.eventType ?? ""
        return [gender, age, type].filter { !$0.isEmpty }.joined(separator: " ")
    }
}
