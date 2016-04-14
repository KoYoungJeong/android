package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
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
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Dao<ResFolder, ?> FolderDao = helper.getDao(ResFolder.class);
            return FolderDao.queryBuilder()
                    .orderBy("seq", true)
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
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Dao<ResFolderItem, ?> FolderItemDao = helper.getDao(ResFolderItem.class);
            return FolderItemDao.queryBuilder()
                    .where()
                    .eq("teamId", selectedTeamId)
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

    public boolean insertFolders(List<ResFolder> resFolders) {
        if (resFolders == null) {
            return true;
        }
        lock.lock();
        boolean result = false;
        try {

            Dao<ResFolder, ?> resFoldersDao = helper.getDao(ResFolder.class);

            for (ResFolder resFolder : resFolders) {
                resFoldersDao.create(resFolder);
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

    public boolean insertFolderItems(List<ResFolderItem> resFolderItems) {
        lock.lock();
        boolean result = false;
        try {
            Dao<ResFolderItem, ?> resFolderItemsDao = helper.getDao(ResFolderItem.class);

            for (ResFolderItem resFolderItem : resFolderItems) {
                resFolderItemsDao.create(resFolderItem);
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

    public boolean removeFolder(long folderId) {
        lock.lock();
        boolean result = false;
        try {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Dao<ResFolder, ?> resFolderDao = helper.getDao(ResFolder.class);
            DeleteBuilder<ResFolder, ?> deleteBuilder = resFolderDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("teamId", selectedTeamId)
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

    public boolean removeFolderItem(long roomId) {
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

    public boolean removeAllFolders() {
        lock.lock();
        boolean result = false;
        try {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Dao<ResFolder, ?> resFolderDao = helper.getDao(ResFolder.class);
            DeleteBuilder<ResFolder, ?> deleteBuilder = resFolderDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("teamId", selectedTeamId);
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

    public boolean removeAllFolderItems() {
        lock.lock();
        boolean result = false;
        try {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Dao<ResFolderItem, ?> resFolderItemDao = helper.getDao(ResFolderItem.class);
            DeleteBuilder<ResFolderItem, ?> deleteBuilder = resFolderItemDao.deleteBuilder();
            deleteBuilder
                    .where()
                    .eq("teamId", selectedTeamId);
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

    public List<FolderExpand> getFolderExpands() {
        lock.lock();
        try {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Dao<FolderExpand, ?> dao = helper.getDao(FolderExpand.class);
            return dao.queryBuilder()
                    .where()
                    .eq("teamId", selectedTeamId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return new ArrayList<>();
    }

    public boolean upsertFolderExpands(long folderId, boolean expand) {
        lock.lock();
        try {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Dao<FolderExpand, ?> dao = helper.getDao(FolderExpand.class);
            FolderExpand data = new FolderExpand();
            data.setExpand(expand);
            data.setFolderId(folderId);
            data.setTeamId(selectedTeamId);
            dao.createOrUpdate(data);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return false;

    }

    public ResFolderItem getFolderOfTopic(long entityId) {
        lock.lock();
        try {
            Dao<ResFolderItem, ?> dao = helper.getDao(ResFolderItem.class);
            ResFolderItem resFolderItem = dao.queryBuilder()
                    .where()
                    .eq("roomId", entityId)
                    .queryForFirst();
            return resFolderItem == null ? new ResFolderItem() : resFolderItem;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }

        return new ResFolderItem();
    }
}