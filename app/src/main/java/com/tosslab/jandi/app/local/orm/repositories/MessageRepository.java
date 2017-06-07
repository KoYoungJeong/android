package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.domain.RoomLinkRelation;
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

    public boolean updateDirty(long roomId, long startLinkId, long endLinkId) {
        return execute(() -> {
            try {
                Dao<RoomLinkRelation, Object> dao = getDao(RoomLinkRelation.class);
                UpdateBuilder<RoomLinkRelation, Object> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("dirty", false);
                updateBuilder.where()
                        .eq("roomId", roomId)
                        .and()
                        .le("linkId", endLinkId)
                        .and()
                        .ge("linkId", startLinkId);
                updateBuilder.update();
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public boolean insertDirty(ResMessages.Link link) {
        return execute(() -> {
            try {
                if (link.toEntity != null && link.toEntity.size() > 0) {
                    Dao<RoomLinkRelation, String> relationDao = getDao(RoomLinkRelation.class);
                    RoomLinkRelation relation;
                    int size = link.toEntity.size();
                    for (int idx = 0; idx < size; idx++) {
                        relation = new RoomLinkRelation();
                        relation.setRoomId(link.toEntity.get(idx));
                        relation.setLinkId(link.id);
                        relation.setDirty(false);
                        relationDao.createIfNotExists(relation);
                    }

                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean isDirty(long roomId, long linkId) {
        return execute(() -> {

            try {
                Dao<RoomLinkRelation, Object> dao = getDao(RoomLinkRelation.class);
                return dao.queryBuilder()
                        .where()
                        .eq("roomId", roomId)
                        .and()
                        .eq("linkId", linkId)
                        .and()
                        .eq("dirty", true)
                        .countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return true;
        });
    }

    public boolean updateDirty(long roomId, long linkId) {
        return execute(() -> {
            try {
                Dao<RoomLinkRelation, Object> dao = getDao(RoomLinkRelation.class);
                UpdateBuilder<RoomLinkRelation, Object> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("dirty", false);
                updateBuilder.where()
                        .eq("roomId", roomId)
                        .and()
                        .eq("linkId", linkId);
                updateBuilder.update();
                return true;

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public boolean updateDirty(List<RoomLinkRelation> relations) {
        return execute(() -> {
            try {
                Dao<RoomLinkRelation, Object> dao = getDao(RoomLinkRelation.class);
                dao.callBatchTasks(() -> {
                    for (RoomLinkRelation relation : relations) {
                        dao.update(relation);
                    }
                    return null;
                });
                return true;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public boolean upsertMessages(List<ResMessages.Link> newMessages) {
        return execute(() -> {
            try {

                List<ResMessages.Link> messages = new ArrayList<>();
                messages.addAll(newMessages);

                Dao<ResMessages.Link, ?> dao = getHelper().getDao(ResMessages.Link.class);

                List<RoomLinkRelation> relations = new ArrayList<>();
                for (ResMessages.Link link : messages) {
                    if (link.toEntity != null && link.toEntity.size() > 0) {
                        int size = link.toEntity.size();
                        for (int idx = 0; idx < size; idx++) {
                            RoomLinkRelation relation = new RoomLinkRelation();
                            relation.setRoomId(link.toEntity.get(idx));
                            relation.setLinkId(link.id);
                            relations.add(relation);
                        }
                    }
                }

                // 내부에서 트랜잭션 commit 컨트롤을 함
                dao.callBatchTasks(() -> {
                    for (ResMessages.Link message : messages) {
                        dao.createOrUpdate(message);
                    }
                    return null;
                });

                Dao<RoomLinkRelation, String> relationDao = getDao(RoomLinkRelation.class);
                relationDao.callBatchTasks(() -> {
                    for (RoomLinkRelation relation : relations) {
                        relationDao.createIfNotExists(relation);
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

    public boolean upsertMessage(ResMessages.Link link) {
        return execute(() -> {
            try {
                Dao<ResMessages.Link, ?> dao = getHelper().getDao(ResMessages.Link.class);
                dao.createOrUpdate(link);

                if (link.toEntity != null && link.toEntity.size() > 0) {
                    Dao<RoomLinkRelation, String> relationDao = getDao(RoomLinkRelation.class);
                    RoomLinkRelation relation;
                    int size = link.toEntity.size();
                    for (int idx = 0; idx < size; idx++) {
                        relation = new RoomLinkRelation();
                        relation.setRoomId(link.toEntity.get(idx));
                        relation.setLinkId(link.id);
                        relationDao.createIfNotExists(relation);
                    }

                }

                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public boolean insertMessage(ResMessages.Link link) {
        return execute(() -> {
            try {
                Dao<ResMessages.Link, ?> dao = getHelper().getDao(ResMessages.Link.class);
                dao.createIfNotExists(link);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public int deleteMessageOfMessageId(long messageId) {
        return execute(() -> {
            if (messageId <= 0) {
                // 이벤트는 삭제하지 않기 위함
                return 0;
            }


            try {
                Dao<ResMessages.Link, ?> linkDao = getHelper().getDao(ResMessages.Link.class);

                Dao<RoomLinkRelation, Object> dao = getDao(RoomLinkRelation.class);
                DeleteBuilder<RoomLinkRelation, Object> relationDeleteBuilder = dao.deleteBuilder();

                /*
                DELETE FROM room_link_relation
                WHERE linkId in (
                        SELECT id FROM message_link WHERE messageId = {messageId}
                    )
                 */
                QueryBuilder<ResMessages.Link, ?> linkQueryBuilder = linkDao.queryBuilder();
                linkQueryBuilder.selectColumns("id")
                        .where()
                        .eq("messageId", messageId);

                relationDeleteBuilder.where()
                        .in("linkId", linkQueryBuilder);
                relationDeleteBuilder.delete();

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

    public List<ResMessages.Link> getMessages(long roomId) {

        return execute(() -> {
            try {
                return getHelper().getDao(ResMessages.Link.class).queryBuilder()
                        .orderBy("time", false)
                        .where()
                        .in("id", inQueryBuildOfRoomRelation(roomId))
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<ResMessages.Link>();

        });
    }


    private QueryBuilder<RoomLinkRelation, String> inQueryBuildOfRoomRelation(long roomId) throws SQLException {
        Dao<RoomLinkRelation, String> relationDao = getDao(RoomLinkRelation.class);
        QueryBuilder<RoomLinkRelation, String> queryBuilder = relationDao.queryBuilder();
        queryBuilder.selectColumns("linkId").where().eq("roomId", roomId);
        return queryBuilder;
    }

    public ResMessages.Link getLastMessage(long roomId) {
        return execute(() -> {
            ResMessages.Link link = null;
            try {
                link = getHelper().getDao(ResMessages.Link.class)
                        .queryBuilder()
                        .orderBy("time", false)
                        .where()
                        .in("id", inQueryBuildOfRoomRelation(roomId))
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
                Dao<RoomLinkRelation, String> relationDao = getDao(RoomLinkRelation.class);
                QueryBuilder<RoomLinkRelation, String> queryBuilder = relationDao.queryBuilder();
                queryBuilder.selectColumns("linkId")
                        .where()
                        .eq("roomId", roomId)
                        .and()
                        .eq("dirty", false);

                return getHelper().getDao(ResMessages.Link.class)
                        .queryBuilder()
                        .limit((long) count)
                        .orderBy("time", false)
                        .where()
                        .in("id", queryBuilder)
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

    public int deleteAllLink() {
        return execute(() -> {
            Dao<ResMessages.Link, ?> dao;
            try {
                dao = getHelper().getDao(ResMessages.Link.class);
                DeleteBuilder<ResMessages.Link, ?> deleteBuilder = dao.deleteBuilder();
                getDao(RoomLinkRelation.class).deleteBuilder().delete();
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
                return (Long.valueOf(getHelper().getDao(ResMessages.Link.class)
                        .queryBuilder()
                        .orderBy("time", true)
                        .where()
                        .in("id", inQueryBuildOfRoomRelation(roomId))
                        .and()
                        .ge("id", startLinkId)
                        .countOf())).intValue();
            } catch (SQLException e) {
                e.printStackTrace();
            }


            return 0;

        });
    }

    /**
     * dirtyflag 가 필요 없는 경우
     *
     * @param roomId
     * @param firstCursorLinkId
     * @param toCursorLinkId
     * @return
     */
    public List<ResMessages.Link> getMessages(long roomId, long firstCursorLinkId, long toCursorLinkId) {
        return execute(() -> {
            try {
                return getHelper().getDao(ResMessages.Link.class)
                        .queryBuilder()
                        .orderBy("time", true)
                        .where()
                        .in("id", inQueryBuildOfRoomRelation(roomId))
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

    public ResMessages.Link getMessage(long linkId) {
        return execute(() -> {

            try {
                Dao<ResMessages.Link, Long> dao = getDao(ResMessages.Link.class);
                return dao.queryForId(linkId);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ResMessages.Link();
        });
    }
}
