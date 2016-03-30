package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.domain.DownloadInfo;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DownloadRepository extends LockExecutorTemplate {

    private static DownloadRepository repository;

    public static DownloadRepository getInstance() {
        if (repository == null) {
            repository = new DownloadRepository();
        }

        return repository;
    }

    public boolean upsertDownloadInfo(DownloadInfo downloadInfo) {
        if (downloadInfo == null || downloadInfo.getNotificationId() <= 0) {
            return false;
        }

        return execute(() -> {
            try {
                Dao<DownloadInfo, ?> dao = getHelper().getDao(DownloadInfo.class);
                dao.createOrUpdate(downloadInfo);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public List<DownloadInfo> getDownloadInfosInProgress() {
        return execute(() -> {

            try {
                Dao<DownloadInfo, ?> dao = getHelper().getDao(DownloadInfo.class);
                return dao.queryBuilder()
                        .where()
                        .eq("state", 0)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<DownloadInfo>();
        });
    }

    public int deleteDownloadInfo(int notificationId) {
        return execute(() -> {

            try {
                Dao<DownloadInfo, ?> dao = getHelper().getDao(DownloadInfo.class);
                DeleteBuilder<DownloadInfo, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where()
                        .eq("notificationId", notificationId);
                return deleteBuilder.delete();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;
        });
    }

    public int updateDownloadState(int notificationId, int state) {
        return execute(() -> {

            try {
                Dao<DownloadInfo, ?> dao = getHelper().getDao(DownloadInfo.class);
                UpdateBuilder<DownloadInfo, ?> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("state", state);
                updateBuilder.where()
                        .eq("notificationId", notificationId);
                return updateBuilder.update();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;
        });
    }
}
