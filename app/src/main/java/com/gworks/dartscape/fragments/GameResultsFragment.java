package com.gworks.dartscape.fragments;

import static com.gworks.dartscape.main.Globals.MAX_PLAYERS;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.data.Player;
import com.gworks.dartscape.data.PlayerStats;
import com.gworks.dartscape.data.Stats;
import com.gworks.dartscape.data.Stats.StatId;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.ui.MultiView;
import com.gworks.dartscape.util.MyColors;

import java.util.ArrayList;

/*
    PLAYERS DIALOG
 */
public class GameResultsFragment extends Fragment {

    private Animation mAniBounce, mAniSpin;
    private TextView mTvWinnerText;
    private ImageView mIvTrophy;

    private TextView mTvGameType;

    private LinearLayout lStats;

    MultiView mBtnStartGame;

    /** creates the view one time to save time. */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_game_result, container, false);

        super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // init control animations
        mAniBounce = AnimationUtils.loadAnimation(getContext(), R.anim.scale_in);
        mAniSpin   = AnimationUtils.loadAnimation(getContext(), R.anim.winner_text_spin);

        initControls();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(gdata().isWinner()) loadResults();
    }

    private void initControls() {
        // Adding the gif here using glide library
        mTvWinnerText = requireView().findViewById(R.id.tvGameResultsWinner);
        mIvTrophy = requireView().findViewById(R.id.ivGameResultTrophy);

        mTvGameType = requireView().findViewById(R.id.tvGameResultGameMode);

        lStats = requireView().findViewById(R.id.lDiagGameResultStats);

        requireView().findViewById(R.id.btnDiagGameResultBack).setOnClickListener
                (v -> frags().goBack());

        mBtnStartGame = requireView().findViewById(R.id.btnDiagGameResultNewGame);
        mBtnStartGame.setOnClickListener(v -> onStartGame());
    }

    public void loadResults() {
        String strWinner = gdata().isWinner() ? gdata().getPlayerUiName(gdata().getWinnerIndex()) : "";


        if(gdata().isWinner() && gdata().winner().isTeam())
            strWinner = gdata().winner().getName() +
                    "\n" + getString(R.string.and) + "\n" + gdata().winner().getTeammateName();

        String resultText = strWinner;
        if(gdata().isLegs())
            if(gdata().isMatchSetWinner())
                resultText   += "\n" + getString(R.string.won_the) + " " + getString(R.string.match) + "!";
            else if(gdata().isSetWinner())
                resultText += "\n" + getString(R.string.won_the) + " " + getString(R.string.set) + "!";
            else resultText += "\n" + getString(R.string.won_the) + " " + getString(R.string.leg) + "!";
        else  resultText += "\n" + getString(R.string.won_the) + " " + getString(R.string.game) + "!";

        mTvWinnerText.setMaxLines(gdata().winner().isTeam() ? 4 : gdata().winner().isBot() ? 3 : 2);
        mTvWinnerText.setText(resultText);

        mTvWinnerText.startAnimation(mAniSpin);
        mIvTrophy.startAnimation(mAniBounce);

        String gameType = gdata().getGameTypeString();
        if(gdata().isLegs()) gameType += "\n" + gdata().getLegsSetsUiString();
        mTvGameType.setText(gameType);


        String startText = getString(R.string.play) + "\n" + getString(R.string.again);
        if(gdata().isLegs())
            if(gdata().isMatchSetWinner()) startText = getString(R.string.play) + "\n" + getString(R.string.again);
            else if(gdata().isSetWinner()) startText = getString(R.string.next) + "\n" + getString(R.string.set);
            else startText = getString(R.string.next) + "\n" + getString(R.string.leg);

        mBtnStartGame.setMultiText(startText);

        loadStats();
    }


    @SuppressLint("SetTextI18n")
    private void loadStats() {
        LayoutInflater inflater = getLayoutInflater();

        if(gdata().isOpenScoring()) {
            lStats.removeAllViews();
            addNoStatsView(inflater, lStats);
            return;
        }

        ArrayList<StatId> statsIds = Stats.getGameResultsStatIds(gdata().getGameFlag());

        // inflate (create) another copy of our custom layout

        lStats.removeAllViews();

        for (int i = 0; i < MAX_PLAYERS + 1; i++) {

            LinearLayout col = new LinearLayout(getContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

            if(i == MAX_PLAYERS) {
                col.setBackgroundColor(MyColors.themePrimary());
                addMultiView(inflater, col, getString(R.string.game) +
                        "\n" + getString(R.string.stats), MyColors.themeControl());
                if(gdata().isLegs()) {
                    String legsText = gdata().isLegRace() ? getString(R.string.legs) + "\n" + getString(R.string.won)
                    : getString(R.string.legs) +"\\" + getString(R.string.sets) + "\n" + getString(R.string.won);
                    addMultiView(inflater, col, legsText,  MyColors.themeControl());
                }
                for (int j = 0; j < statsIds.size(); j++) {
                    addMultiView(inflater, col, Stats.getStatString
                            (statsIds.get(j)).replace(" ", "\n").replace("/", "/\n"),  MyColors.themeControl());
                }

                int playerCount = gdata().getActivePlayerCount();
                int STAT_COL = playerCount == 2 ? 1 : playerCount == 4 ? 2 : 0;
                lStats.addView(col, STAT_COL);

            } else {
                Player player = gdata().player(i);
                if(!player.isActive()) continue;

                addMultiView(inflater, col, player.getName(), MyColors.themePrimary());

                if(gdata().isLegs()) {
                    String legsText = player.getWins() + (gdata().isSetMatch() ?
                            "\\" + player.getSetWins() : "");
                    addMultiView(inflater, col, legsText,  MyColors.themePrimary());
                }

                PlayerStats stats = gdata().stats(i);
                for (int j = 0; j < statsIds.size(); j++) {
                    addMultiView(inflater, col, stats.getStatString(statsIds.get(j)), MyColors.themePrimary());
                }

                lStats.addView(col);
            }
        }
    }

    private MultiView addNoStatsView(LayoutInflater inflater, ViewGroup parent) {
        MultiView mv = (MultiView) inflater.inflate(R.layout.item_multiview, parent, false);
        mv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mv.setBackgroundColor(MyColors.themePrimary());
        mv.setMultiText(getString(R.string.no) + "\n" + getString(R.string.stats));
        mv.setTextRatio(0.5f);
        parent.addView(mv);
        return mv;
    }

    private MultiView addMultiView(LayoutInflater inflater, ViewGroup parent, String text, int color) {
        MultiView mv = (MultiView) inflater.inflate(R.layout.item_multiview, parent, false);
        mv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        mv.setBackgroundColor(color);
        mv.setMultiText(text);
        parent.addView(mv);
        return mv;
    }

    private void onStartGame() {
        ((GameFragment)frags().getFragFromId(FragManager.FragId.FRAG_GAME))
                .onStartGame();
        frags().goBack();
    }


    private FragManager frags() {
        return ((DartScapeActivity) requireContext()).frags();
    }


    /** grab the global game data */
    private GameData gdata() {
        return ((DartScapeActivity) requireContext()).gameData();
    }
}
