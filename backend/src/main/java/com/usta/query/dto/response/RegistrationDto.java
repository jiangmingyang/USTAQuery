package com.usta.query.dto.response;

import java.time.LocalDateTime;

public class RegistrationDto {
    private Long id;
    private TournamentDto tournament;
    private String matchType;
    private String divisionName;
    private PlayerSummaryDto player1;
    private PlayerSummaryDto player2;
    private Integer seed;
    private String status;
    private LocalDateTime registrationDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TournamentDto getTournament() { return tournament; }
    public void setTournament(TournamentDto tournament) { this.tournament = tournament; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }
    public PlayerSummaryDto getPlayer1() { return player1; }
    public void setPlayer1(PlayerSummaryDto player1) { this.player1 = player1; }
    public PlayerSummaryDto getPlayer2() { return player2; }
    public void setPlayer2(PlayerSummaryDto player2) { this.player2 = player2; }
    public Integer getSeed() { return seed; }
    public void setSeed(Integer seed) { this.seed = seed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
}
