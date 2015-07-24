package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.LeftSideMenu;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JacksonMapper;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
public class LeftSideMenuRepository {
    private static LeftSideMenuRepository repository;

    private LeftSideMenuRepository() { }

    public static LeftSideMenuRepository getRepository() {
        if (repository == null) {
            repository = new LeftSideMenuRepository();
        }

        return repository;
    }


    public boolean upsertLeftSideMenu(ResLeftSideMenu leftSideMenu) {
        try {
            ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();

            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(),
                    OrmDatabaseHelper.class);
            LeftSideMenu rawLeftSideMenu = new LeftSideMenu();
            String rawString = JacksonMapper.getInstance().getObjectMapper().writeValueAsString(leftSideMenu);
            rawLeftSideMenu.setRawLeftSideMenu(rawString);
            rawLeftSideMenu.setTeam(selectedTeamInfo);

            Dao<LeftSideMenu, ?> dao = helper.getDao(LeftSideMenu.class);
            dao.createOrUpdate(rawLeftSideMenu);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }

        return false;
    }

    public ResLeftSideMenu getCurrentLeftSideMenu() {

        try {
            int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(),
                    OrmDatabaseHelper.class);

            Dao<LeftSideMenu, ?> leftSideMenuDao = helper.getDao(LeftSideMenu.class);
            LeftSideMenu leftSideMenu = leftSideMenuDao.queryBuilder().where().eq("team_id", selectedTeamId).queryForFirst();
            return JacksonMapper.getInstance()
                    .getObjectMapper()
                    .readValue(leftSideMenu.getRawLeftSideMenu(), ResLeftSideMenu.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            OpenHelperManager.releaseHelper();
        }

        return null;
    }
}
