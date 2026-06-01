package com.usta.query.data.model

data class Match(
    val id: Int,
    val tournamentName: String,
    val tournamentId: Int,
    val divisionName: String,
    val round: String,
    val matchType: String,
    val player1: PlayerSummary,
    val player2: PlayerSummary? = null,
    val opponent1Name: String? = null,
    val opponent1Uaid: String? = null,
    val opponent2Name: String? = null,
    val opponent2Uaid: String? = null,
    val winnerSide: String? = null,
    val winType: String? = null,
    val matchDate: String? = null,
    val scoreSummary: String? = null,
    val durationMinutes: Int? = null,
    val sets: List<SetScore> = emptyList()
)

data class SetScore(
    val setNumber: Int,
    val playerGames: Int,
    val opponentGames: Int,
    val tiebreakPlayer: Int? = null,
    val tiebreakOpponent: Int? = null
)
