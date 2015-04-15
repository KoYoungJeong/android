package com.tosslab.jandi.app.local.database.chats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;

import java.util.ArrayList;
import java.util.List;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.Chats;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Table;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiChatsDatabaseManager {

    private static JandiChatsDatabaseManager instance;

    private SQLiteOpenHelper jandiDatabaseOpenHelper;

    private JandiChatsDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = JandiDatabaseOpenHelper.getInstance(context);
    }

    public static JandiChatsDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new JandiChatsDatabaseManager(context);
        }
        return instance;
    }

    SQLiteDatabase getWriteableDatabase() {
        return jandiDatabaseOpenHelper.getWritableDatabase();

    }

    SQLiteDatabase getReadableDatabase() {
        return jandiDatabaseOpenHelper.getReadableDatabase();
    }


    public void upsertChatList(int teamId, List<ChatItem> chatItems) {

        SQLiteDatabase database = getWriteableDatabase();
        database.delete(Table.chats.name(), null, null);

        List<ContentValues> contentValueses = new ArrayList<ContentValues>();

        ContentValues tempValues;
        for (ChatItem chatItem : chatItems) {
            tempValues = new ContentValues();
            tempValues.put(Chats.name.name(), chatItem.getName());
            tempValues.put(Chats.teamId.name(), teamId);
            tempValues.put(Chats.isStarred.name(), chatItem.isStarred() ? 1 : 0);
            tempValues.put(Chats.lastMessage.name(), chatItem.getLastMessage());
            tempValues.put(Chats.lastLinkId.name(), chatItem.getLastLinkId());
            tempValues.put(Chats.lastMessageId.name(), chatItem.getLastMessageId());
            tempValues.put(Chats.unread.name(), chatItem.getUnread());
            tempValues.put(Chats.entityId.name(), chatItem.getEntityId());
            tempValues.put(Chats.roomId.name(), chatItem.getRoomId());
            tempValues.put(Chats.photo.name(), chatItem.getPhoto());
            tempValues.put(Chats.status.name(), chatItem.getStatus() ? 1 : 0);

            contentValueses.add(tempValues);
        }

        try {
            database.beginTransaction();

            for (ContentValues contentValuese : contentValueses) {
                database.insert(Table.chats.name(), null, contentValuese);
            }

            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }
    }

    public List<ChatItem> getSavedChatItems(int teamId) {

        SQLiteDatabase database = getReadableDatabase();
        String selection = Chats.teamId + " = ?";
        String[] selectionArgs = {String.valueOf(teamId)};
        Cursor cursor = database.query(Table.chats.name(), null, selection, selectionArgs, null, null, null);

        List<ChatItem> chatItems = new ArrayList<ChatItem>();

        if (cursor != null && cursor.getCount() > 0) {

            ChatItem tempItem;


            int nameIdx = cursor.getColumnIndex(Chats.name.name());
            int isStarredIdx = cursor.getColumnIndex(Chats.isStarred.name());
            int lastMessageIdIdx = cursor.getColumnIndex(Chats.lastMessageId.name());
            int unreadIdx = cursor.getColumnIndex(Chats.unread.name());
            int entityIdIdx = cursor.getColumnIndex(Chats.entityId.name());
            int roomIdIdx = cursor.getColumnIndex(Chats.roomId.name());
            int lastLinkIdIdx = cursor.getColumnIndex(Chats.lastLinkId.name());
            int lastMessageIdx = cursor.getColumnIndex(Chats.lastMessage.name());
            int photoIdx = cursor.getColumnIndex(Chats.photo.name());
            int statusIdx = cursor.getColumnIndex(Chats.status.name());

            while (cursor.moveToNext()) {

                tempItem = new ChatItem();

                tempItem.lastLinkId(cursor.getInt(lastLinkIdIdx))
                        .name(cursor.getString(nameIdx))
                        .starred(cursor.getInt(isStarredIdx) == 1)
                        .lastMessageId(cursor.getInt(lastMessageIdIdx))
                        .unread(cursor.getInt(unreadIdx))
                        .entityId(cursor.getInt(entityIdIdx))
                        .roomId(cursor.getInt(roomIdIdx))
                        .lastMessage(cursor.getString(lastMessageIdx))
                        .status(cursor.getInt(statusIdx) == 1)
                        .photo(cursor.getString(photoIdx));

                chatItems.add(tempItem);
            }
        }


        closeCursor(cursor);

        return chatItems;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

}
