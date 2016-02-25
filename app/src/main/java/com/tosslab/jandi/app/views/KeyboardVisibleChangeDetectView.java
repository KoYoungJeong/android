package com.tosslab.jandi.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by tonyjs on 16. 2. 17..
 */
public class KeyboardVisibleChangeDetectView extends View {

    private boolean isShowing;

    public interface OnKeyboardVisibleChangeListener {
        void onKeyboardVisibleChange(boolean isShow, int height);
    }

    private int initializedHeight;

    public KeyboardVisibleChangeDetectView(Context context) {
        super(context);
    }

    public KeyboardVisibleChangeDetectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardVisibleChangeDetectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private OnKeyboardVisibleChangeListener onKeyboardVisibleChangeListener;

    public void setOnKeyboardVisibleChangeListener(
            OnKeyboardVisibleChangeListener onKeyboardVisibleChangeListener) {
        this.onKeyboardVisibleChangeListener = onKeyboardVisibleChangeListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (onKeyboardVisibleChangeListener == null) {
            return;
        }

        if (oldw != w || initializedHeight == 0) {
            initializedHeight = getMeasuredHeight();
            return;
        }

        isShowing = initializedHeight != getMeasuredHeight();
        onKeyboardVisibleChangeListener.onKeyboardVisibleChange(isShowing, h - oldh);
    }

    public boolean isShowing() {
        return isShowing;
    }
}
