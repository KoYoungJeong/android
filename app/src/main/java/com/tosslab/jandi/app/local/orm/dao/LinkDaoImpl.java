package com.tosslab.jandi.app.local.orm.dao;

import android.text.TextUtils;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class LinkDaoImpl extends BaseDaoImpl<ResMessages.Link, Integer> {

    public LinkDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.Link.class);
    }

    @Override
    public List<ResMessages.Link> query(PreparedQuery<ResMessages.Link> preparedQuery) throws SQLException {
        List<ResMessages.Link> links = super.query(preparedQuery);

        for (ResMessages.Link link : links) {
            if (link.info == null) {
                continue;
            }

            link.info = getEventInfo(link.info, link.eventType);
            queryIfCreateEvent(link.info);

        }

        return links;
    }

    private void queryIfCreateEvent(ResMessages.EventInfo info) throws SQLException {
        if (!(info instanceof ResMessages.CreateEvent)) {
            return;
        }

        ResMessages.CreateEvent createEvent = (ResMessages.CreateEvent) info;
        if (TextUtils.equals(createEvent.createType, ResMessages.PublicCreateInfo.class.getSimpleName() )) {
            ResMessages.PublicCreateInfo createInfo =
                    DaoManager.createDao(getConnectionSource(), ResMessages.PublicCreateInfo.class)
                            .queryBuilder()
                            .where()
                            .eq("_id", createEvent.createInfo._id)
                            .queryForFirst();

            createEvent.createInfo = createInfo;
        } else {
            ResMessages.PrivateCreateInfo createInfo =
                    DaoManager.createDao(getConnectionSource(), ResMessages.PrivateCreateInfo.class)
                            .queryBuilder()
                            .where()
                            .eq("_id", createEvent.createInfo._id)
                            .queryForFirst();
            createEvent.createInfo = createInfo;
        }
    }

    private ResMessages.EventInfo getEventInfo(ResMessages.EventInfo info, String rawEventType) throws SQLException {

        ResMessages.EventType eventType = ResMessages.EventType.valueOf(rawEventType);

        switch (eventType) {
            case CREATE:
                return DaoManager.createDao(getConnectionSource(), ResMessages.CreateEvent.class)
                        .queryBuilder()
                        .where()
                        .eq("_id", info._id)
                        .queryForFirst();
            case JOIN:
                return DaoManager.createDao(getConnectionSource(), ResMessages.JoinEvent.class)
                        .queryBuilder()
                        .where()
                        .eq("_id", info._id)
                        .queryForFirst();
            case INVITE:
                return DaoManager.createDao(getConnectionSource(), ResMessages.InviteEvent.class)
                        .queryBuilder()
                        .where()
                        .eq("_id", info._id)
                        .queryForFirst();
            case LEAVE:
                return DaoManager.createDao(getConnectionSource(), ResMessages.LeaveEvent.class)
                        .queryBuilder()
                        .where()
                        .eq("_id", info._id)
                        .queryForFirst();
            case ANNOUNCE_CREATE:
                return DaoManager.createDao(getConnectionSource(), ResMessages.AnnouncementCreateEvent.class)
                        .queryBuilder()
                        .where()
                        .eq("_id", info._id)
                        .queryForFirst();
            case ANNOUNCE_UPDATE:
                return DaoManager.createDao(getConnectionSource(), ResMessages.AnnouncementUpdateEvent.class)
                        .queryBuilder()
                        .where()
                        .eq("_id", info._id)
                        .queryForFirst();
            case ANNOUNCE_DELETE:
                return DaoManager.createDao(getConnectionSource(), ResMessages.AnnouncementDeleteEvent.class)
                        .queryBuilder()
                        .where()
                        .eq("_id", info._id)
                        .queryForFirst();
            default:
            case NONE:
                return info;
        }

    }

    @Override
    public ResMessages.Link queryForFirst(PreparedQuery<ResMessages.Link> preparedQuery) throws SQLException {
        return super.queryForFirst(preparedQuery);
    }
}
