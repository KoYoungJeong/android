package com.tosslab.jandi.app.local.orm.dao.event;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 28..
 */
public class PublicCreateInfoDaoImpl extends BaseDaoImpl<ResMessages.PublicCreateInfo, Integer> {
    public PublicCreateInfoDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.PublicCreateInfo.class);
    }

    @Override
    public int create(ResMessages.PublicCreateInfo data) throws SQLException {

        int result = super.create(data);

        upsertMembers(data);

        return result;
    }

    @Override
    public int update(ResMessages.PublicCreateInfo data) throws SQLException {
        int result = super.update(data);

        upsertMembers(data);

        return result;
    }

    public void upsertMembers(ResMessages.PublicCreateInfo data) throws SQLException {
        for (ResMessages.PublicCreateInfo.IntegerWrapper member : data.members) {
            member.setCreateInfo(data);
        }

        DeleteBuilder<ResMessages.PublicCreateInfo.IntegerWrapper, ?> deleteBuilder
                = DaoManager
                .createDao(getConnectionSource(), ResMessages.PublicCreateInfo.IntegerWrapper.class)
                .deleteBuilder();
        deleteBuilder.where()
                .eq("createInfo_id", data._id);
        deleteBuilder.delete();

        for (ResMessages.PublicCreateInfo.IntegerWrapper member : data.members) {
            DaoManager.createDao(getConnectionSource(), ResMessages.PublicCreateInfo.IntegerWrapper.class)
                    .create(member);
        }
    }
}
