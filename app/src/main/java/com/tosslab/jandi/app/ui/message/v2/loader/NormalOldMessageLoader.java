package com.tosslab.jandi.app.ui.message.v2.loader;

import android.content.Context;

import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
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
    public ResMessages load(int roomId, int linkId) {
        ResMessages oldMessage = null;
        try {

            int itemCount = Math.min(
                    Math.max(MessageManipulator.NUMBER_OF_MESSAGES, messageListPresenter.getItemCount()),
                    MessageManipulator.MAX_OF_MESSAGES);

            if (roomId > 0) {
                // 저장된 정보를 가져옴
                List<ResMessages.Link> oldMessages = MessageRepository.getRepository().getOldMessages(roomId, linkId, itemCount);
                if (oldMessages != null && oldMessages.size() > 0) {


                    int firstLinkId = oldMessages.get(oldMessages.size() - 1).id;
                    messageState.setFirstItemId(firstLinkId);

                    oldMessage = new ResMessages();
                    // 현재 챗의 첫 메세지가 아니라고 하기 위함
                    oldMessage.firstLinkId = -1;
                    // 마커 업로드를 하지 않기 위함
                    if (linkId != -1) {
                        oldMessage.lastLinkId = Integer.MAX_VALUE;
                    } else {
                        oldMessage.lastLinkId = oldMessages.get(0).id;
                    }
                    oldMessage.entityId = roomId;
                    oldMessage.records = oldMessages;
                }
            }

            if (oldMessage == null) {
                // 캐시가 없는 경우
                oldMessage = messageListModel.getOldMessage(linkId, itemCount);
                messageListModel.upsertMessages(oldMessage);
            } else if (oldMessage.records.size() < itemCount) {
                try {
                    // 캐시된 데이터가 부족한 경우
                    ResMessages addOldMessage =
                            messageListModel.getOldMessage(oldMessage.records.get(0).id, itemCount);

                    messageListModel.upsertMessages(addOldMessage);

                    addOldMessage.records.addAll(oldMessage.records);

                    oldMessage = addOldMessage;
                } catch (RetrofitError retrofitError) {
                    retrofitError.printStackTrace();
                }
            }

            if (oldMessage.records == null || oldMessage.records.isEmpty()) {
                checkItemCountIfException(linkId);
                return oldMessage;
            }

            Collections.sort(oldMessage.records, (lhs, rhs) -> lhs.time.compareTo(rhs.time));

            int firstLinkIdInMessage = oldMessage.records.get(0).id;
            messageState.setFirstItemId(firstLinkIdInMessage);
            boolean isFirstMessage = oldMessage.firstLinkId == firstLinkIdInMessage;
            messageState.setFirstMessage(isFirstMessage);

            int lastLinkIdInMessage = oldMessage.records.get(oldMessage.records.size() - 1).id;
            if (oldMessage.lastLinkId <= lastLinkIdInMessage) {
                updateMarker(teamId, oldMessage.entityId, lastLinkIdInMessage);
            }

            if (linkId == -1) {

                messageListPresenter.dismissLoadingView();
                messageListPresenter.clearMessages();

                messageListPresenter.addAll(0, oldMessage.records);
                messageListPresenter.moveLastPage();

                List<ResMessages.Link> dummyMessages = messageListModel.getDummyMessages(roomId);
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
