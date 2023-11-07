package com.gworks.dartscape.dialogs;

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

public class ConfirmDialog extends android.app.Dialog {
    private TextView mTvTitle;
    private ImageView mIvIcon;
    private TextView mTvText;
    private MultiView mBtnConfirm;
    private MultiView mBtnCancel;
    private boolean mDidConfirm;

    public ConfirmDialog(@NonNull Context context) {

        super(context, R.style.DialogBordered);

        initWindow();
        initControls();

        setConfirmStyle("", R.drawable.ic_help, "", "", "", false);
    }

    private void initWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_confirm);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    private void initControls() {
        mTvTitle = findViewById(R.id.tvDiagConfirmTitle);
        mIvIcon = findViewById(R.id.ivDiagConfirmIcon);

        mTvText = findViewById(R.id.tvDiagConfirmText);

        mBtnConfirm = findViewById(R.id.btnDiagConfirmConfirm);
        mBtnConfirm.setOnClickListener(v -> { mDidConfirm = true; dismiss(); });

        mBtnCancel = findViewById(R.id.btnDiagConfirmCancel);
        mBtnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void show() {
        getWindow().setLayout(Helper.getWindowWidth()
                        - (Helper.getWindowWidth() / 12),
                (int) (Helper.getWindowHeight() * 0.35f));

        mDidConfirm = false;

        super.show();
    }

    public void setConfirmStyle(String title, int iconId, String text, String confirmText, String cancelText, boolean oneButton) {
        mTvTitle.setText(title);
        mIvIcon.setImageDrawable(AppCompatResources.getDrawable(getContext(), iconId));
        mTvText.setText(text);

        mBtnConfirm.setText(confirmText);
        mBtnCancel.setText(cancelText);
        mBtnCancel.setVisibility(oneButton ? View.GONE : View.VISIBLE);

    }

    public boolean didConfirm() {
        return mDidConfirm;
    }
}
