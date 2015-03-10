package com.tosslab.jandi.app.local.database.search;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tosslab.jandi.app.local.database.DatabaseConsts;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;
import com.tosslab.jandi.app.utils.KoreanChosungUtil;

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

    public long upsertSearchKeyword(SearchKeyword searchKeyword) {

        SQLiteDatabase database = getWriteableDatabase();

        int type = searchKeyword.getType();
        String keyword = searchKeyword.getKeyword();

        String[] selectionArgs = {String.valueOf(type), keyword};

        String selection = DatabaseConsts.SearchKeyword.type + " = ? AND " + DatabaseConsts.SearchKeyword.keyword + " = ?";
        String[] selectionColumns = {DatabaseConsts.SearchKeyword._id.name()};
        Cursor cursor = database.query(DatabaseConsts.Table.search_keyword.name(), selectionColumns, selection, selectionArgs, null, null, null);
        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                return cursor.getLong(0);
            }
        } finally {
            closeCursor(cursor);
        }

        ContentValues values = new ContentValues();

        values.put(DatabaseConsts.SearchKeyword.type.name(), searchKeyword.getType());
        values.put(DatabaseConsts.SearchKeyword.keyword.name(), searchKeyword.getKeyword());
        values.put(DatabaseConsts.SearchKeyword.initSound.name(), KoreanChosungUtil.getInitSound(searchKeyword.getKeyword()));

        return database.insert(DatabaseConsts.Table.search_keyword.name(), null, values);

    }

    public List<SearchKeyword> searchKeywords(int type, String keyword) {
        List<SearchKeyword> searchKeywords = new ArrayList<SearchKeyword>();

        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor;

        String tableName = DatabaseConsts.Table.search_keyword.name();
        if (KoreanChosungUtil.hasHangul(keyword)) {

            String selection = DatabaseConsts.SearchKeyword.keyword + "\"%?%\" OR " + DatabaseConsts.SearchKeyword.initSound + "\"%?%\"";
            String[] selectionArgs = {KoreanChosungUtil.replaceChosung(keyword, "%"), KoreanChosungUtil.getInitSound(keyword)};
            cursor = database.query(tableName, null, selection, selectionArgs, null, null, null);

        } else {
            String selection = DatabaseConsts.SearchKeyword.keyword + "\"%?%\"";
            String[] selectionArgs = {keyword};
            cursor = database.query(tableName, null, selection, selectionArgs, null, null, null);

        }

        if (cursor == null || cursor.getCount() <= 0) {
            return searchKeywords;
        }

        int idIdx = cursor.getColumnIndex(DatabaseConsts.SearchKeyword._id.name());
        int keywordIdx = cursor.getColumnIndex(DatabaseConsts.SearchKeyword.keyword.name());
        int initSoundIdx = cursor.getColumnIndex(DatabaseConsts.SearchKeyword.initSound.name());

        long id;
        String searchedKeyword;
        String initSound;

        while (cursor.moveToNext()) {

            id = cursor.getLong(idIdx);
            searchedKeyword = cursor.getString(keywordIdx);
            initSound = cursor.getString(initSoundIdx);

            SearchKeyword searchKeyword = new SearchKeyword(id, type, searchedKeyword, initSound);

            searchKeywords.add(searchKeyword);
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
