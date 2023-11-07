package com.gworks.dartscape.data;

import static com.gworks.dartscape.data.GameFlags.GameFlag.ALL_GAMES;
import static com.gworks.dartscape.data.GameFlags.GameFlag.BASEBALL;
import static com.gworks.dartscape.data.GameFlags.GameFlag.CRICKET;
import static com.gworks.dartscape.data.GameFlags.GameFlag.KILLER;
import static com.gworks.dartscape.data.GameFlags.GameFlag.SHANGHAI;
import static com.gworks.dartscape.data.GameFlags.GameFlag.X01;
import static com.gworks.dartscape.data.Stats.StatId.*;

import com.gworks.dartscape.R;
import com.gworks.dartscape.main.DartScapeActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class Stats {

    public enum StatId {
        GAMES_PLAYED,
        TOTAL_WINS,
        WIN_AVG,

        MARK_AVG,
        SCORE_AVG,

        BEST_MARKS,
        BEST_SCORE,

        SCORE_180s,
        SCORE_140_179,
        SCORE_100_139,
        SCORE_60_99,

        BULLSEYES,
        DOUBLE_BULLS,
        SINGLE_BULLS,
        HIT_TRIPLES,
        HIT_DOUBLES,
        HIT_SINGLES,

        BEST_OUT,

        CRICKET_HIGH,
        BEST_NUM,
        WORST_NUM,
        SHANGHAI_HIGH,
        SHANGHAIS,

        BASEBALL_HIGH,
        BEST_INNING,
        RUNS_INNING,
        BASES_INNING,
        HOME_RUNS,
        GRAND_SLAMS,

        KILLS,

        BEST_GAME,
        TOTAL_SCORE;


        public static StatId fromIndex(int index) {
            for(StatId id : StatId.values()) {
                if(id.ordinal() == index) return id;
            }

            return StatId.GAMES_PLAYED;
        }
    }

    private static ArrayList<String> STATS_STRINGS;

    public static final int STAT_TYPE_INT    = 1;
    public static final int STAT_TYPE_FLOAT  = 2;
    public static final int STAT_TYPE_STRING = 3;

    private static ArrayList<Integer> STAT_TYPE;
    private static ArrayList<Boolean> STAT_MAJOR;

    private static ArrayList<StatId> ALL_STATS_IDS;
    private static ArrayList<ArrayList<StatId>> GAME_STATS_IDS;
    private static ArrayList<ArrayList<StatId>> GAME_RESULT_STATS_IDS;

    public static void init(DartScapeActivity activity) {
        STATS_STRINGS = new ArrayList<>();
        STAT_TYPE = new ArrayList<>();
        STAT_MAJOR = new ArrayList<>();
        GAME_STATS_IDS = new ArrayList<>();
        GAME_RESULT_STATS_IDS = new ArrayList<>();

        addStat(activity.getString(R.string.games_played), STAT_TYPE_INT, true);
        addStat(activity.getString(R.string.total_wins), STAT_TYPE_INT, false);
        addStat(activity.getString(R.string.win_average), STAT_TYPE_FLOAT,  false);
        addStat(activity.getString(R.string.mark_avg),   STAT_TYPE_FLOAT,  false);
        addStat(activity.getString(R.string.score_avg),   STAT_TYPE_FLOAT,  false);

        // index 8
        addStat(activity.getString(R.string.best_marks), STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.best_score), STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.score_180s),   STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.score_140_179), STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.score_100_139), STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.score_60_99), STAT_TYPE_INT,    false);

        // index 13
        addStat(activity.getString(R.string.bullseyes),     STAT_TYPE_INT,    true);
        addStat(activity.getString(R.string.double_bullseyes),  STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.single_bullseyes),  STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.hit_triples),   STAT_TYPE_INT,    true);
        addStat(activity.getString(R.string.hit_doubles),   STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.hit_singles),   STAT_TYPE_INT,    false);

        // index 17
        addStat(activity.getString(R.string.best_out),  STAT_TYPE_INT,    false);

        addStat(activity.getString(R.string.cricket_high),  STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.best_num),        STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.worst_num),       STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.shanghai_high), STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.shanghais),     STAT_TYPE_INT,    false);

        addStat(activity.getString(R.string.baseball_high), STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.best_inning), STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.runs_per_inning), STAT_TYPE_FLOAT,    false);
        addStat(activity.getString(R.string.bases_per_inning), STAT_TYPE_FLOAT,    false);
        addStat(activity.getString(R.string.home_runs), STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.grand_slams), STAT_TYPE_INT,    false);

        addStat(activity.getString(R.string.kills),         STAT_TYPE_INT,    false);

        addStat(activity.getString(R.string.best_game), STAT_TYPE_INT,    false);
        addStat(activity.getString(R.string.total_score), STAT_TYPE_INT,    false);


        // create game specific stat lists
        ALL_STATS_IDS = new ArrayList<>();
        ALL_STATS_IDS.addAll(Arrays.asList(values()).subList(0, values().length - 2));

        for (int i = 0; i < 5; i++) {
            GAME_STATS_IDS.add(new ArrayList<>());

            if(i == 0) { // 01
                for (int j = 0; j < 17; j++)
                    GAME_STATS_IDS.get(i).add(StatId.fromIndex(j));

                GAME_STATS_IDS.get(i).add(11, BEST_OUT);
            } else if(i == 1) { // cricket
                for (int j = 0; j < 17; j++)
                    GAME_STATS_IDS.get(i).add(StatId.fromIndex(j));
                GAME_STATS_IDS.get(i).add(3, BEST_GAME);
                GAME_STATS_IDS.get(i).add(BEST_NUM);
                GAME_STATS_IDS.get(i).add(WORST_NUM);
            } else if(i == 2) { // shanghai
                for (int j = 0; j < 17; j++)
                    GAME_STATS_IDS.get(i).add(StatId.fromIndex(j));
                GAME_STATS_IDS.get(i).add(3, SHANGHAIS);
                GAME_STATS_IDS.get(i).add(4, BEST_GAME);
                GAME_STATS_IDS.get(i).add(BEST_NUM);
                GAME_STATS_IDS.get(i).add(WORST_NUM);
            } else if (i == 3) { // baseball
                for (int j = 0; j < 3; j++)
                    GAME_STATS_IDS.get(i).add(StatId.fromIndex(j));
                GAME_STATS_IDS.get(i).add(BEST_GAME);
                GAME_STATS_IDS.get(i).add(BEST_INNING);
                GAME_STATS_IDS.get(i).add(RUNS_INNING);
                GAME_STATS_IDS.get(i).add(BASES_INNING);
                GAME_STATS_IDS.get(i).add(GRAND_SLAMS);
                GAME_STATS_IDS.get(i).add(HOME_RUNS);
                GAME_STATS_IDS.get(i).add(HIT_TRIPLES);
                GAME_STATS_IDS.get(i).add(HIT_DOUBLES);
                GAME_STATS_IDS.get(i).add(HIT_SINGLES);

            } else { // killer
                for (int j = 0; j < 3; j++)
                    GAME_STATS_IDS.get(i).add(StatId.fromIndex(j));
                GAME_STATS_IDS.get(i).add(KILLS);
            }
        }

        for (int i = 0; i < 5; i++) {
            GAME_RESULT_STATS_IDS.add(new ArrayList<>());

            if (i == 0) { // X01
                GAME_RESULT_STATS_IDS.get(i).add(SCORE_AVG);
                GAME_RESULT_STATS_IDS.get(i).add(BEST_SCORE);
                GAME_RESULT_STATS_IDS.get(i).add(MARK_AVG);
                GAME_RESULT_STATS_IDS.get(i).add(BEST_MARKS);
                GAME_RESULT_STATS_IDS.get(i).add(TOTAL_SCORE);
                GAME_RESULT_STATS_IDS.get(i).add(HIT_TRIPLES);
            } else if (i == 1) { // cricket
                GAME_RESULT_STATS_IDS.get(i).add(MARK_AVG);
                GAME_RESULT_STATS_IDS.get(i).add(BEST_MARKS);
                GAME_RESULT_STATS_IDS.get(i).add(HIT_TRIPLES);
                GAME_RESULT_STATS_IDS.get(i).add(SCORE_AVG);
                GAME_RESULT_STATS_IDS.get(i).add(BEST_SCORE);
                GAME_RESULT_STATS_IDS.get(i).add(TOTAL_SCORE);
            } else if (i == 2) { // SHANGHAI
                GAME_RESULT_STATS_IDS.get(i).add(TOTAL_SCORE);
                GAME_RESULT_STATS_IDS.get(i).add(SCORE_AVG);
                GAME_RESULT_STATS_IDS.get(i).add(BEST_SCORE);
                GAME_RESULT_STATS_IDS.get(i).add(MARK_AVG);
                GAME_RESULT_STATS_IDS.get(i).add(BEST_MARKS);
                GAME_RESULT_STATS_IDS.get(i).add(HIT_TRIPLES);
            } else if (i == 3) { // baseball
                GAME_RESULT_STATS_IDS.get(i).add(TOTAL_SCORE);
                GAME_RESULT_STATS_IDS.get(i).add(BEST_INNING);
                GAME_RESULT_STATS_IDS.get(i).add(BASES_INNING);
                GAME_RESULT_STATS_IDS.get(i).add(RUNS_INNING);
                GAME_RESULT_STATS_IDS.get(i).add(HIT_TRIPLES);
                GAME_RESULT_STATS_IDS.get(i).add(HOME_RUNS);
                GAME_RESULT_STATS_IDS.get(i).add(GRAND_SLAMS);
            } else { // killer
                GAME_RESULT_STATS_IDS.get(i).add(KILLS);
            }
        }
    }

    private static void addStat(String name, int type, boolean major) {
        STATS_STRINGS.add(name);
        STAT_TYPE.add(type);
        STAT_MAJOR.add(major);
    }

    public static int getStatCount() {
        return STATS_STRINGS.size();
    }

    public static ArrayList<String> getAllStatStrings() {
        return new ArrayList<>(STATS_STRINGS);
    }

    public static ArrayList<String> getGameStatStrings(GameFlags.GameFlag game) {
        if(game.equals(ALL_GAMES)) return getAllStatStrings();

        ArrayList<String> list = new ArrayList<>();
        ArrayList<StatId> gameStatIds = GAME_STATS_IDS.get(getIndex(game));
        for (int i = 0; i < gameStatIds.size(); i++) {
            list.add(STATS_STRINGS.get(gameStatIds.get(i).ordinal()));
        }
        return list;
    }

    public static ArrayList<StatId> getGameStatIds(GameFlags.GameFlag game) {
        if(game.equals(ALL_GAMES)) return ALL_STATS_IDS;
        return GAME_STATS_IDS.get(getIndex(game));
    }

    public static ArrayList<StatId> getGameResultsStatIds(GameFlags.GameFlag game) {
        return GAME_RESULT_STATS_IDS.get(getIndex(game));
    }

    public static String getStatString(StatId id){
        return STATS_STRINGS.get(id.ordinal());
    }

    public static int getStatType(StatId id) {
        return STAT_TYPE.get(id.ordinal());
    }

    public static boolean getStatMajor(StatId id){
        return STAT_MAJOR.get(id.ordinal());
    }

    private static int getIndex(GameFlags.GameFlag flag) {
        if(flag.equals(X01))      return 0;
        if(flag.equals(CRICKET))  return 1;
        if(flag.equals(SHANGHAI)) return 2;
        if(flag.equals(BASEBALL)) return 3;
        if(flag.equals(KILLER))   return 4;

        return -1;
    }

    public static StatId getStatId(String name) {
        return StatId.fromIndex(STATS_STRINGS.indexOf(name));
    }
}
