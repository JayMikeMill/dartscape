/*
 * Cricket Dart Game java file
 */

package com.gworks.dartscape.fragments;

import static com.gworks.dartscape.main.Globals.*;
import static com.gworks.dartscape.ui.ScoreButton.*;
import static com.gworks.dartscape.util.Helper.*;

import android.annotation.SuppressLint;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.data.GameFlags;
import com.gworks.dartscape.data.GameThrow;
import com.gworks.dartscape.database.PlayerDatabase;
import com.gworks.dartscape.data.PlayerStats;
import com.gworks.dartscape.logic.GameLogic;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.ui.BaseballView;
import com.gworks.dartscape.ui.MultiView;
import com.gworks.dartscape.R;

import com.gworks.dartscape.ui.ProgressView;
import com.gworks.dartscape.ui.ScoreButton;
import com.gworks.dartscape.util.MyColors;
import com.gworks.dartscape.util.Sound;
import com.gworks.dartscape.util.UserPrefs;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

/**
 * GameModeCricket.<p>
 * Cricket Dart Game.<p>
 * You can play against friends or bots.
 */
public class GameFragment extends Fragment implements GameLogic.GameLogicListener {

    // controls
    private LinearLayout mLayLegsSets;
    private AppCompatTextView mTvLegsSets;
    private LinearLayout mLayPlayerLabels;
    private LinearLayout mLayPlayers;
    private LinearLayout[] mLayPlayersCols;
    private LinearLayout mLayWinsLabel;
    private LinearLayout mLayAvgsLabel;
    private AppCompatTextView[] mTvPlayerNames;
    private AppCompatTextView[] mTvPlayerScore;
    private AppCompatTextView[] mTvPlayerWins;
    private AppCompatTextView[] mTvPlayerAvgs;

    private LinearLayout mLayGameInfo;
    private AppCompatTextView mTvGameInfoRoundThrow;
    private AppCompatTextView mTvGameInfoPlayer;
    private AppCompatTextView mTvGameInfoThrow;
    private ProgressView mPvBotProgress;

    private LinearLayout mLayGameSheet;
    private LinearLayout[] mLaySheetCols;
    private LinearLayout mLayNumberLabels;
    private ScoreButton[][] mScButtons;

    private LinearLayout mLayBaseball;
    private ScoreButton  mSbBaseballOut;
    private ScoreButton  mSbBaseballHR;
    private BaseballView mBaseballView;

    ArrayList<ScoreButton> mUndoButtonList;

    Drawable mDrwControlBg;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** creates the view one time to save time. */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_game, container, false);
        super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    /** view has been created, init data, controls and UI. Start now game */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize all game variables on first create.
        initControls();

        glogic().setGameLogicListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /** user has left the app, do stuff */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        glogic().pauseBot(); // pauses the bot when uses leaves app
    }

    /** called when user enters/exits this fragment */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (this.getView() == null) return;

        if (hidden) {
            glogic().pauseBot();
            return;
        }

        uiSyncLabels();

        if (gdata().flags().getClear(GameFlags.GameFlag.GAME_DATA_RESTORED))
            uiCreateAndSync();
        else if (gdata().flags().getClear(GameFlags.GameFlag.STARTED_NEW_GAME))
            onStartGame();
    }


    /** initialize all view widgets in the layout */
    @SuppressLint({"DiscouragedApi", "ClickableViewAccessibility"})
    private void initControls() {

        // undo button
        MultiView btnUndo = requireView().findViewById(R.id.btnGameUndo);
        btnUndo.setOnClickListener(v -> onUndo());

        // bot button
        MultiView btnPause = requireView().findViewById(R.id.btnGameBot);
        btnPause.setOnClickListener(v -> toggleBotPause());

        // end turn button
        MultiView btnEndTurn = requireView().findViewById(R.id.btnGameEndTurn);
        btnEndTurn.setOnClickListener(v -> endTurn());

        // new game button
        MultiView btnGameResults = requireView().findViewById(R.id.btnGameGameResults);
        btnGameResults.setOnClickListener(v -> showGameResults());

        mLayLegsSets      = requireView().findViewById(R.id.lGameLegsSets);
        mTvLegsSets       = requireView().findViewById(R.id.tvGameLegsSets);

        // current player background
        mDrwControlBg = Objects.requireNonNull(AppCompatResources.getDrawable(requireContext(), R.drawable.drw_control_bg));
        mDrwControlBg.setColorFilter(MyColors.themeHighlight(), PorterDuff.Mode.ADD);

        mLayPlayers       = requireView().findViewById(R.id.lGamePlayers);
        mLayPlayersCols   = new LinearLayout[MAX_PLAYERS];
        mLayPlayerLabels  = requireView().findViewById(R.id.lGamePlayerLabels);
        mLayWinsLabel     = requireView().findViewById(R.id.lGameWinsLabel);
        mLayAvgsLabel     = requireView().findViewById(R.id.lGameAvgsLabel);
        mTvPlayerNames = new AppCompatTextView[MAX_PLAYERS]; // player names text views
        mTvPlayerScore = new AppCompatTextView[MAX_PLAYERS]; // scores text views
        mTvPlayerWins  = new AppCompatTextView[MAX_PLAYERS]; // wins text views
        mTvPlayerAvgs  = new AppCompatTextView[MAX_PLAYERS]; // averages text views

        mLayGameInfo      = requireView().findViewById(R.id.lGameInfo);
        mTvGameInfoRoundThrow = requireView().findViewById(R.id.tvGameInfoRoundThrow);
        mTvGameInfoPlayer = requireView().findViewById(R.id.tvGameInfoPlayer);
        mTvGameInfoThrow  = requireView().findViewById(R.id.tvGameInfoThrow);
        mPvBotProgress = requireView().findViewById(R.id.pvGameBotProgress);

        mLayGameSheet     = requireView().findViewById(R.id.lGameSheet);
        mLayNumberLabels  = requireView().findViewById(R.id.lGameSheetColLabels);
        mLaySheetCols  = new LinearLayout[MAX_PLAYERS];
        mScButtons     = new ScoreButton[MAX_PLAYERS][NUMBER_COUNT];

        // Baseball
        mLayBaseball   = requireView().findViewById(R.id.lGameBaseball);
        mSbBaseballOut = requireView().findViewById(R.id.sbGameBaseBallOut);
        mSbBaseballOut.setScoreButtonListener((hitButton, multi) -> onBaseballOut(multi));
        mSbBaseballHR  = requireView().findViewById(R.id.sbGameBaseBallHomeRun);
        mSbBaseballHR.setScoreButtonListener((hitButton, multi) -> onScoreButton(gdata().getBaseballInning(), 4));
        mBaseballView  = requireView().findViewById(R.id.BaseballView);
        mBaseballView.setScoreButtonListener(this::onScoreButton);

        int resourceViewID;

        for(int i = 0; i < MAX_PLAYERS; i++) {
            resourceViewID = getResources().getIdentifier("lGamePlayer" + (i+1), "id", requireActivity().getPackageName());
            mLayPlayersCols[i] = requireView().findViewById(resourceViewID);

            resourceViewID = getResources().getIdentifier("tvGamePlayerName" + (i+1), "id", requireActivity().getPackageName());
            mTvPlayerNames[i] = requireView().findViewById(resourceViewID);

            resourceViewID = getResources().getIdentifier("tvGamePlayerScore" + (i+1), "id",requireActivity().getPackageName());
            mTvPlayerScore[i] = requireView().findViewById(resourceViewID);

            resourceViewID = getResources().getIdentifier("tvGamePlayerAvg" + (i+1), "id",requireActivity().getPackageName());
            mTvPlayerAvgs[i] = requireView().findViewById(resourceViewID);

            resourceViewID = getResources().getIdentifier("lGameSheetCol" + (i+1), "id", requireActivity().getPackageName());
            mLaySheetCols[i] = requireView().findViewById(resourceViewID);


            for(int j = 0; j < NUMBER_COUNT; j++) {
                resourceViewID = getResources().getIdentifier("sbGame" + (i+1) + (j+1), "id" ,requireActivity().getPackageName());

                mScButtons[i][j] = requireView().findViewById(resourceViewID);
                ScoreButton crB = mScButtons[i][j];
                crB.setScoreButtonListener(this::onScoreButton);
            }

            resourceViewID = getResources().getIdentifier("tvGamePlayerWins" + (i+1), "id", requireActivity().getPackageName());
            mTvPlayerWins[i] = requireView().findViewById(resourceViewID);
        }
    }

    private void uiCreateAndSync() {
        // init the cricket sheet ui
        uiCreateScoreSheet();
        uiSyncWithGameData();
        uiSyncPlayerWins();

        if(isCricket()) uiSyncFullCricketSheet();
        else if(gdata().isKillerStarted())
            uiStartKillerLayout();

        uiRefreshGame();

        // set player turn if not free fill

        uiClearTurn();

        if(gdata().isTurnBased()) uiSyncTurn();
        else initOpenScoring();

        setAllScoreButtonsActive(!gdata().isWinner());
    }

    /** initialize the widgets and values of the cricket sheet */
    private void uiCreateScoreSheet() {

        // init scores
        for(int i = 0; i < MAX_PLAYERS ; i++)
            mTvPlayerScore[i].setText(String.valueOf(gdata().player(i).getScore()));

        // init wins
        for(int i = 0; i < MAX_PLAYERS ; i++)
            mTvPlayerWins[i].setText(String.valueOf(gdata().player(i).getWins()));

        mLayGameSheet.setVisibility(isBaseball() ? GONE : VISIBLE);
        mLayBaseball.setVisibility(isBaseball() ? VISIBLE : GONE);

        if(isBaseball()) {
            mBaseballView.initData();
            mBaseballView.setGameMode(GameFlags.GameFlag.NONE);
            mSbBaseballOut.setGameMode(GameFlags.GameFlag.BASEBALL);
            mSbBaseballOut.setNumber(SB_CODE_OUT);
            mSbBaseballHR.setGameMode(GameFlags.GameFlag.BASEBALL);
            mSbBaseballHR.setNumber(SB_CODE_HOME_RUN);
            return;
        }

        // init buttons
        for(int i = 0; i < MAX_PLAYERS ; i++)
            for(int j = 0; j < NUMBER_COUNT; j++) {
                ScoreButton sb = mScButtons[i][j];

                sb.initData();
                sb.setPlayerIndex(i);


                if(is01()) sb.setGameMode(GameFlags.GameFlag.X01);
                if(isCricket()) sb.setGameMode(GameFlags.GameFlag.CRICKET);
                if(isShanghai()) sb.setGameMode(GameFlags.GameFlag.SHANGHAI);
                if(isKiller()) sb.setGameMode(GameFlags.GameFlag.KILLER);

                // set up the scoring board according to game
                if(is01()) {
                    if( j < 6) {
                        if (j == 0) {
                            if (i == 0) sb.setNumber(SB_CODE_BUST_1);
                            if (i > 0)  sb.setNumber(i);
                        } else if (j == 5) {
                            if (i == 1) sb.setNumber(20);
                            else if (i == 2) sb.setNumber(25); // bust
                            else {
                                sb.setVisibility(View.INVISIBLE);
                                sb.setNumber(SB_CODE_INACTIVE);
                            }
                        } else sb.setNumber((j*4) + i);
                    } else {
                        sb.setVisibility(View.GONE);
                        sb.setNumber(SB_CODE_INACTIVE);
                    }
                }

                else if(isCricket()) sb.setCricketNumberIndex(j);

                else if(isShanghai()) {
                    if(sb.equals(getShanghaiButton())) {
                        getShanghaiButton().setShangNumber(gdata().getShanghaiStartNumber(), false);
                    }
                    else sb.setVisibility(View.GONE);
                }

                else if(isKiller()) {
                    if(j > 0 && j < 6) {
                        sb.setNumber(((j - 1) * 4) + (i + 1));
                        sb.setMarks(1);
                    } else {
                        sb.setNumber(SB_CODE_INACTIVE);
                        sb.setVisibility(View.GONE);
                    }
                }
            }



        if(isCricket()) uiCricketOpenCloseAllNumbers();
    }

    /** start a new game or set */
    public void onStartGame() {

        // init game data sheet
        glogic().initGame();
        uiCreateAndSync();
        mUndoButtonList = new ArrayList<>();
    }

    ScoreButton getShanghaiButton() {
        return mScButtons[1][3];
    }

    private void onScoreButton(ScoreButton hitButton, int multi) {
        // cant click buttons while bot is active
        if(!gdata().throwingPlayer().isBot())
            onScore(hitButton, multi);
    }

    private void onScoreButton(int number, int multi) {
        // cant click buttons while bot is active
        if(!gdata().throwingPlayer().isBot())
            onScore(gdata().getCurrentTurn(), number, multi);
    }

    /** Called when a player or bot hits a cricket button.*/
    private void onScore(ScoreButton hitButton, int multi) {
        // get player's index and the number
        int playerIndex = gdata().isTurnBased() ? gdata().getCurrentTurn() :
                hitButton.getPlayerIndex();

        int number = hitButton.getNumber();

        onScore(playerIndex, number,  multi);
    }

    /** Called when a player or bot hits a cricket button.*/
    private void onScore(int playerIndex, int number, int multi) {
        // show popup score
        if(glogic().doScore(playerIndex, number, multi))
            showLastScorePopup(false);
    }

    private void onUndo() {
        // do logic and show popup score
        if(glogic().doUndo()) showLastScorePopup(true);

        // show popup score
        if(isBaseball())  uiSyncBaseballBases();

        // make sure bot is now paused
        glogic().pauseBot();
    }

    private void showLastScorePopup(boolean undo) {
        if(getLastScoredButton() == null) return;
        if(isKiller()) return;

        GameThrow lastThrow = glogic().getLastThrow();

        int score = Math.abs(lastThrow.getScore());

        getLastScoredButton().popSoundAnimate(score, lastThrow.getMulti(), undo);
    }

    private ScoreButton getLastScoredButton() {
        GameThrow lastThrow = glogic().getLastThrow();
        if(lastThrow.getMulti() == 0 && lastThrow.getNumber() == 0) return null;

        if(is01() || isKiller()) return getButtonFromNumber(lastThrow.getNumber());
        if(isCricket())  return mScButtons[lastThrow.getPlayerIndex()]
                        [GameData.getIndexOfNumber(lastThrow.getNumber())];
        if(isShanghai()) return getShanghaiButton();
        if(isBaseball()) return mBaseballView;

        return null;
    }

    private ScoreButton getButtonFromNumber(int number) {
        for (int i = 0; i < MAX_PLAYERS; i++)
            for (int j = 0; j < NUMBER_COUNT; j++)
                if(mScButtons[i][j].getNumber() == number)
                    return mScButtons[i][j];

        return mScButtons[0][0];
    }

    /** Clear any player turn. Used for free fill mode */
    private void initOpenScoring() {
        // set players buttons to active, deactivate rest
        for(int i = 0; i < MAX_PLAYERS ; i++)
            for (int j = 0; j < NUMBER_COUNT; j++)
                mScButtons[i][j].setActive(true);
    }

    private void uiClearTurn() {
        // set button background colors
        for(int i = 0; i < MAX_PLAYERS ; i++) {
            mLaySheetCols[i].setBackgroundColor(Color.TRANSPARENT);
            mLayPlayersCols[i].setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /** pauses/resumes the bot and and updates UI */
    private void toggleBotPause() {
        glogic().toggleBotPause();
        uiSyncBotProgress();
    }

    /** @return True if player at index is active */
    private boolean isPlayerActive(int index) {
        return gdata().player(index).isActive();
    }

    private void endTurn() {
        glogic().endTurn();
    }
    /** Called when a player has won the game. Adds a point to
     *  players total wins and show the winner dialog box. */
    private void winnerWinnerChickenDinner() {
        // stop all bot activity
        glogic().stopBot();

        uiSyncPlayerWins();

        loadGameResults();
        showGameResults();

        uiSyncGameInfo();

        // play winner sound
        Sound.stopAll();
        Sound.play(Sound.TRUMPET);

        // save the game to the database
        if(!isOpenScoring() && UserPrefs.getRecordStats())
            db().saveGame(gdata());
    }

    private void loadGameResults() {
        ((GameResultsFragment) frags().getFragFromId
                (FragManager.FragId.FRAG_GAME_RESULTS)).loadResults();
    }

    private void showGameResults() {
        // create and winner dialog box and pass it
        // the winning players name. Show it.
       frags().show(FragManager.FragId.FRAG_GAME_RESULTS,
                    FragManager.TRANS_ANIM_LONG_FADE_OUT );
    }

    private void setAllScoreButtonsActive(boolean active) {
        for (int i = 0; i < MAX_PLAYERS; i++)
            for (int j = 0; j < NUMBER_COUNT; j++)
                mScButtons[i][j].setActive(active);
    }


    //
    // UI Syncing Members
    //

    private void uiRefreshGame() {
        // update player score text views
        uiSyncPlayerScores();
        uiSyncThrow();
        uiSyncButtons();

        if(is01()) uiSync01Numbers();
        if(isCricket()) uiSyncCricketSheet();
        if(isShanghai()) getShanghaiButton().setShangNumber(gdata().getShanghaiNumber(), true);
        if(isKiller()) uiSyncKillerLayout();
    }

    /** sync the layout to match updated game data */
    private void uiSyncWithGameData() {
        // update player name textviews
        uiSyncSlotNames();
        uiSyncPlayerScores();
        uiSyncPlayerAvgs();

        // update game sheet layout
        uiSyncLayout();
    }

    private void uiSyncLegsSets() {
        if(!mIsLegsSetsShowing) return;
        mTvLegsSets.setText(gdata().getLegsSetsUiString());
    }

    int mCricketLabelAlignment = 2;
    private int getLabelAlignment() {
        int align = (gdata().getActivePlayerCount() % 2 == 0) ? 2 : 0;
        if(gdata().getActivePlayerCount() == 2 && isPlayerActive(0) && isPlayerActive(1)) align = 1;
        if(gdata().getActivePlayerCount() == 2 && isPlayerActive(2) && isPlayerActive(3)) align = 3;
        return align;
    }

    /** Enables or disables players and update the game layout
     *  to match. */
    private void uiSyncLayout() {
        for(int i = 0; i < MAX_PLAYERS; i++) {
            mLayPlayersCols[i].setVisibility(gdata().player(i).isActive() ? View.VISIBLE : View.GONE);

            if(isCricket()) {
                mLaySheetCols[i].setVisibility(gdata().player(i).isActive() ? View.VISIBLE : View.GONE);
            } else if(is01() || isKiller()) {
                mLaySheetCols[i].setVisibility(View.VISIBLE);
            } else if(isShanghai()) {
                mLaySheetCols[i].setVisibility(i == 1 ? View.VISIBLE : View.GONE);
            }
        }

        // hide/show cricket label
        mLayNumberLabels.setVisibility(isCricket() ? View.VISIBLE : View.GONE);
        mLayPlayerLabels.setVisibility(isCricket() ? View.VISIBLE : View.GONE);

        int align = getLabelAlignment();
        moveChildView(mLayGameSheet, mCricketLabelAlignment, align);
        moveChildView(mLayPlayers, mCricketLabelAlignment, align);

        mCricketLabelAlignment = align;

        for (int i = 0; i < MAX_PLAYERS; i++)
            mTvPlayerScore[i].setMaxLines(isKiller() ? 2 : 1);
    }

    private boolean mIsWinsShowing, mIsAvgsShowing, mIsGameInfoShowing, mIsLegsSetsShowing;
    private void uiSyncLabels() {
        mIsLegsSetsShowing = gdata().isLegs();
        mIsWinsShowing = UserPrefs.getShowWins() || gdata().isLegs();
        mIsAvgsShowing = UserPrefs.getShowAvgs() && gdata().isTurnBased() && !isKiller();
        mIsGameInfoShowing = UserPrefs.getShowGameInfo() && gdata().isTurnBased();

        mLayLegsSets.setVisibility(mIsLegsSetsShowing ? VISIBLE : GONE);

        for (int i = 0; i < MAX_PLAYERS; i++) {
            mTvPlayerWins[i].setVisibility(mIsWinsShowing ? VISIBLE : GONE);
            mTvPlayerAvgs[i].setVisibility(mIsAvgsShowing ? VISIBLE : GONE);
        }

        mLayWinsLabel.setVisibility(mIsWinsShowing ? VISIBLE : GONE);
        mLayAvgsLabel.setVisibility(mIsAvgsShowing ? VISIBLE : GONE);
        float newWeight = 2f + (mIsWinsShowing ? .3f : 0f) + (mIsAvgsShowing ? .3f : 0f);
        ((LinearLayout)mLayPlayers.getParent()).setLayoutParams(new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, 0, newWeight));

        mLayGameInfo.setVisibility(mIsGameInfoShowing ? VISIBLE : GONE);

        uiSyncLegsSets();
        uiSyncPlayerWins();
        uiSyncPlayerAvgs();
        uiSyncGameInfo();
    }

    /** update player name views from game data */
    private void uiSyncSlotNames() {
        for(int i = 0; i < MAX_PLAYERS; i++) {
            mTvPlayerNames[i].setMaxLines(gdata().player(i).isTeam() || gdata().player(i).isBot() ? 2 : 1);
            mTvPlayerNames[i].setText(uiGetSlotName(i));
        }
    }

    private String uiGetSlotName(int index) {
        if(!isPlayerActive(index)) return "";

        String name = gdata().player(index).getName();

        if(gdata().player(index).isBot()) name = name.replace("-", "\n");

        if(!gdata().player(index).isTeam()) return name;

        boolean isThrowing = gdata().isTurnBased() && index == gdata().getCurrentTurn();

        String teammateName = gdata().player(index).getTeammateName();

        if(isThrowing && !gdata().isTeammateTurn()) name = "-" + name + "-";
        else if(isThrowing) teammateName = "-" + teammateName + "-";

        name += "\n" + teammateName;

        return name;
    }

    /** update player score textviews */
    private void uiSyncPlayerScores() {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if(gdata().isKillerStarted())
                mTvPlayerScore[i].setText(getKillerScoreString(i));

            else mTvPlayerScore[i].setText(String.valueOf(gdata().player(i).getScore()));
        }
    }

    private String getKillerScoreString(int playerIndex) {
        StringBuilder scoreText = new StringBuilder();

        for (int j = 0; j < gdata().player(playerIndex).getScore(); j++) {
            scoreText.append("|");
        }

        if (gdata().player(playerIndex).getScore() == 0)
            scoreText.append("\uD83D\uDC80"); // 💀

        return scoreText.toString();
    }

    private void uiSyncPlayerWins() {
        if(!mIsWinsShowing) return;

        // update player score textviews
        for (int i = 0; i < MAX_PLAYERS; i++)
            mTvPlayerWins[i].setText(gdata().getPlayerLegsSetsUiString(i));
    }

    private void uiSyncPlayerAvgs() {
        if(!mIsAvgsShowing) return;

        // update player score textviews
        for (int i = 0; i < MAX_PLAYERS; i++) {
            mTvPlayerAvgs[i].setText(getPlayerAvgUiText(i));
        }
    }

    private String getPlayerAvgUiText(int index) {
        if(!gdata().player(index).isActive()) return "";
        float avg = 0.0f; PlayerStats stats;

        stats = gdata().stats(index);

        if(is01()) {
            avg = stats.getScoreAvgRound();
        } else if(isCricket() || isShanghai()) {
            avg = stats.getMarkAvgRound();
        } else if(isBaseball()) {
            avg = stats.getBaseballHitsPerInning();
        }

        return String.format(Locale.US, "%.2f", avg);
    }

    @SuppressLint("SetTextI18n")
    private void uiSyncGameInfo() {
        if(!mIsGameInfoShowing) return;

        if(gdata().isWinner()) {
            mTvGameInfoPlayer.setText((gdata().winner().isTeam() ? getString(R.string.team) + " " : "")
                    + gdata().winner().getTeamName().replace("\n", " ") + " " + getString(R.string.won) + "!");
        } else {
            int curIndex = gdata().getCurrentTurn();
            String name = gdata().player(curIndex).getName();

            if (gdata().player(curIndex).isTeam()) {
                if (gdata().isTeammateTurn()) name = gdata().player(curIndex).getTeammateName();
            }

            mTvGameInfoPlayer.setText(name.replace("\n", " "));
        }

        int round = isShanghai() ? gdata().getShanghaiNumber() :
                isBaseball() ? gdata().getBaseballInning() : gdata().getRoundCount() + 1;

        int throwCount = gdata().getTurnThrows();
        String throwCountStr = String.valueOf(throwCount + 1);
        if(throwCount == gdata().getThrowsInRound()) throwCountStr = "X";
        mTvGameInfoRoundThrow.setText(round + " - " + throwCountStr);
        mTvGameInfoThrow.setText(getPlayerAvgUiText(gdata().getCurrentTurn()));
    }

    private void uiSyncBotProgress() {
        if(mIsGameInfoShowing && gdata().getCurrentPlayer().isBot() && glogic().bot().isRunning()) {
            mPvBotProgress.runProgress(glogic().bot().getThrowWaitMs() - 50);
        } else {
            mPvBotProgress.stopProgress();
        }

    }

    private void uiSyncButtons() {
        // undo button
        requireView().findViewById(R.id.btnGameUndo).setVisibility(gdata().gthrows().size() < 1 ? View.INVISIBLE : View.VISIBLE);

        // bot button
        boolean showBotBtn = gdata().throwingPlayer().isBot() && !gdata().isTurnOver();
        MultiView btnBot = requireView().findViewById(R.id.btnGameBot);
        btnBot.setSelectedIndex(glogic().bot().isRunning() ? 0 : 1);
        btnBot.setVisibility(showBotBtn && !gdata().isWinner() ? View.VISIBLE : View.GONE);

        // next button
        requireView().findViewById(R.id.btnGameEndTurn)
                .setVisibility(gdata().isTurnBased() && !showBotBtn &&
                        !gdata().isWinner() ? View.VISIBLE : View.GONE);

        // new game button
        requireView().findViewById(R.id.btnGameGameResults)
                .setVisibility(gdata().isWinner() ? View.VISIBLE : View.GONE);

    }

    private void uiSyncThrow() {
        uiSyncPlayerAvgs();
        uiSyncGameInfo();
        uiSyncBotProgress();

        if(isBaseball()) uiSyncBaseballLabels();
    }

    private void uiSyncTurn() {
        if(gdata().isOpenScoring()) return;

        if(is01()) uiSync01Numbers();

        setAllScoreButtonsActive(!gdata().getCurrentPlayer().isBot());

        // set players buttons to active, deactivate rest
        if(isCricket()) {
            for (int i = 0; i < MAX_PLAYERS; i++)
                for (int j = 0; j < NUMBER_COUNT; j++)
                    mScButtons[i][j].setActive(gdata().player(i).isActive() && !gdata().player(i).isBot() && i == gdata().getCurrentTurn());
        }

        // set button background colors
        int playerThrowing = gdata().getCurrentTurn();

        for(int i = 0; i < MAX_PLAYERS ; i++) {
            mLayPlayersCols[i].setBackgroundColor(i == playerThrowing ? MyColors.themeHighlight() : Color.TRANSPARENT);
            mLayPlayersCols[i].setBackground(i == playerThrowing ? mDrwControlBg : null);
            if(isCricket()) {
                mLaySheetCols[i].setBackgroundColor(i == playerThrowing ? MyColors.themeHighlight() : Color.TRANSPARENT);
                mLaySheetCols[i].setBackground(i == playerThrowing ? mDrwControlBg : null);
            }
        }

        uiSyncSlotNames();
        uiSyncThrow();
        uiSyncButtons();

        if(isBaseball()) uiSyncBaseballBases();
    }

    /** Ends the current player's turn */
    private void uiSync01Numbers() {
        int score = gdata().throwingPlayer().getScore();

        int minOut = gdata().x01getMinOut();

        for (int i = 0; i < MAX_PLAYERS; i++)
            for (int j = 0; j < NUMBER_COUNT - 1; j++) {
                ScoreButton sb = mScButtons[i][j];
                int number = sb.getNumber();

                // get all outs 1, 2, 3
                // if player can out on number set color of it
                int out = minOut; boolean canOut = false;
                boolean canBullOut = gdata().x01GetInsOuts(false)[3] && score <= 50;

                if (gdata().flags().get(GameFlags.GameFlag.X01_STRAIGHT_OUT)) {
                    while (true) {
                        if (number * out == score) {
                            canOut = true;
                            break;
                        }
                        if (++out > 3) break;
                    }
                } else if (gdata().flags().get(GameFlags.GameFlag.X01_DOUBLE_OUT) || gdata().flags().get(GameFlags.GameFlag.X01_TRIPLE_OUT)) {
                    if (number * out == score) canOut = true;
                } else if (gdata().flags().get(GameFlags.GameFlag.X01_MASTER_OUT)) {
                    if (number * out == score || number * ++out== score) {
                        canOut = true;
                    } else if(canBullOut && number == 25) {
                        if (number * (out -= 2) == score || number * ++out== score) {
                            canOut = true;
                        }
                    }
                }

                boolean isBust = !canOut && !((number <= score - minOut) || (number * minOut == score));
                if(number == -1) isBust = true;

                int outColor = out == 1 ? MyColors.COLOR_SINGLE
                        : out == 2 ? MyColors.COLOR_DOUBLE : MyColors.COLOR_TRIPLE;

                int color = MyColors.themeText();

                if(isBust) color =  MyColors.RED_LIGHT;
                else if(canOut) color = outColor;

                sb.setColor(color);
            }
    }

    private void uiSyncCricketSheet() {
        GameThrow lastThrow = glogic().getLastThrow();
        int playerIndex = lastThrow.getPlayerIndex();
        int numIndex = GameData.getIndexOfNumber(lastThrow.getNumber());
        if(numIndex == -1) numIndex = 0;
        int multi = lastThrow.getMulti();
        if(numIndex > 7 || multi == 0) return;

        mScButtons[playerIndex][numIndex]
                .setMarks(gdata().getSheetHits(playerIndex, numIndex));

        uiCricketOpenCloseNumber(numIndex);
    }

    private void uiSyncFullCricketSheet() {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if(!gdata().player(i).isActive()) continue;
            for (int j = 0; j < NUMBER_COUNT; j++)
                mScButtons[i][j].setMarks(gdata().getSheetHits(i, j));
        }
    }

    /** Checks if number is open or closed, updates cricket sheet in
     * lay out to match number. */
    private void uiCricketOpenCloseNumber(int index) {
        boolean open = gdata().isNumberOpen(index);

        // close/open cricket buttons for active players
        for(int i = 0; i < MAX_PLAYERS; i++)
            if(gdata().player(i).isActive()) mScButtons[i][index].setEnabled(open);

        mLayNumberLabels.getChildAt(index)
                .setBackgroundColor(open ? Color.TRANSPARENT : MyColors.themeDisabled());

        ((TextView)mLayNumberLabels.getChildAt(index))
                .setTextColor(open ? MyColors.themeText() : MyColors.themeTextDisabled());
    }

    /** Updates all numbers to open or close and changes the
     * layout */
    private void uiCricketOpenCloseAllNumbers() {
        for(int i = 0; i < NUMBER_COUNT; i++)
            uiCricketOpenCloseNumber(i);
    }

    private void uiSyncBaseballLabels() {
        mBaseballView.setGameInfo(gdata().getBaseballInning(),
                gdata().getBaseballOuts(),  glogic().getBaseballOnBase(),
                glogic().getBaseballTurnRuns(false), glogic().getBaseballTurnRuns(true));
    }
    private void uiSyncBaseballBases() {
        mBaseballView.post(() -> mBaseballView.setBases(gdata().getPlayersOnBase()));
        uiSyncBaseballLabels();
    }

    /** Relayout the game sheet to start the killer game */
    private static final int KILLER_NUM_ROW = 0;
    private static final int KILLER_KILL_ROW = KILLER_NUM_ROW + 1;
    private static final int KILLER_SPACE_ROW_1 = KILLER_NUM_ROW + 2;
    private static final int KILLER_KILLER_ROW = KILLER_NUM_ROW + 3;
    private static final int KILLER_SPACE_ROW_2 = KILLER_NUM_ROW + 4;
    private void uiSyncKillerLayout() {
        if(!gdata().isKillerStarted()) {
            uiSyncKillerPregame();
            return;
        }

        boolean isKiller = gdata().getActiveKillerIndex() != NO_PLAYER;

        for (int i = 0; i < MAX_PLAYERS; i++) {
            if(!gdata().player(i).isActive()) continue;

            ScoreButton sbNum = mScButtons[i][KILLER_NUM_ROW];
            ScoreButton sbKill = mScButtons[i][KILLER_KILL_ROW];
            ScoreButton sbKiller = mScButtons[i][KILLER_KILLER_ROW];

            sbNum.setVisibility(gdata().player(i).getScore() > 0 ? View.VISIBLE : View.INVISIBLE);
            sbKill.setVisibility(gdata().player(i).getScore() > 0 && isKiller ? View.VISIBLE : View.INVISIBLE);
            sbKiller.setVisibility(gdata().player(i).getScore() > 0 ? View.VISIBLE : View.INVISIBLE);
            sbNum.setEnabled(gdata().player(i).getScore() > 0);
            sbKill.setEnabled(gdata().player(i).getScore() > 0);
            sbKiller.setEnabled(gdata().player(i).getScore() > 0);

            sbKiller.setColor(gdata().player(i).isKiller() ? MyColors.RED_LIGHT : MyColors.themeText());
            if(gdata().getActiveKillerIndex() == i) sbKiller.setColor(MyColors.RED);
        }
    }

    private void uiSyncKillerPregame() {
        for (int i = 0; i < MAX_PLAYERS; i++)
            for (int j = 0; j < NUMBER_COUNT - 1; j++)  {
                ScoreButton sb = mScButtons[i][j];
                for (int k = 0; k < MAX_PLAYERS; k++) {
                    if(sb.getNumber() == gdata().player(k).getKillerNumber()) {
                        sb.setEnabled(false, false); break;
                    } else sb.setEnabled(true, false);
                }
            }
    }

    private void uiStartKillerLayout() {
        uiClearTurn();

        for (int i = 0; i < MAX_PLAYERS; i++) {
            if(!gdata().player(i).isActive()) mLaySheetCols[i].setVisibility(View.GONE);

            for (int j = 0; j < NUMBER_COUNT - 1; j++) {
                ScoreButton sb = mScButtons[i][j];
                sb.setPlayerIndex(i);
                sb.setMarks(0);
                sb.setEnabled(true, false);
                sb.setVisibility(j == KILLER_NUM_ROW || j == KILLER_KILL_ROW ||
                        j == KILLER_KILLER_ROW ? VISIBLE : GONE);
                sb.setActive(j != KILLER_NUM_ROW);
                sb.setNumber(j == KILLER_NUM_ROW ? gdata().player(i).getKillerNumber()
                        : j == KILLER_KILL_ROW ? SB_CODE_KILL
                        : j == KILLER_KILLER_ROW ? SB_CODE_KILLER : SB_CODE_INACTIVE);
                if(j == KILLER_SPACE_ROW_1 || j == KILLER_SPACE_ROW_2) sb.setVisibility(INVISIBLE);
            }
        }
    }

    ScoreButton getKillerButton(int index) {
        return mScButtons[index][KILLER_KILLER_ROW];
    }

    ScoreButton getKillButton(int index) {
        return mScButtons[index][KILLER_KILL_ROW];
    }



    /*
     *  GAME LOGIC LISTENERS
     */

   @Override
    public void onTurnStart() {
        uiSyncTurn();
    }

    //** triggered by game logic win a player wins the game */
    @Override
    public void onWinner(int playerIndex) {
        setAllScoreButtonsActive(false);
        winnerWinnerChickenDinner();
    }

    @Override
    public void onWinnerUndo() {
        setAllScoreButtonsActive(true);
        db().deleteLastGame();
        uiRefreshGame();
        uiSyncTurn();
        uiSyncPlayerWins();
    }

    @Override
    public void onGameDataChanged() {
        uiRefreshGame();
    }

    @Override
    public void onBotThrow(boolean hit) {
        showLastScorePopup(false);
        uiSyncThrow();
    }

    @Override
    public void onNumberClosed() {
        Sound.play(Sound.SCRATCH);
    }

    @Override
    public void onBust(int number) {
        Objects.requireNonNull(getButtonFromNumber(number))
                .popSoundAnimate(SB_CODE_BUST_2, 0, false);
    }

    @Override
    public void on01MissedIn(int number) {
       int missedCode = 0;
       if(gdata().flags().get(GameFlags.GameFlag.X01_DOUBLE_IN)) missedCode = SB_CODE_MISSED_IN_DOUBLE;
       else if(gdata().flags().get(GameFlags.GameFlag.X01_TRIPLE_IN)) missedCode = SB_CODE_MISSED_IN_TRIPLE;
       else if(gdata().flags().get(GameFlags.GameFlag.X01_MASTER_IN)) missedCode = SB_CODE_MISSED_IN_MASTER;
        Objects.requireNonNull(getButtonFromNumber(number))
                .popSoundAnimate(missedCode, 0, false);
    }

    @Override
    public void onCricketCap() {
        Objects.requireNonNull(getLastScoredButton())
                .popSoundAnimate(SB_CODE_CRICKET_CAP, 0, false);
    }

    //
    // Baseball Input
    //

    private void onBaseballOut(int outs) {
        if(gdata().throwingPlayer().isBot()) return;

        if(outs > 3 - gdata().getBaseballOuts())
            outs = 3 - gdata().getBaseballOuts();
        for (int i = 0; i < outs; i++) {
            glogic().doScore(gdata().getCurrentTurn(), 0, 0);
        }

    }

    @Override
    public void onBaseBall(int multi) {
        if(multi == -1) { // undo
            mBaseballView.popSoundAnimate(0, 0, true);
        } else {
            mBaseballView.runBases(multi);
        }
    }

    @Override
    public void onOut() {
        uiSyncPlayerAvgs();
    }

    @Override
    public void onKiller(int code, int playerIndex) {
        if(code == GameLogic.KILL_CODE_PRE_HIT) {
            uiSyncKillerLayout(); getButtonFromNumber(playerIndex).popSoundAnimate(playerIndex, 4, false);  }
        else if(code == GameLogic.KILL_CODE_PRE_HIT_UNDO) {
            uiSyncKillerLayout(); getButtonFromNumber(playerIndex).popSoundAnimate(playerIndex, 4, true); }
        else if(code == GameLogic.KILL_CODE_START)
            uiStartKillerLayout();
        else if(code == GameLogic.KILL_CODE_KILLER)
            getKillerButton(playerIndex).popSoundAnimate(SB_CODE_KILLER, 0, false);
        else if(code == GameLogic.KILL_CODE_KILL)
            getKillButton(playerIndex).popSoundAnimate(SB_CODE_KILL, 0, false);
        else if(code == GameLogic.KILL_CODE_FATALITY)
            getKillButton(playerIndex).popSoundAnimate(SB_CODE_FATALITY, 0, false);
    }


    /** @return the current form fill mode. */
    private boolean isOpenScoring() {
        return gdata().isOpenScoring();
    }

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

    /** @return true if current game mode is Killer */
    private boolean isKiller() {
        return gdata().isKiller();
    }

    /** @return true if current game mode is Killer */
    private boolean isBaseball() {
        return gdata().isBaseball();
    }

    /** grab the global game data */
    private GameData gdata() {
        return ((DartScapeActivity)requireContext()).gameData();
    }

    /** grab the global player database */
    private PlayerDatabase db() {
        return ((DartScapeActivity)requireContext()).playerDB();
    }

    /** grab the global game logic */
    private GameLogic glogic() {
        return ((DartScapeActivity)requireContext()).gameLogic();
    }

    private FragManager frags() {
        return ((DartScapeActivity) requireContext()).frags();
    }
}