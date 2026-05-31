package com.usta.query.controller;

import com.usta.query.dto.response.UnifiedSearchResponse;
import com.usta.query.service.PlayerService;
import com.usta.query.service.TournamentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final PlayerService playerService;
    private final TournamentService tournamentService;

    public SearchController(PlayerService playerService, TournamentService tournamentService) {
        this.playerService = playerService;
        this.tournamentService = tournamentService;
    }

    @GetMapping
    public UnifiedSearchResponse search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (q == null || q.isBlank()) {
            return new UnifiedSearchResponse(null, null);
        }

        Pageable pageable = PageRequest.of(page, size);
        var players = playerService.search(q, pageable);
        var tournaments = tournamentService.searchUnified(q, pageable);
        return new UnifiedSearchResponse(players, tournaments);
    }
}
