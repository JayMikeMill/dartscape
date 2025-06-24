package com.gworks.dartscape.fragments;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.data.GameFlags;
import com.gworks.dartscape.dialogs.SplashDialog;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.ui.MultiView;
import com.gworks.dartscape.util.Helper;
import com.gworks.dartscape.util.UserPrefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FragManager  {

    private TitleBar mTitleBar;

    // different fragments
    public enum FragId {
        FRAG_SETTINGS,
        FRAG_HOME,
        FRAG_PLAYERS,
        FRAG_STATS,
        FRAG_STATS_PLAYER,
        FRAG_HELP,
        FRAG_GAME,
        FRAG_GAME_RESULTS;
    }

    ;

    // fragment transition animation styles
    public static final int TRANS_ANIM_NONE = 0;
    public static final int TRANS_ANIM_SLIDE_LEFT = 1;
    public static final int TRANS_ANIM_SLIDE_RIGHT = 2;
    public static final int TRANS_ANIM_FADE_IN = 3;
    public static final int TRANS_ANIM_FADE_OUT = 4;
    public static final int TRANS_ANIM_LONG_FADE_IN = 5;
    public static final int TRANS_ANIM_LONG_FADE_OUT = 6;

    public static final int TRANS_ANIM_DEFAULT_IN = TRANS_ANIM_SLIDE_LEFT;
    public static final int TRANS_ANIM_DEFAULT_OUT = TRANS_ANIM_SLIDE_RIGHT;
    // fragment transition animation id's
    public static final int[][] TRANS_ANIMATION_IDS = {
            {0, 0},
            {R.anim.enter_from_right, R.anim.exit_to_left},
            {R.anim.enter_from_left, R.anim.exit_to_right},
            {R.anim.fade_out, R.anim.fade_in},
            {R.anim.fade_in, R.anim.fade_out},
            {R.anim.long_fade_out, R.anim.long_fade_in},
            {R.anim.long_fade_in, R.anim.long_fade_out},
    };

    // fragments
    /**
     * Splash screen
     */
    SplashDialog mDiagSplash;
    boolean mSplashIsShown; // is splash screen showing?
    Handler mSplashHandler; // handler to time splash screen.

    private HomeFragment mFragHome;
    private SettingsFragment mFragSettings;
    private PlayersFragment mFragPlayers;
    private StatsFragment mFragStats;
    private StatsPlayerFragment mFragStatsPlayer;
    private HelpFragment mFragHelp;
    private GameFragment mFragGame;
    private GameResultsFragment mFragGameResults;
    /**
     * Previous shown fragments for "go back"
     */
    private ArrayList<Fragment> mFragList;

    private DartScapeActivity mActivity;

    private boolean mIsFinishedLoading;
    private boolean mShowSplash = true; //!BuildConfig.DEBUG;
    private int mSplashTime = 2000;

    boolean mIsTransitioning = false;
    public static final int TRANSITION_TIME = 200;

    public FragManager init(DartScapeActivity activity) {
        mActivity = activity;

        mTitleBar = new TitleBar(mActivity);

        // init fragments shown list
        mFragList = new ArrayList<>();

        // init all fragments
        mFragHome = new HomeFragment();
        mFragSettings = new SettingsFragment();
        mFragPlayers = new PlayersFragment();
        mFragStats = new StatsFragment();
        mFragStatsPlayer = new StatsPlayerFragment();
        mFragHelp = new HelpFragment();
        mFragGame = new GameFragment();
        mFragGameResults = new GameResultsFragment();


        // init splash screen variables
        if (UserPrefs.getAppReset()) {
            mShowSplash = false;
            UserPrefs.setAppReset(false);
        }

        mSplashIsShown = false;
        mIsFinishedLoading = false;
        showSplash();

        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(this::initFrags, mSplashTime - 800);

        return this;
    }

    public boolean isLoading() {
        return !mIsFinishedLoading;
    }

    private void initFrags() {
        // pre load all fragments for quick showing
        // add to app frag manager
        //stack(this);

        if(isFragmentManagerActive()) {
            mActivity.getSupportFragmentManager().executePendingTransactions();
        }

        stack(mFragHome);
        stack(mFragSettings);
        stack(mFragPlayers);
        stack(mFragStats);
        stack(mFragStatsPlayer);
        stack(mFragHelp);
        stack(mFragGame);
        stack(mFragGameResults);

        // mActivity.getSupportFragmentManager().executePendingTransactions();

        //measureFragments();

        show(mFragHome, TRANS_ANIM_LONG_FADE_OUT);

        // only on fist app run
        if (UserPrefs.isFirstRun()) {
            new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() ->
                    show(mFragHelp, TRANS_ANIM_LONG_FADE_OUT), TRANSITION_TIME);

            // show byy pro dialog on first run
            new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
                if (!UserPrefs.getOwnsPro()) Helper.activation().showBuyProDialog();
            }, 2000);

            new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() ->
                    Helper.dialogs().showRating(), 240000);
        }

        if (gdata().flags().get(GameFlags.GameFlag.GAME_DATA_RESTORED)) {
            mFragList.add(0, mFragGame);
            mFragList.add(0, mFragHome);
            titleBar().setBackButtonActive(true);
        }
        // show home screen in case show splash is off

        // close splash screen
        mIsFinishedLoading = true;

        Helper.initAds(mActivity);
    }

    /**
     * show the splash screen until app is finished loading
     */
    private void showSplash() {
        if (!mShowSplash) {
            mSplashTime = 0;
            return;
        }

        if (!mSplashIsShown) {
            // create splash screen, stack and show it
            mDiagSplash = new SplashDialog(mActivity);
            mDiagSplash.show();

            // don't run this if again
            mSplashIsShown = true;

            // show splash for
            mSplashHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));
            mSplashHandler.postDelayed(this::showSplash, mSplashTime);

            return;
        }

        if (mIsFinishedLoading) {
            // if load show home screen and stop splash handler
            mSplashHandler.removeCallbacksAndMessages(null);
            mSplashHandler.postDelayed(() -> mDiagSplash.dismiss(), 300);
        } else {
            // give another 10th second to load
            mSplashHandler.postDelayed(this::showSplash, 100);
        }
    }

    /**
     * @return The currently showing fragment
     */
    public Fragment showing() {
        if (mFragList.isEmpty()) return mFragHome;
        return mFragList.get(mFragList.size() - 1);
    }

    /**
     * @return The last fragment shown before current fragment.
     * Returns the home screen fragment if none.
     */
    public Fragment getLast() {
        if (mFragList.size() < 2) return mFragHome;
        return mFragList.get(mFragList.size() - 2);
    }

    /**
     * @return The last fragment shown before current fragment.
     * Returns the home screen fragment if none.
     */
    public FragId getLastId() {
        return getIdFromFrag(getLast());
    }

    /**
     * Shows the last fragment shown before current fragment.
     * Shows the home screen fragment if none.
     */
    public boolean showLast() {
        return showLast(TRANS_ANIM_DEFAULT_OUT);
    }

    public boolean showLast(int animation) {
        boolean result = show(getLast(), animation);

        if (mFragList.size() > 1) mFragList.remove(mFragList.size()-1);
        if (mFragList.size() > 1) mFragList.remove(mFragList.size()-1);

        mTitleBar.setBackButtonActive(mFragList.size() > 1);

        return result;
    }

    public boolean show(FragId fragId) {
         return show(getFragFromId(fragId));
    }

    public boolean show(FragId fragId, int animation) {
        return show(getFragFromId(fragId), animation);
    }
    /* shows a fragment with default animation */
    private boolean show(Fragment fragment) {
        return show(fragment, TRANS_ANIM_DEFAULT_IN);
    }

    /* shows a fragment with specified animation */
    private boolean show(Fragment fragment, int transAnim) {
        if(!isFragmentManagerActive()) return false;
        if(mIsTransitioning) return false;

        // replace the screen content add it to stack
        FragmentTransaction ft;
        ft =  mActivity.getSupportFragmentManager().beginTransaction();

        // set transition animation if any
        if(transAnim != TRANS_ANIM_NONE)
            ft.setCustomAnimations(TRANS_ANIMATION_IDS[transAnim][0],
                                   TRANS_ANIMATION_IDS[transAnim][1]);

        // hide current fragment and show new one
        ft.hide(showing()).show(fragment).runOnCommit(() -> {
            // add to fragment list for showLast
            mFragList.add(fragment);
            mTitleBar.setBackButtonActive(mFragList.size() > 1);

            mIsTransitioning = true;
            new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
                mTitleBar.setTitleText(getFragTitle(getIdFromFrag(fragment)));
                mTitleBar.setWindowIcon(getFragWindowIcon(getIdFromFrag(fragment)));
                mIsTransitioning = false;
            }, TRANSITION_TIME);
        }).commitAllowingStateLoss();

        return true;
    }

    /** adds the fragment to the stack. should remain here the
     *  the entire life of the app for quick loading. */
    private void stack(Fragment fragment) {
        if(isFragmentManagerActive()) {
            // replace the screen content add it to stack
            mActivity.getSupportFragmentManager().beginTransaction()
                    .add(R.id.lFragContainer, fragment)
                    .show(fragment).hide(fragment).commitAllowingStateLoss();
        }
    }


    public Fragment getFragFromId(FragId id) {
        switch (id) {
            case FRAG_SETTINGS:      return mFragSettings;
            case FRAG_HOME:          return mFragHome;
            case FRAG_PLAYERS:       return mFragPlayers;
            case FRAG_STATS:         return mFragStats;
            case FRAG_STATS_PLAYER:  return mFragStatsPlayer;
            case FRAG_HELP:          return mFragHelp;
            case FRAG_GAME:          return mFragGame;
            case FRAG_GAME_RESULTS:   return mFragGameResults;
        }

        return null;
    }

    public FragId getIdFromFrag(Fragment frag) {
        if(frag.equals(mFragSettings))  return FragId.FRAG_SETTINGS;
        if(frag.equals(mFragHome))  return FragId.FRAG_HOME;
        if(frag.equals(mFragPlayers))  return FragId.FRAG_PLAYERS;
        if(frag.equals(mFragStats))  return FragId.FRAG_STATS;
        if(frag.equals(mFragStatsPlayer))  return FragId.FRAG_STATS_PLAYER;
        if(frag.equals(mFragHelp))  return FragId.FRAG_HELP;
        if(frag.equals(mFragGame))  return FragId.FRAG_GAME;
        if(frag.equals(mFragGameResults))  return FragId.FRAG_GAME_RESULTS;

        return null;
    }

    public String getFragTitle(FragId id) {
        switch (id) {
            case FRAG_HOME:          return "DARTSCAPE";
            case FRAG_SETTINGS:      return "SETTINGS";
            case FRAG_PLAYERS:       return "PLAYERS";
            case FRAG_STATS:         return "STATS";
            case FRAG_STATS_PLAYER:  return "[CUSTOM]";
            case FRAG_HELP:          return "DARTSCAPE HELP";
            case FRAG_GAME:          return "[GAME TITLE]";
            case FRAG_GAME_RESULTS:  return "GAME RESULTS";
        }

        return "";
    }

    public int getFragWindowIcon(FragId id) {
        switch (id) {
            case FRAG_HOME:
            case FRAG_GAME:          return R.drawable.dartscape_icon;
            case FRAG_SETTINGS:      return R.drawable.ic_settings;
            case FRAG_PLAYERS:       return R.drawable.ic_users_trio;
            case FRAG_HELP:          return R.drawable.ic_help;
            case FRAG_GAME_RESULTS:
            case FRAG_STATS:
            case FRAG_STATS_PLAYER:  return R.drawable.ic_stats;
        }

        return 0;
    }


    // pre measure fragments for quick access
    public void measureFragments() {
        List<FragId> ids = Arrays.asList(FragId.values());
        for (int i = 0; i < ids.size(); i++) {
            Helper.relayoutChildren(getFragFromId(ids.get(i)).requireView());
            Helper.setAllChildrenShadows((ViewGroup) getFragFromId(ids.get(i)).requireView());
        }
    }

    public boolean goBack() {
        if(isLoading() || mIsTransitioning) return false;

        if(showing().equals(getFragFromId(FragManager.FragId.FRAG_GAME))) {
            mFragHome.syncGameDataOnShow();
            return show(FragId.FRAG_HOME, TRANS_ANIM_SLIDE_LEFT);
        } else if(showing().equals(getFragFromId(FragId.FRAG_GAME_RESULTS))) {
            return show(FragId.FRAG_GAME, TRANS_ANIM_FADE_OUT);
        } if(showing().equals(getFragFromId(FragId.FRAG_HOME))) {
            if(!gdata().isGameGoing()) return false;
        }

        return showLast();
    }

    public TitleBar titleBar() { return  mTitleBar; }

    public static class TitleBar {
        // title bar
        private final TextView mTvTitle;
        private final ImageView mIvWindowIcon;
        private final MultiView btnBack;

        private final DartScapeActivity mActivity;

        TitleBar(DartScapeActivity activity) {
            mActivity = activity;

            mTvTitle = mActivity.findViewById(R.id.tvMainTitle);
            mIvWindowIcon = mActivity.findViewById(R.id.ivMainWindowIcon);

            btnBack = mActivity.findViewById(R.id.btnMainWindowBack);
            btnBack.setOnClickListener(v -> mActivity.frags().goBack());
        }


        public void setTitleText(String text) {
            if(text.equals("[GAME TITLE]"))
                mTvTitle.setText(mActivity.gameData().getGameTypeString());
            else if(!text.equals("[CUSTOM]") && !text.equals(mTvTitle.getText().toString()))
                mTvTitle.setText(text);
        }

        public void setWindowIcon(int iconId) {
            mIvWindowIcon.setImageDrawable(AppCompatResources.getDrawable(mActivity, iconId));
        }

        public void setBackButtonActive(boolean active) {
            new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
                btnBack.setVisibility(active ? View.VISIBLE : View.INVISIBLE);
            }, TRANSITION_TIME);
        }
    }

    /** grab the global game data */
    private GameData gdata() {
        return mActivity.gameData();
    }

    private boolean isFragmentManagerActive() {
        return !mActivity.getSupportFragmentManager().isDestroyed();
    }

}
