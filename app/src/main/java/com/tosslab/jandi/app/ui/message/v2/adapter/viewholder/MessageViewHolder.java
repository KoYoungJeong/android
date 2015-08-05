package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.MensionMessageSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class MessageViewHolder implements BodyViewHolder {

    protected Context context;
    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView messageTextView;
    private View disableCoverView;
    private View disableLineThroughView;
    private LinkPreviewViewModel linkPreviewViewModel;
    private View lastReadView;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        messageTextView = (TextView) rootView.findViewById(R.id.txt_message_content);
        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);
        context = rootView.getContext();

        linkPreviewViewModel = new LinkPreviewViewModel(context);
        linkPreviewViewModel.initView(rootView);
        lastReadView = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {
        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != null ? entityById.getUser() : null;
        if (user != null && TextUtils.equals(user.status, "enabled")) {
            nameTextView.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            disableCoverView.setVisibility(View.GONE);
            disableLineThroughView.setVisibility(View.GONE);
        } else {
            nameTextView.setTextColor(
                    nameTextView.getResources().getColor(R.color.deactivate_text_color));
            disableCoverView.setVisibility(View.VISIBLE);
            disableLineThroughView.setVisibility(View.VISIBLE);
        }

        nameTextView.setText(fromEntity.name);
        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;

            SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
            messageStringBuilder.append(!TextUtils.isEmpty(textMessage.content.body) ? textMessage.content.body : "");


            boolean hasLink = LinkifyUtil.addLinks(context, messageStringBuilder);
            if (hasLink) {
                Spannable linkSpannable =
                        Spannable.Factory.getInstance().newSpannable(messageStringBuilder);
                messageStringBuilder.setSpan(linkSpannable,
                        0, textMessage.content.body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                LinkifyUtil.setOnLinkClick(messageTextView);
            }

            messageStringBuilder.append(" ");

            Resources resources = context.getResources();

            int startIndex = messageStringBuilder.length();
            messageStringBuilder.append(
                    DateTransformator.getTimeStringForSimple(link.message.createTime));
            int endIndex = messageStringBuilder.length();

            DateViewSpannable spannable =
                    new DateViewSpannable(messageTextView.getContext(),
                            DateTransformator.getTimeStringForSimple(link.message.createTime));
            messageStringBuilder.setSpan(spannable,
                    startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                    link.id, link.fromEntity, EntityManager.getInstance(context).getMe().getId());

            if (unreadCount > 0) {
                NameSpannable unreadCountSpannable =
                        new NameSpannable(
                                context.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_small)
                                , context.getResources().getColor(R.color.jandi_accent_color));
                int beforeLength = messageStringBuilder.length();
                messageStringBuilder.append(" ");
                messageStringBuilder.append(String.valueOf(unreadCount))
                        .setSpan(unreadCountSpannable, beforeLength, messageStringBuilder.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            boolean hasMention = false;
            for (MentionObject mention : textMessage.mentions) {
                String name = messageStringBuilder.subSequence(mention.getOffset() + 1, mention.getLength() + mention.getOffset()).toString();
                MensionMessageSpannable spannable1 = new MensionMessageSpannable(messageTextView.getContext(),
                        name, mention.getId(),
                        messageTextView.getResources().getDimensionPixelSize(R.dimen.jandi_mention_message_item_font_size));
                messageStringBuilder.setSpan(spannable1, mention.getOffset(), mention.getLength() + mention.getOffset(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (!hasMention) {
                    hasMention = true;
                }
            }

            if (hasMention) {
                LinkifyUtil.setOnLinkClick(messageTextView);
            }

            messageTextView.setText(messageStringBuilder);

        }
        profileImageView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
        nameTextView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));

        linkPreviewViewModel.bindData(link);
    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            lastReadView.setVisibility(View.VISIBLE);
        } else {
            lastReadView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_msg_v2;
    }
}
