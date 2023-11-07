package com.gworks.dartscape.ui;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.gworks.dartscape.R;
import com.gworks.dartscape.util.MyColors;

/**
 * an effect dialog to show score after a hit
 */
public class TextPopup extends PopupWindow {
    MultiView mMvText;
    Handler mHandler;

    @SuppressLint("InflateParams")
    public TextPopup(LayoutInflater inflater) {
        super(inflater.inflate(R.layout.popup_text, null));
        setTouchable(false);


        mHandler = new Handler(Looper.myLooper());
        mMvText = getContentView().findViewById(R.id.tvPopupText);

    }

    /**
     * places the score right above the view
     */
    public void popup(String text, int color, View view, float size) {
        popup(text, color, view, size, 1f, false);
    }

    public void popup(String text, int color, View view, float size, float textRat, boolean bounce) {

        if (bounce) {
            setAnimationStyle(R.style.PopupScoreBounce);
        } else {
            setAnimationStyle(R.style.PopupScoreAnimation);
        }
        mMvText.setMultiText(text);
        mMvText.setTextColor(color);
        mMvText.setTextRatio(textRat);
       // mMvText.setBackgroundDrawable(AppCompatResources.getDrawable(getContentView().getContext(), R.drawable.draw_dialog_bg));

        if (view != null) {

            setWidth(view.getWidth());
            setHeight((int)(view.getHeight()*size));
            int[] newLoc = new int[2];
            view.getLocationInWindow(newLoc);

            if (!bounce) {
                newLoc[1] = newLoc[1] - (int)(getHeight()*size);
            } else {
                //newLoc[1] = newLoc[1] - (int)(getHeight()*size);
            }

            showAtLocation(view, Gravity.NO_GRAVITY, newLoc[0], newLoc[1]);
        }
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (!isShowing())
            super.showAtLocation(parent, gravity, x, y);

        mHandler.postDelayed(this::dismiss, 1400);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mHandler.removeCallbacksAndMessages(null);
        mMvText.setVisibility(View.INVISIBLE);
    }
}
