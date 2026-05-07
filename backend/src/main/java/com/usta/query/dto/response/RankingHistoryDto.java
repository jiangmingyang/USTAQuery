package com.usta.query.dto.response;

import java.util.List;

public class RankingHistoryDto {
    private String playerUaid;
    private String catalogId;
    private String displayLabel;
    private List<RankingDto> dataPoints;

    public String getPlayerUaid() { return playerUaid; }
    public void setPlayerUaid(String playerUaid) { this.playerUaid = playerUaid; }
    public String getCatalogId() { return catalogId; }
    public void setCatalogId(String catalogId) { this.catalogId = catalogId; }
    public String getDisplayLabel() { return displayLabel; }
    public void setDisplayLabel(String displayLabel) { this.displayLabel = displayLabel; }
    public List<RankingDto> getDataPoints() { return dataPoints; }
    public void setDataPoints(List<RankingDto> dataPoints) { this.dataPoints = dataPoints; }
}
