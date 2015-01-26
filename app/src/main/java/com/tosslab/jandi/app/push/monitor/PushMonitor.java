package com.tosslab.jandi.app.push.monitor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Steve SeongUg Jung on 15. 1. 26..
 */
public class PushMonitor {

    private static PushMonitor instance;

    private ConcurrentHashMap<Integer, PushEntity> pushMap;

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
}
