package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;

public class FolderRepository extends LockExecutorTemplate {
    private static FolderRepository instance;

    synchronized public static FolderRepository getInstance() {
        if (instance == null) {
            instance = new FolderRepository();
        }
        return instance;
    }

    public List<Folder> getFolders(long teamId) {
        return execute(() -> {
            try {
                Dao<Folder, ?> dao = getHelper().getDao(Folder.class);
                return dao.queryBuilder()
                        .orderBy("seq", true)
                        .where()
                        .eq("initialInfo_id", teamId)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new ArrayList<Folder>();
        });
    }

    public boolean addFolder(long teamId, Folder folder) {
        return execute(() -> {
            try {
                Dao<Folder, ?> dao = getHelper().getDao(Folder.class);
                InitialInfo info = new InitialInfo();
                info.setTeamId(teamId);
                folder.setInitialInfo(info);

                dao.createIfNotExists(folder);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public boolean updateFolderName(long folderId, String name) {
        return execute(() -> {
            try {
                Dao<Folder, ?> dao = getHelper().getDao(Folder.class);
                UpdateBuilder<Folder, ?> updateBuilder = dao.updateBuilder();
                updateBuilder
                        .updateColumnValue("name", name)
                        .where()
                        .eq("id", folderId);
                return updateBuilder.update() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public boolean updateFolderSeq(long teamId, long folderId, int seq) {
        return execute(() -> {
            try {
                Dao<Folder, Long> dao = getHelper().getDao(Folder.class);
                Folder folder = dao.queryForId(folderId);
                if (folder.getSeq() == seq) {
                    return true;
                }

                UpdateBuilder<Folder, ?> folderUpdateBuilder = dao.updateBuilder();
                folderUpdateBuilder.updateColumnValue("seq", seq)
                        .where()
                        .eq("id", folderId);
                folderUpdateBuilder.update();

                List<Folder> folders = dao.queryBuilder()
                        .where()
                        .eq("initialInfo_id", teamId)
                        .and()
                        .ge("seq", seq)
                        .and()
                        .ne("id", folderId)
                        .query();
                for (Folder folder1 : folders) {
                    folder1.setSeq(folder1.getSeq() + 1);
                    dao.update(folder1);
                }

                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean deleteFolder(long folderId) {
        return execute(() -> {
            try {
                Dao<Folder, ?> dao = getHelper().getDao(Folder.class);
                DeleteBuilder<Folder, ?> folderDeleteBuilder = dao.deleteBuilder();
                folderDeleteBuilder.where()
                        .eq("id", folderId);
                return folderDeleteBuilder.delete() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean addTopic(long folderId, long roomId) {
        return execute(() -> {
            try {
                Dao<Folder, Long> dao = getHelper().getDao(Folder.class);
                Folder folder = dao.queryForId(folderId);
                Collection<Long> rooms = folder.getRooms();
                if (rooms == null) {
                    rooms = new ArrayList<Long>();
                    folder.setRooms(rooms);
                }
                if (!rooms.contains(roomId)) {
                    rooms.add(roomId);
                    return dao.update(folder) > 0;
                } else {
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean removeTopic(long folderId, long roomId) {
        return execute(() -> {
            try {
                Dao<Folder, Long> dao = getHelper().getDao(Folder.class);
                Folder folder = dao.queryForId(folderId);
                Collection<Long> rooms = folder.getRooms();
                if (rooms != null && rooms.contains(roomId)) {
                    rooms.remove(roomId);
                    return dao.update(folder) > 0;
                } else {
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
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

    public boolean removeTopicOfTeam(long teamId, Collection<Long> roomIds) {
        return execute(() -> {
            try {
                Dao<Folder, Object> dao = getDao(Folder.class);
                List<Folder> folders = dao.queryForEq("initialInfo_id", teamId);

                List<Folder> newFolders = Observable.from(folders)
                        .filter(folder -> {
                            for (Long roomId : roomIds) {

                                if (folder.getRooms() != null && folder.getRooms().contains(roomId)) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .doOnNext(folder -> {
                            for (Long roomId : roomIds) {
                                folder.getRooms().remove(roomId);
                            }
                        })
                        .toList()
                        .toBlocking()
                        .firstOrDefault(new ArrayList<>());
                for (Folder newFolder : newFolders) {
                    dao.update(newFolder);
                }
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}
