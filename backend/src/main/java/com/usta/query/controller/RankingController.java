package com.usta.query.controller;

import com.usta.query.dto.response.PagedResponse;
import com.usta.query.dto.response.RankingDto;
import com.usta.query.dto.response.RankingHistoryDto;
import com.usta.query.service.RankingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/players/{uaid}/rankings")
    public ResponseEntity<List<RankingDto>> getCurrentByPlayer(
            @PathVariable String uaid,
            @RequestParam(required = false) String listType,
            @RequestParam(required = false) String ageRestriction) {
        return ResponseEntity.ok(rankingService.getCurrentByPlayerUaid(uaid, listType, ageRestriction));
    }

    @GetMapping("/players/{uaid}/rankings/history")
    public ResponseEntity<RankingHistoryDto> getHistory(
            @PathVariable String uaid,
            @RequestParam String catalogId) {
        return ResponseEntity.ok(rankingService.getHistory(uaid, catalogId));
    }

    @GetMapping("/rankings")
    public ResponseEntity<PagedResponse<RankingDto>> getLeaderboard(
            @RequestParam(required = false) String catalogId,
            @RequestParam(required = false) String listType,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String ageRestriction,
            @RequestParam(required = false) String matchFormat,
            @RequestParam(required = false) String publishDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        if (catalogId != null && publishDate != null) {
            LocalDateTime date = LocalDateTime.parse(publishDate);
            return ResponseEntity.ok(rankingService.getLeaderboardByDate(catalogId, date, PageRequest.of(page, size)));
        }
        if (listType != null && gender != null && ageRestriction != null) {
            return ResponseEntity.ok(rankingService.getLeaderboardByFilters(listType, gender, ageRestriction, matchFormat, PageRequest.of(page, size)));
        }
        if (catalogId != null) {
            return ResponseEntity.ok(rankingService.getLeaderboard(catalogId, PageRequest.of(page, size)));
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/rankings/versions")
    public ResponseEntity<List<String>> getRankingVersions(@RequestParam String catalogId) {
        List<LocalDateTime> dates = rankingService.getPublishDates(catalogId);
        List<String> isoStrings = dates.stream().map(LocalDateTime::toString).toList();
        return ResponseEntity.ok(isoStrings);
    }
}
