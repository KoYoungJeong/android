package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.ReadyMessage;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class ReadyMessageRepository {

    private static ReadyMessageRepository repository;
    private final OrmDatabaseHelper helper;

    private ReadyMessageRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
    }

    public static ReadyMessageRepository getRepository() {

        if (repository == null) {
            repository = new ReadyMessageRepository();
        }
        return repository;
    }

    /**
     * It's for Only TestCode.
     */
    public static void release() {
        repository = null;
    }

    public boolean upsertReadyMessage(ReadyMessage readyMessage) {
        try {
            Dao<ReadyMessage, ?> readyMessageDao = helper.getDao(ReadyMessage.class);
            readyMessageDao.createOrUpdate(readyMessage);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int deleteReadyMessage(int roomId) {
        try {
            Dao<ReadyMessage, ?> readyMessageDao = helper.getDao(ReadyMessage.class);
            DeleteBuilder<ReadyMessage, ?> deleteBuilder = readyMessageDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("roomId", roomId);
            return deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ReadyMessage getReadyMessage(int roomId) {
        try {
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
        }

        ReadyMessage readyMessage = new ReadyMessage();
        readyMessage.setRoomId(roomId);
        readyMessage.setText("");
        return readyMessage;
    }

}
