package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                return dao.queryForEq("initialInfo_id", teamId);
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

                return dao.create(folder) > 0;
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
}
