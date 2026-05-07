package com.usta.query.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tournaments")
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tournament_id", nullable = false, unique = true, length = 50)
    private String tournamentId;

    @Column(length = 20, unique = true)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 50)
    private String level;

    @Column(length = 20)
    private String category;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "entry_deadline")
    private LocalDate entryDeadline;

    @Column(name = "accepting_entries", nullable = false)
    private boolean acceptingEntries;

    @Column(name = "venue_name", length = 255)
    private String venueName;

    @Column(length = 100)
    private String city;

    @Column(length = 2)
    private String state;

    @Column(length = 100)
    private String section;

    @Column(length = 255)
    private String organization;

    @Column(name = "org_slug", length = 100)
    private String orgSlug;

    @Column(length = 10)
    private String postcode;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(length = 50)
    private String timezone;

    @Column(length = 20)
    private String status;

    @Column(name = "events_count", columnDefinition = "SMALLINT UNSIGNED")
    private Short eventsCount;

    @Column(length = 30)
    private String surface;

    @Column(length = 500)
    private String url;

    @Column(name = "director_name", length = 200)
    private String directorName;

    @Column(name = "director_email", length = 255)
    private String directorEmail;

    @Column(name = "director_phone", length = 30)
    private String directorPhone;

    @Column(name = "total_draws", columnDefinition = "SMALLINT UNSIGNED")
    private Short totalDraws;

    @Column(name = "detail_scraped_at")
    private LocalDateTime detailScrapedAt;

    @Column(name = "detail_scrape_status", length = 20)
    private String detailScrapeStatus;

    @OneToMany(mappedBy = "tournament", fetch = FetchType.LAZY)
    private List<TournamentEvent> events;

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

    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Short getEventsCount() { return eventsCount; }
    public void setEventsCount(Short eventsCount) { this.eventsCount = eventsCount; }

    public String getSurface() { return surface; }
    public void setSurface(String surface) { this.surface = surface; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDirectorName() { return directorName; }
    public void setDirectorName(String directorName) { this.directorName = directorName; }

    public String getDirectorEmail() { return directorEmail; }
    public void setDirectorEmail(String directorEmail) { this.directorEmail = directorEmail; }

    public String getDirectorPhone() { return directorPhone; }
    public void setDirectorPhone(String directorPhone) { this.directorPhone = directorPhone; }

    public Short getTotalDraws() { return totalDraws; }
    public void setTotalDraws(Short totalDraws) { this.totalDraws = totalDraws; }

    public LocalDateTime getDetailScrapedAt() { return detailScrapedAt; }
    public void setDetailScrapedAt(LocalDateTime detailScrapedAt) { this.detailScrapedAt = detailScrapedAt; }

    public String getDetailScrapeStatus() { return detailScrapeStatus; }
    public void setDetailScrapeStatus(String detailScrapeStatus) { this.detailScrapeStatus = detailScrapeStatus; }

    public List<TournamentEvent> getEvents() { return events; }
    public void setEvents(List<TournamentEvent> events) { this.events = events; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
