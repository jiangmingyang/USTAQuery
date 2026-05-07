package com.usta.query.controller;

import com.usta.query.dto.response.PagedResponse;
import com.usta.query.dto.response.PlayerTournamentEntryDto;
import com.usta.query.dto.response.TournamentDto;
import com.usta.query.dto.response.TournamentEntryDto;
import com.usta.query.service.TournamentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @GetMapping("/tournaments/search")
    public ResponseEntity<PagedResponse<TournamentDto>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String section,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String ageCategory,
            @RequestParam(required = false) String eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(tournamentService.search(
                q, section, level, state, year, gender, ageCategory, eventType,
                PageRequest.of(page, size)));
    }

    @GetMapping("/tournaments/filters")
    public ResponseEntity<Map<String, Object>> getFilters() {
        return ResponseEntity.ok(tournamentService.getFilterOptions());
    }

    @GetMapping("/tournaments/{id}")
    public ResponseEntity<TournamentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tournamentService.getById(id));
    }

    @GetMapping("/tournaments/{id}/entries")
    public ResponseEntity<List<TournamentEntryDto>> getEntries(
            @PathVariable Long id,
            @RequestParam(required = false) String eventId) {
        return ResponseEntity.ok(tournamentService.getEntriesByTournament(id, eventId));
    }

    @GetMapping("/players/{uaid}/tournaments")
    public ResponseEntity<PagedResponse<TournamentDto>> getByPlayer(
            @PathVariable String uaid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(tournamentService.getByPlayerUaid(uaid, PageRequest.of(page, size)));
    }

    @GetMapping("/players/{uaid}/tournament-entries")
    public ResponseEntity<List<PlayerTournamentEntryDto>> getPlayerEntries(
            @PathVariable String uaid) {
        return ResponseEntity.ok(tournamentService.getPlayerEntries(uaid));
    }
}
