package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.local.orm.domain.ReadyMessage;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;

import java.sql.SQLException;

public class ReadyMessageRepository extends LockExecutorTemplate {


    private static ReadyMessageRepository repository;

    synchronized public static ReadyMessageRepository getRepository() {
        if (repository == null) {
            repository = new ReadyMessageRepository();
        }
        return repository;
    }

    public boolean upsertReadyMessage(ReadyMessage readyMessage) {
        return execute(() -> {
            try {
                Dao<ReadyMessage, ?> readyMessageDao = getHelper().getDao(ReadyMessage.class);
                readyMessageDao.createOrUpdate(readyMessage);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public int deleteReadyMessage(long roomId) {
        return execute(() -> {
            try {
                Dao<ReadyMessage, ?> readyMessageDao = getHelper().getDao(ReadyMessage.class);
                DeleteBuilder<ReadyMessage, ?> deleteBuilder = readyMessageDao.deleteBuilder();
                deleteBuilder
                        .where()
                        .eq("roomId", roomId);
                return deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public ReadyMessage getReadyMessage(long roomId) {
        return execute(() -> {
            try {
                Dao<ReadyMessage, ?> readyMessageDao = getHelper().getDao(ReadyMessage.class);
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

        });
    }

}
