package com.tosslab.jandi.app.local.orm.dao;

import android.util.Log;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;
import java.util.List;

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

//    @Override
//    public List<ResMessages.FileMessage> query(PreparedQuery<ResMessages.FileMessage> preparedQuery) throws SQLException {
//        List<ResMessages.FileMessage> query = super.query(preparedQuery);
//
//        Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao(
//                connectionSource, ResMessages.OriginalMessage.IntegerWrapper.class);
//        for (ResMessages.FileMessage message : query) {
//            List<ResMessages.OriginalMessage.IntegerWrapper> sharedEntities = dao.queryBuilder()
//                    .where()
//                    .eq("fileOf_id", message.id)
//                    .query();
//
//            message.shareEntities = sharedEntities;
//        }
//
//        return query;
//    }
//
    @Override
    public ResMessages.FileMessage queryForFirst(PreparedQuery<ResMessages.FileMessage> preparedQuery) throws SQLException {
        ResMessages.FileMessage message = super.queryForFirst(preparedQuery);

        Log.e("tony2", message.toString());

        Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao(
                connectionSource, ResMessages.OriginalMessage.IntegerWrapper.class);
        List<ResMessages.OriginalMessage.IntegerWrapper> sharedEntities = dao.queryBuilder()
                .where()
                .eq("fileOf_id", message.id)
                .query();

        Log.d("tony2", "sharedEntity Size = " + sharedEntities.size());

        message.shareEntities = sharedEntities;

        return message;
    }

    private void upsertMessage(ConnectionSource connectionSource, ResMessages.FileMessage fileMessage) throws SQLException {
        Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao(
                connectionSource, ResMessages.OriginalMessage.IntegerWrapper.class);
        DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq("fileOf_id", fileMessage.id);
        deleteBuilder.delete();

        Log.e("tony", "upsert - " + (fileMessage.shareEntities != null && !fileMessage.shareEntities.isEmpty()));
        if (fileMessage.shareEntities != null && !fileMessage.shareEntities.isEmpty()) {
            for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : fileMessage.shareEntities) {
                Log.e("tony", "entity - " + shareEntity.getShareEntity());
                shareEntity.setFileOf(fileMessage);
                dao.create(shareEntity);
            }
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
