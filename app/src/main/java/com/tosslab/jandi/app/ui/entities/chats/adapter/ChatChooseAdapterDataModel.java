package com.tosslab.jandi.app.ui.entities.chats.adapter;

import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import java.util.List;

public interface ChatChooseAdapterDataModel {
    boolean isEmpty();

    int getCount();

    ChatChooseItem getItem(int position);

    void add(ChatChooseItem chatChooseItem);

    void addAll(List<ChatChooseItem> chatListWithoutMe);

    void clear();

    void remove(ChatChooseItem chatChooseItem);
}
