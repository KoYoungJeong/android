package com.tosslab.jandi.lib.sprinkler.openhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.tosslab.jandi.lib.sprinkler.domain.EventProperty;
import com.tosslab.jandi.lib.sprinkler.domain.TrackEvent;
import com.tosslab.jandi.lib.sprinkler.domain.TrackId;

import java.sql.SQLException;

/**
 * Created by Steve SeongUg Jung on 15. 7. 1..
 */
public class SprinklerOpenHelper extends OrmLiteSqliteOpenHelper {
    private static final int DB_VERSION = 1;

    public SprinklerOpenHelper(Context context) {
        super(context, "sprinkler-android.db", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, TrackId.class);
            TableUtils.createTableIfNotExists(connectionSource, EventProperty.class);
            TableUtils.createTableIfNotExists(connectionSource, TrackEvent.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, TrackId.class, true);
            TableUtils.dropTable(connectionSource, EventProperty.class, true);
            TableUtils.dropTable(connectionSource, TrackEvent.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
