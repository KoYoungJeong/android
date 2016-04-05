package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.domain.FileDetail;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 27..
 */
public class FileDetailRepository extends LockExecutorTemplate {
    private static FileDetailRepository repository;

    synchronized public static FileDetailRepository getRepository() {
        if (repository == null) {
            repository = new FileDetailRepository();
        }
        return repository;
    }

    public boolean upsertFileDetail(FileDetail fileDetail) {
        return execute(() -> {
            try {
                Dao<FileDetail, ?> fileDetailDao = getHelper().getDao(FileDetail.class);
                fileDetailDao.createOrUpdate(fileDetail);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;

        });
    }

    public List<FileDetail> getFileDetail(long fileId) {
        return execute(() -> {
            try {
                Dao<FileDetail, ?> fileDetailDao = getHelper().getDao(FileDetail.class);
                return fileDetailDao.queryBuilder()
                        .where()
                        .eq("fileId", fileId)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<FileDetail>();

        });
    }

    public void updateFileExternalLink(String fileUrl, boolean externalShared, String externalUrl, String externalCode) {
        execute(() -> {
            try {
                Dao<ResMessages.FileContent, ?> fileDetailDao = getHelper().getDao(ResMessages.FileContent.class);
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
            return 0;
        });
    }
}
