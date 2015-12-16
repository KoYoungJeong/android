package com.tosslab.jandi.app.utils.image;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.facebook.common.references.CloseableReference;

/**
 * Created by tonyjs on 15. 12. 8..
 */
public abstract class BaseOnResourceReadyCallback implements OnResourceReadyCallback {
    @Override
    public void onReady(Drawable drawable, CloseableReference reference) {

    }

    @Override
    public void onFail(Throwable cause) {

    }

    @Override
    public void onProgressUpdate(float progress) {

    }
}
