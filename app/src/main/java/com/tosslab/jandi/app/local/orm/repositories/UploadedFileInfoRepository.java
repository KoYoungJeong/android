package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.UploadedFileInfo;

import java.sql.SQLException;

public class UploadedFileInfoRepository {

    private static UploadedFileInfoRepository repository;
    private final OrmDatabaseHelper helper;

    public UploadedFileInfoRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
    }

    public static UploadedFileInfoRepository getRepository() {
        if (repository == null) {
            repository = new UploadedFileInfoRepository();
        }
        return repository;
    }

    public int insertFileInfo(UploadedFileInfo fileInfo) {

        try {
            Dao<UploadedFileInfo, ?> dao = helper.getDao(UploadedFileInfo.class);
            return dao.create(fileInfo);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public UploadedFileInfo getUploadedFileInfo(int messageId) {
        UploadedFileInfo fileInfo = null;
        try {
            Dao<UploadedFileInfo, ?> dao = helper.getDao(UploadedFileInfo.class);
            fileInfo = dao.queryBuilder()
                    .where()
                    .eq("messageId", messageId)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (fileInfo == null) {
            fileInfo = new UploadedFileInfo();
            fileInfo.setMessageId(messageId);
            fileInfo.setLocalPath("");
        }

        return fileInfo;

    }

}
