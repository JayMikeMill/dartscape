package com.gworks.dartscape.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import androidx.annotation.NonNull;

import com.gworks.dartscape.R;
import com.gworks.dartscape.ui.MultiView;
import com.gworks.dartscape.util.Helper;

public class AboutDialog extends android.app.Dialog {
    private MultiView mBtnConfirm;


    public AboutDialog(@NonNull Context context) {
        super(context, R.style.DialogBordered);

        initWindow();
        initControls();
    }

    private void initWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_about);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


    }

    private void initControls() {
        mBtnConfirm = findViewById(R.id.btnAboutDialogOk);
        mBtnConfirm.setOnClickListener(v -> {  dismiss(); });


    }

    @SuppressLint("SetTextI18n")
    @Override
    public void show() {
        getWindow().setLayout(Helper.getWindowWidth()
                        - (Helper.getWindowWidth() / 12),
                (int) (Helper.getWindowHeight() * 0.6f));

        super.show();
    }



}
