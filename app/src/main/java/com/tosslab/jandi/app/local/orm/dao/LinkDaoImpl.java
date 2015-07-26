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
            if (TextUtils.equals(link.status, "event")) {
                link.info = getEventInfo(link.info, link.eventType);
                queryIfCreateEvent(link.info);
            } else {
                queryMessage(link);
            }
        }

        return links;
    }

    private void queryMessage(ResMessages.Link link) throws SQLException {

        if (TextUtils.isEmpty(link.messageType)) {
            return;
        }

        ResMessages.MessageType messageType = ResMessages.MessageType.valueOf(link.messageType);

        switch (messageType) {
            case TEXT: {
                link.message = DaoManager.createDao(getConnectionSource(), ResMessages.TextMessage.class)
                        .queryBuilder()
                        .where()
                        .eq("id", link.messageId)
                        .queryForFirst();
                break;
            }
            case FILE: {
                link.message = DaoManager.createDao(getConnectionSource(), ResMessages.FileMessage.class)
                        .queryBuilder()
                        .where()
                        .eq("id", link.messageId)
                        .queryForFirst();
                break;
            }
            case STICKER: {
                link.message = DaoManager.createDao(getConnectionSource(), ResMessages.StickerMessage.class)
                        .queryBuilder()
                        .where()
                        .eq("id", link.messageId)
                        .queryForFirst();
                break;
            }
            case COMMENT: {
                link.message = DaoManager.createDao(getConnectionSource(), ResMessages.CommentMessage.class)
                        .queryBuilder()
                        .where()
                        .eq("id", link.messageId)
                        .queryForFirst();

                link.feedback = DaoManager.createDao(getConnectionSource(), ResMessages
                        .FileMessage.class)
                        .queryBuilder()
                        .where()
                        .eq("id", link.feedbackId)
                        .queryForFirst();
                break;
            }
            case COMMENT_STICKER: {
                link.message = DaoManager.createDao(getConnectionSource(), ResMessages.CommentStickerMessage.class)
                        .queryBuilder()
                        .where()
                        .eq("id", link.messageId)
                        .queryForFirst();

                link.feedback = DaoManager.createDao(getConnectionSource(), ResMessages.FileMessage.class)
                        .queryBuilder()
                        .where()
                        .eq("id", link.feedbackId)
                        .queryForFirst();
                break;
            }
            case NONE:
                break;
        }
    }

    private void queryIfCreateEvent(ResMessages.EventInfo info) throws SQLException {
        if (!(info instanceof ResMessages.CreateEvent)) {
            return;
        }

        ResMessages.CreateEvent createEvent = (ResMessages.CreateEvent) info;
        if (TextUtils.equals(createEvent.createType, ResMessages.PublicCreateInfo.class.getSimpleName())) {
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
