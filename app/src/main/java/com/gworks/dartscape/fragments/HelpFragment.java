package com.gworks.dartscape.fragments;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.data.GameFlags;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.ui.BaseballView;
import com.gworks.dartscape.ui.MultiView;
import com.gworks.dartscape.ui.ScoreButton;
import com.gworks.dartscape.ui.SuperSpinner;

import java.util.ArrayList;

public class HelpFragment extends Fragment {
    private static final float SCROLL_HELP_TO_WINDOW_HEIGHT = 3.5f;

    private SuperSpinner mSpinChapters;
    private ScrollView   mSvHelp;
    private LinearLayout mLContent;

    private ScoreButton mSb01;
    private ScoreButton mSbCricket;
    private ScoreButton mSbShanghai;
    private BaseballView mBaseball;

    private MultiView mBtnGotIt;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.frag_help, container, false);

        super.onCreateView(inflater, container, savedInstanceState);

        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initControls();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if(hidden) return;

        initScoreButtons();
        mSvHelp.post(this::resizeContentList);
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resizeContentList();
    }

    @SuppressLint({"DiscouragedApi", "ClickableViewAccessibility"})
    private void initControls() {
        mSpinChapters = requireView().findViewById(R.id.spinHelpChapters);
        mSpinChapters.init(getChapters() , 0.5f);
        mSpinChapters.setOnItemSelectedListener(mSpinChapterListener);

        mSvHelp       = requireView().findViewById(R.id.svHelp);
        mLContent     = requireView().findViewById(R.id.lHelpContent);

        // init score buttons
        mSb01 = requireView().findViewById(R.id.sbHelp01);
        mSb01.setGameMode(GameFlags.GameFlag.X01);
        mSb01.setNumber(20);
        mSb01.setScoreButtonListener(this::onScoreButton);

        mSbCricket = requireView().findViewById(R.id.sbHelpCricket);
        mSbCricket.setGameMode(GameFlags.GameFlag.CRICKET);
        mSbCricket.setNumber(15);
        mSbCricket.setScoreButtonListener(this::onScoreButton);

        mSbShanghai = requireView().findViewById(R.id.sbHelpShanghai);
        mSbShanghai.setGameMode(GameFlags.GameFlag.SHANGHAI);
        mSbShanghai.setNumber(1);
        mSbShanghai.setScoreButtonListener(this::onScoreButton);

        mBaseball = requireView().findViewById(R.id.sbHelpBaseball);
        mBaseball.setScoreButtonListener(this::onScoreButton);

        mBtnGotIt = requireView().findViewById(R.id.btnHelpGotIt);
        mBtnGotIt.setOnClickListener(v -> frags().goBack());
    }

    private void resizeContentList() {

        float height = (mSvHelp.getHeight())*(mLContent.getWeightSum());

        mLContent.setLayoutParams(new LinearLayout.LayoutParams
                (MATCH_PARENT, (int) height));

        mSpinChapters.setSelection(0);
        mSvHelp.scrollTo(0, 0);
    }


    private ArrayList<String> getChapters() {
        ArrayList<String> chapters = new ArrayList<>();

        chapters.add(getString(R.string.how_to_score));
        chapters.add(getString(R.string.tips));
        chapters.add(getString(R.string.dartscape_game_menu));
        chapters.add(getString(R.string.legs_and_sets));
        chapters.add(getString(R.string._01_games));
        chapters.add(getString(R.string.cricket));
        chapters.add(getString(R.string.shanghai));
        chapters.add(getString(R.string.baseball));
        chapters.add(getString(R.string.killer));

        return chapters;
    }

    private void initScoreButtons() {
        mSb01.setMarks(0);
        mSbCricket.setMarks(0);

        mSbShanghai.setShangNumber(1, false);
        mSbShanghai.setMarks(0);
        mSbShanghai.cancelPendingInputEvents();

        mBaseball.setGameInfo(1, 0, 0, 0, 0);
    }


    private void onScoreButton(ScoreButton hitButton, int multi) {
        int oldMarks = hitButton.getMarks();
        int newMarks = oldMarks + multi;
        int number = hitButton.getNumber();
        int score = 0;


        if(hitButton.is01()) {
            score = number * multi;
        } else if(hitButton.isCricket()) {
            hitButton.setMarks(newMarks);
            if(oldMarks < 3 && newMarks > 3) score = (newMarks - 3) * number;
            if(oldMarks >= 3) score = number * multi;
        } else if(hitButton.isShanghai()) {
            hitButton.setMarks(oldMarks + 1);
            if ((oldMarks + 1) % 3 == 0)
                hitButton.setShangNumber(number + 1 > 20 ? 1 : number + 1, true);
            score = number * multi;
        } else if(hitButton.equals(mBaseball)) {
           mBaseball.runBases(multi);
        }

        hitButton.popSoundAnimate(score, multi, false);
    }

    AdapterView.OnItemSelectedListener mSpinChapterListener = new AdapterView.OnItemSelectedListener() {
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LinearLayout chap = (LinearLayout) mLContent.getChildAt(position);
            mSvHelp.scrollTo(0, chap.getTop());
        }

        @Override public void onNothingSelected(AdapterView<?> parent) {}
    };


    private GameData gdata() {
        return ((DartScapeActivity)requireContext()).gameData();
    }
    private FragManager frags() {
        return ((DartScapeActivity)requireContext()).frags();
    }
}
