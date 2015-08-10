package com.tosslab.jandi.app.local.orm.dao.event;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 28..
 */
public class CreateEventDaoImpl extends BaseDaoImpl<ResMessages.CreateEvent, Long> {
    public CreateEventDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.CreateEvent.class);
    }

    @Override
    public int create(ResMessages.CreateEvent data) throws SQLException {

        upsertCreateInfo(data);

        return super.create(data);
    }

    @Override
    public int update(ResMessages.CreateEvent data) throws SQLException {

        upsertCreateInfo(data);

        return super.update(data);
    }

    public void upsertCreateInfo(ResMessages.CreateEvent data) throws SQLException {
        if (data.createInfo instanceof ResMessages.PublicCreateInfo) {

            Dao<ResMessages.PublicCreateInfo, ?> dao
                    = DaoManager.createDao(getConnectionSource(), ResMessages.PublicCreateInfo.class);
            dao.createOrUpdate((ResMessages.PublicCreateInfo) data.createInfo);

        } else if (data.createInfo instanceof ResMessages.PrivateCreateInfo) {
            Dao<ResMessages.PrivateCreateInfo, ?> dao
                    = DaoManager.createDao(getConnectionSource(), ResMessages.PrivateCreateInfo.class);
            dao.createOrUpdate((ResMessages.PrivateCreateInfo) data.createInfo);
        }
    }
}
