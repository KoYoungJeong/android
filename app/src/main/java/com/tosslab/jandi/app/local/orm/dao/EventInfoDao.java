package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class EventInfoDao extends BaseDaoImpl<ResMessages.EventInfo, Long> {
    protected EventInfoDao(ConnectionSource connectionSource, Class<ResMessages.EventInfo> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }
}
