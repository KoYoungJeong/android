package com.tosslab.jandi.app.utils;

import android.content.res.Resources;

import com.tosslab.jandi.app.JandiApplication;

/**
 * Created by tonyjs on 16. 1. 28..
 */
public class UiUtils {
    public static float getPixelFromDp(float dp) {
        Resources resources = JandiApplication.getContext().getResources();
        return resources.getDisplayMetrics().density * dp;
    }
}
