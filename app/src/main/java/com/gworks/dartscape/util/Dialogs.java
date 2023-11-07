package com.gworks.dartscape.util;

import static android.content.DialogInterface.*;

import android.content.Context;

import com.gworks.dartscape.R;
import com.gworks.dartscape.dialogs.AboutDialog;
import com.gworks.dartscape.dialogs.BuyProDialog;
import com.gworks.dartscape.dialogs.ConfirmDialog;
import com.gworks.dartscape.dialogs.ReviewDialog;

//
// dialog helper class
//
public class Dialogs {
    private ConfirmDialog mDiagConfirm;
    private BuyProDialog mDiagBuyPro;
    private AboutDialog mDiagAbout;
    private ReviewDialog mDiagRating;

    private final Context mContext;

    Dialogs(Context context) {
        mContext = context;
        init(context);
    }

    private void init(Context context) {

        mDiagConfirm = new ConfirmDialog(context);
        mDiagBuyPro  = new BuyProDialog(context);
        mDiagAbout  = new AboutDialog(context);
        mDiagRating = new ReviewDialog(context);

    }

    public void showBuyPro(OnDismissListener listener) {
        mDiagBuyPro.setOnDismissListener(listener);
        mDiagBuyPro.show();
    }

    public void showConfirm(String text, String confirm) {
        showConfirm(mContext.getString(R.string.confirm), text, confirm, true, null);
    }

    public void showConfirm(String title, String text, String confirm) {
        showConfirm(title, text, confirm, true, null);
    }

    public void showConfirm(String title, int iconId, String text) {
        showConfirm(title, iconId, text, mContext.getString(R.string.ok));
    }
    public void showConfirm(String title, int iconId, String text, String confirm) {
        showConfirm(title, iconId, text, confirm, mContext.getString(R.string.no), true, null);
    }

    public void showConfirm(String title, String text, String confirm, boolean oneButton) {
        showConfirm(title, text, confirm, oneButton, null);
    }

    public void showConfirm(String title, String text, String confirm, boolean oneButton,
                            OnDismissListener dismissListener) {
        showConfirm(title,  R.drawable.ic_help, text, confirm, oneButton,  dismissListener);
    }

    public void showConfirm(String title, int iconId, String text, String confirm, boolean oneButton,
                            OnDismissListener dismissListener) {
        showConfirm(title,  iconId, text, confirm, mContext.getString(R.string.no), oneButton,  dismissListener);
    }

    public void showConfirm(String title, int iconId, String text, String confirm, String cancel, boolean oneButton,
                            OnDismissListener dismissListener) {
        mDiagConfirm.setConfirmStyle(title, iconId, text, confirm, cancel, oneButton);
        mDiagConfirm.setOnDismissListener(dismissListener);
        mDiagConfirm.show();
    }
    public void showRating() {
        mDiagRating.show();
    }

    public void showAbout() {
        mDiagAbout.show();
    }

}
