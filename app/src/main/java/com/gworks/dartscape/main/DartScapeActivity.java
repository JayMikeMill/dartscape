package com.gworks.dartscape.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameData;

import java.io.IOException;

import com.gworks.dartscape.database.PlayerDatabase;
import com.gworks.dartscape.fragments.FragManager;
import com.gworks.dartscape.fragments.HomeFragment;
import com.gworks.dartscape.logic.GameLogic;
import com.gworks.dartscape.util.Font;
import com.gworks.dartscape.util.Helper;
import com.gworks.dartscape.util.MyColors;
import com.gworks.dartscape.util.Themes;
import com.gworks.dartscape.util.UserPrefs;
import com.gworks.dartscape.util.Sound;


public class DartScapeActivity extends AppCompatActivity {

    /** the main game data used by all fragments and classes */
    private FragManager mFragManager;
    private static GameData mGameData;
    private static GameLogic mGameLogic;
    private  PlayerDatabase mPlayerDB;


    public FragManager frags() {
        return mFragManager;
    }
    public GameData gameData() {
        return mGameData;
    }
    public GameLogic gameLogic() {
        return mGameLogic;
    }
    public PlayerDatabase playerDB() {return mPlayerDB; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setLocale("de");
        Globals.init(this);
        UserPrefs.init(this); // initialize app preferences

        // set the apps theme
        setTheme(Themes.getCurrentTheme());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_manager);

        // set up utility classes
        MyColors.init(this);
        Helper.init(this);
        Sound.init(this); // initialize sound
        Font.init(this); // initialize fonts

        // set full screen
        Helper.setFullScreen(true);

        // init game data
        mGameData    = UserPrefs.restoreGameData();
        mGameLogic   = new GameLogic(mGameData);
        mPlayerDB    = new PlayerDatabase(this);
        mFragManager = new FragManager().init(this);
        Helper.initActivation(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        backupGameData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Helper.activation().checkForProVersion();
    }

    private void setLocale(String localeStr) {
        LocaleListCompat appLocale =
                LocaleListCompat.forLanguageTags(localeStr);
        // Call this on the main thread as it may require Activity.restart()
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    public void restartApp() {
       // backupGameData();
        Intent intent = new Intent(DartScapeActivity.this, DartScapeActivity.class);
        startActivity(intent); // start same activity
        backupGameData();
        finish();
        overridePendingTransition(R.anim.long_fade_in, R.anim.long_fade_out); // this is important for seamless transition
    }

    @Override
    protected void onStop() {
        backupGameData();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        frags().goBack();
    }

    private void backupGameData() {
        if (!frags().isFinishedLoading()) return;

        if (gameData().isGameGoing())
            UserPrefs.backupGameData(gameData());
        else UserPrefs.clearGameData();

        ((HomeFragment)frags().getFragFromId(FragManager.FragId.FRAG_HOME)).backupGameSettings();
    }


    private static final int SAVE_DOCUMENT_REQUEST_CODE = 0x445;
    private static final int OPEN_DOCUMENT_REQUEST_CODE = 0x446;
    private static final String DARTSCAPE_FILE_EXT = ".drts";
    @SuppressWarnings("deprecation")
    public void backupPlayerData() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); //not needed, but maybe useful
        intent.putExtra(Intent.EXTRA_TITLE, "dartscape.drts"); //not needed, but maybe useful
        startActivityForResult(intent, SAVE_DOCUMENT_REQUEST_CODE);
    }

    @SuppressWarnings("deprecation")
    public void restorePlayerData() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); //not needed, but maybe useful
        intent.putExtra(Intent.EXTRA_TITLE, "dartscape"); //not needed, but maybe useful
        startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE);
    }

    //after the user has selected a location you get an uri where you can write your data to:
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SAVE_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //just as an example, I am writing a String to the Uri I received from the user:

            try {
                playerDB().saveFile(getContentResolver().openOutputStream(data.getData()));
                Helper.dialogs().showConfirm(getString(R.string.success) + "!", R.drawable.ic_check, getString(R.string.game_data_was_saved) + "!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //just as an example, I am writing a String to the Uri I received from the user:
            try {
                boolean restored = playerDB().loadFile(getContentResolver().openInputStream(data.getData()));

                if (restored) {
                    Helper.dialogs().showConfirm(getString(R.string.success) + "!", R.drawable.ic_check, getString(R.string.game_data_was_restored) + "!");
                    ((HomeFragment) frags().getFragFromId(FragManager.FragId.FRAG_HOME)).syncPlayersOnShow();
                }else {
                    Helper.dialogs().showConfirm(getString(R.string.error), R.drawable.ic_cancel, getString(R.string.dialog_file_error));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

