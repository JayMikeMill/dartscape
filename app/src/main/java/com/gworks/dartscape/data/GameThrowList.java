/*
 * Copyright (C) 2023 G Works
 */

package com.gworks.dartscape.data;

import static com.gworks.dartscape.data.GameFlags.GameFlag.*;
import static com.gworks.dartscape.main.Globals.ROUND_THROWS;

import java.util.ArrayList;

/**
 * manages a list of {@link GameThrow}s, containing functions for retrieving strings
 * representing the game throws for storing and retrieving from a data base.
 */

public class GameThrowList {
    /** the main list of game throws */
    ArrayList<GameThrow> mGameThrows;

    /** the default constructor */
    public GameThrowList() {
        init();
    }

    /** construct from a string representing a list of game throws */
    public GameThrowList(long gameTime, GameFlagSet gameMode, boolean win, String throwList) {
        init();
        initFromString(gameTime, gameMode, win, throwList);
    }

    /** initializes game throw list */
    public void init() {
        mGameThrows = new ArrayList<>();
    }

    /** clears all throws from game throw list */
    public void clear() {
        mGameThrows = new ArrayList<>();
    }

    /** clears all throws from game throw list */
    public void append(GameThrowList gameThrows) {
        for (int i = 0; i < gameThrows.size(); i++) {
            add(gameThrows.get(i));
        }
    }

    /** initialize throw list from a strings. (e.g. "[number, multi, score]" - "[12, 2, 24][10, 3, 30]") */
    public void initFromString(long gameTime, GameFlagSet gameMode, boolean win, String throwList) {

        if(throwList.isEmpty()) return;

        String[] strThrowSet = throwList.substring(1, throwList.length() - 1)
                .replace("][", ", ").split(", ");

        for (int j = 0; j <= strThrowSet.length - 3; j += 3) {
            mGameThrows.add(new GameThrow
                    (gameTime, gameMode, win, -1,
                            Integer.parseInt(strThrowSet[j]),
                            Integer.parseInt(strThrowSet[j + 1]),
                            Integer.parseInt(strThrowSet[j + 2])));
        }
    }

    /** retrieve a string representing items in this throw list
     *  (e.g. "[number, multi, score]" - "[12, 2, 24][10, 3, 30]") */
    public String getThrowsString() {
        StringBuilder strGameThrows = new StringBuilder();
        for(int i = 0; i < mGameThrows.size(); i++)
                strGameThrows.append("[").append(mGameThrows.get(i).getNumber())
                        .append(", ").append(mGameThrows.get(i).getMulti()).append(", ")
                        .append(mGameThrows.get(i).getScore()).append("]");

        return strGameThrows.toString();
    }

    /** get a sub-list of only throw by certain player at index */
    public GameThrowList getPlayerThrowList(int index) {
        return getPlayerThrowList(index, 0);
    }

    /** get a sub-list of only throw by certain player at index, or their teammate */
    public GameThrowList getPlayerThrowList(int index, int team) {
        GameThrowList throwList = new GameThrowList();

        for(int i = 0; i < mGameThrows.size(); i++)
            if(mGameThrows.get(i).getPlayerIndex() == index)
                throwList.add(mGameThrows.get(i));

        // get one team members throws

        if(team > 0 && team < 3) {
            GameThrowList teamThrowList = new GameThrowList();

            if(mGameThrows.get(0).getGameMode().getGameFlag().equals(BASEBALL)) {
                for (GameThrow gthrow : mGameThrows) {
                    if(team == 1 && gthrow.getNumber() % 2 != 0) {
                        teamThrowList.add(gthrow);
                    } else if (team == 2 && gthrow.getNumber() % 2 == 0) {
                        teamThrowList.add(gthrow);
                    }
                }
            } else {
                // add every other three for team
                for (int i = (team == 1 ? 0 : ROUND_THROWS);
                     i <= throwList.size() - ROUND_THROWS; i += ROUND_THROWS * 2)
                    for (int j = 0; j < ROUND_THROWS; j++) teamThrowList.add(throwList.get(i + j));
            }
            return teamThrowList;
        }

        return throwList;
    }

    /** @return the players stats based off this throw set */
    public PlayerStats getStats() {
        return new PlayerStats(this);
    }

    /** @return a single game throw in this set */
    public GameThrow get(int index) {
        return mGameThrows.get(index);
    }

    /** add a throw to this set */
    public void add(GameThrow gameThrow) {
        mGameThrows.add(gameThrow);
    }

    /** @return the last throw in this let */
    public GameThrow getLast() {
        return mGameThrows.get(mGameThrows.size() - 1);
    }

    /** remove the last throw in this let */
    public void removeLast() {
        mGameThrows.remove(mGameThrows.size() - 1);
    }

    /** @return true if their are no throws in this set */
    public boolean isEmpty() {
        return mGameThrows.isEmpty();
    }

    /** @return the number of throws in this set */
    public int size() {
        return mGameThrows.size();
    }

}
