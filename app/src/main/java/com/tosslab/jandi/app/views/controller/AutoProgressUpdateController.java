package com.tosslab.jandi.app.views.controller;

import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.ui.photo.widget.CircleProgressBar;

/**
 * Created by tonyjs on 16. 5. 13..
 */
public class AutoProgressUpdateController {

    public static final long DEFAULT_END_MILLIS = 6_000;
    public static final long DEFAULT_TICK_MILLIS = 120;

    private CountDownTimer countDownTimer;
    private CircleProgressBar progressBar;
    private TextView tvPercentage;

    public AutoProgressUpdateController() {
        this(DEFAULT_END_MILLIS);
    }

    public AutoProgressUpdateController(long endMillis) {
        this(DEFAULT_END_MILLIS, DEFAULT_TICK_MILLIS);
    }

    /**
     * ex) 120 millisecond(tickMillis) 에 한 번씩 progress 를 update 하는 것을
     * 6000 millisecond(endMillis) 동안 수행
     *
     * @param endMillis
     * @param tickMillis
     */
    public AutoProgressUpdateController(long endMillis, long tickMillis) {
        countDownTimer = new CountDownTimer(endMillis, tickMillis) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (progressBar == null || tvPercentage == null) {
                    return;
                }

                int gap = (int) (endMillis - millisUntilFinished);

                int percentage = (int) ((gap / (float) endMillis) * 100);
                percentage = Math.min(percentage, 99);
                progressBar.setProgress(percentage);
                tvPercentage.setText(percentage + "%");
            }

            @Override
            public void onFinish() {
                if (progressBar == null || tvPercentage == null) {
                    return;
                }

                progressBar.setProgress(99);
                tvPercentage.setText(99 + "%");
            }
        };
    }

    public void start() {
        if (countDownTimer != null) {
            countDownTimer.start();
        }
    }

    public void setProgressBar(CircleProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setPercentageTextView(TextView tvPercentage) {
        this.tvPercentage = tvPercentage;
    }

    public void cancel() {
        countDownTimer.cancel();
    }
}
