package com.tosslab.jandi.app.local.orm.dao.event;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 28..
 */
public class PrivateCreateInfoDaoImpl extends BaseDaoImpl<ResMessages.PrivateCreateInfo, Integer> {
    public PrivateCreateInfoDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.PrivateCreateInfo.class);
    }

    @Override
    public int create(ResMessages.PrivateCreateInfo data) throws SQLException {

        int result = super.create(data);

        upsertMembers(data);

        return result;
    }

    @Override
    public int update(ResMessages.PrivateCreateInfo data) throws SQLException {
        int result = super.update(data);

        upsertMembers(data);
        return result;

    }

    public void upsertMembers(ResMessages.PrivateCreateInfo data) throws SQLException {
        Dao<ResMessages.PrivateCreateInfo.IntegerWrapper, ?> integerWrapperDao =
                DaoManager.createDao(getConnectionSource(), ResMessages.PrivateCreateInfo.IntegerWrapper.class);

        for (ResMessages.PrivateCreateInfo.IntegerWrapper member : data.members) {
            member.setCreateInfo(data);
        }

        DeleteBuilder<ResMessages.PrivateCreateInfo.IntegerWrapper, ?> deleteBuilder
                = integerWrapperDao.deleteBuilder();
        deleteBuilder
                .where()
                .eq("createInfo_id", data._id);
        deleteBuilder.delete();

        for (ResMessages.PrivateCreateInfo.IntegerWrapper member : data.members) {
            member.setCreateInfo(data);
            integerWrapperDao.create(member);
        }
    }
}
