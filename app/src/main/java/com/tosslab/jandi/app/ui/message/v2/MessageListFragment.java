package com.tosslab.jandi.app.ui.message.v2;

import android.app.Fragment;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.ReqeustMoreMessageEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.TextChange;
import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EFragment(R.layout.fragment_message_list)
public class MessageListFragment extends Fragment {

    private static final Logger logger = Logger.getLogger(MessageListFragment.class);

    @FragmentArg
    int entityType;
    @FragmentArg
    int entityId;
    @FragmentArg
    boolean isFavorite = false;
    @FragmentArg
    boolean isFromPush = false;
    @FragmentArg
    int teamId;

    @Bean
    MessageListPresenter messageListPresenter;

    @Bean
    MessageListModel messageListModel;

    private MessageState messageState;

    @AfterInject
    void initObject() {
        messageState = new MessageState();
    }

    @AfterViews
    void initViews() {
        messageListModel.setEntityInfo(entityType, entityId);
        messageListPresenter.showProgressWheel();
        getOldMessageList(messageState.getFirstItemId());
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Background
    void getOldMessageList(int linkId) {
        try {
            ResMessages oldMessage = messageListModel.getOldMessage(linkId);

            messageState.setFirstItemId(oldMessage.firstIdOfReceivedList);
            messageState.setFirstMessage(oldMessage.isFirst);

            if (linkId == -1) {

                messageListPresenter.addAll(0, oldMessage.messages);
                messageState.setLastUpdateLinkId(oldMessage.lastLinkId);
                messageListPresenter.moveLastPage();

            } else {

                int lastVisibleLinkId = messageListPresenter.getFirstVisibleItemLinkId();
                int firstVisibleItemTop = messageListPresenter.getFirstVisibleItemTop();
                messageListPresenter.addAll(0, oldMessage.messages);
                messageListPresenter.moveToMessage(lastVisibleLinkId, firstVisibleItemTop);

            }

            if (!oldMessage.isFirst) {
                messageListPresenter.setLoadingComplete();
            } else {
                messageListPresenter.setNoMoreLoading();
            }

        } catch (JandiNetworkException e) {
            logger.debug(e.getErrorInfo() + " : " + e.httpBody, e);
        } finally {
            messageListPresenter.dismissProgressWheel();
        }
    }

    @Background
    void getNewMessageList(int linkId) {
        try {
            ResUpdateMessages newMessage = messageListModel.getNewMessage(linkId);
//            List<ResMessages.Link> links = messageListModel.sortById(newMessage.updateInfo.messages);
            int lastItemPosition = messageListPresenter.getLastItemPosition();
            messageListPresenter.addAll(lastItemPosition, newMessage.updateInfo.messages);
            messageState.setLastUpdateLinkId(newMessage.lastLinkId);

        } catch (JandiNetworkException e) {
            logger.debug(e.getErrorInfo() + " : " + e.httpBody, e);
        }
    }

    @Click(R.id.btn_upload_file)
    void onUploadClick() {

    }

    @Click(R.id.btn_send_message)
    void onSendClick() {

    }

    @TextChange(R.id.et_message)
    void onMessageEditChange(TextView tv, CharSequence text) {

        boolean isEmptyText = messageListModel.isEmpty(text);
        messageListPresenter.setEnableSendButton(!isEmptyText);

    }

    public void onEvent(ReqeustMoreMessageEvent event) {
        if (!messageState.isFirstMessage()) {
            getOldMessageList(messageState.getFirstItemId());
        }
    }
}


