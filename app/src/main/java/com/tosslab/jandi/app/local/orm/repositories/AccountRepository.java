package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.domain.SelectedTeam;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
public class AccountRepository extends LockExecutorTemplate {

    private static AccountRepository repository;

    synchronized public static AccountRepository getRepository() {
        if (repository == null) {
            repository = new AccountRepository();
        }
        return repository;
    }

    public boolean upsertAccountAllInfo(ResAccountInfo accountInfo) {
        return execute(() -> {
            clearAccountData();
            try {
                Dao<ResAccountInfo, String> accountInfoDao = getHelper().getDao(ResAccountInfo.class);

                Dao.CreateOrUpdateStatus status = accountInfoDao.createOrUpdate(accountInfo);
                return status.isCreated() || status.isUpdated();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;

        });
    }

    public ResAccountInfo.UserTeam getTeamInfo(long teamId) {
        return execute(() -> {
            try {
                Dao<ResAccountInfo.UserTeam, Long> dao = getHelper().getDao(ResAccountInfo.UserTeam
                        .class);

                QueryBuilder<ResAccountInfo.UserTeam, Long> queryBuilder = dao.queryBuilder();
                queryBuilder.where().eq("teamId", teamId);
                ResAccountInfo.UserTeam userTeam = queryBuilder.queryForFirst();

                return userTeam;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;

        });
    }

    public List<ResAccountInfo.UserEmail> getAccountEmails() {
        return execute(() -> {
            try {
                Dao<ResAccountInfo.UserEmail, String> dao = getHelper().getDao(ResAccountInfo.UserEmail
                        .class);
                QueryBuilder<ResAccountInfo.UserEmail, String> queryBuilder = dao.queryBuilder();
                return queryBuilder.query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<ResAccountInfo.UserEmail>();
        });
    }

    public int deleteAccountInfo() {
        return execute(() -> {
            try {
                Dao<ResAccountInfo, String> dao = getHelper().getDao(ResAccountInfo.class);
                return dao.deleteBuilder().delete();
            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }

        });
    }

    public List<ResAccountInfo.UserTeam> getAccountTeams() {
        return execute(() -> {
            try {
                Dao<ResAccountInfo.UserTeam, Integer> dao = getHelper().getDao(ResAccountInfo.UserTeam
                        .class);
                return dao.queryBuilder()
                        .orderBy("order", true)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<ResAccountInfo.UserTeam>();

        });
    }

    public long updateSelectedTeamInfo(long teamId) {
        return execute(() -> {
            try {
                Dao<SelectedTeam, Long> dao = getHelper().getDao(SelectedTeam.class);

                dao.deleteBuilder().delete();

                SelectedTeam selectedTeam = new SelectedTeam();
                selectedTeam.setSelectedTeamId(teamId);

                dao.createOrUpdate(selectedTeam);

                return selectedTeam.get_id();

            } catch (SQLException e) {
                e.printStackTrace();
                return 0l;
            }

        });
    }

    public int removeSelectedTeamInfo() {
        return execute(() -> {
            try {
                Dao<SelectedTeam, Long> dao = getHelper().getDao(SelectedTeam.class);

                return dao.deleteBuilder().delete();


            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }

        });
    }

    public long getSelectedTeamId() {
        return execute(() -> {
            try {
                Dao<SelectedTeam, Long> dao = getHelper().getDao(SelectedTeam.class);
                SelectedTeam selectedTeam = dao.queryForId(SelectedTeam.DEFAULT_ID);

                if (selectedTeam == null) {
                    return 0l;
                }

                return selectedTeam.getSelectedTeamId();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0l;

        });
    }

    public ResAccountInfo.UserTeam getSelectedTeamInfo() {
        return execute(() -> {
            try {
                long selectedTeamId = getSelectedTeamId();

                Dao<ResAccountInfo.UserTeam, Long> teamDao = getHelper().getDao(ResAccountInfo
                        .UserTeam.class);

                return teamDao.queryForId(selectedTeamId);

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;

        });
    }


    public void upsertUserEmail(Collection<ResAccountInfo.UserEmail> userEmails) {
        execute(() -> {
            try {
                Dao<ResAccountInfo, String> accountDao = getHelper().getDao(ResAccountInfo.class);
                ResAccountInfo accountInfo = accountDao.queryBuilder().queryForFirst();
                Dao<ResAccountInfo.UserEmail, String> userEmailDao = getHelper().getDao(ResAccountInfo
                        .UserEmail.class);
                DeleteBuilder<ResAccountInfo.UserEmail, String> deleteBuilder = userEmailDao.deleteBuilder();
                deleteBuilder.where().eq("accountInfo_id", accountInfo.getId());
                deleteBuilder.delete();

                try {
                    userEmailDao.callBatchTasks(() -> {
                        for (ResAccountInfo.UserEmail userEmail : userEmails) {
                            userEmail.setAccountInfo(accountInfo);
                            userEmailDao.create(userEmail);
                        }
                        return null;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });

    }

    public ResAccountInfo getAccountInfo() {
        return execute(() -> {
            try {
                Dao<ResAccountInfo, String> dao = getHelper().getDao(ResAccountInfo.class);
                return dao.queryBuilder().queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;

        });
    }

    public void clearAccountData() {
        execute(() -> {
            try {
                getHelper().getDao(ResAccountInfo.class)
                        .deleteBuilder()
                        .delete();

                getHelper().getDao(ResAccountInfo.UserDevice.class)
                        .deleteBuilder()
                        .delete();

                getHelper().getDao(ResAccountInfo.UserTeam.class)
                        .deleteBuilder()
                        .delete();

                getHelper().getDao(ResAccountInfo.UserEmail.class)
                        .deleteBuilder()
                        .delete();

                getHelper().getDao(ResAccountInfo.ThumbnailInfo.class)
                        .deleteBuilder()
                        .delete();

                getHelper().getDao(SelectedTeam.class)
                        .deleteBuilder()
                        .delete();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public void updateAccountName(String newName) {
        execute(() -> {
            try {
                Dao<ResAccountInfo, ?> dao = getHelper().getDao(ResAccountInfo.class);
                UpdateBuilder<ResAccountInfo, ?> updateBuilder = dao.updateBuilder();

                updateBuilder.updateColumnValue("name", newName);
                updateBuilder.update();

            } catch (SQLException e) {

            }
            return 0;
        });
    }

    public boolean removeTeamInfo(long teamId) {
        return execute(() -> {
            try {
                Dao<ResAccountInfo.UserTeam, Long> dao = getDao(ResAccountInfo.UserTeam.class);
                return dao.deleteById(teamId) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateTeamName(long teamId, String name) {
        return execute(() -> {

            try {
                Dao<ResAccountInfo.UserTeam, Long> dao = getDao(ResAccountInfo.UserTeam.class);
                UpdateBuilder<ResAccountInfo.UserTeam, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("name", name)
                        .where()
                        .eq("teamId", teamId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean updateTeamDomain(long teamId, String domain) {
        return execute(() -> {

            try {
                Dao<ResAccountInfo.UserTeam, Long> dao = getDao(ResAccountInfo.UserTeam.class);
                UpdateBuilder<ResAccountInfo.UserTeam, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("teamDomain", domain)
                        .where()
                        .eq("teamId", teamId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });

    }

    public boolean isMine(long memberId) {
        return execute(() -> {

            try {
                Dao<ResAccountInfo.UserTeam, Long> dao = getDao(ResAccountInfo.UserTeam.class);
                return dao.queryBuilder()
                        .where()
                        .eq("memberId", memberId)
                        .countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean increaseUnread(long teamId) {
        return execute(() -> {
            try {
                Dao<ResAccountInfo.UserTeam, Long> dao = getDao(ResAccountInfo.UserTeam.class);
                UpdateBuilder<ResAccountInfo.UserTeam, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnExpression("unread", "unread + 1")
                        .where()
                        .eq("teamId", teamId);

                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean updateUnread(long teamId, int unreadCount) {
        return execute(() -> {
            try {
                Dao<ResAccountInfo.UserTeam, Long> dao = getDao(ResAccountInfo.UserTeam.class);
                UpdateBuilder<ResAccountInfo.UserTeam, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("unread", unreadCount)
                        .where()
                        .eq("teamId", teamId);

                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean decreaseUnread(long teamId) {
        return execute(() -> {
            try {
                Dao<ResAccountInfo.UserTeam, Long> dao = getDao(ResAccountInfo.UserTeam.class);
                UpdateBuilder<ResAccountInfo.UserTeam, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnExpression("unread", "unread - 1")
                        .where()
                        .eq("teamId", teamId);

                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });

    }
}
