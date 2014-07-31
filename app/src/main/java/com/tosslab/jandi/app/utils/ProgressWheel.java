package com.tosslab.jandi.app.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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
        setCancelable(false);
    }

    @Override
    public void show() {
        super.show();
        setContentView(R.layout.progress_wheel);
    }

    public static ProgressWheel getInstance(Context context) {
        if (__instance__ == null) {
            __instance__ = new ProgressWheel(context);
            __instance__.init();
        }
        return __instance__;
    }
}
