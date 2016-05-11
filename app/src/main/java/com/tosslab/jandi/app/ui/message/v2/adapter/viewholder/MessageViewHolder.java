package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
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

        ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
        vProfileCover = rootView.findViewById(R.id.v_message_user_profile_cover);

        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        initTextMessageMathWidth();

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
            setProfileInfos(link);
        } else {
            changeVisible(ivProfile, View.GONE);
            changeVisible(tvName, View.GONE);
            changeVisible(vDisableLineThrough, View.GONE);
        }
        setMessage(link, teamId, roomId);
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

    private void setMessage(ResMessages.Link link, long teamId, long roomId) {
        EntityManager entityManager = EntityManager.getInstance();
        ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;

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

            LinkifyUtil.setOnLinkClick(tvMessage);

        } else {
            messageStringBuilder.append("");
        }

        tvMessage.setText(messageStringBuilder, TextView.BufferType.SPANNABLE);

        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

        if (!hasOnlyBadge) {
            tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
        } else {
            tvMessageTime.setVisibility(View.GONE);
        }

        if (unreadCount > 0) {
            tvMessageBadge.setText(String.valueOf(unreadCount));
        }


        linkPreviewViewModel.bindData(link);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_msg_v3;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        tvMessage.setOnClickListener(itemClickListener);

    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        tvMessage.setOnLongClickListener(itemLongClickListener);
    }

    public void setProfileInfos(ResMessages.Link link) {
        long fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        if (fromEntity != null && entity.isEnabled()) {
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            vProfileCover.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            changeVisible(vDisableLineThrough, View.GONE);
        } else {
            tvName.setTextColor(
                    tvName.getResources().getColor(R.color.deactivate_text_color));
            ShapeDrawable foreground = new ShapeDrawable(new OvalShape());
            foreground.getPaint().setColor(0x66FFFFFF);
            vProfileCover.setBackgroundDrawable(foreground);
            changeVisible(vDisableLineThrough, View.VISIBLE);
        }

        tvName.setText(fromEntity.name);
        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
    }

    public static class Builder extends BaseViewHolderBuilder {

        public MessageViewHolder build() {
            MessageViewHolder messageViewHolder = new MessageViewHolder();
            messageViewHolder.setHasOnlyBadge(hasOnlyBadge);
            messageViewHolder.setHasBottomMargin(hasBottomMargin);
            messageViewHolder.setHasProfile(hasProfile);
            return messageViewHolder;
        }
    }

}