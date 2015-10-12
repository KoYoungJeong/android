package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;

public class Divider implements BodyViewHolder {
    private final BodyViewHolder originBodyViewHolder;
    private final boolean hasDivider;

    private View vDivider;

    public Divider(BodyViewHolder originBodyViewHolder, boolean hasDivider) {
        this.originBodyViewHolder = originBodyViewHolder;
        this.hasDivider = hasDivider;
    }

    public void setUpDividerVisible() {
        if (vDivider != null) {
            if (hasDivider) {
                vDivider.setVisibility(View.VISIBLE);
            } else {
                vDivider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void initView(View rootView) {
        if (originBodyViewHolder != null) {
            originBodyViewHolder.initView(rootView);
        }

        vDivider = rootView.findViewById(R.id.v_message_divider);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {
        if (originBodyViewHolder != null) {
            originBodyViewHolder.bindData(link, teamId, roomId, entityId);
        }
    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
        if (originBodyViewHolder != null) {
            originBodyViewHolder.setLastReadViewVisible(currentLinkId, lastReadLinkId);
        }
    }

    @Override
    public int getLayoutId() {
        if (originBodyViewHolder != null) {
            return originBodyViewHolder.getLayoutId();
        }
        return 0;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (originBodyViewHolder != null) {
            originBodyViewHolder.setOnItemClickListener(itemClickListener);
        }
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        if (originBodyViewHolder != null) {
            originBodyViewHolder.setOnItemLongClickListener(itemLongClickListener);
        }
    }

    public static class Builder {
        private BodyViewHolder originBodyViewHolder;
        private boolean hasDivider;

        public Builder bodyViewHolder(BodyViewHolder originBodyViewHolder) {
            this.originBodyViewHolder = originBodyViewHolder;
            return this;
        }

        public Builder divider(boolean hasDivider) {
            this.hasDivider = hasDivider;
            return this;
        }

        public Divider build() {
            return new Divider(originBodyViewHolder, hasDivider);
        }
    }
}
