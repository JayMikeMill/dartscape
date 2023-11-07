/*
 * Copyright (C) 2023 G Works
 */


package com.gworks.dartscape.data;

import static com.gworks.dartscape.data.GameFlags.GameFlag.ALL_GAMES;
import static com.gworks.dartscape.data.GameFlags.GameFlag.ALL_MODES;
import static com.gworks.dartscape.data.GameFlags.GameFlag.ALL_VARIANTS;
import static com.gworks.dartscape.data.GameFlags.GameFlag.BASEBALL;
import static com.gworks.dartscape.data.GameFlags.GameFlag.CRICKET;
import static com.gworks.dartscape.data.GameFlags.GameFlag.CRICKET_CUT_THROAT;
import static com.gworks.dartscape.data.GameFlags.GameFlag.CRICKET_NORMAL;
import static com.gworks.dartscape.data.GameFlags.GameFlag.CRICKET_OPEN;
import static com.gworks.dartscape.data.GameFlags.GameFlag.CRICKET_TURNS;
import static com.gworks.dartscape.data.GameFlags.GameFlag.KILLER;
import static com.gworks.dartscape.data.GameFlags.GameFlag.MANY_KILLERS;
import static com.gworks.dartscape.data.GameFlags.GameFlag.SHANGHAI;
import static com.gworks.dartscape.data.GameFlags.GameFlag.THE_KILLER;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_1001;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_1201;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_1501;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_301;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_501;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_701;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_901;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_DOUBLE_IN;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_DOUBLE_OUT;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_MASTER_IN;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_MASTER_OUT;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_STRAIGHT_IN;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_STRAIGHT_OUT;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_TRIPLE_IN;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01_TRIPLE_OUT;
import static com.gworks.dartscape.main.Globals.AppStringId.END;
import static com.gworks.dartscape.main.Globals.AppStringId.INNING;
import static com.gworks.dartscape.main.Globals.AppStringId.INNINGS;
import static com.gworks.dartscape.main.Globals.AppStringId.LIFE;
import static com.gworks.dartscape.main.Globals.AppStringId.LIVES;
import static com.gworks.dartscape.main.Globals.AppStringId.START;
import static com.gworks.dartscape.main.Globals.getAppString;

import com.gworks.dartscape.R;
import com.gworks.dartscape.main.DartScapeActivity;

import java.util.ArrayList;

/**
 * flags representing the different game modes, options and settings.
 */


public class GameFlags {

    /**
     * flags representing the different game modes, options and settings.
     */

    public enum GameFlag {
        // game modes
        NONE,
        ALL_GAMES,
        ALL_MODES,
        ALL_VARIANTS,

        // game options
        X01,
        X01_301,
        X01_501,
        X01_701,
        X01_901,
        X01_1001,
        X01_1201,
        X01_1501,
        X01_STRAIGHT_IN,
        X01_STRAIGHT_OUT,
        X01_DOUBLE_IN,
        X01_DOUBLE_OUT,
        X01_TRIPLE_IN,
        X01_TRIPLE_OUT,
        X01_MASTER_IN,
        X01_MASTER_OUT,

        // game options
        CRICKET,
        CRICKET_NORMAL,
        CRICKET_CUT_THROAT,

        CRICKET_TURNS,
        CRICKET_OPEN,

        SHANGHAI,

        BASEBALL,

        KILLER,
        MANY_KILLERS,
        THE_KILLER,

        // game info
        STARTED_NEW_GAME,
        STARTED_NEW_SET,

        GAME_DATA_RESTORED
    }

    public static ArrayList<String> FLAG_UI_TEXT;
    public static ArrayList<Integer> FLAG_UI_INDEX;

    public static void init(DartScapeActivity activity) {
        FLAG_UI_TEXT = new ArrayList<>();
        FLAG_UI_INDEX = new ArrayList<>();

        addFlag(activity.getString(R.string.no), 0);
        addFlag(activity.getString(R.string.all_games), 0);
        addFlag(activity.getString(R.string.all_modes), 0);
        addFlag(activity.getString(R.string.all_variants), 0);

        addFlag(activity.getString(R.string._01_games), 0);
        addFlag(activity.getString(R.string._301), 0);
        addFlag(activity.getString(R.string._501), 1);
        addFlag(activity.getString(R.string._701), 2);
        addFlag(activity.getString(R.string._901), 3);
        addFlag(activity.getString(R.string._1001), 4);
        addFlag(activity.getString(R.string._1201), 5);
        addFlag(activity.getString(R.string._1501), 6);

        addFlag(activity.getString(R.string.straight_in), 0);
        addFlag(activity.getString(R.string.straight_out), 0);
        addFlag(activity.getString(R.string.double_in), 1);
        addFlag(activity.getString(R.string.double_out), 1);
        addFlag(activity.getString(R.string.triple_in), 2);
        addFlag(activity.getString(R.string.triple_out), 2);
        addFlag(activity.getString(R.string.master_in), 3);
        addFlag(activity.getString(R.string.master_out), 3);

        addFlag(activity.getString(R.string.cricket), 1);
        addFlag(activity.getString(R.string.normal), 0);
        addFlag(activity.getString(R.string.cut_throat), 1);

        addFlag(activity.getString(R.string.turns), 0);
        addFlag(activity.getString(R.string.open), 1);

        addFlag(activity.getString(R.string.shanghai), 2);

        addFlag(activity.getString(R.string.baseball), 3);

        addFlag(activity.getString(R.string.killer), 4);
        addFlag(activity.getString(R.string.killers), 0);
        addFlag(activity.getString(R.string.one_killer), 1);
    }

    private static void addFlag(String uiText, int uiIndex) {
        FLAG_UI_TEXT.add(uiText);
        FLAG_UI_INDEX.add(uiIndex);
    }

    public static String getUiText(GameFlags.GameFlag flag) {
        return FLAG_UI_TEXT.get(flag.ordinal());
    }

    /** @return the flag represented by the string */
    public static GameFlag getFlagFromUiText(String strFlag) {
        int index = FLAG_UI_TEXT.indexOf(strFlag);
        return GameFlag.values()[index];
    }

    /** @return a string list containing all game types */
    public static ArrayList<String> getAllGames() {
        return getGames(true);
    }

    /** @return a string list containing all game types */
    public static ArrayList<String> getGames(boolean includeAll) {
        ArrayList<String> gameTypes = new ArrayList<>();

        if(includeAll)
            gameTypes.add(getUiText(ALL_GAMES));

        gameTypes.add(getUiText(X01));
        gameTypes.add(getUiText(CRICKET));
        gameTypes.add(getUiText(SHANGHAI));
        gameTypes.add(getUiText(BASEBALL));
        gameTypes.add(getUiText(KILLER));

        return gameTypes;
    }


    /** @return a string list containing all game modes based on game in gameflag */
    public static ArrayList<String> getGameModes(GameFlag gameFlag) {
        return getGameModes(gameFlag, true);
    }

    /** @return a string list containing all game modes based on game in gameflag */
    public static ArrayList<String> getGameModes(GameFlag gameFlag, boolean includeAll) {
        ArrayList<String> gameModes = new ArrayList<>();

        if(gameFlag.equals(X01)) {
            gameModes.add(getUiText(X01_301));
            gameModes.add(getUiText(X01_501));
            gameModes.add(getUiText(X01_701));
            gameModes.add(getUiText(X01_901));
            gameModes.add(getUiText(X01_1001));
            gameModes.add(getUiText(X01_1201));
            gameModes.add(getUiText(X01_1501));
        }

        else if(gameFlag.equals(CRICKET)) {
            gameModes.add(getUiText(CRICKET_NORMAL));
            gameModes.add(getUiText(CRICKET_CUT_THROAT));
        }

        else if(gameFlag.equals(BASEBALL)) {
            // no game modes yet
        }

        else if(gameFlag.equals(KILLER)) {
            gameModes.add(getUiText(MANY_KILLERS));
            gameModes.add(getUiText(THE_KILLER));
        }

        if(!gameModes.isEmpty() && includeAll)
            gameModes.add(0, getUiText(ALL_MODES));

        return gameModes;
    }

    /** @return a string list containing all game variants based on game in gameflag */
    public static ArrayList<String> getGameVariants(GameFlag gameFlag, boolean statsFlags, boolean secondary) {

        ArrayList<String> gameVariants = new ArrayList<>();

        if(gameFlag.equals(X01)) {
            gameVariants.add(getUiText(
                    secondary ? X01_STRAIGHT_OUT : X01_STRAIGHT_IN));
            gameVariants.add(getUiText(
                    secondary ? X01_DOUBLE_OUT : X01_DOUBLE_IN));
            gameVariants.add(getUiText(
                    secondary ? X01_TRIPLE_OUT : X01_TRIPLE_IN));
            gameVariants.add(getUiText(
                    secondary ? X01_MASTER_OUT : X01_MASTER_IN));
        }

        else if(gameFlag.equals(CRICKET)) {
            if(!statsFlags && secondary) {
                gameVariants.add("NO CAP");
                for(int i = 1; i <= 4; i++)
                    gameVariants.add(String.valueOf(i*100));
            }

            else if (!statsFlags) {
                gameVariants.add(getUiText(CRICKET_TURNS));
                gameVariants.add(getUiText(CRICKET_OPEN));
            }
        }

        else if(gameFlag.equals(SHANGHAI) && !statsFlags) {
            if(!secondary) for (int i = 0; i < 20; i++) gameVariants.add(" " + getAppString(START) + "# " + + (i + 1));
            else for (int i = 20; i > 0; i--) gameVariants.add(" " + getAppString(END) + "# " + i);
            return gameVariants;
        }

        else if(gameFlag.equals(BASEBALL)) {
            if(!statsFlags && !secondary) {
                for (int i = 1; i <= 18; i++) gameVariants.add
                        (i + (i == 1 ? " " + getAppString(INNING): " " + getAppString(INNINGS)));
            }
        }

        else if(gameFlag.equals(KILLER)) {
            if(!statsFlags && !secondary) {
                for (int i = 1; i <= 9; i++) gameVariants.add
                        (i + (i == 1 ? " " + getAppString(LIFE) : " " + getAppString(LIVES)));
            }
        }

        if(!gameVariants.isEmpty() && statsFlags)
            gameVariants.add(0, getUiText(ALL_VARIANTS));

        return gameVariants;
    }

};
