package com.tosslab.jandi.app.local.database.entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tosslab.jandi.app.local.database.DatabaseConsts;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JacksonMapper;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftStarredEntity;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Table;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiEntityDatabaseManager {

    private static JandiEntityDatabaseManager instance;

    private SQLiteOpenHelper jandiDatabaseOpenHelper;
    private Lock lock;

    private JandiEntityDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = JandiDatabaseOpenHelper.getInstance(context);
        lock = new ReentrantLock();

    }

    public static JandiEntityDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new JandiEntityDatabaseManager(context);
        }
        return instance;
    }

    SQLiteDatabase getWriteableDatabase() {
        return jandiDatabaseOpenHelper.getWritableDatabase();

    }

    SQLiteDatabase getReadableDatabase() {
        return jandiDatabaseOpenHelper.getReadableDatabase();
    }

    public void upsertLeftSideMenu(ResLeftSideMenu leftSideMenu) {

        lock.lock();

        try {
            SQLiteDatabase database = getWriteableDatabase();


            if (leftSideMenu != null) {
                int teamId = leftSideMenu.team.id;
                database.delete(Table.left_whole.name(), DatabaseConsts.LeftWhole.teamId + " = ?", new String[]{String.valueOf(teamId)});

                try {
                    String jsonWholeValue = JacksonMapper.getInstance().getObjectMapper().writeValueAsString(leftSideMenu);

                    ContentValues values = new ContentValues();
                    values.put(DatabaseConsts.LeftWhole.teamId.name(), teamId);
                    values.put(DatabaseConsts.LeftWhole.whole.name(), jsonWholeValue);
                    database.insert(Table.left_whole.name(), null, values);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } finally {
            lock.unlock();
        }

    }

    public boolean isStarredEntity(int teamId, int entityId) {
        SQLiteDatabase database = getReadableDatabase();
        String selection = LeftStarredEntity.teamId + " = ? and " + LeftStarredEntity.entityId + " = ?";
        String[] selectionArgs = {String.valueOf(teamId), String.valueOf(entityId)};

        Cursor cursor = database.query(Table.left_starred_entity.name(), null, selection, selectionArgs, null, null, null);

        boolean isStarredEntity = (cursor != null && cursor.getCount() > 0);
        closeCursor(cursor);
        return isStarredEntity;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    synchronized public ResLeftSideMenu getEntityInfoAtWhole(int teamId) {
        lock.lock();

        Cursor cursor = null;
        try {
            SQLiteDatabase database = getReadableDatabase();

            String[] columns = {DatabaseConsts.LeftWhole.whole.name()};
            String selection = DatabaseConsts.LeftWhole.teamId.name() + " = ?";
            String[] selectionArgs = {String.valueOf(teamId)};
            cursor = database.query(Table.left_whole.name(), columns, selection, selectionArgs, null, null, null);

            ResLeftSideMenu resLeftSideMenu = null;

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String jsonWholeValue = cursor.getString(cursor.getColumnIndex(DatabaseConsts.LeftWhole.whole.name()));

                try {
                    resLeftSideMenu = JacksonMapper.getInstance().getObjectMapper().readValue(jsonWholeValue, ResLeftSideMenu.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return resLeftSideMenu;
        } finally {
            closeCursor(cursor);
            lock.unlock();

        }
    }

}
