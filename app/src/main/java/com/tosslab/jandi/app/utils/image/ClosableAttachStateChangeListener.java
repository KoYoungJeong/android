package com.tosslab.jandi.app.utils.image;

import android.view.View;

import com.facebook.common.references.CloseableReference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 12. 10..
 */
public class ClosableAttachStateChangeListener implements View.OnAttachStateChangeListener {

    public static final String TAG = ClosableAttachStateChangeListener.class.getSimpleName();
    private CloseableReference reference;

    public ClosableAttachStateChangeListener(CloseableReference reference) {
        this.reference = reference;
    }

    @Override
    public void onViewAttachedToWindow(View v) {

    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        LogUtil.i(TAG, "onViewDetachedFromWindow");
        if (reference != null) {
            reference.close();
        }
    }
}
