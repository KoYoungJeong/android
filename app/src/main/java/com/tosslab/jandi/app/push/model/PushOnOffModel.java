package com.tosslab.jandi.app.push.model;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;

import org.androidannotations.annotations.EBean;

import retrofit.RetrofitError;

/**
 * Created by tonyjs on 15. 9. 16..
 */
@EBean
public class PushOnOffModel {

    public void updatePushStatus(int teamId, int entityId, boolean pushOn) throws RetrofitError {
        ReqUpdateTopicPushSubscribe req = new ReqUpdateTopicPushSubscribe(pushOn);
        RequestApiManager.getInstance().updateTopicPushSubscribe(teamId, entityId, req);
    }

    public boolean isPushOn(int entityId) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        return entity.isTopicPushOn;
    }
}
