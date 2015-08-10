package com.tosslab.jandi.app.ui.maintab.chat.presenter;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.ui.maintab.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
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

                    int memberId = mainChatListModel.getMemberId(JandiApplication.getContext());
                    int teamId = mainChatListModel.getTeamId(JandiApplication.getContext());

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
                    boolean hasAlarmCount = mainChatListModel.hasAlarmCount(chatItems);

                    EventBus.getDefault().post(new ChatBadgeEvent(hasAlarmCount));

                }, Throwable::printStackTrace);
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Background
    @Override
    public void onInitChatList(Context context, int selectedEntity) {
        int memberId = mainChatListModel.getMemberId(context);
        int teamId = mainChatListModel.getTeamId(context);

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

            int selectedEntityPosition = 0;
            int size = chatItems.size();
            for (int idx = 0; idx < size; idx++) {
                if (chatItems.get(idx).getRoomId() == selectedEntity) {
                    selectedEntityPosition = idx;
                    break;
                }
            }

            view.scrollToPosition(selectedEntityPosition);
            boolean hasAlarmCount = mainChatListModel.hasAlarmCount(chatItems);

            EventBus.getDefault().post(new ChatBadgeEvent(hasAlarmCount));

        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onReloadChatList(Context context) {
        int memberId = mainChatListModel.getMemberId(context);
        int teamId = mainChatListModel.getTeamId(context);

        if (memberId < 0 || teamId < 0) {
            return;
        }


        publishSubject.onNext(1);
    }

    @Override
    public void onMoveDirectMessage(Context context, int entityId) {
        EntityManager entityManager = EntityManager.getInstance(context);
        int roomId = mainChatListModel.getRoomId(entityId);

        view.moveMessageActivity(entityManager.getTeamId()
                , entityId
                , roomId
                , mainChatListModel.isStarred(context, entityId), -1);
    }

    @Override
    public void onEntityItemClick(Context context, int position) {
        ChatItem chatItem = view.getChatItem(position);

        int unread = chatItem.getUnread();
        chatItem.unread(0);
        view.refreshListView();

        view.setSelectedItem(chatItem.getRoomId());
        EventBus.getDefault().post(new MainSelectTopicEvent(chatItem.getEntityId()));

        int badgeCount = JandiPreference.getBadgeCount(context) - unread;
        JandiPreference.setBadgeCount(context, badgeCount);
        BadgeUtils.setBadge(context, badgeCount);

        boolean hasAlarmCount = mainChatListModel.hasAlarmCount(view.getChatItems());
        EventBus.getDefault().post(new ChatBadgeEvent(hasAlarmCount));

        int entityId = chatItem.getEntityId();

        boolean isStarred = EntityManager.getInstance(context.getApplicationContext())
                .getEntityById(entityId)
                .isStarred;

        view.moveMessageActivity(mainChatListModel.getTeamId(context), entityId, chatItem
                .getRoomId(), isStarred, chatItem.getLastLinkId());

    }
}
