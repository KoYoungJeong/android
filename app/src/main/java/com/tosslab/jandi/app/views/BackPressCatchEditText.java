package com.tosslab.jandi.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.AutoCompleteTextView;

/**
 * Created by tee on 15. 12. 1..
 */
public class BackPressCatchEditText extends AutoCompleteTextView {

    private OnBackPressListener onBackPressListener;

    public BackPressCatchEditText(Context context) {
        super(context);
    }

    public BackPressCatchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackPressCatchEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        boolean consumed = false;

        if (keyCode == KeyEvent.KEYCODE_BACK && onBackPressListener != null) {
            consumed = onBackPressListener.onBackPress();
        }

        if (!consumed) {
            return super.onKeyPreIme(keyCode, event);
        } else {
            return true;
        }
    }

    public void setOnBackPressListener(OnBackPressListener onBackPressListener) {
        this.onBackPressListener = onBackPressListener;
    }

    public interface OnBackPressListener {
        boolean onBackPress();
    }

}
