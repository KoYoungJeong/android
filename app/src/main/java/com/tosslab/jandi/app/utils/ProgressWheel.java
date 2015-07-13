package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.tosslab.jandi.app.R;

import java.lang.ref.WeakReference;

/**
 * Created by justinygchoi on 2014. 2. 15..
 */
public class ProgressWheel extends ProgressDialog {
    private Activity activity;

    public ProgressWheel(Activity activity) {
        super(activity, R.style.Jandi_Transparent);
        this.activity = activity;
        setIndeterminate(true);
        setCancelable(true);
    }

    @Override
    public void show() {
        if (isFinished()) {
            return;
        }
        super.show();
        setContentView(R.layout.progress_wheel);
    }

    @Override
    public void dismiss() {
        if (isFinished()) {
            return;
        }
        super.dismiss();
    }

    public boolean isFinished() {
        return activity == null || activity.isFinishing();
    }
}