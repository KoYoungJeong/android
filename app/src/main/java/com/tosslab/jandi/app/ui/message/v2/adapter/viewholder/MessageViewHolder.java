package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.views.spannable.MessageSpannable;
import com.tosslab.jandi.app.views.spannable.UnreadCountSpannable;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class MessageViewHolder implements BodyViewHolder {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView messageTextView;
    private View disableCoverView;
    private View disableLineThroughView;


    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        messageTextView = (TextView) rootView.findViewById(R.id.txt_message_content);
        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance(nameTextView.getContext()).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = ((fromEntity.u_photoThumbnailUrl != null) && TextUtils.isEmpty(fromEntity.u_photoThumbnailUrl.largeThumbnailUrl)) ? fromEntity.u_photoThumbnailUrl.largeThumbnailUrl : fromEntity.u_photoUrl;

        EntityManager entityManager = EntityManager.getInstance(profileImageView.getContext());
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        if (entityById != null && entityById.getUser() != null && TextUtils.equals(entityById.getUser().status, "enabled")) {

            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.jandi_messages_name));

            disableCoverView.setVisibility(View.GONE);
            disableLineThroughView.setVisibility(View.GONE);
        } else {
            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.deactivate_text_color));

            disableCoverView.setVisibility(View.VISIBLE);
            disableLineThroughView.setVisibility(View.VISIBLE);
        }

        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + profileUrl);

        nameTextView.setText(fromEntity.name);

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;

            SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
            messageStringBuilder.append(textMessage.content.body);

            boolean hasLink = LinkifyUtil.addLinks(messageTextView.getContext(), messageStringBuilder);
            if (hasLink) {
                Spannable linkSpannable = Spannable.Factory.getInstance().newSpannable(messageStringBuilder);
                messageStringBuilder.setSpan(linkSpannable, 0, textMessage.content.body.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                LinkifyUtil.setOnLinkClick(messageTextView);
            }

            messageStringBuilder.append(" ");

            Resources resources = messageTextView.getContext().getResources();

            int dateSpannableTextSize = ((int) resources.getDimension(R.dimen.jandi_messages_date));
            int dateSpannableTextColor = resources.getColor(R.color.jandi_messages_date);

            int startIndex = messageStringBuilder.length();
            messageStringBuilder.append(DateTransformator.getTimeStringForSimple(link.message.updateTime));
            int endIndex = messageStringBuilder.length();

            MessageSpannable spannable =
                    new MessageSpannable(dateSpannableTextSize, dateSpannableTextColor);
            messageStringBuilder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            int unreadCount = UnreadCountUtil.getUnreadCount(messageTextView.getContext(), teamId, roomId,
                    link.id, link.fromEntity, EntityManager.getInstance(messageTextView.getContext()).getMe().getId());

            if (unreadCount > 0) {
                UnreadCountSpannable unreadCountSpannable =
                        UnreadCountSpannable.createUnreadCountSpannable(
                                messageTextView.getContext(), String.valueOf(unreadCount));
                messageStringBuilder.append("   ")
                        .setSpan(unreadCountSpannable, messageStringBuilder.length() - 2, messageStringBuilder.length() - 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            messageTextView.setText(messageStringBuilder);

        }
        profileImageView.setOnClickListener(v -> EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
        nameTextView.setOnClickListener(v -> EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_msg_v2;

    }

}
