package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.domain.LeftSideMenu;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
public class LeftSideMenuRepository extends LockExecutorTemplate {

    private static LeftSideMenuRepository repository;

    synchronized public static LeftSideMenuRepository getRepository() {
        if (repository == null) {
            repository = new LeftSideMenuRepository();
        }
        return repository;
    }

    public boolean upsertLeftSideMenu(ResLeftSideMenu leftSideMenu) {
        return execute(() -> {
            if (leftSideMenu == null) {
                return false;
            }

            try {
                long selectedTeamId = leftSideMenu.team.id;

                LeftSideMenu rawLeftSideMenu = new LeftSideMenu();
                String rawString = JacksonMapper.getInstance().getObjectMapper().writeValueAsString(leftSideMenu);
                rawLeftSideMenu.setRawLeftSideMenu(rawString);
                rawLeftSideMenu.setTeamId(selectedTeamId);

                Dao<LeftSideMenu, ?> dao = getHelper().getDao(LeftSideMenu.class);
                dao.createOrUpdate(rawLeftSideMenu);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;

        });
    }

    public ResLeftSideMenu getCurrentLeftSideMenu() {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();

                Dao<LeftSideMenu, ?> leftSideMenuDao = getHelper().getDao(LeftSideMenu.class);
                LeftSideMenu leftSideMenu = leftSideMenuDao.queryBuilder()
                        .where()
                        .eq("teamId", selectedTeamId)
                        .queryForFirst();
                return JacksonMapper.getInstance()
                        .getObjectMapper()
                        .readValue(leftSideMenu.getRawLeftSideMenu(), ResLeftSideMenu.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        });
    }

    public ResLeftSideMenu findLeftSideMenuByTeamId(long teamId) {
        return execute(() -> {
            try {
                Dao<LeftSideMenu, ?> leftSideMenuDao = getHelper().getDao(LeftSideMenu.class);
                LeftSideMenu leftSideMenu = leftSideMenuDao.queryBuilder()
                        .where()
                        .eq("teamId", teamId)
                        .queryForFirst();
                if (leftSideMenu != null) {
                    return JacksonMapper.getInstance()
                            .getObjectMapper()
                            .readValue(leftSideMenu.getRawLeftSideMenu(), ResLeftSideMenu.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        });
    }

}
