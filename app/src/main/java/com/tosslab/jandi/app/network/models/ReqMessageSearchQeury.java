package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class ReqMessageSearchQeury {
    private final long teamId;
    private final String query;
    private final int perPage;
    private int page;
    private long writerId;
    private long entityId;

    public ReqMessageSearchQeury(long teamId, String query, int page, int perPage) {
        this.teamId = teamId;
        this.query = query;
        this.page = page;
        this.perPage = perPage;
    }

    public String getQuery() {
        return query;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPerPage() {
        return perPage;
    }

    public long getWriterId() {
        return writerId;
    }

    public ReqMessageSearchQeury writerId(long writerId) {
        this.writerId = writerId;
        return this;
    }

    public long getEntityId() {
        return entityId;
    }

    public ReqMessageSearchQeury entityId(long entityId) {
        this.entityId = entityId;
        return this;
    }

    public long getTeamId() {
        return teamId;
    }
}
