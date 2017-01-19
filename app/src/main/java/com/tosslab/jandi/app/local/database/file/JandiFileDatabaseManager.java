package com.tosslab.jandi.app.local.database.file;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.json.JsonMapper;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import java.io.IOException;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.Files;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Table;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiFileDatabaseManager {

    private static JandiFileDatabaseManager instance;

    private SQLiteOpenHelper jandiDatabaseOpenHelper;

    private JandiFileDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = JandiDatabaseOpenHelper.getInstance(context);
    }

    public static JandiFileDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new JandiFileDatabaseManager(context);
        }
        return instance;
    }

    SQLiteDatabase getWriteableDatabase() {
        return jandiDatabaseOpenHelper.getWritableDatabase();

    }

    SQLiteDatabase getReadableDatabase() {
        return jandiDatabaseOpenHelper.getReadableDatabase();
    }


    void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public void upsertFiles(long teamId, ResSearchFile fileMessages) {

        SQLiteDatabase database = getWriteableDatabase();

        String where = Files.teamId + " = ?";
        String[] whereArgs = {String.valueOf(teamId)};
        database.delete(Table.files.name(), where, whereArgs);


        ObjectMapper objectMapper = JsonMapper.getInstance().getObjectMapper();
        String messagesString = null;
        try {
            messagesString = objectMapper.writeValueAsString(fileMessages);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(messagesString)) {
            return;
        }


        ContentValues contentValues = new ContentValues();
        contentValues.put(Files.teamId.name(), teamId);
        contentValues.put(Files.files.name(), messagesString);

        database.insert(Table.files.name(), null, contentValues);

    }


    public ResSearchFile getFiles(long teamId) {

        SQLiteDatabase database = getReadableDatabase();
        String selection = Files.teamId + " = ?";
        String[] selectionArgs = {String.valueOf(teamId)};
        String[] columns = {Files.files.name()};
        Cursor cursor = database.query(Table.files.name(), columns, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }

        cursor.moveToFirst();

        String messagesString = cursor.getString(0);

        closeCursor(cursor);

        ObjectMapper objectMapper = JsonMapper.getInstance().getObjectMapper();
        try {
            return objectMapper.readValue(messagesString, ResSearchFile.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

}
