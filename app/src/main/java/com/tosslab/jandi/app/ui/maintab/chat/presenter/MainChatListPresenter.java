package com.tosslab.jandi.app.ui.maintab.chat.presenter;

import android.content.Context;

import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 16..
 */
public interface MainChatListPresenter {

    void setView(View view);

    void onInitChatList(Context context, long selectedEntity);

    void onReloadChatList(Context context);

    void onMoveDirectMessage(Context context, int userId);

    void onEntityItemClick(Context context, int position);

    interface View {
        void refreshListView();

        boolean hasChatItems();

        List<ChatItem> getChatItems();

        void setChatItems(List<ChatItem> chatItems);

        ChatItem getChatItem(int position);

        void setSelectedItem(long selectedEntityId);

        void moveMessageActivity(long teamId, long entityId, long roomId, boolean isStarred, long lastLinkId);

        void scrollToPosition(int selectedEntityPosition);

        void startSelectedItemAnimation();
    }
}
