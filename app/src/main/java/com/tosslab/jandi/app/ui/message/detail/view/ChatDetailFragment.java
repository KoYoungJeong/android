package com.tosslab.jandi.app.ui.message.detail.view;

import android.support.v4.app.Fragment;
import android.view.View;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.fragment_chat_detail)
public class ChatDetailFragment extends Fragment {

    @FragmentArg
    int entityId;

    @ViewById(R.id.iv_topic_detail_starred)
    View ivStarred;

    @AfterInject
    void initObject() {

    }

    @AfterViews
    void initViews() {

    }

    @Click(R.id.vg_topic_detail_starred)
    void onChatStarClick() {

    }

    @Click(R.id.vg_topic_detail_leave)
    void onChatLeaveClick() {

    }

}
