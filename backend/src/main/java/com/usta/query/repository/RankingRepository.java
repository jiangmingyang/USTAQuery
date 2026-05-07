package com.usta.query.repository;

import com.usta.query.entity.Ranking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking, Long> {

    @Query("SELECT r FROM Ranking r WHERE r.player.uaid = :uaid " +
           "AND (:listType IS NULL OR r.listType = :listType) " +
           "AND (:ageRestriction IS NULL OR r.ageRestriction = :ageRestriction) " +
           "AND r.publishDate = (SELECT MAX(r2.publishDate) FROM Ranking r2 WHERE r2.player.uaid = :uaid " +
           "AND r2.catalogId = r.catalogId)")
    List<Ranking> findCurrentByPlayerUaid(@Param("uaid") String uaid,
                                          @Param("listType") String listType,
                                          @Param("ageRestriction") String ageRestriction);

    @Query("SELECT r FROM Ranking r WHERE r.player.uaid = :uaid " +
           "AND r.catalogId = :catalogId " +
           "ORDER BY r.publishDate ASC")
    List<Ranking> findHistory(@Param("uaid") String uaid,
                              @Param("catalogId") String catalogId);

    @Query(value = "SELECT r FROM Ranking r JOIN FETCH r.player WHERE " +
           "r.catalogId = :catalogId " +
           "AND r.publishDate = (SELECT MAX(r2.publishDate) FROM Ranking r2 WHERE r2.catalogId = :catalogId)",
           countQuery = "SELECT COUNT(r) FROM Ranking r WHERE " +
           "r.catalogId = :catalogId " +
           "AND r.publishDate = (SELECT MAX(r2.publishDate) FROM Ranking r2 WHERE r2.catalogId = :catalogId)")
    Page<Ranking> findLeaderboard(@Param("catalogId") String catalogId,
                                  Pageable pageable);

    @Query(value = "SELECT r FROM Ranking r JOIN FETCH r.player WHERE " +
           "r.listType = :listType " +
           "AND r.rankListGender = :gender " +
           "AND r.ageRestriction = :ageRestriction " +
           "AND (:matchFormat IS NULL " +
           "  OR (:matchFormat = 'NOT_DOUBLES' AND (r.matchFormat IS NULL OR r.matchFormat <> 'DOUBLES')) " +
           "  OR (:matchFormat <> 'NOT_DOUBLES' AND r.matchFormat = :matchFormat)) " +
           "AND r.publishDate = (SELECT MAX(r2.publishDate) FROM Ranking r2 " +
           "WHERE r2.listType = :listType AND r2.rankListGender = :gender AND r2.ageRestriction = :ageRestriction " +
           "AND (:matchFormat IS NULL " +
           "  OR (:matchFormat = 'NOT_DOUBLES' AND (r2.matchFormat IS NULL OR r2.matchFormat <> 'DOUBLES')) " +
           "  OR (:matchFormat <> 'NOT_DOUBLES' AND r2.matchFormat = :matchFormat))) " +
           "ORDER BY r.nationalRank ASC NULLS LAST",
           countQuery = "SELECT COUNT(r) FROM Ranking r WHERE " +
           "r.listType = :listType " +
           "AND r.rankListGender = :gender " +
           "AND r.ageRestriction = :ageRestriction " +
           "AND (:matchFormat IS NULL " +
           "  OR (:matchFormat = 'NOT_DOUBLES' AND (r.matchFormat IS NULL OR r.matchFormat <> 'DOUBLES')) " +
           "  OR (:matchFormat <> 'NOT_DOUBLES' AND r.matchFormat = :matchFormat)) " +
           "AND r.publishDate = (SELECT MAX(r2.publishDate) FROM Ranking r2 " +
           "WHERE r2.listType = :listType AND r2.rankListGender = :gender AND r2.ageRestriction = :ageRestriction " +
           "AND (:matchFormat IS NULL " +
           "  OR (:matchFormat = 'NOT_DOUBLES' AND (r2.matchFormat IS NULL OR r2.matchFormat <> 'DOUBLES')) " +
           "  OR (:matchFormat <> 'NOT_DOUBLES' AND r2.matchFormat = :matchFormat)))")
    Page<Ranking> findLeaderboardByFilters(@Param("listType") String listType,
                                           @Param("gender") String gender,
                                           @Param("ageRestriction") String ageRestriction,
                                           @Param("matchFormat") String matchFormat,
                                           Pageable pageable);
    @Query("SELECT r FROM Ranking r JOIN r.player p WHERE p.uaid IN :uaids " +
           "AND r.catalogId = :catalogId " +
           "AND r.publishDate = (SELECT MAX(r2.publishDate) FROM Ranking r2 WHERE r2.catalogId = :catalogId)")
    List<Ranking> findByCatalogIdAndPlayerUaids(@Param("catalogId") String catalogId,
                                                @Param("uaids") List<String> uaids);

    @Query("SELECT r FROM Ranking r JOIN r.player p WHERE p.uaid IN :uaids " +
           "AND r.catalogId = :catalogId " +
           "AND r.publishDate <= :dateCeiling " +
           "AND r.publishDate = (SELECT MAX(r2.publishDate) FROM Ranking r2 " +
           "WHERE r2.catalogId = :catalogId AND r2.publishDate <= :dateCeiling)")
    List<Ranking> findByCatalogIdAndPlayerUaidsAsOf(@Param("catalogId") String catalogId,
                                                    @Param("uaids") List<String> uaids,
                                                    @Param("dateCeiling") LocalDateTime dateCeiling);

    @Query("SELECT DISTINCT r.publishDate FROM Ranking r WHERE r.catalogId = :catalogId ORDER BY r.publishDate DESC")
    List<LocalDateTime> findDistinctPublishDates(@Param("catalogId") String catalogId);

    @Query(value = "SELECT r FROM Ranking r JOIN FETCH r.player WHERE r.catalogId = :catalogId AND r.publishDate = :publishDate ORDER BY r.nationalRank ASC NULLS LAST",
           countQuery = "SELECT COUNT(r) FROM Ranking r WHERE r.catalogId = :catalogId AND r.publishDate = :publishDate")
    Page<Ranking> findLeaderboardByDate(@Param("catalogId") String catalogId,
                                        @Param("publishDate") LocalDateTime publishDate,
                                        Pageable pageable);
}
