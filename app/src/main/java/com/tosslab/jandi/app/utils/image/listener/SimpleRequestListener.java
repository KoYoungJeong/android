package com.tosslab.jandi.app.utils.image.listener;

import android.net.Uri;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * Created by tonyjs on 16. 5. 10..
 */
public abstract class SimpleRequestListener<TARGET, RESOURCE> implements RequestListener<TARGET, RESOURCE> {
    @Override
    public boolean onException(Exception e, TARGET model, Target<RESOURCE> target,
                               boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onResourceReady(RESOURCE resource, TARGET model, Target<RESOURCE> target,
                                   boolean isFromMemoryCache, boolean isFirstResource) {
        return false;
    }
}
