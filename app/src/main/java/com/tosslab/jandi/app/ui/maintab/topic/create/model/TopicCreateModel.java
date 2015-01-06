package com.tosslab.jandi.app.ui.maintab.topic.create.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class TopicCreateModel {

    public static final int TITLE_MAX_LENGTH = 60;
    @Bean
    JandiEntityClient jandiEntityClient;

    @RootContext
    Context context;

    public ResCommon createTopic(String entityName, boolean publicSelected) throws JandiNetworkException {
        if (publicSelected) {
            return jandiEntityClient.createPublicTopic(entityName);
        } else {
            return jandiEntityClient.createPrivateGroup(entityName);
        }

    }

    public void refreshEntity() throws JandiNetworkException {
        ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();
        JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(totalEntitiesInfo);

        ((JandiApplication) context.getApplicationContext()).setEntityManager(new EntityManager(totalEntitiesInfo));
    }

    public boolean isOverMaxLength(CharSequence text) {
        return text.length() > TITLE_MAX_LENGTH;
    }
}
