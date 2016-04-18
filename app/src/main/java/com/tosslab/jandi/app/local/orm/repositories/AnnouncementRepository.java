package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.ResAnnouncement;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 24..
 */
public class AnnouncementRepository extends LockExecutorTemplate {

    private static AnnouncementRepository repository;

    synchronized public static AnnouncementRepository getRepository() {
        if (repository == null) {
            repository = new AnnouncementRepository();
        }
        return repository;
    }

    public boolean upsertAnnounce(ResAnnouncement announcement) {
        return execute(() -> {
            try {
                Dao<ResAnnouncement, ?> announcementDao = getHelper().getDao(ResAnnouncement.class);
                announcementDao.createOrUpdate(announcement);

                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;

        });
    }

    public ResAnnouncement getAnnounce(long roomId) {
        return execute(() -> {
            try {
                Dao<ResAnnouncement, ?> announcementDao = getHelper().getDao(ResAnnouncement.class);
                return announcementDao.queryBuilder()
                        .where()
                        .eq("roomId", roomId)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ResAnnouncement();

        });
    }

    public int deleteAnnouncement(long roomId) {
        return execute(() -> {
            try {
                Dao<ResAnnouncement, ?> announcementDao = getHelper().getDao(ResAnnouncement.class);
                DeleteBuilder<ResAnnouncement, ?> deleteBuilder = announcementDao.deleteBuilder();
                deleteBuilder.where()
                        .eq("teamId", roomId);
                return deleteBuilder.delete();


            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0;

        });
    }
}
