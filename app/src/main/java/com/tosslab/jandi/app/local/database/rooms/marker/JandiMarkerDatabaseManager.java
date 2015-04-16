package com.tosslab.jandi.app.local.database.rooms.marker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tosslab.jandi.app.local.database.DatabaseConsts;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiMarkerDatabaseManager {

    private static JandiMarkerDatabaseManager instance;

    private SQLiteOpenHelper jandiDatabaseOpenHelper;

    private JandiMarkerDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = JandiDatabaseOpenHelper.getInstance(context);
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

        SQLiteDatabase database = getWriteableDatabase();

        String where = DatabaseConsts.RoomsMarker.teamId + " = ? AND " + DatabaseConsts.RoomsMarker.roomId + " = ?";
        String[] whereArgs = {String.valueOf(resRoomInfo.getTeamId()), String.valueOf(resRoomInfo.getId())};
        database.delete(DatabaseConsts.Table.rooms_marker.name(), where, whereArgs);

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
            tempValue.put(DatabaseConsts.RoomsMarker.teamId.name(), teamId);
            tempValue.put(DatabaseConsts.RoomsMarker.type.name(), type);
            tempValue.put(DatabaseConsts.RoomsMarker.roomId.name(), roomId);
            tempValue.put(DatabaseConsts.RoomsMarker.memberId.name(), markerInfo.getMemberId());
            tempValue.put(DatabaseConsts.RoomsMarker.lastLinkId.name(), markerInfo.getLastLinkId());

            contentValueses.add(tempValue);
        }

        int addedRow = 0;

        try {
            database.beginTransaction();

            for (ContentValues contentValuese : contentValueses) {
                database.insert(DatabaseConsts.Table.rooms_marker.name(), null, contentValuese);
                ++addedRow;
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }


        return addedRow;
    }

    public List<ResRoomInfo.MarkerInfo> getMarkers(int teamId, int roomId) {

        List<ResRoomInfo.MarkerInfo> markerInfos = new ArrayList<ResRoomInfo.MarkerInfo>();

        SQLiteDatabase database = getReadableDatabase();

        String[] columns = {DatabaseConsts.RoomsMarker.memberId.name(), DatabaseConsts.RoomsMarker.lastLinkId.name()};
        String selection = DatabaseConsts.RoomsMarker.teamId + " = ? AND " + DatabaseConsts.RoomsMarker.roomId + " = ?";
        String[] selectionArgs = {String.valueOf(teamId), String.valueOf(roomId)};
        Cursor cursor = database.query(DatabaseConsts.Table.rooms_marker.name(), columns, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            closeCursor(cursor);
            return markerInfos;
        }

        int idxMemberId = cursor.getColumnIndex(DatabaseConsts.RoomsMarker.memberId.name());
        int idxLastLinkId = cursor.getColumnIndex(DatabaseConsts.RoomsMarker.lastLinkId.name());

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
        SQLiteDatabase database = getWriteableDatabase();

        String where = DatabaseConsts.RoomsMarker.teamId + " = ? AND " + DatabaseConsts.RoomsMarker.roomId + " + ? AND " + DatabaseConsts.RoomsMarker.memberId + " = ?";
        String[] whereArgs = {String.valueOf(teamId), String.valueOf(roomId), String.valueOf(memberId)};
        database.delete(DatabaseConsts.Table.rooms_marker.name(), where, whereArgs);

        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseConsts.RoomsMarker.teamId.name(), teamId);
        contentValue.put(DatabaseConsts.RoomsMarker.type.name(), "");   // 임시 변수
        contentValue.put(DatabaseConsts.RoomsMarker.roomId.name(), roomId);
        contentValue.put(DatabaseConsts.RoomsMarker.memberId.name(), memberId);
        contentValue.put(DatabaseConsts.RoomsMarker.lastLinkId.name(), lastLinkId);

        return database.insert(DatabaseConsts.Table.rooms_marker.name(), null, contentValue);
    }

    public int deleteMarker(int teamId, int roomId, int memberId) {
        SQLiteDatabase database = getWriteableDatabase();
        String where = DatabaseConsts.RoomsMarker.teamId + " = ? AND " + DatabaseConsts.RoomsMarker.roomId + " + ? AND " + DatabaseConsts.RoomsMarker.memberId + " = ?";
        String[] whereArgs = {String.valueOf(teamId), String.valueOf(roomId), String.valueOf(memberId)};
        return database.delete(DatabaseConsts.Table.rooms_marker.name(), where, whereArgs);

    }
}
