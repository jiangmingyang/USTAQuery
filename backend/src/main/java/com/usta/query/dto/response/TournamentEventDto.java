package com.usta.query.dto.response;

import java.math.BigDecimal;

public class TournamentEventDto {
    private String eventId;
    private String gender;
    private String eventType;
    private String ageCategory;
    private Short minAge;
    private Short maxAge;
    private String surface;
    private String courtLocation;
    private BigDecimal entryFee;
    private String currency;
    private String level;
    private String ballColor;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getAgeCategory() { return ageCategory; }
    public void setAgeCategory(String ageCategory) { this.ageCategory = ageCategory; }
    public Short getMinAge() { return minAge; }
    public void setMinAge(Short minAge) { this.minAge = minAge; }
    public Short getMaxAge() { return maxAge; }
    public void setMaxAge(Short maxAge) { this.maxAge = maxAge; }
    public String getSurface() { return surface; }
    public void setSurface(String surface) { this.surface = surface; }
    public String getCourtLocation() { return courtLocation; }
    public void setCourtLocation(String courtLocation) { this.courtLocation = courtLocation; }
    public BigDecimal getEntryFee() { return entryFee; }
    public void setEntryFee(BigDecimal entryFee) { this.entryFee = entryFee; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getBallColor() { return ballColor; }
    public void setBallColor(String ballColor) { this.ballColor = ballColor; }
}
