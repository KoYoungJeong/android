package com.tosslab.jandi.app.local.database.entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.tosslab.jandi.app.local.database.DatabaseConsts;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftJoinEntity;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftMessageMarkers;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftStarredEntity;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftTeam;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftTopicEntity;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftUser;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Table;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiEntityDatabaseManager {

    private static JandiEntityDatabaseManager instance;

    private SQLiteOpenHelper jandiDatabaseOpenHelper;

    private JandiEntityDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = JandiDatabaseOpenHelper.getInstance(context);
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

        SQLiteDatabase database = getWriteableDatabase();


        int teamId = leftSideMenu.team.id;
        int memberId = leftSideMenu.user.id;


        database.delete(Table.left_user.name(), LeftUser.teamId + " = ?", new String[]{String.valueOf(leftSideMenu.user.teamId)});

        database.delete(Table.left_topic_entity.name(), LeftTopicEntity.teamId + " = ?", new String[]{String.valueOf(teamId)});

        if (leftSideMenu != null) {
            database.delete(Table.left_whole.name(), DatabaseConsts.LeftWhole.teamId + " = ?", new String[]{String.valueOf(teamId)});

            try {
                String jsonWholeValue = new ObjectMapper().writeValueAsString(leftSideMenu);

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

                        } else if (entity instanceof ResLeftSideMenu.Channel) {
                            ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity;

                            creatorId = channel.ch_creatorId;
                            createTime = simpleDateFormat.format(channel.ch_createTime);

                            int size = (channel.ch_members != null) ? channel.ch_members.size() : 0;
                            for (int idx = 0; idx < size; idx++) {

                                members.append(channel.ch_members.get(idx));

                                if (idx < size - 1) {
                                    members.append(",");
                                }
                            }

                            entityValue.put(LeftTopicEntity.type.name(), "channel");
                            entityValue.put(LeftTopicEntity.creatorId.name(), creatorId);
                            entityValue.put(LeftTopicEntity.createdTime.name(), createTime);
                            entityValue.put(LeftTopicEntity.members.name(), members.toString());

                        }

                        entityValue.put(LeftTopicEntity.members.name(), entity.id);

                        database.insert(Table.left_topic_entity.name(), null, entityValue);
                    }
                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                database.endTransaction();
            }
        }

        if (leftSideMenu.joinEntities != null) {

            database.delete(Table.left_join_entity.name(), LeftJoinEntity.teamId + " = ?", new String[]{String.valueOf(teamId)});

            database.beginTransaction();
            try {

                ContentValues values;
                for (ResLeftSideMenu.Entity joinEntity : leftSideMenu.joinEntities) {
                    values = new ContentValues();

                    values.put(LeftJoinEntity.teamId.name(), teamId);
                    values.put(LeftJoinEntity.id.name(), joinEntity.id);
                    if (joinEntity instanceof ResLeftSideMenu.Channel) {

                        values.put(LeftJoinEntity.type.name(), "channel");
                    } else if (joinEntity instanceof ResLeftSideMenu.PrivateGroup) {
                        values.put(LeftJoinEntity.type.name(), "privateGroup");

                    } else if (joinEntity instanceof ResLeftSideMenu.User) {
                        values.put(LeftJoinEntity.type.name(), "user");

                    }

                    database.insert(Table.left_join_entity.name(), null, values);
                }
                database.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                database.endTransaction();
            }
        }

        if (leftSideMenu.user != null) {

            ContentValues values = getLeftUserContentValues(leftSideMenu.user, true);
            database.insert(Table.left_user.name(), null, values);


            if (leftSideMenu.user.u_starredEntities != null) {
                database.delete(Table.left_starred_entity.name(), LeftStarredEntity.teamId + " = ?", new String[]{String.valueOf(teamId)});

                database.beginTransaction();
                try {

                    ContentValues starredEntityValues;
                    for (int starredEntityId : leftSideMenu.user.u_starredEntities) {

                        starredEntityValues = new ContentValues();
                        starredEntityValues.put(LeftStarredEntity.teamId.name(), leftSideMenu.user.teamId);
                        starredEntityValues.put(LeftStarredEntity.entityId.name(), starredEntityId);
                        database.insert(Table.left_starred_entity.name(), null, starredEntityValues);
                    }

                    database.setTransactionSuccessful();
                } catch (SQLiteException e) {
                } finally {
                    database.endTransaction();
                }
            }

            if (leftSideMenu.user.u_messageMarkers != null) {
                database.delete(Table.left_message_marker.name(), LeftMessageMarkers.teamId + " = ?", new String[]{String.valueOf(teamId)});

                database.beginTransaction();
                try {

                    ContentValues messageMarkerValue;
                    for (ResLeftSideMenu.MessageMarker messageMarker : leftSideMenu.user.u_messageMarkers) {

                        messageMarkerValue = new ContentValues();
                        messageMarkerValue.put(LeftMessageMarkers.teamId.name(), leftSideMenu.user.teamId);
                        messageMarkerValue.put(LeftMessageMarkers.entityType.name(), messageMarker.entityType);
                        messageMarkerValue.put(LeftMessageMarkers.entityId.name(), messageMarker.entityId);
                        messageMarkerValue.put(LeftMessageMarkers.lastLinkId.name(), messageMarker.lastLinkId);
                        messageMarkerValue.put(LeftMessageMarkers.alarmCount.name(), messageMarker.alarmCount);

                        database.insert(Table.left_message_marker.name(), null, messageMarkerValue);
                    }

                    database.setTransactionSuccessful();
                } catch (SQLiteException e) {
                } finally {
                    database.endTransaction();
                }
            }

        }

    }

    private ContentValues getLeftUserContentValues(ResLeftSideMenu.User user, boolean isMe) {
        ContentValues values = new ContentValues();
        values.put(LeftUser.id.name(), user.id);
        values.put(LeftUser.teamId.name(), user.teamId);
        values.put(LeftUser.name.name(), user.name);
        values.put(LeftUser.email.name(), user.u_email);
        values.put(LeftUser.authority.name(), user.u_authority);
        values.put(LeftUser.photoUrl.name(), user.u_photoUrl);
        values.put(LeftUser.statusMessage.name(), user.u_statusMessage);
        values.put(LeftUser.nickName.name(), user.u_nickname);

        if (user.u_extraData != null) {
            values.put(LeftUser.phoneNumber.name(), user.u_extraData.phoneNumber);
            values.put(LeftUser.department.name(), user.u_extraData.department);
            values.put(LeftUser.position.name(), user.u_extraData.position);

        }

        if (user.u_photoThumbnailUrl != null) {
            values.put(LeftUser.thumbLarge.name(), user.u_photoThumbnailUrl.largeThumbnailUrl);
            values.put(LeftUser.thumbMedium.name(), user.u_photoThumbnailUrl.mediumThumbnailUrl);
            values.put(LeftUser.thumbSmall.name(), user.u_photoThumbnailUrl.smallThumbnailUrl);
        }

        values.put(LeftUser.isMe.name(), isMe ? 1 : 0);
        return values;
    }

    public ResLeftSideMenu.User getUserEntity(int teamId, int entityId) {
        SQLiteDatabase database = getReadableDatabase();
        String selection = LeftUser.teamId + " = ? and " + LeftUser.id + " = ?";
        String[] selectionArgs = {String.valueOf(teamId), String.valueOf(entityId)};
        Cursor cursor = database.query(Table.left_user.name(), null, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }

        cursor.moveToFirst();

        ResLeftSideMenu.User user = convertUser(cursor);

        closeCursor(cursor);

        return user;
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
                resLeftSideMenu = new ObjectMapper().readValue(jsonWholeValue, ResLeftSideMenu.class);
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

        Cursor teamCursor = database.query(Table.left_team.name(), null, LeftTeam.id + " = ? ", new String[]{String.valueOf(teamId)}, null, null, null);


        if (teamCursor != null && teamCursor.getCount() > 0) {
            teamCursor.moveToFirst();
            resLeftSideMenu.team = convertTeam(teamCursor);
        }
        closeCursor(teamCursor);


        Cursor myUserCursor = database.query(Table.left_user.name(), null, LeftUser.teamId + " = ? and " + LeftUser.isMe + " = 1", new String[]{String.valueOf(teamId)}, null, null, null);

        if (myUserCursor != null && myUserCursor.getCount() > 0) {
            myUserCursor.moveToFirst();

            resLeftSideMenu.user = convertUser(myUserCursor);

        } else {
            resLeftSideMenu.user = new ResLeftSideMenu.User();
        }

        closeCursor(myUserCursor);

        Cursor markerCursor = database.query(Table.left_message_marker.name(), null, LeftMessageMarkers.teamId + " = ?", new String[]{String.valueOf(teamId)}, null, null, null);

        if (markerCursor != null && markerCursor.getCount() > 0) {

            resLeftSideMenu.user.u_messageMarkers = convertMessageMarkers(markerCursor);

        }

        closeCursor(markerCursor);

        Cursor starredCursor = database.query(Table.left_starred_entity.name(), null, LeftStarredEntity.teamId + " = ?", new String[]{String.valueOf(teamId)}, null, null, null);

        if (starredCursor != null && starredCursor.getCount() > 0) {
            resLeftSideMenu.user.u_starredEntities = convertStarred(starredCursor);
        }

        closeCursor(starredCursor);

        Cursor entityCursor = database.query(Table.left_topic_entity.name(), null, LeftStarredEntity.teamId + " = ?", new String[]{String.valueOf(teamId)}, null, null, null);

        if (entityCursor != null && entityCursor.getCount() > 0) {

        }

        return resLeftSideMenu;
    }

    private List<Integer> convertStarred(Cursor starredCursor) {
        List<Integer> starredEntities = new ArrayList<Integer>();

        while (starredCursor.moveToNext()) {
            starredEntities.add(starredCursor.getInt(starredCursor.getColumnIndex(LeftStarredEntity.entityId.name())));
        }
        return starredEntities;
    }

    private List<ResLeftSideMenu.MessageMarker> convertMessageMarkers(Cursor markerCursor) {
        List<ResLeftSideMenu.MessageMarker> messageMarkers = new ArrayList<ResLeftSideMenu.MessageMarker>();

        while (markerCursor.moveToNext()) {
            ResLeftSideMenu.MessageMarker messageMarker = new ResLeftSideMenu.MessageMarker();

            messageMarker.entityId = markerCursor.getInt(markerCursor.getColumnIndex(LeftMessageMarkers.entityId.name()));
            messageMarker.entityType = markerCursor.getString(markerCursor.getColumnIndex(LeftMessageMarkers.entityType.name()));
            messageMarker.lastLinkId = markerCursor.getInt(markerCursor.getColumnIndex(LeftMessageMarkers.lastLinkId.name()));
            messageMarker.alarmCount = markerCursor.getInt(markerCursor.getColumnIndex(LeftMessageMarkers.alarmCount.name()));

            messageMarkers.add(messageMarker);
        }
        return messageMarkers;
    }

    private ResLeftSideMenu.User convertUser(Cursor myUserCursor) {
        int idIdx = myUserCursor.getColumnIndex(LeftUser.id.name());
        int teamIdIdx = myUserCursor.getColumnIndex(LeftUser.teamId.name());
        int nameIdx = myUserCursor.getColumnIndex(LeftUser.name.name());
        int emailIdx = myUserCursor.getColumnIndex(LeftUser.email.name());
        int authorityIdx = myUserCursor.getColumnIndex(LeftUser.authority.name());
        int photoUrlIdx = myUserCursor.getColumnIndex(LeftUser.photoUrl.name());
        int statusMessageIdx = myUserCursor.getColumnIndex(LeftUser.statusMessage.name());
        int nickNameIdx = myUserCursor.getColumnIndex(LeftUser.nickName.name());
        int phoneNumberIdx = myUserCursor.getColumnIndex(LeftUser.phoneNumber.name());
        int departmentIdx = myUserCursor.getColumnIndex(LeftUser.department.name());
        int positionIdx = myUserCursor.getColumnIndex(LeftUser.position.name());
        int thumbLargeIdx = myUserCursor.getColumnIndex(LeftUser.thumbLarge.name());
        int thumbMediumIdx = myUserCursor.getColumnIndex(LeftUser.thumbMedium.name());
        int thumbSmallIdx = myUserCursor.getColumnIndex(LeftUser.thumbSmall.name());

        ResLeftSideMenu.User user = new ResLeftSideMenu.User();

        user.id = myUserCursor.getInt(idIdx);
        user.teamId = myUserCursor.getInt(teamIdIdx);
        user.name = myUserCursor.getString(nameIdx);
        user.u_email = myUserCursor.getString(emailIdx);
        user.u_authority = myUserCursor.getString(authorityIdx);
        user.u_photoUrl = myUserCursor.getString(photoUrlIdx);
        user.u_statusMessage = myUserCursor.getString(statusMessageIdx);
        user.u_nickname = myUserCursor.getString(nickNameIdx);

        user.u_extraData = new ResLeftSideMenu.ExtraData();
        user.u_extraData.phoneNumber = myUserCursor.getString(phoneNumberIdx);
        user.u_extraData.department = myUserCursor.getString(departmentIdx);
        user.u_extraData.position = myUserCursor.getString(positionIdx);

        user.u_photoThumbnailUrl = new ResMessages.ThumbnailUrls();
        user.u_photoThumbnailUrl.smallThumbnailUrl = myUserCursor.getString(thumbSmallIdx);
        user.u_photoThumbnailUrl.mediumThumbnailUrl = myUserCursor.getString(thumbMediumIdx);
        user.u_photoThumbnailUrl.largeThumbnailUrl = myUserCursor.getString(thumbLargeIdx);
        return user;
    }

    private ResLeftSideMenu.Team convertTeam(Cursor teamCursor) {
        int idIdx = teamCursor.getColumnIndex(LeftTeam.id.name());
        int nameIdx = teamCursor.getColumnIndex(LeftTeam.name.name());
        int teamDomainIdx = teamCursor.getColumnIndex(LeftTeam.teamDomain.name());
        int teamDefaultChannelIdIdx = teamCursor.getColumnIndex(LeftTeam.teamDefaultChannelId.name());

        ResLeftSideMenu.Team team = new ResLeftSideMenu.Team();
        team.id = teamCursor.getInt(idIdx);
        team.name = teamCursor.getString(nameIdx);
        team.t_domain = teamCursor.getString(teamDomainIdx);
        team.t_defaultChannelId = teamCursor.getInt(teamDefaultChannelIdIdx);
        return team;
    }
}
