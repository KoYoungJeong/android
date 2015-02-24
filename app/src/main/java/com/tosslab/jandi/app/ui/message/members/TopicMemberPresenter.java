package com.tosslab.jandi.app.ui.message.members;

import android.content.Context;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.message.members.adapter.TopicMemberAdapter;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class TopicMemberPresenter {


    @RootContext
    Context context;

    @ViewById(R.id.list_topic_member)
    ListView memberListView;

    private TopicMemberAdapter topicMemberAdapter;

    @AfterInject
    void initObject() {
        topicMemberAdapter = new TopicMemberAdapter(context);
    }

    @AfterViews
    void initViews() {
        memberListView.setAdapter(topicMemberAdapter);
    }


    @UiThread
    public void setTopicMembers(List<ChatChooseItem> topicMembers) {
        topicMemberAdapter.addAll(topicMembers);
        topicMemberAdapter.notifyDataSetChanged();
    }
}
