package com.tosslab.jandi.app.ui.maintab.chat.presenter;

import android.content.Context;

import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.ui.maintab.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainChatListPresenterImpl implements MainChatListPresenter {

    @Bean
    MainChatListModel mainChatListModel;

    private View view;


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
            List<ChatItem> savedChatList = mainChatListModel.getSavedChatList(context, teamId);
            view.setChatItems(savedChatList);
        }
        try {
            List<ResChat> chatList = mainChatListModel.getChatList(memberId);
            List<ChatItem> chatItems = mainChatListModel.convertChatItems(context, teamId, chatList);
            mainChatListModel.saveChatList(context, teamId, chatItems);
            view.setChatItems(chatItems);

            view.setSelectedItem(selectedEntity);

            int selectedEntityPosition = 0;
            int size = chatItems.size();
            for (int idx = 0; idx < size; idx++) {
                if (chatItems.get(idx).getEntityId() == selectedEntity) {
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


        Observable.create(new Observable.OnSubscribe<ResChat>() {
            @Override
            public void call(Subscriber<? super ResChat> subscriber) {

                List<ResChat> chatList = mainChatListModel.getChatList(memberId);

                for (ResChat resChat : chatList) {
                    subscriber.onNext(resChat);
                }

                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .map(resChat -> mainChatListModel.convertChatItem(context, teamId, resChat))
                .collect((Func0<ArrayList<ChatItem>>) ArrayList::new, ArrayList::add)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatItems -> {
                    view.setChatItems(chatItems);
                    boolean hasAlarmCount = mainChatListModel.hasAlarmCount(chatItems);

                    EventBus.getDefault().post(new ChatBadgeEvent(hasAlarmCount));

                }, Throwable::printStackTrace);

    }

    @Override
    public void onMoveDirectMessage(Context context, int entityId) {
        EntityManager entityManager = EntityManager.getInstance(context);
        int roomId = mainChatListModel.getRoomId(context, entityManager.getTeamId(), entityId);

        view.moveMessageActivity(entityManager.getTeamId()
                , entityId
                , roomId
                , mainChatListModel.isStarred(context, entityId));
    }

    @Override
    public void onEntityItemClick(Context context, int position) {
        ChatItem chatItem = view.getChatItem(position);

        int unread = chatItem.getUnread();
        chatItem.unread(0);
        view.refreshListView();

        view.setSelectedItem(chatItem.getEntityId());
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
                .getRoomId(), isStarred);

    }
}
