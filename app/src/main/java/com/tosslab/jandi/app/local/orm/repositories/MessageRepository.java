package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository extends LockExecutorTemplate {

    private static MessageRepository repository;

    synchronized public static MessageRepository getRepository() {

        if (repository == null) {
            repository = new MessageRepository();
        }
        return repository;
    }

    public boolean upsertMessages(List<ResMessages.Link> messages) {
        return execute(() -> {
            try {

                Dao<ResMessages.Link, ?> dao = getHelper().getDao(ResMessages.Link.class);

                // 내부에서 트랜잭션 commit 컨트롤을 함
                dao.callBatchTasks(() -> {
                    for (ResMessages.Link message : messages) {
                        dao.createOrUpdate(message);
                    }
                    return null;
                });

                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public boolean upsertMessage(ResMessages.Link message) {
        return execute(() -> {
            try {
                Dao<ResMessages.Link, ?> dao = getHelper().getDao(ResMessages.Link.class);
                dao.createOrUpdate(message);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public int deleteMessage(long messageId) {
        return execute(() -> {
            if (messageId <= 0) {
                // 이벤트는 삭제하지 않기 위함
                return 0;
            }


            try {
                Dao<ResMessages.Link, ?> linkDao = getHelper().getDao(ResMessages.Link.class);
                DeleteBuilder<ResMessages.Link, ?> deleteBuilder = linkDao.deleteBuilder();
                deleteBuilder
                        .where()
                        .eq("messageId", messageId);
                return deleteBuilder.delete();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;

        });

    }

    public List<ResMessages.Link> getMessages(int roomId) {

        return execute(() -> {
            try {
                long teamId = AccountRepository.getRepository().getSelectedTeamId();
                return getHelper().getDao(ResMessages.Link.class)
                        .queryBuilder()
                        .orderBy("time", false)
                        .where()
                        .eq("teamId", teamId)
                        .and()
                        .eq("roomId", roomId)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<ResMessages.Link>();

        });
    }

    public ResMessages.Link getLastMessageWitoutDirty(long roomId) {
        return execute(() -> {
            ResMessages.Link link = null;
            try {
                long teamId = AccountRepository.getRepository().getSelectedTeamId();
                link = getHelper().getDao(ResMessages.Link.class)
                        .queryBuilder()
                        .orderBy("time", false)
                        .where()
                        .eq("teamId", teamId)
                        .and()
                        .eq("roomId", roomId)
                        .and()
                        .eq("dirty", false)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (link == null) {
                link = new ResMessages.Link();
                link.id = -1;
            }
            return link;

        });
    }

    public List<ResMessages.Link> getOldMessages(long roomId, long lastLinkId, int count) {
        return execute(() -> {
            try {
                long teamId = AccountRepository.getRepository().getSelectedTeamId();
                return getHelper().getDao(ResMessages.Link.class)
                        .queryBuilder()
                        .limit(count)
                        .orderBy("time", false)
                        .where()
                        .eq("teamId", teamId)
                        .and()
                        .eq("roomId", roomId)
                        .and()
                        .lt("id", lastLinkId > 0 ? lastLinkId : Integer.MAX_VALUE)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<ResMessages.Link>();

        });
    }

    public int updateStarred(long messageId, boolean isStarred) {
        return execute(() -> {
            try {
                Dao<ResMessages.TextMessage, Long> textMessageDao = getHelper().getDao(ResMessages
                        .TextMessage.class);

                if (textMessageDao.idExists(messageId)) {
                    UpdateBuilder<ResMessages.TextMessage, Long> updateBuilder = textMessageDao.updateBuilder();
                    updateBuilder.updateColumnValue("isStarred", isStarred);
                    updateBuilder
                            .where()
                            .eq("id", messageId);
                    return updateBuilder.update();
                }

                Dao<ResMessages.CommentMessage, Long> commentMessageDao
                        = getHelper().getDao(ResMessages.CommentMessage.class);

                if (commentMessageDao.idExists(messageId)) {
                    UpdateBuilder<ResMessages.CommentMessage, Long> updateBuilder = commentMessageDao.updateBuilder();
                    updateBuilder.updateColumnValue("isStarred", isStarred);
                    updateBuilder.where()
                            .eq("id", messageId);
                    return updateBuilder.update();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public void updateStatus(long fileId, String archived) {
        execute(() -> {
            try {
                Dao<ResMessages.FileMessage, Long> dao = getHelper().getDao(ResMessages.FileMessage
                        .class);
                if (dao.idExists(fileId)) {
                    UpdateBuilder<ResMessages.FileMessage, Long> updateBuilder = dao.updateBuilder();
                    updateBuilder.updateColumnValue("status", archived);
                    updateBuilder.where()
                            .eq("id", fileId);
                    updateBuilder.update();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public void deleteSharedRoom(long fileId, long roomId) {
        execute(() -> {
            try {
                Dao<ResMessages.OriginalMessage.IntegerWrapper, Integer> dao = getHelper().getDao(ResMessages.OriginalMessage.IntegerWrapper.class);
                DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("fileOf_id", fileId).and().eq("shareEntity", roomId);
                deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public void upsertFileMessage(ResMessages.FileMessage fileMessage) {
        execute(() -> {
            try {
                Dao<ResMessages.FileMessage, ?> dao = getHelper().getDao(ResMessages.FileMessage.class);
                dao.createOrUpdate(fileMessage);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public boolean upsertTextMessage(ResMessages.TextMessage textMessage) {
        return execute(() -> {
            try {
                Dao<ResMessages.TextMessage, ?> dao = getHelper().getDao(ResMessages.TextMessage.class);
                dao.createOrUpdate(textMessage);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;

        });
    }

    public int deleteLinkByMessageId(long messageId) {
        return execute(() -> {
            try {
                Dao<ResMessages.Link, ?> dao = getHelper().getDao(ResMessages.Link.class);

                DeleteBuilder<ResMessages.Link, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("messageId", messageId);

                return deleteBuilder.delete();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public ResMessages.FileMessage getFileMessage(long fileId) {
        return execute(() -> {
            try {

                Dao<ResMessages.FileMessage, ?> dao = getHelper().getDao(ResMessages.FileMessage.class);

                return dao.queryBuilder()
                        .where()
                        .eq("id", fileId)
                        .queryForFirst();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;

        });
    }

    public ResMessages.TextMessage getTextMessage(long messageId) {
        return execute(() -> {
            try {
                Dao<ResMessages.TextMessage, ?> dao = getHelper().getDao(ResMessages.TextMessage.class);

                return dao.queryBuilder()
                        .where()
                        .eq("id", messageId)
                        .queryForFirst();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;

        });
    }

    public List<ResMessages.CommentMessage> getCommentMessages(long fileId) {
        return execute(() -> {
            try {
                Dao<ResMessages.CommentMessage, ?> dao = getHelper().getDao(ResMessages.CommentMessage.class);
                return dao.queryBuilder()
                        .where()
                        .eq("feedbackId", fileId)
                        .query();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<ResMessages.CommentMessage>();

        });
    }

    public List<ResMessages.CommentStickerMessage> getStickerCommentMessages(long fileId) {
        return execute(() -> {
            try {
                Dao<ResMessages.CommentStickerMessage, ?> dao =
                        getHelper().getDao(ResMessages.CommentStickerMessage.class);
                return dao.queryBuilder()
                        .where()
                        .eq("feedbackId", fileId)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<ResMessages.CommentStickerMessage>();

        });
    }

    /**
     * Only For Test!!!
     *
     * @return
     */
    public List<ResMessages.TextMessage> getTextMessages() {
        return execute(() -> {
            try {
                Dao<ResMessages.TextMessage, ?> dao = getHelper().getDao(ResMessages.TextMessage.class);

                return dao.queryForAll();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<ResMessages.TextMessage>();

        });
    }

    public int clearLinks(long teamId, long roomId) {
        return execute(() -> {
            try {
                Dao<ResMessages.Link, ?> dao = getHelper().getDao(ResMessages.Link.class);
                DeleteBuilder<ResMessages.Link, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where()
                        .eq("teamId", teamId)
                        .and()
                        .eq("roomId", roomId);
                return deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public int deleteAllLink() {
        return execute(() -> {
            Dao<ResMessages.Link, ?> dao;
            try {
                dao = getHelper().getDao(ResMessages.Link.class);
                DeleteBuilder<ResMessages.Link, ?> deleteBuilder = dao.deleteBuilder();
                return deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;

        });
    }

    public int getMessagesCount(long roomId, long startLinkId) {
        return execute(() -> {
            try {
                long teamId = AccountRepository.getRepository().getSelectedTeamId();
                return (Long.valueOf(getHelper().getDao(ResMessages.Link.class)
                        .queryBuilder()
                        .orderBy("time", true)
                        .where()
                        .eq("teamId", teamId)
                        .and()
                        .eq("roomId", roomId)
                        .and()
                        .ge("id", startLinkId)
                        .countOf())).intValue();
            } catch (SQLException e) {
                e.printStackTrace();
            }


            return 0;

        });
    }

    public int getMessagesCount(long roomId, long startLinkId, long endLinkId) {
        return execute(() -> {
            try {
                long teamId = AccountRepository.getRepository().getSelectedTeamId();
                return (Long.valueOf(getHelper().getDao(ResMessages.Link.class)
                        .queryBuilder()
                        .orderBy("time", true)
                        .where()
                        .eq("teamId", teamId)
                        .and()
                        .eq("roomId", roomId)
                        .and()
                        .ge("id", startLinkId)
                        .and()
                        .lt("id", endLinkId)
                        .countOf())).intValue();
            } catch (SQLException e) {
                e.printStackTrace();
            }


            return 0;

        });
    }

    public List<ResMessages.Link> getMessages(long roomId, long firstCursorLinkId, long toCursorLinkId) {
        return execute(() -> {
            try {
                long teamId = AccountRepository.getRepository().getSelectedTeamId();
                return getHelper().getDao(ResMessages.Link.class)
                        .queryBuilder()
                        .orderBy("time", true)
                        .where()
                        .eq("teamId", teamId)
                        .and()
                        .eq("roomId", roomId)
                        .and()
                        .ge("id", firstCursorLinkId)
                        .and()
                        .lt("id", toCursorLinkId)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<ResMessages.Link>(0);

        });
    }

    public boolean hasLinkOfMessageId(long messageId) {
        return execute(() -> {
            if (messageId <= 0) {
                return false;
            }

            try {
                Dao<ResMessages.Link, ?> linkDao = getHelper().getDao(ResMessages.Link.class);
                QueryBuilder<ResMessages.Link, ?> queryBuilder = linkDao.queryBuilder();
                return queryBuilder
                        .where()
                        .eq("messageId", messageId)
                        .countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }


            return false;

        });
    }
}
