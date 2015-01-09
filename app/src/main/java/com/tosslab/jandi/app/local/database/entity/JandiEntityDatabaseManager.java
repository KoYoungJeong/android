package com.tosslab.jandi.app.local.database.entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.Account;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.AccountDevice;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.AccountEmail;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.AccountTeam;
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

    private JandiDatabaseOpenHelper jandiDatabaseOpenHelper;

    private JandiEntityDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = new JandiDatabaseOpenHelper(context);
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

        database.delete(Table.left_team.name(), LeftTeam.id + " = ?", new String[]{String.valueOf(teamId)});

        database.delete(Table.left_user.name(), LeftUser.id + " = ? and " + LeftUser.teamId + " = ?", new String[]{String.valueOf(memberId), String.valueOf(leftSideMenu.user.teamId)});

        database.delete(Table.left_message_marker.name(), LeftMessageMarkers.teamId + " = ?", new String[]{String.valueOf(teamId)});

        database.delete(Table.left_starred_entity.name(), LeftStarredEntity.teamId + " = ?", new String[]{String.valueOf(teamId)});

        database.delete(Table.left_topic_entity.name(), LeftTopicEntity.teamId + " = ?", new String[]{String.valueOf(teamId)});

        database.delete(Table.left_join_entity.name(), LeftJoinEntity.teamId + " = ?", new String[]{String.valueOf(teamId)});


        if (leftSideMenu.team != null) {

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

            ContentValues values;

            database.beginTransaction();
            try {

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

    public void clearAllData() {

        SQLiteDatabase database = getWriteableDatabase();
        Table[] tables = Table.values();

        for (Table table : tables) {
            database.delete(table.name(), null, null);
        }

    }
}