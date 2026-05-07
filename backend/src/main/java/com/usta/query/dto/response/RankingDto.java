package com.usta.query.dto.response;

import java.time.LocalDateTime;

public class RankingDto {
    private Long id;
    private String playerUaid;
    private String playerFirstName;
    private String playerLastName;
    private String catalogId;
    private String displayLabel;
    private String playerType;
    private String ageRestriction;
    private String ageRestrictionModifier;
    private String rankListGender;
    private String listType;
    private String matchFormat;
    private String matchFormatType;
    private String familyCategory;
    private Integer nationalRank;
    private Integer sectionRank;
    private Integer districtRank;
    private Integer points;
    private Integer singlesPoints;
    private Integer doublesPoints;
    private Integer bonusPoints;
    private Integer wins;
    private Integer losses;
    private String trendDirection;
    private LocalDateTime publishDate;
    private String section;
    private String district;
    private String state;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlayerUaid() { return playerUaid; }
    public void setPlayerUaid(String playerUaid) { this.playerUaid = playerUaid; }
    public String getPlayerFirstName() { return playerFirstName; }
    public void setPlayerFirstName(String playerFirstName) { this.playerFirstName = playerFirstName; }
    public String getPlayerLastName() { return playerLastName; }
    public void setPlayerLastName(String playerLastName) { this.playerLastName = playerLastName; }
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
}
