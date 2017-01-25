package com.tosslab.jandi.app.ui.message.v2.loader;

import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.ui.message.v2.search.presenter.MessageSearchListPresenter;

import java.util.Collections;



/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
public class MarkerOldMessageLoader implements OldMessageLoader {

    MessageListModel messageListModel;
    private MessageState messageState;
    private MessageSearchListPresenter.View view;

    public void setMessageListModel(MessageListModel messageListModel) {
        this.messageListModel = messageListModel;
    }

    public void setMessageState(MessageState messageState) {
        this.messageState = messageState;
    }

    @Override
    public ResMessages load(long roomId, long linkId) {
        ResMessages oldMessage = null;
        try {

            boolean noFirstLoad = view.getFirstVisibleItemLinkId() > 0;
            if (noFirstLoad) {
                // 일반적인 Old Message 요청
                int itemCount = Math.min(
                        Math.max(MessageManipulator.NUMBER_OF_MESSAGES, view.getItemCount()),
                        MessageManipulator.MAX_OF_MESSAGES);
                oldMessage = messageListModel.getOldMessage(linkId, itemCount);
                view.showOldLoadingProgress();
            } else {
                // 마커 기준으로 위 아래 요청
                oldMessage = messageListModel.getBeforeMarkerMessage(linkId);
            }

            if (oldMessage.records == null || oldMessage.records.isEmpty()) {
                view.dismissLoadingView();
                return oldMessage;
            }

            if (TeamInfoLoader.getInstance().isDefaultTopic(roomId)) {
                for (int i = oldMessage.records.size() - 1; i >= 0; i--) {
                    if (oldMessage.records.get(i).info instanceof ResMessages.InviteEvent
                            || oldMessage.records.get(i).info instanceof ResMessages.LeaveEvent
                            || oldMessage.records.get(i).info instanceof ResMessages.JoinEvent) {
                        oldMessage.records.remove(i);
                    }
                }
            }

            long firstLinkId = oldMessage.records.get(0).id;
            messageState.setFirstItemId(firstLinkId);
            boolean isFirstMessage = oldMessage.firstLinkId == firstLinkId;
            messageState.setIsFirstMessage(isFirstMessage);
            long lastLinkId = oldMessage.records.get(oldMessage.records.size() - 1).id;

            Collections.sort(oldMessage.records, (lhs, rhs) -> lhs.time.compareTo(rhs.time));

            long latestVisibleMessageId = view.getFirstVisibleItemLinkId();
            int firstVisibleItemTop = 0;
            if (noFirstLoad) {
                firstVisibleItemTop = view.getFirstVisibleItemTop();
            } else {
                // if has no first item...
                messageState.setLastUpdateLinkId(lastLinkId);
            }

            view.updateMarkerMessage(linkId, oldMessage, noFirstLoad,
                    isFirstMessage, latestVisibleMessageId, firstVisibleItemTop);

        } catch (RetrofitException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            view.dismissProgressWheel();
            view.dismissOldLoadingProgress();
        }

        return oldMessage;
    }

    public void setView(MessageSearchListPresenter.View view) {
        this.view = view;
    }

}
