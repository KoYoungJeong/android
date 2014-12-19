package com.tosslab.jandi.app.local.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiDatabaseOpenHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;

    private static final String[] CREATE_TABLES = {
            DatabaseConsts.Table.account + " (" +
                    DatabaseConsts.Account._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.Account.name + " TEXT NOT NULL, " +
                    DatabaseConsts.Account.tutoredAt + " TEXT, " +
                    DatabaseConsts.Account.updatedAt + " TEXT, " +
                    DatabaseConsts.Account.activatedAt + " TEXT, " +
                    DatabaseConsts.Account.createdAt + " TEXT, " +
                    DatabaseConsts.Account.loggedAt + " TEXT, " +
                    DatabaseConsts.Account.notificationTarget + " TEXT, " +
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

    }
}
