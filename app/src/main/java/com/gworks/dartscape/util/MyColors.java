package com.gworks.dartscape.util;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

public class MyColors {
    public static final int GREEN_LIGHT         = 0xFF44FF44;
    public static final int BLUE_LIGHT          = 0xFF66AAFF;
    public static final int ORANGE_LIGHT        = 0xFFFFBB00;
    public static final int RED_LIGHT           = 0xFFFF8888;
    public static final int RED                 = 0xFFFF0000;
    public static final int COLOR_SINGLE        = GREEN_LIGHT;
    public static final int COLOR_DOUBLE        = BLUE_LIGHT;
    public static final int COLOR_TRIPLE        = ORANGE_LIGHT;

    private static int THEME_TEXT;
    private static int THEME_TEXT_DISABLED;
    private static int THEME_SHADOW;
    private static int THEME_PRIMARY;
    private static int THEME_ACCENT;
    private static int THEME_TITLE;
    private static int THEME_HEADER;
    private static int THEME_OUTLINE;
    private static int THEME_HIGHLIGHT;
    private static int THEME_CONTROL;
    private static int THEME_DISABLED;
    public static void init(Context context) {
        TypedValue typedValue = new TypedValue();

        context.getTheme().resolveAttribute(com.google.android.material.R.attr.titleTextColor, typedValue, true);
        THEME_TEXT = ContextCompat.getColor(context, typedValue.resourceId);

        THEME_TEXT_DISABLED = ColorUtils.blendARGB(THEME_TEXT, Color.GRAY, 0.5f);

        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryDark, typedValue, true);

        THEME_SHADOW = ContextCompat.getColor(context, typedValue.resourceId);
        if(!UserPrefs.getShowShadows()) THEME_SHADOW = Color.TRANSPARENT;

        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        THEME_PRIMARY = ContextCompat.getColor(context, typedValue.resourceId);

        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorAccent, typedValue, true);
        THEME_ACCENT = ContextCompat.getColor(context, typedValue.resourceId);

        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, typedValue, true);
        THEME_TITLE = ContextCompat.getColor(context, typedValue.resourceId);

        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
        THEME_HEADER = ContextCompat.getColor(context, typedValue.resourceId);

        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorButtonNormal, typedValue, true);
        THEME_CONTROL = ContextCompat.getColor(context, typedValue.resourceId);


        THEME_HIGHLIGHT = ColorUtils.setAlphaComponent(THEME_TEXT, 60);

        THEME_DISABLED = ColorUtils.blendARGB(THEME_TEXT, Color.GRAY, 0.5f);
        THEME_DISABLED = ColorUtils.setAlphaComponent(THEME_DISABLED, 127);
        THEME_OUTLINE = THEME_PRIMARY;

       // THEME_OUTLINE_SIZE = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
    }

    public static int themeText() {
        return THEME_TEXT;
    }

    public static int themeTextDisabled() {
        return THEME_TEXT_DISABLED;
    }

    public static int themeShadow() {
        return THEME_SHADOW;
    }

    public static int themePrimary() {
        return THEME_PRIMARY;
    }
    public static int themeAccent() {
        return THEME_ACCENT;
    }
    public static int themeTitle() {
        return THEME_TITLE;
    }
    public static int themeHeader() {
        return THEME_HEADER;
    }
    public static int themeHighlight() {
        return THEME_HIGHLIGHT;
    }
    public static int themeControl() {
        return THEME_CONTROL;
    }
    public static int themeOutline() {
        return THEME_OUTLINE;
    }
    public static int themeDisabled() {
        return THEME_DISABLED;
    }
}
