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

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

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
            List<ResChat> savedChatList = mainChatListModel.getSavedChatList();
            List<ChatItem> chatItems = mainChatListModel.convertChatItem(context, teamId, savedChatList);
            view.setChatItems(chatItems);
        }
        try {
            List<ResChat> chatList = mainChatListModel.getChatList(memberId);
            mainChatListModel.saveChatList(teamId, chatList);
            List<ChatItem> chatItems = mainChatListModel.convertChatItem(context, teamId, chatList);
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

    @Background
    @Override
    public void onReloadChatList(Context context) {
        int memberId = mainChatListModel.getMemberId(context);
        int teamId = mainChatListModel.getTeamId(context);

        if (memberId < 0 || teamId < 0) {
            return;
        }

        try {
            List<ResChat> chatList = mainChatListModel.getChatList(memberId);
            mainChatListModel.saveChatList(teamId, chatList);
            List<ChatItem> chatItems = mainChatListModel.convertChatItem(context, teamId, chatList);
            view.setChatItems(chatItems);

            boolean hasAlarmCount = mainChatListModel.hasAlarmCount(chatItems);

            EventBus.getDefault().post(new ChatBadgeEvent(hasAlarmCount));

        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMoveDirectMessage(Context context, int entityId) {
        EntityManager entityManager = EntityManager.getInstance(context);
        int roomId = mainChatListModel.getRoomId(entityId);

        view.moveMessageActivity(entityManager.getTeamId()
                , entityId
                , roomId
                , mainChatListModel.isStarred(context, entityId));
    }

    @Override
    public void onEntityItemClick(Context context, int position) {
        ChatItem chatItem = view.getChatItem(position);
        view.setSelectedItem(chatItem.getEntityId());
        EventBus.getDefault().post(new MainSelectTopicEvent(chatItem.getEntityId()));

        int unread = chatItem.getUnread();
        chatItem.unread(0);
        view.refreshListView();

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
