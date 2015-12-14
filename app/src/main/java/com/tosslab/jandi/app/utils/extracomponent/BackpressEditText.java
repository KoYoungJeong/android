package com.tosslab.jandi.app.utils.extracomponent;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.AutoCompleteTextView;

/**
 * Created by tee on 15. 12. 1..
 */
public class BackpressEditText extends AutoCompleteTextView {

    private OnBackPressListener _listener;


    public BackpressEditText(Context context) {
        super(context);
    }


    public BackpressEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public BackpressEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        boolean onlyHandleByCallback = false;

        if (keyCode == KeyEvent.KEYCODE_BACK && _listener != null) {
            onlyHandleByCallback = _listener.onBackPress();
        }

        if (!onlyHandleByCallback) {
            return super.onKeyPreIme(keyCode, event);
        } else {
            return true;
        }
    }

    public void setOnBackPressListener(OnBackPressListener $listener) {
        _listener = $listener;
    }

    public interface OnBackPressListener {
        public boolean onBackPress();
    }

}
