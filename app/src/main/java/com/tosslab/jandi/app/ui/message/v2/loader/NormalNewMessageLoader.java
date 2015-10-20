package com.tosslab.jandi.app.ui.message.v2.loader;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
@EBean
public class NormalNewMessageLoader implements NewsMessageLoader {

    MessageListModel messageListModel;
    MessageListPresenter messageListPresenter;
    private MessageState messageState;
    private boolean firstLoad = true;
    private boolean historyLoad = true;

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
            // 첫 메세지로 간주함
            return;
        }

        try {
            ResUpdateMessages newMessage;
            if (historyLoad) {
                newMessage = messageListModel.getNewMessage(linkId);
            } else {
                newMessage = new ResUpdateMessages();
                newMessage.updateInfo = new ResUpdateMessages.UpdateInfo();
                newMessage.updateInfo.messages = new ArrayList<>();
                newMessage.updateInfo.messageCount = 0;

                Observable.create(new Observable.OnSubscribe<ResMessages>() {
                    @Override
                    public void call(Subscriber<? super ResMessages> subscriber) {
                        boolean isEnd = false;
                        while (!isEnd) {

                            // 300 개씩 마지막까지 요청함
                            ResMessages afterMarkerMessage = messageListModel.getAfterMarkerMessage(linkId, MessageManipulator.MAX_OF_MESSAGES);
                            subscriber.onNext(afterMarkerMessage);

                            if (afterMarkerMessage.records.size() < MessageManipulator.MAX_OF_MESSAGES) {
                                isEnd = true;
                            }

                        }
                        subscriber.onCompleted();
                        messageListPresenter.setMoreNewFromAdapter(false);
                    }
                }).collect(() -> newMessage,
                        (resUpdateMessages, o) -> newMessage.updateInfo.messages.addAll(o.records))
                        .subscribe(resUpdateMessages -> {
                            resUpdateMessages.updateInfo.messageCount = resUpdateMessages.updateInfo.messages.size();
                            if (resUpdateMessages.updateInfo.messageCount > 0) {
                                resUpdateMessages.lastLinkId = resUpdateMessages.updateInfo.messages.get(resUpdateMessages.updateInfo.messageCount - 1).id;
                            } else {
                                resUpdateMessages.lastLinkId = linkId;
                            }
                        });
            }


            List<ResMessages.Link> messages = newMessage.updateInfo.messages;
            if (messages != null && !messages.isEmpty()) {
                saveToDatabase(roomId, messages);

                Collections.sort(messages, (lhs, rhs) -> lhs.time.compareTo(rhs.time));
                messageState.setLastUpdateLinkId(newMessage.lastLinkId);
                messageListModel.upsertMyMarker(messageListPresenter.getRoomId(), newMessage.lastLinkId);
                updateMarker(roomId);

                messageListPresenter.setUpNewMessage(messages, messageListModel.getMyId(), linkId, firstLoad);
            } else {
                if (firstLoad && messageListPresenter.isLastOfLastReadPosition()) {
                    messageListPresenter.setLastReadLinkId(-1);
                    messageListPresenter.justRefresh();
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
                    if (!TextUtils.equals(message.status, "event")) {
                        if (TextUtils.equals(message.message.status, "archived")) {
                            if (!(message.message instanceof ResMessages.FileMessage)) {
                                MessageRepository.getRepository().deleteMessage(message.messageId);
                            } else {
                                MessageRepository.getRepository().upsertFileMessage((ResMessages.FileMessage) message.message);
                            }
                        } else {
                            MessageRepository.getRepository().upsertMessage(message);
                        }
                    } else {
                        MessageRepository.getRepository().upsertMessage(message);
                    }
                });
    }

    private void updateMarker(int roomId) {
        if (messageState.getLastUpdateLinkId() <= 0) {
            // 마지막 메세지 정보가 갱신되지 않은 것으로 간주함
            return;
        }
        try {
            messageListModel.updateMarker(messageState.getLastUpdateLinkId());
            messageListModel.updateMarkerInfo(AccountRepository.getRepository().getSelectedTeamId(), roomId);
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setHistoryLoad(boolean historyLoad) {
        this.historyLoad = historyLoad;
    }
}
