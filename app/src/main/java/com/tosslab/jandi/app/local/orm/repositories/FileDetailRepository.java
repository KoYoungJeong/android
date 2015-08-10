package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.FileDetail;

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

}
