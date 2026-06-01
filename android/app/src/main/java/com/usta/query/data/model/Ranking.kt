package com.usta.query.data.model

data class Ranking(
    val id: Int,
    val playerUaid: String,
    val playerFirstName: String,
    val playerLastName: String,
    val catalogId: String,
    val displayLabel: String? = null,
    val playerType: String? = null,
    val ageRestriction: String,
    val ageRestrictionModifier: String? = null,
    val rankListGender: String,
    val listType: String,
    val matchFormat: String? = null,
    val matchFormatType: String? = null,
    val familyCategory: String? = null,
    val nationalRank: Int? = null,
    val sectionRank: Int? = null,
    val districtRank: Int? = null,
    val points: Int? = null,
    val singlesPoints: Int? = null,
    val doublesPoints: Int? = null,
    val bonusPoints: Int? = null,
    val wins: Int? = null,
    val losses: Int? = null,
    val trendDirection: String? = null,
    val publishDate: String? = null,
    val section: String? = null,
    val district: String? = null,
    val state: String? = null
)

data class RankingHistory(
    val playerUaid: String,
    val catalogId: String,
    val displayLabel: String? = null,
    val dataPoints: List<Ranking> = emptyList()
)
