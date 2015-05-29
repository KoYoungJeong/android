package com.tosslab.jandi.app.local.database.rooms.marker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.RoomsMarker;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Table;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiMarkerDatabaseManager {

    private static JandiMarkerDatabaseManager instance;

    private SQLiteOpenHelper jandiDatabaseOpenHelper;

    private final Lock lock;

    private JandiMarkerDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = JandiDatabaseOpenHelper.getInstance(context);
        lock = new ReentrantLock();
    }

    public static JandiMarkerDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new JandiMarkerDatabaseManager(context);
        }
        return instance;
    }

    SQLiteDatabase getWriteableDatabase() {
        return jandiDatabaseOpenHelper.getWritableDatabase();

    }

    SQLiteDatabase getReadableDatabase() {
        return jandiDatabaseOpenHelper.getReadableDatabase();
    }

    public int upsertMarkers(ResRoomInfo resRoomInfo) {

        lock.lock();
        try {


            SQLiteDatabase database = getWriteableDatabase();

            String where = RoomsMarker.teamId + " = ? AND " + RoomsMarker.roomId + " = ?";
            String[] whereArgs = {String.valueOf(resRoomInfo.getTeamId()), String.valueOf(resRoomInfo.getId())};
            database.delete(Table.rooms_marker.name(), where, whereArgs);

            int teamId = resRoomInfo.getTeamId();
            String type = resRoomInfo.getType();
            int roomId = resRoomInfo.getId();


            List<ResRoomInfo.MarkerInfo> markers = resRoomInfo.getMarkers();
            if (markers == null || markers.isEmpty()) {
                return 0;
            }

            List<ContentValues> contentValueses = new ArrayList<ContentValues>();

            ContentValues tempValue;
            for (ResRoomInfo.MarkerInfo markerInfo : markers) {

                if (markerInfo.getMemberId() <= 0) {
                    continue;
                }

                tempValue = new ContentValues();
                tempValue.put(RoomsMarker.teamId.name(), teamId);
                tempValue.put(RoomsMarker.type.name(), type);
                tempValue.put(RoomsMarker.roomId.name(), roomId);
                tempValue.put(RoomsMarker.memberId.name(), markerInfo.getMemberId());
                tempValue.put(RoomsMarker.lastLinkId.name(), markerInfo.getLastLinkId());

                contentValueses.add(tempValue);
            }

            int addedRow = 0;

            try {
                database.beginTransaction();

                for (ContentValues contentValuese : contentValueses) {
                    database.insert(Table.rooms_marker.name(), null, contentValuese);
                    ++addedRow;
                }

                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
            return addedRow;
        } finally {
            lock.unlock();
        }

    }

    public List<ResRoomInfo.MarkerInfo> getMarkers(int teamId, int roomId) {

        List<ResRoomInfo.MarkerInfo> markerInfos = new ArrayList<ResRoomInfo.MarkerInfo>();

        SQLiteDatabase database = getReadableDatabase();

        String[] columns = {RoomsMarker.memberId.name(), RoomsMarker.lastLinkId.name()};
        String selection = RoomsMarker.teamId + " = ? AND " + RoomsMarker.roomId + " = ?";
        String[] selectionArgs = {String.valueOf(teamId), String.valueOf(roomId)};
        Cursor cursor = database.query(Table.rooms_marker.name(), columns, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            closeCursor(cursor);
            return markerInfos;
        }

        int idxMemberId = cursor.getColumnIndex(RoomsMarker.memberId.name());
        int idxLastLinkId = cursor.getColumnIndex(RoomsMarker.lastLinkId.name());

        ResRoomInfo.MarkerInfo markerInfo;
        int lastLink;
        int memberId;
        while (cursor.moveToNext()) {

            lastLink = cursor.getInt(idxLastLinkId);
            memberId = cursor.getInt(idxMemberId);

            if (memberId <= 0) {
                continue;
            }

            markerInfo = new ResRoomInfo.MarkerInfo();

            markerInfo.setLastLinkId(lastLink);
            markerInfo.setMemberId(memberId);

            markerInfos.add(markerInfo);

        }

        closeCursor(cursor);

        return markerInfos;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public long updateMarker(int teamId, int roomId, int memberId, int lastLinkId) {


        lock.lock();

        try {
            if (teamId <= 0 || roomId <= 0 || memberId <= 0) {
                return -1;
            }

            SQLiteDatabase database = getWriteableDatabase();

            String where = RoomsMarker.teamId + " = ? AND " + RoomsMarker.roomId + " + ? AND " + RoomsMarker.memberId + " = ?";
            String[] whereArgs = {String.valueOf(teamId), String.valueOf(roomId), String.valueOf(memberId)};

            Cursor query = database.query(Table.rooms_marker.name(), null, where, whereArgs, null, null, null);

            if (query != null && query.getCount() > 0) {

                if (query.getCount() > 1) {
                    closeCursor(query);

                    database.delete(Table.rooms_marker.name(), where, whereArgs);

                    ContentValues contentValue = new ContentValues();
                    contentValue.put(RoomsMarker.teamId.name(), teamId);
                    contentValue.put(RoomsMarker.type.name(), "");   // 임시 변수
                    contentValue.put(RoomsMarker.roomId.name(), roomId);
                    contentValue.put(RoomsMarker.memberId.name(), memberId);
                    contentValue.put(RoomsMarker.lastLinkId.name(), lastLinkId);

                    return database.insert(Table.rooms_marker.name(), null, contentValue);
                } else {

                    query.moveToFirst();

                    int savedLastLinkId = query.getInt(query.getColumnIndex(RoomsMarker.lastLinkId.name()));

                    if (savedLastLinkId >= lastLinkId) {
                        closeCursor(query);
                        return 0;
                    } else {
                        closeCursor(query);

                        ContentValues contentValue = new ContentValues();
                        contentValue.put(RoomsMarker.lastLinkId.name(), lastLinkId);

                        return database.update(Table.rooms_marker.name(), contentValue, where, whereArgs);
                    }

                }


            } else {

                ContentValues contentValue = new ContentValues();
                contentValue.put(RoomsMarker.teamId.name(), teamId);
                contentValue.put(RoomsMarker.type.name(), "");   // 임시 변수
                contentValue.put(RoomsMarker.roomId.name(), roomId);
                contentValue.put(RoomsMarker.memberId.name(), memberId);
                contentValue.put(RoomsMarker.lastLinkId.name(), lastLinkId);

                return database.insert(Table.rooms_marker.name(), null, contentValue);
            }
        } finally {
            lock.unlock();

        }

    }

    public int deleteMarker(int teamId, int roomId, int memberId) {
        SQLiteDatabase database = getWriteableDatabase();
        String where = RoomsMarker.teamId + " = ? AND " + RoomsMarker.roomId + " + ? AND " + RoomsMarker.memberId + " = ?";
        String[] whereArgs = {String.valueOf(teamId), String.valueOf(roomId), String.valueOf(memberId)};
        return database.delete(Table.rooms_marker.name(), where, whereArgs);

    }
}
