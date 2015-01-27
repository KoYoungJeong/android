package com.tosslab.jandi.app.ui.maintab.chat;

import android.app.Fragment;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.ui.entities.EntityChooseActivity;
import com.tosslab.jandi.app.ui.entities.EntityChooseActivity_;
import com.tosslab.jandi.app.ui.maintab.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.ui.maintab.topic.dialog.EntityMenuDialogFragment_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemLongClick;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EFragment(R.layout.fragment_main_chat_list)
public class MainChatListFragment extends Fragment {

    @Bean
    MainChatListPresenter mainChatListPresenter;

    @Bean
    MainChatListModel mainChatListModel;


    @Override
    public void onResume() {
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

    public void onEventMainThread(RetrieveTopicListEvent event) {
        getChatList();
    }


    public void onEvent(MessagePushEvent event) {
        if (TextUtils.equals(event.getEntityType(), "user")) {
            getChatList();
        }
    }

    @Background
    void getChatList() {
        int memberId = mainChatListModel.getMemberId();
        int teamId = mainChatListModel.getTeamId();
        List<ChatItem> savedChatList = mainChatListModel.getSavedChatList(teamId);
        mainChatListPresenter.setChatItems(savedChatList);
        try {
            List<ResChat> chatList = mainChatListModel.getChatList(memberId);
            List<ChatItem> chatItems = mainChatListModel.convertChatItem(teamId, chatList);
            mainChatListModel.saveChatList(teamId, chatItems);
            mainChatListPresenter.setChatItems(chatItems);

        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }

    @ItemClick(R.id.lv_main_chat_list)
    void onEntityItemClick(ChatItem chatItem) {
        MessageListV2Activity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityId(chatItem.getEntityId())
                .isFavorite(chatItem.isStarred())
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

    @Click({R.id.btn_main_chat_fab, R.id.btn_chat_list_no_messages})
    void onAddClick() {
        EntityChooseActivity_.intent(getActivity())
                .type(EntityChooseActivity.Type.MESSAGES.name())
                .start();
    }

}
