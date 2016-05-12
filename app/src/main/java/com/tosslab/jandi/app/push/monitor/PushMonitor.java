package com.tosslab.jandi.app.push.monitor;

import java.util.concurrent.ConcurrentHashMap;

public class PushMonitor {

    private static PushMonitor instance;

    private ConcurrentHashMap<Long, PushEntity> pushMap;

    private PushMonitor() {
        this.pushMap = new ConcurrentHashMap<>();
    }

    public static PushMonitor getInstance() {
        if (instance == null) {
            instance = new PushMonitor();
        }

        return instance;
    }

    public void register(long entityId) {
        pushMap.put(entityId, new PushEntity(entityId));
    }

    public void unregister(long entityId) {
        pushMap.remove(entityId);
    }

    public boolean hasEntityId(long entityId) {
        return pushMap.containsKey(entityId);
    }

}
