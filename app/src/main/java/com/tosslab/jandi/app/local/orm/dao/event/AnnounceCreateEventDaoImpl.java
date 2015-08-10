package com.tosslab.jandi.app.local.orm.dao.event;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 28..
 */
public class AnnounceCreateEventDaoImpl extends BaseDaoImpl<ResMessages.AnnouncementCreateEvent,
        Long> {
    public AnnounceCreateEventDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.AnnouncementCreateEvent.class);
    }

    @Override
    public int create(ResMessages.AnnouncementCreateEvent data) throws SQLException {

        DaoManager.createDao(getConnectionSource(), ResMessages.AnnouncementCreateEvent.Info.class)
                .createIfNotExists(data.getEventInfo());
        return super.create(data);
    }
}
