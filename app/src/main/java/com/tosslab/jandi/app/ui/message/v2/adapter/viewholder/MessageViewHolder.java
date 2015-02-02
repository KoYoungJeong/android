package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GlideCircleTransform;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class MessageViewHolder implements BodyViewHolder {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView dateTextView;
    private TextView messageTextView;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);
        messageTextView = (TextView) rootView.findViewById(R.id.txt_message_content);
    }

    @Override
    public void bindData(ResMessages.Link link) {

        String profileUrl = ((link.message.writer.u_photoThumbnailUrl != null) && TextUtils.isEmpty(link.message.writer.u_photoThumbnailUrl.largeThumbnailUrl)) ? link.message.writer.u_photoThumbnailUrl.largeThumbnailUrl : link.message.writer.u_photoUrl;
        Glide.with(profileImageView.getContext())
                .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + profileUrl)
                .placeholder(R.drawable.jandi_profile)
                .transform(new GlideCircleTransform(profileImageView.getContext()))
                .crossFade()
                .into(profileImageView);

        nameTextView.setText(link.message.writer.name);
        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            messageTextView.setText(textMessage.content.body);
        }
        profileImageView.setOnClickListener(v -> EventBus.getDefault().post(new RequestUserInfoEvent(link.message.writerId)));

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_msg_v2;

    }
}