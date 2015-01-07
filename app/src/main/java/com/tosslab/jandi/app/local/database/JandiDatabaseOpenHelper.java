package com.tosslab.jandi.app.local.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiDatabaseOpenHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 4;

    private static final String[] CREATE_TABLES = {
            DatabaseConsts.Table.account + " (" +
                    DatabaseConsts.Account._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.Account.id + " TEXT NOT NULL, " +
                    DatabaseConsts.Account.name + " TEXT NOT NULL, " +
                    DatabaseConsts.Account.tutoredAt + " TEXT, " +
                    DatabaseConsts.Account.updatedAt + " TEXT, " +
                    DatabaseConsts.Account.activatedAt + " TEXT, " +
                    DatabaseConsts.Account.createdAt + " TEXT, " +
                    DatabaseConsts.Account.loggedAt + " TEXT, " +
                    DatabaseConsts.Account.notificationTarget + " TEXT, " +
                    DatabaseConsts.Account.photoUrl + " TEXT, " +
                    DatabaseConsts.Account.largeThumbPhotoUrl + " TEXT, " +
                    DatabaseConsts.Account.mediumThumbPhotoUrl + " TEXT, " +
                    DatabaseConsts.Account.smallThumbPhotoUrl + " TEXT, " +
                    DatabaseConsts.Account.status + " TEXT" +
                    ");",

            DatabaseConsts.Table.account_email + " (" +
                    DatabaseConsts.AccountEmail._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.AccountEmail.id + " TEXT NOT NULL, " +
                    DatabaseConsts.AccountEmail.confirmedAt + " TEXT NOT NULL, " +
                    DatabaseConsts.AccountEmail.is_primary + " INTEGER NOT NULL DEFAULT 0, " +
                    DatabaseConsts.AccountEmail.status + " TEXT" +
                    ");",

            DatabaseConsts.Table.account_team + " (" +
                    DatabaseConsts.AccountTeam._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.AccountTeam.name + " TEXT NOT NULL, " +
                    DatabaseConsts.AccountTeam.teamId + " INTEGER NOT NULL, " +
                    DatabaseConsts.AccountTeam.teamDomain + " TEXT NOT NULL, " +
                    DatabaseConsts.AccountTeam.unread + " INTEGER, " +
                    DatabaseConsts.AccountTeam.memberId + " INTEGER," +
                    DatabaseConsts.AccountTeam.selected + " INTEGER NOT NULL DEFAULT 0" +
                    ");",

            DatabaseConsts.Table.account_device + " (" +
                    DatabaseConsts.AccountDevice._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.AccountDevice.token + " TEXT NOT NULL, " +
                    DatabaseConsts.AccountDevice.type + " TEXT, " +
                    DatabaseConsts.AccountDevice.badgeCount + " TEXT, " +
                    DatabaseConsts.AccountDevice.subscribe + " INTEGER" +
                    ");",

            DatabaseConsts.Table.left_team + " (" +
                    DatabaseConsts.LeftTeam._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.LeftTeam.id + " INTEGER NOT NULL, " +
                    DatabaseConsts.LeftTeam.name + " TEXT, " +
                    DatabaseConsts.LeftTeam.teamDomain + " TEXT, " +
                    DatabaseConsts.LeftTeam.teamDefaultChannelId + " INTEGER  NOT NULL" +
                    ");",

            DatabaseConsts.Table.left_user + " (" +
                    DatabaseConsts.LeftUser._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.LeftUser.teamId + " INTEGER NOT NULL, " +
                    DatabaseConsts.LeftUser.id + " INTEGER NOT NULL, " +
                    DatabaseConsts.LeftUser.name + " TEXT, " +
                    DatabaseConsts.LeftUser.email + " TEXT  NOT NULL, " +
                    DatabaseConsts.LeftUser.authority + " TEXT, " +
                    DatabaseConsts.LeftUser.firstName + " TEXT, " +
                    DatabaseConsts.LeftUser.lastName + " TEXT, " +
                    DatabaseConsts.LeftUser.photoUrl + " TEXT, " +
                    DatabaseConsts.LeftUser.statusMessage + " TEXT, " +
                    DatabaseConsts.LeftUser.nickName + " TEXT, " +
                    DatabaseConsts.LeftUser.phoneNumber + " TEXT, " +
                    DatabaseConsts.LeftUser.department + " TEXT, " +
                    DatabaseConsts.LeftUser.position + " TEXT, " +
                    DatabaseConsts.LeftUser.thumbSmall + " TEXT, " +
                    DatabaseConsts.LeftUser.thumbMedium + " TEXT, " +
                    DatabaseConsts.LeftUser.thumbLarge + " TEXT, " +
                    DatabaseConsts.LeftUser.isMe + " INTEGER NOT NULL DEFAULT 0" +
                    ");",

            DatabaseConsts.Table.left_starred_entity + " (" +
                    DatabaseConsts.LeftStarredEntity._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.LeftStarredEntity.teamId + " INTEGER NOT NULL, " +
                    DatabaseConsts.LeftStarredEntity.entityId + " INTEGER NOT NULL" +
                    ");",

            DatabaseConsts.Table.left_message_marker + " (" +
                    DatabaseConsts.LeftMessageMarkers._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.LeftMessageMarkers.teamId + " INTEGER NOT NULL, " +
                    DatabaseConsts.LeftMessageMarkers.entityType + " TEXT, " +
                    DatabaseConsts.LeftMessageMarkers.entityId + " INTEGER, " +
                    DatabaseConsts.LeftMessageMarkers.lastLinkId + " INTEGER, " +
                    DatabaseConsts.LeftMessageMarkers.alarmCount + " INTEGER " +
                    ");",

            DatabaseConsts.Table.left_topic_entity + " (" +
                    DatabaseConsts.LeftTopicEntity._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.LeftTopicEntity.teamId + " INTEGER NOT NULL, " +
                    DatabaseConsts.LeftTopicEntity.id + " INTEGER NOT NULL, " +
                    DatabaseConsts.LeftTopicEntity.type + " TEXT NOT NULL, " +
                    DatabaseConsts.LeftTopicEntity.name + " TEXT, " +
                    DatabaseConsts.LeftTopicEntity.creatorId + " INTEGER, " +
                    DatabaseConsts.LeftTopicEntity.createdTime + " TEXT, " +
                    DatabaseConsts.LeftTopicEntity.members + " TEXT " +
                    ");",

            DatabaseConsts.Table.left_join_entity + " (" +
                    DatabaseConsts.LeftJoinEntity._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.LeftJoinEntity.teamId + " INTEGER NOT NULL, " +
                    DatabaseConsts.LeftJoinEntity.id + " INTEGER NOT NULL, " +
                    DatabaseConsts.LeftJoinEntity.type + " TEXT NOT NULL " +
                    ");"
    };


    public JandiDatabaseOpenHelper(Context context) {
        super(context, "jandi.db", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String createTable : CREATE_TABLES) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + createTable);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (DatabaseConsts.Table table : DatabaseConsts.Table.values()) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }

        for (String createTable : CREATE_TABLES) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + createTable);
        }
    }
}
