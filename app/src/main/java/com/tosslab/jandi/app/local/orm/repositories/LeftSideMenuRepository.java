package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.LeftSideMenu;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JacksonMapper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
public class LeftSideMenuRepository {
    private static LeftSideMenuRepository repository;
    private final OrmDatabaseHelper helper;
    private final Lock lock;

    private LeftSideMenuRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        lock = new ReentrantLock();
    }

    public static LeftSideMenuRepository getRepository() {
        if (repository == null) {
            repository = new LeftSideMenuRepository();
        }

        return repository;
    }

    /**
     * It's for Only TestCode.
     */
    public static void release() {
        repository = null;
    }

    public boolean upsertLeftSideMenu(ResLeftSideMenu leftSideMenu) {
        lock.lock();
        try {
            int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();

            LeftSideMenu rawLeftSideMenu = new LeftSideMenu();
            String rawString = JacksonMapper.getInstance().getObjectMapper().writeValueAsString(leftSideMenu);
            rawLeftSideMenu.setRawLeftSideMenu(rawString);
            rawLeftSideMenu.setTeamId(selectedTeamId);

            Dao<LeftSideMenu, ?> dao = helper.getDao(LeftSideMenu.class);
            dao.createOrUpdate(rawLeftSideMenu);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return false;
    }

    public ResLeftSideMenu getCurrentLeftSideMenu() {
        lock.lock();
        try {
            int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();

            Dao<LeftSideMenu, ?> leftSideMenuDao = helper.getDao(LeftSideMenu.class);
            LeftSideMenu leftSideMenu = leftSideMenuDao.queryBuilder()
                    .where()
                    .eq("teamId", selectedTeamId)
                    .queryForFirst();
            return JacksonMapper.getInstance()
                    .getObjectMapper()
                    .readValue(leftSideMenu.getRawLeftSideMenu(), ResLeftSideMenu.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public ResLeftSideMenu findLeftSideMenuByTeamId(int teamId) {
        lock.lock();
        try {
            Dao<LeftSideMenu, ?> leftSideMenuDao = helper.getDao(LeftSideMenu.class);
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
        } finally {
            lock.unlock();
        }
        return null;
    }

}
