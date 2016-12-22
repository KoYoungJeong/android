package com.tosslab.jandi.app.ui.maintab.tabs.chat.presenter;

import android.content.Context;

import com.tosslab.jandi.app.ui.maintab.tabs.chat.to.ChatItem;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 16..
 */
public interface MainChatListPresenter {

    void initChatList(Context context, long selectedEntity);

    void onReloadChatList();

    void onMoveDirectMessage(Context context, long userId);

    void onEntityItemClick(Context context, int position);

    void onEntityStarredUpdate(long entityId);

    interface View {
        void refreshListView();

        boolean hasChatItems();

        List<ChatItem> getChatItems();

        void setChatItems(List<ChatItem> chatItems);

        ChatItem getChatItem(int position);

        void setSelectedItem(long selectedEntityId);

        void moveMessageActivity(long teamId, long entityId, long roomId, long lastLinkId);

        void scrollToPosition(int selectedEntityPosition);

        void startSelectedItemAnimation();

        void setStarred(long entityId, boolean isStarred);

        void showEmptyLayout();

        void hideEmptyLayout();

        void showNewChatInEmptyView(boolean show);
    }
}
