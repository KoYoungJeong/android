package com.tosslab.jandi.app.local.database.account;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.ArrayList;
import java.util.List;

import static com.tosslab.jandi.app.local.database.DatabaseConsts.Account;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.AccountDevice;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.AccountEmail;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.AccountTeam;
import static com.tosslab.jandi.app.local.database.DatabaseConsts.Table;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
public class JandiAccountDatabaseManager {

    private static JandiAccountDatabaseManager instance;

    private SQLiteOpenHelper jandiDatabaseOpenHelper;

    private JandiAccountDatabaseManager(Context context) {
        jandiDatabaseOpenHelper = JandiDatabaseOpenHelper.getInstance(context);
    }

    public static JandiAccountDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new JandiAccountDatabaseManager(context);
        }
        return instance;
    }

    SQLiteDatabase getWriteableDatabase() {
        return jandiDatabaseOpenHelper.getWritableDatabase();

    }

    SQLiteDatabase getReadableDatabase() {
        return jandiDatabaseOpenHelper.getReadableDatabase();
    }


    public ResAccountInfo getAccountInfo() {
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(Table.account.name(), null, null, null, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }

        ResAccountInfo resAccountInfo = new ResAccountInfo();

        cursor.moveToFirst();

        int idIndex = cursor.getColumnIndex(Account.id.name());
        int nameIndex = cursor.getColumnIndex(Account.name.name());
        int tutoredIndex = cursor.getColumnIndex(Account.tutoredAt.name());
        int updatedIndex = cursor.getColumnIndex(Account.updatedAt.name());
        int createdIndex = cursor.getColumnIndex(Account.createdAt.name());
        int loggedIndex = cursor.getColumnIndex(Account.loggedAt.name());
        int activedIndex = cursor.getColumnIndex(Account.activatedAt.name());
        int notiTargetIndex = cursor.getColumnIndex(Account.notificationTarget.name());
        int photoUrlIndex = cursor.getColumnIndex(Account.photoUrl.name());
        int largePhotoUrlIndex = cursor.getColumnIndex(Account.largeThumbPhotoUrl.name());
        int mediumPhotoUrlIndex = cursor.getColumnIndex(Account.mediumThumbPhotoUrl.name());
        int smallPhotoUrlIndex = cursor.getColumnIndex(Account.smallThumbPhotoUrl.name());
        int statusIndex = cursor.getColumnIndex(Account.status.name());

        resAccountInfo.setId(cursor.getString(idIndex));
        resAccountInfo.setName(cursor.getString(nameIndex));
        resAccountInfo.setTutoredAt(cursor.getString(tutoredIndex));
        resAccountInfo.setUpdatedAt(cursor.getString(updatedIndex));
        resAccountInfo.setCreatedAt(cursor.getString(createdIndex));
        resAccountInfo.setLoggedAt(cursor.getString(loggedIndex));
        resAccountInfo.setActivatedAt(cursor.getString(activedIndex));
        resAccountInfo.setNotificationTarget(cursor.getString(notiTargetIndex));
        resAccountInfo.setStatus(cursor.getString(statusIndex));
        resAccountInfo.setPhotoUrl(cursor.getString(photoUrlIndex));

        ResMessages.ThumbnailUrls photoThumbnailUrl = new ResMessages.ThumbnailUrls();
        photoThumbnailUrl.largeThumbnailUrl = cursor.getString(largePhotoUrlIndex);
        photoThumbnailUrl.mediumThumbnailUrl = cursor.getString(mediumPhotoUrlIndex);
        photoThumbnailUrl.smallThumbnailUrl = cursor.getString(smallPhotoUrlIndex);

        resAccountInfo.setPhotoThumbnailUrl(photoThumbnailUrl);


        closeCursor(cursor);
        return resAccountInfo;
    }

    public long upsertAccountInfo(ResAccountInfo resAccountInfo) {

        // 기존 데이터 삭제
        SQLiteDatabase database = getWriteableDatabase();
        database.delete(Table.account.name(), null, null);

        ContentValues contentValue = new ContentValues();
        contentValue.put(Account.name.name(), resAccountInfo.getName());
        contentValue.put(Account.id.name(), resAccountInfo.getId());
        contentValue.put(Account.tutoredAt.name(), resAccountInfo.getTutoredAt());
        contentValue.put(Account.updatedAt.name(), resAccountInfo.getUpdatedAt());
        contentValue.put(Account.createdAt.name(), resAccountInfo.getCreatedAt());
        contentValue.put(Account.loggedAt.name(), resAccountInfo.getLoggedAt());
        contentValue.put(Account.activatedAt.name(), resAccountInfo.getActivatedAt());
        contentValue.put(Account.notificationTarget.name(), resAccountInfo.getNotificationTarget());
        contentValue.put(Account.photoUrl.name(), !TextUtils.isEmpty(resAccountInfo.getPhotoUrl()) ? resAccountInfo.getPhotoUrl() : "");

        ResMessages.ThumbnailUrls photoThumbnailUrl = resAccountInfo.getPhotoThumbnailUrl();

        if (photoThumbnailUrl != null) {
            contentValue.put(Account.largeThumbPhotoUrl.name(), !TextUtils.isEmpty(photoThumbnailUrl.largeThumbnailUrl) ? photoThumbnailUrl.largeThumbnailUrl : "");
            contentValue.put(Account.mediumThumbPhotoUrl.name(), !TextUtils.isEmpty(photoThumbnailUrl.mediumThumbnailUrl) ? photoThumbnailUrl.mediumThumbnailUrl : "");
            contentValue.put(Account.smallThumbPhotoUrl.name(), !TextUtils.isEmpty(photoThumbnailUrl.smallThumbnailUrl) ? photoThumbnailUrl.smallThumbnailUrl : "");
        } else {
            contentValue.put(Account.largeThumbPhotoUrl.name(), "");
            contentValue.put(Account.mediumThumbPhotoUrl.name(), "");
            contentValue.put(Account.smallThumbPhotoUrl.name(), "");
        }
        contentValue.put(Account.status.name(), resAccountInfo.getStatus());

        return database.insert(Table.account.name(), null, contentValue);
    }

    public int deleteAccountInfo() {
        SQLiteDatabase database = getWriteableDatabase();
        return database.delete(Table.account.name(), null, null);
    }

    public int upsertAccountDevices(List<ResAccountInfo.UserDevice> devices) {

        SQLiteDatabase database = getWriteableDatabase();
        database.delete(Table.account_device.name(), null, null);

        List<ContentValues> contentValueses = new ArrayList<ContentValues>();

        for (ResAccountInfo.UserDevice device : devices) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AccountDevice.token.name(), device.getToken());
            contentValues.put(AccountDevice.type.name(), device.getType());
            contentValues.put(AccountDevice.badgeCount.name(), device.getBadgeCount());
            contentValues.put(AccountDevice.subscribe.name(), device.isSubscribe() ? 1 : 0);

            contentValueses.add(contentValues);
        }

        database.beginTransaction();
        try {

            for (ContentValues contentValuese : contentValueses) {
                database.insert(Table.account_device.name(), null, contentValuese);
            }

            database.setTransactionSuccessful();
        } catch (SQLiteException e) {
        } finally {
            database.endTransaction();
        }

        return contentValueses.size();
    }

    public int deleteAccountDevices() {
        SQLiteDatabase database = getWriteableDatabase();
        return database.delete(Table.account_device.name(), null, null);
    }

    public List<ResAccountInfo.UserEmail> getUserEmails() {
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(Table.account_email.name(), null, null, null, null, null, null);

        List<ResAccountInfo.UserEmail> userEmails = new ArrayList<ResAccountInfo.UserEmail>();

        if (cursor == null || cursor.getCount() <= 0) {
            return userEmails;
        }

        int idIndex = cursor.getColumnIndex(AccountEmail.id.name());
        int statusIndex = cursor.getColumnIndex(AccountEmail.status.name());
        int confirmedIndex = cursor.getColumnIndex(AccountEmail.confirmedAt.name());
        int primaryIndex = cursor.getColumnIndex(AccountEmail.is_primary.name());

        while (cursor.moveToNext()) {
            ResAccountInfo.UserEmail userEmail = new ResAccountInfo.UserEmail();
            userEmail.setId(cursor.getString(idIndex));
            userEmail.setStatus(cursor.getString(statusIndex));
            userEmail.setConfirmedAt(cursor.getString(confirmedIndex));
            userEmail.setPrimary(cursor.getInt(primaryIndex) == 1);
            userEmails.add(userEmail);
        }
        closeCursor(cursor);

        return userEmails;
    }

    public int upsertAccountEmail(List<ResAccountInfo.UserEmail> userEmails) {

        SQLiteDatabase database = getWriteableDatabase();
        database.delete(Table.account_email.name(), null, null);

        List<ContentValues> contentValueses = new ArrayList<ContentValues>();


        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AccountEmail.id.name(), userEmail.getId());
            contentValues.put(AccountEmail.is_primary.name(), userEmail.isPrimary() ? 1 : 0);
            contentValues.put(AccountEmail.confirmedAt.name(), userEmail.getConfirmedAt());
            contentValues.put(AccountEmail.status.name(), userEmail.getStatus());

            contentValueses.add(contentValues);
        }

        database.beginTransaction();
        try {

            for (ContentValues contentValuese : contentValueses) {
                database.insert(Table.account_email.name(), null, contentValuese);
            }

            database.setTransactionSuccessful();
        } catch (SQLiteException e) {
        } finally {
            database.endTransaction();
        }

        return contentValueses.size();
    }

    public int deleteAccountEmails() {
        SQLiteDatabase database = getWriteableDatabase();
        return database.delete(Table.account_email.name(), null, null);
    }


    public List<ResAccountInfo.UserTeam> getUserTeams() {

        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query(Table.account_team.name(), null, null, null, null, null, null);

        List<ResAccountInfo.UserTeam> userTeams = new ArrayList<ResAccountInfo.UserTeam>();
        if (cursor == null || cursor.getCount() <= 0) {
            return userTeams;
        }

        int nameIndex = cursor.getColumnIndex(AccountTeam.name.name());
        int teamIdIndex = cursor.getColumnIndex(AccountTeam.teamId.name());
        int unreadIndex = cursor.getColumnIndex(AccountTeam.unread.name());
        int teamDomainIndex = cursor.getColumnIndex(AccountTeam.teamDomain.name());
        int memberIdIndex = cursor.getColumnIndex(AccountTeam.memberId.name());

        while (cursor.moveToNext()) {
            ResAccountInfo.UserTeam userTeam = new ResAccountInfo.UserTeam();
            userTeam.setUnread(cursor.getInt(unreadIndex));
            userTeam.setTeamId(cursor.getInt(teamIdIndex));
            userTeam.setTeamDomain(cursor.getString(teamDomainIndex));
            userTeam.setName(cursor.getString(nameIndex));
            userTeam.setMemberId(cursor.getInt(memberIdIndex));

            userTeams.add(userTeam);
        }

        return userTeams;
    }

    public int upsertAccountTeams(List<ResAccountInfo.UserTeam> userTeams) {

        SQLiteDatabase database = getWriteableDatabase();

        // Get Last Selected TeamId
        int lastSelectTeamId = getLastSelectedTeamId();

        database.delete(Table.account_team.name(), null, null);

        List<ContentValues> contentValueses = new ArrayList<ContentValues>();

        for (ResAccountInfo.UserTeam userTeam : userTeams) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AccountTeam.teamId.name(), userTeam.getTeamId());
            contentValues.put(AccountTeam.memberId.name(), userTeam.getMemberId());
            contentValues.put(AccountTeam.name.name(), userTeam.getName());
            contentValues.put(AccountTeam.teamDomain.name(), userTeam.getTeamDomain());
            contentValues.put(AccountTeam.unread.name(), userTeam.getUnread());

            contentValueses.add(contentValues);
        }

        database.beginTransaction();
        try {

            for (ContentValues contentValuese : contentValueses) {
                database.insert(Table.account_team.name(), null, contentValuese);
            }

            database.setTransactionSuccessful();
        } catch (SQLiteException e) {
        } finally {
            database.endTransaction();
        }

        updateSelectedTeam(lastSelectTeamId);

        return contentValueses.size();

    }

    private int getLastSelectedTeamId() {

        SQLiteDatabase database = getReadableDatabase();

        String[] columns = {AccountTeam.teamId.name()};
        String selection = AccountTeam.selected.name() + " = 1";
        Cursor cursor = database.query(Table.account_team.name(), columns, selection, null, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return -1;
        }

        cursor.moveToFirst();
        int lastSelectedTeamId = cursor.getInt(0);

        closeCursor(cursor);

        return lastSelectedTeamId;
    }

    public int deleteAccountTeams() {
        SQLiteDatabase database = getWriteableDatabase();
        return database.delete(Table.account_team.name(), null, null);
    }

    private void closeCursor(Cursor cursor) {

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public int updateSelectedTeam(int teamId) {
        SQLiteDatabase database = getWriteableDatabase();

        ContentValues contentValue = new ContentValues();
        contentValue.put(AccountTeam.selected.name(), 0);
        database.update(Table.account_team.name(), contentValue, null, null);

        contentValue = new ContentValues();
        contentValue.put(AccountTeam.selected.name(), 1);
        String selection = AccountTeam.teamId + " = ?";
        String[] selectionArgs = {String.valueOf(teamId)};
        return database.update(Table.account_team.name(), contentValue, selection, selectionArgs);
    }

    public ResAccountInfo.UserTeam getTeamInfo(int teamId) {
        SQLiteDatabase database = getReadableDatabase();

        String selection = AccountTeam.teamId + " = " + teamId;
        Cursor cursor = database.query(Table.account_team.name(), null, selection, null, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }

        cursor.moveToFirst();

        int nameIndex = cursor.getColumnIndex(AccountTeam.name.name());
        int memberIdIndex = cursor.getColumnIndex(AccountTeam.memberId.name());
        int teamDomainIndex = cursor.getColumnIndex(AccountTeam.teamDomain.name());
        int teamIdIndex = cursor.getColumnIndex(AccountTeam.teamId.name());
        int unreadIndex = cursor.getColumnIndex(AccountTeam.unread.name());

        ResAccountInfo.UserTeam userTeam = new ResAccountInfo.UserTeam();

        userTeam.setMemberId(cursor.getInt(memberIdIndex));
        userTeam.setName(cursor.getString(nameIndex));
        userTeam.setTeamDomain(cursor.getString(teamDomainIndex));
        userTeam.setTeamId(cursor.getInt(teamIdIndex));
        userTeam.setUnread(cursor.getInt(unreadIndex));

        closeCursor(cursor);

        return userTeam;

    }

    public ResAccountInfo.UserTeam getSelectedTeamInfo() {
        SQLiteDatabase database = getReadableDatabase();

        String selection = AccountTeam.selected + " = 1";
        Cursor cursor = database.query(Table.account_team.name(), null, selection, null, null, null, null);

        if (cursor == null || cursor.getCount() <= 0) {
            return null;
        }

        cursor.moveToFirst();

        int nameIndex = cursor.getColumnIndex(AccountTeam.name.name());
        int memberIdIndex = cursor.getColumnIndex(AccountTeam.memberId.name());
        int teamDomainIndex = cursor.getColumnIndex(AccountTeam.teamDomain.name());
        int teamIdIndex = cursor.getColumnIndex(AccountTeam.teamId.name());
        int unreadIndex = cursor.getColumnIndex(AccountTeam.unread.name());

        ResAccountInfo.UserTeam userTeam = new ResAccountInfo.UserTeam();

        userTeam.setMemberId(cursor.getInt(memberIdIndex));
        userTeam.setName(cursor.getString(nameIndex));
        userTeam.setTeamDomain(cursor.getString(teamDomainIndex));
        userTeam.setTeamId(cursor.getInt(teamIdIndex));
        userTeam.setUnread(cursor.getInt(unreadIndex));

        closeCursor(cursor);

        return userTeam;

    }

    public void clearAllData() {

        SQLiteDatabase database = getWriteableDatabase();
        Table[] tables = Table.values();

        for (Table table : tables) {
            database.delete(table.name(), null, null);
        }

    }
}
