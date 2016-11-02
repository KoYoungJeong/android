package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public boolean updatePhotoUrl(long memberId, String photoUrl) {
        return execute(() -> {
            try {
                Dao<Human, Long> dao = getHelper().getDao(Human.class);
                UpdateBuilder<Human, Long> humanUpdateBuilder = dao.updateBuilder();
                humanUpdateBuilder.updateColumnValue("photoUrl", photoUrl)
                        .where()
                        .eq("id", memberId);
                return humanUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public boolean updateHuman(Human member) {
        return execute(() -> {

            try {
                Dao<Human, Long> dao = getHelper().getDao(Human.class);
                Human savedHuman = dao.queryForId(member.getId());
                member.setInitialInfo(savedHuman.getInitialInfo());
                return dao.update(member) > 0;

            } catch (SQLException e) {
                e.printStackTrace();
            }


            return false;
        });
    }

    public boolean addHuman(long teamId, Human member) {
        return execute(() -> {
            try {
                Dao<Human, ?> dao = getHelper().getDao(Human.class);
                InitialInfo initialInfo = new InitialInfo();
                initialInfo.setTeamId(teamId);
                member.setInitialInfo(initialInfo);
                return dao.create(member) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateStarred(long memberId, boolean isStarred) {
        return execute(() -> {

            try {
                Dao<Human, Object> dao = getDao(Human.class);
                UpdateBuilder<Human, Object> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("isStarred", isStarred)
                        .where()
                        .eq("id", memberId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean containsPhone(String queryNum) {
        return execute(() -> {

            try {
                Dao<Human, Object> humanDao = getDao(Human.class);
                Dao<Human.Profile, Object> dao = getDao(Human.Profile.class);
                QueryBuilder<Human.Profile, Object> profileQueryBuilder = dao.queryBuilder();
                profileQueryBuilder
                        .selectColumns("_id")
                        .where()
                        .like("phoneNumber", "%" + queryNum);

                return humanDao.queryBuilder()
                        .where()
                        .in("id", profileQueryBuilder).countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }


            return false;
        });
    }

    public List<Human> getContainsPhone(String queryNum) {
        return execute(() -> {

            try {
                Dao<Human, Object> humanDao = getDao(Human.class);
                Dao<Human.Profile, Object> dao = getDao(Human.Profile.class);
                QueryBuilder<Human.Profile, Object> profileQueryBuilder = dao.queryBuilder();
                profileQueryBuilder
                        .selectColumns("_id")
                        .where()
                        .like("phoneNumber", "%" + queryNum);

                return humanDao.queryBuilder()
                        .where()
                        .in("id", profileQueryBuilder).query();
            } catch (SQLException e) {
                e.printStackTrace();
            }


            return new ArrayList<Human>(0);
        });
    }
}
