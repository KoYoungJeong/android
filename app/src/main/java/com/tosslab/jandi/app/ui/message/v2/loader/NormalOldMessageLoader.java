package com.tosslab.jandi.app.ui.message.v2.loader;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
public class NormalOldMessageLoader implements OldMessageLoader {

    private final Context context;
    MessageListModel messageListModel;
    MessageListPresenter messageListPresenter;
    private MessageState messageState;
    private int teamId;
    private int entityId;

    public NormalOldMessageLoader(Context context) {

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

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public ResMessages load(int linkId) {
        ResMessages oldMessage = null;
        try {

            int itemCount = messageListPresenter.getItemCount();
            oldMessage = messageListModel.getOldMessage(linkId, itemCount);

            if (oldMessage.records == null || oldMessage.records.isEmpty()) {
                checkItemCountIfException(linkId);
                return oldMessage;
            }

            int firstLinkId = oldMessage.records.get(0).id;
            messageState.setFirstItemId(firstLinkId);
            boolean isFirstMessage = oldMessage.firstLinkId == firstLinkId;
            messageState.setFirstMessage(isFirstMessage);

            Collections.sort(oldMessage.records, (lhs, rhs) -> lhs.time.compareTo(rhs.time));

            int lastLinkIdInMessage = oldMessage.records.get(oldMessage.records.size() - 1).id;
            if (oldMessage.lastLinkId <= lastLinkIdInMessage) {
                updateMarker(teamId, oldMessage.entityId, lastLinkIdInMessage);
            }


            if (linkId == -1) {

                messageListPresenter.dismissLoadingView();
                messageListPresenter.clearMessages();

                messageListPresenter.addAll(0, oldMessage.records);
                messageListPresenter.moveLastPage();

                FormattedEntity me = EntityManager.getInstance(context).getMe();
                List<ResMessages.Link> dummyMessages = messageListModel.getDummyMessages(teamId, entityId, me.getName(), me.getUserLargeProfileUrl());
                messageListPresenter.addDummyMessages(dummyMessages);

                messageState.setLastUpdateLinkId(oldMessage.lastLinkId);
                messageListPresenter.moveLastPage();

            } else {

                int latestVisibleLinkId = messageListPresenter.getFirstVisibleItemLinkId();
                int firstVisibleItemTop = messageListPresenter.getFirstVisibleItemTop();

                messageListPresenter.addAll(0, oldMessage.records);

                messageListPresenter.moveToMessage(latestVisibleLinkId, firstVisibleItemTop);
            }

            if (!isFirstMessage) {
                messageListPresenter.setOldLoadingComplete();
            } else {
                messageListPresenter.setOldNoMoreLoading();
            }

        } catch (RetrofitError e) {
            e.printStackTrace();
            checkItemCountIfException(linkId);
        } catch (Exception e) {
            checkItemCountIfException(linkId);
        } finally {
            messageListPresenter.dismissProgressWheel();
        }

        return oldMessage;

    }

    private void checkItemCountIfException(int linkId) {
        boolean hasItem = linkId > 0;
        if (!hasItem) {
            messageListPresenter.dismissLoadingView();
            messageListPresenter.showEmptyView();
        }
    }

    private void updateMarker(int teamId, int roomId, int lastUpdateLinkId) {
        try {
            if (lastUpdateLinkId > 0) {
                messageListModel.updateMarker(lastUpdateLinkId);
                messageListModel.updateMarkerInfo(teamId, roomId);
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
            LogUtil.e("set marker failed", e);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("set marker failed", e);
        }
    }

}
