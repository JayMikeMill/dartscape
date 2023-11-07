package com.gworks.dartscape.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import com.gworks.dartscape.R;

import java.util.ArrayList;
import java.util.List;

//
// sound helper class
//
public class Sound {
    private static final int MAX_STREAMS = 10;

    private static List<Integer> mStreams;

    private static SoundPool mSoundPool;
    public static int DART1, DART2, DART3, SCRATCH, TRUMPET,
            SWOOSH, BUZZER, MISS, SHARPEN, SLASH, FATALITY,
            BBALL1, BBALL2, BBALL3;

    private static float mVolume;

    private Sound() {
    }

    @SuppressWarnings("deprecation")
    public static void init(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        else
            mSoundPool = new SoundPool.Builder().setMaxStreams(MAX_STREAMS).build();

        DART1 = mSoundPool.load(context, R.raw.dart1, 1);
        DART2 = mSoundPool.load(context, R.raw.dart2, 1);
        DART3 = mSoundPool.load(context, R.raw.dart3, 1);
        SCRATCH = mSoundPool.load(context, R.raw.scratch, 1);
        TRUMPET = mSoundPool.load(context, R.raw.winner_trumpet, 1);
        SWOOSH = mSoundPool.load(context, R.raw.swoosh, 1);
        BUZZER = mSoundPool.load(context, R.raw.buzzer, 1);
        MISS = mSoundPool.load(context, R.raw.miss, 1);
        SHARPEN = mSoundPool.load(context, R.raw.sharpen, 1);
        SLASH = mSoundPool.load(context, R.raw.slash, 1);
        FATALITY = mSoundPool.load(context, R.raw.fatality, 1);
        BBALL1 = mSoundPool.load(context, R.raw.baseball_hit1, 1);
        BBALL2 = mSoundPool.load(context, R.raw.baseball_hit2, 1);
        BBALL3 = mSoundPool.load(context, R.raw.baseball_hit3, 1);
        updateVolume();

        mStreams = new ArrayList<>();
    }

    public static void updateVolume() {
        mVolume = (float) UserPrefs.getVolume() / 100;
    }

    public static void stopAll() {
        for (int i = 0; i < mStreams.size(); i++)
            mSoundPool.stop(mStreams.get(i));

        mStreams = new ArrayList<>();
    }

    public static void play(int sound) {
        if (sound == BUZZER && !UserPrefs.getPlayBustSound()) return;
        if (sound == TRUMPET && !UserPrefs.getPlayWinnerSound()) return;

        play(sound, mVolume);
    }

    public static void play(int sound, float volume) {
        if (volume == 0) return;

        int stream = mSoundPool.play(sound, volume, volume, 0, 0, 1);

        mStreams.add(stream);
        if (mStreams.size() > MAX_STREAMS) mStreams.remove(0);
    }
}
