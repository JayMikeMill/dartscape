package com.gworks.dartscape.fragments;

import static com.gworks.dartscape.main.Globals.MAX_PLAYERS;
import static com.gworks.dartscape.util.Helper.getResIdByName;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.gworks.dartscape.data.GameFlagSet;
import com.gworks.dartscape.data.GameFlags;
import com.gworks.dartscape.database.PlayerDatabase;
import com.gworks.dartscape.dialogs.ConfirmDialog;
import com.gworks.dartscape.fragments.FragManager.FragId;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.ui.MultiView;

import com.gworks.dartscape.logic.GameBot;
import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.R;

import com.gworks.dartscape.ui.SuperSpinner;
import com.gworks.dartscape.util.Helper;
import com.gworks.dartscape.util.UserPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    // init custom spinner adapter with icons
    List<Drawable> player_type_icons = new ArrayList<>();

    public class PlayerRow implements AdapterView.OnItemSelectedListener,
            SeekBar.OnSeekBarChangeListener {
        private final View mView;
        private final SuperSpinner mSpinTypeSlot;
        private final SuperSpinner mSpinPlayerSlot;
        private final SuperSpinner mSpinTeammateSlot;
        private final LinearLayout mSpinBotSlot;
        private final MultiView mMvBotLabel;
        private final SeekBar mSeekBotDiff;
        @SuppressLint("DiscouragedApi")

        public PlayerRow(@NonNull ViewGroup container,
                         @NonNull ArrayList<String> playerNames) {

            // Inflate layout, do NOT attach to container yet
            mView = LayoutInflater.from(container.getContext())
                    .inflate(R.layout.player_row, container, false);

            // Find views by ID
            mSpinTypeSlot     = mView.findViewById(R.id.spinPlayerRowTypeSlot);
            mSpinPlayerSlot   = mView.findViewById(R.id.spinPlayerRowSlot);
            mSpinTeammateSlot = mView.findViewById(R.id.spinPlayerRowTeammateSlot);
            mSpinBotSlot      = mView.findViewById(R.id.spinPlayerRowBotSlot);
            mSeekBotDiff      = mView.findViewById(R.id.seekPlayerRowBot);
            mMvBotLabel       = mView.findViewById(R.id.mvPlayerRowBotLabel);

            // init custom spinner adapter with icons
            if(player_type_icons.isEmpty()) {
                player_type_icons.add(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_users_duo));
                player_type_icons.add(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_user));;
                player_type_icons.add(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_bot));
            }

            mSpinTypeSlot.init(player_type_icons);
            mSpinTypeSlot.setSelection(TOG_PLAYER);

            // update player layout on slot toggle selected
            mSpinTypeSlot.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {toggleSlot();}
                @Override public void onNothingSelected(AdapterView<?> parent) {}});

            // Initialize spinners
            mSpinPlayerSlot.init(playerNames, 0.5f);
            mSpinPlayerSlot.setOnItemSelectedListener(this);

            mSpinTeammateSlot.init(playerNames, 0.5f);
            mSpinTeammateSlot.setOnItemSelectedListener(this);


            mSeekBotDiff.setOnSeekBarChangeListener(this);
            mSeekBotDiff.setProgress(0);

            uiSyncBot();

            // Finally add the inflated view to the container
            int insertIndex = Math.max(container.getChildCount() - 1, 0);
            container.addView(mView, insertIndex);
        }

        public void setRemoveListener(@NonNull View.OnClickListener removeListener) {
            // Setup remove button listener
            mView.findViewById(R.id.btnPlayerRowRemove).setOnClickListener(removeListener);
        }

        public View getView() {
            return mView;
        }

        private void toggleSlot() {
            int togState = mSpinTypeSlot.getSelectedItemPosition();

            mSpinPlayerSlot.setVisibility
                    (togState == TOG_PLAYER || togState == TOG_TEAMMATE ? View.VISIBLE : View.GONE);

            mSpinTeammateSlot.setVisibility
                    (togState == TOG_TEAMMATE ? View.VISIBLE : View.GONE);

            ((View)mSpinPlayerSlot.getParent()).invalidate();
            ((View)mSpinTeammateSlot.getParent()).invalidate();


            mSpinBotSlot.setVisibility
                    (togState == TOG_BOT ? View.VISIBLE : View.GONE);
        }

        private void uiSyncBot() {
            //String avg = String.format(Locale.US, "%.2f", botStats.getScoreAvgRound() * .92777f);
            //String mpr = String.format(Locale.US, "%.2f", botStats.getMarkAvgRound());
            String dif = String.format(Locale.US, "(%.2f)", getBotAverage());

            if(!UserPrefs.getOwnsPro() && (getBotDifficulty() > 0.5f))
                dif = String.format(Locale.US, "(%s)", getString(R.string.pro_only));

            //mMvBotLabels[index].setMultiText(mpr + " - " + dif + " - " + avg);
            mMvBotLabel.setMultiText(getBotName() + " " + dif);
        }

        private String getBotName() {
            return GameBot.getBotDiffName(mSeekBotDiff.getProgress() *.001f);
        }

        private float getBotDifficulty() {
            int progress = mSeekBotDiff.getProgress();
            if(progress == 0) return 0.001f;
            return (mSeekBotDiff.getProgress() *.001f);
        }

        private float getBotAverage() {
            GameBot bot = ((DartScapeActivity) requireContext()).gameLogic().bot();
            return bot.getBotAvg(getBotDifficulty(), getSelectedGame().equals(GameFlags.GameFlag.X01));
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switchNameDuplicate(this);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            uiSyncBot();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(seekBar.getProgress() > 500 && !UserPrefs.getOwnsPro()) {
                seekBar.setProgress(500);
            }
        }
    }

    private static final int TOG_TEAMMATE = 0;
    private static final int TOG_PLAYER = 1;
    private static final int TOG_BOT = 2;

    private SuperSpinner mSpinGame;
    private SuperSpinner mSpinGameMode;
    private SuperSpinner mSpinGameVariants;
    private SuperSpinner mSpinGameVariants2;


    private ArrayList<PlayerRow> mPlayerRows = new ArrayList<>();
    private LinearLayout mlPlayerLayout;
    private MultiView mBtnAddPlayer;
    private MultiView mBtnRemovePlayer;

    private MultiView mBtnStartGame;
    private MultiView mBtnReturnToGame;

    private SuperSpinner mSpinLegs;
    private ArrayList<String> mLegsItems;
    private ArrayList<String> mLegRaceItems;
    private SuperSpinner mSpinSets;
    private ArrayList<String> mSetsItems;

    private boolean mSyncPlayers = false;

    private ArrayList<String> mAllPlayerNames = new ArrayList<>();

    /** creates the view one time to save time. */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_home, container, false);

        super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAllPlayerNames = db().getAllPlayerNames();

        initControls();

        GameData restoredSettings = UserPrefs.restoreGameSettings();
        uiSyncWithGameData(restoredSettings == null ? gdata() : restoredSettings);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {

        super.onHiddenChanged(hidden);

        if (hidden) return;
        uiSyncStartGameLayout(gdata(), false);

        int animDuration = getResources().getInteger(android.R.integer.config_shortAnimTime) + 50;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Start your loading here after the animation duration delay
            if (mSyncPlayers) {
                PlayerDatabase.DBHelper.runAsyncResult(() -> {
                    // Load stats in background thread
                    return db().getAllPlayerNames();
                }, stats -> {
                    mAllPlayerNames = stats;
                    refreshPlayerNames();
                    uiSyncWithGameData(gdata());
                });

                mSyncPlayers = false;
            }
        }, animDuration);
    }

    public void syncPlayersOnShow() {
        mSyncPlayers = true;
    }

    public void backupGameSettings() {
        UserPrefs.backupGameSettings(getGameDataFromUI());
    }

    private void refreshPlayerNames() {
        // This runs on UI thread
        for (PlayerRow row : mPlayerRows) {
            int oldIndex = row.mSpinPlayerSlot.getSelectedItemPosition();
            row.mSpinPlayerSlot.clear();
            row.mSpinPlayerSlot.addAll(mAllPlayerNames);
            row.mSpinPlayerSlot.setSelection(Math.min(oldIndex, row.mSpinPlayerSlot.getCount() - 1));

            oldIndex = row.mSpinTeammateSlot.getSelectedItemPosition();
            row.mSpinTeammateSlot.clear();
            row.mSpinTeammateSlot.addAll(mAllPlayerNames);
            row.mSpinTeammateSlot.setSelection(Math.min(oldIndex, row.mSpinTeammateSlot.getCount() - 1));
        }
    }

    @SuppressLint("DiscouragedApi")
    protected void initControls() {
        mSpinGame = requireView().findViewById(R.id.spinHomeGame);
        mSpinGameMode  = requireView().findViewById(R.id.spinHomeGameMode);
        mSpinGameVariants  = requireView().findViewById(R.id.spinHomeGameVariant);
        mSpinGameVariants2  = requireView().findViewById(R.id.spinHomeGameVariant2);
        mBtnStartGame = requireView().findViewById(R.id.btnHomeStartGame);
        mBtnReturnToGame =  requireView().findViewById(R.id.btnHomeReturnToGame);
        mSpinLegs = requireView().findViewById(R.id.spinHomeLegs);
        mSpinSets = requireView().findViewById(R.id.spinHomeSets);

        mlPlayerLayout = requireView().findViewById(R.id.lHomePlayers);

        mBtnAddPlayer = requireView().findViewById(R.id.btnHomeNewPlayer);
        mBtnRemovePlayer = requireView().findViewById(R.id.btnHomeRemovePlayer);

        mSpinGame.init(GameFlags.getGames(false), 0.6f);
        mSpinGameMode.init(GameFlags.getGameModes(GameFlags.GameFlag.ALL_GAMES, false), 0.6f);
        mSpinGameVariants.init(GameFlags.getGameVariants(GameFlags.GameFlag.ALL_GAMES, false, true), 0.5f);
        mSpinGameVariants2.init(GameFlags.getGameVariants(GameFlags.GameFlag.ALL_GAMES, true, true), 0.5f);

        mSpinGame.setSelection(0);
        mSpinGameMode.setSelection(0);
        mSpinGameVariants.setSelection(0);
        mSpinGameVariants2.setSelection(0);

        mSpinGame.setOnItemSelectedListener(this);
        mSpinGameMode.setOnItemSelectedListener(this);
        mSpinGameVariants.setOnItemSelectedListener(this);

        // start game options
        mBtnStartGame.setOnClickListener(v -> onStartGame());
        mBtnReturnToGame.setOnClickListener(v -> {
            frags().goBack(); updateUiAfterTransition();
        } );

        initItemLists();

        mSpinLegs.init(mLegRaceItems, 0.5f);
        mSpinLegs.setOnItemSelectedListener(this);


        mSpinSets.init(mSetsItems, 0.5f);
        mSpinSets.setOnItemSelectedListener(this);

        mSpinLegs.setSelection(0, false, true);
        mSpinSets.setSelection(0, false, true);

        mBtnAddPlayer.setOnClickListener(v -> addNewPlayer());
        mBtnRemovePlayer.setOnClickListener(v -> removeLastPlayer());

        // bottom navigation bar
        requireView().findViewById(R.id.btnHomePlayers).setOnClickListener(v -> onShowPlayers());
        requireView().findViewById(R.id.btnHomeStats).setOnClickListener(v -> onShowStats());
        requireView().findViewById(R.id.btnHomeSettings).setOnClickListener(v -> onShowSettings());
        requireView().findViewById(R.id.btnHomeHelp).setOnClickListener(v -> onShowHelp());

        updateAddRemovePlayerButtons();
    }

    private void initItemLists() {
        mLegsItems = new ArrayList<>();
        mLegsItems.add(getString(R.string.no) + " " + getString(R.string.legs));
        for (int i = 1; i <= 15; i += 2)
            mLegsItems.add(i + (i == 1 ? " " + getString(R.string.leg) : " " + getString(R.string.legs)));

        mLegRaceItems = new ArrayList<>();
        mLegRaceItems.add(getString(R.string.no) + " " + getString(R.string.legs));
        for (int i = 1; i <= 21; i++)
            mLegRaceItems.add(i + (i == 1 ? " " + getString(R.string.leg) : " " + getString(R.string.legs)));

        mSetsItems = new ArrayList<>();
        mSetsItems.add(getString(R.string.leg) + " " + getString(R.string.race));
        for (int i = 1; i <= 15; i += 2)
            mSetsItems.add(i + (i == 1 ? " " + getString(R.string.set) : " " + getString(R.string.sets)));

    }

    public void addNewPlayer() {
        if(mPlayerRows.size() >= MAX_PLAYERS) return;
        PlayerRow newRow = new PlayerRow(mlPlayerLayout, mAllPlayerNames);

        newRow.setRemoveListener(v -> removePlayer(newRow));
        mPlayerRows.add(newRow);

        updateAddRemovePlayerButtons();
    }

    public void removePlayer(PlayerRow row) {
        ViewGroup container = (ViewGroup) row.getView().getParent();

        if (container != null) {
            container.removeView(row.getView());
        }

        mPlayerRows.remove(row);

        updateAddRemovePlayerButtons();
    }

    public void removeLastPlayer() {
        if(mPlayerRows.isEmpty()) return;
        removePlayer(mPlayerRows.get(mPlayerRows.size() - 1));
    }

    public void updateAddRemovePlayerButtons() {
        int playerCount = mPlayerRows.size();
        mBtnAddPlayer.setVisibility(playerCount < MAX_PLAYERS ? View.VISIBLE : View.GONE);
        mBtnRemovePlayer.setVisibility(playerCount > 0 ? View.VISIBLE : View.GONE);

        requireView().findViewById(R.id.spacerHomePlayerAddRemove)
                .setVisibility(playerCount > 0 && playerCount < MAX_PLAYERS ? View.VISIBLE : View.GONE);;
    }

    public void uiSyncWithGameData(GameData data) {
        // init cricket settings controls
        updateGameModeSpinners(data, data.flags().getGameModeFlag());
        uiSyncStartGameLayout(data, true);
        updateLegsSetsSpinners(data);

        int active_players = data.getActivePlayerCount();
        if(mPlayerRows.size() < active_players) {
            int need_to_add = active_players - mPlayerRows.size();
            for(int i = 0; i < need_to_add; i++) addNewPlayer();
        } else if (mPlayerRows.size() > active_players) {
            int need_to_remove = mPlayerRows.size() - active_players;
            for(int i = 0; i < need_to_remove; i++) removeLastPlayer();
        }

        // init toggle buttons
        int index = 0;
        for (PlayerRow row : mPlayerRows) {
            int togState =  data.player(index).isTeam() ? TOG_TEAMMATE :
                            data.player(index).isBot()  ? TOG_BOT : TOG_PLAYER;
            row.mSpinTypeSlot.setSelection(togState);
            index++;
        }

        toggleSlots();

        uiSyncPlayerNamesWithGameData(data);

        // init bot spinners
        index = 0;
        for (PlayerRow row : mPlayerRows) {
            if (data.player(index).isBot()) {
                row.mSeekBotDiff.setProgress((int) (data.player(index).botDiff() * 1000));
                row.uiSyncBot();
            }
            index++;
        }
    }

    private void uiSyncPlayerNamesWithGameData(GameData data) {
        int player_index = 0;
        for (PlayerRow row : mPlayerRows) {
            int index = mAllPlayerNames.indexOf(data.player(player_index).getName());
            row.mSpinPlayerSlot.setSelection(index != -1 ? index : Math.min(player_index, mAllPlayerNames.size()-1), false, true);
            if (data.player(player_index).isTeam()) {
                int indexT = mAllPlayerNames.indexOf(data.player(player_index).getTeammateName());
                row.mSpinTeammateSlot.setSelection(indexT != -1 ? indexT : Math.min(player_index, mAllPlayerNames.size()-1), false, true);
            }
            player_index++;
        }
    }

    private void toggleSlots() {
        for (PlayerRow row : mPlayerRows)
            row.toggleSlot();
    }

    private void updateGameModeSpinners(GameData data, GameFlags.GameFlag game) {
        if(game == GameFlags.GameFlag.NONE)
            game = GameFlags.getFlagFromUiText(mSpinGame.getSelectedItem().toString());

        mSpinGameMode.clear();
        mSpinGameMode.addAll(GameFlags.getGameModes(game, false));

        mSpinGameVariants.clear();
        mSpinGameVariants.addAll(GameFlags.getGameVariants(game, false, false));
        mSpinGameVariants.setLeftAlign(game.equals(GameFlags.GameFlag.SHANGHAI));
        mSpinGameVariants.setSelection(0);

        mSpinGameVariants2.setLeftAlign(game.equals(GameFlags.GameFlag.SHANGHAI));

        mSpinGameMode.setVisibility(mSpinGameMode.isEmpty() ? View.GONE : View.VISIBLE);
        requireView().findViewById(R.id.spaceHome1).setVisibility
                (mSpinGameMode.isEmpty()  ? View.GONE : View.VISIBLE);

        uiSyncGameModeSelection(data, game);
        updateGameVariantSpinners();
        uiSyncGameModeSelection(data, game);

        uiSyncBots();
    }

    private void updateGameVariantSpinners() {
        GameFlags.GameFlag game = GameFlags.getFlagFromUiText(mSpinGame.getSelectedItem().toString());
        GameFlags.GameFlag mode = !mSpinGameMode.isEmpty() ?
                GameFlags.getFlagFromUiText(mSpinGameMode.getSelectedItem().toString()) : GameFlags.GameFlag.NONE;
        mSpinGameVariants2.clear();

        if(!mode.equals(GameFlags.GameFlag.CRICKET_CUT_THROAT))
            mSpinGameVariants2.addAll(GameFlags.getGameVariants(game, false, true));

        requireView().findViewById(R.id.lHomeVariant).setVisibility(mSpinGameVariants.isEmpty() ? View.GONE : View.VISIBLE);
        mSpinGameVariants2.setVisibility(mSpinGameVariants2.isEmpty() ? View.GONE : View.VISIBLE);
        requireView().findViewById(R.id.spaceHome2).setVisibility
                (mSpinGameVariants2.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public void uiSyncGameModeSelection(GameData data, GameFlags.GameFlag game) {
        // init cricket settings controls
        mSpinGame.setSelection(data.flags().getGameIndex(game));

        if (game.equals(GameFlags.GameFlag.X01)) {
            mSpinGameMode.setSelection(data.flags().get01ModeIndex(), false, true);
            mSpinGameVariants.setSelection(data.flags().get01VariantInIndex(), false, true);
            mSpinGameVariants2.setSelection(data.flags().get01VariantOutIndex(), false, true);
        } else if (game.equals(GameFlags.GameFlag.CRICKET)) {
            mSpinGameMode.setSelection(data.flags().getCricketModeIndex(), false, true);
            mSpinGameVariants.setSelection(data.flags().getCricketVariantIndex(), false, true);
            mSpinGameVariants2.setSelection(data.getCricketSpreadLimitIndex(), false, true);
        } else if (game.equals(GameFlags.GameFlag.SHANGHAI)) {
            mSpinGameVariants.setSelection(data.getShanghaiStartNumber() - 1, false, true);
            mSpinGameVariants2.setSelection(20 - data.getShanghaiEndNumber(), false, true);
        }else if (game.equals(GameFlags.GameFlag.KILLER)) {
            mSpinGameMode.setSelection(data.flags().getKillerVariantIndex(), false, true);
            mSpinGameVariants.setSelection(data.getKillerStartLives() - 1);
        } else if (game.equals(GameFlags.GameFlag.BASEBALL)) {
            mSpinGameVariants.setSelection(data.getBaseballInnings() - 1);
        }
    }

    private void updateLegsSetsSpinners(GameData data) {
        mSpinLegs.addAll(data.isLegRace() ? mLegRaceItems : mLegsItems);
        mSpinLegs.setSelection(data.isLegRace() ? data.getLegs()
                : Math.round(data.getLegs() / 2f));
        mSpinSets.setSelection(Math.round(data.getSets() / 2f));
    }

    private void uiSyncBots() {
        for (PlayerRow row : mPlayerRows)
            row.uiSyncBot();
    }

    private GameFlags.GameFlag getSelectedGame() {
        return GameFlags.getFlagFromUiText(mSpinGame.getSelectedItem().toString());
    }

    @SuppressLint("SetTextI18n")
    private void uiSyncStartGameLayout(@NonNull GameData data, boolean setLegRace) {
        mSpinSets.setVisibility(data.isLegs()  ?  View.VISIBLE : View.GONE);
        requireView().findViewById(R.id.spaceHomeLegsSets)
                .setVisibility(data.isLegs()  ?  View.VISIBLE : View.GONE);

        if(!data.isLegs()) {
            mSpinLegs.addAll(mLegRaceItems);
            mSpinLegs.setSelection(0, false, true);
            mSpinSets.setSelection(0);
        }

        if(setLegRace)  {
            int sel = mSpinLegs.getSelectedItemPosition();
            mSpinLegs.addAll(data.isLegRace() ? mLegRaceItems : mLegsItems);
            mSpinLegs.setSelection(Math.min(sel, mSpinLegs.getCount() - 1));
        }

        mBtnReturnToGame.setVisibility(gdata().isGameGoing() ? View.VISIBLE : View.GONE);
        requireView().findViewById(R.id.spaceHomeReturnStart)
                .setVisibility(gdata().isGameGoing() ? View.VISIBLE : View.GONE);

        mBtnStartGame.setText(getString(R.string.start) + "\n" + (data.isLegs() ?
                getString(R.string.match) : getString(R.string.game)));
    }


    public void onStartGame() {
        GameData gameData = getGameDataFromUI();

        //
        // check if game settings are legit
        //

        GameFlagSet flags = gameData.flags();

        String errorText = "";

        if (flags.get(GameFlags.GameFlag.CRICKET_OPEN) && gameData.getBotCount() > 0)
            errorText = getString(R.string.dialog_no_bots_open_mode);
        else if (flags.get(GameFlags.GameFlag.KILLER)) {
            if (gameData.getBotCount() > 0)
                errorText = getString(R.string.dialog_no_bots_killer_mode);
            if (gameData.getActivePlayerCount() < 2)
                errorText = getString(R.string.dialog_killer_mode_2_player);
        } else if (gameData.getActivePlayerCount() < 1)
            errorText = getString(R.string.dialog_must_have_1_player);

        // make sure there are not duplicate players
        if(checkNameDuplicates(gameData)) {
            errorText = getString(R.string.dialog_unique_players);
        }

        if (!errorText.isEmpty()) {
            Helper.dialogs().showConfirm(getString(R.string.invalid_settings), R.drawable.ic_help, errorText);
            return;
        }

        if (gdata().isGameGoing()) {
            // show on quit confirm dialog
            Helper.dialogs().showConfirm(getString(R.string.game_in_play), R.drawable.ic_help,
                    getString(R.string.dialog_game_will_be_lost),
                    getString(R.string.yes), getString(R.string.no), false, dialog -> {
                        if(((ConfirmDialog)dialog).didConfirm()) startNewGame(); } );

            return;
        }

        startNewGame();

    }

    private boolean checkNameDuplicates(GameData data) {

        ArrayList<String> names = new ArrayList<>();

        for (int i = 0; i < MAX_PLAYERS; i++) {
            if(!data.player(i).isActive() || data.player(i).isBot()) continue;

            String name = data.player(i).getName();
            if(!name.isEmpty() && names.contains(name))
                return true;

            names.add(name);

            if(data.player(i).isTeam()) {
                name = data.player(i).getTeammateName();
                if(!name.isEmpty() && names.contains(name))
                    return true;
                names.add(name);
            }
        }

        return false;
    }

    private void switchNameDuplicate(PlayerRow row) {
    }

   /* private void switchNameDuplicate2(SuperSpinner spinner) {
        if(spinner.isEmpty()) return;

        String prevSelName = spinner.getPreviousSelectedItem();
        String selName = spinner.getSelectedItem().toString();
        String name;

        int index = 0;
        for (PlayerRow row : mPlayerRows) {
            if(mSpinPlayerSlots[i].equals(spinner)) continue;

            if(!mSpinPlayerSlots[i].isEmpty()) {
                name = mSpinPlayerSlots[i].getSelectedItem().toString();
                if (selName.equals(name))
                    mSpinPlayerSlots[i].setSelection
                            (mSpinPlayerSlots[i].getFirstItemPosition(prevSelName), false, true);
            }

            if(mSpinTeammateSlots[i].equals(spinner)) continue;

            if(!mSpinTeammateSlots[i].isEmpty()) {
                name = mSpinTeammateSlots[i].getSelectedItem().toString();
                if (selName.equals(name))
                    mSpinTeammateSlots[i].setSelection
                            (mSpinTeammateSlots[i].getFirstItemPosition(prevSelName), false, true);
            }
        }
    }*/

    private GameData getGameDataFromUI() {
        return updateGameDataFromUI(false);
    }

    private GameData updateGameDataFromUI(boolean update) {
        GameData data  = new GameData();

        if(update) gdata().flags().clearAll();

        if(!mSpinGame.isEmpty()) {
            data.flags().add(GameFlags.getFlagFromUiText(mSpinGame.getSelectedItem().toString()));
            if(update) gdata().flags().add(GameFlags.getFlagFromUiText(mSpinGame.getSelectedItem().toString()));
        }

        if(!mSpinGameMode.isEmpty()) {
            data.flags().add(GameFlags.getFlagFromUiText(mSpinGameMode.getSelectedItem().toString()));
            if(update) gdata().flags().add(GameFlags.getFlagFromUiText(mSpinGameMode.getSelectedItem().toString()));
        }

        if(!mSpinGameVariants.isEmpty()) {
            if (data.isShanghai()) {
                data.setShanghaiStartNumber(mSpinGameVariants.getSelectedItemPosition() + 1);
                if(update) gdata().setShanghaiStartNumber(mSpinGameVariants.getSelectedItemPosition() + 1);
            } else if (data.isKiller()) {
                data.setKillerStartLives(mSpinGameVariants.getSelectedItemPosition() + 1);
                if(update) gdata().setKillerStartLives(mSpinGameVariants.getSelectedItemPosition() + 1);
            } else if (data.isBaseball()) {
                data.setBaseballInnings(mSpinGameVariants.getSelectedItemPosition() + 1);
                if(update) gdata().setBaseballInnings(mSpinGameVariants.getSelectedItemPosition() + 1);
            } else {
                data.flags().add(GameFlags.getFlagFromUiText(mSpinGameVariants.getSelectedItem().toString()));
                if(update) gdata().flags().add(GameFlags.getFlagFromUiText(mSpinGameVariants.getSelectedItem().toString()));
            }
        }

        if(!mSpinGameVariants2.isEmpty()) {
            if (data.isShanghai()) {
                data.setShanghaiEndNumber(20 - mSpinGameVariants2.getSelectedItemPosition());
                if(update) gdata().setShanghaiEndNumber(20 - mSpinGameVariants2.getSelectedItemPosition());
            } else if (data.isCricket()) { // cricket spread limit
                data.flags().add(GameFlags.getFlagFromUiText(mSpinGameVariants.getSelectedItem().toString()));
                data.setCricketSpreadLimit(mSpinGameVariants2.getSelectedItemPosition() * 100);
                if(update) gdata().flags().add(GameFlags.getFlagFromUiText(mSpinGameVariants.getSelectedItem().toString()));
                if(update) gdata().setCricketSpreadLimit(mSpinGameVariants2.getSelectedItemPosition() * 100);
            } else {
                data.flags().add(GameFlags.getFlagFromUiText(mSpinGameVariants2.getSelectedItem().toString()));
                if(update) gdata().flags().add(GameFlags.getFlagFromUiText(mSpinGameVariants2.getSelectedItem().toString()));
            }
        } else {
            data.setCricketSpreadLimit(0);
            if(update) gdata().setCricketSpreadLimit(0);
        }

        // update player names from fields
        int player_index = 0;
        for (PlayerRow row : mPlayerRows) {
            int togState = row.mSpinTypeSlot.getSelectedItemPosition();

            data.player(player_index).setActive(true);
            if(update) gdata().player(player_index).setActive(true);

            switch (togState) {
                case TOG_PLAYER:
                    data.player(player_index).setName(row.mSpinPlayerSlot.getSelectedItem().toString());
                    data.player(player_index).setTeammateName("");
                    data.player(player_index).setBotDiff(0);
                    if(update) gdata().player(player_index).setName(row.mSpinPlayerSlot.getSelectedItem().toString());
                    if(update) gdata().player(player_index).setTeammateName("");
                    if(update) gdata().player(player_index).setBotDiff(0);
                    break;
                case TOG_TEAMMATE:
                    data.player(player_index).setName(row.mSpinPlayerSlot.getSelectedItem().toString());
                    data.player(player_index).setTeammateName(row.mSpinTeammateSlot.getSelectedItem().toString());
                    data.player(player_index).setBotDiff(0);
                    if(update) gdata().player(player_index).setName(row.mSpinPlayerSlot.getSelectedItem().toString());
                    if(update) gdata().player(player_index).setTeammateName(row.mSpinTeammateSlot.getSelectedItem().toString());
                    if(update) gdata().player(player_index).setBotDiff(0);
                    break;
                case TOG_BOT:
                    String dif = String.format(Locale.US, "(%.2f)", row.getBotAverage());
                    data.player(player_index).setName(row.getBotName() + "\nBOT " + dif);
                    data.player(player_index).setTeammateName("");
                    data.player(player_index).setBotDiff(row.getBotDifficulty());
                    if(update) gdata().player(player_index).setName(row.getBotName() + "\nBOT " + dif);
                    if(update) gdata().player(player_index).setTeammateName("");
                    if(update) gdata().player(player_index).setBotDiff(row.getBotDifficulty());
            }

            player_index++;
        }

        for(int i = mPlayerRows.size(); i < MAX_PLAYERS; i++) {
            data.player(i).setActive(false);
            if(update) gdata().player(i).setActive(false);

            gdata().player(i).setName("");
            data.player(i).setTeammateName("");
            data.player(i).setBotDiff(0);
            if(update) gdata().player(i).setName("");
            if(update) gdata().player(i).setTeammateName("");
            if(update) gdata().player(i).setBotDiff(0);
        }

        int selSet = mSpinSets.getSelectedItemPosition();
        int selLeg = mSpinLegs.getSelectedItemPosition();
        boolean legRace = selSet == 0;
        int legs = selLeg == 0 ? 0 : legRace ? selLeg : (selLeg * 2) - 1;
        int sets = selSet == 0 ? 0 : (selSet * 2) - 1;

        data.setLegs(legs);
        data.setSets(sets);
        if(update) gdata().setLegs(legs);
        if(update) gdata().setSets(sets);

        return data;
    }


    private void startNewGame() {
        updateGameDataFromUI(true);

        //if(newSet) gdata().flags().set(GameFlag.STARTED_NEW_SET, true);
        gdata().flags().set(GameFlags.GameFlag.STARTED_NEW_GAME, true);
        frags().show(FragId.FRAG_GAME);
        updateUiAfterTransition();
    }

    public void updateUiAfterTransition() {
        int animDuration = getResources().getInteger(android.R.integer.config_shortAnimTime) + 50;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            uiSyncWithGameData(gdata());

            View root = getView();
            if (root != null) {
                root.measure(
                        View.MeasureSpec.makeMeasureSpec(root.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(root.getHeight(), View.MeasureSpec.EXACTLY)
                );
                root.layout(root.getLeft(), root.getTop(), root.getRight(), root.getBottom());
                root.invalidate(); // Forces redraw
            }
        }, animDuration);

    }

    private void showGameFrag() {

        frags().show(FragId.FRAG_GAME);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SuperSpinner spinner = (SuperSpinner) parent;

        if(spinner.equals(mSpinGame)) {
            updateGameModeSpinners(gdata(), GameFlags.GameFlag.NONE); return; }

        if(spinner.equals(mSpinGameMode) || spinner.equals(mSpinGameVariants)) {
            updateGameVariantSpinners(); return; }

        if(spinner.equals(mSpinLegs)) {
            uiSyncStartGameLayout(getGameDataFromUI(), false); return; }
        else if(spinner.equals(mSpinSets)) {
            uiSyncStartGameLayout
                    (getGameDataFromUI(),
                            spinner.getPreviousSelectedPosition() == 0 ||
                                    spinner.getSelectedItemPosition() == 0); return; }
    }


    private void onShowPlayers() {
        // show on quit confirm dialog
        if(!gdata().isGameGoing()) {
            frags().show(FragId.FRAG_PLAYERS);
            syncPlayersOnShow();
            return;
        }

        Helper.dialogs().showConfirm(getString(R.string.game_in_play),
                getString(R.string.dialog_cant_edit_players),
                getString(R.string.yes), false,
                dialog -> {
                    if(((ConfirmDialog) dialog).didConfirm()) {
                        gdata().setGameStarted(false);
                        frags().show(FragId.FRAG_PLAYERS);
                    }
                });
    }

    private void onShowStats() {
        frags().show(FragId.FRAG_STATS);
    }

    private void onShowSettings() {
        frags().show(FragId.FRAG_SETTINGS);
    }

    private void onShowHelp() { frags().show(FragId.FRAG_HELP); }


    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    /** grab the global game data */
    private GameData gdata() {
        return ((DartScapeActivity)requireContext()).gameData();
    }
    
    /** grab the global player database */
    private PlayerDatabase db() {
        return ((DartScapeActivity)requireContext()).playerDB();
    }

    private FragManager frags() {
        return ((DartScapeActivity) requireContext()).frags();
    }


}
