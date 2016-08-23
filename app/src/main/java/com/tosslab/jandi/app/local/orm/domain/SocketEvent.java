package com.tosslab.jandi.app.local.orm.domain;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;

@DatabaseTable(tableName = "socket_event_log")
public class SocketEvent {
    @DatabaseField
    private long ts;
    @DatabaseField
    private String event;
    @DatabaseField(id = true)
    private String unique;
    @DatabaseField
    private long teamId;

    public static SocketEvent createEvent(EventHistoryInfo event) {
        SocketEvent socketEvent = new SocketEvent();
        socketEvent.setEvent(event.getEvent());
        socketEvent.setTs(event.getTs());
        socketEvent.setUnique(event.getUnique());
        socketEvent.setTeamId(event.getTeamId());
        return socketEvent;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }
}
