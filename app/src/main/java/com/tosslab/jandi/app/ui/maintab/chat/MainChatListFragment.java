package com.tosslab.jandi.app.ui.maintab.chat;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.AbsListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.ChatBadgeEvent;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.MainSelectTopicEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.ui.entities.EntityChooseActivity;
import com.tosslab.jandi.app.ui.entities.EntityChooseActivity_;
import com.tosslab.jandi.app.ui.maintab.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.search.main.view.SearchActivity_;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.FAButtonUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EFragment(R.layout.fragment_main_chat_list)
@OptionsMenu(R.menu.main_activity_menu)
public class MainChatListFragment extends Fragment {

    @Bean
    MainChatListPresenter mainChatListPresenter;

    @Bean
    MainChatListModel mainChatListModel;
    @FragmentArg
    int selectedEntity;

    @AfterViews
    void initViews() {
        LogUtil.d("MainChatListFragment");
        FAButtonUtil.setFAButtonController(((AbsListView) getView().findViewById(R.id.lv_main_chat_list)), getView().findViewById(R.id.btn_main_chat_fab));
    }


    @Override
    public void onResume() {
        LogUtil.d("MainChatListFragment onResume");
        super.onResume();
        EventBus.getDefault().register(this);
        getChatList();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(ProfileDetailEvent event) {
        UserInfoDialogFragment_.builder().entityId(event.getEntityId()).build().show(getFragmentManager(), "dialog");
    }

    public void onEvent(RetrieveTopicListEvent event) {
        getChatList();
    }

    public void onEvent(SocketMessageEvent event) {
        if (TextUtils.equals(event.getMessageType(), "file_comment")) {
            for (SocketMessageEvent.MessageRoom messageRoom : event.getRooms()) {
                if (TextUtils.equals(messageRoom.getType(), "chat")) {
                    getChatList();
                    return;
                }
            }
        } else {

            if (TextUtils.equals(event.getRoom().getType(), "chat")) {
                getChatList();
            }
        }
    }

    public void onEvent(MessagePushEvent event) {
        if (TextUtils.equals(event.getEntityType(), "user")) {
            getChatList();
        }
    }

    public void onEvent(RequestMoveDirectMessageEvent event) {

        EntityManager entityManager = EntityManager.getInstance(getActivity());
        int roomId = mainChatListModel.getRoomId(entityManager.getTeamId(), event.userId);

        MessageListV2Activity_.intent(getActivity())
                .teamId(entityManager.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .entityId(event.userId)
                .roomId(roomId)
                .isFavorite(entityManager.getEntityById(event.userId).isStarred)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();
    }

    @OptionsItem(R.id.action_main_search)
    void onSearchOptionSelect() {
        SearchActivity_.intent(getActivity())
                .start();
    }

    @Background
    void getChatList() {
        int memberId = mainChatListModel.getMemberId();
        int teamId = mainChatListModel.getTeamId();

        if (memberId < 0 || teamId < 0) {
            return;
        }

        if (!mainChatListPresenter.hasChatItems()) {
            List<ChatItem> savedChatList = mainChatListModel.getSavedChatList(teamId);
            mainChatListPresenter.setChatItems(savedChatList);
        }
        try {
            List<ResChat> chatList = mainChatListModel.getChatList(memberId);
            List<ChatItem> chatItems = mainChatListModel.convertChatItem(teamId, chatList);
            mainChatListModel.saveChatList(teamId, chatItems);
            mainChatListPresenter.setChatItems(chatItems);

            mainChatListPresenter.setSelectedItem(this.selectedEntity);
            boolean hasAlarmCount = MainChatListModel.hasAlarmCount(chatItems);

            EventBus.getDefault().post(new ChatBadgeEvent(hasAlarmCount));

        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onEvent(MainSelectTopicEvent event) {
        selectedEntity = event.getSelectedEntity();
        mainChatListPresenter.setSelectedItem(selectedEntity);
    }

    //TODO 메세지 진입시 네트워크 체킹 ?
    @ItemClick(R.id.lv_main_chat_list)
    void onEntityItemClick(int position) {

        ChatItem chatItem = mainChatListPresenter.getChatItem(position);
        this.selectedEntity = chatItem.getEntityId();
        mainChatListPresenter.setSelectedItem(this.selectedEntity);
        EventBus.getDefault().post(new MainSelectTopicEvent(selectedEntity));

        int unread = chatItem.getUnread();
        chatItem.unread(0);
        int badgeCount = JandiPreference.getBadgeCount(getActivity()) - unread;
        JandiPreference.setBadgeCount(getActivity(), badgeCount);
        BadgeUtils.setBadge(getActivity(), badgeCount);
        mainChatListPresenter.refreshListView();

        boolean hasAlarmCount = MainChatListModel.hasAlarmCount(mainChatListPresenter.getChatItems());
        EventBus.getDefault().post(new ChatBadgeEvent(hasAlarmCount));

        int entityId = chatItem.getEntityId();

        boolean isStarred = EntityManager.getInstance(getActivity().getApplicationContext())
                .getEntityById(entityId)
                .isStarred;

        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityId(entityId)
                .roomId(chatItem.getRoomId())
                .isFavorite(isStarred)
                .teamId(mainChatListModel.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .start();
    }

    @ItemLongClick(R.id.lv_main_chat_list)
    void onEntityLongItemClick(ChatItem chatItem) {
        EntityMenuDialogFragment_.builder()
                .entityId(chatItem.getEntityId())
                .build()
                .show(getFragmentManager(), "dialog");
    }

    @Click({R.id.btn_main_chat_fab, R.id.layout_main_chat_list_empty})
    void onAddClick() {
        EntityChooseActivity_.intent(getActivity())
                .type(EntityChooseActivity.Type.MESSAGES.name())
                .start();
        getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.ready);
    }

}
