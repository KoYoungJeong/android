package com.tosslab.jandi.app.ui.message.v2.loader;

import android.support.annotation.Nullable;

import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Presenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.List;



/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
@EBean
public class NormalOldMessageLoader implements OldMessageLoader {

    MessageListModel messageListModel;
    private MessageState messageState;
    private long teamId;
    private boolean cacheMode = true;
    private MessageListV2Presenter.View view;
    private MessageListV2Presenter presenter;

    public void setView(MessageListV2Presenter.View view) {
        this.view = view;
    }

    public void setPresenter(MessageListV2Presenter presenter) {
        this.presenter = presenter;
    }

    public void setMessageListModel(MessageListModel messageListModel) {
        this.messageListModel = messageListModel;
    }

    public void setMessageState(MessageState messageState) {
        this.messageState = messageState;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    @Override
    public ResMessages load(long roomId, long linkId) {
        ResMessages oldMessage = null;
        // 모든 요청은 dummy 가 아닌 실제 데이터 기준...
//        int currentItemCount = presenter.getItemCountWithoutDummy();
//
//        if (currentItemCount > 0) {
//            view.showOldLoadProgress();
//        }
//
//        try {
//            int itemCount = Math.min(
//                    Math.max(MessageManipulator.NUMBER_OF_MESSAGES, currentItemCount),
//                    MessageManipulator.MAX_OF_MESSAGES);
//
//            oldMessage = getOldMessages(roomId, linkId, currentItemCount, itemCount);
//
//            if (oldMessage == null || oldMessage.records == null || oldMessage.records.isEmpty()) {
//                checkItemCountIfException(currentItemCount);
//                return oldMessage;
//            }
//
//            Collections.sort(oldMessage.records, (lhs, rhs) -> lhs.time.compareTo(rhs.time));
//
//            long firstLinkIdInMessage = oldMessage.records.get(0).id;
//            messageState.setFirstItemId(firstLinkIdInMessage);
//            boolean isFirstMessage = oldMessage.firstLinkId == firstLinkIdInMessage;
//            messageState.setIsFirstMessage(isFirstMessage);
//
//            if (currentItemCount <= 0) {
//                // 처음인 경우 로드된 데이터의 마지막 것으로 설정 ( New Load 와 관련있음)
//                messageState.setLastUpdateLinkId(
//                        oldMessage.records.get(oldMessage.records.size() - 1).id);
//            }
//
//            presenter.setUpOldMessage(oldMessage.records, currentItemCount, isFirstMessage);
//        } catch (RetrofitError e) {
//            e.printStackTrace();
//            checkItemCountIfException(currentItemCount);
//        } catch (Exception e) {
//            checkItemCountIfException(currentItemCount);
//        } finally {
//            view.dismissProgressWheel();
//            view.dismissLoadingView();
//            view.dismissOldLoadProgress();
//        }

        return oldMessage;
    }

    @Nullable
    private ResMessages getOldMessages(long roomId, long linkId, int currentItemCount, int itemCount) {
        ResMessages oldMessage = null;
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
                long firstLinkId = oldMessages.get(oldMessages.size() - 1).id;
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
                if (hasMessage(oldMessage)) {
                    if (oldMessage.records.get(oldMessage.records.size() - 1).id == linkId) {
//                        presenter.setLastReadLinkId(-1);
                    }
                    updateMarker(teamId, oldMessage.entityId, oldMessage.lastLinkId);
                    deleteCompletedSendingMessage(oldMessage.entityId);
                }
                // 첫 대화인 경우 해당 채팅방의 보내는 중인 메세지 캐시 데이터 삭제함
            }
            upsertMessages(oldMessage);
        } else if (oldMessage.records.size() < itemCount) {
            try {
                // 캐시된 데이터가 부족한 경우
                ResMessages.Link firstLink = oldMessage.records.get(oldMessage.records.size() - 1);
                ResMessages addOldMessage =
                        messageListModel.getOldMessage(firstLink.id, itemCount);

                upsertMessages(addOldMessage);

                addOldMessage.records.addAll(oldMessage.records);

                oldMessage = addOldMessage;
            } catch (RetrofitError retrofitError) {
                retrofitError.printStackTrace();
            }
        }
        return oldMessage;
    }

    private void deleteCompletedSendingMessage(long roomId) {
        SendMessageRepository.getRepository().deleteCompletedMessageOfRoom(roomId);
    }

    private void upsertMessages(ResMessages oldMessage) {
        if (cacheMode) {
            messageListModel.upsertMessages(oldMessage);
        }
    }

    private boolean hasMessage(ResMessages oldMessage) {
        return oldMessage != null
                && oldMessage.records != null
                && oldMessage.records.size() > 0;
    }

    @UiThread
    void checkItemCountIfException(int currentItemCount) {
        boolean hasItem = currentItemCount > 0;
        if (!hasItem) {
//            view.dismissLoadingView();
            view.showEmptyView(true);
        }
    }

    private void updateMarker(long teamId, long roomId, long lastUpdateLinkId) {
        try {
            if (lastUpdateLinkId > 0) {
                messageListModel.updateLastLinkId(lastUpdateLinkId);
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

    public void setCacheMode(boolean cacheMode) {
        this.cacheMode = cacheMode;
    }

}
