import SwiftUI

struct PlayerRegistrationsTab: View {
    let registrations: PagedResponse<Registration>?
    let isLoading: Bool
    let currentPage: Int
    var onPageChange: (Int) -> Void

    var body: some View {
        if isLoading {
            LoadingView()
        } else if let regs = registrations {
            if regs.content.isEmpty {
                EmptyStateView(title: "No registrations", description: "No active registrations found")
            } else {
                LazyVStack(spacing: 8) {
                    ForEach(regs.content) { reg in
                        RegistrationRow(reg: reg)
                    }
                }
                if regs.totalPages > 1 {
                    PaginationControls(currentPage: currentPage, totalPages: regs.totalPages, onPageChange: onPageChange)
                }
            }
        } else {
            EmptyStateView(title: "No registrations")
        }
    }
}

private struct RegistrationRow: View {
    let reg: Registration

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            NavigationLink(value: TournamentRoute(id: reg.tournament.id)) {
                Text(reg.tournament.name)
                    .font(.subheadline.weight(.medium))
                    .multilineTextAlignment(.leading)
            }
            HStack(spacing: 8) {
                Text(reg.divisionName)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(reg.matchType)
                    .font(.caption2.weight(.medium))
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color(.systemGray5))
                    .clipShape(Capsule())
                Spacer()
                StatusBadge(status: reg.status)
            }
            if let p2 = reg.player2 {
                Text("Partner: \(p2.firstName) \(p2.lastName)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .background(Color(.systemGroupedBackground))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}
