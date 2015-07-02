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
import com.tosslab.jandi.app.utils.logger.LogUtil;

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
<<<<<<< HEAD
        LogUtil.d("JandiEntityDatabaseManager.upsertLeftSideMenu");
        SQLiteDatabase database = getWriteableDatabase();


        int teamId = leftSideMenu.team.id;
        int memberId = leftSideMenu.user.id;


        database.delete(Table.left_user.name(), LeftUser.teamId + " = ?", new String[]{String.valueOf(leftSideMenu.user.teamId)});

        database.delete(Table.left_topic_entity.name(), LeftTopicEntity.teamId + " = ?", new String[]{String.valueOf(teamId)});

        if (leftSideMenu != null) {
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


        if (leftSideMenu.team != null) {

            database.delete(Table.left_team.name(), LeftTeam.id + " = ?", new String[]{String.valueOf(teamId)});

            ContentValues values = new ContentValues();
            values.put(LeftTeam.id.name(), teamId);
            values.put(LeftTeam.teamDomain.name(), leftSideMenu.team.t_domain);
            values.put(LeftTeam.teamDefaultChannelId.name(), leftSideMenu.team.t_defaultChannelId);

            database.insert(Table.left_team.name(), null, values);
        }

        if (leftSideMenu.entities != null) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

            database.beginTransaction();
            try {
                for (ResLeftSideMenu.Entity entity : leftSideMenu.entities) {
                    ContentValues entityValue;
                    if (entity instanceof ResLeftSideMenu.User) {

                        ResLeftSideMenu.User userEntity = (ResLeftSideMenu.User) entity;
                        if (memberId != userEntity.id) {

                            if (userEntity.teamId <= 0) {
                                userEntity.teamId = teamId;
                            }

                            entityValue = getLeftUserContentValues(userEntity, false);
                            database.insert(Table.left_user.name(), null, entityValue);
                        }

                    } else {
                        entityValue = new ContentValues();

                        entityValue.put(LeftTopicEntity.teamId.name(), teamId);
                        entityValue.put(LeftTopicEntity.id.name(), entity.id);
                        entityValue.put(LeftTopicEntity.name.name(), entity.name);


                        int creatorId;
                        String createTime;
                        StringBuffer members = new StringBuffer();

                        if (entity instanceof ResLeftSideMenu.PrivateGroup) {
                            ResLeftSideMenu.PrivateGroup privateGroup = (ResLeftSideMenu.PrivateGroup) entity;
                            creatorId = privateGroup.pg_creatorId;
                            createTime = simpleDateFormat.format(privateGroup.pg_createTime);

                            int size = (privateGroup.pg_members != null) ? privateGroup.pg_members.size() : 0;
                            for (int idx = 0; idx < size; idx++) {

                                members.append(privateGroup.pg_members.get(idx));

                                if (idx < size - 1) {
                                    members.append(",");
                                }
                            }

                            entityValue.put(LeftTopicEntity.type.name(), "privateGroup");
                            entityValue.put(LeftTopicEntity.creatorId.name(), creatorId);
                            entityValue.put(LeftTopicEntity.createdTime.name(), createTime);
                            entityValue.put(LeftTopicEntity.members.name(), members.toString());
=======

        lock.lock();
>>>>>>> origin/entitymanager_thread_safe

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

<<<<<<< HEAD
                    ContentValues messageMarkerValue;
                    for (ResLeftSideMenu.MessageMarker messageMarker : leftSideMenu.user.u_messageMarkers) {

                        messageMarkerValue = new ContentValues();
                        messageMarkerValue.put(LeftMessageMarkers.teamId.name(), leftSideMenu.user.teamId);
                        messageMarkerValue.put(LeftMessageMarkers.entityType.name(), messageMarker.entityType);
                        messageMarkerValue.put(LeftMessageMarkers.entityId.name(), messageMarker.entityId);
                        messageMarkerValue.put(LeftMessageMarkers.lastLinkId.name(), messageMarker.lastLinkId);
                        messageMarkerValue.put(LeftMessageMarkers.alarmCount.name(), messageMarker.alarmCount);
                        messageMarkerValue.put(LeftMessageMarkers.announcementOpened.name(), messageMarker.announcementOpened ? 1 : 0);

                        database.insert(Table.left_message_marker.name(), null, messageMarkerValue);
                    }

                    database.setTransactionSuccessful();
                } catch (SQLiteException e) {
                } finally {
                    database.endTransaction();
                }
=======
>>>>>>> origin/entitymanager_thread_safe
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

<<<<<<< HEAD
    //FIXME
    public ResLeftSideMenu getEntityInfoAtWhole(int teamId) {
        SQLiteDatabase database = getReadableDatabase();

        String[] columns = {DatabaseConsts.LeftWhole.whole.name()};
        String selection = DatabaseConsts.LeftWhole.teamId.name() + " = ?";
        String[] selectionArgs = {String.valueOf(teamId)};
        Cursor cursor = database.query(Table.left_whole.name(), columns, selection, selectionArgs, null, null, null);

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
    }

    public ResLeftSideMenu getEntityInfoAtOrigin(int teamId) {

//        database.delete(Table.left_user.name(), LeftUser.teamId + " = ?", new String[]{String.valueOf(leftSideMenu.user.teamId)});
//
//        database.delete(Table.left_starred_entity.name(), LeftStarredEntity.teamId + " = ?", new String[]{String.valueOf(teamId)});
//
//        database.delete(Table.left_topic_entity.name(), LeftTopicEntity.teamId + " = ?", new String[]{String.valueOf(teamId)});
//
//        database.delete(Table.left_join_entity.name(), LeftJoinEntity.teamId + " = ?", new String[]{String.valueOf(teamId)});


        SQLiteDatabase database = getReadableDatabase();

        ResLeftSideMenu resLeftSideMenu = new ResLeftSideMenu();
=======
    synchronized public ResLeftSideMenu getEntityInfoAtWhole(int teamId) {
        lock.lock();
>>>>>>> origin/entitymanager_thread_safe

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

<<<<<<< HEAD
            messageMarker.entityId = markerCursor.getInt(markerCursor.getColumnIndex(LeftMessageMarkers.entityId.name()));
            messageMarker.entityType = markerCursor.getString(markerCursor.getColumnIndex(LeftMessageMarkers.entityType.name()));
            messageMarker.lastLinkId = markerCursor.getInt(markerCursor.getColumnIndex(LeftMessageMarkers.lastLinkId.name()));
            messageMarker.alarmCount = markerCursor.getInt(markerCursor.getColumnIndex(LeftMessageMarkers.alarmCount.name()));
            int announcementOpened = markerCursor.getInt(markerCursor.getColumnIndex(LeftMessageMarkers.announcementOpened.name()));
            messageMarker.announcementOpened = announcementOpened == 1;
            messageMarkers.add(messageMarker);
=======
            return resLeftSideMenu;
        } finally {
            closeCursor(cursor);
            lock.unlock();

>>>>>>> origin/entitymanager_thread_safe
        }
    }

}
