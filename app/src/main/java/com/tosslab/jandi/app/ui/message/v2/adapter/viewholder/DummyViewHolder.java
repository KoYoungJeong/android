package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.IonCircleTransform;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class DummyViewHolder implements BodyViewHolder {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView messageTextView;
    private View contentView;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        messageTextView = (TextView) rootView.findViewById(R.id.txt_message_content);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

        FormattedEntity entity = EntityManager.getInstance()
                .getEntityById(dummyMessageLink.message.writerId);

        String profileUrl = entity.getUserLargeProfileUrl();

        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        nameTextView.setText(entity.getName());

        SpannableStringBuilder builder = new SpannableStringBuilder();

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            builder.append(textMessage.content.body);
        }

        SendMessage.Status status = SendMessage.Status.valueOf(dummyMessageLink.getStatus());
        int textColor = nameTextView.getContext().getResources().getColor(R.color.jandi_messages_name);
        switch (status) {
            case FAIL: {
                builder.append("  ");
                int beforLenghth = builder.length();
                Drawable drawable = messageTextView.getContext().getResources()
                        .getDrawable(R.drawable.jandi_icon_message_failure);
                drawable.setBounds(0, 0,
                        drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                builder.append(" ")
                        .setSpan(
                                new ImageSpan(drawable,
                                        ImageSpan.ALIGN_BASELINE),
                                beforLenghth, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                nameTextView.setTextColor(textColor);
                messageTextView.setTextColor(textColor);
                break;
            }
            case SENDING: {
                builder.append("  ");
                int beforLenghth = builder.length();
                Drawable drawable = messageTextView.getContext().getResources()
                        .getDrawable(R.drawable.jandi_icon_message_sending);
                drawable.setBounds(0, 0,
                        drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                builder.append(" ")
                        .setSpan(
                                new ImageSpan(drawable,
                                        ImageSpan.ALIGN_BASELINE),
                                beforLenghth, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                messageTextView.setTextColor(textColor);
                nameTextView.setTextColor(textColor);
                messageTextView.setTextColor(textColor);
                break;
            }
            case COMPLETE:
                builder.append(" ");
                nameTextView.setTextColor(textColor);
                messageTextView.setTextColor(textColor);
                break;
        }

        GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                messageTextView, builder, ((DummyMessageLink) link).getMentions(),
                EntityManager.getInstance().getMe().getId());
        builder = generateMentionMessageUtil.generate(false);

        messageTextView.setText(builder);

    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_dummy_v2;

    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (contentView != null && itemClickListener != null) {
            contentView.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        if (contentView != null && itemLongClickListener != null) {
            contentView.setOnLongClickListener(itemLongClickListener);
        }
    }

}
