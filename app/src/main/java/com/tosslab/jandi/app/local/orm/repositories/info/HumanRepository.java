package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Human;

import java.sql.SQLException;

public class HumanRepository extends LockExecutorTemplate {
    private static HumanRepository instance;

    synchronized public static HumanRepository getInstance() {
        if (instance == null) {
            instance = new HumanRepository();
        }
        return instance;
    }

    public boolean isHuman(long memberId) {
        return execute(() -> {

            try {
                Dao<Human, Long> dao = getHelper().getDao(Human.class);
                return dao.queryBuilder()
                        .where()
                        .eq("id", memberId)
                        .countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public Human getHuman(long memberId) {
        return execute(() -> {

            try {
                Dao<Human, Long> dao = getHelper().getDao(Human.class);
                return dao.queryForId(memberId);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    public int getMemberCount(long teamId) {
        return execute(() -> {

            try {
                Dao<Human, ?> dao = getHelper().getDao(Human.class);
                return (int) dao.queryBuilder()
                        .where()
                        .eq("initialInfo_id", teamId)
                        .countOf();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;
        });
    }

    public boolean updateStatus(long memberId, String status) {
        return execute(() -> {
            try {
                Dao<Human, ?> dao = getHelper().getDao(Human.class);
                UpdateBuilder<Human, ?> humanUpdateBuilder = dao.updateBuilder();
                humanUpdateBuilder.updateColumnValue("status", status)
                        .where()
                        .eq("id", memberId);

                return humanUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean updateName(long memberId, String name) {
        return execute(() -> {
            try {
                Dao<Human, ?> dao = getHelper().getDao(Human.class);
                UpdateBuilder<Human, ?> humanUpdateBuilder = dao.updateBuilder();
                humanUpdateBuilder.updateColumnValue("name", name)
                        .where()
                        .eq("id", memberId);

                return humanUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateEmail(long memberId, String email) {
        return execute(() -> {
            try {
                Dao<Human, Long> dao = getHelper().getDao(Human.class);
                Human human = dao.queryForId(memberId);

                long profileId = human.getProfile().get_id();
                Dao<Human.Profile, ?> profileDao = getHelper().getDao(Human.Profile.class);
                UpdateBuilder<Human.Profile, ?> profileUpdateBuilder = profileDao.updateBuilder();
                profileUpdateBuilder.updateColumnValue("email", email)
                        .where()
                        .eq("_id", profileId);
                return profileUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateProfile(long memberId, String department, String phoneNumber, String position, String statusMessage) {
        return execute(() -> {
            try {
                Dao<Human, Long> dao = getHelper().getDao(Human.class);
                Human human = dao.queryForId(memberId);
                long profileId = human.getProfile().get_id();
                Dao<Human.Profile, Long> profileDao = getHelper().getDao(Human.Profile.class);
                Human.Profile profile = profileDao.queryForId(profileId);
                profile.setDepartment(department);
                profile.setEmail(phoneNumber);
                profile.setPosition(position);
                profile.setStatusMessage(statusMessage);

                return profileDao.update(profile) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }
}
