package com.usta.query.dto.response;

public class UnifiedSearchResponse {
    private PagedResponse<PlayerSummaryDto> players;
    private PagedResponse<TournamentDto> tournaments;

    public UnifiedSearchResponse() {}

    public UnifiedSearchResponse(PagedResponse<PlayerSummaryDto> players, PagedResponse<TournamentDto> tournaments) {
        this.players = players;
        this.tournaments = tournaments;
    }

    public PagedResponse<PlayerSummaryDto> getPlayers() { return players; }
    public void setPlayers(PagedResponse<PlayerSummaryDto> players) { this.players = players; }

    public PagedResponse<TournamentDto> getTournaments() { return tournaments; }
    public void setTournaments(PagedResponse<TournamentDto> tournaments) { this.tournaments = tournaments; }
}
