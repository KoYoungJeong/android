package com.tosslab.jandi.app.ui.members.presenter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.members.model.MembersModel;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Tee on 15. x. x..
 */

@EBean
public class MembersListPresenterImpl implements MembersListPresenter {

    @RootContext
    AppCompatActivity myActivity;

    private View view;

    @Bean
    MembersModel memberModel;

    @AfterViews
    void initObject() {
        List<ChatChooseItem> members;
        if(view.getType() == JandiConstants.TYPE_MEMBERS_LIST_TEAM) {
            members = memberModel.getTeamMembers();
        }else{
            members = memberModel.getTopicMembers(view.getEntityId());
        }
        view.showListMembers(members);
    }

    @Override
    public void onListItemClick(ChatChooseItem chatChooseItem) {
         EventBus.getDefault().post(new RequestMoveDirectMessageEvent(chatChooseItem.getEntityId()));
    }

    @Override
    public void onEventBusRegister() {
         EventBus.getDefault().register(this);
    }

    @Override
    public void onEventBusUnregister() {
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(final RequestMoveDirectMessageEvent event) {
        EntityManager entityManager = EntityManager.getInstance(myActivity.getApplicationContext());
        MessageListV2Activity_.intent(myActivity.getApplicationContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .teamId(entityManager.getTeamId())
                .entityType(JandiConstants.TYPE_DIRECT_MESSAGE)
                .roomId(-1)
                .entityId(event.userId)
                .isFavorite(entityManager.getEntityById(event.userId).isStarred)
                .isFromPush(false)
                .start();
    }

    public void onEvent(ProfileDetailEvent event) {
        int entityId = event.getEntityId();
        UserInfoDialogFragment_.builder()
                .entityId(entityId)
                .build()
                .show(myActivity.getSupportFragmentManager(), "dialog");
    }

    public void setView(View view) {
        this.view = view;
    }
}