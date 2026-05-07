package com.usta.query.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_sets")
public class MatchSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "set_number", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Short setNumber;

    @Column(name = "player_games", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Short playerGames;

    @Column(name = "opponent_games", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Short opponentGames;

    @Column(name = "tiebreak_player", columnDefinition = "TINYINT UNSIGNED")
    private Short tiebreakPlayer;

    @Column(name = "tiebreak_opponent", columnDefinition = "TINYINT UNSIGNED")
    private Short tiebreakOpponent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }

    public Short getSetNumber() { return setNumber; }
    public void setSetNumber(Short setNumber) { this.setNumber = setNumber; }

    public Short getPlayerGames() { return playerGames; }
    public void setPlayerGames(Short playerGames) { this.playerGames = playerGames; }

    public Short getOpponentGames() { return opponentGames; }
    public void setOpponentGames(Short opponentGames) { this.opponentGames = opponentGames; }

    public Short getTiebreakPlayer() { return tiebreakPlayer; }
    public void setTiebreakPlayer(Short tiebreakPlayer) { this.tiebreakPlayer = tiebreakPlayer; }

    public Short getTiebreakOpponent() { return tiebreakOpponent; }
    public void setTiebreakOpponent(Short tiebreakOpponent) { this.tiebreakOpponent = tiebreakOpponent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
