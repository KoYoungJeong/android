package com.tosslab.jandi.app.ui.message.v2.loader;

import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
@EBean
public class NormalOldMessageLoader implements OldMessageLoader {

    MessageListModel messageListModel;
    MessageListPresenter messageListPresenter;
    private MessageState messageState;
    private int teamId;

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

    @Override
    public ResMessages load(int roomId, int linkId) {
        ResMessages oldMessage = null;
        int currentItemCount = messageListPresenter.getItemCount();
        try {

            int itemCount = Math.min(
                    Math.max(MessageManipulator.NUMBER_OF_MESSAGES, currentItemCount),
                    MessageManipulator.MAX_OF_MESSAGES);

            if (roomId > 0) {
                // 저장된 정보를 가져옴
                List<ResMessages.Link> oldMessages;
                if (currentItemCount > 0) {
                    // 처음 로드 아니면 현재 링크 - 1 ~ 이전 itemCount 로드
                    oldMessages = MessageRepository.getRepository().getOldMessages(roomId, linkId, itemCount);
                } else {
                    // 처음 로드면 현재 링크 ~ 이전 20개 로드
                    oldMessages = MessageRepository.getRepository().getOldMessages(roomId, linkId + 1, itemCount);
                }
                if (oldMessages != null && oldMessages.size() > 0) {


                    int firstLinkId = oldMessages.get(oldMessages.size() - 1).id;
                    messageState.setFirstItemId(firstLinkId);

                    oldMessage = new ResMessages();
                    // 현재 챗의 첫 메세지가 아니라고 하기 위함
                    oldMessage.firstLinkId = -1;
                    // 마커 업로드를 하지 않기 위함
                    oldMessage.lastLinkId = oldMessages.get(0).id;
                    oldMessage.entityId = roomId;
                    oldMessage.records = oldMessages;
                }
            }

            if (oldMessage == null) {
                // 캐시가 없는 경우
                if (currentItemCount != 0) {
                    // 요청한 링크 ID 이전 값 가져오기
                    try {
                        oldMessage = messageListModel.getOldMessage(linkId, itemCount);
                    } catch (RetrofitError retrofitError) {
                        retrofitError.printStackTrace();
                    }
                } else {
                    // 첫 요청이라 판단
                    // 마커 기준 위아래 값 요청
                    oldMessage = messageListModel.getBeforeMarkerMessage(linkId);
                    if (oldMessage != null && oldMessage.records != null && oldMessage.records.size
                            () > 0) {
                        if (oldMessage.records.get(oldMessage.records.size() - 1).id == linkId) {
                            messageListPresenter.setLastReadLinkId(-1);
                        }
                    }

                }
                messageListModel.upsertMessages(oldMessage);
            } else if (oldMessage.records.size() < itemCount) {
                try {
                    // 캐시된 데이터가 부족한 경우
                    ResMessages.Link firstLink = oldMessage.records.get(oldMessage.records.size() - 1);
                    ResMessages addOldMessage =
                            messageListModel.getOldMessage(firstLink.id, itemCount);

                    messageListModel.upsertMessages(addOldMessage);

                    addOldMessage.records.addAll(oldMessage.records);

                    oldMessage = addOldMessage;
                } catch (RetrofitError retrofitError) {
                    retrofitError.printStackTrace();
                }
            }

            if (oldMessage == null || oldMessage.records == null || oldMessage.records.isEmpty()) {
                checkItemCountIfException(currentItemCount);
                return oldMessage;
            }

            Collections.sort(oldMessage.records, (lhs, rhs) -> lhs.time.compareTo(rhs.time));

            int firstLinkIdInMessage = oldMessage.records.get(0).id;
            messageState.setFirstItemId(firstLinkIdInMessage);
            boolean isFirstMessage = oldMessage.firstLinkId == firstLinkIdInMessage;
            messageState.setFirstMessage(isFirstMessage);

            if (currentItemCount <= 0) {
                // 처음인 경우 로드된 데이터의 마지막 것으로 설정 ( New Load 와 관련있음)
                messageState.setLastUpdateLinkId(
                        oldMessage.records.get(oldMessage.records.size() - 1).id);
            }

            int lastLinkIdInMessage = oldMessage.records.get(oldMessage.records.size() - 1).id;
            if (oldMessage.lastLinkId <= lastLinkIdInMessage) {
                updateMarker(teamId, oldMessage.entityId, lastLinkIdInMessage);
            }

            List<ResMessages.Link> dummyMessages;
            if (currentItemCount == 0) {
                dummyMessages = messageListModel.getDummyMessages(roomId);
            } else {
                dummyMessages = new ArrayList<>();
            }

            messageListPresenter.setUpOldMessage(linkId, oldMessage.records, currentItemCount, isFirstMessage, dummyMessages);

        } catch (RetrofitError e) {
            e.printStackTrace();
            checkItemCountIfException(currentItemCount);
        } catch (Exception e) {
            checkItemCountIfException(currentItemCount);
        } finally {
            messageListPresenter.dismissProgressWheel();
        }

        return oldMessage;

    }

    @UiThread
    void checkItemCountIfException(int currentItemCount) {
        boolean hasItem = currentItemCount > 0;
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
