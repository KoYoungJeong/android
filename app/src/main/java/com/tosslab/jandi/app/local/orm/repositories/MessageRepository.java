package com.tosslab.jandi.app.local.orm.repositories;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 7. 21..
 */
public class MessageRepository {

    private static MessageRepository repository;
    private OrmDatabaseHelper helper;

    public MessageRepository(Context context) {
        helper = OpenHelperManager.getHelper(context, OrmDatabaseHelper.class);
    }

    public static MessageRepository getRepository() {

        if (repository == null) {
            repository = new MessageRepository(JandiApplication.getContext());
        }
        return repository;
    }

    public void upsertMessages(List<ResMessages.Link> messages) {
        try {
            Dao<ResMessages.Link, ?> dao = helper.getDao(ResMessages.Link.class);

            for (ResMessages.Link message : messages) {
                upsertEventInfo(message.info);
                dao.createOrUpdate(message);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void upsertEventInfo(ResMessages.EventInfo info) throws SQLException {

        if (info instanceof ResMessages.CreateEvent) {

            ResMessages.CreateEvent createEvent = (ResMessages.CreateEvent) info;

            if (createEvent.createInfo instanceof ResMessages.PublicCreateInfo) {
                helper.getDao(ResMessages.PublicCreateInfo.class).createOrUpdate((ResMessages
                        .PublicCreateInfo) createEvent.createInfo);
            } else {
                helper.getDao(ResMessages.PrivateCreateInfo.class).createOrUpdate((ResMessages
                        .PrivateCreateInfo) createEvent.createInfo);
            }

            helper.getDao(ResMessages.CreateEvent.class).createOrUpdate(createEvent);


        } else if (info instanceof ResMessages.JoinEvent) {
            helper.getDao(ResMessages.JoinEvent.class).createOrUpdate((ResMessages.JoinEvent) info);

        } else if (info instanceof ResMessages.InviteEvent) {
            helper.getDao(ResMessages.InviteEvent.class).createOrUpdate((ResMessages.InviteEvent) info);

        } else if (info instanceof ResMessages.LeaveEvent) {
            helper.getDao(ResMessages.LeaveEvent.class).createOrUpdate((ResMessages.LeaveEvent) info);

        } else if (info instanceof ResMessages.AnnouncementCreateEvent) {
            ResMessages.AnnouncementCreateEvent info1 = (ResMessages.AnnouncementCreateEvent) info;
            helper.getDao(ResMessages.AnnouncementCreateEvent.Info.class)
                    .createOrUpdate(info1.getEventInfo());
            helper.getDao(ResMessages.AnnouncementCreateEvent.class).createOrUpdate(info1);

        } else if (info instanceof ResMessages.AnnouncementDeleteEvent) {

            helper.getDao(ResMessages.AnnouncementDeleteEvent.class).createOrUpdate((ResMessages.AnnouncementDeleteEvent) info);

        } else if (info instanceof ResMessages.AnnouncementUpdateEvent) {
            ResMessages.AnnouncementUpdateEvent info1 = (ResMessages.AnnouncementUpdateEvent) info;
            helper.getDao(ResMessages.AnnouncementUpdateEvent.Info.class)
                    .createOrUpdate(info1.getEventInfo());

            helper.getDao(ResMessages.AnnouncementUpdateEvent.class).createOrUpdate(info1);

        }

    }
}
