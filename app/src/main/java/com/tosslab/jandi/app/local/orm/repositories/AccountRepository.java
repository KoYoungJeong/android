package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
public class AccountRepository {

    private static AccountRepository repository;
    private final OrmDatabaseHelper helper;
    private final Lock lock;

    private AccountRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        lock = new ReentrantLock();
    }

    public static AccountRepository getRepository() {
        if (repository == null) {
            repository = new AccountRepository();
        }

        return repository;
    }

    public void upsertAccountAllInfo(ResAccountInfo accountInfo) {
        lock.lock();
        try {
            Dao<ResAccountInfo, String> accountInfoDao = helper.getDao(ResAccountInfo.class);

            accountInfoDao.createOrUpdate(accountInfo);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

    }

    public ResAccountInfo.UserTeam getTeamInfo(int teamId) {
        lock.lock();
        try {
            Dao<ResAccountInfo.UserTeam, Integer> dao = helper.getDao(ResAccountInfo.UserTeam
                    .class);

            QueryBuilder<ResAccountInfo.UserTeam, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq("teamId", teamId);
            ResAccountInfo.UserTeam userTeam = queryBuilder.queryForFirst();

            return userTeam;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public List<ResAccountInfo.UserEmail> getAccountEmails() {
        lock.lock();
        try {
            Dao<ResAccountInfo.UserEmail, String> dao = helper.getDao(ResAccountInfo.UserEmail
                    .class);
            QueryBuilder<ResAccountInfo.UserEmail, String> queryBuilder = dao.queryBuilder();
            return queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return new ArrayList<>();
    }

    public int deleteAccountInfo() {
        lock.lock();
        try {
            Dao<ResAccountInfo, String> dao = helper.getDao(ResAccountInfo.class);
            return dao.deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            lock.unlock();
        }

    }

    public List<ResAccountInfo.UserTeam> getAccountTeams() {
        lock.lock();
        try {
            Dao<ResAccountInfo.UserTeam, Integer> dao = helper.getDao(ResAccountInfo.UserTeam
                    .class);
            return dao.queryBuilder()
                    .orderBy("order", true)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return new ArrayList<>();
    }

    public long updateSelectedTeamInfo(int teamId) {
        lock.lock();
        try {
            Dao<SelectedTeam, Long> dao = helper.getDao(SelectedTeam.class);

            dao.deleteBuilder().delete();

            SelectedTeam selectedTeam = new SelectedTeam();
            selectedTeam.setSelectedTeamId(teamId);

            dao.createOrUpdate(selectedTeam);

            return selectedTeam.get_id();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            lock.unlock();
        }
    }

    public int getSelectedTeamId() {
        lock.lock();
        try {
            Dao<SelectedTeam, Long> dao = helper.getDao(SelectedTeam.class);
            SelectedTeam selectedTeam = dao.queryForId(SelectedTeam.DEFAULT_ID);

            if (selectedTeam == null) {
                return 0;
            }

            int selectedTeamId = selectedTeam.getSelectedTeamId();
            return selectedTeamId;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return 0;
    }

    public ResAccountInfo.UserTeam getSelectedTeamInfo() {
        lock.lock();
        try {
            int selectedTeamId = getSelectedTeamId();

            Dao<ResAccountInfo.UserTeam, Integer> teamDao = helper.getDao(ResAccountInfo
                    .UserTeam.class);
            ResAccountInfo.UserTeam userTeam = teamDao.queryForId(selectedTeamId);

            return userTeam;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }


    public void upsertUserEmail(Collection<ResAccountInfo.UserEmail> userEmails) throws
            SQLException {
        lock.lock();
        try {
            Dao<ResAccountInfo, String> accountDao = helper.getDao(ResAccountInfo.class);
            ResAccountInfo accountInfo = accountDao.queryBuilder().queryForFirst();
            Dao<ResAccountInfo.UserEmail, String> userEmailDao = helper.getDao(ResAccountInfo
                    .UserEmail.class);
            DeleteBuilder<ResAccountInfo.UserEmail, String> deleteBuilder = userEmailDao.deleteBuilder();
            deleteBuilder.where().eq("accountInfo_id", accountInfo.getId());
            deleteBuilder.delete();

            for (ResAccountInfo.UserEmail userEmail : userEmails) {
                userEmail.setAccountInfo(accountInfo);
                userEmailDao.create(userEmail);
            }
        } finally {
            lock.unlock();
        }

    }

    public ResAccountInfo getAccountInfo() {
        lock.lock();

        try {
            Dao<ResAccountInfo, String> dao = helper.getDao(ResAccountInfo.class);
            return dao.queryBuilder().queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void clearAccountData() {
        lock.lock();
        try {
            helper.getDao(ResAccountInfo.class)
                    .deleteBuilder()
                    .delete();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
