/*
 * Copyright (C) 2023 G Works
 */

package com.gworks.dartscape.data;

import static com.gworks.dartscape.data.GameFlags.*;
import static com.gworks.dartscape.data.GameFlags.GameFlag.*;

import java.util.EnumSet;

/**
 * manages a set of {@link GameFlag}s, containing functions for settings and
 * retrieve these flags and the name of these flags for ui display.
 */

public class GameFlagSet {
    /** the main enum set */
    private final EnumSet<GameFlag> mGameFlags;

    /** default constructor */
    public GameFlagSet() {
        mGameFlags = EnumSet.noneOf(GameFlag.class);
    }

    /** construct from another GameFlagSet */
    public GameFlagSet(GameFlagSet flags) {
        mGameFlags = flags.getSet().clone();
    }

    /** @return the EnumSet of game flags */
    public EnumSet<GameFlag> getSet() {
        return mGameFlags;
    }

    /** @return a string representing all game flags stored in this set */
    public String flagString() {
        return mGameFlags.toString();
    }

    /** @return true if there are no game flags in this set */
    public boolean isEmpty() {
        return mGameFlags.isEmpty();
    }

    /** @return true if set contains flag, false if not */
    public boolean get(GameFlag flag) {
        return mGameFlags.contains(flag);
    }

    /**@return true if set contains flag, false if not. also removes the flag from the set. */
    public boolean getClear(GameFlag flag) {
        if(flag.equals(NONE)) return false;
        boolean isSet = mGameFlags.contains(flag);
        mGameFlags.remove(flag);
        return isSet;
    }

    /** add a flag to this set */
    public void add(GameFlag flag) {
        if(flag.equals(NONE)) return;
        mGameFlags.add(flag);
    }

    /** add or remove a flag from this set */
    public void set(GameFlag flag, boolean set) {
        if(flag.equals(NONE)) return;
        if(set) mGameFlags.add(flag);
        else mGameFlags.remove(flag);
    }

    /** remove a flag from this set */
    public void clear(GameFlag flag) {
        if(flag.equals(NONE)) return;
        mGameFlags.remove(flag);
    }

    /** remove all flags from this set */
    public void clearAll() {
        mGameFlags.clear();
    }

    /** @return the index of the games based on the flag list retrieval functions */
    public int getGameIndex(GameFlag game) {
        if(game.equals(X01))      return 0;
        if(game.equals(CRICKET))  return 1;
        if(game.equals(SHANGHAI)) return 2;
        if(game.equals(BASEBALL)) return 3;
        if(game.equals(KILLER))   return 4;

        return 0;
    }

    /** @return the index of the games 01 modes based on the flag list retrieval functions */
    public int get01ModeIndex() {
        if(get(X01_301))  return 0;
        if(get(X01_501))  return 1;
        if(get(X01_701))  return 2;
        if(get(X01_901))  return 3;
        if(get(X01_1001)) return 4;
        if(get(X01_1201)) return 5;
        if(get(X01_1501)) return 6;
        return 0;
    }

    /** @return the index of the games 01 variants in based on the flag list retrieval functions*/
    public int get01VariantInIndex() {
        if(get(X01_STRAIGHT_IN))    return 0;
        if(get(X01_DOUBLE_IN)) return 1;
        if(get(X01_TRIPLE_IN)) return 2;
        if(get(X01_MASTER_IN)) return 3;

        return 0;
    }

    /** @return the index of the games 01 variant out based on the flag list retrieval functions */
    public int get01VariantOutIndex() {
        if(get(X01_STRAIGHT_OUT)) return 0;
        if(get(X01_DOUBLE_OUT)) return 1;
        if(get(X01_TRIPLE_OUT)) return 2;
        if(get(X01_MASTER_OUT)) return 3;

        return 0;
    }

    /** @return the index of the cricket game modes based on the flag list retrieval functions */
    public int getCricketModeIndex() {
        if(get(CRICKET_NORMAL))                return 0;
        if(get(CRICKET_CUT_THROAT))            return 1;
        return 0;
    }

    /** @return the index of the cricket variant based on the flag list retrieval functions */
    public int getCricketVariantIndex() {
        if(get(CRICKET_TURNS))     return 0;
        if(get(CRICKET_OPEN)) return 1;
        return 0;
    }

    /** @return the index of the killer game variant based on the flag list retrieval functions */
    public int getKillerVariantIndex() {
        if(get(MANY_KILLERS))    return 0;
        if(get(THE_KILLER))      return 1;

        return 0;
    }

    /** @return a flag set containing all game modes based on game in gameflag */
    public GameFlagSet getGameMode() {
        GameFlagSet flags = new GameFlagSet(this);

        // clear all flags that are not game mode flags
        flags.clear(NONE);
        flags.clear(ALL_GAMES);
        flags.clear(ALL_MODES);
        flags.clear(ALL_VARIANTS);
        flags.clear(CRICKET_OPEN);
        flags.clear(CRICKET_TURNS);
        flags.clear(CRICKET_TURNS);
        return flags;
    }

    /** @return a flag containing the current game */
    public GameFlag getGameFlag() {
        if(get(ALL_GAMES)) return ALL_GAMES;
        if(get(X01)) return X01;
        if(get(CRICKET)) return CRICKET;
        if(get(SHANGHAI)) return SHANGHAI;
        if(get(BASEBALL)) return BASEBALL;
        if(get(KILLER)) return KILLER;

        return null;
    }

    /** @return a flag set from a string representing all flags to add to set */
    public static GameFlagSet fromString(String strFlags) {
        String[] gameModes = strFlags.substring(1, strFlags.length() -1).split(", ");
        GameFlagSet flags = new GameFlagSet();
        for (String mode : gameModes) flags.add(GameFlag.valueOf(mode));

        return flags;
    }

    /** @return the game set in the flag set */
    public GameFlag getGameModeFlag() {
        if(get(X01))      return X01;
        if(get(CRICKET))  return CRICKET;
        if(get(SHANGHAI)) return SHANGHAI;
        if(get(BASEBALL))   return BASEBALL;
        if(get(KILLER))   return KILLER;

        return NONE;
    }

    /** @return compare with another GameFlagSet to see if the contain the same game mode */
    public boolean isGameModeEqual(GameFlagSet gameMode) {
        return getGameModeString().equals(gameMode.getGameModeString());
    }

    /** @return A ui string of game mode */
    public String getGameModeString() {
        String gameMode = "";
        if (get(ALL_GAMES)) return getUiText(ALL_GAMES);
        if     (get(X01) && get(ALL_MODES)) gameMode += getUiText(X01);
        else if (get(X01_301)) gameMode += getUiText(X01_301);
        else if (get(X01_501)) gameMode += getUiText(X01_501);
        else if (get(X01_701)) gameMode += getUiText(X01_701);
        else if (get(X01_901)) gameMode += getUiText(X01_901);
        else if (get(X01_1001)) gameMode += getUiText(X01_1001);
        else if (get(X01_1201)) gameMode += getUiText(X01_1201);
        else if (get(X01_1501)) gameMode += getUiText(X01_1501);
        else if (get(CRICKET)) gameMode += getUiText(CRICKET);
        else if (get(SHANGHAI)) gameMode += getUiText(SHANGHAI);
        else if (get(BASEBALL)) gameMode += getUiText(BASEBALL);
        else if (get(KILLER)) gameMode += getUiText(KILLER);

        if (get(X01)) {
            if     (get(X01_STRAIGHT_IN)) gameMode += " - " + getUiText(X01_STRAIGHT_IN);
            else if (get(X01_DOUBLE_IN)) gameMode += " - " + getUiText(X01_DOUBLE_IN);
            else if (get(X01_TRIPLE_IN)) gameMode += " - " + getUiText(X01_TRIPLE_IN);
            else if (get(X01_MASTER_IN)) gameMode += " - " + getUiText(X01_MASTER_IN);

            if     (get(X01_STRAIGHT_OUT)) gameMode += " - " + getUiText(X01_STRAIGHT_OUT);
            else if (get(X01_DOUBLE_OUT)) gameMode += " - " + getUiText(X01_DOUBLE_OUT);
            else if (get(X01_TRIPLE_OUT)) gameMode += " - " + getUiText(X01_TRIPLE_OUT);
            else if (get(X01_MASTER_OUT)) gameMode += " - " + getUiText(X01_MASTER_OUT);
        } else if (get(CRICKET)) {
            if (get(CRICKET_CUT_THROAT)) gameMode += " - " + getUiText(CRICKET_CUT_THROAT);
        } else if (get(KILLER)) {
            if (get(THE_KILLER)) gameMode = getUiText(THE_KILLER);
            else gameMode = getUiText(MANY_KILLERS);
        }

        return gameMode;
    }
}
