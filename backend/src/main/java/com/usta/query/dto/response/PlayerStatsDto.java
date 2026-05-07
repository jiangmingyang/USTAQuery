package com.usta.query.dto.response;

public class PlayerStatsDto {
    private String uaid;
    private int totalWins;
    private int totalLosses;
    private double winPercentage;
    private int tournamentsPlayed;

    public String getUaid() { return uaid; }
    public void setUaid(String uaid) { this.uaid = uaid; }
    public int getTotalWins() { return totalWins; }
    public void setTotalWins(int totalWins) { this.totalWins = totalWins; }
    public int getTotalLosses() { return totalLosses; }
    public void setTotalLosses(int totalLosses) { this.totalLosses = totalLosses; }
    public double getWinPercentage() { return winPercentage; }
    public void setWinPercentage(double winPercentage) { this.winPercentage = winPercentage; }
    public int getTournamentsPlayed() { return tournamentsPlayed; }
    public void setTournamentsPlayed(int tournamentsPlayed) { this.tournamentsPlayed = tournamentsPlayed; }
}
