package com.gworks.dartscape.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.gworks.dartscape.R;
import com.gworks.dartscape.ui.MultiView;
import com.gworks.dartscape.util.Helper;

public class BuyProDialog extends android.app.Dialog {
    private MultiView mBtnConfirm;
    private MultiView mBtnCancel;
    private boolean mDidConfirm;

    public BuyProDialog(@NonNull Context context) {
        super(context, R.style.DialogBordered);

        initWindow();
        initControls();
    }

    private void initWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_buy_pro);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    private void initControls() {
        mBtnConfirm = findViewById(R.id.btnDiagBuyProConfirm);
        mBtnConfirm.setOnClickListener(v -> { mDidConfirm = true; dismiss(); });

        mBtnCancel = findViewById(R.id.btnDiagBuyProCancel);
        mBtnCancel.setOnClickListener(v -> dismiss());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void show() {
        mBtnConfirm.setText(getContext().getString(R.string.buy_now) + "\n(" + Helper.activation().getProPriceString() + ")");
        getWindow().setLayout(Helper.getWindowWidth()
                        - (Helper.getWindowWidth() / 12),
                (int) (Helper.getWindowHeight() * 0.7f));

        mDidConfirm = false;

        super.show();
    }


    public boolean didConfirm() {
        return mDidConfirm;
    }
}
