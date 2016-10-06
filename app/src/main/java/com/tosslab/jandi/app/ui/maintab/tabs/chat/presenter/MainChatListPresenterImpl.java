package com.tosslab.jandi.app.ui.maintab.tabs.chat.presenter;

import android.content.Context;

import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.services.socket.to.MessageReadEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.to.ChatItem;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import de.greenrobot.event.EventBus;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainChatListPresenterImpl implements MainChatListPresenter {

    @Bean
    MainChatListModel mainChatListModel;

    private View view;
    private PublishSubject<Integer> publishSubject;

    @AfterInject
    void initObject() {
        publishSubject = PublishSubject.create();

        publishSubject
                .onBackpressureBuffer()
                .map(integer -> {

                    List<DirectMessageRoom> savedChatList = mainChatListModel.getSavedChatList();
                    return mainChatListModel.convertChatItems(savedChatList);
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatItems -> {
                    if (chatItems.isEmpty()) {
                        view.showEmptyLayout();
                    } else {
                        view.hideEmptyLayout();
                    }
                    view.setChatItems(chatItems);
                    int count = mainChatListModel.getUnreadCount(chatItems);
                    boolean isBadge = count > 0;
                    EventBus.getDefault().post(new ChatBadgeEvent(isBadge, count));

                }, Throwable::printStackTrace);
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Background
    @Override
    public void initChatList(Context context, long selectedEntity) {
        long memberId = mainChatListModel.getMemberId();
        long teamId = mainChatListModel.getTeamId();

        if (memberId < 0 || teamId < 0) {
            return;
        }

        if (!view.hasChatItems()) {
            List<DirectMessageRoom> savedChatList = mainChatListModel.getSavedChatList();
            List<ChatItem> chatItems = mainChatListModel.convertChatItems(
                    savedChatList);
            if (chatItems == null || chatItems.isEmpty()) {
                view.showEmptyLayout();
            } else {
                view.hideEmptyLayout();
                view.setChatItems(chatItems);
            }

            view.setSelectedItem(selectedEntity);
            view.startSelectedItemAnimation();

            int selectedEntityPosition = 0;
            int size = chatItems.size();
            for (int idx = 0; idx < size; idx++) {
                if (chatItems.get(idx).getRoomId() == selectedEntity) {
                    selectedEntityPosition = idx;
                    break;
                }
            }

            view.scrollToPosition(selectedEntityPosition);
            int unreadCount = mainChatListModel.getUnreadCount(chatItems);

            EventBus.getDefault().post(new ChatBadgeEvent(unreadCount > 0, unreadCount));
        }
    }

    @Override
    public void onReloadChatList() {
        long memberId = mainChatListModel.getMemberId();
        long teamId = mainChatListModel.getTeamId();

        if (memberId < 0 || teamId < 0) {
            return;
        }


        publishSubject.onNext(1);
    }

    @Override
    public void onMoveDirectMessage(Context context, long entityId) {
        long roomId = mainChatListModel.getRoomId(entityId);

        view.moveMessageActivity(TeamInfoLoader.getInstance().getTeamId(),
                entityId,
                roomId,
                -1);
    }

    @Override
    public void onEntityItemClick(Context context, int position) {
        ChatItem chatItem = view.getChatItem(position);
        EventBus.getDefault().post(
                MessageReadEvent.fromSelf(mainChatListModel.getTeamId(), chatItem.getUnread()));
        chatItem.unread(0);
        view.refreshListView();

        view.setSelectedItem(chatItem.getRoomId());
        EventBus.getDefault().post(new MainSelectTopicEvent(chatItem.getEntityId()));

        int unreadCount = mainChatListModel.getUnreadCount(view.getChatItems());
        EventBus.getDefault().post(new ChatBadgeEvent(unreadCount > 0, unreadCount));

        long entityId = chatItem.getEntityId();

        view.moveMessageActivity(mainChatListModel.getTeamId(), entityId, chatItem.getRoomId(),
                chatItem.getLastLinkId());

    }

    @Override
    public void onEntityStarredUpdate(long entityId) {
        boolean isStarred = TeamInfoLoader.getInstance().isStarredUser(entityId);
        view.setStarred(entityId, isStarred);
    }
}
