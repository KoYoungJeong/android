package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.local.orm.domain.ReadyCommentForPoll;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class ReadyCommentForPollRepository extends LockExecutorTemplate {

    private static ReadyCommentForPollRepository repository;

    synchronized public static ReadyCommentForPollRepository getRepository() {
        if (repository == null) {
            repository = new ReadyCommentForPollRepository();
        }
        return repository;
    }

    public boolean upsertReadyComment(ReadyCommentForPoll readyMessage) {
        return execute(() -> {
            try {
                Dao<ReadyCommentForPoll, ?> readyMessageDao = getHelper().getDao(ReadyCommentForPoll.class);
                readyMessageDao.createOrUpdate(readyMessage);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public int deleteReadyComment(long pollId) {
        return execute(() -> {
            try {
                Dao<ReadyCommentForPoll, ?> readyMessageDao = getHelper().getDao(ReadyCommentForPoll.class);
                DeleteBuilder<ReadyCommentForPoll, ?> deleteBuilder = readyMessageDao.deleteBuilder();
                deleteBuilder
                        .where()
                        .eq("pollId", pollId);
                return deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public ReadyCommentForPoll getReadyComment(long pollId) {
        return execute(() -> {
            try {
                Dao<ReadyCommentForPoll, ?> readyMessageDao = getHelper().getDao(ReadyCommentForPoll.class);
                ReadyCommentForPoll readyMessage = readyMessageDao.queryBuilder()
                        .where()
                        .eq("pollId", pollId)
                        .queryForFirst();

                if (readyMessage == null) {
                    readyMessage = new ReadyCommentForPoll();
                    readyMessage.setPollId(pollId);
                    readyMessage.setText("");
                }

                return readyMessage;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            ReadyCommentForPoll readyMessage = new ReadyCommentForPoll();
            readyMessage.setPollId(pollId);
            readyMessage.setText("");
            return readyMessage;

        });
    }

}
