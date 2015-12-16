package com.tosslab.jandi.app.utils.image;

import android.graphics.drawable.Drawable;

import com.facebook.common.references.CloseableReference;

/**
 * Created by tonyjs on 15. 12. 8..
 */
public interface OnResourceReadyCallback {
    void onReady(Drawable drawable, CloseableReference reference);

    void onFail(Throwable cause);

    void onProgressUpdate(float progress);
}
