package com.usta.query.dto.response;

import java.time.LocalDate;
import java.util.List;

public class MatchDto {
    private Long id;
    private String tournamentName;
    private Long tournamentId;
    private String divisionName;
    private String round;
    private String matchType;
    private PlayerSummaryDto player1;
    private PlayerSummaryDto player2;
    private String opponent1Name;
    private String opponent1Uaid;
    private String opponent2Name;
    private String opponent2Uaid;
    private String winnerSide;
    private String winType;
    private LocalDate matchDate;
    private String scoreSummary;
    private Integer durationMinutes;
    private List<SetScoreDto> sets;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }
    public Long getTournamentId() { return tournamentId; }
    public void setTournamentId(Long tournamentId) { this.tournamentId = tournamentId; }
    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }
    public String getRound() { return round; }
    public void setRound(String round) { this.round = round; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public PlayerSummaryDto getPlayer1() { return player1; }
    public void setPlayer1(PlayerSummaryDto player1) { this.player1 = player1; }
    public PlayerSummaryDto getPlayer2() { return player2; }
    public void setPlayer2(PlayerSummaryDto player2) { this.player2 = player2; }
    public String getOpponent1Name() { return opponent1Name; }
    public void setOpponent1Name(String opponent1Name) { this.opponent1Name = opponent1Name; }
    public String getOpponent1Uaid() { return opponent1Uaid; }
    public void setOpponent1Uaid(String opponent1Uaid) { this.opponent1Uaid = opponent1Uaid; }
    public String getOpponent2Name() { return opponent2Name; }
    public void setOpponent2Name(String opponent2Name) { this.opponent2Name = opponent2Name; }
    public String getOpponent2Uaid() { return opponent2Uaid; }
    public void setOpponent2Uaid(String opponent2Uaid) { this.opponent2Uaid = opponent2Uaid; }
    public String getWinnerSide() { return winnerSide; }
    public void setWinnerSide(String winnerSide) { this.winnerSide = winnerSide; }
    public String getWinType() { return winType; }
    public void setWinType(String winType) { this.winType = winType; }
    public LocalDate getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }
    public String getScoreSummary() { return scoreSummary; }
    public void setScoreSummary(String scoreSummary) { this.scoreSummary = scoreSummary; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public List<SetScoreDto> getSets() { return sets; }
    public void setSets(List<SetScoreDto> sets) { this.sets = sets; }
}
