package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.start.Human;

import java.sql.SQLException;

public class HumanDaoImpl extends BaseDaoImpl<Human, Long> {
    public HumanDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Human.class);
    }

    @Override
    public int create(Human data) throws SQLException {
        createProfile(data.getProfile());
        int id = super.create(data);
        return id;
    }

    @Override
    public int update(Human data) throws SQLException {
        createProfile(data.getProfile());
        int row = super.update(data);
        return row;
    }

    private void createProfile(Human.Profile profile) throws SQLException {
        if (profile != null) {
            Dao<Human.Profile, ?> dao = DaoManager.createDao(getConnectionSource(), Human.Profile.class);
            dao.createOrUpdate(profile);
        }
    }
}
