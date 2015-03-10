package com.tosslab.jandi.app.local.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tosslab.jandi.app.ui.message.to.SendingState;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.Account;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.AccountDevice;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.AccountEmail;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.AccountTeam;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Chats;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Files;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftJoinEntity;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftMessageMarkers;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftStarredEntity;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftTeam;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftTopicEntity;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftUser;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.LeftWhole;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Messages;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.SearchKeyword;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.SendingMessages;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Table;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.TempMessages;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiDatabaseOpenHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 14;
    private static final String[] CREATE_TABLES = {
            Table.account + " (" +
                    Account._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    Account.id + " TEXT NOT NULL, " +
                    Account.name + " TEXT NOT NULL, " +
                    Account.tutoredAt + " TEXT, " +
                    Account.updatedAt + " TEXT, " +
                    Account.activatedAt + " TEXT, " +
                    Account.createdAt + " TEXT, " +
                    Account.loggedAt + " TEXT, " +
                    Account.notificationTarget + " TEXT, " +
                    Account.photoUrl + " TEXT, " +
                    Account.largeThumbPhotoUrl + " TEXT, " +
                    Account.mediumThumbPhotoUrl + " TEXT, " +
                    Account.smallThumbPhotoUrl + " TEXT, " +
                    Account.status + " TEXT" +
                    ");",

            Table.account_email + " (" +
                    AccountEmail._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    AccountEmail.id + " TEXT NOT NULL, " +
                    AccountEmail.confirmedAt + " TEXT, " +
                    AccountEmail.is_primary + " INTEGER NOT NULL DEFAULT 0, " +
                    AccountEmail.status + " TEXT" +
                    ");",

            Table.account_team + " (" +
                    AccountTeam._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    AccountTeam.name + " TEXT NOT NULL, " +
                    AccountTeam.teamId + " INTEGER NOT NULL, " +
                    AccountTeam.teamDomain + " TEXT NOT NULL, " +
                    AccountTeam.unread + " INTEGER, " +
                    AccountTeam.memberId + " INTEGER," +
                    AccountTeam.selected + " INTEGER NOT NULL DEFAULT 0" +
                    ");",

            Table.account_device + " (" +
                    AccountDevice._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    AccountDevice.token + " TEXT NOT NULL, " +
                    AccountDevice.type + " TEXT, " +
                    AccountDevice.badgeCount + " TEXT, " +
                    AccountDevice.subscribe + " INTEGER" +
                    ");",

            Table.left_whole + " (" +
                    LeftWhole._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    LeftWhole.teamId + " INTEGER NOT NULL, " +
                    LeftWhole.whole + " TEXT " +
                    ");",

            Table.left_team + " (" +
                    LeftTeam._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    LeftTeam.id + " INTEGER NOT NULL, " +
                    LeftTeam.name + " TEXT, " +
                    LeftTeam.teamDomain + " TEXT, " +
                    LeftTeam.teamDefaultChannelId + " INTEGER  NOT NULL" +
                    ");",

            Table.left_user + " (" +
                    LeftUser._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    LeftUser.teamId + " INTEGER NOT NULL, " +
                    LeftUser.id + " INTEGER NOT NULL, " +
                    LeftUser.name + " TEXT, " +
                    LeftUser.email + " TEXT  NOT NULL, " +
                    LeftUser.authority + " TEXT, " +
                    LeftUser.firstName + " TEXT, " +
                    LeftUser.lastName + " TEXT, " +
                    LeftUser.photoUrl + " TEXT, " +
                    LeftUser.statusMessage + " TEXT, " +
                    LeftUser.status + " TEXT NOT NULL DEFAULT enabled, " +
                    LeftUser.nickName + " TEXT, " +
                    LeftUser.phoneNumber + " TEXT, " +
                    LeftUser.department + " TEXT, " +
                    LeftUser.position + " TEXT, " +
                    LeftUser.thumbSmall + " TEXT, " +
                    LeftUser.thumbMedium + " TEXT, " +
                    LeftUser.thumbLarge + " TEXT, " +
                    LeftUser.isMe + " INTEGER NOT NULL DEFAULT 0" +
                    ");",

            Table.left_starred_entity + " (" +
                    LeftStarredEntity._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    LeftStarredEntity.teamId + " INTEGER NOT NULL, " +
                    LeftStarredEntity.entityId + " INTEGER NOT NULL" +
                    ");",

            Table.left_message_marker + " (" +
                    LeftMessageMarkers._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    LeftMessageMarkers.teamId + " INTEGER NOT NULL, " +
                    LeftMessageMarkers.entityType + " TEXT, " +
                    LeftMessageMarkers.entityId + " INTEGER, " +
                    LeftMessageMarkers.lastLinkId + " INTEGER, " +
                    LeftMessageMarkers.alarmCount + " INTEGER " +
                    ");",

            Table.left_topic_entity + " (" +
                    LeftTopicEntity._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    LeftTopicEntity.teamId + " INTEGER NOT NULL, " +
                    LeftTopicEntity.id + " INTEGER NOT NULL, " +
                    LeftTopicEntity.type + " TEXT NOT NULL, " +
                    LeftTopicEntity.name + " TEXT, " +
                    LeftTopicEntity.creatorId + " INTEGER, " +
                    LeftTopicEntity.createdTime + " TEXT, " +
                    LeftTopicEntity.members + " TEXT " +
                    ");",

            Table.left_join_entity + " (" +
                    LeftJoinEntity._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    LeftJoinEntity.teamId + " INTEGER NOT NULL, " +
                    LeftJoinEntity.id + " INTEGER NOT NULL, " +
                    LeftJoinEntity.type + " TEXT NOT NULL " +
                    ");",

            Table.messages + " (" +
                    Messages._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    Messages.teamId + " INTEGER NOT NULL, " +
                    Messages.entityId + " INTEGER NOT NULL, " +
                    Messages.link + " TEXT NOT NULL " +
                    ");",

            Table.temp_messages + " (" +
                    TempMessages._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    TempMessages.teamId + " INTEGER NOT NULL, " +
                    TempMessages.entityId + " INTEGER NOT NULL, " +
                    TempMessages.text + " TEXT " +
                    ");",

            Table.send_messages + " (" +
                    SendingMessages._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    SendingMessages.teamId + " INTEGER NOT NULL, " +
                    SendingMessages.entityId + " INTEGER NOT NULL, " +
                    SendingMessages.content + " TEXT, " +
                    SendingMessages.state + " TEXT NOT NULL DEFAULT " + SendingState.Sending +
                    ");",

            Table.files + " (" +
                    Files._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    Files.teamId + " INTEGER NOT NULL, " +
                    Files.files + " TEXT " +
                    ");",
            Table.chats + " (" +
                    Chats._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    Chats.teamId + " INTEGER NOT NULL, " +
                    Chats.isStarred + " INTEGER NOT NULL, " +
                    Chats.lastLinkId + " INTEGER NOT NULL, " +
                    Chats.lastMessageId + " INTEGER NOT NULL, " +
                    Chats.entityId + " INTEGER NOT NULL, " +
                    Chats.unread + " INTEGER NOT NULL, " +
                    Chats.name + " TEXT ," +
                    Chats.lastMessage + " TEXT ," +
                    Chats.status + " INTEGER NOT NULL DEFAULT 1," +
                    Chats.photo + " TEXT " +
                    ");",

            Table.search_keyword + " (" +
                    SearchKeyword._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    SearchKeyword.type + " INTEGER NOT NULL, " +
                    SearchKeyword.keyword + " TEXT NOT NULL, " +
                    SearchKeyword.initSound + " TEXT " +
                    ");"


    };
    private static final Table[] EXCLUDE_TABLES = {Table.search_keyword};
    private static SQLiteOpenHelper sqLiteOpenHelper;


    private JandiDatabaseOpenHelper(Context context) {
        super(context, "jandi.db", null, DB_VERSION);
    }

    public static SQLiteOpenHelper getInstance(Context context) {
        if (sqLiteOpenHelper == null) {
            sqLiteOpenHelper = new JandiDatabaseOpenHelper(context);
        }
        return sqLiteOpenHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String createTable : CREATE_TABLES) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + createTable);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        ContentValues contentValues = getSelectedTeamValues(db);

        for (Table table : Table.values()) {
            boolean isExclude = false;
            for (Table excludeTable : EXCLUDE_TABLES) {
                if (table == excludeTable) {
                    isExclude = true;
                    break;
                }
            }

            if (!isExclude) {
                db.execSQL("DROP TABLE IF EXISTS " + table);
            }
        }

        for (String createTable : CREATE_TABLES) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + createTable);
        }

        if (contentValues != null) {
            db.insert(Table.account_team.name(), null, contentValues);
        }
    }

    private ContentValues getSelectedTeamValues(SQLiteDatabase db) {

        ContentValues contentValues = null;

        String selection = AccountTeam.selected + " = 1";
        Cursor cursor = db.query(Table.account_team.name(), null, selection, null, null, null, null);


        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                contentValues = new ContentValues();

                for (String columnName : cursor.getColumnNames()) {
                    int columnIndex = cursor.getColumnIndex(columnName);
                    int type = cursor.getType(columnIndex);

                    if (type == Cursor.FIELD_TYPE_INTEGER) {
                        contentValues.put(columnName, cursor.getInt(columnIndex));
                    } else if (type == Cursor.FIELD_TYPE_STRING) {
                        contentValues.put(columnName, cursor.getString(columnIndex));
                    }
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return contentValues;
    }
}
