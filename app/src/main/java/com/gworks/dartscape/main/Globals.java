package com.gworks.dartscape.main;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameFlags;
import com.gworks.dartscape.data.Stats;
import com.gworks.dartscape.util.Themes;

import java.util.ArrayList;

public class Globals {
    public static final int MAX_PLAYERS  = 4;
    public static final int ROUND_THROWS = 3;
    public static final int NUMBER_COUNT = 7;
    public static final int NO_PLAYER    = -1;
    public static final int NO_NUMBER    = -1;

    public static final int TURN_ORDER_DONT_CHANGE     = 0;
    public static final int TURN_ORDER_FLIP            = 1;
    public static final int TURN_ORDER_WINNER_FIRST    = 2;
    public static final int TURN_ORDER_WINNER_TO_WORST = 3;
    public static final int TURN_ORDER_LOSER_FIRST     = 4;
    public static final int TURN_ORDER_LOSER_TO_BEST   = 5;

    private static ArrayList<String> FLAG_STRINGS;
    private static ArrayList<String> APP_STRINGS;

    public enum AppStringId {
        LIFE,
        LIVES,
        START,
        END,
        INNING,
        INNINGS
    }

    public static void init(DartScapeActivity activity) {
        GameFlags.init(activity);
        Stats.init(activity);
        Themes.init(activity);

        FLAG_STRINGS = new ArrayList<>();
        APP_STRINGS = new ArrayList<>();

        FLAG_STRINGS.add(activity.getString(R.string.all_games));
        FLAG_STRINGS.add(activity.getString(R.string.all_modes));
        FLAG_STRINGS.add(activity.getString(R.string.all_variants));

        FLAG_STRINGS.add(activity.getString(R.string._01_games));
        FLAG_STRINGS.add(activity.getString(R.string._301));
        FLAG_STRINGS.add(activity.getString(R.string._501));
        FLAG_STRINGS.add(activity.getString(R.string._701));
        FLAG_STRINGS.add(activity.getString(R.string._901));
        FLAG_STRINGS.add(activity.getString(R.string._1001));
        FLAG_STRINGS.add(activity.getString(R.string._1201));
        FLAG_STRINGS.add(activity.getString(R.string._1501));

        FLAG_STRINGS.add(activity.getString(R.string.straight_in));
        FLAG_STRINGS.add(activity.getString(R.string.straight_out));
        FLAG_STRINGS.add(activity.getString(R.string.double_in));
        FLAG_STRINGS.add(activity.getString(R.string.double_out));
        FLAG_STRINGS.add(activity.getString(R.string.triple_in));
        FLAG_STRINGS.add(activity.getString(R.string.triple_out));
        FLAG_STRINGS.add(activity.getString(R.string.master_in));
        FLAG_STRINGS.add(activity.getString(R.string.master_out));

        FLAG_STRINGS.add(activity.getString(R.string.cricket));
        FLAG_STRINGS.add(activity.getString(R.string.normal));
        FLAG_STRINGS.add(activity.getString(R.string.cut_throat));

        FLAG_STRINGS.add(activity.getString(R.string.turns));
        FLAG_STRINGS.add(activity.getString(R.string.open));

        FLAG_STRINGS.add(activity.getString(R.string.shanghai));

        FLAG_STRINGS.add(activity.getString(R.string.baseball));

        FLAG_STRINGS.add(activity.getString(R.string.killer));
        FLAG_STRINGS.add(activity.getString(R.string.one_killer));
        FLAG_STRINGS.add(activity.getString(R.string.killers));

        APP_STRINGS.add(activity.getString(R.string.life));
        APP_STRINGS.add(activity.getString(R.string.lives));
        APP_STRINGS.add(activity.getString(R.string.start));
        APP_STRINGS.add(activity.getString(R.string.end));
        APP_STRINGS.add(activity.getString(R.string.inning));
        APP_STRINGS.add(activity.getString(R.string.innings));
    }

    public static ArrayList<String> getFlagString() {
        return FLAG_STRINGS;
    }

    public static String getAppString(AppStringId id) {
        return APP_STRINGS.get(id.ordinal());
    }



    public static void getLocStr() {
        R.string.class.getFields();
    }
}
