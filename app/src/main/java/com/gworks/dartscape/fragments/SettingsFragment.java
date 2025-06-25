package com.gworks.dartscape.fragments;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.dialogs.ConfirmDialog;
import com.gworks.dartscape.logic.GameLogic;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.ui.MultiView;
import com.gworks.dartscape.ui.SuperSpinner;
import com.gworks.dartscape.ui.ThemeSpinner;
import com.gworks.dartscape.util.Helper;
import com.gworks.dartscape.util.UserPrefs;
import com.gworks.dartscape.util.Sound;

import java.util.ArrayList;
import java.util.Locale;

public class SettingsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private static final float SCROLL_SETTING_TO_WINDOW_HEIGHT = 1.5f;
    private ScrollView mSvSettings;
    private LinearLayout mLayContent;

    private MultiView mMvVolumeIcon;
    private MultiView mMvVolume;
    private SeekBar mSeekVolume;

    private MultiView mBtnWinnerSound;
    private MultiView mBtnBustSound;

    private SuperSpinner mSpinTurnOrder;

    private MultiView mMvBotSpeedIcon;
    private MultiView mMvBotSpeedLabel;
    private SeekBar mSeekBotSpeed;

    private MultiView mBtnShowWins;
    private MultiView mBtnShowAvgs;
    private MultiView mBtnShowCurPlayer;

    private MultiView mBtnShowShadows;

    private ThemeSpinner mSpinTheme;

    private MultiView mBtnRecordStats;
    private MultiView mBtnRestoreGameData;
    private MultiView mBtnBackupGameData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.frag_settings, container, false);

        super.onCreateView(inflater, container, savedInstanceState);

        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initControls();
        initData();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resizeContentList();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if(hidden) return;

        loadSettings();
        mSvSettings.scrollTo(0, 0);
     }



    @SuppressLint({"DiscouragedApi", "ClickableViewAccessibility"})
    private void initControls() {
        mSvSettings =  requireView().findViewById(R.id.svSettings);
        mLayContent = requireView().findViewById(R.id.lSettingsContent);

        requireView().findViewById(R.id.btnSettingsDartscapePro)
                .setOnClickListener(v -> Helper.activation().showBuyProDialog());

        mMvVolumeIcon = requireView().findViewById(R.id.mvSettingsVolumeIcon);
        mMvVolume = requireView().findViewById(R.id.mvSettingsVolume);
        mSeekVolume = requireView().findViewById(R.id.seekSettingsVolume);
        mSeekVolume.setOnSeekBarChangeListener(this);

        mBtnWinnerSound = requireView().findViewById(R.id.btnSettingsPlayWinnerSound);
        mBtnWinnerSound.setOnClickListener(v -> {if(mBtnWinnerSound.isToggled()) Sound.play(Sound.TRUMPET, mSeekVolume.getProgress() / 100f);});
        mBtnBustSound = requireView().findViewById(R.id.btnSettingsPlayBustSound);
        mBtnBustSound.setOnClickListener(v -> {if(mBtnBustSound.isToggled()) Sound.play(Sound.BUZZER, mSeekVolume.getProgress() / 100f); });

        // init turn order spinner
        mSpinTurnOrder = requireView().findViewById(R.id.spinSettingsTurnOrder);
        mSpinTurnOrder.init(getTurnOrderItems(), 0.5f);
        mSpinTurnOrder.setSelection(0);

        mMvBotSpeedIcon = requireView().findViewById(R.id.mvSettingsBotIcon);
        mMvBotSpeedLabel = requireView().findViewById(R.id.mvSettingsBotSpeedLabel);
        mSeekBotSpeed = requireView().findViewById(R.id.seekSettingsBotSpeed);
        mSeekBotSpeed.setOnSeekBarChangeListener(this);

        mBtnShowWins      = requireView().findViewById(R.id.btnSettingsShowWins);
        mBtnShowAvgs      = requireView().findViewById(R.id.btnSettingsShowAvgs);
        mBtnShowCurPlayer = requireView().findViewById(R.id.btnSettingsShowCurPlayer);

        mBtnShowShadows   = requireView().findViewById(R.id.btnSettingsShadows);

        mSpinTheme = requireView().findViewById(R.id.spinSettingsTheme);
        mSpinTheme.init(0.5f);
        mSpinTheme.setSelection(0, false, true);

        mBtnRecordStats = requireView().findViewById(R.id.btnSettingsRecordStats);

        mBtnBackupGameData = requireView().findViewById(R.id.btnSettingsBackupData);
        mBtnBackupGameData.setOnClickListener(v -> onBackupGameData());

        mBtnRestoreGameData = requireView().findViewById(R.id.btnSettingsRestoreData);
        mBtnRestoreGameData.setOnClickListener(v -> onRestoreGameData());

        requireView().findViewById(R.id.btnSettingsAbout).setOnClickListener(v -> onShowAbout());
        requireView().findViewById(R.id.btnSettingsSave).setOnClickListener(v -> onSave());

        requireView().post(this::resizeContentList);

        // themes are disabled for pre 21 api
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            requireView().findViewById(R.id.labSettingsTheme).setVisibility(View.GONE);
            requireView().findViewById(R.id.lSettingsTheme).setVisibility(View.GONE);
        }

        setProUiActive(UserPrefs.getOwnsPro());
    }

    public void setProUiActive(boolean active) {
        Helper.setAdsActive(!active);

        requireView().findViewById(R.id.lSettingsDartscapePro)
                .setVisibility(active ? View.GONE : View.VISIBLE);

        mMvBotSpeedLabel.setEnabled(active);
        mMvBotSpeedLabel.setClickable(false);
        mSeekBotSpeed.setEnabled(active);
        mBtnShowWins.setEnabled(active);
        mBtnShowAvgs.setEnabled(active);
        mBtnShowCurPlayer.setEnabled(active);
        mBtnBackupGameData.setEnabled(active);
        mBtnRestoreGameData.setEnabled(active);

        ((MultiView) requireView().findViewById(R.id.labSettingsBotSpeed))
                .setMultiText(getString(R.string.bot_throw_speed) + (active ? "" : " (" + getString(R.string.pro_only) + ")"));
        ((MultiView) requireView().findViewById(R.id.labSettingsInGameOpts))
                .setMultiText(getString(R.string.in_game_options) + (active ? "" : " (" + getString(R.string.pro_only) + ")"));
        ((MultiView) requireView().findViewById(R.id.labSettingsTheme))
                .setMultiText(getString(R.string.dartscape_theme) + (active ? "" : " (" + getString(R.string.pro_only) + ")"));
        ((MultiView) requireView().findViewById(R.id.labSettingsData))
                .setMultiText(getString(R.string.player_data) + (active ? "" : " (" + getString(R.string.pro_only) + ")"));

        mSpinTheme.setOnItemSelectedListener(active ? null : new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) return;

                Helper.dialogs().showConfirm(getString(R.string.pro_only), R.drawable.ic_dartboard,
                            getString(R.string.dialog_themes_pro_only),
                            getString(R.string.buy_now), getString(R.string.maybe_later), false, dialog -> {
                                if(((ConfirmDialog) dialog).didConfirm())
                                    Helper.activation().showBuyProDialog(); });

                mSpinTheme.setSelection(0, false, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // set default free values
        if(!active) {
            mSeekBotSpeed.setProgress(1000);
            UserPrefs.setBotSpeed(MAX_BOT_SPEED - mSeekBotSpeed.getProgress());
            mBtnShowAvgs.setToggled(false);
            UserPrefs.setShowAvgs(mBtnShowAvgs.isToggled());
            mSpinTheme.setSelection(0, false, true);
            UserPrefs.setThemeIndex(0);
        }
    }

    private void resizeContentList() {
        mLayContent.setLayoutParams(new FrameLayout.LayoutParams
                (MATCH_PARENT, (int)(Helper.getWindowHeight()* SCROLL_SETTING_TO_WINDOW_HEIGHT)));
    }

    private GameData gdata() {
        return ((DartScapeActivity)requireContext()).gameData();
    }

    private void initData() {
    }

    private void onBackupGameData() {
        ((DartScapeActivity) requireContext()).backupPlayerData();
    }

    private void onRestoreGameData() {
        // show on quit confirm dialog
        Helper.dialogs().showConfirm(getString(R.string.confirm),
                getString(R.string.dialog_restore_data),
                getString(R.string.yes), false, dialog -> {
                    if(((ConfirmDialog) dialog).didConfirm())
                        ((DartScapeActivity) requireContext()).restorePlayerData(); });
    }


    public static final int MAX_BOT_SPEED = 3000;
    @SuppressLint("SetTextI18n")
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.equals(mSeekVolume)) {
            mMvVolume.setText(progress == 0 ? getString(R.string.volume) + ": " + getString(R.string.off)
                    : String.format(Locale.US, getString(R.string.volume) + ": %d%%", progress));
            mMvVolumeIcon.setIcon(0, progress > 80 ? R.drawable.ic_volume_high :
                    progress > 30 ? R.drawable.ic_volume_medium :
                            progress > 0 ? R.drawable.ic_volume_low : R.drawable.ic_volume_off );
        } else if (seekBar.equals(mSeekBotSpeed)) {
            mMvBotSpeedLabel.setText(String.format(Locale.US, "%1.2f " + getString(R.string.seconds), (MAX_BOT_SPEED - progress) / 1000f));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.equals(mSeekVolume)) {
            Sound.play(Sound.DART3, (float) mSeekVolume.getProgress() / 100);
        } else if (seekBar.equals(mSeekBotSpeed)) {
            playBotSpeedAniSound();
        }
    }

    Handler mBotSpeedSound = new Handler(Looper.myLooper());
    private void playBotSpeedAniSound() {
        mBotSpeedSound.removeCallbacksAndMessages(null);
        mBotSpeedSound.postDelayed(() -> {
            Sound.play(Sound.DART1, (float) mSeekVolume.getProgress() / 100);
            mMvBotSpeedIcon.playClickAnimation();
        }, 0);

        mBotSpeedSound.postDelayed(() -> {
            Sound.play(Sound.DART2, (float) mSeekVolume.getProgress() / 100);
            mMvBotSpeedIcon.playClickAnimation();
        }, MAX_BOT_SPEED - mSeekBotSpeed.getProgress());

        mBotSpeedSound.postDelayed(() -> {
            Sound.play(Sound.DART3, (float) mSeekVolume.getProgress() / 100);
            mMvBotSpeedIcon.playClickAnimation();
        }, (MAX_BOT_SPEED - mSeekBotSpeed.getProgress())*2L);
    }

    private void loadSettings() {
        mSeekVolume.setProgress(UserPrefs.getVolume());
        onProgressChanged(mSeekVolume, mSeekVolume.getProgress(), false);
        mBtnWinnerSound.setToggled(UserPrefs.getPlayWinnerSound());
        mBtnBustSound.setToggled(UserPrefs.getPlayBustSound());

        mSpinTurnOrder.setSelection(UserPrefs.getNewGameOrder());

        mSeekBotSpeed.setProgress(MAX_BOT_SPEED - UserPrefs.getBotSpeed());
        onProgressChanged(mSeekBotSpeed, mSeekBotSpeed.getProgress(), false);

        mBtnShowWins.setToggled(UserPrefs.getShowWins());
        mBtnShowAvgs.setToggled(UserPrefs.getShowAvgs());
        mBtnShowCurPlayer.setToggled(UserPrefs.getShowGameInfo());

        mBtnShowShadows.setToggled(UserPrefs.getShowShadows());
        mBtnRecordStats.setToggled(UserPrefs.getRecordStats());

        mSpinTheme.setSelection(UserPrefs.getThemeIndex(), false, true);
    }

    private void onShowAbout() {
        Helper.dialogs().showAbout();
    }

    private void onSave() {
        UserPrefs.setVolume(mSeekVolume.getProgress());
        Sound.updateVolume();

        UserPrefs.setPlayWinnerSound(mBtnWinnerSound.isToggled());
        UserPrefs.setPlayBustSound(mBtnBustSound.isToggled());

        if(!mSpinTurnOrder.isEmpty())
            gdata().setNewGameOrder(mSpinTurnOrder.getSelectedItemPosition());

        UserPrefs.setBotSpeed(MAX_BOT_SPEED - mSeekBotSpeed.getProgress());
        glogic().bot().updateBotSpeed();

        UserPrefs.setNewGameOrder(mSpinTurnOrder.getSelectedItemPosition());

        UserPrefs.setShowWins(mBtnShowWins.isToggled());
        UserPrefs.setShowAvgs(mBtnShowAvgs.isToggled());
        UserPrefs.setShowGameInfo(mBtnShowCurPlayer.isToggled());

        boolean shadowChange = mBtnShowShadows.isToggled() != UserPrefs.getShowShadows();
        UserPrefs.setShowShadows(mBtnShowShadows.isToggled());

        UserPrefs.setRecordStats(mBtnRecordStats.isToggled());

        boolean themeChange = mSpinTheme.getSelectedItemPosition() != UserPrefs.getThemeIndex();

        if(themeChange) {
            if(UserPrefs.getOwnsPro())
                UserPrefs.setThemeIndex(mSpinTheme.getSelectedItemPosition());
            else if(UserPrefs.getThemeIndex() != 0){
                UserPrefs.setThemeIndex(0);
            } else {
                themeChange = false;
            }
        }

        if(themeChange || shadowChange) {
            UserPrefs.setAppReset(true);
            ((DartScapeActivity) requireContext()).restartApp();
        } else frags().goBack();
    }

    private ArrayList<String> getTurnOrderItems() {
        ArrayList<String> items = new ArrayList<>();
        items.add("DON'T CHANGE");
        items.add("FLIP/SWITCH PLAYERS");
        items.add("WINNER GOES FIRST");
        items.add("WINNER - WORST");
        items.add("LOSER GOES FIRST");
        items.add("LOSER - BEST");

        return items;
    }

    private FragManager frags() {
        return ((DartScapeActivity) requireContext()).frags();
    }

    /** grab the global game logic */
    private GameLogic glogic() {
        return ((DartScapeActivity)requireContext()).gameLogic();
    }

    private void showBuyProDialog() {
        Helper.activation().showBuyProDialog();
    }
}
