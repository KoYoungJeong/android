package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.local.orm.domain.ReadyComment;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class ReadyCommentRepository extends LockExecutorTemplate {

    public static ReadyCommentRepository getRepository() {
        return new ReadyCommentRepository();
    }

    public boolean upsertReadyComment(ReadyComment readyMessage) {
        return execute(() -> {
            try {
                Dao<ReadyComment, ?> readyMessageDao = getHelper().getDao(ReadyComment.class);
                readyMessageDao.createOrUpdate(readyMessage);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public int deleteReadyComment(int fileId) {
        return execute(() -> {
            try {
                Dao<ReadyComment, ?> readyMessageDao = getHelper().getDao(ReadyComment.class);
                DeleteBuilder<ReadyComment, ?> deleteBuilder = readyMessageDao.deleteBuilder();
                deleteBuilder
                        .where()
                        .eq("fileId", fileId);
                return deleteBuilder.delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public ReadyComment getReadyComment(long fileId) {
        return execute(() -> {
            try {
                Dao<ReadyComment, ?> readyMessageDao = getHelper().getDao(ReadyComment.class);
                ReadyComment readyMessage = readyMessageDao.queryBuilder()
                        .where()
                        .eq("fileId", fileId)
                        .queryForFirst();

                if (readyMessage == null) {
                    readyMessage = new ReadyComment();
                    readyMessage.setFileId(fileId);
                    readyMessage.setText("");
                }

                return readyMessage;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            ReadyComment readyMessage = new ReadyComment();
            readyMessage.setFileId(fileId);
            readyMessage.setText("");
            return readyMessage;

        });
    }

}
