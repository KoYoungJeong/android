package com.tosslab.jandi.app.ui.maintab.topic.dialog.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by Steve SeongUg Jung on 15. 1. 7..
 */
@EBean
public class EntityMenuDialogModel {

    @RootContext
    Context context;

    @Bean
    JandiEntityClient jandiEntityClient;

    public FormattedEntity getEntity(int entityId) {
        return ((JandiApplication) context.getApplicationContext()).getEntityManager().getEntityById(entityId);
    }

    public void requestStarred(int entityId) throws JandiNetworkException {
        jandiEntityClient.enableFavorite(entityId);
    }

    public void requestUnstarred(int entityId) throws JandiNetworkException {
        jandiEntityClient.disableFavorite(entityId);
    }

    public void requestLeaveEntity(int entityId, boolean publicTopic) throws JandiNetworkException {

        if (publicTopic) {
            jandiEntityClient.leaveChannel(entityId);
        } else {
            jandiEntityClient.leavePrivateGroup(entityId);
        }

    }

    public void refreshEntities() throws JandiNetworkException {
        ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
        ((JandiApplication) context.getApplicationContext()).setEntityManager(new EntityManager(totalEntitiesInfo));
    }
}
