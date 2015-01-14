package com.tosslab.jandi.app.ui.maintab.chat;

import android.content.Context;
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

    MainChatListAdapter mainChatListAdapter;


    @AfterInject
    void initObject() {
        mainChatListAdapter = new MainChatListAdapter(context);
    }

    @AfterViews
    void initViews() {
        chatListView.setAdapter(mainChatListAdapter);
    }

    @UiThread
    public void setChatItems(List<ChatItem> chatItems) {
        mainChatListAdapter.setChatItem(chatItems);
        mainChatListAdapter.notifyDataSetChanged();
    }
}
