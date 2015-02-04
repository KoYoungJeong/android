package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.utils.GlideCircleTransform;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class DummyViewHolder implements BodyViewHolder {


    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView messageTextView;
    private ProgressBar progressBar;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        messageTextView = (TextView) rootView.findViewById(R.id.txt_message_content);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_message);

    }


    @Override
    public void bindData(ResMessages.Link link) {

        DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

        String profileUrl = ((dummyMessageLink.message.writer.u_photoThumbnailUrl != null) && TextUtils.isEmpty(dummyMessageLink.message.writer.u_photoThumbnailUrl.largeThumbnailUrl)) ? dummyMessageLink.message.writer.u_photoThumbnailUrl.largeThumbnailUrl : dummyMessageLink.message.writer.u_photoUrl;
        Glide.with(profileImageView.getContext())
                .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + profileUrl)
                .placeholder(R.drawable.jandi_profile)
                .transform(new GlideCircleTransform(profileImageView.getContext()))
                .crossFade()
                .into(profileImageView);

        nameTextView.setText(dummyMessageLink.message.writer.name);

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            messageTextView.setText(textMessage.content.body);
        }

        switch (dummyMessageLink.getSendingState()) {
            case Fail:
                break;
            case Sending:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case Complete:
                progressBar.setVisibility(View.GONE);
                break;
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_dummy_v2;

    }
}
