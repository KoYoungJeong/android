package com.tosslab.jandi.app.ui.maintab.chat.presenter;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.ui.maintab.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
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
                .throttleWithTimeout(1000, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .map(integer -> {

                    long memberId = mainChatListModel.getMemberId();
                    long teamId = mainChatListModel.getTeamId();

                    List<ResChat> chatList = mainChatListModel.getChatList(memberId);
                    mainChatListModel.saveChatList(teamId, chatList);

                    return mainChatListModel.convertChatItems(JandiApplication.getContext(),
                            teamId,
                            chatList);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatItems -> {
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
            List<ResChat> savedChatList = mainChatListModel.getSavedChatList();
            List<ChatItem> chatItems = mainChatListModel.convertChatItems(context, teamId,
                    savedChatList);
            view.setChatItems(chatItems);
        }

        if (!NetworkCheckUtil.isConnected()) {
            return;
        }

        try {
            List<ResChat> chatList = mainChatListModel.getChatList(memberId);
            mainChatListModel.saveChatList(teamId, chatList);
            List<ChatItem> chatItems = mainChatListModel.convertChatItems(context, teamId,
                    chatList);
            view.setChatItems(chatItems);

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

        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onReloadChatList(Context context) {
        long memberId = mainChatListModel.getMemberId();
        long teamId = mainChatListModel.getTeamId();

        if (memberId < 0 || teamId < 0) {
            return;
        }


        publishSubject.onNext(1);
    }

    @Override
    public void onMoveDirectMessage(Context context, long entityId) {
        EntityManager entityManager = EntityManager.getInstance();
        long roomId = mainChatListModel.getRoomId(entityId);

        view.moveMessageActivity(entityManager.getTeamId(),
                entityId,
                roomId,
                mainChatListModel.isStarred(entityId), -1);
    }

    @Override
    public void onEntityItemClick(Context context, int position) {
        ChatItem chatItem = view.getChatItem(position);

        int unread = chatItem.getUnread();
        chatItem.unread(0);
        view.refreshListView();

        view.setSelectedItem(chatItem.getRoomId());
        EventBus.getDefault().post(new MainSelectTopicEvent(chatItem.getEntityId()));

        long teamId = mainChatListModel.getTeamId();
        BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
        int badgeCount = badgeCountRepository.findBadgeCountByTeamId(teamId) - unread;
        if (badgeCount <= 0) {
            badgeCount = 0;
        }
        badgeCountRepository.upsertBadgeCount(teamId, badgeCount);
        BadgeUtils.setBadge(context, badgeCountRepository.getTotalBadgeCount());

        int unreadCount = mainChatListModel.getUnreadCount(view.getChatItems());
        EventBus.getDefault().post(new ChatBadgeEvent(unreadCount > 0, unreadCount));

        long entityId = chatItem.getEntityId();

        boolean isStarred = EntityManager.getInstance()
                .getEntityById(entityId)
                .isStarred;

        view.moveMessageActivity(mainChatListModel.getTeamId(), entityId, chatItem
                .getRoomId(), isStarred, chatItem.getLastLinkId());

    }

    @Override
    public void onEntityStarredUpdate(long entityId) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        boolean isStarred = entity.isStarred;
        view.setStarred(entityId, isStarred);
    }
}
