package com.usta.query.mapper;

import com.usta.query.dto.response.RankingDto;
import com.usta.query.entity.Ranking;
import org.springframework.stereotype.Component;

@Component
public class RankingMapper {

    public RankingDto toDto(Ranking r) {
        RankingDto dto = new RankingDto();
        dto.setId(r.getId());
        if (r.getPlayer() != null) {
            dto.setPlayerUaid(r.getPlayer().getUaid());
            dto.setPlayerFirstName(r.getPlayer().getFirstName());
            dto.setPlayerLastName(r.getPlayer().getLastName());
        }
        dto.setCatalogId(r.getCatalogId());
        dto.setDisplayLabel(r.getDisplayLabel());
        dto.setPlayerType(r.getPlayerType());
        dto.setAgeRestriction(r.getAgeRestriction());
        dto.setAgeRestrictionModifier(r.getAgeRestrictionModifier());
        dto.setRankListGender(r.getRankListGender());
        dto.setListType(r.getListType());
        dto.setMatchFormat(r.getMatchFormat());
        dto.setMatchFormatType(r.getMatchFormatType());
        dto.setFamilyCategory(r.getFamilyCategory());
        dto.setNationalRank(r.getNationalRank());
        dto.setSectionRank(r.getSectionRank());
        dto.setDistrictRank(r.getDistrictRank());
        dto.setPoints(r.getPoints());
        dto.setSinglesPoints(r.getSinglesPoints());
        dto.setDoublesPoints(r.getDoublesPoints());
        dto.setBonusPoints(r.getBonusPoints());
        dto.setWins(r.getWins());
        dto.setLosses(r.getLosses());
        dto.setTrendDirection(r.getTrendDirection());
        dto.setPublishDate(r.getPublishDate());
        dto.setSection(r.getSection());
        dto.setDistrict(r.getDistrict());
        dto.setState(r.getState());
        return dto;
    }
}
