package com.usta.query.data.model

data class Registration(
    val id: Int,
    val tournament: Tournament,
    val matchType: String,
    val divisionName: String,
    val player1: PlayerSummary,
    val player2: PlayerSummary? = null,
    val seed: Int? = null,
    val status: String,
    val registrationDate: String? = null
)
