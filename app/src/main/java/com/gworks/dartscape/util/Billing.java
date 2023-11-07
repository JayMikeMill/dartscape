package com.gworks.dartscape.util;

import static com.android.billingclient.api.BillingClient.*;
import static com.android.billingclient.api.BillingFlowParams.*;
import static com.android.billingclient.api.QueryProductDetailsParams.*;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Billing implements
        BillingClientStateListener, ProductDetailsResponseListener,
        PurchasesResponseListener, PurchasesUpdatedListener,
        AcknowledgePurchaseResponseListener, ConsumeResponseListener {

    public static final String PRODUCT_DARTSCAPE_PRO      = "dartscape_pro";
    public static final String PRODUCT_DARTSCAPE_PRO_TEST = "dartscape_pro_test";

    public static final List<String> PRODUCT_LIST = Arrays.asList(PRODUCT_DARTSCAPE_PRO, PRODUCT_DARTSCAPE_PRO_TEST);
    List<Product> mProductList;
    List<ProductDetails> mProductDetails;
    OnBillingEventListener mOnBillingEventListener;


    private final BillingClient mBillingClient;

    Activity mActivity;

    //
    // only sell the test product in inhouse flavor
    //
    @SuppressWarnings("ConstantConditions")
    private static final String PURCHASING_PRODUCT =
            Helper.IS_RELEASE_BUILD ?
                    PRODUCT_DARTSCAPE_PRO : PRODUCT_DARTSCAPE_PRO_TEST;

    public Billing(AppCompatActivity activity, OnBillingEventListener listener) {
        mActivity = activity;
        mOnBillingEventListener = listener;

        initProductList();

        // init billing client
        mBillingClient = newBuilder(mActivity)
                .setListener(this)
                .enablePendingPurchases()
                .build();
    }

    private void initProductList() {
        mProductList = new ArrayList<>();
        for(String productId : PRODUCT_LIST) {
            mProductList.add(Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(ProductType.INAPP)
                            .build());
        }
    }

    public void connectAndCheck() {
        mBillingClient.startConnection(this);
    }

    public ProductDetails getProductDetailsFromId(String productId) {
        for(ProductDetails productDetails : mProductDetails) {
            if (productDetails.getProductId().equals(productId)) {
                return productDetails;
            }
        }

        return null;
    }

    public boolean launchPurchaseFlow() {
        if(mProductDetails == null || mProductDetails.isEmpty()) return false;

        ProductDetails productDetails =
                getProductDetailsFromId(PURCHASING_PRODUCT);

        ImmutableList<ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails).build());

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        // Launch the billing flow
        BillingResult billingResult = mBillingClient.launchBillingFlow(mActivity, billingFlowParams);

        if(billingResult.getResponseCode() != BillingResponseCode.OK) {
            retryBillingServiceConnection();
            return false;
        }

        return true;
    }

    public void validatePurchase(Purchase purchase, boolean newPurchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.
            // make sure purchase is acknowledged
            if (!purchase.isAcknowledged()) {
                mBillingClient.acknowledgePurchase( AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken()).build(), this);
            }

            // iterate through product
            for(String productId : purchase.getProducts()) {
                if (productId.equals(PRODUCT_DARTSCAPE_PRO)) {
                    mOnBillingEventListener.onPurchasedPro(newPurchase);
                } else if (productId.equals(PRODUCT_DARTSCAPE_PRO_TEST)) {
                    mOnBillingEventListener.onPurchasedPro(newPurchase);
                }
            }
        } else {
            mOnBillingEventListener.onHasNotPurchasedPro();
        }
    }

    /** for testing purposes only. no consumables in this app */
    private void consumePurchase(Purchase purchase) {
        mBillingClient.consumeAsync(ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken()).build(), this);
    }

    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {
        if (billingResult.getResponseCode() ==  BillingResponseCode.OK) {
            // get products
            if(mProductDetails == null) {
                QueryProductDetailsParams queryProductDetailsParams =
                        QueryProductDetailsParams.newBuilder().setProductList
                                (mProductList).build();

                mBillingClient.queryProductDetailsAsync(queryProductDetailsParams, this);
            }

            // check for purchases
            mBillingClient.queryPurchasesAsync
                    (QueryPurchasesParams.newBuilder()
                            .setProductType(ProductType.INAPP).build(), this);
        } else {
            retryBillingServiceConnection();
        }
    }

    public String getProPrice() {
        if(mProductDetails == null || mProductDetails.isEmpty()) return "-.--";
        return getProductDetailsFromId(PURCHASING_PRODUCT).getOneTimePurchaseOfferDetails().getFormattedPrice();
    }

    @Override
    public void onProductDetailsResponse(@NonNull BillingResult billingResult,
                                         @NonNull List<ProductDetails> productDetailsList) {
        if (billingResult.getResponseCode() ==  BillingResponseCode.OK) {
            mProductDetails = productDetailsList;
        } else {
            retryBillingServiceConnection();
        }
    }

    @Override
    public void onQueryPurchasesResponse(BillingResult billingResult, @NonNull List<Purchase> purchases) {
        if (billingResult.getResponseCode() ==  BillingResponseCode.OK) {
            if(purchases.isEmpty()) {
                mOnBillingEventListener.onHasNotPurchasedPro();
            } else {
                for(Purchase purchase : purchases) {
                    validatePurchase(purchase, false);
                }
            }
        } else {
            retryBillingServiceConnection();
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingResponseCode.OK && purchases != null) {
            for(Purchase purchase : purchases) {
                validatePurchase(purchase, true);
            }
        }
    }

    @Override
    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
    }

    @Override
    public void onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
        retryBillingServiceConnection();
    }

    private static long RECONNECT_MILLISECONDS = 1000;
    private void retryBillingServiceConnection() {
        new Handler(Looper.myLooper()).postDelayed(() ->
                mBillingClient.startConnection(this), RECONNECT_MILLISECONDS);
        RECONNECT_MILLISECONDS = RECONNECT_MILLISECONDS * 2;
    }

    @Override
    public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
    }

    public interface OnBillingEventListener {
        void onHasNotPurchasedPro();
        void onPurchasedPro(boolean newPurchase);
    }
}
