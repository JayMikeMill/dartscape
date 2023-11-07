package com.gworks.dartscape.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;

import com.gworks.dartscape.BuildConfig;
import com.gworks.dartscape.main.DartScapeActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


//
// helper functions
//

public class Helper {
    // To prevent someone from accidentally instantiating the helper,
    // make the constructor private.
    private static Dialogs mDialog;
    private static Activation mActivation;
    private static Adverts mAds;

    @SuppressWarnings("ConstantConditions")
    public static final boolean IS_RELEASE_BUILD
            = BuildConfig.BUILD_TYPE.equals("release");

    private Helper() {}

    private static DisplayMetrics mDispMetrics;
    private static Window mWindow;


    public static void init(DartScapeActivity activity) {
        setWindowMetrics(activity.getResources().getDisplayMetrics());

        mWindow = activity.getWindow();
        mDialog = new Dialogs(activity);
    }

    public static void initActivation(DartScapeActivity activity) {
        mActivation = new Activation(activity);
    }

    public static void initAds(DartScapeActivity activity) {
        mAds = new Adverts(activity);
    }

    public static void setAdsActive(boolean active) {
        mAds.setAdsActive(active);
    }

    public static Dialogs dialogs() { return mDialog; }

    public static Activation activation() { return mActivation; }

    public static void setWindowMetrics(DisplayMetrics metrics) {
        mDispMetrics = metrics;
    }

    public static int getScreenWidth() { return mDispMetrics.widthPixels; }
    public static int getScreenHeight() { return mDispMetrics.heightPixels; }

    @SuppressWarnings("deprecation")
    public static int getWindowWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        //mWindow.getContext().ged
        mWindow.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    @SuppressWarnings("deprecation")
    public static int getWindowHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        mWindow.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static float getWindowRatio() {
        return (float) getScreenHeight() / getScreenWidth();
    }

    public static float getWindowDensity() {
        return (float) mDispMetrics.density;
    }

    public static void showKeyboard(View view){
        showKeyboard(view.getContext());
    }

    @SuppressWarnings("deprecation")
    public static void showKeyboard(Context context) {
        setFullScreen(false);
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static void showKeyboardForced(){
        setFullScreen(false);
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static void closeKeyboard(View view){
        setFullScreen(true);
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void closeKeyboard(){
        setFullScreen(true);
        mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @SuppressWarnings("deprecation")
    public static void setFullScreen(boolean fullScreen) {
        if (fullScreen) mWindow.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else mWindow.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static float pxToSp(float px) {
        return px / mDispMetrics.scaledDensity;
    }
    public static float spToPx(float sp) {
        return  mDispMetrics.scaledDensity * sp;
    }

    public static int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mDispMetrics);
    }

    public static float pxToScreenPx(int px) {
        float STROKE_WIDTH = mDispMetrics.widthPixels * 0.0035f;
        float Y_POSITION   = mDispMetrics.heightPixels * 0.01f;

        return STROKE_WIDTH*px;
    }
    public static Integer[] arrayIntToInteger(int[] intAry) {
        Integer[] integerAry = new Integer[intAry.length];
        for(int i = 0; i < intAry.length; i++)
            integerAry[i] = intAry[i];
        return  integerAry;
    }

    public static int[] arrayIntegerToInt(Integer[] integerAry) {
        int[] intAry = new int[integerAry.length];
        for(int i = 0; i < intAry.length; i++)
            intAry[i] = integerAry[i];
        return  intAry;
    }

    public static boolean inRange(int value, int min, int max) {
        return (value>= min) && (value<= max);
    }

    @SuppressLint("DiscouragedApi")
    public static int getResIdByName(Context context, String name) {
        return context.getResources().getIdentifier
                (name, "id", context.getPackageName());
    }

    public static void setTextViewAutoSize(TextView tv, boolean auto, int maxSize) {
        if (!auto) TextViewCompat.setAutoSizeTextTypeWithDefaults(tv, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE);
        else TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(tv, 10, Math.max(21, maxSize), 5, TypedValue.COMPLEX_UNIT_PX);
    }
    public static int changeColorHSV(int color, float hue, float saturation, float brightness) {
        // get color values
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        if (hue != -1) hsv[0] = hsv[0] + ((hue * 360) % 360);
        if (saturation != -1) hsv[1] = hsv[1] * saturation;
        if (brightness != -1) hsv[2] = hsv[2] * brightness;

        // change them
        return Color.HSVToColor(hsv);
    }

    public static int[] intToARGB(int color) {
        int[] argb = new int[4];
        argb[0] = Color.alpha(color);
        argb[1] = Color.red(color);
        argb[2] = Color.green(color);
        argb[3] = Color.blue(color);

        return argb;
    }

    public static int addAlphaToColor(int color, int transparency) {
       return ColorUtils.setAlphaComponent(color, transparency);
    }

    public static String getCurrentTimeStamp() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    public static String getTimeStamp(Date date) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    public static String getTimeStamp(int since) {
        return getTimeStamp
                (new Date(new Date().getTime() -
                        (long) since * 60 * 60 * 1000));
    }


    // Function to print difference in
    // time start_date and end_date
    public static void findDifference(String start_date, String end_date) {
        // SimpleDateFormat converts the
        // string format to date object
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat(
                "dd-MM-yyyy HH:mm:ss");

        // Try Block
        try {
            // parse method is used to parse the text from a string to
            // produce the date
            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);
            assert d1 != null;
            assert d2 != null;

            // Calculate time difference
            // in milliseconds
            long difference_In_Time = d2.getTime() - d1.getTime();

            // Calculate time difference in
            // seconds, minutes, hours, years,
            // and days
            long difference_In_Seconds = (difference_In_Time / 1000) % 60;
            long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;
            long difference_In_Hours = (difference_In_Time / (1000 * 60 * 60)) % 24;
            long difference_In_Years = (difference_In_Time / (1000L * 60 * 60 * 24 * 365));
            long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;

            // Print the date difference in years, in days, in hours, in
            // minutes, and in seconds

            System.out.print(
                    "Difference "
                            + "between two dates is: ");

            System.out.println(
                    difference_In_Years
                            + " years, "
                            + difference_In_Days
                            + " days, "
                            + difference_In_Hours
                            + " hours, "
                            + difference_In_Minutes
                            + " minutes, "
                            + difference_In_Seconds
                            + " seconds");
        }

        // Catch the Exception
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void moveChildView(LinearLayout parent, int indexFrom, int indexTo) {
        View v = parent.getChildAt(indexFrom);
        parent.removeViewAt(indexFrom);
        parent.addView(v, indexTo);
    }

    public static void relayoutChildren(View view) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(view.getMeasuredWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.getMeasuredHeight(), View.MeasureSpec.EXACTLY));
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    public static void setAllChildrenShadows(ViewGroup view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            final View child = view.getChildAt(i);
            if (child instanceof ViewGroup) {
                setAllChildrenShadows((ViewGroup) child);
            } else {
                if (child instanceof TextView)  {
                    setShadowColor((TextView) child);
                }
            }
        }
    }

    public static void setShadowColor(TextView tv) {
        tv.setShadowLayer(tv.getShadowRadius(), tv.getShadowDx(), tv.getShadowDy(), MyColors.themeShadow());
    }
}