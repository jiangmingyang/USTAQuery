package com.usta.query.controller;

import com.usta.query.dto.response.*;
import com.usta.query.service.PlayerService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<PlayerSummaryDto>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(playerService.search(q, PageRequest.of(page, size)));
    }

    @GetMapping("/{uaid}")
    public ResponseEntity<PlayerDetailDto> getByUaid(@PathVariable String uaid) {
        return ResponseEntity.ok(playerService.getByUaid(uaid));
    }

    @GetMapping("/{uaid}/stats")
    public ResponseEntity<PlayerStatsDto> getStats(@PathVariable String uaid) {
        return ResponseEntity.ok(playerService.getStats(uaid));
    }
}
