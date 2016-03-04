package com.tosslab.jandi.app.ui.message.v2.loader;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.network.exception.ExceptionData;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Presenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.functions.Func0;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
@EBean
public class NormalNewMessageLoader implements NewsMessageLoader {

    public static final String TAG = NormalNewMessageLoader.class.getSimpleName();
    MessageListModel messageListModel;
    MessageListV2Presenter.View view;
    MessageListV2Presenter presenter;
    private MessageState messageState;
    private boolean firstLoad = true;
    private boolean historyLoad = true;
    private boolean cacheMode = true;

    public void setMessageListModel(MessageListModel messageListModel) {
        this.messageListModel = messageListModel;
    }

    public void setView(MessageListV2Presenter.View view) {
        this.view = view;
    }

    public void setPresenter(MessageListV2Presenter presenter) {
        this.presenter = presenter;
    }

    public void setMessageState(MessageState messageState) {
        this.messageState = messageState;
    }

    @Override
    public void load(long roomId, long linkId) {
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
                showEmptyViewIfNeed();

                if (firstLoad) {
                    view.notifyDataSetChanged();
                    firstLoad = false;
                }
                return;
            }

            List<ResMessages.Link> messages = newMessage;
            saveToDatabase(roomId, messages);

            Collections.sort(messages, (lhs, rhs) -> lhs.time.compareTo(rhs.time));
            long lastLinkId = newMessage.get(newMessage.size() - 1).id;
            messageState.setLastUpdateLinkId(lastLinkId);
//            messageListModel.upsertMyMarker(presenter.getRoomId(), lastLinkId);
            updateMarker(roomId);

            firstLoad = false;

        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showEmptyViewIfNeed() {
//        int originItemCount = presenter.getItemCount();
//        int itemCountWithoutEvent = getItemCountWithoutEvent();
//        int eventCount = originItemCount - itemCountWithoutEvent;
//        if (itemCountWithoutEvent > 0 || eventCount > 1) {
//            // create 이벤트외에 다른 이벤트가 생성된 경우
//            view.setEmptyLayoutVisible(false);
//        } else {
//            // 아예 메세지가 없거나 create 이벤트 외에는 생성된 이벤트가 없는 경우
//            view.setEmptyLayoutVisible(true);
//        }
    }

    public int getItemCountWithoutEvent() {
//        int itemCount = presenter.getItemCount();
//        for (int idx = itemCount - 1; idx >= 0; --idx) {
//            if (presenter.getItemViewType(idx) == BodyViewHolder.Type.Event.ordinal()) {
//                itemCount--;
//            }
//        }
//        return itemCount;
        return 0;
    }

    private List<ResMessages.Link> getResUpdateMessages(final long linkId) {
        List<ResMessages.Link> messages = new ArrayList<>();
//
//        Observable.create(new Observable.OnSubscribe<ResMessages>() {
//            @Override
//            public void call(Subscriber<? super ResMessages> subscriber) {
//
//                // 300 개씩 요청함
//                ResMessages afterMarkerMessage = null;
//
//                try {
//                    afterMarkerMessage = messageListModel.getAfterMarkerMessage(linkId, MessageManipulator.MAX_OF_MESSAGES);
//                    int messageCount = afterMarkerMessage.records.size();
//                    boolean isEndOfRequest = messageCount < MessageManipulator.MAX_OF_MESSAGES;
//                    if (isEndOfRequest) {
//                        ResMessages.Link lastItem;
//                        if (messageCount == 0) {
//                            // 기존 리스트에서 마지막 링크 정보 가져옴
//                            lastItem = presenter.getLastItemWithoutDummy();
//                        } else {
//                            lastItem = afterMarkerMessage.records.get(messageCount - 1);
//                            // 새로 불러온 정보에서 마지막 링크 정보 가져옴
//                        }
//                        if (lastItem != null) {
//                            historyLoad = !DateComparatorUtil.isBefore30Days(lastItem.time);
//                        } else {
//                            // 알 수 없는 경우에도 히스토리 로드 하지 않기
//                            historyLoad = false;
//                        }
//                        presenter.setNewNoMoreLoading();
//                    } else {
//                        presenter.setMoreNewFromAdapter(true);
//                        presenter.setNewLoadingComplete();
//                    }
//                } catch (RetrofitError retrofitError) {
//                    retrofitError.printStackTrace();
//                    presenter.setMoreNewFromAdapter(true);
//                    presenter.setNewLoadingComplete();
//                }
//
//                subscriber.onNext(afterMarkerMessage);
//                subscriber.onCompleted();
//            }
//        }).collect(() -> messages,
//                (resUpdateMessages, o) -> messages.addAll(o.records))
//                .subscribe(resUpdateMessages -> {
//                }, Throwable::printStackTrace);
        return messages;
    }

    private void saveToDatabase(long roomId, List<ResMessages.Link> messages) {
        if (!cacheMode) {
            return;
        }

        Observable.from(messages)
                .doOnNext(link -> link.roomId = roomId)
                .doOnNext(link -> {
                    // event 가 아니고 삭제된 파일/코멘트/메세지만 처리
                    if (!TextUtils.equals(link.status, "event")
                            && TextUtils.equals(link.status, "archived")) {
                        if (!(link.message instanceof ResMessages.FileMessage)) {
                            MessageRepository.getRepository().deleteMessage(link.messageId);
                        } else {
                            MessageRepository.getRepository().upsertFileMessage((ResMessages.FileMessage) link.message);
                        }
                    }
                })
                .filter(link -> {
                    // 이벤트와 삭제된 메세지는 처리 됐으므로..
                    return TextUtils.equals(link.status, "event") || !TextUtils.equals(link.status, "archived");
                })
                .collect((Func0<List<ResMessages.Link>>) ArrayList::new, List::add)
                .subscribe(links -> {

                    List<Long> messageIds = new ArrayList<>();
                    for (ResMessages.Link link : links) {
                        messageIds.add(link.messageId);
                    }

                    // sending 메세지 삭제
                    SendMessageRepository.getRepository().deleteCompletedMessages(messageIds);

                    MessageRepository.getRepository().upsertMessages(links);
                });
    }

    private void updateMarker(long roomId) {
        if (messageState.getLastUpdateLinkId() <= 0) {
            // 마지막 메세지 정보가 갱신되지 않은 것으로 간주함
            return;
        }
        try {
            messageListModel.updateLastLinkId(messageState.getLastUpdateLinkId());
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
