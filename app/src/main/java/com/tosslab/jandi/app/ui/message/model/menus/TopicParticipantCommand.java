package com.tosslab.jandi.app.ui.message.model.menus;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by Steve SeongUg Jung on 15. 1. 20..
 */
@EBean
public class TopicParticipantCommand implements MenuCommand {

    @RootContext
    Context context;
    private int entityId;

    @Override
    public void execute(MenuItem menuItem) {
        MembersListActivity_.intent(context)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .type(MembersListActivity.TYPE_MEMBERS_LIST_TOPIC)
                .entityId(entityId)
                .start();

    }

    public void setEntity(int entityId) {

        this.entityId = entityId;
    }
}