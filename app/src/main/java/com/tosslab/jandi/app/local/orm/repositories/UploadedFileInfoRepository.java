package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.domain.UploadedFileInfo;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;

import java.sql.SQLException;

public class UploadedFileInfoRepository extends LockExecutorTemplate {

    public static UploadedFileInfoRepository getRepository() {
        return new UploadedFileInfoRepository();
    }

    public int insertFileInfo(UploadedFileInfo fileInfo) {
        return execute(() -> {
            try {
                Dao<UploadedFileInfo, ?> dao = getHelper().getDao(UploadedFileInfo.class);
                return dao.create(fileInfo);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public UploadedFileInfo getUploadedFileInfo(long messageId) {
        return execute(() -> {
            UploadedFileInfo fileInfo = null;
            try {
                Dao<UploadedFileInfo, ?> dao = getHelper().getDao(UploadedFileInfo.class);
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

        });
    }

}
