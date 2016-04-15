package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public interface BodyViewHolder {

    void initView(View rootView);

    void bindData(ResMessages.Link link, long teamId, long roomId, long entityId);

    void setLastReadViewVisible(long currentLinkId, long lastReadLinkId);

    int getLayoutId();

    void setOnItemClickListener(View.OnClickListener itemClickListener);

    void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener);
}