package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.local.orm.domain.FileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 27..
 */
public class FileDatailDaoImpl extends BaseDaoImpl<FileDetail, Long> {
    public FileDatailDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, FileDetail.class);
    }

    @Override
    public int create(FileDetail data) throws SQLException {

        if (data.getComment() != null) {
            data.setCommentType(FileDetail.CommentType.TEXT.name());

            upsertCommentMessage(data);


        } else if (data.getSticker() != null) {
            data.setCommentType(FileDetail.CommentType.STICKER.name());

            upsertStickerComment(data);
        }

        return super.create(data);
    }

    public void upsertStickerComment(FileDetail data) throws SQLException {
        Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao =
                DaoManager.createDao(getConnectionSource(), ResMessages.OriginalMessage.IntegerWrapper.class);

        DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq("commentStickerOf_id", data.getSticker().id);

        for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : data.getSticker().shareEntities) {
            shareEntity.setCommentStickerOf(data.getSticker());
            dao.create(shareEntity);
        }

        DaoManager.createDao(getConnectionSource(), ResMessages.StickerContent.class)
                .createOrUpdate(data.getSticker().content);
        DaoManager.createDao(getConnectionSource(), ResMessages.CommentStickerMessage.class)
                .createOrUpdate(data.getSticker());
    }

    public void upsertCommentMessage(FileDetail data) throws SQLException {
        Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao =
                DaoManager.createDao(getConnectionSource(), ResMessages.OriginalMessage.IntegerWrapper.class);

        DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq("commentOf_id", data.getComment().id);

        for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : data.getComment().shareEntities) {
            shareEntity.setCommentOf(data.getComment());
            dao.create(shareEntity);
        }

        DaoManager.createDao(getConnectionSource(), ResMessages.TextContent.class)
                .createOrUpdate(data.getComment().content);
        DaoManager.createDao(getConnectionSource(), ResMessages.CommentMessage.class)
                .createOrUpdate(data.getComment());
    }

    @Override
    public int update(FileDetail data) throws SQLException {

        if (data.getComment() != null) {
            data.setCommentType(FileDetail.CommentType.TEXT.name());
            upsertCommentMessage(data);
        } else if (data.getSticker() != null) {
            data.setCommentType(FileDetail.CommentType.STICKER.name());
            upsertStickerComment(data);
        }
        return super.update(data);
    }

    @Override
    public List<FileDetail> query(PreparedQuery<FileDetail> preparedQuery) throws SQLException {
        List<FileDetail> fileDetails = super.query(preparedQuery);


        for (FileDetail fileDetail : fileDetails) {

            FileDetail.CommentType commentType = FileDetail.CommentType.valueOf(fileDetail.getCommentType());
            switch (commentType) {

                case TEXT:
                    fileDetail.setComment(getComment(fileDetail));
                    break;
                case STICKER:
                    fileDetail.setSticker(getSticker(fileDetail));
                    break;
                case NONE:
                    break;
            }
        }

        return fileDetails;
    }

    private ResMessages.CommentStickerMessage getSticker(FileDetail fileDetail) throws SQLException {
        return DaoManager.createDao(getConnectionSource(), ResMessages.CommentStickerMessage.class)
                .queryBuilder()
                .where()
                .eq("id", fileDetail.getId())
                .queryForFirst();
    }

    private ResMessages.CommentMessage getComment(FileDetail fileDetail) throws SQLException {
        return DaoManager.createDao(getConnectionSource(), ResMessages.CommentMessage.class)
                .queryBuilder()
                .where()
                .eq("id", fileDetail.getId())
                .queryForFirst();

    }

}
