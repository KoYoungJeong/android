package com.tosslab.jandi.app.local.database.sticker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.DatabaseConsts;
import com.tosslab.jandi.app.local.database.DatabaseConsts.StickerRecent;
import com.tosslab.jandi.app.network.models.sticker.ResSticker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.StickerItem;


/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiStickerDatabaseManager {

    private static final Map<Integer, List<ResSticker>> stickerMap = new HashMap<Integer, List<ResSticker>>();

    public static final int DEFAULT_GROUP_ID_MOZZI = 100;

    public static final int DEFAULT_MOZZI_COUNT = 26;

    static {
        stickerMap.put(DEFAULT_GROUP_ID_MOZZI, new ArrayList<ResSticker>());

        List<ResSticker> resStickers = stickerMap.get(DEFAULT_GROUP_ID_MOZZI);

        ResSticker tempSticker;
        for (int i = 1; i <= DEFAULT_MOZZI_COUNT; i++) {

            tempSticker = new ResSticker();
            tempSticker.setId(String.format("%d", i));
            tempSticker.setMobile(String.format("%03d-01", i));
            tempSticker.setWeb(String.format("%d/%03d-01.svg", DEFAULT_GROUP_ID_MOZZI, i));
            tempSticker.setGroupId(DEFAULT_GROUP_ID_MOZZI);
            resStickers.add(tempSticker);
        }

    }

    private static JandiStickerDatabaseManager instance;

    private SQLiteOpenHelper jandiDatabaseOpenHelper;


    private JandiStickerDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = JandiStickerDatabaseOpenHelper.getInstance(context);
    }

    public static JandiStickerDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new JandiStickerDatabaseManager(context);
        }
        return instance;
    }

    SQLiteDatabase getWriteableDatabase() {
        return jandiDatabaseOpenHelper.getWritableDatabase();

    }

    SQLiteDatabase getReadableDatabase() {
        return jandiDatabaseOpenHelper.getReadableDatabase();
    }

    /**
     * 서버 통신 방식으로 바뀌기전까지는 사용하지 말것
     *
     * @param stickers
     * @return
     */
    public int upsertStickers(List<ResSticker> stickers) {

        SQLiteDatabase database = getWriteableDatabase();

        String where = StickerItem.id + " = ?";
        String[] whereArgs = new String[1];
        for (ResSticker sticker : stickers) {
            whereArgs[0] = sticker.getId();
            database.delete(DatabaseConsts.StickerTable.sticker_items.name(), where, whereArgs);
        }

        database.beginTransaction();

        int total = 0;

        try {

            ContentValues values;
            for (ResSticker sticker : stickers) {

                values = new ContentValues();
                values.put(StickerItem.id.name(), sticker.getId());
                values.put(StickerItem.groupId.name(), sticker.getGroupId());
                values.put(StickerItem.mobile.name(), sticker.getMobile());
                values.put(StickerItem.web.name(), sticker.getWeb());

                database.insert(DatabaseConsts.StickerTable.sticker_items.name(), null, values);
                ++total;
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        return total;
    }

    public List<ResSticker> getStickers(int groupId) {

        List<ResSticker> resStickers = stickerMap.get(groupId);
        if (resStickers == null) {
            return new ArrayList<>();
        }

        return resStickers;
    }

    public List<ResSticker> getStickersTemp(int groupId) {
        SQLiteDatabase database = getReadableDatabase();

        List<ResSticker> resStickers = new ArrayList<ResSticker>();

        String selection = StickerItem.groupId + "= ?";
        String[] selectionArgs = {String.valueOf(groupId)};
        Cursor cursor = database.query(DatabaseConsts.StickerTable.sticker_items.name(), null, selection, selectionArgs, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return resStickers;
        }

        resStickers.addAll(convertCursorToStickers(cursor));


        return resStickers;
    }

    private List<ResSticker> convertCursorToStickers(Cursor cursor) {
        List<ResSticker> resStickers = new ArrayList<ResSticker>();
        ResSticker tempSticker;
        int idIdx = cursor.getColumnIndex(StickerItem.id.name());
        int mobileIdx = cursor.getColumnIndex(StickerItem.mobile.name());
        int webIdx = cursor.getColumnIndex(StickerItem.web.name());
        int groupIdIdx = cursor.getColumnIndex(StickerItem.groupId.name());
        while (cursor.moveToNext()) {
            tempSticker = new ResSticker();

            tempSticker.setId(cursor.getString(idIdx));
            tempSticker.setGroupId(cursor.getInt(groupIdIdx));
            tempSticker.setMobile(cursor.getString(mobileIdx));
            tempSticker.setWeb(cursor.getString(webIdx));

            resStickers.add(tempSticker);
        }

        return resStickers;
    }

    public long upsertRecentSticker(int groupId, String id) {
        SQLiteDatabase database = getWriteableDatabase();

        String where = StickerRecent.id + " = ? AND " + StickerRecent.groupId + " = ?";
        String[] whereArgs = {id, String.valueOf(groupId)};
        database.delete(DatabaseConsts.StickerTable.sticker_recent.name(), where, whereArgs);

        ContentValues values = new ContentValues();
        values.put(StickerRecent.id.name(), id);
        values.put(StickerRecent.groupId.name(), groupId);
        return database.insert(DatabaseConsts.StickerTable.sticker_recent.name(), null, values);
    }

    public List<ResSticker> getRecentStickers() {
        SQLiteDatabase database = getReadableDatabase();

        List<ResSticker> resStickers = new ArrayList<ResSticker>();

        Cursor cursor = database.query(DatabaseConsts.StickerTable.sticker_recent.name(), null, null, null, null, null, StickerRecent._id + " DESC");

        if (cursor == null || cursor.getCount() <= 0) {
            return resStickers;
        }

        resStickers.addAll(findSticker(cursor));

        return resStickers;
    }

    private List<ResSticker> findSticker(Cursor cursor) {
        List<ResSticker> resStickers = new ArrayList<ResSticker>();
        int groupIdIdx = cursor.getColumnIndex(StickerRecent.groupId.name());
        int idIdIdx = cursor.getColumnIndex(StickerRecent.id.name());

        while (cursor.moveToNext()) {

            int groupId = cursor.getInt(groupIdIdx);
            String id = cursor.getString(idIdIdx);

            List<ResSticker> tempStickers = stickerMap.get(groupId);
            for (ResSticker tempSticker : tempStickers) {
                if (TextUtils.equals(tempSticker.getId(), id)) {
                    resStickers.add(tempSticker);
                    break;
                }
            }

        }
        return resStickers;
    }

    /**
     * 서버 모드로 전환시 사용할 예정
     *
     * @return
     */
    public List<ResSticker> getRecentStickersTemp() {
        SQLiteDatabase database = getReadableDatabase();

        List<ResSticker> resStickers = new ArrayList<ResSticker>();

        StringBuffer buffer = new StringBuffer();

        buffer.append("SELECT items.id, items.groupId, items.mobile, items.web FROM sticker_recent as recent ")
                .append("LEFT JOIN sticker_items as items on recent.id = items.id ORDER BY recent._id DESC");

        Cursor cursor = database.rawQuery(buffer.toString(), null);

        resStickers.addAll(convertCursorToStickers(cursor));

        return resStickers;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }
}
