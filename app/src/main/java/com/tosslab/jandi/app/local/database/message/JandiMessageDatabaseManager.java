package com.tosslab.jandi.app.local.database.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.Messages;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Table;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.TempMessages;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiMessageDatabaseManager {

    private static JandiMessageDatabaseManager instance;

    private JandiDatabaseOpenHelper jandiDatabaseOpenHelper;

    private JandiMessageDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = new JandiDatabaseOpenHelper(context);
    }

    public static JandiMessageDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new JandiMessageDatabaseManager(context);
        }
        return instance;
    }

    SQLiteDatabase getWriteableDatabase() {
        return jandiDatabaseOpenHelper.getWritableDatabase();

    }

    SQLiteDatabase getReadableDatabase() {
        return jandiDatabaseOpenHelper.getReadableDatabase();
    }

    public void upsertMessage(int teamId, int entityId, List<ResMessages.Link> messages) {

        SQLiteDatabase database = getWriteableDatabase();

        String where = Messages.teamId + " = ? and " + Messages.entityId + " = ?";
        String[] whereArgs = {String.valueOf(teamId), String.valueOf(entityId)};
        database.delete(Table.messages.name(), where, whereArgs);


        ObjectMapper objectMapper = new ObjectMapper();
        String messagesString = null;
        try {
            messagesString = objectMapper.writeValueAsString(messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(messagesString)) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(Messages.teamId.name(), teamId);
        contentValues.put(Messages.entityId.name(), entityId);
        contentValues.put(Messages.link.name(), messagesString);

        database.insert(Table.messages.name(), null, contentValues);

    }

    public List<ResMessages.Link> getSavedMessages(int teamId, int entityId) {
        SQLiteDatabase readableDatabase = getReadableDatabase();

        String selection = Messages.teamId + " = ? and " + Messages.entityId + " = ?";
        String[] selectionArgs = {String.valueOf(teamId), String.valueOf(entityId)};
        Cursor cursor = readableDatabase.query(Table.messages.name(), null, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            return new ArrayList<ResMessages.Link>();
        }

        cursor.moveToFirst();

        String messagesString = cursor.getString(cursor.getColumnIndex(Messages.link.name()));

        closeCursor(cursor);

        if (TextUtils.isEmpty(messagesString)) {
            return new ArrayList<ResMessages.Link>();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(messagesString, objectMapper.getTypeFactory().constructCollectionType(List.class, ResMessages.Link.class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<ResMessages.Link>();
    }

    void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public void upsertTempMessage(int teamId, int entityId, String message) {

        SQLiteDatabase database = getWriteableDatabase();

        String where = TempMessages.teamId + " = ? AND " + TempMessages.entityId + " = ?";
        String[] whereArgs = {String.valueOf(teamId), String.valueOf(entityId)};
        database.delete(Table.temp_messages.name(), where, whereArgs);

        if (TextUtils.isEmpty(message)) {
            return;
        }


        ContentValues values = new ContentValues();
        values.put(TempMessages.teamId.name(), teamId);
        values.put(TempMessages.entityId.name(), entityId);
        values.put(TempMessages.text.name(), message);
        database.insert(Table.temp_messages.name(), null, values);
    }

    public String getTempMessage(int teamId, int entityId) {

        SQLiteDatabase database = getReadableDatabase();

        String[] columns = {TempMessages.text.name()};
        String selection = TempMessages.teamId + " = ? AND " + TempMessages.entityId + " = ?";
        String[] selectionArgs = {String.valueOf(teamId), String.valueOf(entityId)};
        Cursor cursor = database.query(Table.temp_messages.name(), columns, selection, selectionArgs, null, null, null);

        try {
            if (cursor == null || cursor.getCount() <= 0) {
                return "";
            }

            cursor.moveToFirst();
            String message = cursor.getString(0);

            return message;
        } finally {
            closeCursor(cursor);
        }
    }
}
