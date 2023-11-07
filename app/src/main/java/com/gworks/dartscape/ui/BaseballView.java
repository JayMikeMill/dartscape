package com.gworks.dartscape.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.gworks.dartscape.R;
import com.gworks.dartscape.data.GameFlags;
import com.gworks.dartscape.util.MyColors;
import com.gworks.dartscape.util.Sound;

import java.util.ArrayList;

/**
 * A button widget for dart game sheets. Keeps
 * track of hits and shows them in the form of game
 * sheet marks.
 */
public class BaseballView extends ScoreButton {
    private float mFieldQuad, mBaseQuad;
    private PointF[] mBasePos;
    private RectF[] mRectBases;

    private int mInning, mOuts, mOnBase, mRuns, mHits;

    private ArrayList<BaseBallPlayer> mPlayers;

    public BaseballView(Context context) {
        this(context, null);

    }

    public BaseballView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        setGameMode(GameFlags.GameFlag.BASEBALL);
        initData();
    }

    @Override
    public void initData() {
        super.initData();

        mPlayers = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int smallSide = Math.min(w, h);



        mTextSize =((float) smallSide / 2) *.3f;

        mFieldQuad = (smallSide / 2f) - (smallSide/10f);
        setStrokeWidth(mFieldQuad * .05f);

        mBasePos = new PointF[]
                { new PointF(mCenterX,  mCenterY + mFieldQuad),
                        new PointF(mCenterX + mFieldQuad, mCenterY),
                        new PointF(mCenterX, mCenterY - mFieldQuad),
                        new PointF(mCenterX - mFieldQuad, mCenterY) };


        mBaseQuad = mFieldQuad / 16f;
        mRectBases = new RectF[] {
                new RectF(mBasePos[0].x - mBaseQuad, mBasePos[0].y - mBaseQuad,
                        mBasePos[0].x + mBaseQuad, mBasePos[0].y + (mBaseQuad * 1.1f)),
                new RectF(mBasePos[1].x - mBaseQuad, mBasePos[1].y - mBaseQuad,
                        mBasePos[1].x + mBaseQuad, mBasePos[1].y + mBaseQuad),
                new RectF(mBasePos[2].x - mBaseQuad, mBasePos[2].y - mBaseQuad,
                        mBasePos[2].x + mBaseQuad, mBasePos[2].y + mBaseQuad),
                new RectF(mBasePos[3].x - mBaseQuad, mBasePos[3].y - mBaseQuad,
                        mBasePos[3].x + mBaseQuad, mBasePos[3].y + mBaseQuad)
        };
   }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawField(canvas);
        drawPlayers(canvas);

        // drow center label
       // drawText(getString(R.string.hit), canvas, getPaint(), mCenterX, mCenterY, 1f, false, true, true);

        drawGameInfo(canvas);
    }

    private void drawField(Canvas canvas) {
        Path path = new Path();
        float baseQuad = mRectBases[0].width()/2;
        path.setLastPoint(mBasePos[0].x, mBasePos[0].y + (baseQuad*1.1f));
        path.lineTo(mBasePos[1].x + baseQuad, mBasePos[1].y);
        path.lineTo(mBasePos[2].x, mBasePos[2].y - baseQuad);
        path.lineTo(mBasePos[3].x - baseQuad, mBasePos[3].y);
        path.close();

        canvas.drawPath(path, getShadowPaint());
        canvas.drawPath(path, getPaint(MyColors.themeTextDisabled()));

        // draw home plate

        // draw bases

     ;
        // draw bases
        for (int i = 0; i < 4; i++) {
            path = new Path();

            if (i == 0) {
                path.setLastPoint(mRectBases[i].centerX(), mRectBases[i].bottom);
                path.lineTo(mRectBases[i].right, mRectBases[i].centerY() + (mRectBases[i].height()*0.05f));
                path.lineTo(mRectBases[i].right, mRectBases[i].top);
                path.lineTo(mRectBases[i].left, mRectBases[i].top);
                path.lineTo(mRectBases[i].left, mRectBases[i].centerY() + (mRectBases[i].height()*0.05f));
            } else {
                path.setLastPoint(mRectBases[i].centerX(), mRectBases[i].bottom);
                path.lineTo(mRectBases[i].right, mRectBases[i].centerY());
                path.lineTo(mRectBases[i].centerX(), mRectBases[i].top);
                path.lineTo(mRectBases[i].left, mRectBases[i].centerY());
            }

            path.close();

            canvas.drawPath(path, getShadowPaint());
            canvas.drawPath(path, getFillPaint());

        }
    }

    private void drawPlayers(Canvas canvas) {
        for(BaseBallPlayer player : mPlayers) {
           player.draw(canvas);
        }
    }

    private void drawGameInfo(Canvas canvas) {
        float spreadPad = mCenterX / 8f;
        float inningCenterX = (mCenterX * 0.45f) - spreadPad;
        float outsCenterX = mCenterX + (mCenterX * 0.55f) + spreadPad;
        float valueSize = 1.2f;
//        drawText(getString(R.string.inning), canvas, getPaint(false),  inningCenterX, mCenterY * 0.125f, .4f, false, true, false);
//        drawText(String.valueOf(mInning), canvas, getPaint(false),  inningCenterX, mCenterY * 0.4f, 1.5f, false, true, false);
        //drawText(getString(R.string.inning), canvas, getPaint(false),  mCenterX, mCenterY *.65f, .4f, false, true, false);
        drawText(String.valueOf(mInning), canvas, getPaint(),   mCenterX, mCenterY, 3f, false, true, true);

        drawText(getString(R.string.outs), canvas, getPaint(false),  outsCenterX, mCenterY * 0.125f, .4f, false, true, false);
        drawText(String.valueOf(mOuts), canvas, getPaint(false),  outsCenterX, mCenterY * 0.4f, valueSize, false, true, false);

        drawText(getString(R.string.runs),
                canvas, getPaint(false),  inningCenterX, mCenterY * 0.125f, .4f, false, true, false);
        drawText(String.valueOf(mRuns), canvas, getPaint(false), inningCenterX, mCenterY * 0.4f, valueSize, false, true, false);

        drawText(getString(R.string.on_base),
                canvas, getPaint(false), outsCenterX, mCenterY + (mCenterY * 0.875f), .4f, false, true, false);
        drawText(String.valueOf(mOnBase), canvas, getPaint(false), outsCenterX, mCenterY + (mCenterY * 0.6f), valueSize, false, true, false);

        drawText(getString(R.string.base) + " " + getString( mHits == 1 ? R.string.hit :  R.string.hits),
                canvas, getPaint(false), inningCenterX, mCenterY + (mCenterY * 0.875f), .4f, false, true, false);
        drawText(String.valueOf(mHits), canvas, getPaint(false), inningCenterX, mCenterY + (mCenterY * 0.6f), valueSize, false, true, false);

    }

    public void setGameInfo(int inning, int outs, int onBase, int runs, int hits) {
        mInning = inning; mOuts = outs; mOnBase = onBase; mRuns = runs; mHits = hits;
        invalidate();
    }

    private class BaseBallPlayer {
        private int mBase;
        private final PointF mPosition;
        private final BaseballPlayerListener mListener;

        BaseBallPlayer(int base, BaseballPlayerListener listener) {
            mBase = base;
            mPosition = new PointF(mBasePos[base].x, mBasePos[base].y);
            mListener = listener;
        }

        public void draw(Canvas canvas) {
            float radius = mFieldQuad / 8;
            float invRadius = (int) (radius / 3.1415);

            canvas.drawCircle(mPosition.x, mPosition.y, radius * 1.2f, getShadowPaint());
            canvas.drawCircle(mPosition.x, mPosition.y, radius * 1.2f,
                    getPaint(MyColors.themePrimary(), getStrokeWidth(), true) );
            canvas.drawCircle(mPosition.x, mPosition.y, radius,
                    getPaint(getColor(), getStrokeWidth(), true) );
            canvas.drawCircle(mPosition.x, mPosition.y, radius * .7f,
                    getPaint(MyColors.themePrimary(), getStrokeWidth(), true) );

            canvas.drawLine(mPosition.x - radius + invRadius, mPosition.y - radius + invRadius,
                    mPosition.x + radius - invRadius, mPosition.y + radius - invRadius,
                    getPaint(MyColors.themeText(), getStrokeWidth() / 2f, true));
            canvas.drawLine(mPosition.x + radius - invRadius, mPosition.y - radius + invRadius,
                    mPosition.x - radius + invRadius, mPosition.y + radius - invRadius,
                    getPaint(MyColors.themeText(), getStrokeWidth() / 2f, true));
        }

        private boolean mHeadedHome = false;
        public void runBases(int bases) {
            if(mHeadedHome) return;
            if(mBase + bases > 3) bases = 4 - mBase;
            if(mBase + bases < 0) bases = mBase;

            boolean undo = bases < 0;
            for (int i = 0; i < Math.abs(bases); i++) {
                int runto = mBase + (undo ? -(i + 1) : (i + 1));
                if(runto == 4) runto = 0;
                if(runto == 0) mHeadedHome = true;

                int finalRunto = runto;
                new Handler(Looper.myLooper()).postDelayed
                        ((Runnable) () -> runPlayerAnimation(finalRunto),
                                (long) PLAYER_ANI_DURATION * i);
            }

            new Handler(Looper.myLooper()).postDelayed(() -> {
                        if(undo && mBase == 0) mListener.onReturn(this);
                        else if(mBase == 0) mListener.onHomeRun(this);
                    }, (long) PLAYER_ANI_DURATION * Math.abs(bases));

            mBase += bases;
            if(mBase  > 3) mBase = 0;
        }

        public boolean isAnimating() {
            return  mPlayerAnimator.isRunning();
        }

        private static final int PLAYER_ANI_DURATION = 200;
        private ValueAnimator mPlayerAnimator = new ValueAnimator();
        private void runPlayerAnimation(int toBase) {
            if(mPlayerAnimator.isRunning()) {
               mPlayerAnimator.end();
            }

            PointF fromPoint = mPosition;
            PointF toPoint  = new PointF(mBasePos[toBase].x, mBasePos[toBase].y);

            mPlayerAnimator = ValueAnimator.ofFloat(0f, 1f);
            mPlayerAnimator.setDuration(PLAYER_ANI_DURATION); // milliseconds
            mPlayerAnimator.setInterpolator(new LinearInterpolator());

            mPlayerAnimator.addUpdateListener(animator -> {
                float val = (float) animator.getAnimatedValue();

                float newX = fromPoint.x + ((toPoint.x - fromPoint.x)  * val);
                float newY = fromPoint.y + ((toPoint.y - fromPoint.y)  * val);
                mPosition.set(newX, newY);
            }); mPlayerAnimator.start();
        }
    }

    private final BaseballPlayerListener mPlayerListener = new BaseballPlayerListener() {
        @Override
        public void onHomeRun(BaseBallPlayer player) {
            mPlayers.remove(player);
            //popSoundAnimate(BB_SCORE_CODE_RUN, 0, false);
        }

        @Override
        public void onReturn(BaseBallPlayer player) {
            mPlayers.remove(player);
        }
    };

    private interface BaseballPlayerListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        void onHomeRun(BaseBallPlayer player);
        void onReturn(BaseBallPlayer player);
    }


    /* called when a player presses this button */
    @Override
    public void hit(int bases) {
        //if(isAnimating()) return;

        super.hit(bases);
    }

    public boolean isAnimating() {
        for(BaseBallPlayer player : mPlayers) {
            if(player.isAnimating()) return true;
        }
        return false;
    }

    public void setBases(boolean[] onBase) {
        mPlayers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if(onBase[i]) mPlayers.add(new BaseBallPlayer(i, mPlayerListener));
        }
    }

    public void runBases(int bases) {
        if(bases > 0) {
            mPlayers.add(new BaseBallPlayer(0, mPlayerListener));
        } else if(bases == 0) {
            popSoundAnimate(0, 0, false);
        }

        for (BaseBallPlayer player : mPlayers)
            player.runBases(bases);
    }


    private static final int BB_SCORE_CODE_RUN = 4851;
    private boolean mAnimatingOut = false;
    @Override
    public void popSoundAnimate(int score, int multi, boolean undo)  {

        int aniColor;

        String hitter =  multi == 4 ? getString(R.string.homer) :
                         multi == 3 ? getString(R.string.triple) :
                         multi == 2 ? getString(R.string._double) :
                         multi == 1 ? getString(R.string.single) : "";

        String text = score == 0 ? "" : score + " " + getString(R.string.run);

        text += !hitter.isEmpty() ? (text.isEmpty() ? "" : "\n") + hitter : "";

        if(score == 4) text = getString(R.string.grand) + "\n" + getString(R.string.slam) + "!";

        if(score == BB_SCORE_CODE_RUN) text = getString(R.string.run);

        boolean isOut = multi == 0;
        if(isOut) {
            if(mAnimatingOut && isAnimatingHit()) return;
            else mAnimatingOut = true;
            text = getString(R.string.out);
        } else {
            mAnimatingOut = false;
        }

        if(undo && isOut) {
            return;
        }

        //text += undo ? "" : "!";

        aniColor =  multi == 4 ? MyColors.themeText() :
                    multi == 3 ? MyColors.COLOR_TRIPLE  :
                    multi == 2 ? MyColors.COLOR_DOUBLE  :
                    multi == 1 ? MyColors.COLOR_SINGLE  :
                    MyColors.RED_LIGHT;

        if(score == BB_SCORE_CODE_RUN) aniColor = MyColors.themeText();

        if(undo) aniColor = MyColors.RED_LIGHT;

        new TextPopup((LayoutInflater) getContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE)).popup
                (text, aniColor, this, 1f,
                        text.contains("\n") ? 0.35f : (isOut ? .4f : .25f), true);

        // play hit sound.
        if(!undo) {
            if (multi == 0) Sound.play(Sound.MISS);
            else if (multi == 1) Sound.play(Sound.BBALL1);
            else if (multi == 2) Sound.play(Sound.BBALL2);
            else if (multi == 3) Sound.play(Sound.BBALL2);
            else if (multi == 4) Sound.play(Sound.BBALL3);
        }

        runHitAnimation(aniColor);
    }

    private String getString(int stringId) {
        return getResources().getString(stringId);
    }
}
