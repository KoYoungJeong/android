package com.tosslab.jandi.app.local.database.search;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.DatabaseConsts;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiSearchDatabaseManager {

    private static JandiSearchDatabaseManager instance;

    private SQLiteOpenHelper jandiDatabaseOpenHelper;

    private JandiSearchDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = JandiDatabaseOpenHelper.getInstance(context);
    }

    public static JandiSearchDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new JandiSearchDatabaseManager(context);
        }
        return instance;
    }

    SQLiteDatabase getWriteableDatabase() {
        return jandiDatabaseOpenHelper.getWritableDatabase();

    }

    SQLiteDatabase getReadableDatabase() {
        return jandiDatabaseOpenHelper.getReadableDatabase();
    }

    public long removeItemByKeyword(String searchKeyword) {
        SQLiteDatabase database = getWriteableDatabase();
        String where = DatabaseConsts.SearchKeyword.type + " = 0 AND " + DatabaseConsts.SearchKeyword.keyword + " = ?";
        String[] whereArgs = {searchKeyword};
        return database.delete(DatabaseConsts.Table.search_keyword.name(), where, whereArgs);
    }

    public long removeAllItems() {
        SQLiteDatabase database = getWriteableDatabase();
        return database.delete(DatabaseConsts.Table.search_keyword.name(), null, null);
    }

    public long upsertSearchKeyword(String searchKeyword) {
        if (searchKeyword == null || TextUtils.isEmpty(searchKeyword)) {
            return -1;
        }

        SQLiteDatabase database = getWriteableDatabase();

        String[] selectionArgs = {searchKeyword};
        String selection = DatabaseConsts.SearchKeyword.type + " = 0 AND " + DatabaseConsts.SearchKeyword.keyword + " = ?";

        String[] selectionColumns = {DatabaseConsts.SearchKeyword._id.name()};
        Cursor cursor = database.query(DatabaseConsts.Table.search_keyword.name(), selectionColumns, selection, selectionArgs, null, null, null);

        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                removeItemByKeyword(searchKeyword);
            }
        } finally {
            closeCursor(cursor);
        }

        ContentValues values = new ContentValues();

        values.put(DatabaseConsts.SearchKeyword.type.name(), 0);
        values.put(DatabaseConsts.SearchKeyword.keyword.name(), searchKeyword);

        return database.insert(DatabaseConsts.Table.search_keyword.name(), null, values);

    }

    public List<String> getSearchAllHistory() {
        List<String> searchKeywords = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor;
        String tableName = DatabaseConsts.Table.search_keyword.name();
        cursor = database.query(tableName, null, null, null, null, null, "_id desc");

        if (cursor == null || cursor.getCount() <= 0) {
            return searchKeywords;
        }

        int keywordIdx = cursor.getColumnIndex(DatabaseConsts.SearchKeyword.keyword.name());

        String searchedKeyword;

        while (cursor.moveToNext()) {
            searchedKeyword = cursor.getString(keywordIdx);
            searchKeywords.add(searchedKeyword);
        }

        return searchKeywords;
    }

    public List<String> getSearchKeywords(String keyword) {
        List<String> searchKeywords = new ArrayList<>();
        if (TextUtils.isEmpty(keyword)) {
            return searchKeywords;
        }

        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor;

        String tableName = DatabaseConsts.Table.search_keyword.name();
        String selection = DatabaseConsts.SearchKeyword.keyword + " LIKE ?";
        String[] selectionArgs = {String.format("%s%s%s", "%", keyword, "%")};
        cursor = database.query(tableName, null, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return searchKeywords;
        }

        int keywordIdx = cursor.getColumnIndex(DatabaseConsts.SearchKeyword.keyword.name());

        while (cursor.moveToNext()) {
            String searchedKeyword = cursor.getString(keywordIdx);
            searchKeywords.add(searchedKeyword);
        }

        closeCursor(cursor);

        return searchKeywords;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}
