package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class ReqMessageSearchQeury {
    private final int teamId;
    private final String q;
    private final int page;
    private final int perPage;
    private final int writerId;
    private final int entityId;

    public ReqMessageSearchQeury(int teamId, String q, int page, int perPage, int writerId, int entityId) {
        this.teamId = teamId;
        this.q = q;
        this.page = page;
        this.perPage = perPage;
        this.writerId = writerId;
        this.entityId = entityId;
    }

    public String getQ() {
        return q;
    }

    public int getPage() {
        return page;
    }

    public int getPerPage() {
        return perPage;
    }

    public int getWriterId() {
        return writerId;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getTeamId() {
        return teamId;
    }
}
