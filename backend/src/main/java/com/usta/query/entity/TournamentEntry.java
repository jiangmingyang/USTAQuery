package com.usta.query.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_entries")
public class TournamentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "event_id", nullable = false, length = 50)
    private String eventId;

    @Column(name = "participant_id", nullable = false, length = 50)
    private String participantId;

    @Column(name = "player_uaid", length = 20)
    private String playerUaid;

    @Column(name = "player_name", length = 200)
    private String playerName;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 10)
    private String gender;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(name = "event_type", length = 20)
    private String eventType;

    @Column(name = "entry_stage", length = 30)
    private String entryStage;

    @Column(name = "entry_status", length = 30)
    private String entryStatus;

    @Column(name = "entry_position")
    private Integer entryPosition;

    @Column(name = "status_detail", length = 100)
    private String statusDetail;

    @Column(name = "draw_id", length = 50)
    private String drawId;

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

    public Tournament getTournament() { return tournament; }
    public void setTournament(Tournament tournament) { this.tournament = tournament; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getPlayerUaid() { return playerUaid; }
    public void setPlayerUaid(String playerUaid) { this.playerUaid = playerUaid; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEntryStage() { return entryStage; }
    public void setEntryStage(String entryStage) { this.entryStage = entryStage; }

    public String getEntryStatus() { return entryStatus; }
    public void setEntryStatus(String entryStatus) { this.entryStatus = entryStatus; }

    public Integer getEntryPosition() { return entryPosition; }
    public void setEntryPosition(Integer entryPosition) { this.entryPosition = entryPosition; }

    public String getStatusDetail() { return statusDetail; }
    public void setStatusDetail(String statusDetail) { this.statusDetail = statusDetail; }

    public String getDrawId() { return drawId; }
    public void setDrawId(String drawId) { this.drawId = drawId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
