package com.tosslab.jandi.lib.sprinkler.track;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.lib.sprinkler.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 15. 7. 21..
 */
public class TrackDatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = TrackDatabaseHelper.class.getSimpleName();

    interface TableColumns {
        String _ID = "_id";
        String EVENT = "ev";
        String IDENTIFIERS = "id";
        String PLATFORM = "pl";
        String PROPERTIES = "pr";
        String TIME = "time";

        String[] COLUMNS = {_ID, EVENT, IDENTIFIERS, PLATFORM, PROPERTIES, TIME};
    }

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "sprinkler.db";
    private static final String TABLE_NAME_TRACK = "track";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME_TRACK + "("
            + TableColumns._ID + " INTEGER AUTO_INCREMENT, "
            + TableColumns.EVENT + " TEXT, "
            + TableColumns.IDENTIFIERS + " TEXT, "
            + TableColumns.PLATFORM + " TEXT, "
            + TableColumns.PROPERTIES + " TEXT, "
            + TableColumns.TIME + " INTEGER );";

    private static TrackDatabaseHelper sInstance;
    private final Object sLock = new Object();
    private SQLiteDatabase database;

    private TrackDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    public static synchronized TrackDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TrackDatabaseHelper(context);
        }
        return sInstance;
    }

    public boolean insert(String event, String identifiers, String platform, String properties, long time)
            throws SQLiteException {
        synchronized (sLock) {
            Logger.d(TAG, "insert start");
            if (TextUtils.isEmpty(event)) {
                Logger.e(TAG, "Insert fail.(\'event\' must be set.");
                return false;
            }
            ContentValues cv = new ContentValues();
            cv.put(TableColumns.EVENT, event);
            cv.put(TableColumns.IDENTIFIERS, identifiers);
            cv.put(TableColumns.PLATFORM, platform);
            cv.put(TableColumns.PROPERTIES, properties);
            cv.put(TableColumns.TIME, time);

            try {
                database.insertOrThrow(TABLE_NAME_TRACK, null, cv);
            } catch (SQLException e) {
                Logger.e(TAG, "Insert fail. SQLException has occurred.");
                Logger.print(e);
                return false;
            }
            Logger.d(TAG, "insert end");
            return true;
        }
    }

    public Pair<Integer, List<Track>> query() {
        synchronized (sLock) {
            Logger.i(TAG, "query start");
            List<Track> list = new ArrayList<>();
            Cursor cursor =
                    database.query(TABLE_NAME_TRACK, TableColumns.COLUMNS, null, null, null, null, null);

            int count = cursor.getCount();
            while (cursor.moveToNext()) {
                int index = cursor.getInt(cursor.getColumnIndex(TableColumns._ID));
                String event = cursor.getString(cursor.getColumnIndex(TableColumns.EVENT));
                String identifiers = cursor.getString(cursor.getColumnIndex(TableColumns.IDENTIFIERS));
                String platform = cursor.getString(cursor.getColumnIndex(TableColumns.PLATFORM));
                String properties = cursor.getString(cursor.getColumnIndex(TableColumns.PROPERTIES));
                long time = cursor.getLong(cursor.getColumnIndex(TableColumns.TIME));

                list.add(new Track(index, event, identifiers, platform, properties, time));
            }

            cursor.close();
            Logger.i(TAG, "query end");
            return new Pair<>(count, list);
        }
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
}
