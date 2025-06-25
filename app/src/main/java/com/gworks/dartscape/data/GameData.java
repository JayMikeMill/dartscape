/*
 * Copyright (C) 2023 G Works
 */

package com.gworks.dartscape.data;

import static com.gworks.dartscape.main.Globals.*;

import com.gworks.dartscape.util.Helper;
import com.gworks.dartscape.util.UserPrefs;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The main data class for Dartscape containing all the game data and
 * also other data used by most fragment classes and used closely
 * with {@link com.gworks.dartscape.logic.GameLogic}
 */

public class GameData {

    //
    // Members Begin
    //

    /** the player's in the current game */
    private final Player[] mPlayers = new Player[MAX_PLAYERS];

    /** list of {@link GameThrow}s in current came */
    GameThrowList mGameThrows = new GameThrowList();

    /** all the game options and settings */
    private final GameFlagSet mGameFlags = new GameFlagSet();

    /** has first game started? */
    private boolean mIsGameStarted;

    /** new game turn order defined in setting/user prefs */
    private int mNewGameOrder;

    /** game legs */
    private int mLegs = 0;

    /** game sets */
    private int mSets = 0;

    /** the index of the current player throwing */
    private int mCurrentTurn = 1;

    /** the number of throws in the current turn */
    private int mTurnThrows;

    /** last game winner */
    private int mWinner = NO_PLAYER;

    /** did last game winner win a set? */
    private boolean mIsSetWinner = false;

    /** did last game winner win a match? */
    private boolean mIsMatchSetWinner = false;

    // Cricket game data

    /** cricket sheet game hits */
    private int[][] mCricketSheet;

    /** is Cricket number open in this game? */
    private boolean[] mIsNumberOpen;

    /** Cricket point spread limit, 0 = no limit */
    private int mCricketSpreadLimit = 0;

    // Shanghai game data

    /** current Shanghai number in game */
    private int mShanghaiNumber   = 1;

    /** Shanghai number to start at */
    private int mShangStartNumber = 1;

    /** Shanghai number to finish at */
    private int mShangEndNumber   = 20;

    // Baseball game data

    /** the current inning in Baseball */
    private int mBaseballInning = 1;

    /** the number of inning in Baseball */
    private int mBaseballInnings = 9;

    /** the current players on base in Baseball */
    private boolean[] mBaseballOnBase = { false, false, false, false };

    /** the current number of outs in Baseball innings */
    private int mBaseballOuts = 0;

    private ArrayList<boolean[]> mUndoBases;
    private ArrayList<Integer> mUndoOuts;

    // Killer game data

    /** Killer lives to start at to start at */
    private int mKillerStartLives = 3;

    /** the current active killer in game. Used to track stats */
    private int mActiveKiller = NO_PLAYER;


    //
    // Methods begin
    //

    /** construct game settings with default values */
    public GameData() {
        init();
    }

    /** initializes all data to default values */
    public void init() {
        // init players array
        for (int i = 0; i < MAX_PLAYERS; i++) mPlayers[i] = new Player();

        // set new game order from user preferences
        setNewGameOrder(UserPrefs.getNewGameOrder());

        // init cricket sheet array
        initCricketSheet();
    }

    /** @return the game player at index */
    public Player player(int index) {
        return mPlayers[index];
    }

    /** @return the current player throwing. */
    public Player throwingPlayer() {
        return player(getCurrentTurn());
    }

    /** @return the {@link GameFlagSet} for this game */
    public GameFlagSet flags() {
        return mGameFlags;
    }

    /** @return the {@link GameFlags.GameFlag} for this game */
    public GameFlags.GameFlag getGameFlag() {
        return flags().getGameFlag();
    }

    /** @return the {@link GameThrowList} for this game */
    public GameThrowList gthrows() {
        return mGameThrows;
    }

    /** add a {@link GameThrow} to this games {@link GameThrowList} */
    public void addGameThrow(GameThrow gthrow) {
        gthrow.setGameMode(flags().getGameMode());
        gthrows().add(gthrow);
    }

    /** @return the player's stats at index  */
    public PlayerStats stats(int index) {
        PlayerStats stats = gthrows().getPlayerThrowList(index).getStats();
        stats.setName(player(index).getName());
        return stats;
    }

    /** initializes all players win count to 0 */
    public void initWins() {
        for(int i = 0; i < MAX_PLAYERS; i++)
            player(i).setWins(0);
    }

    /** initializes all players win count to 0 */
    public void initSetWins() {
        for(int i = 0; i < MAX_PLAYERS; i++)
            player(i).setSetWins(0);
    }

    /** set when a new game has started */
    public void setGameStarted(boolean started) {
        mIsGameStarted = started;
    }

    /** @return true if there is a game in progress */
    public boolean isGameGoing() {
        return mIsGameStarted && (!gthrows().isEmpty() || isLegs());
    }

    /** initializes Cricket game sheet and data */
    public void initCricketSheet() {
        // init arrays
        mCricketSheet = new int[MAX_PLAYERS][NUMBER_COUNT];
        mIsNumberOpen = new boolean[NUMBER_COUNT];

        // set all cricket sheet to 0s
        for (int i = 0; i < MAX_PLAYERS; i++)
            for (int j = 0; j < NUMBER_COUNT; j++)
                mCricketSheet[i][j] = 0;

        // set all numbers to open
        for (int i = 0; i < NUMBER_COUNT; i++)
            mIsNumberOpen[i] = true;
    }

    /** @return true if current game is an 01 game */
    public boolean is01() {
        return flags().get(GameFlags.GameFlag.X01);
    }

    /** @return true if current game is Cricket */
    public boolean isCricket() {
        return flags().get(GameFlags.GameFlag.CRICKET);
    }

    /** @return true if current game is Cricket cut throat*/
    public boolean isCutThroat() {
        return flags().get(GameFlags.GameFlag.CRICKET_CUT_THROAT);
    }

    /** @return true if current game is Shanghai */
    public boolean isShanghai() {
        return flags().get(GameFlags.GameFlag.SHANGHAI);
    }

    /** @return true if current game is killer */
    public boolean isKiller() {
        return flags().get(GameFlags.GameFlag.KILLER);
    }

    /** @return true if current game is baseball */
    public boolean isBaseball() {
        return flags().get(GameFlags.GameFlag.BASEBALL);
    }

    /** @return true is the current game is in open scoring mode.
     *  in open scoring mode players do not have to end turn, but the
     *  game stats cannot be tracked and will not be saved. Only cricket
     *  has open scoring as of now. */
    public boolean isOpenScoring() {
        return flags().get(GameFlags.GameFlag.CRICKET_OPEN);
    }

    /** @return true if the current game is in turn based mode */
    public boolean isTurnBased() {
        return !(isOpenScoring() || isKillerStarted());
    }


    /** @return The game type in game flags as a string.
     * (e.g "CRICKET - CUT-THROAT") used for ui. */
    public String getGameTypeString() {
        // get a pre-string from game flags
        String gameType = flags().getGameModeString();

        // update game flags game mode string with GameData data
        if(isCricket()) {
            if(getCricketSpreadLimit() > 0) gameType += " - " + getCricketSpreadLimit();
            if(isOpenScoring()) gameType += " - " + GameFlags.getUiText(GameFlags.GameFlag.CRICKET_OPEN) +
                    " (NO STATS)";
        }
        else if(isShanghai())
            gameType += " - # " + getShanghaiStartNumber() + " to # " + getShanghaiEndNumber();

        else if(isKiller())
            gameType += " - " + getKillerStartLives() + (getKillerStartLives()  == 1 ? " LIFE" : " LIVES") ;

        return gameType;
    }
    /** @return The name of the player at index to be displayed on ui */
    public String getPlayerUiName(int index) {
        // player not active, no ui name
        if(!player(index).isActive()) return "";

        // get players in-data name
        String name = player(index).getName();

        // update the name for bots and teams
        if(player(index).isBot()) name = name.replace("-", "\n");

        if(!player(index).isTeam()) return name;
        String teammateName = player(index).getTeammateName();
        name += "\n" + teammateName;

        return name;
    }


    /** @return The first index of a player that is active stating at index 0. */
    public int getFirstPlayerActiveIndex() {
        for(int i = 0; i < MAX_PLAYERS; i++) if(player(i).isActive()) return i;
        return NO_PLAYER;
    }

    /** @return The first index of a player that is active stating at index MAX_PLAYERS. */
    public int getLastPlayerActiveIndex() {
        int lastIndex = NO_PLAYER;
        for(int i = 0; i < MAX_PLAYERS; i++) if(player(i).isActive()) lastIndex = i;
        return lastIndex;
    }

    /** @return the number of bots in this game */
    public int getBotCount() {
        int count = 0;
        for (int i = 0; i < MAX_PLAYERS; i++)
            if(player(i).isBot()) count++;

        return count;
    }

    /** @return The number of active players. */
    public int getActivePlayerCount() {
        int count = 0;
        for (int i = 0; i < MAX_PLAYERS; i++)
            if (player(i).isActive()) count++;

        return count;
    }

    /** @return the starting score based on the current game mode */
    public int getStartScore() {
        if (is01()) {
            if (flags().get(GameFlags.GameFlag.X01_301)) return 301;
            if (flags().get(GameFlags.GameFlag.X01_501)) return 501;
            if (flags().get(GameFlags.GameFlag.X01_701)) return 701;
            if (flags().get(GameFlags.GameFlag.X01_901)) return 901;
            if (flags().get(GameFlags.GameFlag.X01_1001)) return 1001;
            if (flags().get(GameFlags.GameFlag.X01_1201)) return 1201;
            if (flags().get(GameFlags.GameFlag.X01_1501)) return 1501;
        }

        else if (isCricket()) return 0;
        else if(isShanghai()) return 0;
        else if(isKiller()) return 0; // not lives. game starts in pre game.

        return 0;
    }

    /** @return true if the current game/match has a set number of legs */
    public boolean isLegs() {
        return getLegs() > 0;
    }

    /** @return true if the current game is a leg race */
    public boolean isLegRace() {
        return isLegs() && getSets() == 0;
    }

    /** @return true if the current game is a set match */
    public boolean isSetMatch() {
        return isLegs() && getSets() > 0;
    }

    /** @return the number of legs set for this game/match. 0 = 1 game, no legs */
    public int getLegs() {
        return mLegs;
    }

    /** set the number of legs set for this game/match. 0 = 1 game, no legs */
    public void setLegs(int legs) {
        mLegs = legs;
        initWins(); initSetWins();
    }

    /** @return the number of sets for this game/match. 0 sets, > 0 legs = legs race */
    public int getSets() {
        return mSets;
    }

    /**get  the number of sets for this game/match. 0 sets, > 0 legs = legs race  */
    public void setSets(int sets) {
        mSets = sets;
    }

    /** @return a string showing the current legs/sets in game/match to bo shown
     *  in the ui. (e.g "BEST OF 3 LEGS - 2 SETS") */
    public String getLegsSetsUiString() {
        // update player score textviews
        String legsSetText = "";
        if(isLegRace())
            legsSetText = "LEG-RACE TO " + getLegs() + (getLegs() == 1 ? " LEG" : " LEGS");
        else if(isSetMatch())
            legsSetText = "BEST OF " + getLegs() + (getLegs() == 1 ? " LEG - " : " LEGS - ")
                    + getSets() + (getSets() == 1 ? " SET" : " SETS");

        return legsSetText;
    }

    /** @return a string showing the number of legs/sets the player at index has won.
     *  shown below player score and in game results.*/
    public String getPlayerLegsSetsUiString(int index) {
        return player(index).getWins() + (isSetMatch() ? "\\" + player(index).getSetWins() : "");
    }

    /** @return The index of current player throwing. */
    public int getCurrentTurn() {
        return mCurrentTurn;
    }

    /** @return the player currently throwing */
    public Player getCurrentPlayer() {
        return player(getCurrentTurn());
    }

    /** @return the current number of throw in the current turn. */
    public int getTurnThrows() {
        return mTurnThrows;
    }

    /** set the current number of throw in the current turn. */
    public void setTurnThrows(int turnThrows) {
        mTurnThrows = turnThrows;
    }

    /** add to the current number of throw in the current turn. */
    public void addTurnThrows(int turnThrows) {
        mTurnThrows += turnThrows;
    }


    /** @return the current number of throw in the current turn. */
    public int getThrowsInRound() {
        if(isBaseball()) return 9999;
        else return 3;
    }

    /** @return true if the current turn  is over. */
    public boolean isTurnOver() {
        return !isBaseball() && getTurnThrows() == 3;
    }

    /** @return The number of rounds played during current game. */
    public int getRoundCount() {
        int roundThrows = ROUND_THROWS * getActivePlayerCount();
        int rounds = (gthrows().size() - (gthrows().size() % roundThrows)) / roundThrows;
        return Math.max(rounds, 0);
    }

    /** Set the current player's turn to player at index. */
    public void setCurrentTurn(int index) {
        if(index > MAX_PLAYERS - 1) index = MAX_PLAYERS - 1;
        if(index < 0) index = 0;

        mCurrentTurn = index;
    }

    /** @return True if the current turn lands on the teammate. */
    public boolean isTeammateTurn() {
        if(isBaseball()) {
            return getBaseballInning() % 2 == 0;
        } else {
            return getRoundCount() % 2 != 0;
        }
    }

    /** @return The winner of the last played game. */
    public Player winner() {
        if(!isWinner()) return null;
        return player(getWinnerIndex());
    }
    
    /** @return The index of winning player. */
    public int getWinnerIndex() {
        return mWinner;
    }

    /** @return True is the current game has been won */
    public boolean isWinner() {
        return mWinner != NO_PLAYER;
    }

    /** @return True if the winner of current game won the set */
    public boolean isSetWinner() {
        return isLegs() && isWinner() && mIsSetWinner;
    }

    /** @return True if the winner of current game won the set match */
    public boolean isMatchSetWinner() {
        return isLegs() && isWinner() && mIsMatchSetWinner;
    }

    /** Set the index of winning player. Update the wins of the player.
     * if legs, game, check for set/match winner and update set wins. */
    public void setWinner(int index) {
        if(index == NO_PLAYER) {
            mWinner = NO_PLAYER;
            mIsSetWinner = false;
            mIsMatchSetWinner = false;
            return;
        }

        // update player wins
        player(index).addWins(1);

        int winnerIndex = index;

        int legsToWin =  isLegRace() ? getLegs() : Math.round(getLegs() / 2f);
        int setsToWin =  Math.round(getSets() / 2f);

        if(isLegs()) {
            for (int i = 0; i < MAX_PLAYERS; i++) {
                if (player(i).getWins() == legsToWin) {
                    winnerIndex = i;
                    if(isLegRace()) mIsMatchSetWinner = true;
                    else { player(i).addSetWins(1); mIsSetWinner = true; }
                }
            }

            if(!isLegRace()) {
                for (int i = 0; i < MAX_PLAYERS; i++) {
                    if (player(i).getSetWins() == setsToWin) {
                        winnerIndex = i;
                        mIsMatchSetWinner = true;
                    }
                }
            }
        }

        mWinner = winnerIndex;
    }

    /** resent the last winners wins and set winner to no players */
    public void undoWinner() {
        if(getWinnerIndex() == NO_PLAYER) return;

        // update player wins
        winner().addWins(-1);
        if(isSetWinner()) winner().addSetWins(-1);
        setWinner(NO_PLAYER);
    }

    /** reorder the players on a new game according to the order set in the settings */
    public void reorderPlayers() {
        if(mNewGameOrder == TURN_ORDER_DONT_CHANGE) return;
        if(getWinnerIndex() == NO_PLAYER) return;

        int[] order = getNewPlayerOrder();

        Player[] oldPlayerOrder = mPlayers.clone();

        for(int i = 0; i < MAX_PLAYERS; i++)
            mPlayers[i] = oldPlayerOrder[order[i]];
    }

    /** set the order to reorder players on a new game. */
    public void setNewGameOrder(int order) {
        mNewGameOrder = order;
    }

    /** retrieve the new order of players according to the new game order
     * set in the settings.  */
    private int[] getNewPlayerOrder() {
        int[] order = new int[MAX_PLAYERS];

        if(mNewGameOrder == TURN_ORDER_WINNER_FIRST)
            order[0] = getWinnerIndex();
        else if(mNewGameOrder == TURN_ORDER_WINNER_TO_WORST)
            order = getScoreOrder(false, true);
        else if(mNewGameOrder == TURN_ORDER_LOSER_FIRST)
            order[0] = getWorstScoreIndex();
        else if(mNewGameOrder == TURN_ORDER_LOSER_TO_BEST)
            order = getScoreOrder(true, true);

        if(mNewGameOrder == TURN_ORDER_WINNER_FIRST ||
                mNewGameOrder == TURN_ORDER_LOSER_FIRST) {
            order[1] = (order[0] + 1) > (MAX_PLAYERS - 1) ? 0 : (order[0] + 1) ;
            order[2] = (order[1] + 1) > (MAX_PLAYERS - 1) ? 0 : (order[1] + 1) ;
            order[3] = (order[2] + 1) > (MAX_PLAYERS - 1) ? 0 : (order[2] + 1) ;
        }

        if(mNewGameOrder == TURN_ORDER_FLIP) {
            order[0] = 3;
            order[1] = 2;
            order[2] = 1;
            order[3] = 0;
        }

        // set inactive players to bottom
        Integer[] iOrder = Helper.arrayIntToInteger(order);
        Arrays.sort(iOrder, (a, b) -> !player(a).isActive() ? 1 : !player(b).isActive() ? -1 : 0);

        return Helper.arrayIntegerToInt(iOrder);
    }


    //
    // 01 Methods
    //

    /** @return the minimum out */
    public int x01getMinOut() {
        int minOut = 1;
        if(flags().get(GameFlags.GameFlag.X01_DOUBLE_OUT)) minOut = 2;
        if(flags().get(GameFlags.GameFlag.X01_TRIPLE_OUT)) minOut = 3;
        if(flags().get(GameFlags.GameFlag.X01_MASTER_OUT)) minOut = 2;

        return minOut;
    }

    /** @return the first 01 in or out stating at a triple */
    public int x01getFirstIn() {
        boolean[] insOuts = x01GetInsOuts(true);
        for(int i = insOuts.length - 1; i >= 0; i--)
            if(insOuts[i]) return i+1;
        return 1;
    }

    /** @return the active out multipliers in game */
    public boolean[] x01GetInsOuts(boolean ins) {
        // check for 01 in/out. if not, return
        boolean[] insOuts = new boolean[4];

        // if player has not got a score yet, check for in
        if(ins) {
            if(flags().get(GameFlags.GameFlag.X01_STRAIGHT_IN))
                insOuts = new boolean[] {true, true, true, false};
            else if(flags().get(GameFlags.GameFlag.X01_DOUBLE_IN)) insOuts[1] = true;
            else if(flags().get(GameFlags.GameFlag.X01_TRIPLE_IN)) insOuts[2] = true;
            else if(flags().get(GameFlags.GameFlag.X01_MASTER_IN))
                insOuts = new boolean[] {false, true, true, true};
        } else {
            if(flags().get(GameFlags.GameFlag.X01_STRAIGHT_OUT))
                insOuts = new boolean[] {true, true, true, false};
            else if(flags().get(GameFlags.GameFlag.X01_DOUBLE_OUT)) insOuts[1] = true;
            else if(flags().get(GameFlags.GameFlag.X01_TRIPLE_OUT)) insOuts[2] = true;
            else if(flags().get(GameFlags.GameFlag.X01_MASTER_OUT))
                insOuts = new boolean[] {false, true, true, true};
        }

        return insOuts;
    }

    //
    // Cricket Methods
    //

    /** Retrieve the number of hits on a player's number */
    public int getSheetHits(int playerIndex, int numberIndex) {
        if(!isCricket()) return 0;

        return mCricketSheet[playerIndex][numberIndex];
    }

    /** Sets the number of hits on a player's number */
    public void setSheetHits(int playerIndex, int numberIndex, int hits) {
        if(!isCricket()) return;

        if(!flags().get(GameFlags.GameFlag.CRICKET)) return;
        mCricketSheet[playerIndex][numberIndex] = hits;
        updateNumberOpen(numberIndex);
    }

    /** returns the total marks in the cricket sheet for the player at index. */
    public int getPlayerMarks(int index) {
        if(!isCricket()) return 0;

        int marks = 0;
        for(int i = 0; i < NUMBER_COUNT; i++)
            marks += getSheetHits(index, i);
        return marks;
    }

    /** check if all the players numbers are closed in the cricket sheet */
    public boolean areAllPlayersNumbersClosed(int index) {
        if(!isCricket()) return false;

        for(int i = 0; i < NUMBER_COUNT; i++)
            if(getSheetHits(index, i) < 3) return false;
        return true;
    }

    public boolean isPlayersNumberOpen(int playerIndex, int numberIndex) {
        if(!isCricket()) return true;

        return getSheetHits(playerIndex, numberIndex) < 3;
    }

    public boolean isNumberOpen(int index) {
        if(!isCricket()) return true;

        // 01 games numbers always open
        if(flags().get(GameFlags.GameFlag.X01)) return true;

        return mIsNumberOpen[index];
    }

    public int getCricketSpreadLimit() {
        return mCricketSpreadLimit;
    }

    public int getCricketSpreadLimitIndex() {
        if(getCricketSpreadLimit() == 0) return 0;
        return getCricketSpreadLimit() / 100;
    }

    public void setCricketSpreadLimit(int cap) {
        mCricketSpreadLimit = cap;
    }

    /** @return The index of the first open number. */
    public int getFirstOpenNumber() {
        for (int i = 0; i < NUMBER_COUNT; i++)
            if (mIsNumberOpen[i]) return getNumberFromIndex(i);

        return NO_NUMBER;
    }

    /** @return The index of the first open number. */
    public int getPlayerFirstOpenNumber(int index) {
        for (int i = 0; i < NUMBER_COUNT; i++)
            if (mCricketSheet[index][i] < 3)
                return getNumberFromIndex(i);

        return NO_NUMBER;
    }

    /** Called when a number has been changed. Opens or closes the number
     * in the sheet data. */
    private void updateNumberOpen(int index) {
        boolean open = false;
        for (int i = 0; i < MAX_PLAYERS; i++)
            // if the player is active and the player number is
            // below the, the number is open. Quit checking.
            if (player(i).isActive() &&
                    getSheetHits(i, index) < 3) {
                open = true;
                break;
            }

        mIsNumberOpen[index] = open;
    }

    /** @return  the index from numbers 15 - 20 and 25 for cricket */
    public static int getIndexOfNumber(int number) {
        int index = (number == 25) ? 6 : 20 - number;
        if(index < 0 || index > 6) return NO_NUMBER;
        return index;
    }

    /** @return  the number from index 0 - 6 for cricket */
    public static int getNumberFromIndex(int index) {
        return (index == 6) ? 25 : 20 - index;
    }

    //
    // Shanghai Methods
    //

    /** @return  the current shanghai number */
    public int getShanghaiNumber() {
        return mShanghaiNumber;
    }

    /** set the current shanghai number */
    public void setShanghaiNumber(int number) {
        mShanghaiNumber = number;
    }

    /** @return the number to start the game Shanghai with */
    public int getShanghaiStartNumber() {
        return mShangStartNumber;
    }

    /** set the number to start the game Shanghai with */
    public void setShanghaiStartNumber(int number) {
        mShangStartNumber = number;
    }

    /** @return the number to end the game Shanghai with */
    public int getShanghaiEndNumber() {
        return mShangEndNumber;
    }

    /** set the number to end the game Shanghai with */
    public void setShanghaiEndNumber(int number) {
        mShangEndNumber = number;
    }

    //
    // Baseball Methods
    //

    /** reset all based to empty in Baseball */
    public void resetBaseball() {
        setBaseballInning(1);
        setBaseballOuts(0);
        mUndoBases = new ArrayList<>();
        mUndoOuts  = new ArrayList<>();
        resetBases();
    }

    public ArrayList<boolean[]> undoBases() {
        return mUndoBases;
    }

    public ArrayList<Integer> undoOuts() {
        return mUndoOuts;
    }

    /** @return the number of innings in Baseball */
    public int getBaseballInnings() {
        return mBaseballInnings;
    }

    /** set the number of innings in Baseball */
    public void setBaseballInnings(int innings) {
        mBaseballInnings = innings;
    }

    /** @return the current inning in Baseball */
    public int getBaseballInning() {
        return mBaseballInning;
    }

    /** set the current inning in Baseball */
    public void setBaseballInning(int inning) {
        mBaseballInning = inning;
    }

    /** set the current inning in Baseball */
    public void addBaseballInnings(int innings) {
        mBaseballInning += innings;
    }

    /** @return true there is a player on 1st, 2nd and 3rd */
    public boolean areBasesLoaded() {
        return mBaseballOnBase[1] && mBaseballOnBase[2] && mBaseballOnBase[3];
    }

    /** @return true if a player is on base in Baseball */
    public boolean isPlayerOnBase(int index) {
        return mBaseballOnBase[index];
    }

    /** set if a player is on base in Baseball */
    public void setPlayerOnBase(int index, boolean onBase) {
        mBaseballOnBase[index] = onBase;
    }

    /** set if a player is on base in Baseball */
    public void setPlayersOnBase(boolean[] onBase) {
        mBaseballOnBase = onBase.clone();
    }

    public void resetBases() {
        for (int i = 0; i < 4; i++) {
            mBaseballOnBase[i] = false;
        }
    }
    /** @return all bases in Baseball */
    public boolean[] getPlayersOnBase() {
        return mBaseballOnBase;
    }


    /** @return the number of outs in current inning */
    public int getBaseballOuts() {
        return mBaseballOuts;
    }

    /** set the number of outs in current inning  */
    public void setBaseballOuts(int outs) {
        mBaseballOuts = outs;
    }

    /** add to the number of outs in current inning  */
    public void addBaseballOuts(int out) {
        mBaseballOuts += out;
    }

    //
    // Killer Methods
    //

    /** @return the Player that has the number in the game killer */
    public Player getKillerPlayerFromNumber(int number) {
        for (int i = 0; i < MAX_PLAYERS; i++)
            if(player(i).getKillerNumber() == number) return player(i);

        return null;
    }
    /** @return true if the killer game has started. ( players have selected their numbers) */
    public boolean isKillerStarted() {
        return isKiller() && (player(getLastPlayerActiveIndex()).getKillerNumber() > 0);
    }

    /** @return the number of live to start killer game with */
    public int getKillerStartLives() {
        return mKillerStartLives;
    }

    /** set the number of live to start killer game with */
    public void setKillerStartLives(int lives) {
        mKillerStartLives = lives;
    }

    /** set the players score to the starting number of killer lives */
    public void initKillerLives() {
        for (int i = 0; i < MAX_PLAYERS; i++)
            player(i).setScore(getKillerStartLives());
    }

    /** set's a players killer status to true. if one killer, remove status from other players */
    public void setPlayerKiller(int playerIndex, boolean killer) {
        if(isOneKillerMode())
            for (int i = 0; i < MAX_PLAYERS; i++)
                player(i).setKiller(i == playerIndex && killer);
        else player(playerIndex).setKiller(killer);

        if(killer) setActiveKiller(playerIndex);
        else if(getActiveKillerIndex() == playerIndex) setActiveKiller(NO_PLAYER);
    }

    /** @return true if the Killer game mode is set to one killer */
    public boolean isOneKillerMode() {
        return flags().get(GameFlags.GameFlag.THE_KILLER);
    }

    /** @return the index of the current active killer */
    public int getActiveKillerIndex() {
        return mActiveKiller;
    }


    /** set the active killer to the players at index*/
    public void setActiveKiller(int playerIndex) {
        mActiveKiller = playerIndex;
    }

    //
    // Score methods
    //

    /** @return the total number of triples the player hit this game */
    public int getPlayerTriples(int index) {
        return new PlayerStats(gthrows().getPlayerThrowList(index)).getTriples();
    }

    /** @return True if player at index has the best score
     * (based on game mode). */
    public boolean isPlayerBestScore(int index) {
        return player(index).getScore() == getScoreOrder()[0];
    }

    /** @return The player that has the best score (based on game mode)
     *  out of all active players.*/
    public int getWorstScoreIndex() {
        int lastScoreIndex = MAX_PLAYERS;

        while(true) {
            int worstScore = getScoreOrder()[--lastScoreIndex];
            for (int i = 0; i < MAX_PLAYERS; i++)
                if(player(i).isActive() && player(i).getScore() == worstScore)
                    return i;
        }
    }

    /** Check if the player has the best score out of all
     * active players .*/
    private int[] getPlayerScores() {
        int[] scores = new int[MAX_PLAYERS];
        for (int i = 0; i < MAX_PLAYERS; i++)
            scores[i] = player(i).getScore();

        return scores;
    }
    public int[] getScoreOrder() { return getScoreOrder(false, false); }
    public int[] getScoreOrder(boolean reverse, boolean indexes) {
        boolean descending = !(isCutThroat() || is01());
        if(reverse) descending = !descending;
        boolean finalDescending = descending;

        Integer[] scores = Helper.arrayIntToInteger(getPlayerScores());

        if(indexes) {
            Integer[][] scoresIndexes = new Integer[MAX_PLAYERS][2];
            for(int i = 0; i < MAX_PLAYERS; i++) {
                scoresIndexes[i][0] = i;
                scoresIndexes[i][1] = scores[i];
            }

            Arrays.sort(scoresIndexes, (a, b) -> {
                if(isCricket() && a[1].equals(b[1]))
                    return finalDescending ?
                            -Integer.compare(getPlayerMarks(a[0]), getPlayerMarks(b[0])) :
                             Integer.compare(getPlayerMarks(a[0]), getPlayerMarks(b[0]));

                if(isShanghai() && a[1].equals(b[1]))
                    return finalDescending ?
                            -Integer.compare(getPlayerTriples(a[0]), getPlayerTriples(b[0])):
                             Integer.compare(getPlayerTriples(a[0]), getPlayerTriples(b[0]));

                return finalDescending ? -Integer.compare(a[1], b[1]) :
                                          Integer.compare(a[1], b[1]);
            });

            for(int i = 0; i < MAX_PLAYERS; i++)
                scores[i] = scoresIndexes[i][0];

        } else
            Arrays.sort(scores, (a, b) -> finalDescending ? -Integer.compare(a, b) : Integer.compare(a, b));

        return Helper.arrayIntegerToInt(scores);
    }
}
