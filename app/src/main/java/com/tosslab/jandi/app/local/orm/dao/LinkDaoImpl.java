package com.tosslab.jandi.app.local.orm.dao;

import android.text.TextUtils;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

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
    public int create(ResMessages.Link data) throws SQLException {

        if (TextUtils.equals(data.status, "event")) {

            data.eventType = getEventType(data.info);
            upsertEventInfo(data.info, getConnectionSource());
        } else {
            upsertMessage(data, getConnectionSource());
        }
        return super.create(data);
    }

    @Override
    public int update(ResMessages.Link data) throws SQLException {

        if (TextUtils.equals(data.status, "event")) {

            data.eventType = getEventType(data.info);
            upsertEventInfo(data.info, getConnectionSource());
        } else {
            upsertMessage(data, getConnectionSource());
        }

        return super.update(data);
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

        return ResMessages.EventType.NONE.name();
    }

    private void upsertEventInfo(ResMessages.EventInfo info, ConnectionSource connectionSource) throws
            SQLException {

        if (info instanceof ResMessages.CreateEvent) {

            DaoManager.createDao(connectionSource, ResMessages.CreateEvent.class)
                    .createOrUpdate((ResMessages.CreateEvent) info);

        } else if (info instanceof ResMessages.JoinEvent) {
            DaoManager.createDao(connectionSource, ResMessages.JoinEvent.class)
                    .createOrUpdate((ResMessages.JoinEvent) info);

        } else if (info instanceof ResMessages.InviteEvent) {
            DaoManager.createDao(connectionSource, ResMessages.InviteEvent.class)
                    .createOrUpdate((ResMessages.InviteEvent) info);

        } else if (info instanceof ResMessages.LeaveEvent) {
            DaoManager.createDao(connectionSource, ResMessages.LeaveEvent.class)
                    .createOrUpdate((ResMessages.LeaveEvent) info);

        } else if (info instanceof ResMessages.AnnouncementCreateEvent) {
            DaoManager.createDao(connectionSource, ResMessages.AnnouncementCreateEvent.class)
                    .createOrUpdate((ResMessages.AnnouncementCreateEvent) info);

        } else if (info instanceof ResMessages.AnnouncementDeleteEvent) {

            DaoManager.createDao(connectionSource, ResMessages.AnnouncementDeleteEvent.class)
                    .createOrUpdate((ResMessages.AnnouncementDeleteEvent) info);

        } else if (info instanceof ResMessages.AnnouncementUpdateEvent) {
            DaoManager.createDao(connectionSource, ResMessages.AnnouncementUpdateEvent.class)
                    .createOrUpdate((ResMessages.AnnouncementUpdateEvent) info);

        }

    }

    private void upsertMessage(ResMessages.Link message, ConnectionSource connectionSource) throws
            SQLException {
        ResMessages.OriginalMessage contentMessage = message.message;

        if (contentMessage instanceof ResMessages.TextMessage) {
            message.messageType = ResMessages.MessageType.TEXT.name();

            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) contentMessage;

            Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao
                    (connectionSource, ResMessages
                            .OriginalMessage.IntegerWrapper.class);
            DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder
                    = dao.deleteBuilder();

            deleteBuilder.where().eq("textOf_id", textMessage.id);
            deleteBuilder.delete();

            for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : textMessage.shareEntities) {
                shareEntity.setTextOf(textMessage);
                dao.create(shareEntity);
            }

            Dao<MentionObject, ?> mentionObjectDao = DaoManager.createDao(connectionSource, MentionObject.class);
            DeleteBuilder<MentionObject, ?> mentionObjectDeleteBuilder = mentionObjectDao.deleteBuilder();
            mentionObjectDeleteBuilder.where().eq("textOf_id", textMessage.id);
            mentionObjectDeleteBuilder.delete();

            for (MentionObject mention : textMessage.mentions) {
                mention.setTextOf(textMessage);
                mentionObjectDao.create(mention);
            }

            Dao<ResMessages.TextContent, ?> textContentDao = DaoManager.createDao
                    (connectionSource, ResMessages.TextContent
                            .class);
            textContentDao.create(textMessage.content);

            if (message.hasLinkPreview()) {
                Dao<ResMessages.LinkPreview, ?> linkPreviewDao = DaoManager.createDao
                        (connectionSource, ResMessages
                                .LinkPreview.class);
                linkPreviewDao.createOrUpdate(textMessage.linkPreview);
            }

            DaoManager.createDao(connectionSource, ResMessages.TextMessage.class).createOrUpdate
                    (textMessage);

        } else if (contentMessage instanceof ResMessages.StickerMessage) {
            message.messageType = ResMessages.MessageType.STICKER.name();
            ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) contentMessage;

            Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao
                    (connectionSource, ResMessages
                            .OriginalMessage.IntegerWrapper.class);
            DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("stickerOf_id", stickerMessage.id);
            deleteBuilder.delete();
            for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : stickerMessage.shareEntities) {
                shareEntity.setStickerOf(stickerMessage);
                dao.create(shareEntity);
            }


            DaoManager.createDao(connectionSource, ResMessages.StickerContent.class)
                    .createOrUpdate(stickerMessage.content);
            DaoManager.createDao(connectionSource, ResMessages.StickerMessage.class)
                    .createOrUpdate(stickerMessage);

        } else if (contentMessage instanceof ResMessages.FileMessage) {
            message.messageType = ResMessages.MessageType.FILE.name();

            upsertFileMessage((ResMessages.FileMessage) contentMessage, connectionSource);
        } else if (contentMessage instanceof ResMessages.CommentStickerMessage) {
            message.messageType = ResMessages.MessageType.COMMENT_STICKER.name();
            ResMessages.CommentStickerMessage stickerMessage = (ResMessages.CommentStickerMessage) contentMessage;

            Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao
                    (connectionSource, ResMessages
                            .OriginalMessage.IntegerWrapper.class);
            DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("commentStickerOf_id", stickerMessage.id);

            for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : stickerMessage.shareEntities) {
                shareEntity.setCommentStickerOf(stickerMessage);
                dao.create(shareEntity);
            }
            DaoManager.createDao(connectionSource, ResMessages.StickerContent.class)
                    .createOrUpdate(stickerMessage.content);
            DaoManager.createDao(connectionSource, ResMessages.CommentStickerMessage.class)
                    .createOrUpdate(stickerMessage);

            upsertFileMessage(message.feedback, connectionSource);

        } else if (contentMessage instanceof ResMessages.CommentMessage) {
            message.messageType = ResMessages.MessageType.COMMENT.name();

            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) contentMessage;

            Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao
                    (connectionSource, ResMessages
                            .OriginalMessage.IntegerWrapper.class);
            DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("commentOf_id", commentMessage.id);
            deleteBuilder.delete();

            for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : commentMessage.shareEntities) {
                shareEntity.setCommentOf(commentMessage);
                dao.create(shareEntity);
            }

            Dao<MentionObject, ?> mentionObjectDao = DaoManager.createDao(connectionSource, MentionObject.class);
            DeleteBuilder<MentionObject, ?> mentionObjectDeleteBuilder = mentionObjectDao.deleteBuilder();
            mentionObjectDeleteBuilder.where().eq("textOf_id", commentMessage.id);
            mentionObjectDeleteBuilder.delete();

            for (MentionObject mention : commentMessage.mentions) {
                mention.setCommentOf(commentMessage);
                mentionObjectDao.create(mention);
            }

            DaoManager.createDao(connectionSource, ResMessages.TextContent.class).createOrUpdate
                    (commentMessage.content);
            DaoManager.createDao(connectionSource, ResMessages.CommentMessage.class)
                    .createOrUpdate(commentMessage);

            upsertFileMessage(message.feedback, connectionSource);

        }
    }

    private void upsertFileMessage(ResMessages.FileMessage fileMessage, ConnectionSource connectionSource)
            throws SQLException {

        Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao
                (connectionSource, ResMessages
                        .OriginalMessage
                        .IntegerWrapper.class);
        DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq("fileOf_id", fileMessage.id);
        deleteBuilder.delete();

        for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : fileMessage.shareEntities) {
            shareEntity.setFileOf(fileMessage);
            dao.create(shareEntity);
        }


        DaoManager.createDao(connectionSource, ResMessages.FileContent.class).createOrUpdate
                (fileMessage.content);
        DaoManager.createDao(connectionSource, ResMessages.ThumbnailUrls.class).createOrUpdate
                (fileMessage.content
                        .extraInfo);
        DaoManager.createDao(connectionSource, ResMessages.FileMessage.class).createOrUpdate
                (fileMessage);
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
