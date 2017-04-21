package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.ui.poll.util.PollBinder;

/**
 * Created by tonyjs on 16. 6. 15..
 */
public class PollViewHolder extends BaseMessageViewHolder {

    private View vgPoll;

    private ImageView ivProfile;
    private TextView tvName;
    private View vDisableLineThrough;
    private View vProfileCover;

    private ImageView vPollIcon;
    private TextView tvSubject;
    private TextView tvDescription;
    private TextView tvCreator;
    private TextView tvDueDate;
    private TextView tvPollDeleted;

    private View vMargin;

    @Override
    public void initView(View rootView) {
        super.initView(rootView);

        vgPoll = rootView.findViewById(R.id.vg_message_poll);

        if (hasProfile) {
            ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
            vProfileCover = rootView.findViewById(R.id.v_message_user_profile_cover);
            tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
            vDisableLineThrough = rootView.findViewById(R.id.iv_name_line_through);
        }

        vPollIcon = (ImageView) rootView.findViewById(R.id.v_message_poll_icon);
        tvSubject = (TextView) rootView.findViewById(R.id.tv_message_poll_subject);
        tvCreator = (TextView) rootView.findViewById(R.id.tv_message_poll_creator);
        tvDueDate = (TextView) rootView.findViewById(R.id.tv_message_poll_due_date);
        tvPollDeleted = (TextView) rootView.findViewById(R.id.tv_message_poll_deleted);
        tvDescription = (TextView) rootView.findViewById(R.id.tv_message_poll_description);

        vMargin = rootView.findViewById(R.id.v_margin);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        setMarginVisible();

        if (hasProfile) {
            ProfileUtil.setProfile(link.fromEntity, ivProfile, vProfileCover, tvName, vDisableLineThrough);
        }

        PollBinder.bindPoll(link.poll, false,
                vPollIcon, tvSubject, tvDescription, tvCreator, tvDueDate, tvPollDeleted);

        setMessageBackground(link);
    }

    private void setMessageBackground(ResMessages.Link link) {
        long writerId = link.fromEntity;
        if (TeamInfoLoader.getInstance().getMyId() == writerId) {
            vgPoll.setBackgroundResource(R.drawable.bg_message_item_selector_mine);
        } else {
            vgPoll.setBackgroundResource(R.drawable.bg_message_item_selector);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_poll_v3;
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

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        vgPoll.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        vgPoll.setOnLongClickListener(itemLongClickListener);
    }

    public static class Builder extends BaseViewHolderBuilder {

        public PollViewHolder build() {
            PollViewHolder pollViewHolder = new PollViewHolder();
            pollViewHolder.setHasBottomMargin(hasBottomMargin);
            return pollViewHolder;
        }
    }
}
