package com.tosslab.jandi.app.utils;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.module.GlideModule;

/**
 * Created by tonyjs on 15. 12. 3..
 */
public class GlideQualityModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDecodeFormat(DecodeFormat.ALWAYS_ARGB_8888);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {

    }
}
