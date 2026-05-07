package com.usta.query.service;

import com.usta.query.dto.response.MatchDto;
import com.usta.query.dto.response.PagedResponse;
import com.usta.query.entity.Match;
import com.usta.query.mapper.MatchMapper;
import com.usta.query.mapper.PlayerMapper;
import com.usta.query.repository.MatchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final PlayerMapper playerMapper;

    public MatchService(MatchRepository matchRepository, MatchMapper matchMapper, PlayerMapper playerMapper) {
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
        this.playerMapper = playerMapper;
    }

    public PagedResponse<MatchDto> getByPlayerUaid(String uaid, Pageable pageable) {
        Page<Match> page = matchRepository.findByPlayerUaid(uaid, pageable);
        var content = page.getContent().stream().map(matchMapper::toDto).toList();
        return playerMapper.toPagedResponse(page, content);
    }
}
