import SwiftUI

struct EventsSummaryView: View {
    let events: [TournamentEvent]

    private static let genderOrder = ["Boys", "Girls", "Coed", "Mixed"]

    private var grouped: [(String, [TournamentEvent])] {
        let dict = Dictionary(grouping: events) { ev in
            AppConstants.genderMap[ev.gender ?? ""] ?? ev.gender ?? "Other"
        }
        let ordered = Self.genderOrder.filter { dict[$0] != nil }
        let remaining = dict.keys.filter { !Self.genderOrder.contains($0) }.sorted()
        return (ordered + remaining).compactMap { key in
            guard let evts = dict[key] else { return nil }
            return (key, evts)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 3) {
            ForEach(grouped, id: \.0) { gender, genderEvents in
                HStack(alignment: .top, spacing: 4) {
                    Text(gender)
                        .font(.system(size: 9, weight: .medium))
                        .foregroundStyle(.secondary)
                        .frame(width: 32, alignment: .leading)

                    FlowLayout(spacing: 4) {
                        ForEach(genderEvents) { event in
                            eventBadge(event)
                        }
                    }
                }
            }
        }
        .padding(.top, 4)
    }

    @ViewBuilder
    private func eventBadge(_ event: TournamentEvent) -> some View {
        HStack(spacing: 3) {
            Text(compactLabel(event))
                .font(.system(size: 9, weight: .medium))
            if let color = AppTheme.ballColor(for: event.ballColor) {
                Circle()
                    .fill(color)
                    .frame(width: 5, height: 5)
            }
        }
        .padding(.horizontal, 5)
        .padding(.vertical, 2)
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 3))
    }

    private func compactLabel(_ event: TournamentEvent) -> String {
        let age = event.ageCategory ?? ""
        let type: String
        if let et = event.eventType {
            let lower = et.lowercased()
            if lower.hasPrefix("s") { type = "S" }
            else if lower.hasPrefix("d") { type = "D" }
            else { type = "" }
        } else {
            type = ""
        }
        return [age, type].filter { !$0.isEmpty }.joined(separator: " ")
    }
}

/// Simple flow layout that wraps children to the next line.
struct FlowLayout: Layout {
    var spacing: CGFloat = 4

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var totalHeight: CGFloat = 0
        var rowWidth: CGFloat = 0
        var rowHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if rowWidth + size.width > maxWidth, rowWidth > 0 {
                totalHeight += rowHeight + spacing
                rowWidth = 0
                rowHeight = 0
            }
            rowWidth += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        totalHeight += rowHeight
        return CGSize(width: maxWidth, height: totalHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var rowHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX, x > bounds.minX {
                x = bounds.minX
                y += rowHeight + spacing
                rowHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}
