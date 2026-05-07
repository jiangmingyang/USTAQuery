package com.usta.query.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_events")
public class TournamentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 50)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(length = 20)
    private String gender;

    @Column(name = "event_type", length = 20)
    private String eventType;

    @Column(name = "age_category", length = 20)
    private String ageCategory;

    @Column(name = "min_age", columnDefinition = "TINYINT UNSIGNED")
    private Short minAge;

    @Column(name = "max_age", columnDefinition = "TINYINT UNSIGNED")
    private Short maxAge;

    @Column(length = 30)
    private String surface;

    @Column(name = "court_location", length = 20)
    private String courtLocation;

    @Column(name = "entry_fee", precision = 8, scale = 2)
    private BigDecimal entryFee;

    @Column(length = 3)
    private String currency;

    @Column(length = 50)
    private String level;

    @Column(name = "ball_color", length = 20)
    private String ballColor;

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

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
