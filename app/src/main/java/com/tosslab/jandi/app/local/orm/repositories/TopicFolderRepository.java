package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by tee on 15. 8. 26..
 */
public class TopicFolderRepository {

    private static TopicFolderRepository repository;
    private final OrmDatabaseHelper helper;
    private final Lock lock;

    private TopicFolderRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        lock = new ReentrantLock();
    }

    public static TopicFolderRepository getRepository() {
        if (repository == null) {
            repository = new TopicFolderRepository();
        }
        return repository;
    }

    /**
     * It's for Only TestCode.
     */
    public static void release() {
        repository = null;
    }

    public List<ResFolder> getFolders() {
        try {
            int selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Dao<ResFolder, ?> FolderDao = helper.getDao(ResFolder.class);
            return FolderDao.queryBuilder()
                    .where()
                    .eq("teamId", selectedTeamId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public List<ResFolderItem> getFolderItems() {
        try {
            Dao<ResFolderItem, ?> FolderItemDao = helper.getDao(ResFolderItem.class);
            return FolderItemDao.queryBuilder()
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public boolean upsertFolders(List<ResFolder> resFolders) {
        if (resFolders == null) {
            return true;
        }
        lock.lock();
        boolean result = false;
        try {

            Dao<ResFolder, ?> resFoldersDao = helper.getDao(ResFolder.class);

            for (ResFolder resFolder : resFolders) {
                resFoldersDao.createOrUpdate(resFolder);
            }

            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            lock.unlock();
            return result;
        }
    }

    public boolean upsertFolderItems(List<ResFolderItem> resFolderItems) {
        lock.lock();
        boolean result = false;
        try {
            Dao<ResFolderItem, ?> resFolderItemsDao = helper.getDao(ResFolderItem.class);

            for (ResFolderItem resFolderItem : resFolderItems) {
                resFolderItemsDao.createOrUpdate(resFolderItem);
            }
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            lock.unlock();
            return result;
        }
    }

    public boolean removeFolder(int folderId) {
        lock.lock();
        boolean result = false;
        try {
            Dao<ResFolder, ?> resFolderDao = helper.getDao(ResFolder.class);
            DeleteBuilder<ResFolder, ?> deleteBuilder = resFolderDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("id", folderId);
            deleteBuilder.delete();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            lock.unlock();
            return result;
        }
    }

    public boolean removeFolderItem(int roomId) {
        lock.lock();
        boolean result = false;
        try {
            Dao<ResFolderItem, ?> resFolderItemDao = helper.getDao(ResFolderItem.class);
            DeleteBuilder<ResFolderItem, ?> deleteBuilder = resFolderItemDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("roomId", roomId);
            deleteBuilder.delete();
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            lock.unlock();
            return result;
        }
    }

}