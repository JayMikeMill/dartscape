package com.gworks.dartscape.fragments;

import static com.gworks.dartscape.data.Stats.*;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameFlagSet;
import com.gworks.dartscape.data.GameFlags;
import com.gworks.dartscape.data.PlayerStats;
import com.gworks.dartscape.database.PlayerDatabase;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.ui.StatsAllRecycleView;
import com.gworks.dartscape.ui.SuperSpinner;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
    PLAYERS DIALOG
 */
public class StatsFragment extends Fragment  {
    private SuperSpinner mSpinGame;
    private SuperSpinner mSpinGameMode;
    private SuperSpinner mSpinGameVariants;
    private SuperSpinner mSpinGameVariants2;
    private SuperSpinner mSpinStartTime;

    private SuperSpinner mSpinStat1;

    private StatsAllRecycleView mRvStats;

    /** creates the view one time to save time. */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_stats, container, false);

        super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize all game variables on first create.
        initControls();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) return;

        updateGameModeSpinners();
    }


    private void initControls() {
        // init game types
        mSpinGame  = requireView().findViewById(R.id.spinStatsGame);
        mSpinGame.init(GameFlags.getAllGames(), 0.5f);
        mSpinGame.setSelection(0);
        mSpinGame.setOnItemSelectedListener(mSpinGameListener);

        // init game modes
        mSpinGameMode  = requireView().findViewById(R.id.spinStatsGameMode);
        mSpinGameMode.init(GameFlags.getGameModes(GameFlags.GameFlag.ALL_GAMES), 0.5f);
        mSpinGameMode.setSelection(0);
        mSpinGameMode.setOnItemSelectedListener(mSpinGameModeListener);

        // init game variants
        mSpinGameVariants  = requireView().findViewById(R.id.spinStatsGameVariant);
        mSpinGameVariants.init(GameFlags.getGameVariants(GameFlags.GameFlag.ALL_GAMES, true, false), 0.5f);
        mSpinGameVariants.setSelection(0);
        mSpinGameVariants.setOnItemSelectedListener(mSpinGameModeListener);

        // init game variants
        mSpinGameVariants2  = requireView().findViewById(R.id.spinStatsGameVariant2);
        mSpinGameVariants2.init(GameFlags.getGameVariants(GameFlags.GameFlag.ALL_GAMES, true, true), 0.5f);
        mSpinGameVariants2.setSelection(0);
        mSpinGameVariants2.setOnItemSelectedListener(mSpinGameModeListener);

        // init start dates
        mSpinStartTime  = requireView().findViewById(R.id.spinStatsStartTime);
        mSpinStartTime.init(getStartTimes(), 0.5f);
        mSpinStartTime.setSelection(0);
        mSpinStartTime.setOnItemSelectedListener(mSpinGameModeListener);

        // init sort by spinner
        mSpinStat1 = requireView().findViewById(R.id.spinStatsStat1);
        mSpinStat1.init(getAllStatStrings(), 0.5f);
        mSpinStat1.setSelection(0);
        mSpinStat1.setOnItemSelectedListener(mSpinGameModeListener);

        mRvStats = requireView().findViewById(R.id.rvAllStats);
        mRvStats.setClickListener((view, position) -> showPlayerStats());

        updateGameModeSpinners();
    }

    private boolean isLoadingStats = false;

    private void uiSyncStatsList() {
        if(isLoadingStats) return;
        isLoadingStats = true;

        PlayerDatabase.DBHelper.runAsyncResult(() -> {
            // Load stats in background thread
            return db().getAllPlayerStats(getSelectedStartHours(), getGameFlags());
        }, stats -> {
            // Update UI on main thread
            mRvStats.fillWithAllPlayerStats(stats, getStat1());
            isLoadingStats = false;
        });
    }

    private void showPlayerStats() {
        if(isLoadingStats) return;
        isLoadingStats = true;

        PlayerDatabase.DBHelper.runAsyncResult(() -> {
            // background thread, DB-safe
            return db().getPlayerStats(
                    mRvStats.getSelectedName(),
                    getSelectedStartHours(),
                    getGameFlags()
            );
        }, stats -> {
            // UI thread here with result (PlayerStats stats)
            DartScapeActivity activity = (DartScapeActivity) requireContext();
            FragManager manager = activity.frags();

            manager.show(FragManager.FragId.FRAG_STATS_PLAYER);

            ((StatsPlayerFragment) manager.getFragFromId(FragManager.FragId.FRAG_STATS_PLAYER))
                    .setPlayerStats(
                            stats,
                            getSelectedStartHours(),
                            mSpinStartTime.getSelectedItem().toString(),
                            getGameFlags()
                    );

            isLoadingStats = false;
        });


    }

    private void updateGameModeSpinners() {
        GameFlags.GameFlag game = GameFlags.getFlagFromUiText(mSpinGame.getSelectedItem().toString());

        mSpinGameMode.clear();
        mSpinGameMode.addAll(GameFlags.getGameModes(game, true));
        mSpinGameMode.setSelection(0);

        mSpinGameMode.setVisibility(mSpinGameMode.isEmpty() ? View.GONE : View.VISIBLE);
        requireView().findViewById(R.id.spaceStats1).setVisibility
                (mSpinGameMode.isEmpty()  ? View.GONE : View.VISIBLE);

        mSpinGameVariants.setLeftAlign(game.equals(GameFlags.GameFlag.SHANGHAI));
        mSpinGameVariants.addAllKeepSel(GameFlags.getGameVariants(game, true, false));

        mSpinGameVariants2.setLeftAlign(game.equals(GameFlags.GameFlag.SHANGHAI));
        mSpinGameVariants2.addAllKeepSel(GameFlags.getGameVariants(game, true, true));

        requireView().findViewById(R.id.lStatsGameVariants).setVisibility(mSpinGameVariants.isEmpty() ? View.GONE : View.VISIBLE);
        mSpinGameVariants2.setVisibility(mSpinGameVariants2.isEmpty() ? View.GONE : View.VISIBLE);
        requireView().findViewById(R.id.spaceStats2).setVisibility
                (mSpinGameVariants2.isEmpty() ? View.GONE : View.VISIBLE);

        mSpinStat1.addAllKeepSel(getGameStatStrings(game));

        uiSyncStatsList();
    }


    public GameFlagSet getGameFlags() {
        GameFlagSet flags = new GameFlagSet();
        if (mSpinGame.getCount() > 0)
            flags.add(GameFlags.getFlagFromUiText(mSpinGame.getSelectedItem().toString()));
        if (mSpinGameMode.getCount() > 0)
            flags.add(GameFlags.getFlagFromUiText(mSpinGameMode.getSelectedItem().toString()));
        if (mSpinGameVariants.getCount() > 0)
            flags.add(GameFlags.getFlagFromUiText(mSpinGameVariants.getSelectedItem().toString()));
        if (mSpinGameVariants2.getCount() > 0)
            flags.add(GameFlags.getFlagFromUiText(mSpinGameVariants2.getSelectedItem().toString()));

        return flags;
    }


    public StatId getStat1() {
        return getStatId(mSpinStat1.getSelectedItem().toString());
    }

    public ArrayList<String> getStartTimes() {
        ArrayList<String> startTimes = new ArrayList<>();

        startTimes.add(getString(R.string.all_time));
        startTimes.add(getString(R.string.last_hour));
        startTimes.add(getString(R.string.last_day));
        startTimes.add(getString(R.string.last_week));
        startTimes.add(getString(R.string.last_month));
        startTimes.add(getString(R.string.last_year));

        return startTimes;
    }

    public int getSelectedStartHours() {
        String selectedText = mSpinStartTime.getSelectedItem().toString();

        if (selectedText.equals(getString(R.string.last_hour))) return 1;
        if (selectedText.equals(getString(R.string.last_day))) return 24;
        if (selectedText.equals(getString(R.string.last_week))) return 168;
        if (selectedText.equals(getString(R.string.last_month))) return 730;
        if (selectedText.equals(getString(R.string.last_year))) return 8760;
        if (selectedText.equals(getString(R.string.all_time))) return 876000;

        return 0;
    }

    AdapterView.OnItemSelectedListener mSpinGameListener = new AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            updateGameModeSpinners();
        }

        @Override public void onNothingSelected(AdapterView<?> parent) {}
    };

    AdapterView.OnItemSelectedListener mSpinGameModeListener = new AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            uiSyncStatsList();
        }

        @Override public void onNothingSelected(AdapterView<?> parent) {}
    };

    /** grab the global player database */
    private PlayerDatabase db() {
        return ((DartScapeActivity)requireContext()).playerDB();
    }

}
