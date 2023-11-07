package com.gworks.dartscape.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.RatingBar;

import androidx.annotation.NonNull;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.gworks.dartscape.R;
import com.gworks.dartscape.main.DartScapeActivity;
import com.gworks.dartscape.ui.MultiView;
import com.gworks.dartscape.util.Helper;

public class ReviewDialog extends android.app.Dialog {
    private MultiView mBtnLater;
    private RatingBar mRating;

    private DartScapeActivity mActivity;
    public ReviewDialog(@NonNull Context context) {
        super(context, R.style.DialogBordered);

        mActivity = (DartScapeActivity)context;
        initWindow();
        initControls();
    }

    private void initWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rating);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initControls() {
        mBtnLater = findViewById(R.id.btnRatingDialogLater);
        mBtnLater.setOnClickListener(v -> {  dismiss(); });

        mRating = findViewById(R.id.rbRatingDialog);
        mRating.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showReviewFlow();
            }
            return true;
        });

    }


    private void showReviewFlow() {
        ReviewManager manager = ReviewManagerFactory.create(getContext());
        Task<ReviewInfo> request = manager.requestReviewFlow();

        request.addOnCompleteListener(task -> {
            if (request.isSuccessful()) {
                // We got the ReviewInfo object
                ReviewInfo reviewInfo = request.getResult();
                Task<Void> flow = manager.launchReviewFlow(mActivity, reviewInfo);

                flow.addOnCompleteListener(task1 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            } else {
                // There was some problem, continue regardless of the result.
            }
        });

        dismiss();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void show() {
        getWindow().setLayout(Helper.getWindowWidth()
                        - (Helper.getWindowWidth() / 12),
                (int) (Helper.getWindowHeight() * 0.4f));

        super.show();
    }



}
