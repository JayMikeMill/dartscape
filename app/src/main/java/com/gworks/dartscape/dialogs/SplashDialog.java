package com.gworks.dartscape.dialogs;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gworks.dartscape.R;
import com.gworks.dartscape.util.Helper;

/*
    PLAYERS DIALOG
 */
public class SplashDialog extends android.app.Dialog {

    ConstraintLayout lSplashScreen;
    ImageView mIvLoading;
    Animation mAniFadeIn, mAniLoading;

    public SplashDialog(@NonNull Context context) {

        super(context, R.style.SplashDialog);

        initWindow();
        initControls();
    }

    private void initWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_splash);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
    }

    private void initControls() {
        lSplashScreen = findViewById(R.id.lSplashScreen);
        mIvLoading =  findViewById(R.id.ivSplashLoading);

        //mDartBoard.setVisibility(View.INVISIBLE);
        mAniFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.long_fade_in);
        mAniFadeIn.setFillAfter(true);

        mAniLoading = AnimationUtils.loadAnimation(getContext(), R.anim.splash_dartboard);
       // mAniLoading.setRepeatMode(Animation.INFINITE);
    }

    @Override
    public void show() {
        super.show();
        lSplashScreen.startAnimation(mAniFadeIn);
        mIvLoading.startAnimation(mAniLoading);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onBackPressed() {

    }
}
