package com.tosslab.jandi.app.ui.entities.chats.presenter;

import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import java.util.List;

public interface ChatChoosePresenter {

    void setView(View view);

    void initMembers();

    void onSearch(String text);

    void invite();

    void onMoveChatMessage(long entityId);

    interface View {

        void setUsers(List<ChatChooseItem> users);

        void moveChatMessage(long teamId, long entityId);
    }
}
