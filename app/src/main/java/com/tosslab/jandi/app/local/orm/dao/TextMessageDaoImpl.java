package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 8. 8..
 */
public class TextMessageDaoImpl extends BaseDaoImpl<ResMessages.TextMessage, Integer> {
    public TextMessageDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.TextMessage.class);
    }

    @Override
    public int create(ResMessages.TextMessage data) throws SQLException {
        upsertTextMessage(data);
        return super.create(data);
    }

    @Override
    public int update(ResMessages.TextMessage data) throws SQLException {
        upsertTextMessage(data);
        return super.update(data);
    }

    public void upsertTextMessage(ResMessages.TextMessage textMessage) throws SQLException {

        Dao<ResMessages.OriginalMessage.IntegerWrapper, ?> dao = DaoManager.createDao
                (connectionSource, ResMessages
                        .OriginalMessage.IntegerWrapper.class);
        DeleteBuilder<ResMessages.OriginalMessage.IntegerWrapper, ?> deleteBuilder
                = dao.deleteBuilder();

        deleteBuilder.where().eq("textOf_id", textMessage.id);
        deleteBuilder.delete();

        if (textMessage.shareEntities != null && !textMessage.shareEntities.isEmpty()) {
            for (ResMessages.OriginalMessage.IntegerWrapper shareEntity : textMessage.shareEntities) {
                shareEntity.setTextOf(textMessage);
                dao.create(shareEntity);
            }
        }

        Dao<MentionObject, ?> mentionObjectDao = DaoManager.createDao(connectionSource, MentionObject.class);
        DeleteBuilder<MentionObject, ?> mentionObjectDeleteBuilder = mentionObjectDao.deleteBuilder();
        mentionObjectDeleteBuilder.where().eq("textOf_id", textMessage.id);
        mentionObjectDeleteBuilder.delete();

        if (textMessage.mentions != null && !textMessage.mentions.isEmpty()) {
            for (MentionObject mention : textMessage.mentions) {
                mention.setTextOf(textMessage);
                mentionObjectDao.create(mention);
            }
        }

        textMessage.content.textMessage = textMessage;

        Dao<ResMessages.TextContent, ?> textContentDao = DaoManager.createDao(connectionSource, ResMessages.TextContent.class);
        DeleteBuilder<ResMessages.TextContent, ?> textContentDeleteBuilder = textContentDao.deleteBuilder();
        textContentDeleteBuilder.where()
                .eq("textMessage_id", textMessage.id);
        textContentDeleteBuilder.delete();

        textContentDao.create(textMessage.content);

        if (hasLinkPreview(textMessage)) {
            Dao<ResMessages.LinkPreview, ?> linkPreviewDao = DaoManager.createDao
                    (connectionSource, ResMessages
                            .LinkPreview.class);
            linkPreviewDao.createOrUpdate(textMessage.linkPreview);
        }
    }

    private boolean hasLinkPreview(ResMessages.TextMessage textMessage) {

        ResMessages.LinkPreview linkPreview = textMessage.linkPreview;
        return linkPreview != null && !linkPreview.isEmpty();
    }
}
