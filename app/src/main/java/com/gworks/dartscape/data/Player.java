/*
 * Copyright (C) 2023 G Works
 */

package com.gworks.dartscape.data;

/**
 * represent a dart player, containing data like their name and score and wins.
 */

public class Player {

    /** the name of the player */
    private String mName         = "";

    /** the name of the player's teammate */
    private String mTeammateName = "";

    /** is the player active? */
    private boolean mIsActive    = true;

    /** the current score of the player */
    private int mScore           = 0;

    /** the current wins of the player */
    private int mWins            = 0;

    /** the current set wins of the player */
    private int mSetWins         = 0;

    /** the bot difficulty level, if > 0 player is a bot */
    private float mBotDifficulty   = 0;

    /** the player's killer number */
    private int mKillerNumber    = 0;

    /** does this player have killer status? */
    private boolean mIsKiller    = false;

    /** default constructor */
    public Player() {

    }

    /** @return the name of the player */
    public String getName() {
        return mName;
    }

    /** set the name of the player */
    public void setName(String name) {
        if(name.equals(mName)) return;
        mName = name;
        setWins(0); // player changed wins are reset
    }

    /** @return the name of the player's teammate */
    public String getTeammateName() {
        return mTeammateName;
    }

    /** set the name of the player's teammate */
    public void setTeammateName(String name) {
        if(name.equals(mTeammateName)) return;
        mTeammateName = name;
        setWins(0); // player changed wins are reset
    }

    /** @return the name of the player's team */
    public String getTeamName() {
        if(!isTeam()) return getName();
        return getName() + "-" + getTeammateName();
    }

    /** @return true if the player is active */
    public boolean isActive() {
        return mIsActive;
    }

    /** set  the player's active state */
    public void setActive(boolean active) {
        mIsActive = active;
    }

    /** @return the player's current score */
    public int getScore() {
        return mScore;
    }

    /** set the player's current score */
    public void setScore(int score) {
        mScore = score;
    }
    /** add score to the player's current score */
    public void addScore(int score) {
        setScore(getScore() + score);
    }

    /** @return the number of games or legs player has won */
    public int getWins() {
        return mWins;
    }

    /** set the number of games or legs player has won */
    public void setWins(int wins) {
        mWins = wins;
    }

    /** add wins tp the number of games or legs player has won */
    public void addWins(int wins) {
        setWins(getWins() + wins);
    }

    /** @return the number of sets player has won */
    public int getSetWins() {
        return mSetWins;
    }

    /** set the number of sets player has won */
    public void setSetWins(int wins) {
        mSetWins = wins;
    }

    /** add wins to the number of sets player has won */
    public void addSetWins(int wins) {
        setSetWins(getSetWins() + wins);
    }

    /** @return the bot difficulty of player, if > 0 player is a bot */
    public float botDiff() {
        return mBotDifficulty;
    }

    /** set the bot difficulty of player, if > 0 player is a bot */
    public void setBotDiff(float difficulty) {
        mBotDifficulty = difficulty;
    }

    /** @return true if the player has a teammate */
    public boolean isTeam() {
        return !getTeammateName().isEmpty();
    }

    /** @return true if the player is a bot */
    public boolean isBot() {
        return botDiff() > 0;
    }

    /** @return the killer number of the player */
    public int getKillerNumber() {
        return mKillerNumber;
    }

    /** set the killer number of the player */
    public void setKillerNumber(int number) {
        mKillerNumber = number;
    }

    /** @return true if the player has killer status */
    public boolean isKiller() {
        return mIsKiller;
    }

    /** set the player's killer status */
    public void setKiller(boolean killer) {
        mIsKiller = killer;
    }

}
