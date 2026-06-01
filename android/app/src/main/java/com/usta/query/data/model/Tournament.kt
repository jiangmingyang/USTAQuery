package com.usta.query.data.model

data class Tournament(
    val id: Int,
    val tournamentId: String,
    val code: String? = null,
    val name: String,
    val level: String,
    val category: String,
    val startDate: String,
    val endDate: String? = null,
    val entryDeadline: String? = null,
    val acceptingEntries: Boolean = false,
    val venueName: String? = null,
    val city: String? = null,
    val state: String? = null,
    val section: String? = null,
    val organization: String? = null,
    val orgSlug: String? = null,
    val status: String? = null,
    val registrationStatus: String? = null,
    val eventsCount: Int? = null,
    val surface: String? = null,
    val url: String? = null,
    val directorName: String? = null,
    val totalDraws: Int? = null,
    val events: List<TournamentEvent>? = null
)

data class TournamentEvent(
    val eventId: String,
    val gender: String? = null,
    val eventType: String? = null,
    val ageCategory: String? = null,
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val surface: String? = null,
    val courtLocation: String? = null,
    val entryFee: Double? = null,
    val currency: String? = null,
    val level: String? = null,
    val ballColor: String? = null
)

data class TournamentEntry(
    val eventId: String,
    val participantId: String? = null,
    val playerUaid: String? = null,
    val playerName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val city: String? = null,
    val state: String? = null,
    val eventType: String? = null,
    val entryStage: String? = null,
    val entryStatus: String? = null,
    val entryPosition: Int? = null,
    val statusDetail: String? = null,
    val drawId: String? = null,
    val rankingPoints: Int? = null
)

data class PlayerTournamentEntry(
    val tournamentInternalId: Int,
    val tournamentName: String,
    val tournamentLevel: String? = null,
    val tournamentCategory: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val city: String? = null,
    val state: String? = null,
    val section: String? = null,
    val registrationStatus: String? = null,
    val eventId: String,
    val eventType: String? = null,
    val eventGender: String? = null,
    val eventAgeCategory: String? = null,
    val entryStatus: String? = null,
    val entryStage: String? = null,
    val entryPosition: Int? = null
)

data class TournamentFilterOptions(
    val sections: List<String>,
    val levels: List<String>,
    val genders: List<String>,
    val ageCategories: List<String>,
    val eventTypes: List<String>
)

data class UnifiedSearchResponse(
    val players: PagedResponse<PlayerSummary>? = null,
    val tournaments: PagedResponse<Tournament>? = null
)
