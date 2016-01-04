package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class SendMessageRepository {

    private static SendMessageRepository repository;
    private final OrmDatabaseHelper helper;

    private SendMessageRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
    }

    public static SendMessageRepository getRepository() {

        if (repository == null) {
            repository = new SendMessageRepository();
        }
        return repository;
    }

    /**
     * It's for Only TestCode.
     */
    public static void release() {
        repository = null;
    }

    public boolean insertSendMessage(SendMessage sendMessage) {
        try {
            Dao<SendMessage, ?> sendMessageDao = helper.getDao(SendMessage.class);
            sendMessage.setStatus(SendMessage.Status.SENDING.name());
            sendMessageDao.create(sendMessage);

            Dao<MentionObject, ?> mentionObjectDao = helper.getDao(MentionObject.class);
            DeleteBuilder<MentionObject, ?> deleteBuilder = mentionObjectDao.deleteBuilder();
            deleteBuilder.where()
                    .eq("sendMessageOf_id", sendMessage.getId());
            deleteBuilder.delete();

            Collection<MentionObject> mentionObjects = sendMessage.getMentionObjects();
            if (mentionObjects != null && !mentionObjects.isEmpty()) {
                for (MentionObject mentionObject : mentionObjects) {
                    mentionObjectDao.create(mentionObject);
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    public List<SendMessage> getSendMessage(int roomId) {
        try {
            Dao<SendMessage, ?> dao = helper.getDao(SendMessage.class);
            return dao.queryBuilder()
                    .where()
                    .eq("roomId", roomId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(0);
    }

    public int deleteSendMessage(long id) {
        try {
            Dao<SendMessage, ?> dao = helper.getDao(SendMessage.class);
            DeleteBuilder<SendMessage, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("id", id);
            return deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }


    public int updateSendMessageStatus(long id, SendMessage.Status status) {
        try {
            Dao<SendMessage, ?> dao = helper.getDao(SendMessage.class);
            UpdateBuilder<SendMessage, ?> updateBuilder = dao.updateBuilder();
            updateBuilder.updateColumnValue("status", status.name());
            updateBuilder.where()
                    .eq("id", id);
            return updateBuilder.update();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getMessagesCount(int roomId) {
        try {
            return (Long.valueOf(helper.getDao(SendMessage.class)
                    .queryBuilder()
                    .where()
                    .eq("roomId", roomId)
                    .countOf())).intValue();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return 0;
    }

    public SendMessage getSendMessage(int roomId, int index) {
        try {
            Dao<SendMessage, ?> dao = helper.getDao(SendMessage.class);
            return dao.queryBuilder()
                    .offset(index)
                    .limit(1)
                    .where()
                    .eq("roomId", roomId)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public int updateSendMessageStatus(long localId, int messageId, SendMessage.Status status) {
        try {
            Dao<SendMessage, ?> dao = helper.getDao(SendMessage.class);
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
    }

    public void deleteCompletedMessages(List<Integer> messageIds) {
        try {
            Dao<SendMessage, ?> dao = helper.getDao(SendMessage.class);


            dao.callBatchTasks(() -> {

                for (Integer messageId : messageIds) {
                    DeleteBuilder<SendMessage, ?> deleteBuilder = dao.deleteBuilder();
                    deleteBuilder.where().eq("messageId", messageId);
                    deleteBuilder.delete();
                }

                return null;
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int deleteAllOfCompletedMessages() {
        try {
            Dao<SendMessage, ?> dao = helper.getDao(SendMessage.class);
            DeleteBuilder<SendMessage, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("status", SendMessage.Status.COMPLETE.name());
            return deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;

    }
}
