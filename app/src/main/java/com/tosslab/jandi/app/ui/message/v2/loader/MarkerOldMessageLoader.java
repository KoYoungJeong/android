package com.tosslab.jandi.app.ui.message.v2.loader;

import android.content.Context;

import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;

import java.util.Collections;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
public class MarkerOldMessageLoader implements OldMessageLoader {

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
    public ResMessages load(int roomId, int linkId) {
        ResMessages oldMessage = null;
        try {

            boolean noFirstLoad = messageListPresenter.getFirstVisibleItemLinkId() > 0;
            if (noFirstLoad) {
                // 일반적인 Old Message 요청
                int itemCount = Math.min(
                        Math.max(MessageManipulator.NUMBER_OF_MESSAGES, messageListPresenter.getItemCount()),
                        MessageManipulator.MAX_OF_MESSAGES);
                oldMessage = messageListModel.getOldMessage(linkId, itemCount);
            } else {
                // 마커 기준으로 위 아래 요청
                oldMessage = messageListModel.getBeforeMarkerMessage(linkId);
            }


            if (oldMessage.records == null || oldMessage.records.isEmpty()) {
                messageListPresenter.dismissLoadingView();
                return oldMessage;
            }

            int firstLinkId = oldMessage.records.get(0).id;
            messageState.setFirstItemId(firstLinkId);
            boolean isFirstMessage = oldMessage.firstLinkId == firstLinkId;
            messageState.setFirstMessage(isFirstMessage);
            int lastLinkId = oldMessage.records.get(oldMessage.records.size() - 1).id;

            Collections.sort(oldMessage.records, (lhs, rhs) -> lhs.time.compareTo(rhs.time));

            int latestVisibleMessageId = messageListPresenter.getFirstVisibleItemLinkId();
            int firstVisibleItemTop = 0;
            if (noFirstLoad) {
                firstVisibleItemTop = messageListPresenter.getFirstVisibleItemTop();
            } else {
                // if has no first item...
                messageState.setLastUpdateLinkId(lastLinkId);
            }


            messageListPresenter.updateMarkerMessage(linkId, oldMessage, noFirstLoad,
                    isFirstMessage, latestVisibleMessageId, firstVisibleItemTop);

        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            messageListPresenter.dismissProgressWheel();
        }

        return oldMessage;
    }

}
