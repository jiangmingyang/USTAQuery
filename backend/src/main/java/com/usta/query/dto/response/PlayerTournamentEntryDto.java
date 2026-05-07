package com.usta.query.dto.response;

import java.time.LocalDate;

public class PlayerTournamentEntryDto {

    // Tournament info
    private Long tournamentInternalId;
    private String tournamentName;
    private String tournamentLevel;
    private String tournamentCategory;
    private LocalDate startDate;
    private LocalDate endDate;
    private String city;
    private String state;
    private String section;

    // Entry info
    private String eventId;
    private String eventType;
    private String entryStatus;
    private String entryStage;
    private Integer entryPosition;

    public Long getTournamentInternalId() { return tournamentInternalId; }
    public void setTournamentInternalId(Long tournamentInternalId) { this.tournamentInternalId = tournamentInternalId; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public String getTournamentLevel() { return tournamentLevel; }
    public void setTournamentLevel(String tournamentLevel) { this.tournamentLevel = tournamentLevel; }

    public String getTournamentCategory() { return tournamentCategory; }
    public void setTournamentCategory(String tournamentCategory) { this.tournamentCategory = tournamentCategory; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEntryStatus() { return entryStatus; }
    public void setEntryStatus(String entryStatus) { this.entryStatus = entryStatus; }

    public String getEntryStage() { return entryStage; }
    public void setEntryStage(String entryStage) { this.entryStage = entryStage; }

    public Integer getEntryPosition() { return entryPosition; }
    public void setEntryPosition(Integer entryPosition) { this.entryPosition = entryPosition; }
}
