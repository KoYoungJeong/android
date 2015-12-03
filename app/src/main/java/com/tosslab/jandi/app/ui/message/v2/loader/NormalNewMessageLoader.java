package com.tosslab.jandi.app.ui.message.v2.loader;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.exception.ExceptionData;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.DateComparatorUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
@EBean
public class NormalNewMessageLoader implements NewsMessageLoader {

    public static final String TAG = NormalNewMessageLoader.class.getSimpleName();
    MessageListModel messageListModel;
    MessageListPresenter messageListPresenter;
    private MessageState messageState;
    private boolean firstLoad = true;
    private boolean historyLoad = true;
    private boolean cacheMode = true;

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

        LogUtil.d(TAG, "historyLoad ? " + historyLoad);

        try {
            List<ResMessages.Link> newMessage = null;
            boolean moveToLinkId = firstLoad;

            if (historyLoad) {
                try {
                    // FIXME SocketTimeoutExcpetion
                    newMessage = messageListModel.getNewMessage(linkId);
                } catch (RetrofitError retrofitError) {
                    LogUtil.w(TAG, "errorUrl - " + retrofitError.getUrl());
                    retrofitError.printStackTrace();

                    if (retrofitError.getKind() == RetrofitError.Kind.NETWORK
                            && retrofitError.getCause() instanceof IOException) {

                        LogUtil.e(TAG, "IOException - " + Log.getStackTraceString(retrofitError.getCause()));

                    } else if (retrofitError.getKind() == RetrofitError.Kind.HTTP) {
                        try {
                            ExceptionData exceptionData = (ExceptionData) retrofitError.getBodyAs(ExceptionData.class);
                            LogUtil.e(TAG, "errorCode = " + exceptionData.getCode());
                            if (exceptionData.getCode() == 40017 || exceptionData.getCode() == 40018) {
                                moveToLinkId = true;
                                newMessage = getResUpdateMessages(linkId);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                moveToLinkId = true;
                newMessage = getResUpdateMessages(linkId);
            }

            if (newMessage == null || newMessage.isEmpty()) {
                // 메세지가 없다면 종료시킴
                messageListPresenter.showEmptyViewIfNeed();
                return;
            }

            List<ResMessages.Link> messages = newMessage;
            if (messages != null && !messages.isEmpty()) {
                saveToDatabase(roomId, messages);

                Collections.sort(messages, (lhs, rhs) -> lhs.time.compareTo(rhs.time));
                int lastLinkId = newMessage.get(newMessage.size() - 1).id;
                messageState.setLastUpdateLinkId(lastLinkId);
                messageListModel.upsertMyMarker(messageListPresenter.getRoomId(), lastLinkId);
                updateMarker(roomId);

                messageListPresenter.setUpNewMessage(messages, messageListModel.getMyId(), linkId, moveToLinkId);
            } else {
                if (firstLoad && messageListPresenter.isLastOfLastReadPosition()) {
                    messageListPresenter.setLastReadLinkId(-1);
                    messageListPresenter.justRefresh();
                }
            }
            firstLoad = false;

            messageListPresenter.showEmptyViewIfNeed();
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<ResMessages.Link> getResUpdateMessages(final int linkId) {
        List<ResMessages.Link> messages = new ArrayList<>();

        Observable.create(new Observable.OnSubscribe<ResMessages>() {
            @Override
            public void call(Subscriber<? super ResMessages> subscriber) {

                // 300 개씩 요청함
                messageListPresenter.setMoreNewFromAdapter(false);

                ResMessages afterMarkerMessage = null;
                try {
                    afterMarkerMessage = messageListModel.getAfterMarkerMessage(linkId, MessageManipulator.MAX_OF_MESSAGES);
                    int messageCount = afterMarkerMessage.records.size();
                    boolean isEndOfRequest = messageCount < MessageManipulator.MAX_OF_MESSAGES;
                    if (isEndOfRequest) {
                        ResMessages.Link lastItem;
                        if (messageCount == 0) {
                            // 기존 리스트에서 마지막 링크 정보 가져옴
                            lastItem = messageListPresenter.getLastItemWithoutDummy();
                        } else {
                            lastItem = afterMarkerMessage.records.get(messageCount - 1);
                            // 새로 불러온 정보에서 마지막 링크 정보 가져옴
                        }
                        if (lastItem != null) {
                            if (DateComparatorUtil.isBefore30Days(lastItem.time)) {
                                // 마지막 링크가 30일 이전이면 히스토리 로드 하지 않기
                                historyLoad = false;
                            } else {
                                historyLoad = true;
                            }
                        } else {
                            // 알 수 없는 경우에도 히스토리 로드 하지 않기
                            historyLoad = false;
                        }
                        messageListPresenter.setNewNoMoreLoading();
                    } else {
                        messageListPresenter.setMoreNewFromAdapter(true);
                        messageListPresenter.setNewLoadingComplete();
                    }
                } catch (RetrofitError retrofitError) {
                    retrofitError.printStackTrace();
                    messageListPresenter.setMoreNewFromAdapter(true);
                    messageListPresenter.setNewLoadingComplete();
                }

                subscriber.onNext(afterMarkerMessage);
                subscriber.onCompleted();
            }
        }).collect(() -> messages,
                (resUpdateMessages, o) -> messages.addAll(o.records))
                .subscribe(resUpdateMessages -> {
                }, Throwable::printStackTrace);
        return messages;
    }

    private void saveToDatabase(int roomId, List<ResMessages.Link> messages) {

        if (!cacheMode) {
            return;
        }

        Observable.from(messages)
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

    public void setCacheMode(boolean cacheMode) {
        this.cacheMode = cacheMode;
    }
}
