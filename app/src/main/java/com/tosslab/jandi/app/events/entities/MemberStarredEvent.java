package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
public class MemberStarredEvent {

    private final long id;

    public MemberStarredEvent(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemberStarredEvent that = (MemberStarredEvent) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
