package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.v4.util.LongSparseArray;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;

public class FolderRepository extends LockTemplate {
    private static LongSparseArray<FolderRepository> instance;

    private LongSparseArray<Folder> folders;

    private FolderRepository() {
        super();
        folders = new LongSparseArray<>();
    }

    synchronized public static FolderRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            FolderRepository value = new FolderRepository();
            instance.put(teamId, value);
            return value;

        }
    }

    public static FolderRepository getInstance() {
        return getInstance(TeamInfoLoader.getInstance().getTeamId());
    }

    public List<Folder> getFolders() {
        return execute(() -> {
            int size = folders.size();
            List<Folder> folderList = new ArrayList<>();
            for (int idx = 0; idx < size; idx++) {
                folderList.add(folders.valueAt(idx));
            }
            return folderList;
        });
    }

    private boolean hasFolder(long folderId) {
        return execute(() -> folders.indexOfKey(folderId) >= 0);
    }

    public boolean addFolder(Folder folder) {
        return execute(() -> {
            folders.put(folder.getId(), folder);
            return true;
        });
    }

    public boolean updateFolderName(long folderId, String name) {
        return execute(() -> {

            if (hasFolder(folderId)) {
                folders.get(folderId).setName(name);
                return true;
            }

            return false;
        });
    }

    public boolean updateFolderSeq(long teamId, long folderId, int newSeq) {
        return execute(() -> {

            if (!hasFolder(folderId)) {
                return false;
            }


            Folder changedFolder = folders.get(folderId);

            int diffSeq;
            if (changedFolder.getSeq() > newSeq) {
                diffSeq = 1;
            } else {
                diffSeq = -1;
            }

            Observable.range(0, folders.size())
                    .map(idx -> folders.valueAt(idx))
                    .filter(raw -> raw.getSeq() >= newSeq)
                    .subscribe(raw -> raw.setSeq(raw.getSeq() + diffSeq));

            changedFolder.setSeq(newSeq);

            return true;
        });
    }

    public boolean deleteFolder(long folderId) {
        return execute(() -> {
            folders.remove(folderId);
            return true;
        });
    }

    public boolean addTopic(long folderId, long roomId) {
        return execute(() -> {

            if (hasFolder(folderId)) {
                Folder folder = folders.get(folderId);
                List<Long> rooms = folder.getRooms();
                if (!rooms.contains(roomId)) {
                    rooms.add(roomId);
                }
                return true;
            }

            return false;
        });
    }

    public boolean removeTopic(long folderId, long roomId) {
        return execute(() -> {

            if (hasFolder(folderId)) {
                List<Long> rooms = folders.get(folderId).getRooms();
                if (rooms != null && !rooms.isEmpty()) {
                    rooms.remove(roomId);
                    return true;
                }
            }
            return false;

        });
    }

    public List<FolderExpand> getFolderExpands() {
        return execute(() -> {

            try {
                long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
                Dao<FolderExpand, ?> dao = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                        .getDao(FolderExpand.class);
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
                Dao<FolderExpand, ?> dao = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class)
                        .getDao(FolderExpand.class);
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

            int size = folders.size();
            for (int idx = size - 1; idx >= 0; idx--) {
                Folder folder = folders.valueAt(idx);
                List<Long> rooms = folder.getRooms();
                if (rooms != null && !rooms.isEmpty()) {
                    for (Long roomId : roomIds) {
                        rooms.remove(roomId);
                    }
                }
            }

            return true;
        });
    }

    public boolean clear() {
        return execute(() -> {
            folders.clear();
            return true;
        });
    }

}
