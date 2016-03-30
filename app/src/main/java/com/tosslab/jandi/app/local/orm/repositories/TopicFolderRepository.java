package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 15. 8. 26..
 */
public class TopicFolderRepository extends LockExecutorTemplate {


    public static TopicFolderRepository getRepository() {
        return new TopicFolderRepository();
    }

    public List<ResFolder> getFolders() {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<ResFolder, ?> FolderDao = getHelper().getDao(ResFolder.class);
                return FolderDao.queryBuilder()
                        .orderBy("seq", true)
                        .where()
                        .eq("teamId", selectedTeamId)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<ResFolder>();

        });
    }

    public List<ResFolderItem> getFolderItems() {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<ResFolderItem, ?> FolderItemDao = getHelper().getDao(ResFolderItem.class);
                return FolderItemDao.queryBuilder()
                        .where()
                        .eq("teamId", selectedTeamId)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<ResFolderItem>();

        });
    }

    public boolean upsertFolders(List<ResFolder> resFolders) {
        return execute(() -> {
            if (resFolders == null) {
                return true;
            }
            boolean result = false;
            try {

                Dao<ResFolder, ?> resFoldersDao = getHelper().getDao(ResFolder.class);

                for (ResFolder resFolder : resFolders) {
                    resFoldersDao.createOrUpdate(resFolder);
                }

                result = true;
            } catch (SQLException e) {
                e.printStackTrace();
                result = false;
            }
            return result;

        });
    }

    public boolean insertFolders(List<ResFolder> resFolders) {
        return execute(() -> {
            if (resFolders == null) {
                return true;
            }
            boolean result = false;
            try {

                Dao<ResFolder, ?> resFoldersDao = getHelper().getDao(ResFolder.class);

                for (ResFolder resFolder : resFolders) {
                    resFoldersDao.create(resFolder);
                }

                result = true;
            } catch (SQLException e) {
                e.printStackTrace();
                result = false;
            }
            return result;

        });
    }

    public boolean insertFolderItems(List<ResFolderItem> resFolderItems) {
        return execute(() -> {
            boolean result = false;
            try {
                Dao<ResFolderItem, ?> resFolderItemsDao = getHelper().getDao(ResFolderItem.class);

                for (ResFolderItem resFolderItem : resFolderItems) {
                    resFolderItemsDao.create(resFolderItem);
                }
                result = true;
            } catch (SQLException e) {
                e.printStackTrace();
                result = false;
            }
            return result;

        });
    }

    public boolean upsertFolderItems(List<ResFolderItem> resFolderItems) {
        return execute(() -> {
            boolean result = false;
            try {
                Dao<ResFolderItem, ?> resFolderItemsDao = getHelper().getDao(ResFolderItem.class);

                for (ResFolderItem resFolderItem : resFolderItems) {
                    resFolderItemsDao.createOrUpdate(resFolderItem);
                }
                result = true;
            } catch (SQLException e) {
                e.printStackTrace();
                result = false;
            }
            return result;

        });
    }

    public boolean removeFolder(long folderId) {
        return execute(() -> {
            boolean result = false;
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<ResFolder, ?> resFolderDao = getHelper().getDao(ResFolder.class);
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
            }
            return result;

        });
    }

    public boolean removeFolderItem(long roomId) {
        return execute(() -> {
            boolean result = false;
            try {
                Dao<ResFolderItem, ?> resFolderItemDao = getHelper().getDao(ResFolderItem.class);
                DeleteBuilder<ResFolderItem, ?> deleteBuilder = resFolderItemDao.deleteBuilder();
                deleteBuilder
                        .where()
                        .eq("roomId", roomId);
                deleteBuilder.delete();
                result = true;
            } catch (SQLException e) {
                e.printStackTrace();
                result = false;
            }
            return result;

        });
    }

    public boolean removeAllFolders() {
        return execute(() -> {
            boolean result = false;
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<ResFolder, ?> resFolderDao = getHelper().getDao(ResFolder.class);
                DeleteBuilder<ResFolder, ?> deleteBuilder = resFolderDao.deleteBuilder();
                deleteBuilder
                        .where()
                        .eq("teamId", selectedTeamId);
                deleteBuilder.delete();
                result = true;
            } catch (SQLException e) {
                e.printStackTrace();
                result = false;
            }
            return result;

        });
    }

    public boolean removeAllFolderItems() {
        return execute(() -> {
            boolean result = false;
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<ResFolderItem, ?> resFolderItemDao = getHelper().getDao(ResFolderItem.class);
                DeleteBuilder<ResFolderItem, ?> deleteBuilder = resFolderItemDao.deleteBuilder();
                deleteBuilder
                        .where()
                        .eq("teamId", selectedTeamId);
                deleteBuilder.delete();
                result = true;
            } catch (SQLException e) {
                e.printStackTrace();
                result = false;
            }
            return result;

        });
    }

    public List<FolderExpand> getFolderExpands() {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<FolderExpand, ?> dao = getHelper().getDao(FolderExpand.class);
                return dao.queryBuilder()
                        .where()
                        .eq("teamId", selectedTeamId)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<FolderExpand>();

        });
    }

    public boolean upsertFolderExpands(long folderId, boolean expand) {
        return execute(() -> {
            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<FolderExpand, ?> dao = getHelper().getDao(FolderExpand.class);
                FolderExpand data = new FolderExpand();
                data.setExpand(expand);
                data.setFolderId(folderId);
                data.setTeamId(selectedTeamId);
                dao.createOrUpdate(data);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;

        });
    }

    public ResFolderItem getFolderOfTopic(long entityId) {
        return execute(() -> {
            try {
                Dao<ResFolderItem, ?> dao = getHelper().getDao(ResFolderItem.class);
                return dao.queryBuilder()
                        .where()
                        .eq("roomId", entityId)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ResFolderItem();

        });
    }
}