package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Absence;

import java.sql.SQLException;

/**
 * Created by tee on 2017. 5. 29..
 */

public class InitialAccountInfoRepository extends LockExecutorTemplate {

    private static InitialAccountInfoRepository instance;

    private InitialAccountInfoRepository() {
    }

    synchronized public static InitialAccountInfoRepository getInstance() {
        if (instance == null) {
            instance = new InitialAccountInfoRepository();
        }
        return instance;
    }

    public boolean upsertAbsenceInfo(Absence absenceInfo) {
        return execute(() -> {
            try {
                deleteAbsenceInfo();
                Dao<Absence, ?> absenceInfoDao = getHelper().getDao(Absence.class);
                Dao.CreateOrUpdateStatus status = absenceInfoDao.createOrUpdate(absenceInfo);
                return status.isCreated() || status.isUpdated();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    protected void deleteAbsenceInfo() throws SQLException {
        getHelper().getDao(Absence.class)
                .deleteBuilder()
                .delete();
    }

    public Absence getAbsenceInfo() {
        try {
            return getHelper().getDao(Absence.class)
                    .queryBuilder()
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
