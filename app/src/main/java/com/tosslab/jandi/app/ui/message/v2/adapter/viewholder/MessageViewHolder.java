package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.UiUtils;

public class MessageViewHolder extends BaseMessageViewHolder {

    protected Context context;

    private ImageView ivProfile;

    private TextView tvName;
    private View vDisableLineThrough;

    private TextView tvMessage;
    private LinkPreviewViewModel linkPreviewViewModel;
    private View vProfileCover;

    private MessageViewHolder() {}

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        context = rootView.getContext();

        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        initTextMessageMathWidth();

        int topMargin = (int) UiUtils.getPixelFromDp(5f);
        if (hasProfile) {
            ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
            vProfileCover = rootView.findViewById(R.id.v_message_user_profile_cover);

            tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
            vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        } else {
            if(hasTopMargin) {
                topMargin = (int) UiUtils.getPixelFromDp(12f);
            } else {
                topMargin = (int) UiUtils.getPixelFromDp(6f);
            }
        }

        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) tvMessage.getLayoutParams();
        layoutParams.topMargin = topMargin;
        tvMessage.setLayoutParams(layoutParams);

        linkPreviewViewModel = new LinkPreviewViewModel(context);
        linkPreviewViewModel.initView(rootView);
    }

    private void initTextMessageMathWidth() {
        int left = tvMessage.getLeft();
        DisplayMetrics displayMetrics = JandiApplication.getContext().getResources().getDisplayMetrics();
        int remainWidth = displayMetrics.widthPixels - left;
        int maxWidth = (int) (remainWidth - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 59f, displayMetrics));
        tvMessage.setMaxWidth(maxWidth);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        setMarginVisible();
        setTimeVisible();
        if (hasProfile) {
            changeVisible(ivProfile, View.VISIBLE);
            changeVisible(tvName, View.VISIBLE);
            ProfileUtil.setProfile(link.fromEntity, ivProfile, vProfileCover, tvName, vDisableLineThrough);
        }
        setMessage(link);
        setMessageTime(link);
        setBadge(teamId, roomId, link);

        setMessageBackground(link);
    }

    private void setMessageBackground(ResMessages.Link link) {
        long writerId = link.fromEntity;
        if (EntityManager.getInstance().isMe(writerId)) {
            tvMessage.setBackgroundResource(R.drawable.bg_message_item_selector_mine);
        } else {
            tvMessage.setBackgroundResource(R.drawable.bg_message_item_selector);
        }
    }

    private void changeVisible(View view, int visible) {
        if (view.getVisibility() != visible) {
            view.setVisibility(visible);
        }
    }

    private void setMessage(ResMessages.Link link) {
        ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;

        if (textMessage.content.contentBuilder == null) {
            EntityManager entityManager = EntityManager.getInstance();
            SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
            if (!TextUtils.isEmpty(textMessage.content.body)) {
                messageStringBuilder.append(textMessage.content.body);
                long myId = entityManager.getMe().getId();
                MentionAnalysisInfo mentionInfo = MentionAnalysisInfo.newBuilder(myId, textMessage.mentions)
                        .textSize(tvMessage.getTextSize())
                        .clickable(true)
                        .build();

                SpannableLookUp.text(messageStringBuilder)
                        .hyperLink(false)
                        .markdown(false)
                        .webLink(false)
                        .telLink(false)
                        .emailLink(false)
                        .mention(mentionInfo, false)
                        .lookUp(tvMessage.getContext());


            } else {
                messageStringBuilder.append("");
            }
            textMessage.content.contentBuilder = messageStringBuilder;
        }

        LinkifyUtil.setOnLinkClick(tvMessage);

        tvMessage.setText(textMessage.content.contentBuilder, TextView.BufferType.SPANNABLE);

        linkPreviewViewModel.bindData(link);
    }

    private void setMessageTime(ResMessages.Link link) {
        if (!hasOnlyBadge) {
            tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
        } else {
            tvMessageTime.setVisibility(View.GONE);
        }
    }

    private void setBadge(long teamId, long roomId, ResMessages.Link link) {
        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());


        if (unreadCount > 0) {
            tvMessageBadge.setText(String.valueOf(unreadCount));
            tvMessageBadge.setVisibility(View.VISIBLE);
        } else {
            tvMessageBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        if (hasProfile) {
            return R.layout.item_message_msg_v3;
        } else {
            return R.layout.item_message_msg_collapse_v3;
        }
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        tvMessage.setOnClickListener(itemClickListener);

    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        tvMessage.setOnLongClickListener(itemLongClickListener);
    }

    public static class Builder extends BaseViewHolderBuilder {

        public MessageViewHolder build() {
            MessageViewHolder messageViewHolder = new MessageViewHolder();
            messageViewHolder.setHasOnlyBadge(hasOnlyBadge);
            messageViewHolder.setHasBottomMargin(hasBottomMargin);
            messageViewHolder.setHasProfile(hasProfile);
            messageViewHolder.setHasTopMargin(hasTopMargin);
            return messageViewHolder;
        }
    }

}