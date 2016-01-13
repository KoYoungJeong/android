package com.tosslab.jandi.app.utils.progresswheel;

import android.app.Activity;

import com.tosslab.jandi.app.utils.ProgressWheel;

/**
 * Created by tee on 16. 1. 13..
 */
public class ProgressWheelUtil {

    private ProgressWheel progressWheel;

    private ProgressWheelUtil() {
    }

    public static ProgressWheelUtil makeInstance() {
        return new ProgressWheelUtil();
    }

    public void init(Activity activity) {
        progressWheel = new ProgressWheel(activity);
    }

    public void showProgressWheel(Activity activity) {
        if (progressWheel == null) {
            init(activity);
        }
        activity.runOnUiThread(() -> {
            dismissProgressWheel(activity);

            if (!progressWheel.isShowing()) {
                progressWheel.show();
            }
        });
    }

    public void dismissProgressWheel(Activity activity) {
        if (progressWheel == null) {
            init(activity);
        }
        activity.runOnUiThread(() -> {
            if (progressWheel != null && progressWheel.isShowing()) {
                progressWheel.dismiss();
            }
        });
    }

}
