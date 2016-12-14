package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.tosslab.jandi.app.R;

/**
 * Created by tee on 2016. 12. 13..
 */

public class ProgressWheelForInvitation extends ProgressDialog {

    private Activity activity;

    public ProgressWheelForInvitation(Activity activity) {
        super(activity, R.style.Jandi_Transparent_Dialog);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_wheel_for_invitation);
        setIndeterminate(true);
        setCancelable(true);
    }

    @Override
    public void show() {
        if (isFinished()) {
            return;
        }
        super.show();
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
