package com.ustaquery.data.api

import com.ustaquery.data.model.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("search")
    suspend fun unifiedSearch(
        @Query("q") q: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 5
    ): UnifiedSearchResponse

    @GET("players/search")
    suspend fun searchPlayers(
        @Query("q") q: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PagedResponse<PlayerSummary>

    @GET("players/{uaid}")
    suspend fun getPlayer(@Path("uaid") uaid: String): PlayerDetail

    @GET("players/{uaid}/stats")
    suspend fun getPlayerStats(@Path("uaid") uaid: String): PlayerStats

    @GET("players/{uaid}/tournament-entries")
    suspend fun getPlayerTournamentEntries(@Path("uaid") uaid: String): List<PlayerTournamentEntry>

    @GET("players/{uaid}/registrations")
    suspend fun getPlayerRegistrations(
        @Path("uaid") uaid: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PagedResponse<Registration>

    @GET("players/{uaid}/matches")
    suspend fun getPlayerMatches(
        @Path("uaid") uaid: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PagedResponse<Match>

    @GET("players/{uaid}/rankings")
    suspend fun getPlayerRankings(
        @Path("uaid") uaid: String,
        @Query("listType") listType: String? = null,
        @Query("ageRestriction") ageRestriction: String? = null
    ): List<Ranking>

    @GET("tournaments/search")
    suspend fun searchTournaments(
        @Query("q") q: String? = null,
        @Query("section") section: String? = null,
        @Query("level") level: String? = null,
        @Query("state") state: String? = null,
        @Query("year") year: Int? = null,
        @Query("gender") gender: String? = null,
        @Query("ageCategory") ageCategory: String? = null,
        @Query("eventType") eventType: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): PagedResponse<Tournament>

    @GET("tournaments/filters")
    suspend fun getTournamentFilters(): TournamentFilterOptions

    @GET("tournaments/{id}")
    suspend fun getTournament(@Path("id") id: Int): Tournament

    @GET("tournaments/{id}/entries")
    suspend fun getTournamentEntries(
        @Path("id") id: Int,
        @Query("eventId") eventId: String? = null
    ): List<TournamentEntry>

    @GET("rankings")
    suspend fun getLeaderboard(
        @Query("catalogId") catalogId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("publishDate") publishDate: String? = null
    ): PagedResponse<Ranking>

    @GET("rankings/versions")
    suspend fun getRankingVersions(@Query("catalogId") catalogId: String): List<String>
}
