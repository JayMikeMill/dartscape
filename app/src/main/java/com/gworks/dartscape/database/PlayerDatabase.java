/*
 * Copyright (C) 2023 G Works
 */

package com.gworks.dartscape.database;

import static com.gworks.dartscape.main.Globals.*;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.GsonBuilder;
import com.gworks.dartscape.BuildConfig;
import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.data.GameFlagSet;
import com.gworks.dartscape.data.GameThrowList;
import com.gworks.dartscape.data.PlayerStats;
import com.gworks.dartscape.util.Helper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Database class for saving and retrieving players and game data, for stats.
 */

public class PlayerDatabase {
    // SQLite return codes
    private static final String SQLITE_HEADER = "SQLite format 3\u0000";

    public static final int CODE_SUCCESS = 1;
    public static final int CODE_ERROR_UNKNOWN = -1;
    public static final int CODE_ERROR_UNIQUE_NAME = 2067;


    private static final String DATABASE_NAME = "DartscoreData"; //Database Name


    // database table structures
    private static final String TABLE_PLAYERS = "players";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";


    private static final String TABLE_GAMES        = "games";
    private static final String COLUMN_GAME_MODE   = "game_mode";
    private static final String COLUMN_GAME_TIME = "game_time";

    private static final String TABLE_STATS = "stats";
    private static final String COLUMN_GAME_ID              = "game_id";
    private static final String COLUMN_PLAYER_ID            = "player_id";
    private static final String COLUMN_WIN_LOSS             = "win_loss";

    private static final String COLUMN_THROWS               = "throws";
    private static final String COLUMN_HITS                 = "hits";
    private static final String COLUMN_MARKS                = "marks";

    private static final String COLUMN_SCORE                = "score";
    private static final String COLUMN_SCORE_180S           = "score_180s";
    private static final String COLUMN_SCORE_140            = "score_140s";
    private static final String COLUMN_SCORE_100            = "score_100s";
    private static final String COLUMN_SCORE_60             = "score_60s";
    private static final String COLUMN_HIGH_ROUND           = "highest_round";

    private static final String COLUMN_DUB_BULLS            = "dub_bulls";
    private static final String COLUMN_SINGLE_BULLS         = "single_bulls";
    private static final String COLUMN_TRIPLES              = "triples";
    private static final String COLUMN_DOUBLES              = "doubles";
    private static final String COLUMN_SINGLES              = "singles";

    private static final String COLUMN_01_OUT               = "x01_out";

    private static final String COLUMN_SHANGHAIED           = "shanghaied";
    private static final String COLUMN_KILLS                = "kills";

    private static final String COLUMN_BASEBALL_INNINGS     = "baseball_innings";
    private static final String COLUMN_BASEBALL_BEST_INNING = "baseball_best_inning";
    private static final String COLUMN_BASEBALL_RUNS        = "baseball_runs";
    private static final String COLUMN_BASEBALL_BASES       = "baseball_bases";
    private static final String COLUMN_GRAND_SLAMS          = "grand_slams";
    private static final String COLUMN_HOME_RUNS            = "home_runs";

    private static final String COLUMN_BEST_NUMBER          = "best_number";
    private static final String COLUMN_WORST_NUMBER         = "worst_number";

    private static final String COLUMN_GAME_THROWS          = "game_throws";

    /** helper class for sqlite */
    private DBHelper mHelper;




    /** sqlite database */
    private SQLiteDatabase mDatabase;

    /** the timestamp of the last saved game */
    private int mLastGameId;

    /** the application context */
    private final Context mContext;

    /** default constructor */
    public PlayerDatabase(Context context) {
        mContext = context;
    }

    /** copy the db file to new location */
    public boolean copyFile(InputStream in, OutputStream out) {
        try {
            if(out == null) { // if loading file, check header
                // check if is valid sqlite file, if not, close streams and return.
                byte[] header = new byte[16];
                if(in.read(header) != header.length)
                { in.close(); return false; }
                if(!new String(header, StandardCharsets.UTF_8).equals(SQLITE_HEADER))
                { in.close(); return false; }

                out = new FileOutputStream(path());

                // write sqlite header
                out.write(header, 0, header.length);
            }

            // Transfer bytes from in to out
            byte[] buf = new byte[1024]; int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);

            in.close(); out.close(); return true;

        } catch (IOException e){
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /** save the db file to location */
    public boolean loadFile(InputStream in) throws IOException {
        return copyFile(in, null);
    }

    /** save the db file to location */
    public void saveFile(OutputStream out) throws IOException {
        InputStream in = new FileInputStream(path());
        copyFile(in, out);
    }

    /** @return The file path of the database */
    public String path() {
        return mContext.getDatabasePath(DATABASE_NAME).getPath();
    }

    /** open the database and return handle */
    public SQLiteDatabase openDB() throws SQLException {
        if(mDatabase == null) {
            mHelper = new DBHelper(mContext);
            mDatabase = mHelper.getWritableDatabase();
        }

        return mDatabase;
    }

    /** close the database and return handle */
    public void closeDB() {
        if(mDatabase == null) return;

        mHelper.close();
        mDatabase = null;
    }

    /** Execute SQL command and catch and return any errors */


    private int execSQL(String sql) {
        boolean dbOpened = mDatabase == null;

        try {
            openDB().execSQL(sql);
        } catch (SQLException e) {
            String msg = Objects.requireNonNull(e.getMessage());
            if (msg.contains("code 2067")) {
                return CODE_ERROR_UNIQUE_NAME;
            }

            if (BuildConfig.DEBUG)
                Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();

            return CODE_ERROR_UNKNOWN;
        }

        if(dbOpened) closeDB();

        return CODE_SUCCESS;
    }

    public int queryInt(String sql) {
        boolean dbOpened = mDatabase == null;
        int result = -1;

        Cursor cur;
        try {
            cur = openDB().rawQuery(sql, null);

        } catch (SQLException e){
            if (BuildConfig.DEBUG)
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();

            return result;
        }

        if (cur.moveToFirst()) result = cur.getInt(0);
        cur.close();
        if(dbOpened) closeDB();

        return result;
    }


    /** helper function for surrounding string in quotes for sql syntax */
    public String sqlText(String str) { return "'" + str + "'"; }

    /** helper function for surrounding string in percent signs for sql syntax */
    public String sqlLike(String str) { return "%" + str + "%"; }

    /** retrieve the name of a player from a given id */
    public ArrayList<String> getAllPlayerNames() {
        ArrayList<Player> players = getAllPlayers();
        ArrayList<String> names = new ArrayList<>();

        for (int i = 0; i < players.size(); i++)
                names.add(players.get(i).mName);

        if(players.isEmpty()) {
            for (int i = 0; i < 4; i++)
                createPlayer(mContext.getString(R.string.player) + (i+1));
            return getAllPlayerNames();
        }

        return names;
    }

    /** retrieve the name of a player from a given id */
    public ArrayList<Player> getAllPlayers() {
        String sql = "SELECT * FROM " + TABLE_PLAYERS;

        Cursor cur = openDB().rawQuery(sql, null);

        ArrayList<Player> players = new ArrayList<>();

        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
            players.add(new Player(cur.getInt(0), cur.getString(1)));

        cur.close();
        closeDB();

        return players;
    }

    /** retrieve the id of a player from a given name */
    public int getPlayerId(String name) {
        String sql = "SELECT " + COLUMN_ID + " FROM " + TABLE_PLAYERS +
                " WHERE " + COLUMN_NAME + "=" +  sqlText(name);

        //for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
        Cursor cursor = openDB().rawQuery(sql, null);

        int id = -1;

        if(cursor.moveToFirst()) id = cursor.getInt(0);

        cursor.close();
        closeDB();

        return id;
    }

    /** add a new player to database with given name */
    public int createPlayer(String name) {
        String sql = "INSERT INTO " + TABLE_PLAYERS +
                "(" + COLUMN_NAME + ")" + " VALUES (" + sqlText(name) + ")";

        return execSQL(sql);
    }

    public void updatePlayerName(String oldName, String newName) {
        String sql = "UPDATE " + TABLE_PLAYERS + " SET " +
                COLUMN_NAME + "=" + sqlText(newName) + " WHERE " +
                COLUMN_NAME + "=" +  sqlText(oldName);

        execSQL(sql);
    }

    /** delete a player from the databased using their name */
    public void deletePlayer(String name) {
        int playerId = getPlayerId(name);

        String sql1 = "DELETE FROM " + TABLE_PLAYERS + " WHERE " +
                COLUMN_NAME + "=" +  sqlText(name);
        String sql2 = "DELETE FROM " + TABLE_STATS + " WHERE " +
                COLUMN_PLAYER_ID + "=" +  playerId;

        SQLiteDatabase db = openDB();
        db.execSQL(sql1);
        db.execSQL(sql2);
        closeDB();
    }

    /** save the game in gdata to the database. */
    public void saveGame(GameData gdata) {
        // if there are no game throws don't save
        if(gdata.gthrows().isEmpty()) return;

        String game_mode = gdata.flags().getGameMode().flagString();
        String game_time = dateFromHours(0);
        int game_id = saveGameInfo(game_mode, game_time);


        for(int i = 0; i < MAX_PLAYERS; i++) {
            if(!gdata.player(i).isActive() || gdata.player(i).isBot()) continue;

            int player_id = getPlayerId(gdata.player(i).getName());
            boolean won   = gdata.getWinnerIndex() == i;
            PlayerStats stats = gdata.stats(i);

            savePlayerStats(game_id, player_id, won, stats);
        }

        mLastGameId = game_id;
    }

    private int saveGameInfo(String mode, String time) {
        openDB();

        execSQL("INSERT INTO " + TABLE_GAMES + " (" + COLUMN_GAME_TIME + ", " + COLUMN_GAME_MODE +
                ") VALUES (" + time + ", " + sqlText(mode) + ")");

        int game_id = queryInt("SELECT last_insert_rowid()");

        closeDB();

        return game_id;
    }

    public void savePlayerStats(int game_id, int player_id, boolean win_loss, PlayerStats stats) {
        String sql = "INSERT INTO " + TABLE_STATS + " VALUES (" +
                game_id + ", " + player_id + ", " + (win_loss ? 1 : 0) + ", " +

                stats.getTotalThrows() + ", " +
                stats.getTotalHits()   + ", " +
                stats.getTotalMarks()  + ", " +

                stats.getTotalScore()  + ", " +
                stats.getScore180s()   + ", " +
                stats.getScore140s()   + ", " +
                stats.getScore100s()   + ", " +
                stats.getScore60s()    + ", " +
                stats.getHighScoreRound() + ", " +

                stats.getDubBulls()    + ", " +
                stats.getSingleBulls() + ", " +
                stats.getTriples()     + ", " +
                stats.getDoubles()     + ", " +
                stats.getSingles()     + ", " +

                stats.getBestOut()     + ", " +
                (stats.Shanghaied() ? 1 : 0)  + ", " +
                stats.getKills()       + ", " +

                stats.getBaseballInnings()     + ", " +
                stats.getBaseballBestInning()  + ", " +
                stats.getBaseballRuns()        + ", " +
                stats.getBaseballBases()       + ", " +
                stats.getGrandSlams()          + ", " +
                stats.getHomeRuns()            + ", " +

                stats.getBestNumber()          + ", " +
                stats.getWorstNumber()         + //", " +
                ")";

        execSQL(sql);
    }


    /** @return a list of all the players stats */
    public ArrayList<PlayerStats> getAllPlayerStats(int sinceHours, GameFlagSet flags) {
        ArrayList<PlayerStats> stats = new ArrayList<>();
        ArrayList<Player> allPlayers = getAllPlayers();

        openDB();

        for(int i = 0; i < allPlayers.size(); i++)
            stats.add(getPlayerStats(allPlayers.get(i).mName, sinceHours, flags));

        closeDB();

        return stats;
    }


    /** @return a single players stats */
    public PlayerStats getPlayerStats(String playerName, int sinceHours, GameFlagSet flags)
    {
        boolean dbOpened = mDatabase == null;
        openDB();

        int playerId = getPlayerId(playerName);

        execSQL("PRAGMA foreign_keys = ON");

        String sql_sub_set = "(SELECT * FROM " +
                TABLE_STATS + " JOIN " + TABLE_GAMES +
                " ON " + TABLE_STATS + "." + COLUMN_GAME_ID + "=" +
                TABLE_GAMES + "." + COLUMN_ID  + " WHERE " +
                COLUMN_PLAYER_ID + "=" + playerId + " AND " +
                COLUMN_GAME_TIME + " BETWEEN " +
                dateFromHours(sinceHours) + " AND " + dateFromHours(0) +
                getGameFlagsLikeString(flags) + ") AS subset";

        PlayerStats stats = new PlayerStats(playerName);
        stats.setName(playerName);

        stats.setGamesPlayed(queryInt
                ("SELECT COUNT(*) FROM " + sql_sub_set));
        stats.setWins(queryInt
                ("SELECT SUM(" + COLUMN_WIN_LOSS + ") FROM " + sql_sub_set));
        stats.setTotalThrows(queryInt
                ("SELECT SUM(" + COLUMN_THROWS + ") FROM " + sql_sub_set));
        stats.setTotalHits(queryInt
                ("SELECT SUM(" + COLUMN_HITS + ") FROM " + sql_sub_set));
        stats.setTotalMarks(queryInt
                ("SELECT SUM(" + COLUMN_MARKS + ") FROM " + sql_sub_set));

        stats.setTotalScore(queryInt
                ("SELECT SUM(" + COLUMN_SCORE + ") FROM " + sql_sub_set));
        stats.setScore180s(queryInt
                ("SELECT SUM(" + COLUMN_SCORE_180S + ") FROM " + sql_sub_set));
        stats.setScore140s(queryInt
                ("SELECT SUM(" + COLUMN_SCORE_140 + ") FROM " + sql_sub_set));
        stats.setScore100s(queryInt
                ("SELECT SUM(" + COLUMN_SCORE_100 + ") FROM " + sql_sub_set));


        stats.setDubBulls(queryInt
                ("SELECT SUM(" + COLUMN_DUB_BULLS + ") FROM " + sql_sub_set));
        stats.setSingleBulls(queryInt
                ("SELECT SUM(" + COLUMN_SINGLE_BULLS + ") FROM " + sql_sub_set));
        stats.setTriples(queryInt
                ("SELECT SUM(" + COLUMN_TRIPLES + ") FROM " + sql_sub_set));
        stats.setDoubles(queryInt
                ("SELECT SUM(" + COLUMN_DOUBLES + ") FROM " + sql_sub_set));
        stats.setSingles(queryInt
                ("SELECT SUM(" + COLUMN_SINGLES + ") FROM " + sql_sub_set));

        stats.setBestOut(queryInt
                ("SELECT MAX(" + COLUMN_01_OUT + ") FROM " + sql_sub_set));
        stats.setShanghais(queryInt
                ("SELECT SUM(" + COLUMN_SHANGHAIED + ") FROM " + sql_sub_set));
        stats.setKills(queryInt
                ("SELECT SUM(" + COLUMN_KILLS + ") FROM " + sql_sub_set));

        stats.setBaseballInnings(queryInt
                ("SELECT SUM(" + COLUMN_BASEBALL_INNINGS + ") FROM " + sql_sub_set));
        stats.setBaseballBestInning(queryInt
                ("SELECT SUM(" + COLUMN_BASEBALL_BEST_INNING + ") FROM " + sql_sub_set));
        stats.setBaseballRuns(queryInt
                ("SELECT SUM(" + COLUMN_BASEBALL_RUNS + ") FROM " + sql_sub_set));
        stats.setBaseballBases(queryInt
                ("SELECT SUM(" + COLUMN_BASEBALL_BASES + ") FROM " + sql_sub_set));
        stats.setGrandSlams(queryInt
                ("SELECT SUM(" + COLUMN_GRAND_SLAMS + ") FROM " + sql_sub_set));
        stats.setHomeRuns(queryInt
                ("SELECT SUM(" + COLUMN_HOME_RUNS + ") FROM " + sql_sub_set));

        stats.setHighScoreGame(queryInt
                ("SELECT MAX(" + COLUMN_SCORE + ") FROM " + sql_sub_set));
        stats.setHighScoreRound(queryInt
                ("SELECT MAX(" + COLUMN_HIGH_ROUND + ") FROM " + sql_sub_set));
        stats.setHighScoreCricket(queryInt
                ("SELECT MAX(" + COLUMN_SCORE + ") FROM " + sql_sub_set +
                        " WHERE " + COLUMN_GAME_MODE + " LIKE " + sqlText("%CRICKET%")));
        stats.setHighScoreShanghai(queryInt
                ("SELECT MAX(" + COLUMN_SCORE + ") FROM " + sql_sub_set +
                        " WHERE " + COLUMN_GAME_MODE + " LIKE " + sqlText("%SHANGHAI%")));
        stats.setHighScoreBaseball(queryInt
                ("SELECT MAX(" + COLUMN_SCORE + ") FROM " + sql_sub_set +
                        " WHERE " + COLUMN_GAME_MODE + " LIKE " + sqlText("%BASEBALL%")));

        stats.setBestNumber(queryInt
                ("SELECT " + COLUMN_BEST_NUMBER + ", COUNT(*) AS freq FROM " + sql_sub_set +
                        " GROUP BY " + COLUMN_BEST_NUMBER + " ORDER BY freq DESC LIMIT 1"));
        stats.setWorstNumber(queryInt
                ("SELECT " + COLUMN_WORST_NUMBER + ", COUNT(*) AS freq FROM " + sql_sub_set +
                        " GROUP BY " + COLUMN_WORST_NUMBER + " ORDER BY freq DESC LIMIT 1"));

        if(dbOpened) closeDB();

        return stats;
    }

    private String getGameFlagsLikeString(GameFlagSet flags) {
        GameFlagSet dbFlags = new GameFlagSet();
        if (flags != null) dbFlags = flags.getGameMode();
        String likeStr = "";
        if (dbFlags.getGameFlag() != null) {
            likeStr = sqlLike(dbFlags.getGameFlag().toString().replace("[", "").replace("]", ""));
            likeStr = likeStr.substring(0, likeStr.length() - 2);
            dbFlags.clear(dbFlags.getGameFlag());
        }
        likeStr += dbFlags.isEmpty() ? "%" : sqlLike(dbFlags.flagString().replace("[", "").replace("]", ""));
        likeStr = likeStr.isEmpty() ? "" : sqlText(likeStr);

        if(!likeStr.isEmpty())
            return " AND " + COLUMN_GAME_MODE + " LIKE " + likeStr;

        return "";
    }

    /** delete the last game saved to the database */
    public void deleteLastGame() {
        String sql = "DELETE FROM " + TABLE_GAMES + " WHERE " + COLUMN_ID + "=" +  mLastGameId;
        execSQL(sql);

        sql = "DELETE FROM " + TABLE_STATS + " WHERE " + COLUMN_GAME_ID + "=" +  mLastGameId;
        execSQL(sql);
    }

    /** @return a timestamp from a number of hours */
    public String dateFromHours(int sinceHours) {
        return sqlText(Helper.getTimeStamp(sinceHours));
    }

    /**
     * a helper class for interacting with a sqlite database
     */
    public static class DBHelper extends SQLiteOpenHelper {
        // create table SQL command strings
        private static final int DATABASE_VERSION = 12;
        private static final String SQL_CREATE_TABLE_PLAYERS =
                "CREATE TABLE " + TABLE_PLAYERS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME + " TEXT UNIQUE)";

        private static final String SQL_CREATE_TABLE_GAMES =
                "CREATE TABLE " + TABLE_GAMES + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"   +
                        COLUMN_GAME_TIME + " TEXT, "     +
                        COLUMN_GAME_MODE   + " TEXT)";

        private static final String SQL_CREATE_TABLE_STATS =
                "CREATE TABLE " + TABLE_STATS + " (" +
                        COLUMN_GAME_ID              + " INTEGER, " +
                        COLUMN_PLAYER_ID            + " INTEGER, " +
                        COLUMN_WIN_LOSS             + " BOOLEAN, " +

                        COLUMN_THROWS               + " INTEGER, " +
                        COLUMN_HITS                 + " INTEGER, " +
                        COLUMN_MARKS                + " INTEGER, " +

                        COLUMN_SCORE                + " INTEGER, " +
                        COLUMN_SCORE_180S           + " INTEGER, " +
                        COLUMN_SCORE_140            + " INTEGER, " +
                        COLUMN_SCORE_100            + " INTEGER, " +
                        COLUMN_SCORE_60             + " INTEGER, " +
                        COLUMN_HIGH_ROUND           + " INTEGER, " +

                        COLUMN_DUB_BULLS            + " INTEGER, " +
                        COLUMN_SINGLE_BULLS         + " INTEGER, " +
                        COLUMN_TRIPLES              + " INTEGER, " +
                        COLUMN_DOUBLES              + " INTEGER, " +
                        COLUMN_SINGLES              + " INTEGER, " +
                        COLUMN_01_OUT               + " INTEGER, " +
                        COLUMN_SHANGHAIED           + " INTEGER, " +
                        COLUMN_KILLS                + " INTEGER, " +
                        COLUMN_BASEBALL_INNINGS     + " INTEGER, " +
                        COLUMN_BASEBALL_BEST_INNING + " INTEGER, " +
                        COLUMN_BASEBALL_RUNS        + " INTEGER, " +
                        COLUMN_BASEBALL_BASES       + " INTEGER, " +
                        COLUMN_GRAND_SLAMS          + " INTEGER, " +
                        COLUMN_HOME_RUNS            + " INTEGER, " +
                        COLUMN_BEST_NUMBER          + " INTEGER, " +
                        COLUMN_WORST_NUMBER         + " INTEGER, " +
                        "FOREIGN KEY(" + COLUMN_GAME_ID + ") REFERENCES " + TABLE_GAMES + "(" + COLUMN_ID + ")" +
                        ")";

        private static final Object dbLock = new Object();
        private static final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create tables
            db.execSQL(SQL_CREATE_TABLE_PLAYERS);
            db.execSQL(SQL_CREATE_TABLE_GAMES);
            db.execSQL(SQL_CREATE_TABLE_STATS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //if(oldVersion == 11) upgradeV11toV12(db);
        }

        private void upgradeV11toV12(SQLiteDatabase db) {
           // db.execSQL("UPDATE " + TABLE_PLAYER_DATA + " SET " + COLUMN_GAME_MODE + " = REPLACE(" +
           //         COLUMN_GAME_MODE + ", '_01', 'X01')");
        }


        public static void runAsync(Runnable task) {
            dbExecutor.execute(() -> {
                synchronized (dbLock) {
                    task.run();
                }
            });
        }

        public static <T> T runSync(Callable<T> task) {
            synchronized (dbLock) {
                try {
                    return task.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public static <T> void runAsyncResult(Callable<T> task, Consumer<T> callback) {
            dbExecutor.execute(() -> {
                T result;
                synchronized (dbLock) {
                    try {
                        result = task.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                // Post result to main thread
                new Handler(Looper.getMainLooper()).post(() -> callback.accept(result));
            });
        }
    }



    @NonNull
    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this, GameData.class);
    }

    /** helper class for saving/retrieving players */
    private static class Player {
        public int mId;
        public String mName;

        public Player(int id, String name) {
            mId = id;
            mName = name;
        }
    }
}