package com.usta.query.dto.response;

public class TournamentEntryDto {

    private String eventId;
    private String participantId;
    private String playerUaid;
    private String playerName;
    private String firstName;
    private String lastName;
    private String gender;
    private String city;
    private String state;
    private String eventType;
    private String entryStage;
    private String entryStatus;
    private Integer entryPosition;
    private String statusDetail;
    private String drawId;

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

    private Integer rankingPoints;

    public Integer getRankingPoints() { return rankingPoints; }
    public void setRankingPoints(Integer rankingPoints) { this.rankingPoints = rankingPoints; }
}
