package com.tosslab.jandi.app.local.orm.repositories;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

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


    public void upsertLeftSideMenu(ResLeftSideMenu leftSideMenu) {
        try {
            Dao<ResLeftSideMenu, Long> leftSideMenuDao = openHelper.getDao(ResLeftSideMenu.class);
            Dao<ResLeftSideMenu.Team, Integer> teamDao = openHelper.getDao(ResLeftSideMenu.Team
                    .class);
            Dao<ResLeftSideMenu.User, Integer> userDao = openHelper.getDao(ResLeftSideMenu.User.class);
            Dao<ResLeftSideMenu.AlarmInfo, Long> alarmDao = openHelper.getDao(ResLeftSideMenu
                    .AlarmInfo.class);

            Dao<ResLeftSideMenu.PrivateGroup, Integer> privateTopicDao = openHelper.getDao
                    (ResLeftSideMenu.PrivateGroup.class);
            Dao<ResLeftSideMenu.Channel, Integer> publicTopicDao = openHelper.getDao(ResLeftSideMenu
                    .Channel.class);
            Dao<ResLeftSideMenu.UserThumbNailInfo, Long> thumbDao = openHelper.getDao
                    (ResLeftSideMenu.UserThumbNailInfo.class);
            Dao<ResLeftSideMenu.MessageMarker, Long> markerDao = openHelper.getDao(ResLeftSideMenu
                    .MessageMarker.class);
            Dao<ResLeftSideMenu.ExtraData, Long> extraDataDao = openHelper.getDao(ResLeftSideMenu
                    .ExtraData.class);


            leftSideMenuDao.create(leftSideMenu);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
