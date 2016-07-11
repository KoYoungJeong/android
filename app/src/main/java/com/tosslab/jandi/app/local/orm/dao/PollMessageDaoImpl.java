package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;

/**
 * Created by tonyjs on 16. 6. 15..
 */
public class PollMessageDaoImpl extends BaseDaoImpl<ResMessages.PollMessage, Integer> {

    public PollMessageDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.PollMessage.class);
    }

    @Override
    public int create(ResMessages.PollMessage data) throws SQLException {
        upsertPollMessage(data);
        return super.create(data);
    }

    @Override
    public int update(ResMessages.PollMessage data) throws SQLException {
        upsertPollMessage(data);
        return super.update(data);
    }

    private void upsertPollMessage(ResMessages.PollMessage message) throws SQLException {
        message.content.pollMessage = message;

        Dao<ResMessages.PollContent, ?> pollContentDao =
                DaoManager.createDao(connectionSource, ResMessages.PollContent.class);
        DeleteBuilder<ResMessages.PollContent, ?> pollContentDeleteBuilder = pollContentDao.deleteBuilder();
        pollContentDeleteBuilder.where()
                .eq("pollMessage_id", message.id);
        pollContentDeleteBuilder.delete();

        pollContentDao.create(message.content);

        Dao<ResMessages.PollConnectInfo, ?> connectInfoDao =
                DaoManager.createDao(getConnectionSource(), ResMessages.PollConnectInfo.class);
        DeleteBuilder<ResMessages.PollConnectInfo, ?> connectInfoDeleteBuilder = connectInfoDao.deleteBuilder();
        connectInfoDeleteBuilder.where()
                .eq("pollContentOf_id", message.content._id);
        connectInfoDeleteBuilder.delete();

        if (message.content.connectInfo != null && !message.content.connectInfo.isEmpty()) {
            for (ResMessages.PollConnectInfo connectInfo : message.content.connectInfo) {
                connectInfo.pollContentOf = message.content;
                connectInfoDao.create(connectInfo);
            }
        }
    }
}
