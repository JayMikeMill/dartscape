package com.gworks.dartscape.logic;

import static com.gworks.dartscape.main.Globals.MAX_PLAYERS;
import static com.gworks.dartscape.main.Globals.NO_PLAYER;

import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.data.GameFlags;
import com.gworks.dartscape.data.GameThrow;
import com.gworks.dartscape.data.GameThrowList;
import com.gworks.dartscape.ui.ScoreButton;

import java.util.ArrayList;


public class GameLogic {
    public static final int[] ALL_NUMBERS =
            {20, 1, 18, 4, 13, 6, 10, 15, 2, 17,
                    3, 19, 7, 16, 8, 11, 14, 9, 12, 5 };

    public static final int[] ALL_NUMBERSB = {
            25, 20, 1, 18, 4, 13, 6, 10, 15, 2,
            17, 3, 19, 7, 16, 8, 11, 14, 9, 12, 5
    };

    public static final int[][] ACTIVE_GAME_NUMBERS  = {
            ALL_NUMBERS, // 01
            { 20, 18, 15, 17, 19, 16 }, // Cricket
    };

    public static final int[][] ACTIVE_GAME_NUMBERSB  = {
            ALL_NUMBERSB, // 01
            { 25, 20, 18, 15, 17, 19, 16 }, // Cricket
    };

    GameData mGameData;
    GameLogicListener mListener;
    GameThrow mLastThrow;

    /** game bot */
    private final GameBot mBot;

    public GameLogic(GameData data) {
        mGameData = data;
        mLastThrow = new GameThrow(0, 0, 0, 0);
        mBot = new GameBot(mGameData, this::botThrow);
    }

    public void setGameLogicListener(GameLogicListener listener) {
        mListener = listener;
    }

    /** Initializes the GameDate to start a new game */
    public void initGame() {
        gdata().reorderPlayers();  // reorder players

        // init players variables
        for(int i = 0; i < MAX_PLAYERS; i++) initPlayer(i);

        // init legs/sets
        if(gdata().isMatchSetWinner()) {
            gdata().initWins(); gdata().initSetWins(); }
        else if(gdata().isSetWinner()) gdata().initWins();

        // new game, no winner
        gdata().setWinner(NO_PLAYER);

        // initialize specific game types
        gdata().initCricketSheet();
        gdata().setShanghaiNumber(gdata().getShanghaiStartNumber());
        gdata().resetBaseball();
        gdata().setActiveKiller(NO_PLAYER);

        gthrows().init();  // init throw list
        setPlayersTurn(0);         // set first turn to player 1
        gdata().setTurnThrows(0);  // new round

        gdata().setGameStarted(true);

    }

    /** Initializes player members to start a new game */
    private void initPlayer(int index) {
        gdata().player(index).setScore(gdata().getStartScore());
        gdata().player(index).setKillerNumber(0);
        gdata().player(index).setKiller(false);
    }

    public GameThrow getLastThrow() {
        return mLastThrow;
    }

    private void addGameThrow(int playerIndex, int number, int multi, int score){
        gdata().addGameThrow(new GameThrow(0L, gdata().flags().getGameMode(), false,
                playerIndex, number, multi, score));
        mLastThrow = new GameThrow(gthrows().getLast());
    }

    /** Called when a player or bot hits a cricket button.
     *  adds hit to all games hits, updates cricket button,
     *  and updates score if any. Checks for winner.
     *  @param multi the multiplier hit with (0, 1, 2, 3) */
    public boolean doScore(int playerIndex, int number,  int multi) {
        // player has won, no more hitting
        if (gdata().isWinner()) {
            return false; // player has won
        }

        if(isBaseball()) {
            return doScoreBaseBall(playerIndex, number,  multi);
        } // special logic for killer game
        else if(isKiller()) return doKillerScore(playerIndex, number);

        // sitting on last turn from undo
        if (gdata().getTurnThrows() == 3) {
            addTurnThrows(1); return false;
        }
        // bullseye has no triple, change to double
        if (number == 25 && multi == 3) multi = 2;

        boolean doScore = false;
        int numIndex = GameData.getIndexOfNumber(isCricket() ? number : 20);
        int oldScore = gdata().player(playerIndex).getScore();

        // set the bonus score
        int bonusScore = 0;

        if(is01()) bonusScore = -(number * multi);
        if(isCricket() || isShanghai()) bonusScore = number * multi;

        int newScore = oldScore + bonusScore;

        // true of hit is out
        boolean isOut = multi == 0 || number == 0;
        boolean x01MissedIn = false;
        if (is01()) {
            boolean[] ins = gdata().x01GetInsOuts(true);
            boolean[] outs = gdata().x01GetInsOuts(false);

            // 01 bust
            if (number == -1 || newScore < 0 || (newScore != 0 && newScore < gdata().x01getMinOut()))
            { x01Bust(number); return false; }

            if(!isOut) {
                if (oldScore == gdata().getStartScore() && !(ins[multi - 1] || ins[3] && number == 25)) {
                    bonusScore = 0; multi = 0; newScore = oldScore + bonusScore;
                    x01MissedIn = true;
                }
                else if (newScore == 0 && !((outs[multi - 1]) || outs[3] && number == 25)) {
                    x01Bust(number);
                    return false;
                }
            }
        }

        // cricket score cap
        int spreadLimit = gdata().getCricketSpreadLimit();
        int spreadScoreLimit = 9999;
        boolean doSpread = isCricket() && spreadLimit > 0;
        boolean hitCricketCap = false;
        if(isCricket() && !gdata().isCutThroat() && doSpread && !isOut) {
            for(int i = 0; i < MAX_PLAYERS; i++)
                if(gdata().player(i).isActive() && playerIndex != i &&
                        gdata().player(i).getScore() < spreadScoreLimit)
                    spreadScoreLimit = gdata().player(i).getScore();

            spreadScoreLimit += spreadLimit;

            boolean isPlayerNumOpen = gdata().isPlayersNumberOpen(playerIndex, GameData.getIndexOfNumber(number));

            if (oldScore >= spreadScoreLimit && !isPlayerNumOpen)
            { bonusScore = 0; newScore = oldScore + bonusScore; hitCricketCap = true;} // get multi for stats, no score.
        }

        // at hit to button if not out
        // if 0 multiplier or number is closed, it is a out.
        // End player's turn if it is their third throw
        addGameThrow(playerIndex, number, multi, bonusScore);
        addTurnThrows(1);

        if(isOut) {
            mListener.onOut();
            return false;
        }

        // was number open before hit?
        boolean wasOpen = gdata().isNumberOpen(numIndex);
        int oldPlayerNumberMarks =  gdata().getSheetHits(playerIndex, numIndex);

        // update game sheet data
        gdata().setSheetHits(playerIndex, numIndex,
                gdata().getSheetHits(playerIndex, numIndex) + multi);

        int newPlayerNumberMarks = gdata().getSheetHits(playerIndex, numIndex);
        boolean nowOpen = gdata().isNumberOpen(numIndex);

        if(is01() || isShanghai()) doScore = multi > 0;
        if(isCricket()) doScore = nowOpen && newPlayerNumberMarks > 3;
        if(isCricket() && !doScore) newScore = oldScore;
        if(isCricket() && oldPlayerNumberMarks < 3
                && newPlayerNumberMarks > 3)
            newScore -= (3 - oldPlayerNumberMarks) * number;
        if (doSpread && newScore > spreadScoreLimit) newScore = spreadScoreLimit;
        if(isCricket() && !nowOpen) newScore = oldScore;

        bonusScore = newScore - oldScore;

        // calculate and set scores
        if(doScore && (isCricket() && gdata().isCutThroat())) {
            // add score to all other players that don't have the number closed
            for (int i = 0; i < MAX_PLAYERS; i++) {
                boolean isPlayerNumOpen2 = gdata().isPlayersNumberOpen(i, numIndex);
                if (!gdata().player(i).isActive() || i == playerIndex || !isPlayerNumOpen2) continue;
                gdata().player(i).addScore(bonusScore);
            }
        } else if(doScore && (is01() || isCricket() || isShanghai())) {
            gdata().player(playerIndex).setScore(newScore);
        }

        // update the last game throw score
        gthrows().getLast().setScore(bonusScore);

        if(isCricket() && wasOpen && !nowOpen) {
            multi = 3 - oldPlayerNumberMarks;
            gthrows().getLast().setMulti(multi);

            // update game sheet data
            gdata().setSheetHits(playerIndex, numIndex, 3);
            // if number was open and now closed play scratch
            // sound, and subtract difference from number hits
            // and update sheet data again
            mListener.onNumberClosed();
        }


        mLastThrow = new GameThrow(gthrows().getLast());

        // check for winner
        checkForWinner(playerIndex);

        mListener.onGameDataChanged();

        if(x01MissedIn) {
            mListener.on01MissedIn(number);
            return false;
        } else if(hitCricketCap) {
            mListener.onCricketCap();
            return false;
        }

        return true; // hit and scored
    }

    /** Takes back one user or bot game hit and sets
     *  current player to the new last game hit player
     *  turn. */
    public boolean doUndo() {
        // no game hits, no more to undo
        if (gthrows().size() < 1) return false;

        GameThrow lastThrow = gthrows().getLast();
        boolean isOut = lastThrow.isOut();

        // change player turn if necessary
        if(gdata().isTurnBased() && gdata().getTurnThrows() == 0) {
            addTurnThrows(-1);
            if(!isOut) return false; // stop on previous turn hit
        }

        // if there was a winner, no more
        if(gdata().getWinnerIndex() > -1) onWinnerUndo();

        // if last multi is 0, it was an out,
        // remove it from hit list, minus throw
        if(isOut) {
            gthrows().removeLast();
            addTurnThrows(-1);

            if(isBaseball()) {
                doBaseballUndo(true);
            }

            mListener.onGameDataChanged();

            // stop on previous players turn or on previous player's
            // last hit
            if(gdata().isTurnBased() && gdata().getTurnThrows() != 0
                    && gthrows().size() > 0 && gthrows().getLast().isOut()) return doUndo();
            else {
                return false;
            }
        }

        int playerIndex = lastThrow.getPlayerIndex();
        int numIndex = GameData.getIndexOfNumber(lastThrow.getNumber());

        int lastMulti = lastThrow.getMulti();
        int lastNumber  = lastThrow.getNumber();

        // update game sheet data
        gdata().setSheetHits(playerIndex, numIndex,
                gdata().getSheetHits(playerIndex, numIndex) - lastMulti);

        // calculate is there is any score to take back from
        // player(s)
        int bonusScore = -lastThrow.getScore();

        // subtract score(s) and update score textview(s)
        if(isCricket() && gdata().isCutThroat()) {
            // if game mode is cut-throat subtract score from all other players.
            for (int i = 0; i < MAX_PLAYERS; i++) {
                if (!gdata().player(i).isActive() || playerIndex == i || !gdata().isPlayersNumberOpen(i, numIndex)) continue;
                gdata().player(i).addScore(bonusScore);
            }
        }

        else if (is01() || isCricket() || isShanghai() || isBaseball()) {
            gdata().player(playerIndex).addScore(bonusScore);
        }

        else if (isKiller()) {
            if(lastMulti == 3) { // killer
                gdata().setPlayerKiller(playerIndex, false);

                if(gdata().isOneKillerMode()) {
                    int lastKiller = getLastKiller();
                    if (lastKiller != -1) gdata().setPlayerKiller(lastKiller, true);
                }
            }
            else if(lastMulti == 1) {
                gdata().getKillerPlayerFromNumber(lastNumber).addScore(bonusScore);
            }

            // special for killer game
            if(!gdata().isKillerStarted()) {
                gdata().player(playerIndex).setKillerNumber(0);
                addTurnThrows(-1);
                addTurnThrows(-1);

                mListener.onKiller(KILL_CODE_PRE_HIT_UNDO, lastNumber);
            }
        }

        if(isBaseball()) doBaseballUndo(false);

        mLastThrow = new GameThrow(lastThrow);
        gthrows().removeLast();
        addTurnThrows(-1);

        mListener.onGameDataChanged();

        return true;
    }

    /**
     * a player has made hit. Calculate if the player has won.
     */
    private boolean checkForWinner() { return checkForWinner(-1); }
    private boolean checkForWinner(int playerIndex) {
        int winner = NO_PLAYER;
        // for 01 games, player hits 0 they win
        if(is01() && gdata().player(playerIndex).getScore() == 0) winner = playerIndex;

        // closed all numbers now check if score
        // is higher, (or lower in cut-throat) than
        // all the other active players.
        // if score is best, player wins!
        else if(isCricket() && gdata().areAllPlayersNumbersClosed(playerIndex)
                && gdata().isPlayerBestScore(playerIndex)) winner = playerIndex;

        else if(isShanghai() && playerIndex == -1)  {
            int[] scoresIndexes = gdata().getScoreOrder(false, true);
            // make sure winner is active
            for(int i = 0; i < MAX_PLAYERS; i++)
                if(gdata().player(scoresIndexes[i]).isActive()) {
                    onWinner(scoresIndexes[i]); break; }
        }

        else if(isKiller()) {
            int playersIn = gdata().getActivePlayerCount();
            int playerInIndex = -1;

            for(int i = 0; i < MAX_PLAYERS; i++) {
                if (gdata().player(i).isActive() && gdata().player(i).getScore() < 1) playersIn--;
                else if (gdata().player(i).isActive()) playerInIndex = i;
            }
            
            if(playersIn == 1) winner = playerInIndex;
        }

        else if(isBaseball()) {
            // get the best score
            int best = 0;
            int bestIndex = -1;
            for(int i = 0; i < MAX_PLAYERS; i++) {
                if (!gdata().player(i).isActive()) continue;
                if (gdata().player(i).getScore() > best) {
                    best = gdata().player(i).getScore();
                    bestIndex = i;
                }
            }

            // check for tie
            for(int i = 0; i < MAX_PLAYERS; i++) {
                if (!gdata().player(i).isActive() || i == bestIndex) continue;
                if (gdata().player(i).getScore() == best) {
                    bestIndex = NO_PLAYER;
                }
            }

            winner =  bestIndex;
        }

        if(winner != NO_PLAYER) {
            onWinner(winner);
            return true;
        }

        return false;
    }

    private void onWinner(int playerIndex) {
        gdata().setWinner(playerIndex);

        // cycle back turn if turn changed
        if(gdata().getTurnThrows() == 0)
            cyclePlayersTurn(true);

        mListener.onWinner(playerIndex);
    }

    private void onWinnerUndo() {
        gdata().undoWinner();
        mListener.onWinnerUndo();
    }

    //
    // X01 game logic
    //


    public void x01Bust(int number) {
        int undoHits = gdata().getTurnThrows();
        for(int i = 0; i < undoHits; i++) doUndo();
        endTurn(); mListener.onBust(number);
    }

    //
    // Shanghai game logic
    //

    private void shanghaiTurnChange(int previousIndex) {
        // do nothing on winner undo
        if(gdata().isWinner()) return;

        // check if player hit a 3, 2, 1
        GameThrowList tl = gdata().gthrows().getPlayerThrowList(previousIndex);

        if(tl.size() > 2) {
            int lastMulti1 = tl.get(tl.size() - 1).getMulti();
            int lastMulti2 = tl.get(tl.size() - 2).getMulti();
            int lastMulti3 = tl.get(tl.size() - 3).getMulti();

            int shangCount = 0;
            if (lastMulti1 == 1 || lastMulti2 == 1 || lastMulti3 == 1) shangCount++;
            if (lastMulti1 == 2 || lastMulti2 == 2 || lastMulti3 == 2) shangCount++;
            if (lastMulti1 == 3 || lastMulti2 == 3 || lastMulti3 == 3) shangCount++;

            if (shangCount == 3)  onWinner(previousIndex);
        }
    }

    private void shanghaiCycledRound(boolean previous) {
        // do nothing on winner undo
        if(gdata().isWinner())  return;

        // if round was cycled, increase or decrease current
        // shanghai number
        int cycledRound = previous ? -1 : 1;

        // counting down
        boolean countDown = gdata().getShanghaiStartNumber() > gdata().getShanghaiEndNumber();

        if(countDown) cycledRound = -cycledRound;

        // get the next/or previous shanghai number
        int newShangNum = gdata().getShanghaiNumber() + cycledRound;

        // end of game
        if(!countDown && newShangNum < gdata().getShanghaiStartNumber()) newShangNum = gdata().getShanghaiStartNumber();
        else if(!countDown && newShangNum > gdata().getShanghaiEndNumber()) { checkForWinner(); return; }
        else if(countDown && newShangNum > gdata().getShanghaiStartNumber()) newShangNum = gdata().getShanghaiStartNumber();
        else if(countDown && newShangNum < gdata().getShanghaiEndNumber())  { checkForWinner(); return; }

        // update shanghai current number
        gdata().setShanghaiNumber(newShangNum);
        mListener.onGameDataChanged();
    }

    //
    // Baseball Logic
    //

    private boolean doScoreBaseBall(int playerIndex, int number,  int multi) {
        int score;
        multi = multi == 1 && number == 25 ? 4 : multi;

        gdata().undoBases().add(gdata().getPlayersOnBase().clone());

        if(multi > 0) {
            // single bullseyes = home run
            score = baseballRunPlayers(multi);
        } else {
            score = 0;
            gdata().addBaseballOuts(1);
            gdata().undoOuts().add(gdata().getBaseballOuts());
        }

        number = gdata().getBaseballInning();

        addGameThrow(playerIndex, number, multi, score);

        if(gdata().getBaseballOuts() == 3) {
            gdata().setBaseballOuts(0);
            gdata().resetBases();
            cyclePlayersTurn(false);
        } else {
            gdata().addTurnThrows(1);
        }

        gdata().getCurrentPlayer().addScore(score);

        mListener.onBaseBall(multi);
        mListener.onGameDataChanged();

        return true;
    }

    private void doBaseballUndo(boolean out) {
        ArrayList<Integer> undoOuts = gdata().undoOuts();

        if(out)  {
            gdata().setBaseballOuts(undoOuts.get(undoOuts.size() - 1));
            undoOuts.remove(undoOuts.size() - 1);

            if(gdata().getBaseballOuts() > 0)
                gdata().addBaseballOuts(-1);

            mListener.onBaseBall(-1);
        }


        if(gdata().undoBases().size() < 1) return;

        for (int i = 0; i < 4; i++) {
            gdata().setPlayersOnBase(gdata().undoBases().get(gdata().undoBases().size() - 1));
        }

        gdata().undoBases().remove(gdata().undoBases().size() - 1);
    }

    private int baseballRunPlayers(int bases) {

        // bring hitter
        gdata().setPlayerOnBase(0, true);

        int homeruns = 0;
        boolean[] onBase = { false, false, false, false };

        for (int i = 0; i < 4; i++) {
            if(gdata().isPlayerOnBase(i)) {
                boolean overRun = i + bases >= 4;
                boolean returned = i + bases <= 0;
                if(overRun) homeruns++;
                else if(!returned) onBase[i + bases] = true;
            }
        }

        for (int i = 0; i < 4; i++) {
            gdata().setPlayerOnBase(i, onBase[i]);
        }


        return homeruns;
    }

    private void baseballCycleRound(boolean previous) {
        gdata().addBaseballInnings(previous ? -1 : 1);

        if(gdata().isWinner()) return;

        if(gdata().getBaseballInning() > gdata().getBaseballInnings()) {
            boolean winner = checkForWinner();
            if(!winner && gdata().getBaseballInning() == gdata().getBaseballInnings() + 1) {
                // overtime
            }
        }

        mListener.onTurnStart();
    }

    private void endTurnBaseball() {
        // add outs for remaining turn throws
        int remainingThrows = 3 - gdata().getBaseballOuts();

        for(int i = 0; i < remainingThrows; i++)
            doScore(gdata().getCurrentTurn(), gdata().getBaseballInning(), 0);

        mListener.onGameDataChanged();
    }

    private void baseballTurnChange(boolean previous) {
        if(previous) {
            int playerIndex = gdata().getCurrentTurn();
            int throwCount = 0;
            for (int i = gthrows().size() - 1; i >= 0; i--) {
                if(gthrows().get(i).getPlayerIndex() != playerIndex || i == 0) {
                    if(i == 0) throwCount++;
                    gdata().setTurnThrows(throwCount); break;
                }

                throwCount++;
            }
        }
    }

    public int getBaseballOnBase() {
        int onBase = 0;
        for (int i = 0; i < 4; i++) {
            if(gdata().isPlayerOnBase(i)) onBase++;
        }
        return onBase;
    }

    public int getBaseballTurnRuns(boolean baseHits) {
        int runs = 0;
        for (int i = gthrows().size() - 1; i >= 0; i--) {
            if(gthrows().get(i).getPlayerIndex() != gdata().getCurrentTurn()) break;
            runs += baseHits ? (gthrows().get(i).getMulti()) : gthrows().get(i).getScore();
        }
        return runs;
    }

    //
    // Killer game logic
    //

    private boolean doKillerScore(int playerIndex, int number) {
        if(!gdata().isKillerStarted()) return doPreKillerScore(playerIndex, number);

        int multi = 0;
        if(number == ScoreButton.SB_CODE_KILLER) {
            if(gdata().getActiveKillerIndex() == playerIndex) return false;
            gdata().setPlayerKiller(playerIndex, true); multi = 3;
            mListener.onKiller(KILL_CODE_KILLER, playerIndex);
            number = gdata().player(playerIndex).getKillerNumber();
        }

        else if(number == ScoreButton.SB_CODE_KILL) {
            number = gdata().player(playerIndex).getKillerNumber();
            killerHit(number);

            if(gdata().player(playerIndex).getScore() == 0) // deaded self
                gdata().setPlayerKiller(playerIndex, false);

            playerIndex = gdata().getActiveKillerIndex(); multi = 1;
        }

        addGameThrow(playerIndex, number, multi, multi == 1 ? -1 : 0);

        checkForWinner();

        mListener.onGameDataChanged();

        return true;
    }

    private boolean doPreKillerScore(int playerIndex, int number) {
        // cant have same numbers
        for (int i = 0; i < MAX_PLAYERS; i++)
            if(gdata().player(i).getKillerNumber() == number) return false;

        gdata().player(playerIndex).setKillerNumber(number);
        gdata().player(playerIndex).setScore(number);

        addGameThrow(playerIndex, number, 1, number);

        addTurnThrows(3);

        mListener.onKiller(KILL_CODE_PRE_HIT, number);

        if(gdata().isKillerStarted()) {
            mListener.onKiller(KILL_CODE_START, 0);
            gdata().initKillerLives();
            gdata().gthrows().clear();
            mListener.onGameDataChanged();
        }

        mListener.onGameDataChanged();

        return true;
    }

    private void killerHit(int number) {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (gdata().player(i).getKillerNumber() == number) {
                gdata().player(i).addScore(-1);
                mListener.onKiller(gdata().player(i).getScore() == 0 ?
                        KILL_CODE_FATALITY : KILL_CODE_KILL, i);
                return;
            }
        }
    }

    private int getLastKiller() {
        GameThrowList gthrows = gdata().gthrows();
        if(gthrows.size() < 2) return -1;

        for(int i = gthrows.size() - 2; i >= 0; i--)
            if(gthrows.get(i).getMulti() == 3) return gthrows.get(i).getPlayerIndex();

        return -1;
    }


    //
    // Turn Logic
    //

    private void addTurnThrows(int thrws) {
        // no throw count in free fill mode
        if(!gdata().isTurnBased()) return;

        thrws += gdata().getTurnThrows();

        gdata().setTurnThrows(thrws);

        if(thrws >= gdata().getThrowsInRound())
            cyclePlayersTurn(false);
        else if (gdata().getTurnThrows() < 0)
            cyclePlayersTurn(true);
    }

    /** Ends the current player's turn */
    public void endTurn() {
        // add outs for remaining turn throws
        if(isBaseball()) { endTurnBaseball(); return; }

        int remainingThrows = 3 -  gdata().getTurnThrows();
        if(remainingThrows == 0) addTurnThrows(1);

        for(int i = 0; i < remainingThrows; i++)
            doScore(gdata().getCurrentTurn(), 0, 0);

        mListener.onGameDataChanged();
    }

    /** Change players turn to next in line */
    public void cyclePlayersTurn(boolean previous) {
        if(gdata().isOpenScoring()) return;

        int playerIndex = gdata().getCurrentTurn();
        int previousIndex = playerIndex;

        int cycledRound = 0;
        do{ playerIndex += previous ? -1 : 1;
            if(playerIndex > MAX_PLAYERS - 1) {
                playerIndex = 0;
                cycledRound = 1;
            } else if(playerIndex < 0) {
                playerIndex = MAX_PLAYERS - 1;
                cycledRound = -1;
            }
        } while(!gdata().player(playerIndex).isActive());

        gdata().setTurnThrows(previous ? 3 : 0);
        setPlayersTurn(playerIndex);

        onCycledTurn(previousIndex, previous);
        if(cycledRound != 0) onCycledRound(cycledRound == -1);
    }

    private void onCycledTurn(int previousIndex, boolean previous) {
        if(isShanghai()) shanghaiTurnChange(previousIndex);
        else if(isBaseball()) baseballTurnChange(previous);
    }

    private void onCycledRound(boolean previous) {
        if(isShanghai()) shanghaiCycledRound(previous);
        else if(isBaseball()) baseballCycleRound(previous);
    }

    /** Set the current player's turn to player at index. */
    public void setPlayersTurn(int index) {
        if(!gdata().player(index).isActive()) { cyclePlayersTurn(false); return; }

        if(index > MAX_PLAYERS - 1) index = MAX_PLAYERS - 1;
        if(index < 0) index = 0;

        gdata().setCurrentTurn(index);

        // start running bot on new turn
        setBotActive(gdata().throwingPlayer().isBot());

        mListener.onTurnStart();
    }

    public static int[] getActiveNumbers(GameFlags.GameFlag game) {
        if(game == null) return ALL_NUMBERS;
        if(game.equals(GameFlags.GameFlag.X01)) return ACTIVE_GAME_NUMBERS[0];
        else if(game.equals(GameFlags.GameFlag.CRICKET)) return ACTIVE_GAME_NUMBERS[1];
        return ALL_NUMBERS;
    }

    //
    // Game Logic Listener
    //

    public static final int KILL_CODE_PRE_HIT       = 0;
    public static final int KILL_CODE_PRE_HIT_UNDO  = 1;
    public static final int KILL_CODE_START         = 2;
    public static final int KILL_CODE_KILLER        = 3;
    public static final int KILL_CODE_KILL          = 4;
    public static final int KILL_CODE_FATALITY      = 5;

    public interface GameLogicListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        void onTurnStart();
        void onWinner(int playerIndex);
        void onWinnerUndo();
        void onGameDataChanged();
        void onBotThrow(boolean hit);
        void onNumberClosed();
        void onBust(int number);
        void on01MissedIn(int number);
        void onCricketCap();
        void onBaseBall(int multi);
        void onOut();
        void onKiller(int code, int playerIndex);
    }


    //
    // Game Bot Logic
    //

    /* the one game bot */
    public GameBot bot() {
        return mBot;
    }

    /** Turn the game bot on or off */
    private void setBotActive(boolean on) {
        // if bot is now off or paused, stop bot do nothing more.
        if(!on) { stopBot(); return; }

        // start the bot with current game data
        bot().start();
    }

    /** Function passed to game bot to update score */
    private void botThrow() {
        boolean hit = doScore(gdata().getCurrentTurn(),
                bot().getLastHitNumber(), bot().getLastHitMulti());
        mListener.onBotThrow(hit);
    }

    /** pauses/resumes the bot and and updates UI */
    public void toggleBotPause() {
        if(mBot.isPaused()) resumeBot();
        else pauseBot();
    }

    /** stops the bot */
    public void stopBot() {
        mBot.stop();
        mListener.onGameDataChanged();
    }

    /** pauses the bot */
    public void pauseBot() {
        mBot.pause();
        mListener.onGameDataChanged();
    }

    /** resumes the bot */
    public void resumeBot() {
        mBot.resume();
        mListener.onGameDataChanged();
    }

    //
    // Game Mode +Helpers
    //

    /** @return true if current game mode is 01 */
    private boolean is01() {
        return gdata().is01();
    }

    /** @return true if current game mode is Cricket */
    private boolean isCricket() {
        return gdata().isCricket();
    }

    /** @return true if current game mode is Shanghai */
    private boolean isShanghai() {
        return gdata().isShanghai();
    }

    /** @return true if current game mode is Baseball */
    private boolean isBaseball() {
        return gdata().isBaseball();
    }

    /** @return true if current game mode is Killer */
    private boolean isKiller() {
        return gdata().isKiller();
    }

    /** @return the current game throws */
    public GameThrowList gthrows() {
        return gdata().gthrows();
    }

    /** @return the current game data */
    public GameData gdata() {
        return mGameData;
    }
}
