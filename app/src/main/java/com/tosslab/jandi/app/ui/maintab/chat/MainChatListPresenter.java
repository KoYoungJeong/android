package com.tosslab.jandi.app.ui.maintab.chat;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.chat.adapter.MainChatListAdapter;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainChatListPresenter {

    @RootContext
    Context context;

    @ViewById(R.id.lv_main_chat_list)
    ListView chatListView;

    @ViewById(R.id.layout_main_chat_list_empty)
    View emptyView;

    MainChatListAdapter mainChatListAdapter;


    @AfterInject
    void initObject() {
        mainChatListAdapter = new MainChatListAdapter(context);
    }

    @AfterViews
    void initViews() {
        chatListView.setEmptyView(emptyView);
        chatListView.setAdapter(mainChatListAdapter);
    }

    public void refreshListView() {
        mainChatListAdapter.notifyDataSetChanged();
    }

    public boolean hasChatItems() {
        return mainChatListAdapter != null && mainChatListAdapter.getCount() > 0;
    }

    public List<ChatItem> getChatItems() {
        return mainChatListAdapter.getChatItems();
    }

    @UiThread
    public void setChatItems(List<ChatItem> chatItems) {
        mainChatListAdapter.setChatItem(chatItems);
        mainChatListAdapter.notifyDataSetChanged();
    }

    public ChatItem getChatItem(int position) {
        return mainChatListAdapter.getItem(position);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setSelectedItem(int selectedEntityId) {
        for (int idx = 0, count = mainChatListAdapter.getCount(); idx < count; ++idx) {
            ChatItem item = mainChatListAdapter.getItem(idx);
            item.setSelected(item.getEntityId() == selectedEntityId);
        }
    }
}
