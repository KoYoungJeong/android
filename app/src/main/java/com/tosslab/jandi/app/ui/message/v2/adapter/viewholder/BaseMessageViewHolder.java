package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by tee on 16. 4. 12..
 */
public abstract class BaseMessageViewHolder implements BodyViewHolder {

    protected ViewGroup vgMessageContent;
    protected ViewGroup vgStickerMessageContent;
    protected ViewGroup vgFileMessageContent;
    protected ViewGroup vgImageMessageContent;
    protected TextView tvMessageBadge;
    protected TextView tvMessageTime;
    protected boolean hasProfile = true;
    protected boolean hasBottomMargin = false;
    protected boolean hasOnlyBadge = false;
    private ViewGroup vgMessageProfileImage;
    private ViewGroup vgMessageProfileName;
    private View vMargin;
    private ViewGroup vgMessageLastRead;

    @Override
    public void initView(View rootView) {
        vgMessageProfileImage = (ViewGroup) rootView.findViewById(R.id.vg_message_profile_image);
        vgMessageProfileName = (ViewGroup) rootView.findViewById(R.id.vg_message_profile_user_name);

        vgMessageContent = (ViewGroup) rootView.findViewById(R.id.vg_message_content);
        vgStickerMessageContent = (ViewGroup) rootView.findViewById(R.id.vg_sticker_message_content);
        vgFileMessageContent = (ViewGroup) rootView.findViewById(R.id.vg_file_message_content);
        vgImageMessageContent = (ViewGroup) rootView.findViewById(R.id.vg_image_message_content);
        vMargin = rootView.findViewById(R.id.v_margin);
        vgMessageLastRead = (ViewGroup) rootView.findViewById(R.id.vg_message_last_read);

        tvMessageBadge = (TextView) rootView.findViewById(R.id.tv_message_badge);
        tvMessageTime = (TextView) rootView.findViewById(R.id.tv_message_time);

        setOptionView();
        initObjects();
    }

    private void setOptionView() {

        if (hasBottomMargin) {
            vMargin.setVisibility(View.VISIBLE);
        } else {
            vMargin.setVisibility(View.GONE);
        }

        if (hasProfile) {
            // PROFILE MODE
            vgMessageProfileImage.setVisibility(View.VISIBLE);
            vgMessageProfileName.setVisibility(View.VISIBLE);
        } else {
            // PURE MODE
            vgMessageProfileImage.setVisibility(View.INVISIBLE);
            vgMessageProfileName.setVisibility(View.GONE);
        }

        if (hasOnlyBadge) {
            tvMessageTime.setVisibility(View.GONE);
        } else {
            tvMessageTime.setVisibility(View.VISIBLE);
        }

    }

    abstract protected void initObjects();

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
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {

    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            vgMessageLastRead.setVisibility(View.VISIBLE);
        } else {
            vgMessageLastRead.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_msg_v3;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {

    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {

    }

}
