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
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

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

    private static final String TABLE_PLAYER_DATA = "player_data";
    private static final String COLUMN_PLAYER_ID = "player_id";
    private static final String COLUMN_TIME = "game_time";
    private static final String COLUMN_GAME_MODE = "game_mode";
    private static final String COLUMN_WIN_LOSS = "win_loss";
    private static final String COLUMN_GAME_THROWS = "game_throws";


    private static final String TABLE_PLAYER_STATS = "player_stats";
    private static final String COLUMN_GAME_ID              = "game_id";
    private static final String COLUMN_GAME_TIME            = "game_time";
    private static final String COLUMN_TOTAL_THROWS         = "total_throws";
    private static final String COLUMN_TOTAL_SCORE          = "total_score";
    private static final String COLUMN_HITS                 = "hits";
    private static final String COLUMN_MARKS                = "marks";

    private static final String COLUMN_BEST_OUT             = "best_out";
    private static final String COLUMN_HIGH_SCORE_GAME      = "high_score_game";
    private static final String COLUMN_HIGH_SCORE_ROUND     = "high_score_round";
    private static final String COLUMN_HIGH_SCORE_CRICKET   = "high_score_cricket";
    private static final String COLUMN_HIGH_SCORE_SHANGHAI  = "high_score_shanghai";
    private static final String COLUMN_HIGH_SCORE_BASEBALL  = "high_score_baseball";

    private static final String COLUMN_SCORE_60_99          = "score_60_99";
    private static final String COLUMN_SCORE_100_139        = "score_100_139";
    private static final String COLUMN_SCORE_140_179        = "score_140_179";
    private static final String COLUMN_SCORE_180S           = "score_180s";

    private static final String COLUMN_DUB_BULLS            = "dub_bulls";
    private static final String COLUMN_SINGLE_BULLS         = "single_bulls";
    private static final String COLUMN_TRIPLES              = "triples";
    private static final String COLUMN_DOUBLES              = "doubles";
    private static final String COLUMN_SINGLES              = "singles";

    private static final String COLUMN_SHANGHAIS            = "shanghais";
    private static final String COLUMN_KILLS                = "kills";

    private static final String COLUMN_BASEBALL_INNINGS     = "baseball_innings";
    private static final String COLUMN_BASEBALL_BEST_INNING = "baseball_best_inning";
    private static final String COLUMN_BASEBALL_RUNS        = "baseball_runs";
    private static final String COLUMN_BASEBALL_BASES       = "baseball_bases";
    private static final String COLUMN_GRAND_SLAMS          = "grand_slams";
    private static final String COLUMN_HOME_RUNS            = "home_runs";

    private static final String COLUMN_BEST_NUMBER          = "best_number";
    private static final String COLUMN_WORST_NUMBER         = "worst_number";

    /** helper class for sqlite */
    private DBHelper mHelper;

    /** sqlite database */
    private SQLiteDatabase mDatabase;

    /** the timestamp of the last saved game */
    private String mLastSavedGameTime;

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
        mHelper = new DBHelper(mContext);
        mDatabase = mHelper.getWritableDatabase();
        return mDatabase;
    }

    /** close the database and return handle */
    public void closeDB() {
        mHelper.close();
        mDatabase = null;
    }

    /** Execute SQL command and catch and return any errors */
    private int execSQL(String sql) {
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

        closeDB();

        return CODE_SUCCESS;
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
        String sql2 = "DELETE FROM " + TABLE_PLAYER_DATA + " WHERE " +
                COLUMN_PLAYER_ID + "=" +  playerId;

        SQLiteDatabase db = openDB();
        db.execSQL(sql1);
        db.execSQL(sql2);
        closeDB();
    }

    /** save the game in gdata to the database. */
    public void saveGame(GameData gdata) {
        // if there are no game throws dont save
        if(gdata.gthrows().isEmpty()) return;

        // get players names and ids in current game
        ArrayList<Integer> playersIds = new ArrayList<>();
        ArrayList<Integer> playersIndexes = new ArrayList<>();
        ArrayList<Boolean> isTeam = new ArrayList<>();
        ArrayList<Boolean> isTeammate = new ArrayList<>();
        for(int i = 0; i < MAX_PLAYERS; i++) {
            if(!gdata.player(i).isActive() || gdata.player(i).isBot()) continue;

            playersIds.add(getPlayerId(gdata.player(i).getName()));
            playersIndexes.add(i);
            isTeammate.add(false);
            isTeam.add(gdata.player(i).isTeam());

            if(gdata.player(i).isTeam()) {
                playersIds.add(getPlayerId(gdata.player(i).getTeammateName()));
                playersIndexes.add(i);
                isTeammate.add(true);
                isTeam.add(gdata.player(i).isTeam());
            }
        }

        if(playersIds.isEmpty()) return;

        mLastSavedGameTime = dateFromHours(0);

        for(int i = 0; i < playersIds.size(); i++) {
            if(playersIds.get(i) == -1) continue;

            if(gdata.isKiller() && isTeam.get(i))
                continue;

            execSQL("INSERT INTO " + TABLE_PLAYER_DATA + " VALUES (" +
                    playersIds.get(i) + ", " +
                    mLastSavedGameTime + ", " +
                    sqlText(gdata.flags().getGameMode().flagString()) + ", " +
                    (gdata.getWinnerIndex() == playersIndexes.get(i) ? 1 : 0) + ", " +
                    sqlText(gdata.gthrows().getPlayerThrowList
                            (playersIndexes.get(i),
                                    isTeam.get(i) ? (isTeammate.get(i) ? 2 : 1) : 0).getThrowsString()) + ")");
        }
    }

    /** delete the last game saved to the database */
    public void deleteLastGame() {
        String sql2 = "DELETE FROM " + TABLE_PLAYER_DATA + " WHERE " +
                COLUMN_TIME + "=" +  mLastSavedGameTime;
        execSQL(sql2);
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
    public PlayerStats getPlayerStats(String playerName, int sinceHours, GameFlagSet flags) {
        return new PlayerStats
                (playerName, getPlayerThrows(getPlayerId(playerName), sinceHours, flags));
    }

    /** @return a timestamp from a number of hours */
    public String dateFromHours(int sinceHours) {
        return sqlText(Helper.getTimeStamp(sinceHours));
    }

    /** @return a list of players game throws */
    public GameThrowList getPlayerThrows(int playerId, int sinceHours, GameFlagSet flags) {
        String sql = "SELECT * FROM " + TABLE_PLAYER_DATA + " WHERE "
                + COLUMN_PLAYER_ID + "=" + playerId + " AND " +
                COLUMN_TIME + " BETWEEN " +
                dateFromHours(sinceHours) + " AND " + dateFromHours(0);

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
            sql += " AND " + COLUMN_GAME_MODE + " LIKE " + likeStr;

        Cursor c; boolean opened = false;

        if(mDatabase == null) {
            c = openDB().rawQuery(sql, null);
            opened = true;
        } else c = mDatabase.rawQuery(sql, null);

        StringBuilder strThrows = new StringBuilder();

        // "[CRICKET]-[7, 2, 14];[CRICKET][NORMAL]
        GameThrowList throwList = new GameThrowList();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            long time = java.sql.Timestamp.valueOf(c.getString(1)).getTime();
            GameFlagSet gameMode = GameFlagSet.fromString(c.getString(2));
            boolean win = c.getInt(3) > 0;
            throwList.append(new GameThrowList(time, gameMode, win, c.getString(4)));
        }


        c.close();

        if(opened) closeDB();

        return throwList;
    }

    /**
     * a helper class for interacting with a sqlite database
     */
    private static class DBHelper extends SQLiteOpenHelper {
        // create table SQL command strings
        private static final int DATABASE_VERSION = 12;
        private static final String SQL_CREATE_TABLE_PLAYERS =
                "CREATE TABLE " + TABLE_PLAYERS + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME + " TEXT UNIQUE)";

        private static final String SQL_CREATE_TABLE_PLAYER_DATA =
                "CREATE TABLE " + TABLE_PLAYER_DATA + " (" +
                        COLUMN_PLAYER_ID   + " INTEGER,"   +
                        COLUMN_TIME        + " TEXT, "     +
                        COLUMN_GAME_MODE   + " TEXT, "     +
                        COLUMN_WIN_LOSS    + " BOOLEAN, "  +
                        COLUMN_GAME_THROWS + " TEXT)";

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create tables
            db.execSQL(SQL_CREATE_TABLE_PLAYERS);
            db.execSQL(SQL_CREATE_TABLE_PLAYER_DATA);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if(oldVersion == 11) upgradeV11toV12(db);
        }

        private void upgradeV11toV12(SQLiteDatabase db) {
            db.execSQL("UPDATE " + TABLE_PLAYER_DATA + " SET " + COLUMN_GAME_MODE + " = REPLACE(" +
                    COLUMN_GAME_MODE + ", '_01', 'X01')");
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
