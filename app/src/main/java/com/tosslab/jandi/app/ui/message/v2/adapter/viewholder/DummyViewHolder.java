package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class DummyViewHolder implements BodyViewHolder {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView messageTextView;
    private ProgressBar progressBar;
    private TextView failAlertTextView;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        messageTextView = (TextView) rootView.findViewById(R.id.txt_message_content);
        failAlertTextView = (TextView) rootView.findViewById(R.id.txt_message_fail);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_message);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId) {

        DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

        FormattedEntity entity = EntityManager.getInstance(nameTextView.getContext())
                .getEntityById(dummyMessageLink.message.writerId);

        String profileUrl = entity.getUserLargeProfileUrl();

        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        nameTextView.setText(entity.getName());

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            messageTextView.setText(textMessage.content.body);
        }

        switch (dummyMessageLink.getSendingState()) {
            case Fail:
                failAlertTextView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                break;
            case Sending:
                failAlertTextView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case Complete:
                failAlertTextView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                break;
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_dummy_v2;

    }

}
