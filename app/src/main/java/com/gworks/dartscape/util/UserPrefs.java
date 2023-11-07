package com.gworks.dartscape.util;

import static com.gworks.dartscape.main.Globals.*;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.gworks.dartscape.BuildConfig;
import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.data.GameFlags;

public class UserPrefs {
    private static SharedPreferences mPrefs;

    // User Preference storage addresses                         User Preference storage addresses

    private static final String PREF_STR_NAME                  = "com.gworks.dartscape";
    private static final String PREF_BOOL_OWNS_PRO              = PREF_STR_NAME + ".owns_pro";
    private static final String PREF_STR_GAME_DATA_BACKUP      = PREF_STR_NAME + ".game_data_backup";
    private static final String PREF_STR_GAME_SETTING_BACKUP   = PREF_STR_NAME + ".game_settings_backup";
    private static final String PREF_INT_VOLUME                = PREF_STR_NAME + ".volume";
    private static final String PREF_BOOL_PLAY_WINNER_SOUND    = PREF_STR_NAME + ".play_winner_sound";
    private static final String PREF_BOOL_PLAY_BUST_SOUND      = PREF_STR_NAME + ".play_bust_sound";
    private static final String PREF_INT_BOT_SPEED             = PREF_STR_NAME + ".bot_speed";
    private static final String PREF_INT_NEW_GAME_ORDER        = PREF_STR_NAME + ".new_game_order";
    private static final String PREF_BOOL_SHOW_WINS            = PREF_STR_NAME + ".show_wins";
    private static final String PREF_BOOL_SHOW_AVGS            = PREF_STR_NAME + ".show_avgs";
    private static final String PREF_BOOL_SHOW_GAME_INFO       = PREF_STR_NAME + ".show_cur_player";
    private static final String PREF_BOOL_SHOW_SHADOWS         = PREF_STR_NAME + ".show_shadows";
    private static final String PREF_INT_THEME_INDEX           = PREF_STR_NAME + ".theme_index";
    private static final String PREF_BOOL_APP_RESET            = PREF_STR_NAME + ".app_reset";

    // User Preference storage  Default User Preference Values
    private static final int     DEFAULT_VOLUME                 = 100;
    private static final boolean DEFAULT_PLAY_SOUNDS            = true;
    private static final int     DEFAULT_BOT_SPEED              = 2000;
    private static final int     DEFAULT_TURN_ORDER             = TURN_ORDER_WINNER_FIRST;
    private static final boolean DEFAULT_SHOW_IN_GAME_WINS      = false;
    private static final boolean DEFAULT_SHOW_IN_GAME_AVG       = false;
    private static final boolean DEFAULT_SHOW_GAME_INFO         = true;
    private static final boolean DEFAULT_SHOW_SHADOWS           = true;
    private static final int     DEFAULT_THEME_INDEX            = 0;

    private static boolean mIsFirstRun;

    private UserPrefs() {}

    public static void init(Context context) {
        mPrefs = context.getSharedPreferences(PREF_STR_NAME, Context.MODE_PRIVATE);
        checkFirstRun();
    }

    public static void checkFirstRun() {
        boolean firstRun = mPrefs.getInt(PREF_INT_VOLUME, -1) == -1;
        if (firstRun) setVolume(DEFAULT_VOLUME);

        mIsFirstRun = firstRun;
    }

    public static boolean isFirstRun() {
        return mIsFirstRun;
    }

    public static boolean getOwnsPro() {
        if (BuildConfig.DEBUG) {
            return mPrefs.getBoolean(PREF_BOOL_OWNS_PRO, false);
        } else {
            return mPrefs.getBoolean(PREF_BOOL_OWNS_PRO, false);
        }
    }

    public static void setOwnsPro(boolean ownsPro) {
        mPrefs.edit().putBoolean(PREF_BOOL_OWNS_PRO, ownsPro).apply();
    }

    //
    //  Getter's and Setter's for all user preferences
    //
    public static GameData restoreGameData() {
        String json = mPrefs.getString(PREF_STR_GAME_DATA_BACKUP, "");

        if (json.isEmpty()) return new GameData();
        else {
            GameData data = new Gson().fromJson(json, GameData.class);
            data.flags().add(GameFlags.GameFlag.GAME_DATA_RESTORED);

            return data;
        }
    }

    public static void backupGameData(GameData data) {
        String json = new Gson().toJson(data);
        mPrefs.edit().putString(PREF_STR_GAME_DATA_BACKUP, json).apply();
    }

    public static GameData restoreGameSettings() {
        String json = mPrefs.getString(PREF_STR_GAME_SETTING_BACKUP, "");

        if (json.isEmpty()) return null;
        else return new Gson().fromJson(json, GameData.class);
    }

    public static void backupGameSettings(GameData data) {
        String json = new Gson().toJson(data);
        mPrefs.edit().putString(PREF_STR_GAME_SETTING_BACKUP, json).apply();
    }

    public static void clearGameData() {
        mPrefs.edit().putString(PREF_STR_GAME_DATA_BACKUP, "").apply();
    }

    public static int getVolume() {
        return mPrefs.getInt(PREF_INT_VOLUME, DEFAULT_VOLUME);
    }

    public static void setVolume(int volume) {
        mPrefs.edit().putInt(PREF_INT_VOLUME, volume).apply();
    }

    public static boolean getPlayWinnerSound() {
        return mPrefs.getBoolean(PREF_BOOL_PLAY_WINNER_SOUND, DEFAULT_PLAY_SOUNDS);
    }

    public static void setPlayWinnerSound(boolean play) {
        mPrefs.edit().putBoolean(PREF_BOOL_PLAY_WINNER_SOUND, play).apply();
    }

    public static boolean getPlayBustSound() {
        return mPrefs.getBoolean(PREF_BOOL_PLAY_BUST_SOUND, DEFAULT_PLAY_SOUNDS);
    }

    public static void setPlayBustSound(boolean play) {
        mPrefs.edit().putBoolean(PREF_BOOL_PLAY_BUST_SOUND, play).apply();
    }

    public static int getBotSpeed() {
        return mPrefs.getInt(PREF_INT_BOT_SPEED, DEFAULT_BOT_SPEED);
    }

    public static void setBotSpeed(int speed) {
        mPrefs.edit().putInt(PREF_INT_BOT_SPEED, speed).apply();
    }

    public static int getNewGameOrder() {
        return mPrefs.getInt(PREF_INT_NEW_GAME_ORDER, DEFAULT_TURN_ORDER);
    }

    public static void setNewGameOrder(int order) {
        mPrefs.edit().putInt(PREF_INT_NEW_GAME_ORDER, order).apply();
    }

    public static boolean getShowWins() {
        return mPrefs.getBoolean(PREF_BOOL_SHOW_WINS, DEFAULT_SHOW_IN_GAME_WINS);
    }

    public static void setShowWins(boolean show) {
        mPrefs.edit().putBoolean(PREF_BOOL_SHOW_WINS, show).apply();
    }

    public static boolean getShowAvgs() {
        return mPrefs.getBoolean(PREF_BOOL_SHOW_AVGS, DEFAULT_SHOW_IN_GAME_AVG);
    }

    public static void setShowAvgs(boolean show) {
        mPrefs.edit().putBoolean(PREF_BOOL_SHOW_AVGS, show).apply();
    }

    public static boolean getShowGameInfo() {
        return mPrefs.getBoolean(PREF_BOOL_SHOW_GAME_INFO, DEFAULT_SHOW_GAME_INFO);
    }

    public static void setShowGameInfo(boolean show) {
        mPrefs.edit().putBoolean(PREF_BOOL_SHOW_GAME_INFO, show).apply();
    }

    public static boolean getShowShadows() {
        return mPrefs.getBoolean(PREF_BOOL_SHOW_SHADOWS, DEFAULT_SHOW_SHADOWS);
    }

    public static void setShowShadows(boolean show) {
        mPrefs.edit().putBoolean(PREF_BOOL_SHOW_SHADOWS, show).apply();
    }

    public static int getThemeIndex() {
        return mPrefs.getInt(PREF_INT_THEME_INDEX, DEFAULT_THEME_INDEX);
    }

    public static void setThemeIndex(int index) {
        mPrefs.edit().putInt(PREF_INT_THEME_INDEX, index).apply();
    }

    public static boolean getAppReset() {
        return mPrefs.getBoolean(PREF_BOOL_APP_RESET, false);
    }

    public static void setAppReset(boolean reset) {
        mPrefs.edit().putBoolean(PREF_BOOL_APP_RESET, reset).apply();
    }
}
