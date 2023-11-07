package com.gworks.dartscape.ui;

import static com.gworks.dartscape.util.Themes.THEME_IDS;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import com.gworks.dartscape.R;
import com.gworks.dartscape.util.Helper;
import com.gworks.dartscape.util.Themes;
import com.gworks.dartscape.util.UserPrefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ThemeSpinner extends AppCompatSpinner {
    ThemeSpinnerAdapter mAdapter;
    private OnItemSelectedListener mTempListener;
    public ThemeSpinner(@NonNull Context context) {
        super(context);
    }

    public ThemeSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemeSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSelection(int position, boolean animate, boolean noTrigger) {
        //mPreviousSelection = getSelectedItemPosition();
        if(noTrigger) super.setOnItemSelectedListener(null);
        super.setSelection(position);


        if(noTrigger) {
            this.post(() -> super.setOnItemSelectedListener(mTempListener)); }
    }

    @Override
    public void setSelection(int position) {
        //mPreviousSelection = getSelectedItemPosition();
        super.setSelection(position);
    }

    @Override
    public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
        super.setOnItemSelectedListener(listener);
        mTempListener = listener;
    }

    public void init(float textSize) {
            // get player names from preferences
            mAdapter = new ThemeSpinnerAdapter(getContext(),
                    R.layout.item_spinner,
                    R.id.tvCustomSpinText, textSize, 0, Themes.getThemeNames());

            setAdapter(mAdapter);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setDropDownWidth(w);

        if(mAdapter != null) {
            mAdapter.onSizeChange(w, h);
            setDropDownVerticalOffset(-(getHeight() * (mAdapter.getCount() / 2) - (getHeight()/2)));
        }
    }

    public static final int THEME_COUNT = THEME_IDS.length;
    public static class ThemeSpinnerAdapter extends SuperSpinner.SuperSpinAdapter {

        private final int[] mThemePrimeColors = new int[THEME_COUNT];
        private final int[] mThemeTextColors = new int[THEME_COUNT];
        private final int[] mThemeShadowColors = new int[THEME_COUNT];
        private final int[] mThemeButtonColors = new int[THEME_COUNT];
        private final int[] mThemeAccentColors = new int[THEME_COUNT];
        private final GradientDrawable[] mThemeButtonBg = new GradientDrawable[THEME_COUNT];

        public ThemeSpinnerAdapter(Context context, int resourceId, int textviewId, float textSize, int iconId, List<String> list) {
            super(context, resourceId, textviewId, textSize, iconId, list);
            initThemeColors();
        }

        private void initThemeColors() {

            for (int i = 0; i < THEME_COUNT; i++) {
                Resources.Theme theme = getContext().getResources().newTheme();

                theme.applyStyle(THEME_IDS[i], true);

                TypedValue typedValue = new TypedValue();
                theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
                mThemePrimeColors[i] = ContextCompat.getColor(getContext(), typedValue.resourceId);
                theme.resolveAttribute(com.google.android.material.R.attr.titleTextColor, typedValue, true);
                mThemeTextColors[i]  = ContextCompat.getColor(getContext(), typedValue.resourceId);
                theme.resolveAttribute(com.google.android.material.R.attr.colorPrimaryDark, typedValue, true);
                mThemeShadowColors[i] = ContextCompat.getColor(getContext(), typedValue.resourceId);
                theme.resolveAttribute(com.google.android.material.R.attr.colorButtonNormal, typedValue, true);
                mThemeButtonColors[i] = ContextCompat.getColor(getContext(), typedValue.resourceId);
                theme.resolveAttribute(com.google.android.material.R.attr.colorAccent, typedValue, true);
                mThemeAccentColors[i] = ContextCompat.getColor(getContext(), typedValue.resourceId);

                mThemeButtonBg[i] = (GradientDrawable) Objects.requireNonNull(AppCompatResources
                        .getDrawable(getContext(), R.drawable.drw_control_bg)).mutate();

                mThemeButtonBg[i].setColors(new int[] { mThemePrimeColors[i], mThemePrimeColors[i] });
                mThemeButtonBg[i].setStroke(Helper.dpToPx(2), mThemeAccentColors[i]);
            }
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return setViewTheme(super.getDropDownView(position, convertView, parent), position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return setViewTheme(super.getView(position, convertView, parent), position);
        }

        private View setViewTheme(View view, int position) {
            TextView textView = view.findViewById(R.id.tvCustomSpinText);

            view.setBackground(mThemeButtonBg[position]);

            textView.setBackgroundColor(mThemeButtonColors[position]);
            textView.setTextColor(mThemeTextColors[position]);
            textView.setShadowLayer(textView.getShadowRadius(),
                    textView.getShadowDx(),textView.getShadowDy(), mThemeShadowColors[position]);

            return view;
        }
    }
}
