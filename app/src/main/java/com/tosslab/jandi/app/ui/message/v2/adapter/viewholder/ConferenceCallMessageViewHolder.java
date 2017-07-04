package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.utils.DateTransformator;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by gary on 2017. 7. 4..
 */

public class ConferenceCallMessageViewHolder  extends BaseMessageViewHolder {

    @Bind(R.id.iv_message_user_profile)
    ImageView ivMessageUserProfile;

    @Bind(R.id.vg_message_conference_call)
    ViewGroup vgMessageConferenceCall;

    @Bind(R.id.vg_profile_absence)
    LinearLayout vgProfileAbsence;

    @Bind(R.id.v_message_user_profile_cover)
    View vMessageUserProfileCover;

    @Bind(R.id.tv_message_user_name)
    TextView tvMessageUserName;

    @Bind(R.id.iv_name_line_through)
    ImageView ivNameLineThrough;

    @Bind(R.id.tv_message_badge)
    TextView tvMessageBadge;

    @Bind(R.id.tv_message_time)
    TextView tvMessageTime;

    @Bind(R.id.v_margin)
    View vMargin;

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        ButterKnife.bind(this, rootView);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        String url = ((ResMessages.TextMessage)link.message).content.body;
        ProfileUtil.setProfile(link.fromEntity, ivMessageUserProfile,
                vgProfileAbsence, vMessageUserProfileCover, tvMessageUserName, ivNameLineThrough);
        tvMessageBadge.setText(String.valueOf(link.unreadCnt));
        tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
        if(hasBottomMargin){
            vMargin.setVisibility(View.VISIBLE);
        }else{
            vMargin.setVisibility(View.GONE);
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_conference_call;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        vgMessageConferenceCall.setOnClickListener(v -> itemClickListener.onClick(v));
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
    }

    public static class Builder extends BaseViewHolderBuilder {
        public ConferenceCallMessageViewHolder build() {
            ConferenceCallMessageViewHolder ViewHolder = new ConferenceCallMessageViewHolder();
            ViewHolder.setHasBottomMargin(hasBottomMargin);
            return ViewHolder;
        }
    }
}
