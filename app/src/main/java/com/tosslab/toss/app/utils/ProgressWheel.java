package com.tosslab.toss.app.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.tosslab.toss.app.R;

/**
 * Created by justinygchoi on 2014. 2. 15..
 */
public class ProgressWheel extends Dialog {
//    private static ProgressWheel __instance__;
    Context mContext;

    public ProgressWheel(Context context) {
        super(context, R.style.ProgressWheel);
        mContext = context;
    }

    public void init() {
        addContentView(
                new ProgressBar(mContext),
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
    }

//    public static ProgressWheel getInstance(Context context) {
//        if (__instance__ == null) {
//            __instance__ = new ProgressWheel(context);
//            __instance__.init();
//        }
//        return __instance__;
//    }
}
