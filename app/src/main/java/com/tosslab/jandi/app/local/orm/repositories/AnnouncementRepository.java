package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.ResAnnouncement;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 24..
 */
public class AnnouncementRepository {

    private static AnnouncementRepository repository;
    private OrmDatabaseHelper helper;

    public AnnouncementRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
    }

    public static AnnouncementRepository getRepository() {
        if (repository == null) {
            repository = new AnnouncementRepository();
        }
        return repository;
    }

    public boolean upsertAnnounce(ResAnnouncement announcement) {
        try {
            Dao<ResAnnouncement, ?> announcementDao = helper.getDao(ResAnnouncement.class);
            announcementDao.createOrUpdate(announcement);

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public ResAnnouncement getAnnounce(int roomId) {
        try {
            Dao<ResAnnouncement, ?> announcementDao = helper.getDao(ResAnnouncement.class);
            return announcementDao.queryBuilder()
                    .where()
                    .eq("roomId", roomId)
                    .queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ResAnnouncement();
    }

    public int deleteAnnouncement(int roomId) {
        try {
            Dao<ResAnnouncement, ?> announcementDao = helper.getDao(ResAnnouncement.class);
            DeleteBuilder<ResAnnouncement, ?> deleteBuilder = announcementDao.deleteBuilder();
            deleteBuilder.where()
                    .eq("teamId", roomId);
            return deleteBuilder.delete();


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
