package com.usta.query.dto.response;

import java.time.LocalDate;
import java.util.List;

public class TournamentDto {
    private Long id;
    private String tournamentId;
    private String code;
    private String name;
    private String level;
    private String category;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate entryDeadline;
    private boolean acceptingEntries;
    private String venueName;
    private String city;
    private String state;
    private String section;
    private String organization;
    private String orgSlug;
    private String status;
    private Integer eventsCount;
    private String surface;
    private String url;
    private String directorName;
    private Integer totalDraws;
    private List<TournamentEventDto> events;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public LocalDate getEntryDeadline() { return entryDeadline; }
    public void setEntryDeadline(LocalDate entryDeadline) { this.entryDeadline = entryDeadline; }
    public boolean isAcceptingEntries() { return acceptingEntries; }
    public void setAcceptingEntries(boolean acceptingEntries) { this.acceptingEntries = acceptingEntries; }
    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public String getOrgSlug() { return orgSlug; }
    public void setOrgSlug(String orgSlug) { this.orgSlug = orgSlug; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getEventsCount() { return eventsCount; }
    public void setEventsCount(Integer eventsCount) { this.eventsCount = eventsCount; }
    public String getSurface() { return surface; }
    public void setSurface(String surface) { this.surface = surface; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getDirectorName() { return directorName; }
    public void setDirectorName(String directorName) { this.directorName = directorName; }
    public Integer getTotalDraws() { return totalDraws; }
    public void setTotalDraws(Integer totalDraws) { this.totalDraws = totalDraws; }
    public List<TournamentEventDto> getEvents() { return events; }
    public void setEvents(List<TournamentEventDto> events) { this.events = events; }
}
