package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class LinkDaoImpl extends BaseDaoImpl<ResMessages.Link, Integer> {

    public LinkDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.Link.class);
    }

    @Override
    public int create(ResMessages.Link data) throws SQLException {

        ResMessages.EventInfo info = data.info;
        data.eventType = getEventType(info);

        return super.create(data);
    }

    @Override
    public int update(ResMessages.Link data) throws SQLException {
        ResMessages.EventInfo info = data.info;
        data.eventType = getEventType(info);
        return super.update(data);
    }

    private String getEventType(ResMessages.EventInfo info) {

        if (info instanceof ResMessages.CreateEvent) {
            return ResMessages.EventType.CREATE.name();
        } else if (info instanceof ResMessages.JoinEvent) {
            return ResMessages.EventType.JOIN.name();
        } else if (info instanceof ResMessages.LeaveEvent) {
            return ResMessages.EventType.LEAVE.name();
        } else if (info instanceof ResMessages.InviteEvent) {
            return ResMessages.EventType.INVITE.name();
        } else if (info instanceof ResMessages.AnnouncementUpdateEvent) {
            return ResMessages.EventType.ANNOUNCE_UPDATE.name();
        } else if (info instanceof ResMessages.AnnouncementDeleteEvent) {
            return ResMessages.EventType.ANNOUNCE_DELETE.name();
        } else if (info instanceof ResMessages.AnnouncementCreateEvent) {
            return ResMessages.EventType.ANNOUNCE_CREATE.name();
        }

        return ResMessages.EventType.UNKNOWN.name();
    }
}
