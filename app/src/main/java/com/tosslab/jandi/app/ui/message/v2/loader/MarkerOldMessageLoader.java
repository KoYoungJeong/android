package com.tosslab.jandi.app.ui.message.v2.loader;

import android.content.Context;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.apache.log4j.Logger;

import java.util.Collections;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
public class MarkerOldMessageLoader implements OldMessageLoader {

    private static final Logger logger = Logger.getLogger(MarkerOldMessageLoader.class);

    private final Context context;
    MessageListModel messageListModel;
    MessageListPresenter messageListPresenter;
    private MessageState messageState;

    public MarkerOldMessageLoader(Context context) {

        this.context = context;
    }

    public void setMessageListModel(MessageListModel messageListModel) {
        this.messageListModel = messageListModel;
    }

    public void setMessageListPresenter(MessageListPresenter messageListPresenter) {
        this.messageListPresenter = messageListPresenter;
    }

    public void setMessageState(MessageState messageState) {
        this.messageState = messageState;
    }

    @Override
    public void load(int linkId) {
        try {

            ResMessages oldMessage;
            boolean isCallByMarker = messageListPresenter.getFirstVisibleItemLinkId() > 0;
            if (isCallByMarker) {
                oldMessage = messageListModel.getOldMessage(linkId);
            } else {
                oldMessage = messageListModel.getBeforeMarkerMessage(linkId);
            }


            if (oldMessage.records == null || oldMessage.records.isEmpty()) {
                messageListPresenter.setEmptyView();
                return;
            }

            if (!isCallByMarker) {
                if (oldMessage.lastLinkId == oldMessage.records.get(oldMessage.records.size() - 1).id) {
                    messageListPresenter.setGotoLatestLayoutVisibleGone();
                }
            }

            int firstLinkId = oldMessage.records.get(0).id;
            messageState.setFirstItemId(firstLinkId);
            boolean isFirstMessage = oldMessage.firstLinkId == firstLinkId;
            messageState.setFirstMessage(isFirstMessage);
            int lastLinkId = oldMessage.records.get(oldMessage.records.size() - 1).id;

            Collections.sort(oldMessage.records, (lhs, rhs) -> lhs.time.compareTo(rhs.time));

            int latestVisibleMessageId = messageListPresenter.getFirstVisibleItemLinkId();
            int firstVisibleItemTop = 0;
            if (isCallByMarker) {
                firstVisibleItemTop = messageListPresenter.getFirstVisibleItemTop();
            } else {
                // if has no first item...
                messageListPresenter.setEmptyView();
                messageState.setLastUpdateLinkId(lastLinkId);
            }


            messageListPresenter.addAll(0, oldMessage.records);

            if (latestVisibleMessageId > 0) {
                messageListPresenter.moveToMessage(latestVisibleMessageId, firstVisibleItemTop);
            } else {
                // if has no first item...

                int messageId = -1;
                for (ResMessages.Link record : oldMessage.records) {
                    if (record.id == linkId) {
                        messageId = record.messageId;
                    }
                }
                if (messageId > 0) {
                    int yPosition = context.getResources().getDisplayMetrics().heightPixels * 2 / 5;
                    messageListPresenter.moveToMessage(messageId, yPosition);
                } else {
                    messageListPresenter.moveToMessage(oldMessage.records.get(oldMessage.records.size() - 1).messageId, firstVisibleItemTop);
                }
            }

            if (!isFirstMessage) {
                messageListPresenter.setOldLoadingComplete();
            } else {
                messageListPresenter.setOldNoMoreLoading();
            }

        } catch (JandiNetworkException e) {
            logger.debug(e.getErrorInfo() + " : " + e.httpBody, e);
        } finally {
            messageListPresenter.dismissProgressWheel();
        }

    }
}
