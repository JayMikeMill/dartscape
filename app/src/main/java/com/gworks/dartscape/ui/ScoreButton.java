package com.gworks.dartscape.ui;

import static com.gworks.dartscape.logic.GameLogic.ALL_NUMBERS;
import static com.gworks.dartscape.util.Helper.dpToPx;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameFlags;
import com.gworks.dartscape.util.Font;
import com.gworks.dartscape.util.Helper;
import com.gworks.dartscape.util.MyColors;
import com.gworks.dartscape.util.Sound;

/**
 * A button widget for dart game sheets. Keeps
 * track of hits and shows them in the form of game
 * sheet marks.
 */
public class ScoreButton extends View {
    private int mPlayerIndex;
    private int mNumber;
    private int mColor, mShadowColor, mBlendColor;
    private float mBlendRatio;
    private float mStrokeWidth;
    private boolean mActive;

    private GameFlags.GameFlag mGameMode;

    protected float mTextSize;
    protected float mCenterX, mCenterY;
    private float mPadding, mRadius;

    private int mMarks;

    private float mShangRadius;
    private RectF mShangOval;

    protected Rect mRectView;

    private final Paint mPaint = new Paint();
    private final Paint mShadowPaint = new Paint();

    private ScoreButtonListener mListener;

    public ScoreButton(Context context) {
        this(context, null);
    }

    public ScoreButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setSoundEffectsEnabled(false);
        setOnTouchListener(mScoreTouchListener);


        initData();
    }

    public void initData() {
        setGameMode(GameFlags.GameFlag.NONE);
        setPlayerIndex(0);
        setNumber(0);

        setEnabled(true);
        setActive(true);
        setVisibility(View.VISIBLE);

        setColor(MyColors.themeText());
        setShadowColor(MyColors.themeShadow());

        setBlendColor(Color.TRANSPARENT);
        setBlendRatio(0f);

        setLayoutParams(new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setActive(true);
        setEnabled(true);

        setMarks(0);
        setNumber(0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mRectView = new Rect(0, 0, w, h);

        int smallSide = Math.min(w, h);

        setStrokeWidth(smallSide/12f);

        mCenterX = w / 2f;
        mCenterY = h / 2f;
        mPadding = (getStrokeWidth() / 4f);

        if (!isInEditMode()) mPadding += dpToPx(1);

        mRadius = ((float) smallSide / 2) - getStrokeWidth() - mPadding;
        mTextSize = (mRadius*2) * ((getNumber() == SB_CODE_BUST_1 || getNumber() == 25)  ? .3f : .7f);
        if(getNumber() == SB_CODE_OUT) mTextSize = (mRadius*2) * .4f;
        if(getNumber() == SB_CODE_HOME_RUN) mTextSize = (mRadius*2) * .4f;
        mShangRadius = mRadius * 0.9f;
        mShangOval = new RectF((int) (mCenterX - mShangRadius), (int) (mCenterY - mShangRadius),
                (int) (mCenterX + mShangRadius), (int) (mCenterY + mShangRadius));
    }


    /** @return The normal paint with default color. */
    protected Paint getPaint() {
        return getPaint(getColor(), getStrokeWidth(), false);
    }

    /** @return The normal paint with default color. */
    protected Paint getPaint(boolean blend) {
        return getPaint(getColor(blend), getStrokeWidth(), false, blend);
    }

    /** @return The normal paint with default color. */
    protected Paint getFillPaint() {
        return getPaint(getColor(), getStrokeWidth(), true);
    }

    /** @return The normal paint with edited color. */
    protected Paint getPaint(int color) {
        return getPaint(color, getStrokeWidth(), false);
    }

    /** @return The normal paint with edited color. */
    protected Paint getPaint(float strokeWidth) {
        return getPaint(getColor(), strokeWidth, false);
    }

    /** @return The normal paint with edited attributes. */
    protected Paint getPaint(int color, float stroke, boolean fill) {
        return getPaint(color, stroke, fill, true);
    }

    /** @return The normal paint with edited attributes. */
    protected Paint getPaint(int color, float stroke, boolean fill, boolean blend) {
        mPaint.setStyle(fill ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);
        mPaint.setStrokeWidth(stroke);
        mPaint.setColor(color);
        mPaint.setAlpha(Color.alpha(color));
        mPaint.setMaskFilter(null);

        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);

        return mPaint;
    }

    /** @return The paint for painting shadows. */
    protected Paint getShadowPaint() {
        return getShadowPaint(true);
    }

    /** @return The paint for painting shadows. */
    protected Paint getShadowPaint(boolean blend) {
        float strokeWidth = getStrokeWidth();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) Helper.dpToPx(1);
        if (isInEditMode()) strokeWidth = 5;

        mShadowPaint.setStyle(Paint.Style.STROKE);
        mShadowPaint.setStrokeWidth(strokeWidth);
        mShadowPaint.setColor(getShadowColor(blend));

        mShadowPaint.setMaskFilter(new BlurMaskFilter
                (strokeWidth, BlurMaskFilter.Blur.NORMAL));

        mShadowPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        setLayerType(LAYER_TYPE_SOFTWARE, mShadowPaint);

        return mShadowPaint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int color = isEnabled() ? getColor() : MyColors.themeTextDisabled();

        if(is01() || isBaseball()) {
            canvas.drawCircle(mCenterX, mCenterY, mRadius, getShadowPaint());
            canvas.drawCircle(mCenterX, mCenterY, mRadius, getPaint(color));
            drawLabel(canvas, getPaint(color));
        } else if (isCricket()) {
            drawCricketMarker(canvas, getShadowPaint());
            drawCricketMarker(canvas, getPaint(color));
            //drawCricketMarker(canvas, getPaint(colorFor, 255, getStrokeWidth() / 3f));
        } else if(isShanghai()) {
            drawShanghaiSlice(canvas, getPaint(color));

            canvas.drawCircle(mCenterX, mCenterY, mShangRadius, getShadowPaint());
            canvas.drawCircle(mCenterX, mCenterY, mShangRadius, getPaint(color));


            drawShanghaiNumbers(canvas, getPaint(color), true);

            //drawLabel(canvas, getShadowPaint(), 0.7f);
            drawLabel(canvas, getPaint(color), 0.7f, true);
        } if(isKiller()) {
            boolean isKiller = getNumber() == SB_CODE_KILLER;
            boolean isKill   = getNumber() == SB_CODE_KILL;
            float labelSize = 1f;

            if(isKiller ||isKill || getMarks() == 1) {
                canvas.drawCircle(mCenterX, mCenterY, mRadius, getShadowPaint());
                canvas.drawCircle(mCenterX, mCenterY, mRadius, getPaint(color));

                if(isKiller) labelSize = 0.4f;
                else if(isKill) labelSize = 0.6f;
            }

            drawLabel(canvas, getPaint(color), labelSize);
        }
    }

    private void drawLabel(Canvas c, Paint paint) {
        drawLabel(c, paint, 1, false);}
    private void drawLabel(Canvas c, Paint paint, float textRat) {
        drawLabel(c, paint, textRat, false);}
    private void drawLabel(Canvas c, Paint paint, float textRat, boolean outLine) {
        drawLabel(c, paint,  mCenterX, mCenterY, textRat, outLine);}
    private void drawLabel(Canvas c, Paint paint, float x, float y, float textRat, boolean outLine) {
        String numText = String.valueOf(getNumber());

        if(getNumber() == 25) numText = getString(R.string.bull);
        else if(getNumber() == SB_CODE_BUST_1)     numText = getString(R.string.bust);
        else if(getNumber() == SB_CODE_KILLER)     numText = getString(R.string.killer);
        else if(getNumber() == SB_CODE_KILL)       numText = getString(R.string.kill);
        else if(getNumber() == SB_CODE_OUT)        numText = getString(R.string.out);
        else if(getNumber() == SB_CODE_HOME_RUN)   numText = getString(R.string.hr);

        drawText(numText, c, paint, x, y, textRat, outLine);
    }

    protected void drawText(String text, Canvas canvas, Paint paint,  float x, float y, float textRat, boolean outline) {
        drawText(text, canvas, paint,  x, y, textRat, outline, true, true);
    }

    protected void drawText(String text, Canvas canvas, Paint paint,  float x, float y,
                            float textRat,  boolean outline, boolean shadow, boolean blend) {

        paint.setTextSize(mTextSize*textRat);
        paint.setTypeface(Font.LILITA);
        paint.setStrokeWidth(1f);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);

        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        float centerTextX = x - rect.width() / 2f - rect.left;
        float centerTextY = y + rect.height() / 2f - rect.bottom;

        if(shadow) {
            // shadows dont work on older devices
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                drawText(text, canvas, getShadowPaint(blend), x, y, textRat, false, false, blend);
                setLayerType(LAYER_TYPE_NONE, paint);
            }
        }

        canvas.drawText(text, centerTextX, centerTextY, paint);

        if(!outline) return;

        // outlines dont work on older devices
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) return;

        paint.setStrokeWidth(Helper.dpToPx(2));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(MyColors.themeOutline());
        setLayerType(LAYER_TYPE_NONE, paint);

        canvas.drawText(text, centerTextX, centerTextY, paint);
    }

    private void drawCricketMarker(Canvas c, Paint p) {
        int invRadius = (int) (mRadius / 3.1415);

        if (mMarks <= 0) // "--"
            c.drawRect(mCenterX - mRadius + mPadding, mCenterY - mPadding,
                    mCenterX + mRadius - mPadding, mCenterY + mPadding, p);
        if (mMarks > 0) // "\"
            c.drawLine(mCenterX - mRadius + invRadius, mCenterY - mRadius + invRadius,
                    mCenterX + mRadius - invRadius, mCenterY + mRadius - invRadius, p);
        if (mMarks > 1) // "/"
            c.drawLine(mCenterX + mRadius - invRadius, mCenterY - mRadius + invRadius,
                    mCenterX - mRadius + invRadius, mCenterY + mRadius - invRadius, p);
        if (mMarks > 2) // "O"
            c.drawCircle(mCenterX, mCenterY, mRadius, p);
    }

    private static final float DEG_TO_RAD = 0.0174533f;
    protected void drawShanghaiNumbers(Canvas canvas, Paint paint, boolean shadow) {
        float numSweep = 360 / 20f;
        float startAng, newAngle, textX, textY;
        float textSize;

        int origColor = paint.getColor();
        int darkColor = MyColors.themeTextDisabled();

        for (int i = 0; i < ALL_NUMBERS.length; i++) {
            startAng = (i * numSweep) - 90 - (numSweep / 2);

            newAngle = startAng + (numSweep / 2);
            textX = mCenterX + (float) Math.cos(newAngle*DEG_TO_RAD) * mShangRadius;
            textY = mCenterY + (float) Math.sin(newAngle*DEG_TO_RAD) * mShangRadius;

            textSize = ALL_NUMBERS[i] == getNumber() ? .25f : .15f;


            paint.setColor(ALL_NUMBERS[i] == getNumber() ? origColor : darkColor);

            drawText(String.valueOf(ALL_NUMBERS[i]), canvas, paint, textX, textY, textSize, shadow);
        }
    }

    private float mShangSliceAngle;
    private static final float NUM_SWEEP = 360 / 20f;
    private void drawShanghaiSlice(Canvas canvas, Paint paint) {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAlpha(140);
        paint.setStrokeWidth(1);

        canvas.drawArc(mShangOval, mShangSliceAngle, NUM_SWEEP, true, paint);
    }

    private ValueAnimator mSliceAnimator = new ValueAnimator();
    private void animateShanghaiSlice(int numTo) {
        int numToIndex = 0;
        for (int i = 0; i < ALL_NUMBERS.length; i++) {
            if (ALL_NUMBERS[i] == numTo) numToIndex = i;
        }

        float startAng = mShangSliceAngle; //((numFromIndex) * NUM_SWEEP) - 90 - (NUM_SWEEP / 2);
        float EndAng = ((numToIndex) * NUM_SWEEP) - 90 - (NUM_SWEEP / 2);

        mSliceAnimator = ValueAnimator.ofFloat(startAng, EndAng);
        mSliceAnimator.setDuration(500); // milliseconds
        mSliceAnimator.addUpdateListener(animator -> {
            mShangSliceAngle = (float) animator.getAnimatedValue();
        }); mSliceAnimator.start();
    }

    private void setShanghaiSlice(int number) {
        mShangSliceAngle = ((number) * NUM_SWEEP) - 90 - (NUM_SWEEP / 2);
        invalidate();
    }

    private static final int HIT_ANI_DURATION = 400;
    private ValueAnimator mHitAnimator = new ValueAnimator();
    protected void runHitAnimation(int color) {
        if(mTouchAnimator.isRunning()) mTouchAnimator.end();
        if(mHitAnimator.isRunning()) mHitAnimator.end();

        setBlendRatio(0);
        setBlendColor(color);

        float fromBrightness = 0.0f;
        float toBrightness   = 0.8f;
        float midPoint   = toBrightness / 2f;
        mHitAnimator = ValueAnimator.ofFloat(fromBrightness, toBrightness);
        mHitAnimator.setDuration(HIT_ANI_DURATION); // milliseconds
       // mHitAnimator.setInterpolator(new FastOutLinearInInterpolator());
        mHitAnimator.addUpdateListener(animator -> {
            float val = (float) animator.getAnimatedValue();
            if(val > midPoint) val = midPoint - (val - midPoint);
            setBlendRatio(val*2);
        });

        mHitAnimator.start();
    }

    private static final int TOUCH_ANI_DURATION = 150;
    private static final int ANI_STYLE_LIGHT_UP = 2;
    private static final int ANI_STYLE_LIGHT_DOWN = 3;
    private ValueAnimator mTouchAnimator = new ValueAnimator();
    private void runTouchAnimation(int aniStyle) {

        if(mHitAnimator.isRunning()) return;
        if(mTouchAnimator.isRunning()) mTouchAnimator.end();

        setBlendColor(Color.WHITE);

        float fromBlendRatio = getBlendRatio();
        float toBlendRatio = 0.0f;

        if(aniStyle == ANI_STYLE_LIGHT_UP)  toBlendRatio = 0.5f;
        else if(aniStyle == ANI_STYLE_LIGHT_DOWN) toBlendRatio = 0.0f;

        mTouchAnimator = ValueAnimator.ofFloat(fromBlendRatio, toBlendRatio);
        mTouchAnimator.setDuration(TOUCH_ANI_DURATION); // milliseconds
        mTouchAnimator.setInterpolator(new FastOutLinearInInterpolator());

        mTouchAnimator.addUpdateListener(animator -> {
            float val = (float) animator.getAnimatedValue();
            setBlendRatio(val);
        });

        mTouchAnimator.start();
    }

    public boolean isAnimatingHit() {
        return mHitAnimator.isRunning();
    }


    /* called when a player presses this button */
    public void hit(int multiplier) {
        if (!isEnabled() || !isActive()) return;
        if (mListener != null) mListener.onHit(this, multiplier);
    }


    public int getPlayerIndex() {
        return mPlayerIndex;
    }

    public void setPlayerIndex(int index) {
        mPlayerIndex = index;
    }

    public void setCricketNumberIndex(int index) {
        mNumber = (index == 6) ? 25 : 20 - index;
    }


    public void setShangNumber(int number, boolean animate) {
        if(!isShanghai()) return;

        if(mNumber != number && animate)
            animateShanghaiSlice(number);
        else if(mNumber != number) setShanghaiSlice(number);

        mNumber = number;
        invalidate();
    }
    public void setNumber(int number) {
        mNumber = number;
        invalidate();
    }

    public int getNumber() {
        return mNumber;
    }

    public void setStrokeWidth(float width) {
        mStrokeWidth = width;
    }

    public float getStrokeWidth() {
        // larger stroke for bigger shanghai button
        if(isShanghai()) return mStrokeWidth / 3;
        return mStrokeWidth;
    }

    public int getColor() {
        return getColor(true);
    }

    public int getColor(boolean blend) {
        if(isBlending() && blend)
            return ColorUtils.blendARGB(mColor, getBlendColor(), getBlendRatio());
        return mColor;
    }


    public void setColor(int color) {
        mColor = color;
        invalidate();
    }

    public int getShadowColor() {
        return getShadowColor(true);
    }
    public int getShadowColor(boolean blend) {
        if(isBlending() && blend)
            return ColorUtils.blendARGB(mShadowColor, getBlendColor(), getBlendRatio());

        return mShadowColor;
    }

    public void setShadowColor(int color) {
        mShadowColor = color;
        invalidate();
    }

    public int getBlendColor() {
        return mBlendColor;
    }

    public void setBlendColor(int color) {
        mBlendColor = color;
        invalidate();
    }

    public float getBlendRatio() {
        return mBlendRatio;
    }

    public void setBlendRatio(float ratio) {
        mBlendRatio = ratio;
        invalidate();
    }

    public boolean isBlending() {
        return getBlendRatio() != 0;
    }


    @Override
    public void setEnabled(boolean enabled) {
        setEnabled(enabled, true);
    }
    public void setEnabled(boolean enabled, boolean changeBackground) {
        super.setEnabled(enabled);
        if(changeBackground)
            setBackgroundColor(enabled ? Color.TRANSPARENT : MyColors.themeDisabled());
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        mActive = active;
        invalidate();
    }

    public void setGameMode(GameFlags.GameFlag gameMode) {
        mGameMode = gameMode;
        invalidate();
    }

    public int getMarks() {
        return mMarks;
    }

    public void setMarks(int marks) {
        mMarks = marks;
        invalidate();
    }


    public static final int SB_CODE_INACTIVE         = 9999;
    public static final int SB_CODE_MISS             = 0;
    public static final int SB_CODE_MISSED_IN_DOUBLE = -3122141;
    public static final int SB_CODE_MISSED_IN_TRIPLE = -3122142;
    public static final int SB_CODE_MISSED_IN_MASTER = -3122143;
    public static final int SB_CODE_BUST_1           = -1;
    public static final int SB_CODE_BUST_2           = -9999;
    public static final int SB_CODE_CRICKET_CAP      = -31512296;
    public static final int SB_CODE_KILLER           = -311134;
    public static final int SB_CODE_KILL             = -3111;
    public static final int SB_CODE_FATALITY         = -8348;
    public static final int SB_CODE_OUT              = -8077;
    public static final int SB_CODE_HOME_RUN         = -3054567;
    public void popSoundAnimate(int score, int multi, boolean undo) {
        score = undo ? -score : score;

        boolean isMiss = score == SB_CODE_MISS && multi == SB_CODE_MISS;
        boolean isMissedIn = is01() && (score == SB_CODE_MISSED_IN_DOUBLE
                || score == SB_CODE_MISSED_IN_TRIPLE || score == SB_CODE_MISSED_IN_MASTER );
        boolean isBust    = is01()      &&  (getNumber() == SB_CODE_BUST_1 || score == SB_CODE_BUST_2);
        boolean isCap     = isCricket() &&  score == SB_CODE_CRICKET_CAP;
        boolean isKiller  = isKiller()  &&  score == SB_CODE_KILLER;
        boolean isKill    = isKiller()  &&  score == SB_CODE_KILL;
        boolean isFatal   = isKiller()  &&  score == SB_CODE_FATALITY;
        boolean isSpecial = isMiss || isBust || isCap || isKiller || isKill || isFatal || isMissedIn;

        String strScore;
        strScore = isMiss ? getString(R.string.miss) : isBust ?  getString(R.string.bust) + "!" :
                isCap ? getString(R.string.cap) + "!" : isKill ? getString(R.string.kill) + "!" :
                isKiller ? getString(R.string.killer) + "!"  : isFatal ? getString(R.string.fatality) + "!" : "";

        if(isMissedIn) {
            if(score == SB_CODE_MISSED_IN_DOUBLE) strScore = getString(R.string.double_in) + "!";
            if(score == SB_CODE_MISSED_IN_TRIPLE) strScore = getString(R.string.triple_in) + "!";
            if(score == SB_CODE_MISSED_IN_MASTER) strScore = getString(R.string.master_in) + "!";
        }

        if (!isSpecial && score != 0)  strScore = String.valueOf(Math.abs(score));
        else if (!isSpecial && isCricket()) strScore = "x" + multi;


        int aniColor = MyColors.themeText();
        if (isMiss || isBust || isMissedIn || isCap || undo) aniColor = MyColors.RED_LIGHT;
        else if (isKiller || isKill || isFatal) aniColor = MyColors.RED;
        else if (multi == 1) aniColor   = MyColors.COLOR_SINGLE;
        else if (multi == 2) aniColor   = MyColors.COLOR_DOUBLE;
        else if (multi == 3) aniColor   = MyColors.COLOR_TRIPLE;

        new TextPopup((LayoutInflater) getContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE )).popup(strScore, aniColor, this,
                1f, isShanghai() ? .6f : 1f, isShanghai());

        if(!undo) {
            // play hit sound.
            if(isMiss || isMissedIn) Sound.play(Sound.MISS);
            else if(isBust)     Sound.play(Sound.BUZZER);
            else if(isCap)      Sound.play(Sound.BUZZER);
            else if(isKiller)   Sound.play(Sound.SHARPEN);
            else if(isKill)     Sound.play(Sound.SLASH);
            else if(isFatal)    Sound.play(Sound.FATALITY);
            else if(multi == 1) Sound.play(Sound.DART1);
            else if(multi == 2) Sound.play(Sound.DART2);
            else if(multi == 3) Sound.play(Sound.DART3);
            else Sound.play(Sound.DART1);
        }

        runHitAnimation(aniColor);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        if(visibility == INVISIBLE || visibility == GONE)
            cancelPendingInputEvents();
    }

    public interface ScoreButtonListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        void onHit(ScoreButton hitButton, int multi);
    }

    public void setScoreButtonListener(ScoreButtonListener listener) {
        mListener = listener;
    }

    @Override
    public void onCancelPendingInputEvents() {
        super.onCancelPendingInputEvents();
        mScoreTouchListener.onTouch(this, null);
        if(mHitAnimator.isRunning()) mHitAnimator.end();
        if(mSliceAnimator.isRunning()) mSliceAnimator.end();
    }

    /** Undo touch listener for looping undo method when button
     * is held down.
     */
    View.OnTouchListener mScoreTouchListener = new View.OnTouchListener() {
        private boolean mTapped, mLongPressed;
        private int mTapCount = 0;

        private boolean mTouchIsInBounds = false;

        private static final int MAX_DOUBLE_TAP_DURATION = 250;
        private static final int LONG_PRESS_DURATION = 150; // +TOUCH_ANI_DURATION

        private final Handler mTouchHandler = new Handler(Looper.myLooper());

        private final Runnable mTapAction       = () -> { hit(1); mTapCount = 0; mTapped = true;      };

        private final Runnable mLongPressFinal = () -> {
            if(!mTouchIsInBounds) return; hit(3); mTapCount = 0; mLongPressed = true;
        };
        private final Runnable mLongPressAction = () -> {
            if(!mTouchIsInBounds) return;
            runTouchAnimation(ANI_STYLE_LIGHT_DOWN);
            mTouchHandler.postDelayed(mLongPressFinal, TOUCH_ANI_DURATION);
        };
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(!isActive() || !isEnabled()) return false;

            if(event == null) {
                mTouchHandler.removeCallbacksAndMessages(null);
                return false;
            }

            switch(event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    // perform touch animation
                    runTouchAnimation(ANI_STYLE_LIGHT_UP);
                    mTouchIsInBounds = true;

                    mTapped = false;
                    mLongPressed = false;
                    mTapCount++;

                    mTouchHandler.postDelayed(mLongPressAction, LONG_PRESS_DURATION);

                    return true;

                case MotionEvent.ACTION_MOVE:
                    if(mLongPressed) return true;

                    boolean isInBounds = mRectView.contains(mRectView.left + (int) event.getX(),
                            mRectView.top + (int) event.getY());

                    boolean justLeft = mTouchIsInBounds && !isInBounds;
                    boolean justEntered = !mTouchIsInBounds && isInBounds;

                    mTouchIsInBounds = isInBounds;

                    // perform touch animation
                    if (justLeft) runTouchAnimation(ANI_STYLE_LIGHT_DOWN);
                    if (justEntered) {
                        mTouchHandler.postDelayed(mLongPressAction, LONG_PRESS_DURATION);
                        runTouchAnimation(ANI_STYLE_LIGHT_UP);
                    }

                    return true;

                case MotionEvent.ACTION_UP:

                    // if not long pressed, no need to
                    mTouchHandler.removeCallbacks(mLongPressAction);
                    mTouchHandler.removeCallbacks(mLongPressFinal);

                    if(mLongPressed) return true;

                    if(!mTouchIsInBounds) break;

                    mTouchHandler.postDelayed(mTapAction, MAX_DOUBLE_TAP_DURATION);
                    v.performClick();

                    if(!mTapped && mTapCount == 2) {
                        mTouchHandler.removeCallbacks(mTapAction);
                        mTouchHandler.postDelayed(() -> hit(2), TOUCH_ANI_DURATION);
                        runTouchAnimation(ANI_STYLE_LIGHT_DOWN);
                        mTapCount = 0;
                        return true;
                    }

                    runTouchAnimation(ANI_STYLE_LIGHT_DOWN);

                    return true;

                case MotionEvent.ACTION_CANCEL:
                    break;
            }

            mTouchHandler.removeCallbacksAndMessages(null);
            runTouchAnimation(ANI_STYLE_LIGHT_DOWN);

            return v.onTouchEvent(event);
        }
    };

    public boolean is01() {
        return mGameMode.equals(GameFlags.GameFlag.X01);
    }
    public boolean isCricket() {
        return mGameMode.equals(GameFlags.GameFlag.CRICKET);
    }
    public boolean isShanghai() {
        return mGameMode.equals(GameFlags.GameFlag.SHANGHAI);
    }
    public boolean isKiller() {
        return mGameMode.equals(GameFlags.GameFlag.KILLER);
    }
    public boolean isBaseball() {
        return mGameMode.equals(GameFlags.GameFlag.BASEBALL);
    }

    private String getString(int stringId) {
        return getResources().getString(stringId);
    }
}
