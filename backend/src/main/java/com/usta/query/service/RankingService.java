package com.usta.query.service;

import com.usta.query.dto.response.PagedResponse;
import com.usta.query.dto.response.RankingDto;
import com.usta.query.dto.response.RankingHistoryDto;
import com.usta.query.entity.Ranking;
import com.usta.query.mapper.PlayerMapper;
import com.usta.query.mapper.RankingMapper;
import com.usta.query.repository.RankingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RankingService {

    private final RankingRepository rankingRepository;
    private final RankingMapper rankingMapper;
    private final PlayerMapper playerMapper;

    public RankingService(RankingRepository rankingRepository, RankingMapper rankingMapper, PlayerMapper playerMapper) {
        this.rankingRepository = rankingRepository;
        this.rankingMapper = rankingMapper;
        this.playerMapper = playerMapper;
    }

    public List<RankingDto> getCurrentByPlayerUaid(String uaid, String listType, String ageRestriction) {
        return rankingRepository.findCurrentByPlayerUaid(uaid, listType, ageRestriction)
                .stream().map(rankingMapper::toDto).toList();
    }

    public RankingHistoryDto getHistory(String uaid, String catalogId) {
        List<Ranking> rankings = rankingRepository.findHistory(uaid, catalogId);

        RankingHistoryDto dto = new RankingHistoryDto();
        dto.setPlayerUaid(uaid);
        dto.setCatalogId(catalogId);
        if (!rankings.isEmpty()) {
            dto.setDisplayLabel(rankings.get(0).getDisplayLabel());
        }
        dto.setDataPoints(rankings.stream().map(rankingMapper::toDto).toList());
        return dto;
    }

    public PagedResponse<RankingDto> getLeaderboard(String catalogId, Pageable pageable) {
        Page<Ranking> page = rankingRepository.findLeaderboard(catalogId, pageable);
        var content = page.getContent().stream().map(rankingMapper::toDto).toList();
        return playerMapper.toPagedResponse(page, content);
    }

    public PagedResponse<RankingDto> getLeaderboardByFilters(String listType, String gender, String ageRestriction, String matchFormat, Pageable pageable) {
        Page<Ranking> page = rankingRepository.findLeaderboardByFilters(listType, gender, ageRestriction, matchFormat, pageable);
        var content = page.getContent().stream().map(rankingMapper::toDto).toList();
        return playerMapper.toPagedResponse(page, content);
    }

    public List<LocalDateTime> getPublishDates(String catalogId) {
        return rankingRepository.findDistinctPublishDates(catalogId);
    }

    public PagedResponse<RankingDto> getLeaderboardByDate(String catalogId, LocalDateTime publishDate, Pageable pageable) {
        Page<Ranking> page = rankingRepository.findLeaderboardByDate(catalogId, publishDate, pageable);
        var content = page.getContent().stream().map(rankingMapper::toDto).toList();
        return playerMapper.toPagedResponse(page, content);
    }
}
