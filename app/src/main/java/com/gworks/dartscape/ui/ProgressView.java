package com.gworks.dartscape.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.gworks.dartscape.util.MyColors;


/**
 * A button widget for dart game sheets. Keeps
 * track of hits and shows them in the form of game
 * sheet marks.
 */
public class ProgressView extends View {
    private RectF mViewBounds;
    private Paint mPaint;

    private float mProgress;

    public ProgressView(Context context) {
        this(context, null);
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public void initPaint() {
        mPaint = new Paint();

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(1);
        mPaint.setColor(MyColors.themeHighlight());
        mPaint.setMaskFilter(null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewBounds = new RectF(0, 0, w, h);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, mViewBounds.width() * mProgress, mViewBounds.height(), mPaint);
    }

    public void runProgress(long progress) {
        animateProgress(progress);
        mProgress = progress;
    }

    public void stopProgress() {
        if(mProgressAnimator.isRunning()) mProgressAnimator.end();
        mProgress = 0; invalidate();
    }

    private ValueAnimator mProgressAnimator = new ValueAnimator();
    private void animateProgress(long progressTo) {
        if(mProgressAnimator.isRunning()) mProgressAnimator.end();
        mProgressAnimator = ValueAnimator.ofFloat(0f, 1f);
        mProgressAnimator.setDuration(progressTo); // milliseconds
        mProgressAnimator.setInterpolator(new AccelerateInterpolator());
        mProgressAnimator.addUpdateListener(animator -> {
            mProgress = (float) animator.getAnimatedValue();
            invalidate();
        }); mProgressAnimator.start();
    }
}
