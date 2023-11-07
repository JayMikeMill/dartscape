package com.gworks.dartscape.util;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.gworks.dartscape.R;

//
// sound helper class
//
public class Font {
    // fonts
    public static Typeface BOOGALOO;
    public static Typeface LILITA;
    private Font() {
    }

    public static void init(Context context) {
        // initialize fonts
        BOOGALOO = ResourcesCompat.getFont(context, R.font.boogaloo);
        LILITA = ResourcesCompat.getFont(context, R.font.lilita_one);
    }
}
