package com.gworks.dartscape.util;

import android.app.Activity;

import com.gworks.dartscape.R;

import java.util.ArrayList;

//
// dialog helper class
//
public class Themes {
    public static final int[] THEME_IDS = {
            R.style.AppTheme_Chalk,
            R.style.AppTheme_Moonlight,
            R.style.AppTheme_SummerTime,
            R.style.AppTheme_BlueSkies,
            R.style.AppTheme_NightTime,
            R.style.AppTheme_InkPad,
            R.style.AppTheme_RoyalBlood,
            R.style.AppTheme_TheForest,
            R.style.AppTheme_BlackCherry,
            R.style.AppTheme_MidnightBlues,
            R.style.AppTheme_CottonCandy,
            R.style.AppTheme_BlackAndYellow,
            R.style.AppTheme_NeonLights
    };

    private static ArrayList<String> THEME_NAMES;

    public static void init(Activity activity) {
        THEME_NAMES = new ArrayList<>();

        THEME_NAMES.add(activity.getString(R.string.chalk_board));
        THEME_NAMES.add(activity.getString(R.string.moonlight));
        THEME_NAMES.add(activity.getString(R.string.summer_time));
        THEME_NAMES.add(activity.getString(R.string.blue_skies));
        THEME_NAMES.add(activity.getString(R.string.night_time));
        THEME_NAMES.add(activity.getString(R.string.ink_pad));
        THEME_NAMES.add(activity.getString(R.string.royal_blood));
        THEME_NAMES.add(activity.getString(R.string.the_forest));
        THEME_NAMES.add(activity.getString(R.string.black_cherry));
        THEME_NAMES.add(activity.getString(R.string.midnight_blues));
        THEME_NAMES.add(activity.getString(R.string.cotton_candy));
        THEME_NAMES.add(activity.getString(R.string.black_and_yellow));
        THEME_NAMES.add(activity.getString(R.string.neon_lights));
    }
    
    public static ArrayList<String> getThemeNames() {
        return THEME_NAMES;
    }

    public static int getCurrentTheme() {
        int id = UserPrefs.getThemeIndex();
        if(id >= THEME_IDS.length) {
            UserPrefs.setThemeIndex(0);
            return THEME_IDS[0];
        }
        return THEME_IDS[id];
    }
}
