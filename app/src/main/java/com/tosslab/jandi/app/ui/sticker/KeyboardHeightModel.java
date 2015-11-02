package com.tosslab.jandi.app.ui.sticker;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

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
    private ArrayList<OnKeyboardShowListener> onKeyboardShowListeners;

    public boolean isOpened() {
        return isOpened;
    }

    @AfterViews
    void initViews() {
        rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public OnKeyboardShowListener getOnKeyboardShowListener() {
        return onKeyboardShowListener;
    }

    public void setOnKeyboardShowListener(OnKeyboardShowListener onKeyboardShowListener) {
        this.onKeyboardShowListener = onKeyboardShowListener;
    }

    public void addOnKeyboardShowListener(OnKeyboardShowListener onKeyboardShowListener) {
        if (onKeyboardShowListeners == null) {
            onKeyboardShowListeners = new ArrayList<>();
        }
        onKeyboardShowListeners.add(onKeyboardShowListener);
    }

    public void setOnKeyboardHeightCaptureListener(OnKeyboardCaptureListener onKeyboardHeightCapture) {

        Resources resources = JandiApplication.getContext().getResources();
        Configuration configuration = resources.getConfiguration();
        if (configuration.keyboard != Configuration.KEYBOARD_NOKEYS) {
            this.onKeyboardHeightCapture = null;
            // if connect Hard keyboard

            // 전체 화면의 3/5 만큼만 되도록 지정
            int stickerHeight = resources.getDisplayMetrics().heightPixels * 2 / 5;
            JandiPreference.setKeyboardHeight(activity, stickerHeight);

            if (onKeyboardHeightCapture != null) {
                onKeyboardHeightCapture.onCapture();
            }
            return;
        }

        this.onKeyboardHeightCapture = onKeyboardHeightCapture;

        Observable.just(1)
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(integer1 -> KeyboardHeightModel.this.onKeyboardHeightCapture != null)
                .subscribe(integer -> {
                    int stickerHeight = resources.getDisplayMetrics().heightPixels * 2 / 5;
                    JandiPreference.setKeyboardHeight(activity, stickerHeight);
                    if (KeyboardHeightModel.this.onKeyboardHeightCapture != null) {
                        KeyboardHeightModel.this.onKeyboardHeightCapture.onCapture();
                    }
                });

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
                notifyOnKeyboardShowListener(true);
            }

            if (onKeyboardHeightCapture != null) {
                onKeyboardHeightCapture.onCapture();
            }
        } else {
            if (isOpened) {
                isOpened = false;
                notifyOnKeyboardShowListener(false);
            }
        }
    }

    private void notifyOnKeyboardShowListener(boolean isShow) {
        if (onKeyboardShowListener != null) {
            onKeyboardShowListener.onShow(isShow);
        }

        if (onKeyboardShowListeners != null && !onKeyboardShowListeners.isEmpty()) {
            for (OnKeyboardShowListener listener : onKeyboardShowListeners) {
                listener.onShow(isShow);
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
