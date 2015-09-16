package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.BadgeCount;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Steve SeongUg Jung on 15. 7. 24..
 */
public class BadgeCountRepository {

    private static BadgeCountRepository repository;
    private OrmDatabaseHelper helper;
    private final Lock lock;

    public BadgeCountRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        lock = new ReentrantLock();
    }

    public static BadgeCountRepository getRepository() {
        if (repository == null) {
            repository = new BadgeCountRepository();
        }
        return repository;
    }

    /**
     * It's for Only TestCode.
     */
    public static void release() {
        repository = null;
    }

    public void upsertBadgeCount(int teamId, int badgeCount) {
        lock.lock();
        try {
            Dao<BadgeCount, ?> dao = helper.getDao(BadgeCount.class);
            dao.createOrUpdate(new BadgeCount(teamId, badgeCount));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public int findBadgeCountByTeamId(int teamId) {
        lock.lock();
        try {
            Dao<BadgeCount, ?> dao = helper.getDao(BadgeCount.class);
            BadgeCount badgeCount = dao.queryBuilder()
                    .where()
                    .eq("teamId", teamId)
                    .queryForFirst();
            return badgeCount.getBadgeCount();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return 0;
    }

    public int getTotalBadgeCount() {
        lock.lock();
        try {
            Dao<BadgeCount, ?> dao = helper.getDao(BadgeCount.class);
            List<BadgeCount> badgeCountList = dao.queryForAll();

            int totalBadgeCount = 0;
            for (BadgeCount badgeCount : badgeCountList) {
                totalBadgeCount += badgeCount.getBadgeCount();
            }

            return totalBadgeCount;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return 0;
    }

    public void deleteAll() {
        lock.lock();
        try {
            Dao<BadgeCount, ?> dao = helper.getDao(BadgeCount.class);
            dao.deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
