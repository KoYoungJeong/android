package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class MessageRepository {

    private static MessageRepository repository;
    private final OrmDatabaseHelper helper;
    private final Lock lock;

    private MessageRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        lock = new ReentrantLock();

    }

    public static MessageRepository getRepository() {

        if (repository == null) {
            repository = new MessageRepository();
        }
        return repository;
    }

    /**
     * It's for Only TestCode.
     */
    public static void release() {
        repository = null;
    }

    public boolean upsertMessages(List<ResMessages.Link> messages) {
        lock.lock();
        try {

            Dao<ResMessages.Link, ?> dao = helper.getDao(ResMessages.Link.class);

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
        } finally {
            lock.unlock();
        }
        return false;
    }

    public boolean upsertMessage(ResMessages.Link message) {
        lock.lock();
        try {
            Dao<ResMessages.Link, ?> dao = helper.getDao(ResMessages.Link.class);
            dao.createOrUpdate(message);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return false;
    }

    public int deleteMessage(int messageId) {

        if (messageId <= 0) {
            // 이벤트는 삭제하지 않기 위함
            return 0;
        }

        lock.lock();

        try {
            Dao<ResMessages.Link, ?> linkDao = helper.getDao(ResMessages.Link.class);
            DeleteBuilder<ResMessages.Link, ?> deleteBuilder = linkDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("messageId", messageId);
            return deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return 0;

    }

    public List<ResMessages.Link> getMessages(int roomId) {

        try {
            int teamId = AccountRepository.getRepository().getSelectedTeamId();
            return helper.getDao(ResMessages.Link.class)
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

        return new ArrayList<>();
    }

    public ResMessages.Link getLastMessage(int roomId) {

        try {
            int teamId = AccountRepository.getRepository().getSelectedTeamId();
            return helper.getDao(ResMessages.Link.class)
                    .queryBuilder()
                    .orderBy("time", false)
                    .where()
                    .eq("teamId", teamId)
                    .and()
                    .eq("roomId", roomId)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ResMessages.Link link = new ResMessages.Link();
        link.id = -1;
        return link;
    }

    public List<ResMessages.Link> getOldMessages(int roomId, int lastLinkId, long count) {
        try {
            int teamId = AccountRepository.getRepository().getSelectedTeamId();
            return helper.getDao(ResMessages.Link.class)
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

        return new ArrayList<>();

    }

    public int updateStarred(int messageId, boolean isStarred) {
        lock.lock();
        try {
            Dao<ResMessages.TextMessage, Integer> textMessageDao = helper.getDao(ResMessages
                    .TextMessage.class);

            if (textMessageDao.idExists(messageId)) {
                UpdateBuilder<ResMessages.TextMessage, Integer> updateBuilder = textMessageDao.updateBuilder();
                updateBuilder.updateColumnValue("isStarred", isStarred);
                updateBuilder
                        .where()
                        .eq("id", messageId);
                return updateBuilder.update();
            }

            Dao<ResMessages.CommentMessage, Integer> commentMessageDao
                    = helper.getDao(ResMessages.CommentMessage.class);

            if (commentMessageDao.idExists(messageId)) {
                UpdateBuilder<ResMessages.CommentMessage, Integer> updateBuilder = commentMessageDao.updateBuilder();
                updateBuilder.updateColumnValue("isStarred", isStarred);
                updateBuilder.where()
                        .eq("id", messageId);
                return updateBuilder.update();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return 0;
    }

    public void updateStatus(int fileId, String archived) {
        lock.lock();
        try {
            Dao<ResMessages.FileMessage, Integer> dao = helper.getDao(ResMessages.FileMessage
                    .class);
            if (dao.idExists(fileId)) {
                UpdateBuilder<ResMessages.FileMessage, Integer> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("status", archived);
                updateBuilder.where()
                        .eq("id", fileId);
                updateBuilder.update();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void upsertFileMessage(ResMessages.FileMessage fileMessage) {
        lock.lock();
        try {
            Dao<ResMessages.FileMessage, ?> dao = helper.getDao(ResMessages.FileMessage.class);
            dao.createOrUpdate(fileMessage);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public boolean upsertTextMessage(ResMessages.TextMessage textMessage) {
        lock.lock();
        try {
            Dao<ResMessages.TextMessage, ?> dao = helper.getDao(ResMessages.TextMessage.class);
            dao.createOrUpdate(textMessage);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return false;
    }

    public int deleteLinkByMessageId(int messageId) {
        lock.lock();
        try {
            Dao<ResMessages.Link, ?> dao = helper.getDao(ResMessages.Link.class);

            DeleteBuilder<ResMessages.Link, ?> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq("messageId", messageId);

            return deleteBuilder.delete();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return 0;
    }

    public ResMessages.FileMessage getFileMessage(int fileId) {
        lock.lock();

        try {

            Dao<ResMessages.FileMessage, ?> dao = helper.getDao(ResMessages.FileMessage.class);

            return dao.queryBuilder()
                    .where()
                    .eq("id", fileId)
                    .queryForFirst();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public ResMessages.TextMessage getTextMessage(int messageId) {
        lock.lock();
        try {
            Dao<ResMessages.TextMessage, ?> dao = helper.getDao(ResMessages.TextMessage.class);

            return dao.queryBuilder()
                    .where()
                    .eq("id", messageId)
                    .queryForFirst();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }
}
