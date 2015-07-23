package com.tosslab.jandi.app.local.database.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;
import com.tosslab.jandi.app.ui.message.to.SendingState;

import java.util.ArrayList;
import java.util.List;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.SendingMessages;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Table;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiMessageDatabaseManager {

    private static JandiMessageDatabaseManager instance;

    private SQLiteOpenHelper jandiDatabaseOpenHelper;

    private JandiMessageDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = JandiDatabaseOpenHelper.getInstance(context);
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

    void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public long insertSendMessage(int teamId, int entityId, String message) {

        SQLiteDatabase database = getWriteableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(SendingMessages.teamId.name(), teamId);
        contentValues.put(SendingMessages.entityId.name(), entityId);
        contentValues.put(SendingMessages.content.name(), message);
        contentValues.put(SendingMessages.state.name(), SendingState.Sending.name());

        return database.insert(Table.send_messages.name(), null, contentValues);
    }

    public List<ResMessages.Link> getSendMessage(int teamId, int entityId) {
        SQLiteDatabase database = getReadableDatabase();

        String selection = SendingMessages.teamId + " = ? AND " + SendingMessages.entityId + " = ? AND " + SendingMessages.state + " != ?";
        String[] selectionArgs = {String.valueOf(teamId), String.valueOf(entityId), SendingState.Complete.name()};

        Cursor cursor = database.query(Table.send_messages.name(), null, selection, selectionArgs, null, null, null);

        List<ResMessages.Link> dummyList = new ArrayList<ResMessages.Link>();
        try {

            if (cursor == null || cursor.getCount() <= 0) {
                return dummyList;
            }

            int localIdx = cursor.getColumnIndex(SendingMessages._id.name());
            int contentIdx = cursor.getColumnIndex(SendingMessages.content.name());
            int sendingStateIdx = cursor.getColumnIndex(SendingMessages.state.name());

            long localId;
            String content;
            SendingState sendingState;

            while (cursor.moveToNext()) {

                localId = cursor.getLong(localIdx);
                content = cursor.getString(contentIdx);
                sendingState = SendingState.valueOf(cursor.getString(sendingStateIdx));

                DummyMessageLink dummyMessageLink = new DummyMessageLink(localId, content, sendingState);
                dummyList.add(dummyMessageLink);
            }

        } finally {
            closeCursor(cursor);
        }
        return dummyList;
    }

    public int deleteSendMessage(long localId) {
        SQLiteDatabase database = getWriteableDatabase();
        String where = SendingMessages._id + " = ?";
        String[] whereArgs = {String.valueOf(localId)};
        return database.delete(Table.send_messages.name(), where, whereArgs);
    }

    public int updateSendState(long localId, SendingState state) {
        SQLiteDatabase database = getWriteableDatabase();

        ContentValues values = new ContentValues();
        values.put(SendingMessages.state.name(), state.name());
        String where = SendingMessages._id + " = ?";
        String[] whereArgs = {String.valueOf(localId)};
        return database.update(Table.send_messages.name(), values, where, whereArgs);
    }
}
