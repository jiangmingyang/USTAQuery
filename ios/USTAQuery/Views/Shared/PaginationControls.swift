import SwiftUI

struct PaginationControls: View {
    let currentPage: Int
    let totalPages: Int
    var onPageChange: (Int) -> Void

    var body: some View {
        HStack(spacing: 16) {
            Button {
                onPageChange(currentPage - 1)
            } label: {
                Image(systemName: "chevron.left")
            }
            .disabled(currentPage <= 0)

            Text("Page \(currentPage + 1) of \(max(totalPages, 1))")
                .font(.caption)
                .foregroundStyle(.secondary)
                .monospacedDigit()

            Button {
                onPageChange(currentPage + 1)
            } label: {
                Image(systemName: "chevron.right")
            }
            .disabled(currentPage >= totalPages - 1)
        }
        .padding(.vertical, 8)
    }
}
