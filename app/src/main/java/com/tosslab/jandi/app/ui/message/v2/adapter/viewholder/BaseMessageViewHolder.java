package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

/**
 * Created by tee on 16. 4. 12..
 */
public abstract class BaseMessageViewHolder implements BodyViewHolder {

    protected TextView tvMessageBadge;
    protected TextView tvMessageTime;
    protected boolean hasProfile = true;
    protected boolean hasBottomMargin = false;
    protected boolean hasOnlyBadge = false;
    private View vMargin;
    private ViewGroup vgMessageLastRead;

    @Override
    public void initView(View rootView) {

        vMargin = rootView.findViewById(R.id.v_margin);
        vgMessageLastRead = (ViewGroup) rootView.findViewById(R.id.vg_message_last_read);

        tvMessageBadge = (TextView) rootView.findViewById(R.id.tv_message_badge);
        tvMessageTime = (TextView) rootView.findViewById(R.id.tv_message_time);
    }

    void setMarginVisible() {
        if (vMargin != null) {
            if (hasBottomMargin) {
                vMargin.setVisibility(View.VISIBLE);
            } else {
                vMargin.setVisibility(View.GONE);
            }
        }
    }

    void setTimeVisible() {
        if (tvMessageTime != null) {
            if (hasOnlyBadge) {
                tvMessageTime.setVisibility(View.GONE);
            } else {
                tvMessageTime.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    protected void setHasProfile(boolean hasProfile) {
        this.hasProfile = hasProfile;
    }

    protected void setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
        if (vgMessageLastRead != null) {
            if (currentLinkId == lastReadLinkId) {
                vgMessageLastRead.removeAllViews();
                LayoutInflater.from(vgMessageLastRead.getContext()).inflate(R.layout.item_message_last_read_v2, vgMessageLastRead);
                vgMessageLastRead.setVisibility(View.VISIBLE);
            } else {
                vgMessageLastRead.setVisibility(View.GONE);
            }
        }
    }
}
