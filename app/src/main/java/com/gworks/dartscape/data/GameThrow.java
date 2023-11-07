/*
 * Copyright (C) 2023 G Works
 */

package com.gworks.dartscape.data;

/**
 * contains data for a single throw in a game including the player index, the
 * number hit if any, the multi hit and the score made.
 */

public class GameThrow {
    /** the index of the player who threw */
    private long mTime;

    /** the game mode of this throw */
    private GameFlagSet mGameMode;
    /** the index of the player who threw */
    private boolean mWin;

    /** the index of the player who threw */
    private int mPlayerIndex;

    /** the number aiming at or hit */
    private int mNumber; // 25 = bull, -1 = out

    /** the multi aiming at or hit */
    private int mMulti;

    /** score accumulated from throw */
    private int mScore;

    /** default constructor */
    public GameThrow(int player, int number, int multi, int score) {
        init(0, new GameFlagSet(), false, player, number, multi, score);
    }

    /** default constructor */
    public GameThrow(long time, GameFlagSet gameMode, boolean win, int player, int number, int multi, int score) {
        init(time, gameMode, win, player, number, multi, score);
    }

    /** construct from another game throw */
    public GameThrow(GameThrow gameThrow) {
        init(gameThrow.getTime(), gameThrow.getGameMode(), gameThrow.isWin(),
                gameThrow.getPlayerIndex(), gameThrow.getNumber(),
                gameThrow.getMulti(), gameThrow.getScore());
    }

    /** initialize game throw with values */
    private void init(long time, GameFlagSet gameMode, boolean win,
                      int player, int number, int multi, int score) {
        mTime        = time;
        mGameMode    = new GameFlagSet(gameMode);
        mWin         = win;
        mPlayerIndex = player;
        mNumber      = number;
        mMulti       = multi;
        mScore       = score;

    }

    /** @return the game mode of the throw */
    public GameFlagSet getGameMode() {
        return new GameFlagSet(mGameMode);
    }

    /** get the game mode of the throw */
    public void setGameMode(GameFlagSet gameMode) {
        mGameMode = new GameFlagSet(gameMode);
    }

    /** @return the index of the player who threw */
    public long getTime() {
        return mTime;
    }

    /** @return the number aimed at or hit */
    public void setTime(long time) {
        mTime = time;
    }

    /** @return the index of the player who threw */
    public boolean isWin() {
        return mWin;
    }

    /** @return the number aimed at or hit */
    public void setWin(boolean win) {
        mWin = win;
    }


    /** @return true if the throw did not hit anything */
    public boolean isOut() { return getNumber() == 0 || getMulti() == 0; }

    /** @return the index of the player who threw */
    public int getPlayerIndex() {
        return mPlayerIndex;
    }

    /** @return the number aimed at or hit */
    public int getNumber() {
        return mNumber;
    }

    /** @return the multiplier aimed at or hit */
    public int getMulti() {
        return mMulti;
    }

    /** set the multiplier aimed at or hit */
    public void setMulti(int multi) { mMulti = multi; }

    /** @return the score accumulated by the throw */
    public int getScore() {
        return mScore;
    }

    /** @return the positive value of  the score accumulated by the throw */
    public int getScoreAbs() {
        return Math.abs(mScore);
    }

    /** set the score accumulated by the throw */
    public void setScore(int score) { mScore = score; }

}
