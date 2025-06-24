package com.gworks.dartscape.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.ColorUtils;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.gworks.dartscape.R;
import com.gworks.dartscape.util.Helper;
import com.gworks.dartscape.util.MyColors;

import java.util.ArrayList;

public class MultiView extends androidx.appcompat.widget.AppCompatButton {

    private ArrayList<String> mTexts;
    private ArrayList<Drawable> mIcons;

    private int mIconColorTint;
    private int mSelectedIndex;
    private int mColor, mShadowColor;
    private float mTextSizeRatio;
    private float mTextSizeRatio2;
    private float mTextSize;
    private Paint mPaint;
    private Rect mIconBounds;
    private Rect mViewBounds;
    private Rect mCanvasBounds;

    private boolean mIsToggleButton;
    private boolean mIsToggled;

    private float mCenterX;
    private float mCenterY;
    private float mTextOffSetX;
    private float mTextOffSetY;

    private boolean mOutLineText;

    public MultiView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public MultiView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MultiView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = new Paint();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiView);

            mTextSizeRatio2 =  a.getFloat(R.styleable.MultiView_textSizeRatio, 1f);

            Drawable[] icons = new Drawable[4];
            icons[0] =  a.getResourceId(R.styleable.MultiView_icon1, -1) != -1 ?
                    AppCompatResources.getDrawable(getContext(), a.getResourceId(R.styleable.MultiView_icon1, -1)) : null;
            icons[1] =  a.getResourceId(R.styleable.MultiView_icon2, -1) != -1 ?
                    AppCompatResources.getDrawable(getContext(), a.getResourceId(R.styleable.MultiView_icon2, -1)) : null;
            icons[2] =  a.getResourceId(R.styleable.MultiView_icon3, -1) != -1 ?
                    AppCompatResources.getDrawable(getContext(), a.getResourceId(R.styleable.MultiView_icon3, -1)) : null;
            icons[3] =  a.getResourceId(R.styleable.MultiView_icon4, -1) != -1 ?
                    AppCompatResources.getDrawable(getContext(), a.getResourceId(R.styleable.MultiView_icon4, -1)) : null;

            mIcons = new ArrayList<>();
            for (int i = 0; i < 4; i++)
                if (icons[i] != null) mIcons.add(icons[i]);

            mIconColorTint = a.getInt(R.styleable.MultiView_iconColorTint, Color.TRANSPARENT);

            String[] texts = new String[3];
            texts[0] =  a.getString(R.styleable.MultiView_text2);
            texts[1] =  a.getString(R.styleable.MultiView_text3);
            texts[2] =  a.getString(R.styleable.MultiView_text4);

            mTexts = new ArrayList<>();
            for (int i = 0; i < 3; i++)
                if (texts[i] != null) mTexts.add(texts[i]);

            mOutLineText = a.getBoolean(R.styleable.MultiView_outlineText, false);

            mIsToggleButton = a.getBoolean(R.styleable.MultiView_toggleButton, false);

            a.recycle();
        }

        onSizeChanged(0, 0, 0, 0);

        setColor(MyColors.themeText());
        setShadowColor(MyColors.themeShadow());

        setSelectedIndex(0);

        setText(getText());
    }

    @SuppressLint("RtlHardcoded")
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCenterX = w / 2f;
        mCenterY = h / 2f;

        mViewBounds = new Rect(0, 0, w, h);
        mCanvasBounds = new Rect(getPaddingLeft(), getPaddingTop(),
                w - getPaddingRight(), h - getPaddingBottom());

        int smallSide = Math.min(w, h);
        float quadSize = (float) smallSide / 2;

        int gravVert = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
        int gravHori = getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK;

        float iconCenterX = mCenterX;
        float iconCenterY = mCenterY;
        float iconQuadSize = quadSize;


        if(!getText().toString().isEmpty()) {
            iconQuadSize -= (smallSide / 6f);
            iconCenterY -= (iconQuadSize / 4f);
        } else {
            iconQuadSize -= (smallSide / 8f);
        }

        if(getGravity() == Gravity.CENTER) {
            iconCenterX = mCenterX;
            iconCenterY = mCenterY;

            mTextOffSetY = ((float)h/4)+(((float)h/16));
            mTextOffSetX = 0;
            mTextSizeRatio = 0.2f;
        } else if(gravVert == Gravity.BOTTOM) {
            iconCenterX = mCenterX;
            iconCenterY = ((float)h/4)+(((float)h/8));

            mTextOffSetY = ((float)h/4)+(((float)h/16));
            mTextOffSetX = 0;
            mTextSizeRatio = 0.2f;
        } else if(gravHori == Gravity.LEFT) {
            iconCenterX = ((float) w / 2) + ((float)w/4);
            iconCenterY = (float) h  / 2;

            mTextOffSetY = 0;
            mTextOffSetX = -((float)w/5);
            mTextSizeRatio = 0.9f;
        } else if(gravHori == Gravity.RIGHT) {
            iconCenterX = ((float) w / 2) - ((float)w/4);
            iconCenterY = (float) h / 2;

            mTextOffSetY = 0;
            mTextOffSetX = ((float)w/5);
            mTextSizeRatio = 0.9f;
        }

        mIconBounds = new Rect(
                (int) (iconCenterX - iconQuadSize),
                (int) (iconCenterY - iconQuadSize),
                (int) (iconCenterX + iconQuadSize),
                (int) (iconCenterY + iconQuadSize));

        calculateTextSize();
    }

    private Paint getShadowPaint() {
        float strokeWidth = isInEditMode() ? 1 : Helper.pxToScreenPx(1);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getShadowColor());
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setMaskFilter(new BlurMaskFilter(strokeWidth*2, BlurMaskFilter.Blur.NORMAL));

        // Important for certain APIs
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        setLayerType(LAYER_TYPE_SOFTWARE, mPaint);

        return mPaint;
    }

    private Paint getTextPaint() {
        return getTextPaint(getTextColors().getDefaultColor());
    }
    private Paint getTextPaint(int color) {
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(1f);
        mPaint.setMaskFilter(null);
        mPaint.setTypeface(getTypeface());
        mPaint.setTextAlign(Paint.Align.LEFT);

        // Important for certain APIs
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        setLayerType(LAYER_TYPE_NONE, null);

        return mPaint;
    }

    private Paint getPaint(int color) {
        mPaint.setFlags(mPaint.getFlags() & (~ Paint.ANTI_ALIAS_FLAG));
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(1f);
        mPaint.setMaskFilter(null);

        // Important for certain APIs
        setLayerType(LAYER_TYPE_NONE, null);

        return mPaint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawTouchAnimation(canvas);
        drawText(canvas);
        drawIcon(canvas);
    }

    private void drawIcon(Canvas canvas) {
        if (mIcons.isEmpty()) return;

        Drawable icon = mIcons.get(mSelectedIndex);
        icon.setBounds(mIconBounds);

        if(isEnabled()) icon.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.ADD);
        else icon.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

        if(mIconColorTint != Color.TRANSPARENT)
            icon.setColorFilter(mIconColorTint, PorterDuff.Mode.SRC_IN);
        icon.draw(canvas);
    }

    private void drawText(Canvas canvas) {
        String text = getText().toString();

        if(mSelectedIndex > 0 && mTexts.size() >= mSelectedIndex)
            text = mTexts.get(mSelectedIndex-1);

        if(text.isEmpty()) return; // nothing to draw

        getTextPaint();
        drawMultiText(canvas, text, getShadowPaint());
        drawMultiText(canvas, text, isEnabled() ? getTextPaint() : getTextPaint(Color.GRAY));
    }

    private void drawMultiText(Canvas canvas, String text, Paint paint) {
        String[] lines = text.split("\n");
        int lineCount = lines.length;

        float textOffX = mTextOffSetX;
        float textOffY = mTextOffSetY;

        if (mIcons.isEmpty()) { textOffX = 0; textOffY = 0; }

        float hh = paint.descent() - paint.ascent();
        float startOffsetY = -(hh*(lineCount - 1))*0.5f;
        float centerTextX, centerTextY;

        Rect textRect = new Rect(0, 0, 0, 0);
        for (int i = 0; i < lineCount; i++) {
            paint.setTextSize(mTextSize);
            paint.getTextBounds(lines[i], 0, lines[i].length(), textRect);

            centerTextX = mCenterX - textRect.width() / 2f - textRect.left + textOffX;
            centerTextY = mCenterY + textRect.height() / 2f - textRect.bottom + textOffY;
            centerTextY += startOffsetY + hh*i;

            canvas.drawText(lines[i], centerTextX, centerTextY, paint);

            if(mOutLineText) {
                // outlines don't work on older devices
                Paint olPaint = new Paint(paint);
                olPaint.setStrokeWidth(Helper.dpToPx(2));
                olPaint.setStyle(Paint.Style.STROKE);
                olPaint.setColor(MyColors.themeOutline());

                canvas.drawText(lines[i], centerTextX, centerTextY, olPaint);
            }
        }
    }

    private void calculateTextSize() {
        Paint paint = new Paint(getTextPaint());

        String text = getText().toString();
        if(mSelectedIndex > 0 && mTexts.size() >= mSelectedIndex)
            text = mTexts.get(mSelectedIndex-1);

        String[] lines = text.split("\n");
        int lineCount = lines.length;
        boolean multiLine = lineCount > 1;

        float textRat = mTextSizeRatio*mTextSizeRatio2;

        if (mIcons.isEmpty()) {
            textRat = multiLine ? 0.9f : 0.8f; }

        textRat *= mTextSizeRatio2;

        float textSize = multiLine ?
                (float) (mCanvasBounds.height() / lineCount)*textRat
                : mCanvasBounds.height()*textRat;

        // auto size text
        Rect textRect = new Rect(0, 0, 0, 0);
        Rect lineRect = new Rect(0, 0, 0, 0);

        // get widest line of text
        String widestLine = "";
        for (String line : lines) {
            paint.getTextBounds(line, 0, line.length(), lineRect);
            if(lineRect.width() > textRect.width()) {
                textRect = new Rect(lineRect); widestLine = line; }
        }

        // set text size to height, start shrinking till the widest
        // line fits
        paint.setTextSize(textSize);
        float textSizeOff = 0;
        float textSizeStep = 2;  float textSizeMin = 20;
        paint.getTextBounds(widestLine, 0, widestLine.length(), textRect);
        while (textRect.width() > mCanvasBounds.width() ||
                textRect.height() > mCanvasBounds.height()) {
            textSizeOff += textSizeStep;
            if(textSizeOff > textSize - textSizeMin) break;
            paint.setTextSize(textSize - textSizeOff);
            paint.getTextBounds(widestLine, 0, widestLine.length(), textRect);
        }

        mTextSize = textSize - textSizeOff;
    }

    private void drawTouchAnimation(Canvas canvas) {
        canvas.drawRect(mViewBounds,
                getPaint(ColorUtils.setAlphaComponent(MyColors.themeText(), (int) (mTouchBrightness *255))));
    }

    private static final int ANI_DURATION = 100;
    private static final int ANI_STYLE_LIGHT_UP = 2;
    private static final int ANI_STYLE_LIGHT_DOWN = 3;
    float mTouchBrightness;
    private ValueAnimator mTouchAnimator = new ValueAnimator();
    private void animate(int aniStyle) {

        if(mTouchAnimator.isRunning()) mTouchAnimator.end();

        float fromBrightness = mTouchBrightness;
        float toBrightness = 1.0f;

        if(aniStyle == ANI_STYLE_LIGHT_UP)
            toBrightness = isToggled() ? 0.6f : 0.3f;
        else if(aniStyle == ANI_STYLE_LIGHT_DOWN)
            toBrightness = isToggled() ? 0.3f : 0.0f;

        mTouchAnimator = ValueAnimator.ofFloat(fromBrightness, toBrightness);
        mTouchAnimator.setDuration(ANI_DURATION); // milliseconds
        mTouchAnimator.setInterpolator(new FastOutLinearInInterpolator());

        mTouchAnimator.addUpdateListener((ValueAnimator animator) -> {
            float val = (float) animator.getAnimatedValue();
            setTouchBrightness(val);
        });


        mTouchAnimator.start();
    }

    public void playClickAnimation() {
        animate(ANI_STYLE_LIGHT_UP);
        new Handler(Looper.myLooper()).postDelayed(()-> animate(ANI_STYLE_LIGHT_DOWN), ANI_DURATION);
    }

    public void setTextRatio(float ratio) {
        mTextSizeRatio2 = ratio;
    }

    public void setIcon(int index, int iconId) {
        mIcons.set(index, AppCompatResources.getDrawable(getContext(), iconId));
        invalidate();
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;

        int indexes = Math.max(mIcons.size(), mTexts.size() + (getText().toString().isEmpty() ? 0 : 1));
        if (mSelectedIndex > indexes - 1) mSelectedIndex = 0;
    }

    public void setMultiText(String text) {
        setMultiText(0,  text);
    }

    public void setMultiText(int index, String text) {
        if(index == 0) setText(text);
        else mTexts.set(index, text);
        calculateTextSize();
        invalidate();
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
        invalidate();
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    public void setShadowColor(int color) {
        mShadowColor = color;
        invalidate();
    }

    public void setTouchBrightness(float brightness) {
        mTouchBrightness = brightness;
        invalidate();
    }

    public boolean isToggleButton() {
        return mIsToggleButton;
    }

    public void setToggled(boolean toggled) {
        mIsToggled = toggled;
        animate(ANI_STYLE_LIGHT_DOWN);
    }

    public void toggle() {
        setToggled(!isToggled());
    }

    public boolean isToggled() {
        return isToggleButton() && mIsToggled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        super.setClickable(enabled);
        invalidate();
    }

    @Override
    public boolean performClick() {
        if(isToggleButton()) toggle();
        else setSelectedIndex(mSelectedIndex + 1);
        return super.performClick();
    }




    private boolean mTouchIsInBounds = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isClickable()) return true;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

                animate(ANI_STYLE_LIGHT_UP);
                mTouchIsInBounds = true;
                return true;

            case MotionEvent.ACTION_MOVE:

                boolean isInBounds = mViewBounds.contains(mViewBounds.left + (int) event.getX(),
                        mViewBounds.top + (int) event.getY());

                boolean justLeft = mTouchIsInBounds && !isInBounds;
                boolean justEntered = !mTouchIsInBounds && isInBounds;

                mTouchIsInBounds = isInBounds;

                if (justLeft) animate(ANI_STYLE_LIGHT_DOWN);
                if (justEntered) animate(ANI_STYLE_LIGHT_UP);

                return true;

            case MotionEvent.ACTION_UP:
                if (mTouchIsInBounds) {
                    performClick();
                    animate(ANI_STYLE_LIGHT_DOWN);
                    return true;
                }

                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }

        animate(ANI_STYLE_LIGHT_DOWN);

        return super.onTouchEvent(event);
    }
}
