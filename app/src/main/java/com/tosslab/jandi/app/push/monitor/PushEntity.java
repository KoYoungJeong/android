package com.tosslab.jandi.app.push.monitor;

/**
 * Created by Steve SeongUg Jung on 15. 1. 26..
 */
public class PushEntity {
    private final long entityId;

    public PushEntity(long entityId) {
        this.entityId = entityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PushEntity that = (PushEntity) o;

        return entityId == that.entityId;

    }

    @Override
    public int hashCode() {
        return (int) (entityId ^ (entityId >>> 32));
    }
}
