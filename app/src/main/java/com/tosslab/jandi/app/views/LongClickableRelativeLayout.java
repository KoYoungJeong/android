package com.tosslab.jandi.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 9. 24..
 */
public class LongClickableRelativeLayout extends RelativeLayout {

    public static final String TAG = "LongClickableViewGroup";

    private long lastTouchTime;
    private float lastTouchX;
    private float lastTouchY;
    private float touchSlop;

    public LongClickableRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public LongClickableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LongClickableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                LogUtil.e(TAG, "ACTION_DOWN");

                lastTouchTime = ev.getDownTime();
                lastTouchX = ev.getX();
                lastTouchY = ev.getY();

                break;
            case MotionEvent.ACTION_MOVE:
                LogUtil.e(TAG, "ACTION_MOVE");

                long eventTime = ev.getEventTime();

                float distanceX = Math.abs(ev.getX() - lastTouchX);
                float distanceY = Math.abs(ev.getY() - lastTouchY);

                boolean isDrag = (distanceX * distanceY) >= (touchSlop * touchSlop);
                if (isDrag) {
                    clear();
                    setPressed(true);
                    return true;
                }

                if (eventTime - lastTouchTime >= ViewConfiguration.getLongPressTimeout()) {
                    LogUtil.i(TAG, "longPress");
                    clear();
                    if(isLongClickable()) {
                        setPressed(true);
                        performLongClick();
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    return true;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                LogUtil.e(TAG, "ACTION_UP");
                clear();
                setPressed(false);
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    private void clear() {
        lastTouchTime = 0;
        lastTouchX = 0;
        lastTouchY = 0;
    }
}
