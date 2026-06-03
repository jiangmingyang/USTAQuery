package com.ustaquery.data.model

data class PlayerSummary(
    val uaid: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val city: String? = null,
    val state: String? = null,
    val section: String? = null,
    val district: String? = null,
    val ratingNtrp: String? = null,
    val wtnSingles: Double? = null,
    val wtnDoubles: Double? = null,
    val utrSingles: Double? = null
)

data class PlayerDetail(
    val uaid: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val city: String? = null,
    val state: String? = null,
    val section: String? = null,
    val district: String? = null,
    val sectionCode: String? = null,
    val districtCode: String? = null,
    val nationality: String? = null,
    val itfTennisId: String? = null,
    val ageCategory: String? = null,
    val wheelchair: Boolean? = null,
    val wtnSingles: Double? = null,
    val wtnSinglesConfidence: Double? = null,
    val wtnSinglesLastPlayed: String? = null,
    val wtnSinglesGameZoneUpper: Double? = null,
    val wtnSinglesGameZoneLower: Double? = null,
    val wtnDoubles: Double? = null,
    val wtnDoublesConfidence: Double? = null,
    val wtnDoublesLastPlayed: String? = null,
    val wtnDoublesGameZoneUpper: Double? = null,
    val wtnDoublesGameZoneLower: Double? = null,
    val utrId: String? = null,
    val utrSingles: Double? = null,
    val utrDoubles: Double? = null,
    val ratingNtrp: String? = null,
    val profileImageUrl: String? = null,
    val membershipType: String? = null,
    val membershipExpiry: String? = null
)

data class PlayerStats(
    val uaid: String,
    val totalWins: Int,
    val totalLosses: Int,
    val winPercentage: Double,
    val tournamentsPlayed: Int
)
