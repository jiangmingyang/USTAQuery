package com.usta.query.repository;

import com.usta.query.entity.Tournament;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    @Query(value = "SELECT t.* FROM tournaments t WHERE " +
           "(:q IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:filterSections = 0 OR t.section IN (:sections)) " +
           "AND (:filterLevels = 0 OR t.level IN (:levels)) " +
           "AND (:state IS NULL OR t.state = :state) " +
           "AND (:yearStart IS NULL OR t.start_date >= :yearStart) " +
           "AND (:yearEnd IS NULL OR t.start_date <= :yearEnd) " +
           "AND (:filterGenders = 0 OR EXISTS (SELECT 1 FROM tournament_events e WHERE e.tournament_id = t.id AND e.gender IN (:genders))) " +
           "AND (:filterAgeCategories = 0 OR EXISTS (SELECT 1 FROM tournament_events e WHERE e.tournament_id = t.id AND e.age_category IN (:ageCategories))) " +
           "AND (:filterEventTypes = 0 OR EXISTS (SELECT 1 FROM tournament_events e WHERE e.tournament_id = t.id AND e.event_type IN (:eventTypes))) " +
           "ORDER BY t.start_date DESC",
           countQuery = "SELECT COUNT(*) FROM tournaments t WHERE " +
           "(:q IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:filterSections = 0 OR t.section IN (:sections)) " +
           "AND (:filterLevels = 0 OR t.level IN (:levels)) " +
           "AND (:state IS NULL OR t.state = :state) " +
           "AND (:yearStart IS NULL OR t.start_date >= :yearStart) " +
           "AND (:yearEnd IS NULL OR t.start_date <= :yearEnd) " +
           "AND (:filterGenders = 0 OR EXISTS (SELECT 1 FROM tournament_events e WHERE e.tournament_id = t.id AND e.gender IN (:genders))) " +
           "AND (:filterAgeCategories = 0 OR EXISTS (SELECT 1 FROM tournament_events e WHERE e.tournament_id = t.id AND e.age_category IN (:ageCategories))) " +
           "AND (:filterEventTypes = 0 OR EXISTS (SELECT 1 FROM tournament_events e WHERE e.tournament_id = t.id AND e.event_type IN (:eventTypes)))",
           nativeQuery = true)
    Page<Tournament> searchWithFilters(
            @Param("q") String query,
            @Param("filterSections") int filterSections,
            @Param("sections") List<String> sections,
            @Param("filterLevels") int filterLevels,
            @Param("levels") List<String> levels,
            @Param("state") String state,
            @Param("yearStart") LocalDate yearStart,
            @Param("yearEnd") LocalDate yearEnd,
            @Param("filterGenders") int filterGenders,
            @Param("genders") List<String> genders,
            @Param("filterAgeCategories") int filterAgeCategories,
            @Param("ageCategories") List<String> ageCategories,
            @Param("filterEventTypes") int filterEventTypes,
            @Param("eventTypes") List<String> eventTypes,
            Pageable pageable);

    @Query("SELECT DISTINCT t.section FROM Tournament t WHERE t.section IS NOT NULL ORDER BY t.section")
    List<String> findDistinctSections();

    @Query("SELECT DISTINCT t.level FROM Tournament t WHERE t.level IS NOT NULL ORDER BY t.level")
    List<String> findDistinctLevels();

    @Query("SELECT DISTINCT e.gender FROM TournamentEvent e WHERE e.gender IS NOT NULL ORDER BY e.gender")
    List<String> findDistinctGenders();

    @Query("SELECT DISTINCT e.ageCategory FROM TournamentEvent e WHERE e.ageCategory IS NOT NULL ORDER BY e.ageCategory")
    List<String> findDistinctAgeCategories();

    @Query("SELECT DISTINCT e.eventType FROM TournamentEvent e WHERE e.eventType IS NOT NULL ORDER BY e.eventType")
    List<String> findDistinctEventTypes();

    @Query("SELECT DISTINCT r.tournament FROM Registration r WHERE r.player1.uaid = :uaid " +
           "ORDER BY r.tournament.startDate DESC")
    Page<Tournament> findByPlayerUaid(@Param("uaid") String uaid, Pageable pageable);
}
