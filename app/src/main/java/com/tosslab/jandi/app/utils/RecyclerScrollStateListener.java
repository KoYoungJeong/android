package com.tosslab.jandi.app.utils;

import android.support.v7.widget.RecyclerView;

public class RecyclerScrollStateListener {

    private boolean scrolling = false;
    private Call call;

    public void onScrollState(int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (scrolling) {
                scrolling = false;
                if (call != null) {
                    call.changedScrollState(false);
                }
            }
        } else {
            if (!scrolling) {
                scrolling = true;
                if (call != null) {
                    call.changedScrollState(true);
                }
            }
        }
    }

    public void setListener(Call call) {
        this.call = call;
    }

    public boolean hasListener() {
        return call != null ? true : false;
    }

    public interface Call {
        void changedScrollState(boolean scrolling);
    }
}
