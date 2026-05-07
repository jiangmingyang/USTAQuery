package com.usta.query.repository;

import com.usta.query.entity.TournamentEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TournamentEntryRepository extends JpaRepository<TournamentEntry, Long> {

    List<TournamentEntry> findByTournamentId(Long tournamentId);

    List<TournamentEntry> findByTournamentIdAndEventId(Long tournamentId, String eventId);

    List<TournamentEntry> findByTournamentIdIn(List<Long> tournamentIds);

    List<TournamentEntry> findByPlayerUaid(String playerUaid);

    @Query("SELECT e FROM TournamentEntry e JOIN FETCH e.tournament WHERE e.playerUaid = :uaid ORDER BY e.tournament.startDate DESC")
    List<TournamentEntry> findByPlayerUaidWithTournament(@Param("uaid") String uaid);

    long countByTournamentId(Long tournamentId);
}
