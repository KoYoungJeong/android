package com.tosslab.jandi.app.ui.sticker;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by Steve SeongUg Jung on 15. 6. 3..
 */
@EBean
public class KeyboardHeightModel implements ViewTreeObserver.OnGlobalLayoutListener {

    public static final int MIN_KEYBOARD_HEIGHT = 150;
    @RootContext
    Activity activity;

    View rootView;
    private OnKeyboardCaptureListener onKeyboardHeightCapture;
    private OnKeyboardShowListener onKeyboardShowListener;
    private boolean isOpened;

    public OnKeyboardShowListener getOnKeyboardShowListener() {
        return onKeyboardShowListener;
    }

    public void setOnKeyboardShowListener(OnKeyboardShowListener onKeyboardShowListener) {
        this.onKeyboardShowListener = onKeyboardShowListener;
    }

    @AfterViews
    void initViews() {
        rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void setOnKeyboardHeightCaptureListener(OnKeyboardCaptureListener onKeyboardHeightCapture) {
        this.onKeyboardHeightCapture = onKeyboardHeightCapture;
    }

    private int getNavigationHeight() {
        int navigationHeightId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        int navigationHeight = 0;
        if (navigationHeightId > 0) {
            navigationHeight = activity.getResources().getDimensionPixelSize(navigationHeightId);
        }
        return navigationHeight;
    }

    private int getStatusbarHeight(Context context) {
        Resources resources = context.getResources();
        int statusbarHeightId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int statusbarHeight = 0;
        if (statusbarHeightId > 0) {
            statusbarHeight = resources.getDimensionPixelSize(statusbarHeightId);
        }
        return statusbarHeight;
    }

    @Override
    public void onGlobalLayout() {
        Rect r = new Rect();
        View view = activity.getWindow().getDecorView();
        view.getWindowVisibleDisplayFrame(r);

        int statusbarHeight = getStatusbarHeight(KeyboardHeightModel.this.activity);

        int keyboardHeight = rootView.getRootView().getHeight() - r.height() - statusbarHeight;

        if (!hasNoNavigationBar()) {
            int navigationHeight = getNavigationHeight();
            keyboardHeight -= navigationHeight;
        }

        if (keyboardHeight > MIN_KEYBOARD_HEIGHT) {
            JandiPreference.setKeyboardHeight(activity, keyboardHeight);
            if (!isOpened) {
                isOpened = true;
                if (onKeyboardShowListener != null) {
                    onKeyboardShowListener.onShow(true);
                }
            }

            if (onKeyboardHeightCapture != null) {
                onKeyboardHeightCapture.onCapture();
            }
        } else {
            if (isOpened) {
                isOpened = false;
                if (onKeyboardShowListener != null) {
                    onKeyboardShowListener.onShow(false);
                }
            }
        }

    }

    private boolean hasNoNavigationBar() {
        // 기기에 백키/홈키가 있다면 네비게이션바가 없는 기기로 간주...
        return KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
                && KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
    }

    public interface OnKeyboardCaptureListener {
        void onCapture();
    }

    public interface OnKeyboardShowListener {
        void onShow(boolean isShow);
    }
}
