package com.tosslab.jandi.app.local.orm.dao;

import android.text.TextUtils;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by Steve SeongUg Jung on 15. 7. 28..
 */
public class AccountInfoDaoImpl extends BaseDaoImpl<ResAccountInfo, String> {

    public AccountInfoDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResAccountInfo.class);
    }

    @Override
    public int create(ResAccountInfo data) throws SQLException {

        upsertUserDevice(data);
        upsertUserTeam(data);
        upsertUserEmail(data);

        int result = super.create(data);

        return result;
    }

    @Override
    public int update(ResAccountInfo data) throws SQLException {


        upsertUserDevice(data);
        upsertUserTeam(data);
        upsertUserEmail(data);


        int result = super.update(data);
        return result;
    }

    private void upsertUserEmail(ResAccountInfo accountInfo) throws SQLException {

        Dao<ResAccountInfo.UserEmail, String> userEmailDao
                = DaoManager.createDao(getConnectionSource(), ResAccountInfo.UserEmail.class);

        DeleteBuilder<ResAccountInfo.UserEmail, String> deleteBuilder = userEmailDao.deleteBuilder();
        deleteBuilder.where().eq("accountInfo_id", accountInfo.getId());
        deleteBuilder.delete();

        Collection<ResAccountInfo.UserEmail> emails = accountInfo.getEmails();
        if (emails == null || emails.isEmpty()) {
            return;
        }

        for (ResAccountInfo.UserEmail userEmail : emails) {
            userEmail.setAccountInfo(accountInfo);
            userEmailDao.create(userEmail);
        }

    }

    private void upsertUserTeam(ResAccountInfo accountInfo) throws SQLException {

        Dao<ResAccountInfo.UserTeam, Integer> userTeamDao
                = DaoManager.createDao(getConnectionSource(), ResAccountInfo.UserTeam.class);

        DeleteBuilder<ResAccountInfo.UserTeam, Integer> deleteBuilder = userTeamDao.deleteBuilder();
        deleteBuilder.where().eq("accountInfo_id", accountInfo.getId());
        deleteBuilder.delete();

        Collection<ResAccountInfo.UserTeam> memberships = accountInfo.getMemberships();
        if (memberships == null || memberships.isEmpty()) {
            return;
        }

        int order = 1;
        for (ResAccountInfo.UserTeam userTeam : memberships) {
            if (!TextUtils.equals(userTeam.getStatus(), "enabled")) {
                continue;
            }
            userTeam.setAccountInfo(accountInfo);
            userTeam.setOrder(order++);
            userTeamDao.create(userTeam);
        }

    }

    private void upsertUserDevice(ResAccountInfo accountInfo) throws SQLException {
        Dao<ResAccountInfo.UserDevice, Long> userDeviceDao
                = DaoManager.createDao(getConnectionSource(), ResAccountInfo.UserDevice.class);

        DeleteBuilder<ResAccountInfo.UserDevice, Long> deleteBuilder = userDeviceDao.deleteBuilder();
        deleteBuilder.where().eq("accountInfo_id", accountInfo.getId());
        deleteBuilder.delete();

        Collection<ResAccountInfo.UserDevice> devices = accountInfo.getDevices();
        if (devices == null || devices.isEmpty()) {
            return;
        }

        for (ResAccountInfo.UserDevice userDevice : devices) {
            userDevice.setAccountInfo(accountInfo);
            userDeviceDao.create(userDevice);
        }
    }
}
