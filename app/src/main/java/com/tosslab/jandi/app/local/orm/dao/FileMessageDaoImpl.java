package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;
import java.util.Iterator;

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
        Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao(
                connectionSource, ResMessages.OriginalMessage.IntegerWrapper.class);
        Iterator<ResMessages.OriginalMessage.IntegerWrapper> iterator = fileMessage.shareEntities.iterator();

        DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq("fileOf_id", fileMessage.id);
        deleteBuilder.delete();

        while (iterator.hasNext()) {
            ResMessages.OriginalMessage.IntegerWrapper shareEntity = iterator.next();
            shareEntity.setFileOf(fileMessage);
            dao.create(shareEntity);
        }

        ResMessages.FileContent content = fileMessage.content;
        if (content != null) {

            if (content.extraInfo != null) {
                Dao<ResMessages.ThumbnailUrls, ?> thumbnailUrlsDao =
                        DaoManager.createDao(connectionSource, ResMessages.ThumbnailUrls.class);
                thumbnailUrlsDao.createOrUpdate(content.extraInfo);
            }

            DaoManager.createDao(connectionSource, ResMessages.FileContent.class)
                    .createOrUpdate(content);

        }

    }
}
