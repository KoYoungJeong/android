package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.utils.DateTransformator;

public class StickerMessageViewHolder extends BaseMessageViewHolder {

    protected Context context;

    private ImageView ivProfile;
    private ViewGroup vgProfileAbsence;

    private TextView tvName;
    private View vDisableLineThrough;
    private ImageView ivSticker;
    private View vProfileCover;

    private StickerMessageViewHolder() {
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        context = rootView.getContext();
        if (hasProfile) {
            ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
            vgProfileAbsence = (ViewGroup) rootView.findViewById(R.id.vg_profile_absence);
            vProfileCover = rootView.findViewById(R.id.v_message_user_profile_cover);
            tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
            vDisableLineThrough = rootView.findViewById(R.id.iv_name_line_through);
        }
        ivSticker = (ImageView) rootView.findViewById(R.id.iv_message_sticker);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        setMarginVisible();
        setTimeVisible();
        if (hasProfile) {
            ivProfile.setVisibility(View.VISIBLE);
            tvName.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);
            ProfileUtil.setProfile(link.fromEntity, ivProfile, vgProfileAbsence, vProfileCover,
                    tvName, vDisableLineThrough);
        }
        setBadge(link, teamId, roomId);
        setTime(link);
        setSticker(link);
    }

    private void setTime(ResMessages.Link link) {
        if (!hasOnlyBadge) {
            tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
        } else {
            tvMessageTime.setVisibility(View.GONE);
        }
    }

    private void setSticker(ResMessages.Link link) {
        ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) link.message;
        ResMessages.StickerContent content = stickerMessage.content;

        StickerManager.getInstance().loadStickerNoOption(ivSticker, content.groupId, content.stickerId);
    }

    private void setBadge(ResMessages.Link link, long teamId, long roomId) {

        if (link.unreadCnt > 0) {
            tvMessageBadge.setText(String.valueOf(link.unreadCnt));
            tvMessageBadge.setVisibility(View.VISIBLE);
        } else {
            tvMessageBadge.setVisibility(View.GONE);
        }

    }

    @Override
    public int getLayoutId() {
        if (hasProfile) {
            return R.layout.item_message_sticker_v3;
        } else {
            return R.layout.item_message_sticker_collapse_v3;
        }
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        ivSticker.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        ivSticker.setOnLongClickListener(itemLongClickListener);
    }

    public static class Builder extends BaseViewHolderBuilder {

        public StickerMessageViewHolder build() {
            StickerMessageViewHolder messageViewHolder = new StickerMessageViewHolder();
            messageViewHolder.setHasOnlyBadge(hasOnlyBadge);
            messageViewHolder.setHasBottomMargin(hasBottomMargin);
            messageViewHolder.setHasProfile(hasProfile);
            return messageViewHolder;
        }
    }

}