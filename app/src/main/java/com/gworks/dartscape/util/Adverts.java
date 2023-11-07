package com.gworks.dartscape.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.gworks.dartscape.R;
import com.gworks.dartscape.main.DartScapeActivity;

public class Adverts {
    private static final String REAL_AD_ID = "ca-app-pub-1136824463148589/7630371176";
    private static final String TEST_AD_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final String AD_ID = Helper.IS_RELEASE_BUILD ?
                                        REAL_AD_ID : TEST_AD_ID;

    AdView mAdView;

    Adverts(DartScapeActivity activity) {
        init(activity);
    }

    public void setAdsActive(boolean active) {
        if(mAdView == null) return;
        mAdView.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    private void init(DartScapeActivity activity) {

        MobileAds.initialize(activity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        LinearLayout lScreen = activity.findViewById(R.id.lScreen);

        mAdView = new AdView(activity);
        mAdView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        lScreen.addView(mAdView);

        mAdView.setAdSize(getAdSize(activity));
        mAdView.setAdUnitId(AD_ID);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private AdSize getAdSize(DartScapeActivity activity) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();

        float widthPixels = metrics.widthPixels;
        float density = metrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }
}
