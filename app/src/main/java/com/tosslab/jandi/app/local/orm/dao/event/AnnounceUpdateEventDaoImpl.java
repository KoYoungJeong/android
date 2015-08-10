package com.tosslab.jandi.app.local.orm.dao.event;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 28..
 */
public class AnnounceUpdateEventDaoImpl extends BaseDaoImpl<ResMessages.AnnouncementUpdateEvent,
        Long> {
    public AnnounceUpdateEventDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.AnnouncementUpdateEvent.class);
    }

    @Override
    public int create(ResMessages.AnnouncementUpdateEvent data) throws SQLException {

        DaoManager.createDao(getConnectionSource(), ResMessages.AnnouncementUpdateEvent.Info.class)
                .createOrUpdate(data.getEventInfo());


        return super.create(data);
    }
}
