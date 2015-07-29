package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.view.View;

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