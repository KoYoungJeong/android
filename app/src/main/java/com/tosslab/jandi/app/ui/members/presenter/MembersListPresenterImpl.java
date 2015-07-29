package com.tosslab.jandi.app.ui.members.presenter;

import android.support.v7.app.AppCompatActivity;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.model.MembersModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Tee on 15. x. x..
 */

@EBean
public class MembersListPresenterImpl implements MembersListPresenter {

    @RootContext
    AppCompatActivity myActivity;
    @Bean
    MembersModel memberModel;
    private View view;

    @AfterViews
    void initViews() {
        initMemberList();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void initMemberList() {
        List<ChatChooseItem> members;
        if (view.getType() == MembersListActivity.TYPE_MEMBERS_LIST_TEAM) {
            members = memberModel.getTeamMembers();
        } else {
            members = memberModel.getTopicMembers(view.getEntityId());
        }
        view.showListMembers(members);
    }

    @Override
    public void onEventBusRegister() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onEventBusUnregister() {
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(final RequestMoveDirectMessageEvent event) {
        EntityManager entityManager = EntityManager.getInstance(JandiApplication.getContext());
        view.moveDirectMessageActivity(entityManager.getTeamId(), event.userId, entityManager.getEntityById(event.userId).isStarred);
    }

    public void onEventMainThread(ProfileDetailEvent event) {
        int entityId = event.getEntityId();
        UserInfoDialogFragment_.builder()
                .entityId(entityId)
                .build()
                .show(myActivity.getSupportFragmentManager(), "dialog");
    }

    public void onEvent(RetrieveTopicListEvent event) {
        initMemberList();
    }

    public void setView(View view) {
        this.view = view;
    }
}