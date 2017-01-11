package com.tosslab.jandi.lib.sprinkler.io.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.tosslab.jandi.lib.sprinkler.util.Logger;

/**
 * Created by tonyjs on 15. 7. 21..
 */
public final class SprinklerDatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = Logger.makeTag(SprinklerDatabaseHelper.class);
    public static final int QUERY_LIMIT = 500;
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "sprinkler.db";
    private static final String TABLE_NAME_TRACK = "track";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME_TRACK + "("
            + TableColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TableColumns.EVENT + " TEXT, "
            + TableColumns.IDENTIFIERS + " TEXT, "
            + TableColumns.PLATFORM + " TEXT, "
            + TableColumns.PROPERTIES + " TEXT, "
            + TableColumns.TIME + " INTEGER, "
            + TableColumns.VERSION + " TEXT );";

    private static SprinklerDatabaseHelper sInstance;
    private SQLiteDatabase database;

    private SprinklerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    public static SprinklerDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SprinklerDatabaseHelper(context);
        }
        return sInstance;
    }

    public boolean insert(String event, String identifiers, String platform, String properties, long time, String version)
            throws SQLiteException {
        Logger.d(TAG, "insert start");
        if (TextUtils.isEmpty(event)) {
            Logger.e(TAG, "Insert fail.(\'event\' must be set.");
            Logger.d(TAG, "insert end");
            return false;
        }
        ContentValues cv = new ContentValues();
        cv.put(TableColumns.EVENT, event);
        cv.put(TableColumns.IDENTIFIERS, identifiers);
        cv.put(TableColumns.PLATFORM, platform);
        cv.put(TableColumns.PROPERTIES, properties);
        cv.put(TableColumns.TIME, time);
        cv.put(TableColumns.VERSION, version);

        try {
            database.insertOrThrow(TABLE_NAME_TRACK, null, cv);
        } catch (SQLException e) {
            Logger.e(TAG, "Insert fail. SQLException has occurred.");
            Logger.print(e);
            Logger.d(TAG, "insert end");
            return false;
        }
        Logger.d(TAG, "insert end");
        return true;
    }

    public Cursor query() {
        Logger.i(TAG, "query start");
        Cursor cursor =
                database.query(TABLE_NAME_TRACK, TableColumns.COLUMNS,
                        null, null, null, null, null,
                        String.valueOf(QUERY_LIMIT));
        Logger.i(TAG, "query end");
        return cursor;
    }

    public Cursor queryForCount() {
        Logger.i(TAG, "query start");
        Cursor cursor =
                database.query(TABLE_NAME_TRACK, null, null, null, null, null, null, null);
        Logger.i(TAG, "query end");
        return cursor;
    }

    public int deleteAll() {
        int numberOfDeletedRows = 0;
        try {
            numberOfDeletedRows = database.delete(TABLE_NAME_TRACK, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numberOfDeletedRows;
    }

    public int deleteRows(int startIndex, int lastIndex) {
        int numberOfDeletedRows = 0;
        try {
            numberOfDeletedRows = database.delete(TABLE_NAME_TRACK,
                    TableColumns._ID + " >= ? AND " + TableColumns._ID + " <= ?",
                    new String[]{String.valueOf(startIndex), String.valueOf(lastIndex)});
        } catch (Exception e) {
            Logger.print(e);
        }
        return numberOfDeletedRows;
    }

    public int deleteFromBottom() {
        int numberOfDeletedRows = 0;

        try {
            numberOfDeletedRows = database.delete(TABLE_NAME_TRACK,
                    TableColumns._ID + " IN "
                            + "(SELECT " + TableColumns._ID + " FROM " + TABLE_NAME_TRACK
                            + " ORDER BY " + TableColumns._ID + " LIMIT " + QUERY_LIMIT + ")"
                    , null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return numberOfDeletedRows;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_TRACK);
        db.execSQL(CREATE_TABLE);
    }

    public interface TableColumns {
        String _ID = "_id";
        String EVENT = "ev";
        String IDENTIFIERS = "id";
        String PLATFORM = "pl";
        String PROPERTIES = "pr";
        String TIME = "time";
        String VERSION = "version";

        String[] COLUMNS = {_ID, EVENT, IDENTIFIERS, PLATFORM, PROPERTIES, TIME, VERSION};
    }
}
