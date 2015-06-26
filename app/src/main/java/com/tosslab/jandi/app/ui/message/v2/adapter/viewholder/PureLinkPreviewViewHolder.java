package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;
import com.tosslab.jandi.app.views.AutoScaleImageView;

/**
 * Created by tonyjs on 15. 6. 10..
 */
public class PureLinkPreviewViewHolder extends PureMessageViewHolder {
    @Override
    public void initView(View rootView) {
        super.initView(rootView);

        Context context = rootView.getContext();

        int paddingLeft = rootView.getPaddingLeft();
        int paddingTop = (int) (context.getResources().getDisplayMetrics().density * 15);
        int paddingRight = rootView.getPaddingRight();
        int paddingBottom = rootView.getPaddingBottom();

        rootView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }
}