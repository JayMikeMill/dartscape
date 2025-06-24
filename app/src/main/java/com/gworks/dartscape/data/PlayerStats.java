/*
 * Copyright (C) 2023 G Works
 */

package com.gworks.dartscape.data;

import java.util.Locale;

import com.gworks.dartscape.data.Stats.StatId;

import static com.gworks.dartscape.data.GameFlags.GameFlag.*;
import static com.gworks.dartscape.data.Stats.StatId.*;


/**
 * calculates player stats from throw list
 */

public class PlayerStats {
    
    GameThrowList mThrows;

    private String mName = "";

    private int mGames              = 0;
    private int mWins               = 0;
    private int mTotalThrows        = 0;
    private int mHits               = 0;

    private int mMarks              = 0;
    private final GameThrow[] mHighestMarksRound  = {
            new GameThrow(-1, 0, 0, 0),
            new GameThrow(-1, 0, 0, 0),
            new GameThrow(-1, 0, 0, 0)
    };

    private int mTotalScore         = 0;
    private int mHighScoreRound     = 0;
    private int mHighScoreGame      = 0;
    private int mHighScoreCrick     = 0;
    private int mHighScoreShang     = 0;
    private int mHighScoreBball     = 0;

    private int mBestOut            = 0;

    private final GameThrow[] mHighestScoreRound = {
            new GameThrow(-1, 0, 0, 0),
            new GameThrow(-1, 0, 0, 0),
            new GameThrow(-1, 0, 0, 0)
    };

    private final int[] mScorePlus = {0, 0, 0, 0}; // 60, 80, 100, ect..

    private int mShanghais          = 0;
    private int mKills              = 0;

    private int mDubBulls           = 0;
    private int mSingleBulls        = 0;

    private int mTriples            = 0;
    private int mDoubles            = 0;
    private int mSingles            = 0;

    private int[] mNumberHits;

    // Baseball
    private int mBaseballInnings    = 0;
    private int mBaseballBestInning = 0;
    private int mBaseballRuns       = 0;
    private int mBaseballBases      = 0;
    private int mGrandSlams         = 0;
    private int mHomeRuns           = 0;

    private int mBestNumber         = 0;
    private int mWorstNumber        = 0;

    public PlayerStats(GameThrowList gameThrows) {
        initFromThrowlist("", gameThrows);
    }
    public PlayerStats(String name, GameThrowList gameThrows) {
        initFromThrowlist(name,  gameThrows);
    }

    public PlayerStats(String name) {
    }

    private void initFromThrowlist(String name, GameThrowList gameThrows) {
        mThrows = gameThrows;
        mName = name;

        mNumberHits = new int[21];

        getStatsFromThrowList();
    }

    private void getStatsFromThrowList() {
        if(mThrows.isEmpty()) return;

        int gameScore = 0, gameThrows = 0;
        int runsLastInning = 0;
        for (int i = 0; i < mThrows.size(); i++) {
            // game mode of current throw
            GameThrow gameThrow = mThrows.get(i);

            long time = gameThrow.getTime();
            GameFlagSet gameMode = gameThrow.getGameMode();

            int number = gameThrow.getNumber();
            int multi  = gameThrow.getMulti();
            int score  = gameThrow.getScoreAbs();


            gameThrows++;
            mTotalThrows++;

            gameScore += score;
            mTotalScore += score;

            if(multi > 0) mHits++;

            mMarks += multi;

            int roundThrows = gameThrows % 3;

            // is last throw or game time is different from previous throw
            boolean isEndOfGame = i == mThrows.size() - 1 || time != mThrows.get(i + 1).getTime();


            // count bullseyes
            if(number == 25) {
                mDubBulls += gameThrow.getMulti() == 2 ? 1 : 0;
                mSingleBulls += gameThrow.getMulti() == 1 ? 1 : 0;
            }

            // count multis
            mTriples += gameThrow.getMulti() == 3 ? 1 : 0;
            mDoubles += gameThrow.getMulti() == 2 ? 1 : 0;
            mSingles += gameThrow.getMulti() == 1 ? 1 : 0;

            if(number > 0 && (number <= 20 || number == 25))
                mNumberHits[(number == 25) ? 20 : number - 1]++;


            if(!gameMode.get(BASEBALL) && !gameMode.get(KILLER)) {
                if (gameThrows > 2 && roundThrows == 0) {
                    getRoundStats(new GameThrow[] {
                    mThrows.get(i - 2), mThrows.get(i - 1), gameThrow } );
                } else if(roundThrows > 0 && isEndOfGame) {
                    GameThrow newThrow = new GameThrow(time, gameMode, gameThrow.isWin(), 0, 0, 0, 0);
                    getRoundStats(new GameThrow[] {
                            gameThrow, roundThrows == 2 ? mThrows.get(i - 1) : newThrow, newThrow } );
                }
            }

            if(gameMode.get(X01) && isEndOfGame && gameThrow.isWin()) {
                int out = score +
                        ((roundThrows == 2 || roundThrows == 0) ? mThrows.get(i - 1).getScoreAbs() : 0) +
                        ((roundThrows == 0) ? mThrows.get(i - 2).getScoreAbs() : 0);
                if(out > mBestOut) mBestOut = out;
            } else if(gameMode.get(BASEBALL)) {
                GameThrow nextThrow = (i < mThrows.size() - 2) ? mThrows.get(i + 1) : null;

                if(isEndOfGame || (nextThrow != null && nextThrow.getNumber() != number)) {
                    mBaseballInnings++;
                    if(runsLastInning > mBaseballBestInning)
                        mBaseballBestInning = runsLastInning;

                    runsLastInning = 0;
                }

                runsLastInning += score;

                if(score == 4) mGrandSlams++;
                if(multi == 4) mHomeRuns++;
                mBaseballBases += multi;
                mBaseballRuns += score;

            } else if(gameMode.get(KILLER)) {
                if(multi == 1) mKills++;
                continue;
            }

            if (isEndOfGame) {
                getHighScoreStats(gameMode.getGameModeFlag(), gameScore);

                // last throw no more stats
                mGames++;
                mWins += gameThrow.isWin() ? 1 : 0;

                // it is a new game, reset throw count.
                gameThrows = 0;
                gameScore = 0;
            }
        }

        calculateBestNumber();
        calculateWorstNumber();
    }

    private void getHighScoreStats(GameFlags.GameFlag gameMode, int gameScore) {
        if(gameMode.equals(CRICKET) && gameScore > mHighScoreCrick)
            mHighScoreCrick = gameScore;
        else if(gameMode.equals(SHANGHAI) && gameScore > mHighScoreShang)
            mHighScoreShang = gameScore;
        else if(gameMode.equals(BASEBALL) && gameScore > mHighScoreBball)
            mHighScoreBball = gameScore;

        if(gameScore > mHighScoreGame)
            mHighScoreGame = gameScore;
    }

    private void getRoundStats(GameThrow[] round) {
        // get game mode
        GameFlags.GameFlag gameMode = round[0].getGameMode().getGameModeFlag();

        int roundMarks = 0; int highMarks = 0;
        int roundScore = 0; int highScore = 0;
        for (int i = 0; i < 3; i++) {
            roundMarks += round[i].getMulti();
            highMarks  += mHighestMarksRound[i].getMulti();
            roundScore += round[i].getScore();
            highScore  += mHighestScoreRound[i].getScore();
        }

        roundScore = Math.abs(roundScore);
        highScore = Math.abs(highScore);
        roundMarks = Math.abs(roundMarks);
        highMarks = Math.abs(highMarks);

        if(roundMarks > highMarks)
            for (int i = 0; i < 3; i++) mHighestMarksRound[i] = new GameThrow(round[i]);

        if(roundScore > highScore) {
            for (int i = 0; i < 3; i++) mHighestScoreRound[i] = new GameThrow(round[i]);
            mHighScoreRound = roundScore;
        }

        if(gameMode.equals(SHANGHAI))
            mShanghais += isShanghai(round) ? 1 : 0;

        if(roundScore == 180) mScorePlus[0]++;
        else if(roundScore >= 140) mScorePlus[1]++;
        else if(roundScore >= 100) mScorePlus[2]++;
        else if(roundScore >= 60) mScorePlus[3]++;
    }

    private boolean isShanghai(GameThrow[] round) {
        boolean single = false, dub = false, triple = false;
        for (int i = 0; i < 3; i++) {
            if(round[i].getMulti() == 1) single = true;
            if(round[i].getMulti() == 2) dub = true;
            if(round[i].getMulti() == 3) triple = true;
        }

        return single && dub && triple;
    }

    private void calculateBestNumber() {
        int highest = 14;
        for(int i = 14; i < 20; i++)
            if(mNumberHits[i] > mNumberHits[highest]) highest = i;

        mBestNumber = highest + 1;
    }

    private void calculateWorstNumber() {
        int lowest = 14;
        for(int i = 14; i < 20;  i++)
            if(mNumberHits[i] < mNumberHits[lowest]) lowest = i;

        mWorstNumber = lowest + 1;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getGamesPlayed() {
        return mGames;
    }

    public void setGamesPlayed(int value) {
        mGames = value;
    }

    private float getRounds() {
        return getTotalThrows() / 3f;
    }

    public int getWins() {
        return mWins;
    }

    public void setWins(int value) {
        mWins = value;
    }

    public float getWinPercent() {
        if(getWins() == 0) return 0;
        return ((float) getWins() / getGamesPlayed()) * 100f;
    }


    public float getScoreAvgRound() {
        if(mTotalScore == 0) return 0;
        return (float) mTotalScore / getRounds();
    }

    public float getHitsAvgRound() {
        if(mHits == 0) return 0;
        return  (float) mHits / getRounds();
    }

    public float getMarkAvgRound() {
        if(mMarks == 0) return 0;
        return (float) mMarks / getRounds();
    }

    public int getTotalThrows() {
        return mTotalThrows;
    }
    public void setTotalThrows(int value) {
        mTotalThrows = value;
    }

    public int getTotalHits() {
        return mHits;
    }

    public void setTotalHits(int value) {
        mHits = value;
    }

    public int getTotalMarks() {
        return mMarks;
    }

    public void setTotalMarks(int value) {
        mMarks = value;
    }

    public int getTotalScore() {
        return mTotalScore;
    }

    public void setTotalScore(int value) {
        mTotalScore = value;
    }

    public int getScorePlus(int index) {
        return mScorePlus[index];
    }

    public void setScorePlus(int index, int value) {
        mScorePlus[index] = value;
    }

    public int getScore180s() {
        return getScorePlus(0);
    }
    public void setScore180s(int value) {
        setScorePlus(0, value);
    }
    public int getScore140s() {
        return getScorePlus(1);
    }

    public void setScore140s(int value) {
        setScorePlus(1, value);
    }

    public int getScore100s() {
        return getScorePlus(2);
    }

    public void setScore100s(int value) {
        setScorePlus(2, value);
    }

    public int getScore60s() {
        return  getScorePlus(3);
    }

    public void setScore60s(int value) {
        setScorePlus(3, value);
    }

    public int getBestOut() {
        return mBestOut;
    }

    public void setBestOut(int value) {
        mBestOut = value;
    }

    public int getShanghais() {
        return mShanghais;
    }

    public void setShanghais(int value) {
        mShanghais = value;
    }

    public boolean Shanghaied() { return mShanghais > 0; }

    public int getKills() {
        return mKills;
    }

    public void setKills(int value) {
        mKills = value;
    }

    public int getBulls() {
        return (mDubBulls * 2) + mSingleBulls;
    }

    public int getDubBulls() {
        return mDubBulls;
    }

    public void setDubBulls(int value) {
        mDubBulls = value;
    }

    public int getSingleBulls() {
        return mSingleBulls;
    }

    public void setSingleBulls(int value) {
        mSingleBulls = value;
    }

    public int getTriples() {
        return mTriples;
    }

    public void setTriples(int value) {
        mTriples = value;
    }

    public int getDoubles() {
        return mDoubles;
    }

    public void setDoubles(int value) {
        mDoubles = value;
    }

    public int getSingles() {
        return mSingles;
    }

    public void setSingles(int value) {
        mSingles = value;
    }

    public int getHighScoreGame() {
        return mHighScoreGame;
    }
    public void setHighScoreGame(int value) {
        mHighScoreGame = value;
    }

    public int getHighScoreRound() {
        return mHighScoreRound;
    }

    public void setHighScoreRound(int value) {
        mHighScoreRound = value;
    }

    public int getHighScoreCricket() {
        return mHighScoreCrick;
    }

    public void setHighScoreCricket(int value) {
        mHighScoreCrick = value;
    }

    public int getHighScoreShanghai() {
        return mHighScoreShang;
    }

    public void setHighScoreShanghai(int value) {
        mHighScoreShang = value;
    }

    public int getHighScoreBaseball() {
        return mHighScoreBball;
    }

    public void setHighScoreBaseball(int value) {
        mHighScoreBball = value;
    }


    public int getBestNumber() {
        return mBestNumber;
    }

    public void setBestNumber(int value) {
        mBestNumber = value;
    }

    public int getWorstNumber() {
        return mWorstNumber;
    }

    public void setWorstNumber(int value) {
        mWorstNumber = value;
    }

    public String getHighestMarksRoundString() {
        if(mHighestMarksRound[0] == null) return "---";

        String str = "";
        str += mHighestMarksRound[0].getNumber() + "x" + mHighestMarksRound[0].getMulti() + ", ";
        str += mHighestMarksRound[1].getNumber() + "x" + mHighestMarksRound[1].getMulti() + ", ";
        str += mHighestMarksRound[2].getNumber() + "x" + mHighestMarksRound[2].getMulti();

        return str;
    }

    public int getHighestMarksRound() {
        int count = 0;
        for (int i = 0; i < 3; i++)
            count += mHighestMarksRound[i].getMulti();
        return count;
    }


    public String getBestToWorstNumber() {
        int[][] order = new int[21][2];

        for(int i = 0; i < 21; i++) {
            order[i][0] = (i == 20) ? 25 : i + 1;
            order[i][1] = mNumberHits[i];
        }

        java.util.Arrays.sort(order, (a, b) -> -Integer.compare(a[1], b[1]));

        StringBuilder numList = new StringBuilder();
        for(int i = 0; i < 21; i++)
            numList.append(order[i][0] == 25 ? "B" : order[i][0]).append(i == 20 ? "" : ", ");

        return numList.toString();
    }

    //
    // Baseball Stats
    //

    public int getBaseballInnings() {
        return mBaseballInnings;
    }

    public void setBaseballInnings(int value) {
        mBaseballInnings = value;
    }

    public int getBaseballBestInning() {
        return mBaseballBestInning;
    }

    public void setBaseballBestInning(int value) {
        mBaseballBestInning = value;
    }

    public int getBaseballRuns() {
        return mBaseballRuns;
    }

    public void setBaseballRuns(int value) {
        mBaseballRuns = value;
    }

    public int getBaseballBases() {
        return mBaseballBases;
    }

    public void setBaseballBases(int value) {
        mBaseballBases = value;
    }

    public float getBaseballRunsPerInning() {
        if(mBaseballRuns == 0) return 0;
        return (float) mBaseballRuns / getBaseballInnings();
    }

    public float getBaseballHitsPerInning() {
        if(mBaseballBases == 0) return 0;
        return (float) mBaseballBases / getBaseballInnings();
    }

    public int getGrandSlams() {
        return mGrandSlams;
    }

    public void setGrandSlams(int value) {
        mGrandSlams = value;
    }

    public int getHomeRuns() {
        return mHomeRuns;
    }

    public void setHomeRuns(int value) {
        mHomeRuns = value;
    }

    public String getStatString(StatId id) {
        int i = 0;

        // games, wins, loses, win avg
        if(id.equals(GAMES_PLAYED))         return String.valueOf(getGamesPlayed());
        else if(id.equals(TOTAL_WINS))      return String.valueOf(getWins());
        else if(id.equals(WIN_AVG))         return String.format(Locale.US, "%.2f", getWinPercent());

            // marks
        else if(id.equals(MARK_AVG))     return String.format(Locale.US, "%.2f", getMarkAvgRound());
        else if(id.equals(BEST_MARKS))   return String.valueOf(getHighestMarksRound());
        else if(id.equals(SCORE_AVG))     return String.format(Locale.US, "%.2f", getScoreAvgRound());

            // score highest round
        else if(id.equals(BEST_SCORE))      return String.valueOf(getHighScoreRound());
        else if(id.equals(SCORE_180s))      return String.valueOf(getScorePlus(0));
        else if(id.equals(SCORE_140_179))      return String.valueOf(getScorePlus(1));
        else if(id.equals(SCORE_100_139))      return String.valueOf(getScorePlus(2));
        else if(id.equals(SCORE_60_99))      return String.valueOf(getScorePlus(3));

        // bulls
        else if(id.equals(BULLSEYES))    return String.valueOf(getBulls());
        else if(id.equals(DOUBLE_BULLS)) return String.valueOf(getDubBulls());
        else if(id.equals(SINGLE_BULLS)) return String.valueOf(getSingleBulls());

        // multis
        else if(id.equals(HIT_TRIPLES)) return String.valueOf(getTriples());
        else if(id.equals(HIT_DOUBLES)) return String.valueOf(getDoubles());
        else if(id.equals(HIT_SINGLES)) return String.valueOf(getSingles());

        else if(id.equals(BEST_OUT))     return String.valueOf(getBestOut());

        // best and worst numbers
        else if(id.equals(BEST_NUM))  return String.valueOf(getBestNumber());
        else if(id.equals(WORST_NUM)) return String.valueOf(getWorstNumber());

            // shanghais, kills
            //  high scores
        else if(id.equals(CRICKET_HIGH))    return String.valueOf(getHighScoreCricket());
        else if(id.equals(SHANGHAI_HIGH))   return String.valueOf(getHighScoreShanghai());
        else if(id.equals(SHANGHAIS))   return String.valueOf(getShanghais());
        else if(id.equals(KILLS))       return String.valueOf(getKills());
        else if(id.equals(BASEBALL_HIGH))   return String.valueOf(getHighScoreBaseball());
        else if(id.equals(BEST_INNING))   return String.valueOf(getBaseballBestInning());
        else if(id.equals(RUNS_INNING))   return String.format(Locale.US, "%.2f", getBaseballRunsPerInning());
        else if(id.equals(BASES_INNING))   return String.format(Locale.US, "%.2f", getBaseballHitsPerInning());
        else if(id.equals(HOME_RUNS))   return String.valueOf(getHomeRuns());
        else if(id.equals(GRAND_SLAMS))   return String.valueOf(getGrandSlams());
        else if(id.equals(BEST_GAME))   return String.valueOf(mHighScoreGame);
        else if(id.equals(TOTAL_SCORE))   return String.valueOf(mTotalScore);


        return "";
    }
}
