package com.usta.query.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "division_name", nullable = false, length = 100)
    private String divisionName;

    @Column(nullable = false, length = 30)
    private String round;

    @Column(name = "match_type", nullable = false, length = 10)
    private String matchType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id", nullable = false)
    private Player player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id")
    private Player player2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent1_id")
    private Player opponent1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent2_id")
    private Player opponent2;

    @Column(name = "opponent1_name", length = 200)
    private String opponent1Name;

    @Column(name = "opponent2_name", length = 200)
    private String opponent2Name;

    @Column(name = "winner_side", length = 10)
    private String winnerSide;

    @Column(name = "win_type", length = 30)
    private String winType;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "score_summary", length = 100)
    private String scoreSummary;

    @Column(name = "duration_minutes", columnDefinition = "SMALLINT UNSIGNED")
    private Short durationMinutes;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("setNumber ASC")
    private List<MatchSet> sets = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addSet(MatchSet set) {
        sets.add(set);
        set.setMatch(this);
    }

    public void removeSet(MatchSet set) {
        sets.remove(set);
        set.setMatch(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public String getDivisionName() { return divisionName; }
    public void setDivisionName(String divisionName) { this.divisionName = divisionName; }

    public String getRound() { return round; }
    public void setRound(String round) { this.round = round; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public Player getPlayer1() { return player1; }
    public void setPlayer1(Player player1) { this.player1 = player1; }

    public Player getPlayer2() { return player2; }
    public void setPlayer2(Player player2) { this.player2 = player2; }

    public Player getOpponent1() { return opponent1; }
    public void setOpponent1(Player opponent1) { this.opponent1 = opponent1; }

    public Player getOpponent2() { return opponent2; }
    public void setOpponent2(Player opponent2) { this.opponent2 = opponent2; }

    public String getOpponent1Name() { return opponent1Name; }
    public void setOpponent1Name(String opponent1Name) { this.opponent1Name = opponent1Name; }

    public String getOpponent2Name() { return opponent2Name; }
    public void setOpponent2Name(String opponent2Name) { this.opponent2Name = opponent2Name; }

    public String getWinnerSide() { return winnerSide; }
    public void setWinnerSide(String winnerSide) { this.winnerSide = winnerSide; }

    public String getWinType() { return winType; }
    public void setWinType(String winType) { this.winType = winType; }

    public LocalDate getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDate matchDate) { this.matchDate = matchDate; }

    public String getScoreSummary() { return scoreSummary; }
    public void setScoreSummary(String scoreSummary) { this.scoreSummary = scoreSummary; }

    public Short getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Short durationMinutes) { this.durationMinutes = durationMinutes; }

    public List<MatchSet> getSets() { return sets; }
    public void setSets(List<MatchSet> sets) { this.sets = sets; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
