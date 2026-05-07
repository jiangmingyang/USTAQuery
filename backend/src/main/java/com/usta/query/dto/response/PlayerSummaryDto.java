package com.usta.query.dto.response;

import java.math.BigDecimal;

public class PlayerSummaryDto {
    private String uaid;
    private String firstName;
    private String lastName;
    private String gender;
    private String city;
    private String state;
    private String section;
    private String district;
    private String ratingNtrp;
    private BigDecimal wtnSingles;
    private BigDecimal wtnDoubles;
    private BigDecimal utrSingles;

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
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getRatingNtrp() { return ratingNtrp; }
    public void setRatingNtrp(String ratingNtrp) { this.ratingNtrp = ratingNtrp; }
    public BigDecimal getWtnSingles() { return wtnSingles; }
    public void setWtnSingles(BigDecimal wtnSingles) { this.wtnSingles = wtnSingles; }
    public BigDecimal getWtnDoubles() { return wtnDoubles; }
    public void setWtnDoubles(BigDecimal wtnDoubles) { this.wtnDoubles = wtnDoubles; }
    public BigDecimal getUtrSingles() { return utrSingles; }
    public void setUtrSingles(BigDecimal utrSingles) { this.utrSingles = utrSingles; }
}
