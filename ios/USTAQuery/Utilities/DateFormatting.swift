import Foundation

enum DateFormatting {
    private static let isoFormatter: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withFullDate]
        return f
    }()

    private static let displayFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "MMM d, yyyy"
        f.locale = Locale(identifier: "en_US")
        return f
    }()

    static func format(_ dateStr: String?) -> String {
        guard let dateStr, !dateStr.isEmpty else { return "-" }
        // Try ISO date first (yyyy-MM-dd)
        let cleaned = String(dateStr.prefix(10))
        let df = DateFormatter()
        df.dateFormat = "yyyy-MM-dd"
        df.locale = Locale(identifier: "en_US_POSIX")
        if let date = df.date(from: cleaned) {
            return displayFormatter.string(from: date)
        }
        return dateStr
    }
}
