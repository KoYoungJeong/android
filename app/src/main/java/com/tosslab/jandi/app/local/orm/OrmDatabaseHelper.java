package com.tosslab.jandi.app.local.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.tosslab.jandi.app.local.orm.domain.SelectedTeam;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

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

            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.AlarmInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.Channel.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.ExtraData.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.MessageMarker.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.PrivateGroup.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.Team.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.User.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.UserThumbNailInfo.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.UserRef.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.PublicTopicRef.class);
            TableUtils.createTableIfNotExists(connectionSource, ResLeftSideMenu.PrivateTopicRef.class);

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

            TableUtils.dropTable(connectionSource, ResLeftSideMenu.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.AlarmInfo.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.Channel.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.ExtraData.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.MessageMarker.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.PrivateGroup.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.Team.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.User.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.UserThumbNailInfo.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.UserRef.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.PrivateTopicRef.class, true);
            TableUtils.dropTable(connectionSource, ResLeftSideMenu.PublicTopicRef.class, true);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
