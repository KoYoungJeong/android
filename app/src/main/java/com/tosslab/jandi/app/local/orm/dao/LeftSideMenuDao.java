package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class LeftSideMenuDao extends BaseDaoImpl<ResLeftSideMenu, Long> {
    public LeftSideMenuDao(ConnectionSource connectionSource, Class<ResLeftSideMenu> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public ResLeftSideMenu queryForFirst(PreparedQuery<ResLeftSideMenu> preparedQuery) throws SQLException {
        return super.queryForFirst(preparedQuery);
    }
}
