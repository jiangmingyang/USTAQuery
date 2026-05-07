import Foundation

enum AppConstants {
    static let ageRestrictions = ["Y12", "Y14", "Y16", "Y18"]

    static let ageGroupLabels: [String: String] = [
        "Y12": "12 & Under",
        "Y14": "14 & Under",
        "Y16": "16 & Under",
        "Y18": "18 & Under",
    ]

    static let listTypes: [(value: String, label: String, catalogPattern: String)] = [
        ("STANDING", "Combined National Standing List", "JUNIOR_NULL_{G}_STANDING_{A}_UNDER_NULL_NULL_NULL"),
        ("SEEDING_SINGLES", "Singles Seeding List", "JUNIOR_NULL_{G}_SEEDING_{A}_UNDER_SINGLES_NULL_NULL"),
        ("SEEDING_DOUBLES", "Doubles Seeding List", "JUNIOR_NULL_{G}_SEEDING_{A}_UNDER_DOUBLES_INDIVIDUAL_NULL"),
        ("BONUS_POINTS", "Bonus Points List", "JUNIOR_NULL_{G}_BONUS_POINTS_{A}_UNDER_NULL_NULL_NULL"),
        ("QUOTA", "Quota List", "JUNIOR_NULL_{G}_QUOTA_{A}_UNDER_NULL_NULL_S05"),
        ("YEAR_END_COMBINED", "Final Year End Combined Rank List", "JUNIOR_NULL_{G}_YEAR_END_{A}_UNDER_NULL_NULL_NULL"),
        ("YEAR_END_DOUBLES", "Final Year End Doubles Rank List", "JUNIOR_NULL_{G}_YEAR_END_{A}_UNDER_DOUBLES_INDIVIDUAL_NULL"),
    ]

    static let genders: [(value: String, label: String)] = [
        ("M", "Boys"),
        ("F", "Girls"),
    ]

    static func buildCatalogId(pattern: String, gender: String, age: String) -> String {
        pattern.replacingOccurrences(of: "{G}", with: gender)
               .replacingOccurrences(of: "{A}", with: age)
    }

    static let genderMap: [String: String] = [
        "Male": "Boys", "Female": "Girls", "Coed": "Coed", "Mixed": "Mixed",
        "male": "Boys", "female": "Girls", "coed": "Coed", "mixed": "Mixed",
        "Boys": "Boys", "Girls": "Girls",
    ]
}
