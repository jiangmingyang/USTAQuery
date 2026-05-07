export const AGE_RESTRICTIONS = ["Y12", "Y14", "Y16", "Y18"] as const
export type AgeRestriction = typeof AGE_RESTRICTIONS[number]

export const LIST_TYPES = [
  { value: "STANDING", label: "Combined National Standing List" },
  { value: "SEEDING", label: "Singles Seeding List" },
  { value: "BONUS_POINTS", label: "Bonus Points List" },
  { value: "QUOTA", label: "Quota List" },
  { value: "YEAR_END", label: "Year End Rank List" },
] as const
export type ListType = typeof LIST_TYPES[number]["value"]

export const GENDERS = [
  { value: "M", label: "Boys" },
  { value: "F", label: "Girls" },
] as const

export const US_STATES = [
  "AL","AK","AZ","AR","CA","CO","CT","DE","FL","GA","HI","ID","IL","IN","IA",
  "KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT","NE","NV","NH","NJ",
  "NM","NY","NC","ND","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VT",
  "VA","WA","WV","WI","WY"
] as const

/**
 * Derive a human-readable ranking list name.
 * Primarily parses the catalogId string which encodes all info:
 *   JUNIOR_NULL_{gender}_{listType}_{age}_UNDER_{matchFormat}_{matchFormatType}_{familyCategory}
 * Falls back to listType + matchFormat fields, then displayLabel.
 */
export function formatRankingListName(r: { catalogId?: string | null; listType?: string | null; matchFormat?: string | null; displayLabel?: string | null }): string {
  const cid = r.catalogId ?? ""

  // Parse from catalogId: split on _UNDER_ to get suffix fields
  const suffix = cid.split("_UNDER_")[1] ?? ""
  const hasDoubles = suffix.startsWith("DOUBLES") || r.matchFormat === "DOUBLES"

  // Determine list type from catalogId or field
  let lt = r.listType
  if (!lt && cid) {
    if (cid.includes("_STANDING_")) lt = "STANDING"
    else if (cid.includes("_SEEDING_")) lt = "SEEDING"
    else if (cid.includes("_BONUS_POINTS_")) lt = "BONUS_POINTS"
    else if (cid.includes("_QUOTA_")) lt = "QUOTA"
    else if (cid.includes("_YEAR_END_")) lt = "YEAR_END"
  }

  if (lt === "STANDING") return "Combined National Standing List"
  if (lt === "SEEDING" && hasDoubles) return "Doubles Seeding List"
  if (lt === "SEEDING") return "Singles Seeding List"
  if (lt === "BONUS_POINTS") return "Bonus Points List"
  if (lt === "QUOTA") return "Quota List"
  if (lt === "YEAR_END" && hasDoubles) return "Final Year End Doubles Rank List"
  if (lt === "YEAR_END") return "Final Year End Combined Rank List"

  return r.displayLabel || r.catalogId || lt || "Unknown"
}
