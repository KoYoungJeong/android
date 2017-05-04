package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class SendMessageRepository extends LockExecutorTemplate {

    private static SendMessageRepository repository;

    synchronized public static SendMessageRepository getRepository() {
        if (repository == null) {
            repository = new SendMessageRepository();
        }
        return repository;
    }

    public boolean insertSendMessage(SendMessage sendMessage) {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> sendMessageDao = getHelper().getDao(SendMessage.class);
                sendMessage.setStatus(SendMessage.Status.SENDING.name());
                sendMessageDao.create(sendMessage);

                Dao<MentionObject, ?> mentionObjectDao = getHelper().getDao(MentionObject.class);
                DeleteBuilder<MentionObject, ?> deleteBuilder = mentionObjectDao.deleteBuilder();
                deleteBuilder.where()
                        .eq("sendMessageOf_id", sendMessage.getId());
                deleteBuilder.delete();

                Collection<MentionObject> mentionObjects = sendMessage.getMentionObjects();
                if (mentionObjects != null && !mentionObjects.isEmpty()) {
                    mentionObjectDao.callBatchTasks(() -> {
                        for (MentionObject mentionObject : mentionObjects) {
                            mentionObjectDao.create(mentionObject);
                        }
                        return null;
                    });
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;

        });
    }

    public List<SendMessage> getSendMessageOfRoom(long roomId) {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);
                return dao.queryBuilder()
                        .where()
                        .eq("roomId", roomId)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<SendMessage>(0);

        });
    }

    public SendMessage getSendMessageOfLocal(long localId) {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);
                return dao.queryBuilder()
                        .where()
                        .eq("id", localId)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;

        });
    }

    public int deleteSendMessage(long id) {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);
                DeleteBuilder<SendMessage, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("id", id);
                return deleteBuilder.delete();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;

        });
    }

    public int deleteAllSendingMessage(long roomId) {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);
                DeleteBuilder<SendMessage, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("roomId", roomId);
                return deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;
        });
    }

    public int updateSendMessageStatus(long id, SendMessage.Status status) {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);
                UpdateBuilder<SendMessage, ?> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("status", status.name());
                updateBuilder.where()
                        .eq("id", id);
                return updateBuilder.update();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public int getMessagesCount(int roomId) {
        return execute(() -> {
            try {
                return (Long.valueOf(getHelper().getDao(SendMessage.class)
                        .queryBuilder()
                        .where()
                        .eq("roomId", roomId)
                        .countOf())).intValue();
            } catch (SQLException e) {
                e.printStackTrace();
            }


            return 0;

        });
    }

    public SendMessage getSendMessageOfRoom(int roomId, int index) {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);
                return dao.queryBuilder()
                        .offset((long) index)
                        .limit(1l)
                        .where()
                        .eq("roomId", roomId)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;

        });
    }

    public int updateSendMessageStatus(long localId, long messageId, SendMessage.Status status) {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);
                UpdateBuilder<SendMessage, ?> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("status", status.name());
                updateBuilder.updateColumnValue("messageId", messageId);
                updateBuilder.where()
                        .eq("id", localId);
                return updateBuilder.update();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public void deleteCompletedMessages(List<Long> messageIds) {
        execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);


                dao.callBatchTasks(() -> {

                    for (long messageId : messageIds) {
                        DeleteBuilder<SendMessage, ?> deleteBuilder = dao.deleteBuilder();
                        deleteBuilder.where().eq("messageId", messageId);
                        deleteBuilder.delete();
                    }

                    return null;
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public int deleteAllOfCompletedMessages() {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);
                DeleteBuilder<SendMessage, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("status", SendMessage.Status.COMPLETE.name());
                return deleteBuilder.delete();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;

        });
    }

    public int deleteCompletedMessageOfRoom(long roomId) {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);
                DeleteBuilder<SendMessage, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where()
                        .eq("status", SendMessage.Status.COMPLETE.name())
                        .and()
                        .eq("roomId", roomId);
                return deleteBuilder.delete();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;

        });
    }

    public int deleteAllMessages() {
        return execute(() -> {
            try {
                Dao<SendMessage, ?> dao = getHelper().getDao(SendMessage.class);
                return dao.deleteBuilder()
                        .delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;

        });

    }
}
