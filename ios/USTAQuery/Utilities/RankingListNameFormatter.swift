import Foundation

enum RankingListNameFormatter {
    static func format(catalogId: String?, listType: String? = nil, matchFormat: String? = nil, displayLabel: String? = nil) -> String {
        let cid = catalogId ?? ""

        let suffix = cid.components(separatedBy: "_UNDER_").last ?? ""
        let hasDoubles = suffix.hasPrefix("DOUBLES") || matchFormat == "DOUBLES"

        var lt = listType
        if lt == nil, !cid.isEmpty {
            if cid.contains("_STANDING_") { lt = "STANDING" }
            else if cid.contains("_SEEDING_") { lt = "SEEDING" }
            else if cid.contains("_BONUS_POINTS_") { lt = "BONUS_POINTS" }
            else if cid.contains("_QUOTA_") { lt = "QUOTA" }
            else if cid.contains("_YEAR_END_") { lt = "YEAR_END" }
        }

        switch lt {
        case "STANDING": return "Combined National Standing List"
        case "SEEDING": return hasDoubles ? "Doubles Seeding List" : "Singles Seeding List"
        case "BONUS_POINTS": return "Bonus Points List"
        case "QUOTA": return "Quota List"
        case "YEAR_END": return hasDoubles ? "Final Year End Doubles Rank List" : "Final Year End Combined Rank List"
        default: return displayLabel ?? catalogId ?? lt ?? "Unknown"
        }
    }
}
