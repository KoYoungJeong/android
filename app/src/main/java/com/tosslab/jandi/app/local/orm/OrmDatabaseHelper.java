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
            createTable(connectionSource, ResAccountInfo.class);
            createTable(connectionSource, ResAccountInfo.class);
            createTable(connectionSource, ResAccountInfo.ThumbnailInfo.class);
            createTable(connectionSource, ResAccountInfo.UserDevice.class);
            createTable(connectionSource, ResAccountInfo.UserEmail.class);
            createTable(connectionSource, ResAccountInfo.UserTeam.class);
            createTable(connectionSource, SelectedTeam.class);

            createTable(connectionSource, LeftSideMenu.class);

            createTable(connectionSource, ResMessages.Link.class);
            createTable(connectionSource, ResMessages.OriginalMessage.IntegerWrapper.class);
            createTable(connectionSource, ResMessages.CreateEvent.class);
            createTable(connectionSource, ResMessages.PublicCreateInfo.class);
            createTable(connectionSource, ResMessages.PublicCreateInfo.IntegerWrapper.class);
            createTable(connectionSource, ResMessages.JoinEvent.class);
            createTable(connectionSource, ResMessages.InviteEvent.class);
            createTable(connectionSource, ResMessages.InviteEvent.IntegerWrapper.class);
            createTable(connectionSource, ResMessages.LeaveEvent.class);
            createTable(connectionSource, ResMessages.AnnouncementCreateEvent.class);
            createTable(connectionSource, ResMessages.AnnouncementCreateEvent.Info.class);
            createTable(connectionSource, ResMessages.AnnouncementUpdateEvent.class);
            createTable(connectionSource, ResMessages.AnnouncementUpdateEvent.Info.class);
            createTable(connectionSource, ResMessages.AnnouncementDeleteEvent.class);

            createTable(connectionSource, ResMessages.TextMessage.class);
            createTable(connectionSource, ResMessages.TextContent.class);
            createTable(connectionSource, ResMessages.LinkPreview.class);
            createTable(connectionSource, ResMessages.FileMessage.class);
            createTable(connectionSource, ResMessages.FileContent.class);
            createTable(connectionSource, ResMessages.ThumbnailUrls.class);
            createTable(connectionSource, ResMessages.StickerMessage.class);
            createTable(connectionSource, ResMessages.StickerContent.class);
            createTable(connectionSource, ResMessages.CommentStickerMessage.class);
            createTable(connectionSource, ResMessages.CommentStickerMessage.class);
            createTable(connectionSource, ResMessages.CommentMessage.class);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            // ResAccountInfo.java
            dropTable(connectionSource, ResAccountInfo.ThumbnailInfo.class);
            dropTable(connectionSource, ResAccountInfo.ThumbnailInfo.class);
            dropTable(connectionSource, ResAccountInfo.UserDevice.class);
            dropTable(connectionSource, ResAccountInfo.UserEmail.class);
            dropTable(connectionSource, ResAccountInfo.UserTeam.class);
            dropTable(connectionSource, ResAccountInfo.class);
            dropTable(connectionSource, SelectedTeam.class);

            dropTable(connectionSource, LeftSideMenu.class);

            dropTable(connectionSource, ResMessages.Link.class);
            dropTable(connectionSource, ResMessages.OriginalMessage.IntegerWrapper.class);
            dropTable(connectionSource, ResMessages.CreateEvent.class);
            dropTable(connectionSource, ResMessages.PublicCreateInfo.class);
            dropTable(connectionSource, ResMessages.PublicCreateInfo.IntegerWrapper.class);
            dropTable(connectionSource, ResMessages.JoinEvent.class);
            dropTable(connectionSource, ResMessages.InviteEvent.class);
            dropTable(connectionSource, ResMessages.InviteEvent.IntegerWrapper.class);
            dropTable(connectionSource, ResMessages.LeaveEvent.class);
            dropTable(connectionSource, ResMessages.AnnouncementCreateEvent.class);
            dropTable(connectionSource, ResMessages.AnnouncementCreateEvent.Info.class);
            dropTable(connectionSource, ResMessages.AnnouncementUpdateEvent.class);
            dropTable(connectionSource, ResMessages.AnnouncementUpdateEvent.Info.class);
            dropTable(connectionSource, ResMessages.AnnouncementDeleteEvent.class);

            dropTable(connectionSource, ResMessages.TextMessage.class);
            dropTable(connectionSource, ResMessages.TextContent.class);
            dropTable(connectionSource, ResMessages.LinkPreview.class);
            dropTable(connectionSource, ResMessages.FileMessage.class);
            dropTable(connectionSource, ResMessages.FileContent.class);
            dropTable(connectionSource, ResMessages.ThumbnailUrls.class);
            dropTable(connectionSource, ResMessages.StickerMessage.class);
            dropTable(connectionSource, ResMessages.StickerContent.class);
            dropTable(connectionSource, ResMessages.CommentStickerMessage.class);
            dropTable(connectionSource, ResMessages.CommentStickerMessage.class);
            dropTable(connectionSource, ResMessages.CommentMessage.class);


            onCreate(database, connectionSource);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable(ConnectionSource connectionSource, Class<?> dataClass) throws SQLException {
        TableUtils.createTableIfNotExists(connectionSource, dataClass);
    }

    private void dropTable(ConnectionSource connectionSource, Class<?> dataClass) throws SQLException {
        TableUtils.dropTable(connectionSource, dataClass, true);
    }
}
