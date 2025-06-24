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

    /** initializes game throw list */
    public void init() {
        mGameThrows = new ArrayList<>();
    }

    /** clears all throws from game throw list */
    public void clear() {
        mGameThrows = new ArrayList<>();
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
