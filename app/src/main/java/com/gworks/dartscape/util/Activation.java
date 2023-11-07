package com.gworks.dartscape.util;

import com.gworks.dartscape.R;
import com.gworks.dartscape.dialogs.BuyProDialog;
import com.gworks.dartscape.fragments.FragManager;
import com.gworks.dartscape.fragments.SettingsFragment;
import com.gworks.dartscape.main.DartScapeActivity;

public class Activation implements Billing.OnBillingEventListener {

    DartScapeActivity mActivity;
    Billing mBilling;

    private boolean mFinishedInitialProCheck = false;

    public Activation(DartScapeActivity activity) {
        mActivity = activity;
        mBilling = new Billing(mActivity, this);
    }

    public void checkForProVersion() {
        if(mFinishedInitialProCheck) return;
        mBilling.connectAndCheck();
    }

    public void showBuyProDialog() {
        Helper.dialogs().showBuyPro(dialog -> {
            if(((BuyProDialog)dialog).didConfirm()) {
                if (!mBilling.launchPurchaseFlow()) {
                    Helper.dialogs().showConfirm(mActivity.getString(R.string.error), mActivity.getString(R.string.dialog_buy_pro_error),
                            mActivity.getString(R.string.ok));
                    mBilling.connectAndCheck();
                }
            }
        });
    }

    private void setProActive(boolean active) {
        UserPrefs.setOwnsPro(active);

        ((SettingsFragment) mActivity.frags().getFragFromId
                (FragManager.FragId.FRAG_SETTINGS)).setProUiActive(active);
    }

    @Override
    public void onHasNotPurchasedPro() {
        mFinishedInitialProCheck = true;
        setProActive(false);
    }

    @Override
    public void onPurchasedPro(boolean newActivation) {
        mFinishedInitialProCheck = true;
        setProActive(true);


        if(newActivation) {
            // double check on new activation to make sure ui is unlocked correctly
            checkForProVersion();

            // show user a thank you dialog
            Helper.dialogs().showConfirm(mActivity.getString(R.string.thank_you) + "!", R.drawable.ic_happy_face,
                    mActivity.getString(R.string.dialog_purchased_pro), mActivity.getString(R.string.ok));
        }
    }

    public String getProPriceString() {
        return mBilling.getProPrice();
    }
}
