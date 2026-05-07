package com.usta.query.dto.response;

public class SetScoreDto {
    private int setNumber;
    private int playerGames;
    private int opponentGames;
    private Integer tiebreakPlayer;
    private Integer tiebreakOpponent;

    public int getSetNumber() { return setNumber; }
    public void setSetNumber(int setNumber) { this.setNumber = setNumber; }
    public int getPlayerGames() { return playerGames; }
    public void setPlayerGames(int playerGames) { this.playerGames = playerGames; }
    public int getOpponentGames() { return opponentGames; }
    public void setOpponentGames(int opponentGames) { this.opponentGames = opponentGames; }
    public Integer getTiebreakPlayer() { return tiebreakPlayer; }
    public void setTiebreakPlayer(Integer tiebreakPlayer) { this.tiebreakPlayer = tiebreakPlayer; }
    public Integer getTiebreakOpponent() { return tiebreakOpponent; }
    public void setTiebreakOpponent(Integer tiebreakOpponent) { this.tiebreakOpponent = tiebreakOpponent; }
}
