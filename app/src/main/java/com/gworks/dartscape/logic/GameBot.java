package com.gworks.dartscape.logic;

import static com.gworks.dartscape.data.Stats.StatId.*;

import android.os.Handler;
import android.os.Looper;

import com.gworks.dartscape.data.GameData;
import com.gworks.dartscape.data.GameThrow;
import com.gworks.dartscape.data.GameThrowList;
import com.gworks.dartscape.data.PlayerStats;
import com.gworks.dartscape.util.UserPrefs;

import java.util.Random;

public class  GameBot {



    private final GameData mGameData;
    private final Runnable mHitFunction;

    // to get random numbers
    private Random mRandom;
    private Handler mBotHandler;
    private Runnable mRunning;
    private float mDifficulty;

    private int mThrowWaitMs;
    private long mThrowWaitStartTime;
    private long mThrowWaitProgress;

    private int mLastHitMulti;
    private int mLastHitNumber;
    private boolean mIsPaused;
    private boolean mIsStopped;


    public GameBot(GameData gameData, Runnable hitFunction) {
        mGameData = gameData;
        mHitFunction = hitFunction;
        init();
    }

    public void init() {
        mRandom = new Random();
        mBotHandler = new Handler(Looper.myLooper());
        mDifficulty = 0;
        updateBotSpeed();
        mLastHitMulti = 0;
        mLastHitNumber = 0;
        mIsPaused = false;
        mIsStopped = false;

        mRunning = this::throwAndLoop;
    }

    public void updateBotSpeed() {
        mThrowWaitMs = UserPrefs.getBotSpeed();
    }

    public void stop() {
        mIsPaused = false;
        mIsStopped = true;
        mThrowWaitProgress = 0;
        mBotHandler.removeCallbacksAndMessages(null);
    }

    public void pause() {
        if (!mIsPaused)
            mBotHandler.removeCallbacksAndMessages(null);

        mIsPaused = true;
        mThrowWaitProgress = System.currentTimeMillis() - mThrowWaitStartTime;
    }

    public void resume() {
        start();
    }

    public void start() {
        mIsPaused = false;
        mIsStopped = false;

        mBotHandler.removeCallbacksAndMessages(null);
        mThrowWaitStartTime = System.currentTimeMillis();
        mBotHandler.postDelayed(mRunning, getThrowWaitMs());
    }

    public int getThrowWaitProgressMs() {
        if (isPaused() || mIsStopped) return 0;
        return (int) (System.currentTimeMillis() - mThrowWaitStartTime);
    }

    public void throwAndLoop() {
        if (!isRunning()) return; // make sure bot is still running
        if (gdata().isWinner()) return; // if game is won, no more shooting

        setDifficulty(gdata().throwingPlayer().botDiff());

        GameThrow thrw = throwDart();

        if (thrw == null) return;

        // set hit info for retrieval by client
        mLastHitNumber = thrw.getNumber();
        mLastHitMulti  = thrw.getMulti();

        // keep running until round is over, mThrowCount == 3
        mThrowWaitStartTime = System.currentTimeMillis();
        mBotHandler.postDelayed(mRunning, getThrowWaitMs());

        // run hit function (doScore)
        mHitFunction.run();
    }

    public GameThrow throwDart() {


        // max accuracy
        float accuracy = getAccuracy();
        float precision = getPrecision(); // == 1 ? 1 : accuracy*(multiChance*multiMultiplier);
        float luck = getLuck();

        accuracy += rnd().nextFloat() * luck;
        precision += rnd().nextFloat() * luck;

        // choose the number we are aiming at. Different games,
        // different numbers.
        int[] aimingAt = getAim();
        int aimingNumber = aimingAt[0];
        if (aimingNumber == -1) return null; // no points open, return

        // did bot hit anything active?
        // handicap for aiming at bullseye
        float handicap = aimingNumber == 25 ? precision : 1.0f;
        boolean hit = rnd().nextFloat() <= accuracy * handicap;
        int hitNumber = 0;

        // 01 almost never miss
        if(!hit && gdata().is01()) {
            hit = rnd().nextFloat() < .5f;
            hitNumber = getErrorHitNumber(aimingNumber);
            // no hit, return
        } else {
            // hit another active number close by by luck?
            // no other numbers in shanghai
            if (rnd().nextFloat() < getError()) {
                hitNumber = getErrorHitNumber(aimingNumber);
            } else {
                hitNumber = aimingNumber;
            }
        }

        if (!hit) return new GameThrow(0, aimingNumber, 0, 0);

        // number is not active game number
        if (gdata().isCricket() && GameData.getIndexOfNumber(hitNumber) == -1 ||
                !gdata().isNumberOpen(GameData.getIndexOfNumber(hitNumber))) {
            return new GameThrow(0, aimingNumber, 0, 0);
        } else if (gdata().isShanghai() && hitNumber != gdata().getShanghaiNumber()) {
            return new GameThrow(0, aimingNumber, 0, 0);
        } else if (gdata().isBaseball() && (hitNumber != 25 && hitNumber != gdata().getBaseballInning())) {
            return new GameThrow(0, aimingNumber, 0, 0);
        }

        //
        // get the multiplier that is being aimed at and
        // the multiplier that is hit
        //
        int aimingMulti = aimingAt[1];
        handicap = aimingMulti == 3 ? precision : 1.0f;
        int multi = rnd().nextFloat() <= precision * handicap ? aimingMulti : 0;

        if (multi == 0) { // didnt hit aiming number
            boolean luckHit = rnd().nextFloat() < luck;
            multi = aimingMulti == 1 ? (rnd().nextBoolean() && luckHit ? 3 : 2) :
                    aimingMulti == 2 ? (rnd().nextBoolean() ? 1 : rnd().nextBoolean() && luckHit ? 3 : 1) :
                    aimingMulti == 3 ? (rnd().nextBoolean() ? 1 : rnd().nextBoolean() && luckHit ? 2 : 1) : 1;
        }

        if (hitNumber == 25 && multi == 3) multi = 2;

        return new GameThrow(0, hitNumber, multi, 0);
    }

    private int[] getAim() {
        int aimingNumber = 0;
        int aimingMulti = 0;

        if(gdata().is01()) {
            // aim for 20's unless there is an out
            if (getPlayerScore() <= 180) {
                return X01Logic.x01GetBestOut(gdata(), getPlayerScore());
            } else {
                aimingMulti = 3;
                if(getAccuracy() < 0.5f) { // lower difficulties might aim at 17-18-19
                    aimingNumber = 20 - Math.round(rnd().nextFloat() * 3);
                } else {
                    aimingNumber = 20;
                }
            }

            // aim for 20's unless there is an out
            // if player is at beginning
            if(getPlayerScore() == gdata().getStartScore())
                aimingMulti = gdata().x01getFirstIn();
        } else if(gdata().isCricket()) {
            // if not high score aim for highest open point
            // else, current players next open point
            aimingNumber = gdata().getFirstOpenNumber();
            if(aimingNumber == -1) aimingNumber = 20;
            if (gdata().isPlayerBestScore(gdata().getCurrentTurn()))
                aimingNumber = gdata().getPlayerFirstOpenNumber(gdata().getCurrentTurn());
            aimingMulti = 3;
        } else if(gdata().isShanghai()) {
            // nothing else to aim at
            aimingNumber = gdata().getShanghaiNumber();
            aimingMulti = getAimingMulti();
        }else if(gdata().isBaseball()) {
            // nothing else to aim at
            aimingNumber = gdata().areBasesLoaded() || getPrecision() > .7f ? 25 :
                    gdata().getBaseballInning();
            aimingMulti = gdata().areBasesLoaded() || getPrecision() > .7f ? 1 : 3;
        }

        return new int[] { aimingNumber, aimingMulti };
    }

    private int getAimingMulti() {
        if(gdata().isShanghai()) {
            // retrieve the previous throw this round
            GameThrowList gThrows = gdata().gthrows();
            boolean[] hit = new boolean[3]; int curMulti = 0;
            boolean missedAny = false;

            int lastThrowsCount = gdata().getTurnThrows();
            for (int i = 0; i < lastThrowsCount; i++) {
                curMulti = gThrows.get(gThrows.size() - 1 - i).getMulti();
                if(curMulti == 0) missedAny = true;
                else if(curMulti == 1) hit[0] = true;
                else if(curMulti == 2) hit[1] = true;
                else if(curMulti == 3) hit[2] = true;
            }

            // if a previous throw was missed, aim for triple,
            // else, aim for next highest multi for a shanghai
            if(missedAny) return 3;
            else {
                for (int i = 2; i >= 0; i--) {
                    if (!hit[i]) return i + 1;
                }
            }
        }

        return 3;
    }

    private int getErrorHitNumber(int aimingNumber) {
        // baseball and shang have 1 active number, no room for error
        if(gdata().isShanghai() || gdata().isBaseball()) return aimingNumber;


        int[] numbers = GameLogic.getActiveNumbers(gdata().getGameFlag());
        int numberCount = numbers.length;

        int maxOffset = Math.round(getError() * 3f);

        int numberIndex = -1;
        for (int i = 0; i < numberCount; i++)
            if (aimingNumber == numbers[i]) numberIndex = i;

        int otherOffset = Math.round(rnd().nextFloat() * maxOffset);

        // chance for bullseye
        if (otherOffset > 1 && rnd().nextFloat() <= 0.05f) return 25;

        // random left right
        otherOffset = (rnd().nextBoolean() ? otherOffset : -otherOffset);

        int errorNumberIndex = numberIndex + otherOffset;
        if(errorNumberIndex > numberCount - 1) errorNumberIndex %= numberCount;
        if(errorNumberIndex < 0) errorNumberIndex += numberCount;

        return numbers[errorNumberIndex];
    }


    private int getPlayerScore() {
        return gdata().throwingPlayer().getScore();
    }

    public String getBotMultiAverage(float difficulty, int iterations) {
        PlayerStats stats = getBotStats(difficulty, iterations);

        return getBotDiffName(difficulty) + ": " +
                (stats.getStatString(MARK_AVG)) + ", " +
                stats.getStatString(HIT_TRIPLES) + ", " +
                stats.getStatString(HIT_DOUBLES) + ", " +
                stats.getStatString(HIT_SINGLES);
    }

    public PlayerStats getBotStats(float difficulty, int iterations) {
        iterations *= 3;
        setDifficulty(difficulty);
        GameThrowList throwList = new GameThrowList();

        for (int i = 0; i < iterations; i++) {
            GameThrow gthrow = throwDart();
            gthrow.setScore(gthrow.getNumber() * gthrow.getMulti());
            throwList.add(gthrow);
        }

        return new PlayerStats(throwList);
    }

    public float getBotAvg(float difficulty, boolean score) {
        setDifficulty(difficulty);
        float accuracy = getAccuracy() + (getLuck() / 2);
        float precision = getPrecision() + (getLuck() / 2);

        if(score) return ((Math.min(accuracy +.5505f, 1f) * 1.5f) +
    (float) Math.pow(precision, 2.1f) * 7.5f) * 18.5555f;
        return ((accuracy * 3f) + (precision * 3f) +
                (((float) Math.pow(accuracy, 12))  * 3f));
    }


    public static String getBotDiffName(float difficulty) {
        if(difficulty >= 1.0f) return "PERFECT";
        if(difficulty > 0.95f) return "MASTER";
        if(difficulty > 0.85f) return "EXPERT";
        if(difficulty > 0.7f) return "HARDER";
        if(difficulty > 0.5f) return "HARD";
        if(difficulty > 0.4f) return "MEDIUM";
        if(difficulty > 0.3f) return "NORMAL";
        if(difficulty > 0.2f) return "EASY";
        if(difficulty > 0.1f) return "NOVICE";
        if(difficulty > 0.0f) return "BEGINNER";
        return "BEGINNER";
    }

    public float getDifficulty() { return mDifficulty; }
    public float getAccuracy() { return mDifficulty ; }
    public float getError() { return 1f - getAccuracy(); }
    public float getPrecision() { return getAccuracy() * getAccuracy(); }
    public float getLuck() { return getError() * 0.1f; }

    public void setDifficulty(float difficulty) { mDifficulty = difficulty; }

    public int getThrowWaitMs() { return mThrowWaitMs; }
    public void setThrowWaitMs(int ms) { mThrowWaitMs = ms; }

    public int getLastHitMulti() { return mLastHitMulti; }
    public int getLastHitNumber() { return mLastHitNumber; }

    public boolean isRunning() { return !(mIsPaused || mIsStopped); }

    public boolean isPaused() { return mIsPaused;}

    private Random rnd() { return mRandom; }
    /** grab the global game data */
    private GameData gdata() {
        return mGameData;
    }

    /** retrieve the dimensions of a standard dart board to calculate
     *  bot accuracy. The only data the bot needs is the multiplier and bulls
     *  eye rations (at the bottom) */
    public static class DartboardDimensions {
        private static final int pointCount = 20;
        private static final double pi = 3.1415f;
        private static final double dartBoardDiameter = 17.75f;
        private static final double bullDiameter = 1.26f;
        private static final double dubBullDiameter = .5f;
        private static final double multiplierThickness = .315f;
        private static final double centerToOutTrip = 4.21f;
        private static final double centerToOutDub = 6.69f;

        private static final double centerToOutTripArea = Math.pow(centerToOutTrip, 2) * pi;
        private static final double centerToInTripArea = Math.pow(centerToOutTrip-multiplierThickness, 2) * pi;
        private static final double centerToOutDubArea = Math.pow(centerToOutDub, 2) * pi;
        private static final double centerToInDubArea = Math.pow(centerToOutDub-multiplierThickness, 2) * pi;

        private static final double dartBoardArea = Math.pow(dartBoardDiameter/2f, 2)* pi;
        private static final double bullArea = Math.pow(bullDiameter/2f, 2) * pi;
        private static final double dubBullArea = Math.pow(dubBullDiameter/2f, 2) * pi;

        private static final double tripArea = (centerToOutTripArea-centerToInTripArea)/pointCount;
        private static final double dubArea = (centerToOutDubArea-centerToInDubArea)/pointCount;

        private static final double fullNumberArea = (dartBoardArea/pointCount)-(bullArea/pointCount);
        private static final double singleArea = fullNumberArea-tripArea-dubArea-(bullArea/pointCount);

        // these are the ratios of board areas to the single point area
        // what we are looking for to calculate bot accuracy
        public static final float doubleToSingle = (float) (dubArea / singleArea);
        public static final float tripleToSingle = (float) (tripArea / singleArea);
        public static final float bullToSingle = (float) ((bullArea/pointCount) / singleArea);
        public static final float dubBullToSingle = (float) ((dubArea/pointCount) / singleArea);
    }
}
