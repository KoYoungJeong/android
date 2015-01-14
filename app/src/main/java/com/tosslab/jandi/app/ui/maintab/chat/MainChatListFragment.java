package com.tosslab.jandi.app.ui.maintab.chat;

import android.app.Fragment;
import android.content.Intent;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.ui.maintab.chat.model.MainChatListModel;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.ui.message.MessageListActivity_;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;

import java.util.List;

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
        getChatList();
    }

    @Background
    void getChatList() {
        int memberId = mainChatListModel.getMemberId();
        int teamId = mainChatListModel.getTeamId();
        try {
            List<ResChat> chatList = mainChatListModel.getChatList(memberId);
            List<ChatItem> chatItems = mainChatListModel.convertChatItem(teamId, chatList);
            mainChatListPresenter.setChatItems(chatItems);
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }

    @ItemClick(R.id.lv_main_chat_list)
    void onEntityItemClick(ChatItem chatItem) {
        MessageListActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .entityId(chatItem.getEntityId())
                .isFavorite(chatItem.isStarred())
                .teamId(mainChatListModel.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .start();
    }

    @Click(R.id.btn_main_chat_fab)
    void onAddClick() {

    }

}
