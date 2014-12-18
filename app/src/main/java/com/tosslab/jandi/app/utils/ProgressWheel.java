package com.tosslab.jandi.app.utils;

import android.app.ProgressDialog;
import android.content.Context;

import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 2014. 2. 15..
 */
public class ProgressWheel extends ProgressDialog {
    private static ProgressWheel __instance__;
    Context mContext;

    public ProgressWheel(Context context) {
        super(context);
        mContext = context;
    }

    public void init() {
        setIndeterminate(true);
        setCancelable(true);
    }

    @Override
    public void show() {
        super.show();
        setContentView(R.layout.progress_wheel);
    }
}
