package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 8. 5..
 */
public class FileMessageDaoImpl extends BaseDaoImpl<ResMessages.FileMessage, Integer> {
    public FileMessageDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.FileMessage.class);
    }

    @Override
    public int create(ResMessages.FileMessage data) throws SQLException {
        upsertMessage(getConnectionSource(), data);
        return super.create(data);
    }

    @Override
    public int update(ResMessages.FileMessage data) throws SQLException {
        upsertMessage(getConnectionSource(), data);
        return super.update(data);
    }

    private void upsertMessage(ConnectionSource connectionSource, ResMessages.FileMessage fileMessage) throws SQLException {
        Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao
                (connectionSource, ResMessages
                        .OriginalMessage
                        .IntegerWrapper.class);
        DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq("fileOf_id", fileMessage.id);
        deleteBuilder.delete();

        for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : fileMessage.shareEntities) {
            shareEntity.setFileOf(fileMessage);
            dao.create(shareEntity);
        }

        DaoManager.createDao(connectionSource, ResMessages.FileContent.class)
                .createOrUpdate(fileMessage.content);
        DaoManager.createDao(connectionSource, ResMessages.ThumbnailUrls.class)
                .createOrUpdate(fileMessage.content.extraInfo);
    }
}
