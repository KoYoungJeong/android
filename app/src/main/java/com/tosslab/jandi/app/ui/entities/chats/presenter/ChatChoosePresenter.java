package com.tosslab.jandi.app.ui.entities.chats.presenter;

public interface ChatChoosePresenter {

    void initMembers();

    void onSearch(String text);

    void invite();

    void onMoveChatMessage(long entityId);

    void onItemClick(int position);

    interface View {

        void moveChatMessage(long teamId, long entityId);

        void refresh();

        void MoveDisabledEntityList();
    }
}
