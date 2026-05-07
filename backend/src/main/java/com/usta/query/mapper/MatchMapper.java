package com.usta.query.mapper;

import com.usta.query.dto.response.MatchDto;
import com.usta.query.dto.response.SetScoreDto;
import com.usta.query.entity.Match;
import com.usta.query.entity.MatchSet;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class MatchMapper {

    private final PlayerMapper playerMapper;

    public MatchMapper(PlayerMapper playerMapper) {
        this.playerMapper = playerMapper;
    }

    public MatchDto toDto(Match m) {
        MatchDto dto = new MatchDto();
        dto.setId(m.getId());
        dto.setTournamentName(m.getTournament().getName());
        dto.setTournamentId(m.getTournament().getId());
        dto.setDivisionName(m.getDivisionName());
        dto.setRound(m.getRound());
        dto.setMatchType(m.getMatchType());
        dto.setPlayer1(playerMapper.toSummary(m.getPlayer1()));
        if (m.getPlayer2() != null) {
            dto.setPlayer2(playerMapper.toSummary(m.getPlayer2()));
        }
        dto.setOpponent1Name(m.getOpponent1Name());
        if (m.getOpponent1() != null) {
            dto.setOpponent1Uaid(m.getOpponent1().getUaid());
        }
        dto.setOpponent2Name(m.getOpponent2Name());
        if (m.getOpponent2() != null) {
            dto.setOpponent2Uaid(m.getOpponent2().getUaid());
        }
        dto.setWinnerSide(m.getWinnerSide());
        dto.setWinType(m.getWinType());
        dto.setMatchDate(m.getMatchDate());
        dto.setScoreSummary(m.getScoreSummary());
        dto.setDurationMinutes(m.getDurationMinutes() != null ? m.getDurationMinutes().intValue() : null);
        dto.setSets(m.getSets().stream().map(this::toSetDto).toList());
        return dto;
    }

    private SetScoreDto toSetDto(MatchSet s) {
        SetScoreDto dto = new SetScoreDto();
        dto.setSetNumber(s.getSetNumber());
        dto.setPlayerGames(s.getPlayerGames());
        dto.setOpponentGames(s.getOpponentGames());
        dto.setTiebreakPlayer(s.getTiebreakPlayer() != null ? s.getTiebreakPlayer().intValue() : null);
        dto.setTiebreakOpponent(s.getTiebreakOpponent() != null ? s.getTiebreakOpponent().intValue() : null);
        return dto;
    }
}
