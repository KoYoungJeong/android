package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class DummyPureViewHolder implements BodyViewHolder {

    private TextView messageTextView;
    private ProgressBar progressBar;
    private TextView failAlertTextView;

    @Override
    public void initView(View rootView) {
        messageTextView = (TextView) rootView.findViewById(R.id.txt_message_content);
        failAlertTextView = (TextView) rootView.findViewById(R.id.txt_message_fail);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_message);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        DummyMessageLink dummyMessageLink = (DummyMessageLink) link;

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;
            messageTextView.setText(textMessage.content.body);
        }

        SendMessage.Status status = SendMessage.Status.valueOf(dummyMessageLink.getStatus());
        switch (status) {
            case FAIL:
                failAlertTextView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                break;
            case SENDING:
                failAlertTextView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case COMPLETE:
                failAlertTextView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                break;
        }

    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_dummy_pure_v2;

    }

}
