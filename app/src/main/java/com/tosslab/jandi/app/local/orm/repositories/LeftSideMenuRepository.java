package com.tosslab.jandi.app.local.orm.repositories;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.LeftSideMenu;
import com.tosslab.jandi.app.local.orm.domain.SelectedTeam;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JacksonMapper;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
public class LeftSideMenuRepository {
    private static LeftSideMenuRepository repository;
    private final OrmDatabaseHelper openHelper;

    public LeftSideMenuRepository(Context context) {
        openHelper = OpenHelperManager.getHelper(context, OrmDatabaseHelper.class);
    }

    public static LeftSideMenuRepository getRepository() {
        if (repository == null) {
            repository = new LeftSideMenuRepository(JandiApplication.getContext());
        }

        return repository;
    }


    public boolean upsertLeftSideMenu(ResLeftSideMenu leftSideMenu) {
        LeftSideMenu rawLeftSideMenu = new LeftSideMenu();
        try {
            String rawString = JacksonMapper.getInstance().getObjectMapper().writeValueAsString(leftSideMenu);
            rawLeftSideMenu.setRawLeftSideMenu(rawString);
            rawLeftSideMenu.setTeam(AccountRepository.getRepository().getSelectedTeamInfo());

            Dao<LeftSideMenu, ?> dao = openHelper.getDao(LeftSideMenu.class);
            dao.createOrUpdate(rawLeftSideMenu);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public ResLeftSideMenu getCurrentLeftSideMenu() {

        try {
            Dao<SelectedTeam, ?> selectedTeamDao = openHelper.getDao(SelectedTeam.class);
            SelectedTeam selectedTeam = selectedTeamDao.queryBuilder().queryForFirst();
            int selectedTeamId = selectedTeam.getSelectedTeamId();

            Dao<LeftSideMenu, ?> leftSideMenuDao = openHelper.getDao(LeftSideMenu.class);
            LeftSideMenu leftSideMenu = leftSideMenuDao.queryBuilder().where().eq("team_id", selectedTeamId).queryForFirst();
            return JacksonMapper.getInstance()
                    .getObjectMapper()
                    .readValue(leftSideMenu.getRawLeftSideMenu(), ResLeftSideMenu.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
