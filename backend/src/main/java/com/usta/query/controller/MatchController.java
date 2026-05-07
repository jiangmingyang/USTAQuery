package com.usta.query.controller;

import com.usta.query.dto.response.MatchDto;
import com.usta.query.dto.response.PagedResponse;
import com.usta.query.service.MatchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players/{uaid}/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<MatchDto>> getByPlayer(
            @PathVariable String uaid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(matchService.getByPlayerUaid(uaid, PageRequest.of(page, size)));
    }
}
