package com.tosslab.jandi.app.local.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.tosslab.jandi.app.local.orm.domain.LeftSideMenu;
import com.tosslab.jandi.app.local.orm.domain.SelectedTeam;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
public class OrmDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public OrmLiteSqliteOpenHelper helper;

    public OrmDatabaseHelper(Context context) {
        super(context, "jandi-v2.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            // ResAccountInfo.java
            TableUtils.createTableIfNotExists(connectionSource, ResAccountInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, ResAccountInfo.ThumbnailInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, ResAccountInfo.UserDevice.class);
            TableUtils.createTableIfNotExists(connectionSource, ResAccountInfo.UserEmail.class);
            TableUtils.createTableIfNotExists(connectionSource, ResAccountInfo.UserTeam.class);
            TableUtils.createTableIfNotExists(connectionSource, SelectedTeam.class);

            TableUtils.createTableIfNotExists(connectionSource, LeftSideMenu.class);

            TableUtils.createTableIfNotExists(connectionSource, ResMessages.Link.class);
            TableUtils.createTableIfNotExists(connectionSource, ResMessages.CreateEvent.class);
            TableUtils.createTableIfNotExists(connectionSource, ResMessages.PublicCreateInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, ResMessages.JoinEvent.class);
            TableUtils.createTableIfNotExists(connectionSource, ResMessages.InviteEvent.class);
            TableUtils.createTableIfNotExists(connectionSource, ResMessages.LeaveEvent.class);
            TableUtils.createTableIfNotExists(connectionSource, ResMessages.AnnouncementCreateEvent.class);
            TableUtils.createTableIfNotExists(connectionSource, ResMessages
                    .AnnouncementCreateEvent.Info.class);
            TableUtils.createTableIfNotExists(connectionSource, ResMessages.AnnouncementUpdateEvent.class);
            TableUtils.createTableIfNotExists(connectionSource, ResMessages
                    .AnnouncementUpdateEvent.Info.class);
            TableUtils.createTableIfNotExists(connectionSource, ResMessages.AnnouncementDeleteEvent.class);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            // ResAccountInfo.java
            TableUtils.dropTable(connectionSource, ResAccountInfo.ThumbnailInfo.class, true);
            TableUtils.dropTable(connectionSource, ResAccountInfo.UserDevice.class, true);
            TableUtils.dropTable(connectionSource, ResAccountInfo.UserEmail.class, true);
            TableUtils.dropTable(connectionSource, ResAccountInfo.UserTeam.class, true);
            TableUtils.dropTable(connectionSource, ResAccountInfo.class, true);
            TableUtils.dropTable(connectionSource, SelectedTeam.class, true);

            TableUtils.dropTable(connectionSource, LeftSideMenu.class, true);

            TableUtils.dropTable(connectionSource, ResMessages.Link.class, true);
            TableUtils.dropTable(connectionSource, ResMessages.CreateEvent.class, true);
            TableUtils.dropTable(connectionSource, ResMessages.PublicCreateInfo.class, true);
            TableUtils.dropTable(connectionSource, ResMessages.JoinEvent.class, true);
            TableUtils.dropTable(connectionSource, ResMessages.InviteEvent.class, true);
            TableUtils.dropTable(connectionSource, ResMessages.LeaveEvent.class, true);
            TableUtils.dropTable(connectionSource, ResMessages.AnnouncementCreateEvent.class, true);
            TableUtils.dropTable(connectionSource, ResMessages.AnnouncementCreateEvent.Info.class,
                    true);
            TableUtils.dropTable(connectionSource, ResMessages.AnnouncementUpdateEvent.class, true);
            TableUtils.dropTable(connectionSource, ResMessages.AnnouncementUpdateEvent.Info.class,
                    true);
            TableUtils.dropTable(connectionSource, ResMessages.AnnouncementDeleteEvent.class, true);


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
