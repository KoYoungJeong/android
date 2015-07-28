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
public class InviteEventDaoImpl extends BaseDaoImpl<ResMessages.InviteEvent, Long> {
    public InviteEventDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResMessages.InviteEvent.class);
    }

    @Override
    public int create(ResMessages.InviteEvent data) throws SQLException {

        int result = super.create(data);

        upsertInviteUsers(data);


        return result;
    }

    @Override
    public int update(ResMessages.InviteEvent data) throws SQLException {
        int result = super.update(data);

        upsertInviteUsers(data);

        return result;
    }

    public void upsertInviteUsers(ResMessages.InviteEvent data) throws SQLException {
        DeleteBuilder<ResMessages.InviteEvent.IntegerWrapper, ?> deleteBuilder
                = DaoManager.createDao(getConnectionSource(), ResMessages.InviteEvent.IntegerWrapper.class)
                .deleteBuilder();

        deleteBuilder.where()
                .eq("inviteEvent_id", data._id);
        deleteBuilder.delete();

        for (ResMessages.InviteEvent.IntegerWrapper inviteUser : data.inviteUsers) {
            inviteUser.setInviteEvent(data);
            DaoManager.createDao(getConnectionSource(), ResMessages.InviteEvent.IntegerWrapper.class)
                    .create(inviteUser);
        }
    }
}
