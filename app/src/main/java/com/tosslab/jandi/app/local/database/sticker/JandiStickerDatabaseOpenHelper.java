package com.tosslab.jandi.app.local.database.sticker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tosslab.jandi.app.local.database.DatabaseConsts;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiStickerDatabaseOpenHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 16;
    private static final String[] CREATE_TABLES = {
            DatabaseConsts.StickerTable.sticker_items + " (" +
                    DatabaseConsts.StickerItem._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.StickerItem.id + " TEXT NOT NULL, " +
                    DatabaseConsts.StickerItem.groupId + " INTEGER NOT NULL, " +
                    DatabaseConsts.StickerItem.mobile + " TEXT, " +
                    DatabaseConsts.StickerItem.web + " TEXT NOT NULL" +
                    ");",

            DatabaseConsts.StickerTable.sticker_recent + " (" +
                    DatabaseConsts.StickerRecent._id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    DatabaseConsts.StickerRecent.id + " TEXT NOT NULL, " +
                    DatabaseConsts.StickerRecent.groupId + " INTEGER NOT NULL" +
                    ");",

    };
    private static SQLiteOpenHelper sqLiteOpenHelper;


    private JandiStickerDatabaseOpenHelper(Context context) {
        super(context, "jandi_sticker.db", null, DB_VERSION);
    }

    public static SQLiteOpenHelper getInstance(Context context) {
        if (sqLiteOpenHelper == null) {
            sqLiteOpenHelper = new JandiStickerDatabaseOpenHelper(context);
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

    }

}
