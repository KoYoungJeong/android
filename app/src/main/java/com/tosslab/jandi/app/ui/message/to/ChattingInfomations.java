package com.tosslab.jandi.app.ui.message.to;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.entities.EntityManager;

/**
 * *********************************************************
 * 채팅방 정보들.
 * **********************************************************
 */
public class ChattingInfomations {
    public int entityType;
    private final Context context;
    public int entityId;
    public boolean isMyEntity;
    public boolean isFavorite;
    public String entityName;
    public boolean willBeFinishedFromPush;

    public ChattingInfomations(Context context, int entityId, int entityType, boolean isFromPush, boolean isFavorite) {
        this.context = context;
        this.entityId = entityId;
        this.entityType = entityType;
        this.willBeFinishedFromPush = isFromPush;
        this.isFavorite = isFavorite;
        loadExtraInfo();
    }

    public void loadExtraInfo() {
        EntityManager entityManager = ((JandiApplication) context.getApplicationContext()).getEntityManager();
        if (entityManager != null) {
            this.isMyEntity = entityManager.isMyTopic(entityId);
            this.entityName = entityManager.getEntityNameById(entityId);
        }
    }

    public boolean isPublicTopic() {
        return (entityType == JandiConstants.TYPE_PUBLIC_TOPIC) ? true : false;
    }

    public boolean isPrivateTopic() {
        return (entityType == JandiConstants.TYPE_PRIVATE_TOPIC) ? true : false;
    }

    public boolean isDirectMessage() {
        return (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) ? true : false;
    }
}