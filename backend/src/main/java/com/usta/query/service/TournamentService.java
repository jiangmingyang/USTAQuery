package com.usta.query.service;

import com.usta.query.dto.response.PagedResponse;
import com.usta.query.dto.response.PlayerTournamentEntryDto;
import com.usta.query.dto.response.TournamentDto;
import com.usta.query.dto.response.TournamentEntryDto;
import com.usta.query.dto.response.TournamentEventDto;
import com.usta.query.entity.Tournament;
import com.usta.query.entity.TournamentEntry;
import com.usta.query.entity.TournamentEvent;
import com.usta.query.entity.Ranking;
import com.usta.query.mapper.PlayerMapper;
import com.usta.query.mapper.TournamentMapper;
import com.usta.query.repository.RankingRepository;
import com.usta.query.repository.TournamentEntryRepository;
import com.usta.query.repository.TournamentEventRepository;
import com.usta.query.repository.TournamentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentEventRepository tournamentEventRepository;
    private final TournamentEntryRepository tournamentEntryRepository;
    private final RankingRepository rankingRepository;
    private final TournamentMapper tournamentMapper;
    private final PlayerMapper playerMapper;

    public TournamentService(TournamentRepository tournamentRepository,
                             TournamentEventRepository tournamentEventRepository,
                             TournamentEntryRepository tournamentEntryRepository,
                             RankingRepository rankingRepository,
                             TournamentMapper tournamentMapper,
                             PlayerMapper playerMapper) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentEventRepository = tournamentEventRepository;
        this.tournamentEntryRepository = tournamentEntryRepository;
        this.rankingRepository = rankingRepository;
        this.tournamentMapper = tournamentMapper;
        this.playerMapper = playerMapper;
    }

    public PagedResponse<TournamentDto> search(String q, String section, String level,
                                                String state, Integer year,
                                                String gender, String ageCategory, String eventType,
                                                Pageable pageable) {
        LocalDate yearStart = year != null ? LocalDate.of(year, 1, 1) : null;
        LocalDate yearEnd = year != null ? LocalDate.of(year, 12, 31) : null;

        List<String> sections = splitFilter(section);
        List<String> levels = splitFilter(level);
        List<String> genders = splitFilter(gender);
        List<String> ageCategories = splitFilter(ageCategory);
        List<String> eventTypes = splitFilter(eventType);

        // Native SQL uses int flags (0/1) to toggle collection filters;
        // pass a dummy non-empty list when filter is inactive to avoid empty IN clause.
        List<String> dummy = List.of("");

        Page<Tournament> page = tournamentRepository.searchWithFilters(
                q,
                sections != null ? 1 : 0, sections != null ? sections : dummy,
                levels != null ? 1 : 0, levels != null ? levels : dummy,
                state, yearStart, yearEnd,
                genders != null ? 1 : 0, genders != null ? genders : dummy,
                ageCategories != null ? 1 : 0, ageCategories != null ? ageCategories : dummy,
                eventTypes != null ? 1 : 0, eventTypes != null ? eventTypes : dummy,
                pageable);

        // Batch-load events for all tournaments in the page
        List<Long> ids = page.getContent().stream().map(Tournament::getId).toList();
        Map<Long, List<TournamentEvent>> eventsMap = ids.isEmpty()
                ? Map.of()
                : tournamentEventRepository.findByTournamentIdIn(ids).stream()
                        .collect(Collectors.groupingBy(e -> e.getTournament().getId()));

        var content = page.getContent().stream().map(t -> {
            TournamentDto dto = tournamentMapper.toDto(t);
            List<TournamentEvent> events = eventsMap.getOrDefault(t.getId(), List.of());
            dto.setEvents(events.stream().map(tournamentMapper::toEventDto).toList());
            return dto;
        }).toList();
        return playerMapper.toPagedResponse(page, content);
    }

    private List<String> splitFilter(String value) {
        if (value == null || value.isBlank()) return null;
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public TournamentDto getById(Long id) {
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found: " + id));
        TournamentDto dto = tournamentMapper.toDto(t);
        List<TournamentEvent> events = tournamentEventRepository.findByTournamentId(id);
        dto.setEvents(events.stream().map(tournamentMapper::toEventDto).toList());
        return dto;
    }

    public PagedResponse<TournamentDto> getByPlayerUaid(String uaid, Pageable pageable) {
        Page<Tournament> page = tournamentRepository.findByPlayerUaid(uaid, pageable);
        var content = page.getContent().stream().map(tournamentMapper::toDto).toList();
        return playerMapper.toPagedResponse(page, content);
    }

    public Map<String, Object> getFilterOptions() {
        Map<String, Object> filters = new HashMap<>();
        filters.put("sections", tournamentRepository.findDistinctSections());
        filters.put("levels", tournamentRepository.findDistinctLevels());
        filters.put("genders", tournamentRepository.findDistinctGenders());
        filters.put("ageCategories", tournamentRepository.findDistinctAgeCategories());
        filters.put("eventTypes", tournamentRepository.findDistinctEventTypes());
        return filters;
    }

    public List<TournamentEntryDto> getEntriesByTournament(Long tournamentId, String eventId) {
        List<TournamentEntry> entries;
        if (eventId != null && !eventId.isBlank()) {
            entries = tournamentEntryRepository.findByTournamentIdAndEventId(tournamentId, eventId);
        } else {
            entries = tournamentEntryRepository.findByTournamentId(tournamentId);
        }
        List<TournamentEntryDto> dtos = entries.stream().map(tournamentMapper::toEntryDto).toList();
        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
        LocalDate startDate = tournament != null ? tournament.getStartDate() : null;
        enrichWithRankingPoints(tournamentId, dtos, startDate);
        return dtos;
    }

    /**
     * Enrich entry DTOs with ranking points from the appropriate seeding list.
     * Singles events use the Singles Seeding List; doubles use the Doubles Seeding List.
     * The age group is matched: event U12 → ranking Y12, U14 → Y14, etc.
     * If the tournament has already started, use the ranking version current at start date.
     */
    private void enrichWithRankingPoints(Long tournamentId, List<TournamentEntryDto> dtos, LocalDate tournamentStartDate) {
        // Determine date ceiling: if tournament has started, freeze to that date's rankings
        LocalDateTime dateCeiling = null;
        if (tournamentStartDate != null && !tournamentStartDate.isAfter(LocalDate.now())) {
            dateCeiling = tournamentStartDate.atTime(23, 59, 59);
        }

        // Build eventId → ageCategory map from tournament events (case-insensitive keys
        // because entries store eventId in uppercase while events use lowercase)
        List<TournamentEvent> events = tournamentEventRepository.findByTournamentId(tournamentId);
        Map<String, String> eventAgeMap = new HashMap<>();
        for (TournamentEvent ev : events) {
            if (ev.getAgeCategory() != null) {
                eventAgeMap.put(ev.getEventId().toUpperCase(), ev.getAgeCategory());
            }
        }

        // Group entries by (gender, ageRestriction, matchFormat) to batch query
        // key = catalogId, value = list of (dto, playerUaid)
        Map<String, List<TournamentEntryDto>> byCatalog = new HashMap<>();
        for (TournamentEntryDto dto : dtos) {
            String uaid = dto.getPlayerUaid();
            if (uaid == null || uaid.isBlank()) continue;

            String gender = normalizeGender(dto.getGender());
            if (gender == null) continue;

            String ageRestriction = toAgeRestriction(eventAgeMap.get(dto.getEventId().toUpperCase()));
            if (ageRestriction == null) continue;

            String matchFormat = isDoubles(dto.getEventType()) ? "DOUBLES_INDIVIDUAL" : "SINGLES";
            String catalogId = buildSeedingCatalogId(gender, ageRestriction, matchFormat);
            byCatalog.computeIfAbsent(catalogId, k -> new ArrayList<>()).add(dto);
        }

        // Batch fetch rankings per catalog and map points to entries
        for (Map.Entry<String, List<TournamentEntryDto>> entry : byCatalog.entrySet()) {
            String catalogId = entry.getKey();
            List<TournamentEntryDto> group = entry.getValue();
            List<String> uaids = group.stream()
                    .map(TournamentEntryDto::getPlayerUaid)
                    .filter(u -> u != null && !u.isBlank())
                    .distinct()
                    .toList();
            if (uaids.isEmpty()) continue;

            List<Ranking> rankings = dateCeiling != null
                    ? rankingRepository.findByCatalogIdAndPlayerUaidsAsOf(catalogId, uaids, dateCeiling)
                    : rankingRepository.findByCatalogIdAndPlayerUaids(catalogId, uaids);
            Map<String, Integer> pointsMap = new HashMap<>();
            for (Ranking r : rankings) {
                if (r.getPlayer() != null && r.getPoints() != null) {
                    pointsMap.put(r.getPlayer().getUaid(), r.getPoints());
                }
            }
            for (TournamentEntryDto dto : group) {
                Integer pts = pointsMap.get(dto.getPlayerUaid());
                if (pts != null) {
                    dto.setRankingPoints(pts);
                }
            }
        }
    }

    private static String normalizeGender(String gender) {
        if (gender == null) return null;
        String g = gender.toUpperCase().trim();
        if (g.startsWith("M")) return "M";
        if (g.startsWith("F")) return "F";
        return null;
    }

    /** Convert event age category (U12, U14) to ranking age restriction (Y12, Y14). */
    private static String toAgeRestriction(String ageCategory) {
        if (ageCategory == null) return null;
        String upper = ageCategory.toUpperCase().trim();
        if (upper.startsWith("U")) return "Y" + upper.substring(1);
        if (upper.startsWith("Y")) return upper;
        // Try to extract digits
        String digits = upper.replaceAll("[^0-9]", "");
        if (!digits.isEmpty()) return "Y" + digits;
        return null;
    }

    private static boolean isDoubles(String eventType) {
        return eventType != null && eventType.toUpperCase().contains("DOUBLES");
    }

    /** Build seeding catalog ID matching the USTA catalog pattern. */
    private static String buildSeedingCatalogId(String gender, String ageRestriction, String matchFormat) {
        // Singles:  JUNIOR_NULL_{G}_SEEDING_{A}_UNDER_SINGLES_NULL_NULL
        // Doubles:  JUNIOR_NULL_{G}_SEEDING_{A}_UNDER_DOUBLES_INDIVIDUAL_NULL
        if ("DOUBLES_INDIVIDUAL".equals(matchFormat)) {
            return "JUNIOR_NULL_" + gender + "_SEEDING_" + ageRestriction + "_UNDER_DOUBLES_INDIVIDUAL_NULL";
        }
        return "JUNIOR_NULL_" + gender + "_SEEDING_" + ageRestriction + "_UNDER_SINGLES_NULL_NULL";
    }

    public List<PlayerTournamentEntryDto> getPlayerEntries(String uaid) {
        List<TournamentEntry> entries = tournamentEntryRepository.findByPlayerUaidWithTournament(uaid);
        return entries.stream().map(tournamentMapper::toPlayerEntryDto).toList();
    }
}
