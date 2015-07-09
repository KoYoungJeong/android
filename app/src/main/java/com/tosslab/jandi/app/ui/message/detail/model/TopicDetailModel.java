package com.tosslab.jandi.app.ui.message.detail.model;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.androidannotations.annotations.EBean;

@EBean
public class TopicDetailModel {


    public String getTopicName(Context context, int entityId) {

        return EntityManager.getInstance(context).getEntityById(entityId).getName();
    }

    public String getTopicDescription(Context context, int entityId) {
        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);
        ResLeftSideMenu.Entity rawEntity = entity.getEntity();
        if (entity.isPublicTopic()) {
            return ((ResLeftSideMenu.Channel) rawEntity).description;
        } else if (entity.isPrivateGroup()) {
            return ((ResLeftSideMenu.PrivateGroup) rawEntity).description;
        } else {
            return "";
        }
    }

    public int getTopicMemberCount(Context context, int entityId) {
        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);
        return entity.getMemberCount();
    }

    public boolean isStarred(Context context, int entityId) {

        return EntityManager.getInstance(context).getEntityById(entityId).isStarred;
    }
}
