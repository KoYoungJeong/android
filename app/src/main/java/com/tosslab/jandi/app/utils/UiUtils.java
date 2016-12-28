package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.tosslab.jandi.app.JandiApplication;

/**
 * Created by tonyjs on 16. 1. 28..
 */
public class UiUtils {
    public static float getPixelFromDp(float dp) {
        Resources resources = JandiApplication.getContext().getResources();
        return resources.getDisplayMetrics().density * dp;
    }

    public static float getPixelFromSp(float sp) {
        Resources resources = JandiApplication.getContext().getResources();
        return resources.getDisplayMetrics().scaledDensity * sp;
    }

    public static Drawable getRippleEffectBackgroundDrawable() {
        Context context = JandiApplication.getContext();
        int selectableItemBackground = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? android.R.attr.selectableItemBackgroundBorderless
                : android.R.attr.selectableItemBackground;
        int[] attrs = new int[] {selectableItemBackground};

        TypedArray ta = context.obtainStyledAttributes(attrs);

        Drawable drawable = ta.getDrawable(0 /* index */);

        ta.recycle();
        return drawable;
    }

    public static int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        Context context = JandiApplication.getContext();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, displayMetrics);
        }
        return actionBarHeight;
    }

    public static int getStatusBarHeight() {
        int statusBarHeight = 0;
        Resources resources = JandiApplication.getContext().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public static int getSoftKeyHeight() {
        Resources resources = JandiApplication.getContext().getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public interface KeyboardHandler {
        void hideKeyboard();
    }
}
