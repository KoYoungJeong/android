package com.tosslab.jandi.app.utils;

/**
 * Created by tonyjs on 15. 11. 6..
 */
public interface OnSwipeExitListener {
    int DIRECTION_TO_TOP = 0;

    int DIRECTION_TO_BOTTOM = 1;

    void onSwipeExit(int direction);
}
