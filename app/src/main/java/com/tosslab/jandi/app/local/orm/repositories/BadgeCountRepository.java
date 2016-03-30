package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.domain.BadgeCount;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 24..
 */
public class BadgeCountRepository extends LockExecutorTemplate {


    public static BadgeCountRepository getRepository() {
        return new BadgeCountRepository();
    }

    public void upsertBadgeCount(long teamId, int badgeCount) {
        execute(() -> {
            try {
                Dao<BadgeCount, ?> dao = getHelper().getDao(BadgeCount.class);
                dao.createOrUpdate(new BadgeCount(teamId, badgeCount));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public int findBadgeCountByTeamId(long teamId) {
        return execute(() -> {
            try {
                Dao<BadgeCount, ?> dao = getHelper().getDao(BadgeCount.class);
                BadgeCount badgeCount = dao.queryBuilder()
                        .where()
                        .eq("teamId", teamId)
                        .queryForFirst();

                return badgeCount != null ? badgeCount.getBadgeCount() : 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public int getTotalBadgeCount() {
        return execute(() -> {
            try {
                Dao<BadgeCount, ?> dao = getHelper().getDao(BadgeCount.class);
                List<BadgeCount> badgeCountList = dao.queryForAll();
                int totalBadgeCount = 0;
                for (BadgeCount badgeCount : badgeCountList) {
                    totalBadgeCount += badgeCount.getBadgeCount();
                }

                return totalBadgeCount;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        });
    }

    public void deleteAll() {
        execute(() -> {
            try {
                Dao<BadgeCount, ?> dao = getHelper().getDao(BadgeCount.class);
                dao.deleteBuilder().delete();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }
}
