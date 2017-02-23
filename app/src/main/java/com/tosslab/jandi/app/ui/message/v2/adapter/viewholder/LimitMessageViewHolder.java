package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;

/**
 * Created by tee on 2017. 2. 22..
 */

public class LimitMessageViewHolder implements BodyViewHolder {

    @Override
    public void initView(View rootView) {

    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {

    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_limit;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {

    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {

    }

    public static class Builder extends BaseViewHolderBuilder {
        public LimitMessageViewHolder build() {
            LimitMessageViewHolder viewHolder = new LimitMessageViewHolder();
            return viewHolder;
        }
    }

}
