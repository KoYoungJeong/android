package com.tosslab.jandi.app.push.monitor;

/**
 * Created by Steve SeongUg Jung on 15. 1. 26..
 */
public class PushEntity {
    private final int entityId;

    public PushEntity(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PushEntity)) return false;

        PushEntity that = (PushEntity) o;

        if (entityId != that.entityId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return entityId;
    }
}
