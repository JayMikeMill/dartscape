package com.gworks.dartscape.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.data.GameFlagSet;
import com.gworks.dartscape.data.PlayerStats;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.ui.StatsPlayerRecycleView;
import com.gworks.dartscape.util.Helper;

/*
    PLAYERS DIALOG
 */
public class StatsPlayerFragment extends Fragment  {

    private StatsPlayerRecycleView mRvPlayerStats;

    /** creates the view one time to save time. */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_stats_player, container, false);

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
    public void onStart() {
        super.onStart();
        resizeControls();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden) return;

        refreshStats();
    }

    @SuppressLint("SetTextI18n")
    public void setPlayerStats(PlayerStats playerStats, int since, String strSince, GameFlagSet flags) {
        ((TextView) requireView().findViewById(R.id.tvStatsPlayerGame))
                .setText(flags.getGameModeString());

        ((DartScapeActivity) requireContext())
                .frags().titleBar().setTitleText(playerStats.getName() + "'S STATS");

        ((TextView) requireView().findViewById(R.id.tvStatsPlayerDate))
                .setText(strSince + ((since == 876000) ? "" : ", " +  Helper.getTimeStamp(since)));

        mRvPlayerStats.fillWithPlayerStats(playerStats, flags.getGameFlag());
        mRvPlayerStats.scrollToPosition(0);
    }
    private void initControls() {
        mRvPlayerStats = requireView().findViewById(R.id.rvPlayerStats);
    }

    protected void resizeControls() {

    }

    public void refreshStats() {

    }



    AdapterView.OnItemSelectedListener mSpinListener = new AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            refreshStats();
        }

        @Override public void onNothingSelected(AdapterView<?> parent) {}
    };
    /** grab the global game data */
    private GameData gdata() {
        return ((DartScapeActivity)requireContext()).gameData();
    }

    private void onBack() {
        ((DartScapeActivity) requireContext()).frags().goBack();
    }

}
