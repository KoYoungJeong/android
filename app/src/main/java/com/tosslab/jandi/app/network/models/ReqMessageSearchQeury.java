package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class ReqMessageSearchQeury {
    private final int teamId;
    private final String query;
    private final int perPage;
    private int page;
    private int writerId;
    private int entityId;

    public ReqMessageSearchQeury(int teamId, String query, int page, int perPage) {
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

    public int getWriterId() {
        return writerId;
    }

    public ReqMessageSearchQeury writerId(int writerId) {
        this.writerId = writerId;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public ReqMessageSearchQeury entityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getTeamId() {
        return teamId;
    }
}
