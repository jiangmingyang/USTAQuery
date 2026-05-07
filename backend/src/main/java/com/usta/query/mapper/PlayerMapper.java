package com.usta.query.mapper;

import com.usta.query.dto.response.*;
import com.usta.query.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PlayerMapper {

    public PlayerSummaryDto toSummary(Player player) {
        PlayerSummaryDto dto = new PlayerSummaryDto();
        dto.setUaid(player.getUaid());
        dto.setFirstName(player.getFirstName());
        dto.setLastName(player.getLastName());
        dto.setGender(player.getGender());
        dto.setCity(player.getCity());
        dto.setState(player.getState());
        dto.setSection(player.getSection());
        dto.setDistrict(player.getDistrict());
        dto.setRatingNtrp(player.getRatingNtrp());
        dto.setWtnSingles(player.getWtnSingles());
        dto.setWtnDoubles(player.getWtnDoubles());
        dto.setUtrSingles(player.getUtrSingles());
        return dto;
    }

    public PlayerDetailDto toDetail(Player player) {
        PlayerDetailDto dto = new PlayerDetailDto();
        dto.setUaid(player.getUaid());
        dto.setFirstName(player.getFirstName());
        dto.setLastName(player.getLastName());
        dto.setGender(player.getGender());
        dto.setCity(player.getCity());
        dto.setState(player.getState());
        dto.setSection(player.getSection());
        dto.setSectionCode(player.getSectionCode());
        dto.setDistrict(player.getDistrict());
        dto.setDistrictCode(player.getDistrictCode());
        dto.setNationality(player.getNationality());
        dto.setItfTennisId(player.getItfTennisId());
        dto.setAgeCategory(player.getAgeCategory());
        dto.setWheelchair(player.getWheelchair());
        dto.setRatingNtrp(player.getRatingNtrp());

        dto.setWtnSingles(player.getWtnSingles());
        dto.setWtnSinglesConfidence(player.getWtnSinglesConfidence());
        dto.setWtnSinglesLastPlayed(player.getWtnSinglesLastPlayed());
        dto.setWtnSinglesGameZoneUpper(player.getWtnSinglesGameZoneUpper());
        dto.setWtnSinglesGameZoneLower(player.getWtnSinglesGameZoneLower());

        dto.setWtnDoubles(player.getWtnDoubles());
        dto.setWtnDoublesConfidence(player.getWtnDoublesConfidence());
        dto.setWtnDoublesLastPlayed(player.getWtnDoublesLastPlayed());
        dto.setWtnDoublesGameZoneUpper(player.getWtnDoublesGameZoneUpper());
        dto.setWtnDoublesGameZoneLower(player.getWtnDoublesGameZoneLower());

        dto.setUtrId(player.getUtrId());
        dto.setUtrSingles(player.getUtrSingles());
        dto.setUtrDoubles(player.getUtrDoubles());

        dto.setProfileImageUrl(player.getProfileImageUrl());
        dto.setMembershipType(player.getMembershipType());
        dto.setMembershipExpiry(player.getMembershipExpiry());
        return dto;
    }

    public <T> PagedResponse<T> toPagedResponse(Page<?> page, List<T> content) {
        return new PagedResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
