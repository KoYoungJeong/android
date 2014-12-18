package com.tosslab.jandi.app.local.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.tosslab.jandi.app.network.models.ResAccountInfo;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiDatabaseManager {

    private static JandiDatabaseManager instance;

    private JandiDatabaseOpenHelper jandiDatabaseOpenHelper;

    private JandiDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = new JandiDatabaseOpenHelper(context);
    }

    public static JandiDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new JandiDatabaseManager(context);
        }
        return instance;
    }

    SQLiteDatabase getWriteableDatabase() {
        return jandiDatabaseOpenHelper.getWritableDatabase();

    }

    SQLiteDatabase getReadableDatabase() {
        return jandiDatabaseOpenHelper.getReadableDatabase();
    }


    public long upsertAccountInfo(ResAccountInfo resAccountInfo) {

        return 0;
    }

    public int upsertAccountDevices(List<ResAccountInfo.UserDevice> devices) {

        return 0;
    }

    public int upsertAccountEmail(List<ResAccountInfo.UserEmail> userEmails) {

        return 0;
    }

    public int upsertAccountTeam(List<ResAccountInfo.UserTeam> userTeams) {

        return 0;
    }

}
