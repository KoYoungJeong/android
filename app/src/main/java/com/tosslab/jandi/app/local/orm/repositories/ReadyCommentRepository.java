package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.ReadyComment;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class ReadyCommentRepository {

    private static ReadyCommentRepository repository;
    private final OrmDatabaseHelper helper;

    private ReadyCommentRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
    }

    public static ReadyCommentRepository getRepository() {

        if (repository == null) {
            repository = new ReadyCommentRepository();
        }
        return repository;
    }

    /**
     * It's for Only TestCode.
     */
    public static void release() {
        repository = null;
    }

    public boolean upsertReadyComment(ReadyComment readyMessage) {
        try {
            Dao<ReadyComment, ?> readyMessageDao = helper.getDao(ReadyComment.class);
            readyMessageDao.createOrUpdate(readyMessage);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int deleteReadyComment(int fileId) {
        try {
            Dao<ReadyComment, ?> readyMessageDao = helper.getDao(ReadyComment.class);
            DeleteBuilder<ReadyComment, ?> deleteBuilder = readyMessageDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("fileId", fileId);
            return deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ReadyComment getReadyComment(int fileId) {
        try {
            Dao<ReadyComment, ?> readyMessageDao = helper.getDao(ReadyComment.class);
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
    }

}
