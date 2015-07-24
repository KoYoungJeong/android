package com.tosslab.jandi.app.local.orm.repositories;

import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.ReadyMessage;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class MessageRepository {

    private static MessageRepository repository;

    private MessageRepository() {
    }

    public static MessageRepository getRepository() {

        if (repository == null) {
            repository = new MessageRepository();
        }
        return repository;
    }

    public boolean upsertMessages(List<ResMessages.Link> messages) {
        try {
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            Dao<ResMessages.Link, ?> dao = helper.getDao(ResMessages.Link.class);

            for (ResMessages.Link message : messages) {
                if (TextUtils.equals(message.status, "event")) {

                    message.eventType = getEventType(message.info);
                    ResMessages.EventInfo info = message.info;
                    setTypeIfCreateEvent(info);

                    upsertEventInfo(message.info, helper);
                } else if (message != null) {
                    upsertMessage(message, helper);
                }
                dao.createOrUpdate(message);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return false;
    }

    public int deleteMessage(int messageId) {

        if (messageId <= 0) {
            // 이벤트는 삭제하지 않기 위함
            return 0;
        }

        try {
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            Dao<ResMessages.Link, ?> linkDao = helper.getDao(ResMessages.Link.class);
            DeleteBuilder<ResMessages.Link, ?> deleteBuilder = linkDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("messageId", messageId);
            return deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }

        return 0;

    }

    public List<ResMessages.Link> getMessages(int roomId) {

        try {
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            int teamId = AccountRepository.getRepository().getSelectedTeamId();
            return helper.getDao(ResMessages.Link.class)
                    .queryBuilder()
                    .where()
                    .eq("teamId", teamId)
                    .and()
                    .eq("roomId", roomId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }

        return new ArrayList<>();
    }

    public boolean upsertReadyMessage(ReadyMessage readyMessage) {
        try {
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            Dao<ReadyMessage, ?> readyMessageDao = helper.getDao(ReadyMessage.class);
            readyMessageDao.createOrUpdate(readyMessage);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return false;
    }

    public int deleteReadyMessage(int roomId) {
        try {
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            Dao<ReadyMessage, ?> readyMessageDao = helper.getDao(ReadyMessage.class);
            DeleteBuilder<ReadyMessage, ?> deleteBuilder = readyMessageDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("roomId", roomId);
            return deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return 0;
    }

    public ReadyMessage getReadyMessage(int roomId) {
        try {
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            Dao<ReadyMessage, ?> readyMessageDao = helper.getDao(ReadyMessage.class);
            ReadyMessage readyMessage = readyMessageDao.queryBuilder()
                    .where()
                    .eq("roomId", roomId)
                    .queryForFirst();

            if (readyMessage == null) {
                readyMessage = new ReadyMessage();
                readyMessage.setRoomId(roomId);
                readyMessage.setText("");
            }

            return readyMessage;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }

        ReadyMessage readyMessage = new ReadyMessage();
        readyMessage.setRoomId(roomId);
        readyMessage.setText("");
        return readyMessage;
    }

    public boolean insertSendMessage(SendMessage sendMessage) {
        try {
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            Dao<SendMessage, ?> sendMessageDao = helper.getDao(SendMessage.class);
            sendMessage.setStatus(SendMessage.Status.SENDING.name());
            sendMessageDao.create(sendMessage);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }

        return false;

    }

    public List<SendMessage> getSendMessage(int roomId) {
        try {
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            Dao<SendMessage, ?> dao = helper.getDao(SendMessage.class);
            return dao.queryBuilder()
                    .where()
                    .eq("roomId", roomId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return new ArrayList<>();
    }

    public int deleteSendMessage(long id) {
        try {
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            Dao<SendMessage, ?> dao = helper.getDao(SendMessage.class);
            DeleteBuilder<SendMessage, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("id", id);
            return deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }

        return 0;
    }

    public int updateSendMessageStatus(long id, SendMessage.Status status) {
        try {
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
            Dao<SendMessage, ?> dao = helper.getDao(SendMessage.class);
            UpdateBuilder<SendMessage, ?> updateBuilder = dao.updateBuilder();
            updateBuilder.updateColumnValue("status", status.name());
            updateBuilder.where()
                    .eq("id", id);

            return updateBuilder.update();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }
        return 0;
    }


    private void upsertMessage(ResMessages.Link message, OrmDatabaseHelper helper) throws
            SQLException {
        ResMessages.OriginalMessage contentMessage = message.message;

        if (contentMessage instanceof ResMessages.TextMessage) {
            message.messageType = ResMessages.MessageType.TEXT.name();

            ResMessages.TextMessage textMessage = (ResMessages.TextMessage) contentMessage;

            Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = helper.getDao(ResMessages.OriginalMessage.IntegerWrapper.class);
            DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder
                    = dao.deleteBuilder();

            deleteBuilder.where().eq("textOf_id", textMessage.id);
            deleteBuilder.delete();

            for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : textMessage.shareEntities) {
                shareEntity.setTextOf(textMessage);
                dao.create(shareEntity);
            }


            Dao<ResMessages.TextContent, ?> textContentDao = helper.getDao(ResMessages.TextContent.class);
            textContentDao.create(textMessage.content);

            if (message.hasLinkPreview()) {
                Dao<ResMessages.LinkPreview, ?> linkPreviewDao = helper.getDao(ResMessages.LinkPreview.class);
                linkPreviewDao.create(textMessage.linkPreview);
            }

            helper.getDao(ResMessages.TextMessage.class).createOrUpdate(textMessage);

        } else if (contentMessage instanceof ResMessages.StickerMessage) {
            message.messageType = ResMessages.MessageType.STICKER.name();
            ResMessages.StickerMessage stickerMessage = (ResMessages.StickerMessage) contentMessage;

            Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = helper.getDao(ResMessages.OriginalMessage.IntegerWrapper.class);
            DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("stickerOf_id", stickerMessage.id);
            deleteBuilder.delete();
            for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : stickerMessage.shareEntities) {
                shareEntity.setStickerOf(stickerMessage);
                dao.create(shareEntity);
            }


            helper.getDao(ResMessages.StickerContent.class).createOrUpdate(stickerMessage.content);
            helper.getDao(ResMessages.StickerMessage.class).createOrUpdate(stickerMessage);

        } else if (contentMessage instanceof ResMessages.FileMessage) {
            message.messageType = ResMessages.MessageType.FILE.name();

            upsertFileMessage((ResMessages.FileMessage) contentMessage, helper);
        } else if (contentMessage instanceof ResMessages.CommentStickerMessage) {
            message.messageType = ResMessages.MessageType.COMMENT_STICKER.name();
            ResMessages.CommentStickerMessage stickerMessage = (ResMessages.CommentStickerMessage) contentMessage;

            Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = helper.getDao(ResMessages.OriginalMessage.IntegerWrapper.class);
            DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("commentStickerOf_id", stickerMessage.id);

            for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : stickerMessage.shareEntities) {
                shareEntity.setCommentStickerOf(stickerMessage);
                dao.create(shareEntity);
            }
            helper.getDao(ResMessages.StickerContent.class).createOrUpdate(stickerMessage.content);
            helper.getDao(ResMessages.CommentStickerMessage.class).createOrUpdate(stickerMessage);

        } else if (contentMessage instanceof ResMessages.CommentMessage) {
            message.messageType = ResMessages.MessageType.COMMENT.name();

            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) contentMessage;

            Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = helper.getDao(ResMessages.OriginalMessage.IntegerWrapper.class);
            DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("commentOf_id", commentMessage.id);
            deleteBuilder.delete();

            for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : commentMessage.shareEntities) {
                shareEntity.setCommentOf(commentMessage);
                dao.create(shareEntity);
            }

            helper.getDao(ResMessages.TextContent.class).create(commentMessage.content);
            helper.getDao(ResMessages.CommentMessage.class).create(commentMessage);

        }
    }

    private void upsertFileMessage(ResMessages.FileMessage fileMessage, OrmDatabaseHelper helper)
            throws SQLException {

        Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = helper.getDao(ResMessages.OriginalMessage.IntegerWrapper.class);
        DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq("fileOf_id", fileMessage.id);

        for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : fileMessage.shareEntities) {
            shareEntity.setFileOf(fileMessage);
            dao.create(shareEntity);
        }


        helper.getDao(ResMessages.FileContent.class).createOrUpdate(fileMessage.content);
        helper.getDao(ResMessages.ThumbnailUrls.class).createOrUpdate(fileMessage.content.extraInfo);
        helper.getDao(ResMessages.FileMessage.class).createOrUpdate(fileMessage);
    }

    private void setTypeIfCreateEvent(ResMessages.EventInfo info) {
        if (!(info instanceof ResMessages.CreateEvent)) {
            return;
        }

        ResMessages.CreateEvent info1 = (ResMessages.CreateEvent) info;

        if (info1.createInfo instanceof ResMessages.PublicCreateInfo) {
            info1.createType = ResMessages.PublicCreateInfo.class.getSimpleName();
            for (ResMessages.PublicCreateInfo.IntegerWrapper member : ((ResMessages.PublicCreateInfo) info1.createInfo).members) {
                member.setCreateInfo((ResMessages.PublicCreateInfo) info1.createInfo);
            }
        } else {
            info1.createType = ResMessages.PrivateCreateInfo.class.getSimpleName();
            for (ResMessages.PrivateCreateInfo.IntegerWrapper member : ((ResMessages.PrivateCreateInfo) info1
                    .createInfo).members) {
                member.setCreateInfo((ResMessages.PrivateCreateInfo) info1.createInfo);
            }
        }
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

    private void upsertEventInfo(ResMessages.EventInfo info, OrmDatabaseHelper helper) throws
            SQLException {

        if (info instanceof ResMessages.CreateEvent) {

            ResMessages.CreateEvent createEvent = (ResMessages.CreateEvent) info;

            if (createEvent.createInfo instanceof ResMessages.PublicCreateInfo) {
                ResMessages.PublicCreateInfo createInfo = (ResMessages.PublicCreateInfo) createEvent.createInfo;
                helper.getDao(ResMessages.PublicCreateInfo.class).createOrUpdate(createInfo);

                DeleteBuilder<ResMessages.PublicCreateInfo.IntegerWrapper, ?> deleteBuilder
                        = helper.getDao(ResMessages.PublicCreateInfo.IntegerWrapper.class)
                        .deleteBuilder();
                deleteBuilder.where()
                        .eq("createInfo_id", createInfo._id);
                deleteBuilder.delete();


                for (ResMessages.PublicCreateInfo.IntegerWrapper member : (createInfo).members) {
                    member.setCreateInfo(createInfo);
                    helper.getDao(ResMessages.PublicCreateInfo.IntegerWrapper.class).createOrUpdate(member);
                }
            } else {
                ResMessages.PrivateCreateInfo createInfo = (ResMessages.PrivateCreateInfo) createEvent.createInfo;
                helper.getDao(ResMessages.PrivateCreateInfo.class).createOrUpdate(createInfo);

                DeleteBuilder<ResMessages.PrivateCreateInfo.IntegerWrapper, ?> deleteBuilder
                        = helper.getDao(ResMessages.PrivateCreateInfo.IntegerWrapper.class)
                        .deleteBuilder();
                deleteBuilder.where()
                        .eq("createInfo_id", createInfo._id);
                deleteBuilder.delete();

                for (ResMessages.PrivateCreateInfo.IntegerWrapper member : (createInfo).members) {
                    member.setCreateInfo(createInfo);
                    helper.getDao(ResMessages.PrivateCreateInfo.IntegerWrapper.class)
                            .createOrUpdate(member);
                }
            }

            helper.getDao(ResMessages.CreateEvent.class).createOrUpdate(createEvent);


        } else if (info instanceof ResMessages.JoinEvent) {
            helper.getDao(ResMessages.JoinEvent.class).createOrUpdate((ResMessages.JoinEvent) info);

        } else if (info instanceof ResMessages.InviteEvent) {
            ResMessages.InviteEvent info1 = (ResMessages.InviteEvent) info;
            helper.getDao(ResMessages.InviteEvent.class).createOrUpdate(info1);

            DeleteBuilder<ResMessages.InviteEvent.IntegerWrapper, ?> deleteBuilder
                    = helper.getDao(ResMessages.InviteEvent.IntegerWrapper.class)
                    .deleteBuilder();
            deleteBuilder.where()
                    .eq("inviteEvent_id", info1._id);
            deleteBuilder.delete();

            for (ResMessages.InviteEvent.IntegerWrapper inviteUser : info1.inviteUsers) {
                inviteUser.setInviteEvent(info1);
                helper.getDao(ResMessages.InviteEvent.IntegerWrapper.class)
                        .createOrUpdate(inviteUser);
            }


        } else if (info instanceof ResMessages.LeaveEvent) {
            helper.getDao(ResMessages.LeaveEvent.class).createOrUpdate((ResMessages.LeaveEvent) info);

        } else if (info instanceof ResMessages.AnnouncementCreateEvent) {
            ResMessages.AnnouncementCreateEvent info1 = (ResMessages.AnnouncementCreateEvent) info;
            helper.getDao(ResMessages.AnnouncementCreateEvent.Info.class)
                    .createOrUpdate(info1.getEventInfo());
            helper.getDao(ResMessages.AnnouncementCreateEvent.class).createOrUpdate(info1);

        } else if (info instanceof ResMessages.AnnouncementDeleteEvent) {

            helper.getDao(ResMessages.AnnouncementDeleteEvent.class).createOrUpdate((ResMessages.AnnouncementDeleteEvent) info);

        } else if (info instanceof ResMessages.AnnouncementUpdateEvent) {
            ResMessages.AnnouncementUpdateEvent info1 = (ResMessages.AnnouncementUpdateEvent) info;
            helper.getDao(ResMessages.AnnouncementUpdateEvent.Info.class)
                    .createOrUpdate(info1.getEventInfo());

            helper.getDao(ResMessages.AnnouncementUpdateEvent.class).createOrUpdate(info1);

        }

    }
}
