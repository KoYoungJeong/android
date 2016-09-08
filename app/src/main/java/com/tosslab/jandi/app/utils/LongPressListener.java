package com.tosslab.jandi.app.utils;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by tonyjs on 2016. 9. 8..
 */
public abstract class LongPressListener implements View.OnTouchListener {

    private static final int CONSUME_LONG_PRESS = 1;
    private static final int DEFAULT_PRESS_TIMEOUT = 5000;
    private long pressTimeOut;
    private LongPressHandler handler;

    public LongPressListener() {
        this(DEFAULT_PRESS_TIMEOUT);
    }

    public LongPressListener(long pressTimeOut) {
        this.pressTimeOut = pressTimeOut;
        handler = new LongPressHandler(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handler.sendEmptyMessageDelayed(CONSUME_LONG_PRESS, pressTimeOut);
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (event.getEventTime() - event.getDownTime() < pressTimeOut) {
                handler.removeMessages(CONSUME_LONG_PRESS);
                v.performClick();
            }
            return true;
        }
        return false;
    }

    public abstract void onLongPressed();

    private static class LongPressHandler extends Handler {

        private WeakReference<LongPressListener> ref;

        public LongPressHandler(LongPressListener longPressListener) {
            this.ref = new WeakReference<>(longPressListener);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LongPressListener listener = ref != null ? ref.get() : null;
            if (listener != null) {
                listener.onLongPressed();
            }
        }
    }
}
