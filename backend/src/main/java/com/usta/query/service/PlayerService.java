package com.usta.query.service;

import com.usta.query.dto.response.*;
import com.usta.query.entity.Player;
import com.usta.query.exception.PlayerNotFoundException;
import com.usta.query.mapper.PlayerMapper;
import com.usta.query.repository.MatchRepository;
import com.usta.query.repository.PlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final PlayerMapper playerMapper;

    public PlayerService(PlayerRepository playerRepository, MatchRepository matchRepository, PlayerMapper playerMapper) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.playerMapper = playerMapper;
    }

    public PagedResponse<PlayerSummaryDto> search(String query, Pageable pageable) {
        Page<Player> page = playerRepository.search(query, pageable);
        var content = page.getContent().stream().map(playerMapper::toSummary).toList();
        return playerMapper.toPagedResponse(page, content);
    }

    public PlayerDetailDto getByUaid(String uaid) {
        Player player = playerRepository.findByUaid(uaid)
                .orElseThrow(() -> new PlayerNotFoundException(uaid));
        return playerMapper.toDetail(player);
    }

    public PlayerStatsDto getStats(String uaid) {
        playerRepository.findByUaid(uaid)
                .orElseThrow(() -> new PlayerNotFoundException(uaid));

        long wins = matchRepository.countByPlayerUaidAndWinnerSide(uaid, "PLAYER");
        long losses = matchRepository.countByPlayerUaidAndWinnerSide(uaid, "OPPONENT");
        long tournaments = matchRepository.countDistinctTournamentsByPlayerUaid(uaid);

        PlayerStatsDto stats = new PlayerStatsDto();
        stats.setUaid(uaid);
        stats.setTotalWins((int) wins);
        stats.setTotalLosses((int) losses);
        stats.setWinPercentage(wins + losses > 0 ? (double) wins / (wins + losses) * 100 : 0);
        stats.setTournamentsPlayed((int) tournaments);
        return stats;
    }
}
