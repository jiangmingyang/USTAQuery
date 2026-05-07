package com.usta.query.repository;

import com.usta.query.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query(value = "SELECT m FROM Match m LEFT JOIN FETCH m.sets " +
           "WHERE m.player1.uaid = :uaid OR m.player2.uaid = :uaid " +
           "ORDER BY m.matchDate DESC",
           countQuery = "SELECT COUNT(m) FROM Match m " +
           "WHERE m.player1.uaid = :uaid OR m.player2.uaid = :uaid")
    Page<Match> findByPlayerUaid(@Param("uaid") String uaid, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Match m WHERE (m.player1.uaid = :uaid OR m.player2.uaid = :uaid) AND m.winnerSide = :side")
    long countByPlayerUaidAndWinnerSide(@Param("uaid") String uaid, @Param("side") String side);

    @Query("SELECT COUNT(DISTINCT m.tournament.id) FROM Match m WHERE m.player1.uaid = :uaid OR m.player2.uaid = :uaid")
    long countDistinctTournamentsByPlayerUaid(@Param("uaid") String uaid);
}
