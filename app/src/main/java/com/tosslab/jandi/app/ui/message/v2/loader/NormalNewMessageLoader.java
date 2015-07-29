package com.tosslab.jandi.app.ui.message.v2.loader;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;

import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
public class NormalNewMessageLoader implements NewsMessageLoader {

    MessageListModel messageListModel;
    MessageListPresenter messageListPresenter;
    private MessageState messageState;
    private int roomId;
    private boolean firstLoad = true;

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
    public void load(int roomId, int linkId) {
        if (linkId <= 0) {
            return;
        }

        try {
            ResUpdateMessages newMessage = messageListModel.getNewMessage(linkId);

            if (newMessage.updateInfo.messages != null && newMessage.updateInfo.messages.size() > 0) {

                saveToDatabase(roomId, newMessage.updateInfo.messages);

                int visibleLastItemPosition = messageListPresenter.getLastVisibleItemPosition();
                int lastItemPosition = messageListPresenter.getLastItemPosition();
                Collections.sort(newMessage.updateInfo.messages, (lhs, rhs) -> lhs.time.compareTo(rhs.time));
                messageListPresenter.addAll(lastItemPosition, newMessage.updateInfo.messages);
                messageState.setLastUpdateLinkId(newMessage.lastLinkId);
                messageListModel.upsertMyMarker(messageListPresenter.getRoomId(), newMessage.lastLinkId);
                updateMarker();

                ResMessages.Link lastUpdatedMessage = newMessage.updateInfo.messages.get(newMessage.updateInfo.messages.size() - 1);
                if (!firstLoad
                        && visibleLastItemPosition >= 0
                        && visibleLastItemPosition < lastItemPosition - 1
                        && !messageListModel.isMyMessage(lastUpdatedMessage.fromEntity)) {
                    messageListPresenter.showPreviewIfNotLastItem();
                } else {
                    int messageId = lastUpdatedMessage.messageId;
                    if (firstLoad) {
                        messageListPresenter.moveLastReadLink();
                    } else if (messageId <= 0) {
                        if (!messageListModel.isMyMessage(lastUpdatedMessage.fromEntity)) {
                            messageListPresenter.moveToMessageById(lastUpdatedMessage.id, 0);
                        }
                    } else {
                        messageListPresenter.moveToMessage(messageId, 0);
                    }
                }

            }

            firstLoad = false;
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveToDatabase(int roomId, List<ResMessages.Link> messages) {

        Observable.from(messages)
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribe(message -> {
                    message.roomId = roomId;
                    if (TextUtils.equals(message.status, "archived")
                            && !(message.message instanceof ResMessages.FileMessage)) {
                        MessageRepository.getRepository().deleteMessage(message.messageId);
                    } else {
                        MessageRepository.getRepository().upsertMessage(message);
                    }
                });
    }

    private void updateMarker() {
        try {
            if (messageState.getLastUpdateLinkId() > 0) {
                messageListModel.updateMarker(messageState.getLastUpdateLinkId());
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
