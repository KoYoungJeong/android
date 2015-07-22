package com.tosslab.jandi.app.local.orm.repositories;

import android.content.Context;
import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.SelectedTeam;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
public class AccountRepository {

    private static AccountRepository repository;

    private OrmLiteSqliteOpenHelper openHelper;

    private AccountRepository(Context context) {
        openHelper = OpenHelperManager.getHelper(context, OrmDatabaseHelper.class);
    }

    public static AccountRepository getRepository() {
        if (repository == null) {
            repository = new AccountRepository(JandiApplication.getContext());
        }

        return repository;
    }

    public void upsertAccountAllInfo(ResAccountInfo accountInfo) {
        try {
            Dao<ResAccountInfo, String> accountInfoDao = openHelper.getDao(ResAccountInfo.class);
            Dao<ResAccountInfo.UserDevice, Long> userDeviceDao = openHelper.getDao(ResAccountInfo.UserDevice.class);
            Dao<ResAccountInfo.UserTeam, Integer> userTeamDao = openHelper.getDao(ResAccountInfo
                    .UserTeam.class);
            Dao<ResAccountInfo.UserEmail, String> userEmailDao = openHelper.getDao(ResAccountInfo
                    .UserEmail.class);
            Dao<ResAccountInfo.ThumbnailInfo, Long> thumbnailDao = openHelper.getDao(ResAccountInfo.ThumbnailInfo.class);

            ResAccountInfo.ThumbnailInfo photoThumbnailUrl = accountInfo.getThumbnailInfo();
            thumbnailDao.create(photoThumbnailUrl);

            upsertUserDevice(accountInfo, userDeviceDao);
            upsertUserTeam(accountInfo, userTeamDao);
            upsertUserEmail(accountInfo, userEmailDao);

            accountInfoDao.createOrUpdate(accountInfo);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public ResAccountInfo.UserTeam getTeamInfo(int teamId) {
        try {
            Dao<ResAccountInfo.UserTeam, Integer> dao = openHelper.getDao(ResAccountInfo.UserTeam
                    .class);

            QueryBuilder<ResAccountInfo.UserTeam, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("teamId", teamId);
            ResAccountInfo.UserTeam userTeam = queryBuilder.queryForFirst();

            return userTeam;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ResAccountInfo.UserEmail> getAccountEmails() {
        try {
            Dao<ResAccountInfo.UserEmail, String> dao = openHelper.getDao(ResAccountInfo.UserEmail
                    .class);
            QueryBuilder<ResAccountInfo.UserEmail, String> queryBuilder = dao.queryBuilder();
            return queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int deleteAccountInfo() {
        try {
            Dao<ResAccountInfo, String> dao = openHelper.getDao(ResAccountInfo.class);
            return dao.deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<ResAccountInfo.UserTeam> getAccountTeams() {
        try {
            Dao<ResAccountInfo.UserTeam, Integer> dao = openHelper.getDao(ResAccountInfo.UserTeam
                    .class);
            return dao.queryBuilder().query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public long updateSelectedTeamInfo(int teamId) {
        try {
            Dao<SelectedTeam, Long> dao = openHelper.getDao(SelectedTeam.class);

            dao.deleteBuilder().delete();

            SelectedTeam selectedTeam = new SelectedTeam();
            selectedTeam.setSelectedTeamId(teamId);

            dao.createOrUpdate(selectedTeam);

            return selectedTeam.get_id();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getSelectedTeamId() {
        Dao<SelectedTeam, Long> dao = null;
        try {
            dao = openHelper.getDao(SelectedTeam.class);
            SelectedTeam selectedTeam = dao.queryForId(SelectedTeam.DEFAULT_ID);

            if (selectedTeam == null) {
                return 0;
            }

            int selectedTeamId = selectedTeam.getSelectedTeamId();
            return selectedTeamId;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public ResAccountInfo.UserTeam getSelectedTeamInfo() {
        try {
            int selectedTeamId = getSelectedTeamId();

            Dao<ResAccountInfo.UserTeam, Integer> teamDao = openHelper.getDao(ResAccountInfo
                    .UserTeam.class);
            ResAccountInfo.UserTeam userTeam = teamDao.queryForId(selectedTeamId);

            return userTeam;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void upsertUserEmail(Collection<ResAccountInfo.UserEmail> userEmails) throws
            SQLException {
        Dao<ResAccountInfo, String> accountDao = openHelper.getDao(ResAccountInfo.class);
        ResAccountInfo accountInfo = accountDao.queryBuilder().queryForFirst();
        Dao<ResAccountInfo.UserEmail, String> userEmailDao = openHelper.getDao(ResAccountInfo
                .UserEmail.class);
        DeleteBuilder<ResAccountInfo.UserEmail, String> deleteBuilder = userEmailDao.deleteBuilder();
        deleteBuilder.where().eq("accountInfo_id", accountInfo.getId());
        deleteBuilder.delete();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            userEmail.setAccountInfo(accountInfo);
            userEmailDao.create(userEmail);
        }

    }

    private void upsertUserEmail(ResAccountInfo accountInfo, Dao<ResAccountInfo.UserEmail, String> userEmailDao) throws SQLException {
        DeleteBuilder<ResAccountInfo.UserEmail, String> deleteBuilder = userEmailDao.deleteBuilder();
        deleteBuilder.where().eq("accountInfo_id", accountInfo.getId());
        deleteBuilder.delete();

        for (ResAccountInfo.UserEmail userEmail : accountInfo.getEmails()) {
            userEmail.setAccountInfo(accountInfo);
            userEmailDao.create(userEmail);
        }
    }

    private void upsertUserTeam(ResAccountInfo accountInfo, Dao<ResAccountInfo.UserTeam, Integer> userTeamDao) throws SQLException {
        DeleteBuilder<ResAccountInfo.UserTeam, Integer> deleteBuilder = userTeamDao.deleteBuilder();
        deleteBuilder.where().eq("accountInfo_id", accountInfo.getId());
        deleteBuilder.delete();

        for (ResAccountInfo.UserTeam userTeam : accountInfo.getMemberships()) {
            if (!TextUtils.equals(userTeam.getStatus(), "enabled")) {
                continue;
            }
            userTeam.setAccountInfo(accountInfo);
            userTeamDao.create(userTeam);
        }
    }

    private void upsertUserDevice(ResAccountInfo accountInfo, Dao<ResAccountInfo.UserDevice, Long> userDeviceDao) throws SQLException {
        DeleteBuilder<ResAccountInfo.UserDevice, Long> deleteBuilder = userDeviceDao.deleteBuilder();
        deleteBuilder.where().eq("accountInfo_id", accountInfo.getId());
        deleteBuilder.delete();

        for (ResAccountInfo.UserDevice userDevice : accountInfo.getDevices()) {
            userDevice.setAccountInfo(accountInfo);
            userDeviceDao.create(userDevice);
        }
    }

    public ResAccountInfo getAccountInfo() {

        try {
            Dao<ResAccountInfo, String> dao = openHelper.getDao(ResAccountInfo.class);
            return dao.queryBuilder().queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void clearAccountData() {
        try {
            openHelper.getDao(ResAccountInfo.class)
                    .deleteBuilder()
                    .delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}