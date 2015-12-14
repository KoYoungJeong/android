package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.FileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 27..
 */
public class FileDetailRepository {
    private static FileDetailRepository repository;
    private final OrmDatabaseHelper helper;

    public FileDetailRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
    }

    public static FileDetailRepository getRepository() {
        if (repository == null) {
            repository = new FileDetailRepository();
        }
        return repository;
    }

    /**
     * It's for Only TestCode.
     */
    public static void release() {
        repository = null;
    }

    public boolean upsertFileDetail(FileDetail fileDetail) {

        try {
            Dao<FileDetail, ?> fileDetailDao = helper.getDao(FileDetail.class);
            fileDetailDao.createOrUpdate(fileDetail);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<FileDetail> getFileDetail(int fileId) {

        try {
            Dao<FileDetail, ?> fileDetailDao = helper.getDao(FileDetail.class);
            return fileDetailDao.queryBuilder()
                    .where()
                    .eq("fileId", fileId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public void updateFileExternalLink(String fileUrl, boolean externalShared, String externalUrl, String externalCode) {
        try {
            Dao<ResMessages.FileContent, ?> fileDetailDao = helper.getDao(ResMessages.FileContent.class);
            UpdateBuilder<ResMessages.FileContent, ?> builder = fileDetailDao.updateBuilder();
            builder.updateColumnValue("externalShared", externalShared);
            builder.updateColumnValue("externalUrl", externalUrl);
            builder.updateColumnValue("externalCode", externalCode);
            builder.where()
                    .eq("fileUrl", fileUrl);
            builder.update();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
