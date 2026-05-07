package com.usta.query.repository;

import com.usta.query.entity.TournamentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TournamentEventRepository extends JpaRepository<TournamentEvent, Long> {

    List<TournamentEvent> findByTournamentId(Long tournamentId);

    List<TournamentEvent> findByTournamentIdIn(List<Long> tournamentIds);
}
