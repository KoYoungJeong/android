package com.tosslab.jandi.app.push.monitor;

import android.support.v4.app.NotificationCompat;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Steve SeongUg Jung on 15. 1. 26..
 */
public class PushMonitor {

    private static PushMonitor instance;

    private ConcurrentHashMap<Integer, PushEntity> pushMap;

    private String lastNotifiedCreatedAt;
    private NotificationCompat.Builder lastNotificationBuilder;

    private PushMonitor() {
        this.pushMap = new ConcurrentHashMap<Integer, PushEntity>();
    }

    public static PushMonitor getInstance() {
        if (instance == null) {
            instance = new PushMonitor();
        }

        return instance;
    }

    public void register(int entityId) {
        pushMap.put(entityId, new PushEntity(entityId));
    }

    public void unregister(int entityId) {
        pushMap.remove(entityId);
    }

    public boolean hasEntityId(int entityId) {
        return pushMap.containsKey(entityId);
    }

    public String getLastNotifiedCreatedAt() {
        return lastNotifiedCreatedAt;
    }

    public void setLastNotifiedCreatedAt(String lastNotifiedCreatedAt) {
        this.lastNotifiedCreatedAt = lastNotifiedCreatedAt;
    }

    public void setLastNotificationBuilder(NotificationCompat.Builder notificationBuilder) {

        this.lastNotificationBuilder = notificationBuilder;
    }

    public NotificationCompat.Builder getLastNotificationBuilder() {
        return lastNotificationBuilder;
    }
}
