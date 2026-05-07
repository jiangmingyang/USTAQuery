package com.usta.query.mapper;

import com.usta.query.dto.response.PlayerTournamentEntryDto;
import com.usta.query.dto.response.TournamentDto;
import com.usta.query.dto.response.TournamentEntryDto;
import com.usta.query.dto.response.TournamentEventDto;
import com.usta.query.entity.Tournament;
import com.usta.query.entity.TournamentEntry;
import com.usta.query.entity.TournamentEvent;
import org.springframework.stereotype.Component;

@Component
public class TournamentMapper {

    public TournamentDto toDto(Tournament t) {
        TournamentDto dto = new TournamentDto();
        dto.setId(t.getId());
        dto.setTournamentId(t.getTournamentId());
        dto.setCode(t.getCode());
        dto.setName(t.getName());
        dto.setLevel(t.getLevel());
        dto.setCategory(t.getCategory());
        dto.setStartDate(t.getStartDate());
        dto.setEndDate(t.getEndDate());
        dto.setEntryDeadline(t.getEntryDeadline());
        dto.setAcceptingEntries(t.isAcceptingEntries());
        dto.setVenueName(t.getVenueName());
        dto.setCity(t.getCity());
        dto.setState(t.getState());
        dto.setSection(t.getSection());
        dto.setOrganization(t.getOrganization());
        dto.setOrgSlug(t.getOrgSlug());
        dto.setStatus(t.getStatus());
        dto.setEventsCount(t.getEventsCount() != null ? t.getEventsCount().intValue() : null);
        dto.setSurface(t.getSurface());
        dto.setUrl(t.getUrl());
        dto.setDirectorName(t.getDirectorName());
        dto.setTotalDraws(t.getTotalDraws() != null ? t.getTotalDraws().intValue() : null);
        return dto;
    }

    public TournamentEventDto toEventDto(TournamentEvent e) {
        TournamentEventDto dto = new TournamentEventDto();
        dto.setEventId(e.getEventId());
        dto.setGender(e.getGender());
        dto.setEventType(e.getEventType());
        dto.setAgeCategory(e.getAgeCategory());
        dto.setMinAge(e.getMinAge());
        dto.setMaxAge(e.getMaxAge());
        dto.setSurface(e.getSurface());
        dto.setCourtLocation(e.getCourtLocation());
        dto.setEntryFee(e.getEntryFee());
        dto.setCurrency(e.getCurrency());
        dto.setLevel(e.getLevel());
        dto.setBallColor(e.getBallColor());
        return dto;
    }

    public TournamentEntryDto toEntryDto(TournamentEntry e) {
        TournamentEntryDto dto = new TournamentEntryDto();
        dto.setEventId(e.getEventId());
        dto.setParticipantId(e.getParticipantId());
        dto.setPlayerUaid(e.getPlayerUaid());
        dto.setPlayerName(e.getPlayerName());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setGender(e.getGender());
        dto.setCity(e.getCity());
        dto.setState(e.getState());
        dto.setEventType(e.getEventType());
        dto.setEntryStage(e.getEntryStage());
        dto.setEntryStatus(e.getEntryStatus());
        dto.setEntryPosition(e.getEntryPosition());
        dto.setStatusDetail(e.getStatusDetail());
        dto.setDrawId(e.getDrawId());
        return dto;
    }

    public PlayerTournamentEntryDto toPlayerEntryDto(TournamentEntry e) {
        PlayerTournamentEntryDto dto = new PlayerTournamentEntryDto();
        Tournament t = e.getTournament();
        dto.setTournamentInternalId(t.getId());
        dto.setTournamentName(t.getName());
        dto.setTournamentLevel(t.getLevel());
        dto.setTournamentCategory(t.getCategory());
        dto.setStartDate(t.getStartDate());
        dto.setEndDate(t.getEndDate());
        dto.setCity(t.getCity());
        dto.setState(t.getState());
        dto.setSection(t.getSection());
        dto.setEventId(e.getEventId());
        dto.setEventType(e.getEventType());
        dto.setEntryStatus(e.getEntryStatus());
        dto.setEntryStage(e.getEntryStage());
        dto.setEntryPosition(e.getEntryPosition());
        return dto;
    }
}
