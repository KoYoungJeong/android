package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.RealmLong;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmResults;

public class FolderRepository extends RealmRepository {
    private static FolderRepository instance;

    synchronized public static FolderRepository getInstance() {
        if (instance == null) {
            instance = new FolderRepository();
        }
        return instance;
    }

    public List<Folder> getFolders(long teamId) {
        return execute((realm) -> {
            RealmResults<Folder> it = realm.where(Folder.class)
                    .equalTo("teamId", teamId)
                    .findAll();
            if (it != null) {
                return realm.copyFromRealm(it);
            } else {
                return null;
            }
        });
    }

    public boolean addFolder(long teamId, Folder folder) {
        return execute((realm) -> {

            InitialInfo initialInfo = realm.where(InitialInfo.class)
                    .equalTo("teamId", teamId)
                    .findFirst();

            folder.set_id(teamId + "_" + folder.getId());
            folder.setTeamId(teamId);

            realm.executeTransaction(realm1 -> {
                initialInfo.getFolders().add(folder);
            });
            return true;
        });
    }

    public boolean updateFolderName(long folderId, String name) {
        return execute((realm) -> {

            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();

            Folder folder = realm.where(Folder.class)
                    .equalTo("_id", selectedTeamId + "_" + folderId)
                    .findFirst();

            if (folder != null) {
                realm.executeTransaction(realm1 -> folder.setName(name));
                return true;
            }

            return false;
        });
    }

    public boolean updateFolderSeq(long teamId, long folderId, int newSeq) {
        return execute((realm) -> {

            Folder changedFolder = realm.where(Folder.class)
                    .equalTo("_id", teamId + "_" + folderId)
                    .findFirst();

            if (changedFolder == null) {
                return false;
            }

            int diffSeq;
            if (changedFolder.getSeq() > newSeq) {
                diffSeq = 1;
            } else {
                diffSeq = -1;
            }

            RealmResults<Folder> folders = realm.where(Folder.class)
                    .equalTo("teamId", teamId)
                    .greaterThanOrEqualTo("seq", newSeq)
                    .findAll();
            realm.executeTransaction(realm1 -> {

                for (Folder folder : folders) {
                    folder.setSeq(folder.getSeq() + diffSeq);
                }

                changedFolder.setSeq(newSeq);
            });

            return true;
        });
    }

    public boolean deleteFolder(long folderId) {
        return execute((realm) -> {

            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            Folder first = realm.where(Folder.class)
                    .equalTo("_id", selectedTeamId + "_" + folderId)
                    .findFirst();
            if (first != null) {
                realm.executeTransaction(realm1 -> first.deleteFromRealm());
            }

            return true;
        });
    }

    public boolean addTopic(long folderId, long roomId) {
        return execute((realm) -> {

            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            Folder folder = realm.where(Folder.class)
                    .equalTo("_id", teamId + "_" + folderId)
                    .findFirst();

            if (folder == null) {
                return false;
            }

            realm.executeTransaction(realm1 -> {

                RealmList<RealmLong> roomIds = folder.getRoomIds();

                if (roomIds == null) {
                    roomIds = new RealmList<>();
                    folder.setRoomIds(roomIds);
                }

                boolean contains = false;
                for (RealmLong id : roomIds) {
                    if (id.getValue() == roomId) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    RealmLong object = realm.createObject(RealmLong.class);
                    object.setValue(roomId);
                    roomIds.add(object);
                }
            });


            return false;
        });
    }

    public boolean removeTopic(long folderId, long roomId) {
        return execute((realm) -> {

            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            Folder folder = realm.where(Folder.class)
                    .equalTo("_id", teamId + "_" + folderId)
                    .findFirst();

            if (folder == null) {
                return false;
            }

            if (folder.getRoomIds() == null || folder.getRoomIds().isEmpty()) {
                return true;
            }

            realm.executeTransaction(realm1 -> {
                for (RealmLong id : folder.getRoomIds()) {
                    if (id.getValue() == roomId) {
                        id.deleteFromRealm();
                        break;
                    }
                }
            });

            return false;
        });
    }

    public List<FolderExpand> getFolderExpands() {
        return execute((realm) -> {

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
        return execute((realm) -> {
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
        return execute((realm) -> {

            RealmResults<Folder> folders = realm.where(Folder.class)
                    .equalTo("teamId", teamId)
                    .findAll();
            realm.executeTransaction(realm1 -> {
                for (Folder folder : folders) {
                    if (folder.getRoomIds() != null) {
                        for (RealmLong roomId : folder.getRoomIds()) {
                            if (roomIds.contains(roomId.getValue())) {
                                roomId.deleteFromRealm();
                                break;
                            }
                        }
                    }
                }
            });

            return true;
        });
    }
}
