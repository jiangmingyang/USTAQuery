package com.usta.query.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PlayerDetailDto {
    private String uaid;
    private String firstName;
    private String lastName;
    private String gender;
    private String city;
    private String state;
    private String section;
    private String sectionCode;
    private String district;
    private String districtCode;
    private String nationality;
    private String itfTennisId;
    private String ageCategory;
    private Boolean wheelchair;
    private String ratingNtrp;

    // WTN Singles
    private BigDecimal wtnSingles;
    private Integer wtnSinglesConfidence;
    private LocalDate wtnSinglesLastPlayed;
    private BigDecimal wtnSinglesGameZoneUpper;
    private BigDecimal wtnSinglesGameZoneLower;

    // WTN Doubles
    private BigDecimal wtnDoubles;
    private Integer wtnDoublesConfidence;
    private LocalDate wtnDoublesLastPlayed;
    private BigDecimal wtnDoublesGameZoneUpper;
    private BigDecimal wtnDoublesGameZoneLower;

    // UTR
    private String utrId;
    private BigDecimal utrSingles;
    private BigDecimal utrDoubles;

    private String profileImageUrl;
    private String membershipType;
    private LocalDate membershipExpiry;

    public String getUaid() { return uaid; }
    public void setUaid(String uaid) { this.uaid = uaid; }
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
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDistrictCode() { return districtCode; }
    public void setDistrictCode(String districtCode) { this.districtCode = districtCode; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getItfTennisId() { return itfTennisId; }
    public void setItfTennisId(String itfTennisId) { this.itfTennisId = itfTennisId; }
    public String getAgeCategory() { return ageCategory; }
    public void setAgeCategory(String ageCategory) { this.ageCategory = ageCategory; }
    public Boolean getWheelchair() { return wheelchair; }
    public void setWheelchair(Boolean wheelchair) { this.wheelchair = wheelchair; }
    public String getRatingNtrp() { return ratingNtrp; }
    public void setRatingNtrp(String ratingNtrp) { this.ratingNtrp = ratingNtrp; }

    public BigDecimal getWtnSingles() { return wtnSingles; }
    public void setWtnSingles(BigDecimal wtnSingles) { this.wtnSingles = wtnSingles; }
    public Integer getWtnSinglesConfidence() { return wtnSinglesConfidence; }
    public void setWtnSinglesConfidence(Integer v) { this.wtnSinglesConfidence = v; }
    public LocalDate getWtnSinglesLastPlayed() { return wtnSinglesLastPlayed; }
    public void setWtnSinglesLastPlayed(LocalDate v) { this.wtnSinglesLastPlayed = v; }
    public BigDecimal getWtnSinglesGameZoneUpper() { return wtnSinglesGameZoneUpper; }
    public void setWtnSinglesGameZoneUpper(BigDecimal v) { this.wtnSinglesGameZoneUpper = v; }
    public BigDecimal getWtnSinglesGameZoneLower() { return wtnSinglesGameZoneLower; }
    public void setWtnSinglesGameZoneLower(BigDecimal v) { this.wtnSinglesGameZoneLower = v; }

    public BigDecimal getWtnDoubles() { return wtnDoubles; }
    public void setWtnDoubles(BigDecimal wtnDoubles) { this.wtnDoubles = wtnDoubles; }
    public Integer getWtnDoublesConfidence() { return wtnDoublesConfidence; }
    public void setWtnDoublesConfidence(Integer v) { this.wtnDoublesConfidence = v; }
    public LocalDate getWtnDoublesLastPlayed() { return wtnDoublesLastPlayed; }
    public void setWtnDoublesLastPlayed(LocalDate v) { this.wtnDoublesLastPlayed = v; }
    public BigDecimal getWtnDoublesGameZoneUpper() { return wtnDoublesGameZoneUpper; }
    public void setWtnDoublesGameZoneUpper(BigDecimal v) { this.wtnDoublesGameZoneUpper = v; }
    public BigDecimal getWtnDoublesGameZoneLower() { return wtnDoublesGameZoneLower; }
    public void setWtnDoublesGameZoneLower(BigDecimal v) { this.wtnDoublesGameZoneLower = v; }

    public String getUtrId() { return utrId; }
    public void setUtrId(String utrId) { this.utrId = utrId; }
    public BigDecimal getUtrSingles() { return utrSingles; }
    public void setUtrSingles(BigDecimal utrSingles) { this.utrSingles = utrSingles; }
    public BigDecimal getUtrDoubles() { return utrDoubles; }
    public void setUtrDoubles(BigDecimal utrDoubles) { this.utrDoubles = utrDoubles; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public String getMembershipType() { return membershipType; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }
    public LocalDate getMembershipExpiry() { return membershipExpiry; }
    public void setMembershipExpiry(LocalDate membershipExpiry) { this.membershipExpiry = membershipExpiry; }
}
