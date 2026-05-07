package com.usta.query.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String uaid;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 1)
    private String gender;

    @Column(length = 100)
    private String city;

    @Column(length = 2)
    private String state;

    @Column(length = 50)
    private String section;

    @Column(name = "section_code", length = 10)
    private String sectionCode;

    @Column(length = 50)
    private String district;

    @Column(name = "district_code", length = 10)
    private String districtCode;

    @Column(length = 10)
    private String nationality;

    @Column(name = "itf_tennis_id", length = 30)
    private String itfTennisId;

    @Column(name = "age_category", length = 20)
    private String ageCategory;

    @Column(nullable = false)
    private Boolean wheelchair = false;

    // WTN Singles
    @Column(name = "wtn_singles", precision = 5, scale = 2)
    private BigDecimal wtnSingles;

    @Column(name = "wtn_singles_confidence")
    private Integer wtnSinglesConfidence;

    @Column(name = "wtn_singles_last_played")
    private LocalDate wtnSinglesLastPlayed;

    @Column(name = "wtn_singles_game_zone_upper", precision = 5, scale = 2)
    private BigDecimal wtnSinglesGameZoneUpper;

    @Column(name = "wtn_singles_game_zone_lower", precision = 5, scale = 2)
    private BigDecimal wtnSinglesGameZoneLower;

    // WTN Doubles
    @Column(name = "wtn_doubles", precision = 5, scale = 2)
    private BigDecimal wtnDoubles;

    @Column(name = "wtn_doubles_confidence")
    private Integer wtnDoublesConfidence;

    @Column(name = "wtn_doubles_last_played")
    private LocalDate wtnDoublesLastPlayed;

    @Column(name = "wtn_doubles_game_zone_upper", precision = 5, scale = 2)
    private BigDecimal wtnDoublesGameZoneUpper;

    @Column(name = "wtn_doubles_game_zone_lower", precision = 5, scale = 2)
    private BigDecimal wtnDoublesGameZoneLower;

    // UTR
    @Column(name = "utr_id", length = 20)
    private String utrId;

    @Column(name = "utr_singles", precision = 4, scale = 2)
    private BigDecimal utrSingles;

    @Column(name = "utr_doubles", precision = 4, scale = 2)
    private BigDecimal utrDoubles;

    @Column(name = "rating_ntrp", length = 4)
    private String ratingNtrp;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "membership_type", length = 30)
    private String membershipType;

    @Column(name = "membership_expiry")
    private LocalDate membershipExpiry;

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

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public BigDecimal getWtnSingles() { return wtnSingles; }
    public void setWtnSingles(BigDecimal wtnSingles) { this.wtnSingles = wtnSingles; }

    public Integer getWtnSinglesConfidence() { return wtnSinglesConfidence; }
    public void setWtnSinglesConfidence(Integer wtnSinglesConfidence) { this.wtnSinglesConfidence = wtnSinglesConfidence; }

    public LocalDate getWtnSinglesLastPlayed() { return wtnSinglesLastPlayed; }
    public void setWtnSinglesLastPlayed(LocalDate wtnSinglesLastPlayed) { this.wtnSinglesLastPlayed = wtnSinglesLastPlayed; }

    public BigDecimal getWtnSinglesGameZoneUpper() { return wtnSinglesGameZoneUpper; }
    public void setWtnSinglesGameZoneUpper(BigDecimal v) { this.wtnSinglesGameZoneUpper = v; }

    public BigDecimal getWtnSinglesGameZoneLower() { return wtnSinglesGameZoneLower; }
    public void setWtnSinglesGameZoneLower(BigDecimal v) { this.wtnSinglesGameZoneLower = v; }

    public BigDecimal getWtnDoubles() { return wtnDoubles; }
    public void setWtnDoubles(BigDecimal wtnDoubles) { this.wtnDoubles = wtnDoubles; }

    public Integer getWtnDoublesConfidence() { return wtnDoublesConfidence; }
    public void setWtnDoublesConfidence(Integer wtnDoublesConfidence) { this.wtnDoublesConfidence = wtnDoublesConfidence; }

    public LocalDate getWtnDoublesLastPlayed() { return wtnDoublesLastPlayed; }
    public void setWtnDoublesLastPlayed(LocalDate wtnDoublesLastPlayed) { this.wtnDoublesLastPlayed = wtnDoublesLastPlayed; }

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

    public String getRatingNtrp() { return ratingNtrp; }
    public void setRatingNtrp(String ratingNtrp) { this.ratingNtrp = ratingNtrp; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getMembershipType() { return membershipType; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }

    public LocalDate getMembershipExpiry() { return membershipExpiry; }
    public void setMembershipExpiry(LocalDate membershipExpiry) { this.membershipExpiry = membershipExpiry; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
