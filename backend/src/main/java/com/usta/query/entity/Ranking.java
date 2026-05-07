package com.usta.query.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rankings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"player_id", "catalog_id", "publish_date"})
})
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "catalog_id", nullable = false, length = 120)
    private String catalogId;

    @Column(name = "display_label", length = 200)
    private String displayLabel;

    @Column(name = "player_type", length = 20)
    private String playerType;

    @Column(name = "age_restriction", nullable = false, length = 10)
    private String ageRestriction;

    @Column(name = "age_restriction_modifier", length = 10)
    private String ageRestrictionModifier;

    @Column(name = "rank_list_gender", nullable = false, length = 1)
    private String rankListGender;

    @Column(name = "list_type", nullable = false, length = 30)
    private String listType;

    @Column(name = "match_format", length = 20)
    private String matchFormat;

    @Column(name = "match_format_type", length = 20)
    private String matchFormatType;

    @Column(name = "family_category", length = 20)
    private String familyCategory;

    @Column(name = "national_rank")
    private Integer nationalRank;

    @Column(name = "section_rank")
    private Integer sectionRank;

    @Column(name = "district_rank")
    private Integer districtRank;

    @Column
    private Integer points;

    @Column(name = "singles_points")
    private Integer singlesPoints;

    @Column(name = "doubles_points")
    private Integer doublesPoints;

    @Column(name = "bonus_points")
    private Integer bonusPoints;

    @Column
    private Integer wins;

    @Column
    private Integer losses;

    @Column(name = "trend_direction", length = 20)
    private String trendDirection;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(length = 50)
    private String section;

    @Column(length = 50)
    private String district;

    @Column(length = 2)
    private String state;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public String getCatalogId() { return catalogId; }
    public void setCatalogId(String catalogId) { this.catalogId = catalogId; }

    public String getDisplayLabel() { return displayLabel; }
    public void setDisplayLabel(String displayLabel) { this.displayLabel = displayLabel; }

    public String getPlayerType() { return playerType; }
    public void setPlayerType(String playerType) { this.playerType = playerType; }

    public String getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(String ageRestriction) { this.ageRestriction = ageRestriction; }

    public String getAgeRestrictionModifier() { return ageRestrictionModifier; }
    public void setAgeRestrictionModifier(String v) { this.ageRestrictionModifier = v; }

    public String getRankListGender() { return rankListGender; }
    public void setRankListGender(String rankListGender) { this.rankListGender = rankListGender; }

    public String getListType() { return listType; }
    public void setListType(String listType) { this.listType = listType; }

    public String getMatchFormat() { return matchFormat; }
    public void setMatchFormat(String matchFormat) { this.matchFormat = matchFormat; }

    public String getMatchFormatType() { return matchFormatType; }
    public void setMatchFormatType(String matchFormatType) { this.matchFormatType = matchFormatType; }

    public String getFamilyCategory() { return familyCategory; }
    public void setFamilyCategory(String familyCategory) { this.familyCategory = familyCategory; }

    public Integer getNationalRank() { return nationalRank; }
    public void setNationalRank(Integer nationalRank) { this.nationalRank = nationalRank; }

    public Integer getSectionRank() { return sectionRank; }
    public void setSectionRank(Integer sectionRank) { this.sectionRank = sectionRank; }

    public Integer getDistrictRank() { return districtRank; }
    public void setDistrictRank(Integer districtRank) { this.districtRank = districtRank; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Integer getSinglesPoints() { return singlesPoints; }
    public void setSinglesPoints(Integer singlesPoints) { this.singlesPoints = singlesPoints; }

    public Integer getDoublesPoints() { return doublesPoints; }
    public void setDoublesPoints(Integer doublesPoints) { this.doublesPoints = doublesPoints; }

    public Integer getBonusPoints() { return bonusPoints; }
    public void setBonusPoints(Integer bonusPoints) { this.bonusPoints = bonusPoints; }

    public Integer getWins() { return wins; }
    public void setWins(Integer wins) { this.wins = wins; }

    public Integer getLosses() { return losses; }
    public void setLosses(Integer losses) { this.losses = losses; }

    public String getTrendDirection() { return trendDirection; }
    public void setTrendDirection(String trendDirection) { this.trendDirection = trendDirection; }

    public LocalDateTime getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDateTime publishDate) { this.publishDate = publishDate; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
