package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration;

import android.view.View;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;

public class CollapseIntegrationBotViewHolder implements BodyViewHolder {

    private View contentView;
    @Override
    public void initView(View rootView) {

    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {

    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (contentView != null && itemClickListener != null) {
            contentView.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        if (contentView != null && itemLongClickListener != null) {
            contentView.setOnLongClickListener(itemLongClickListener);
        }
    }
}
