package com.tosslab.jandi.app.local.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.domain.BadgeCount;
import com.tosslab.jandi.app.local.orm.domain.DownloadInfo;
import com.tosslab.jandi.app.local.orm.domain.FileDetail;
import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.local.orm.domain.LeftSideMenu;
import com.tosslab.jandi.app.local.orm.domain.PushHistory;
import com.tosslab.jandi.app.local.orm.domain.ReadyComment;
import com.tosslab.jandi.app.local.orm.domain.ReadyMessage;
import com.tosslab.jandi.app.local.orm.domain.RecentSticker;
import com.tosslab.jandi.app.local.orm.domain.SelectedTeam;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.domain.UploadedFileInfo;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.upgrade.UpgradeChecker;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.utils.JandiPreference;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 20..
 */
public class OrmDatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final int DATABASE_VERSION_ORIGIN = 1;
    private static final int DATABASE_VERSION_FOLDER = 2;
    private static final int DATABASE_VERSION_BADGE = 3;
    private static final int DATABASE_VERSION_FOLDER_MODIFY = 4;
    private static final int DATABASE_VERSION_STICKER_SEND_STATUS = 5;
    private static final int DATABASE_VERSION_ADD_TOKEN_TABLE = 6;
    private static final int DATABASE_VERSION_ADD_READY_COMMENT = 7;
    private static final int DATABASE_VERSION_SHARE_ENTITY_RESET = 8;
    private static final int DATABASE_VERSION_FILE_SHARE_INFO = 9;
    private static final int DATABASE_VERSION_DOWNLOAD_INFO = 10;
    private static final int DATABASE_VERSION_ADD_INTEGRATION = 11;
    private static final int DATABASE_VERSION_MODIFY_DATE_TYPE = 12;
    private static final int DATABASE_VERSION_PUSH_TOKEN = 13;
    private static final int DATABASE_VERSION_MESSAGE_DIRTY = 14;
    private static final int DATABASE_VERSION = DATABASE_VERSION_MESSAGE_DIRTY;
    public OrmLiteSqliteOpenHelper helper;

    public OrmDatabaseHelper(Context context) {
        super(context, "jandi-v2.db", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            // ResAccountInfo.java
            createTable(connectionSource, ResAccountInfo.class);
            createTable(connectionSource, ResAccountInfo.class);
            createTable(connectionSource, ResAccountInfo.ThumbnailInfo.class);
            createTable(connectionSource, ResAccountInfo.UserDevice.class);
            createTable(connectionSource, ResAccountInfo.UserEmail.class);
            createTable(connectionSource, ResAccountInfo.UserTeam.class);
            createTable(connectionSource, SelectedTeam.class);

            createTable(connectionSource, LeftSideMenu.class);

            createTable(connectionSource, ResMessages.Link.class);
            createTable(connectionSource, ResMessages.OriginalMessage.IntegerWrapper.class);
            createTable(connectionSource, ResMessages.CreateEvent.class);
            createTable(connectionSource, ResMessages.PublicCreateInfo.class);
            createTable(connectionSource, ResMessages.PublicCreateInfo.IntegerWrapper.class);
            createTable(connectionSource, ResMessages.JoinEvent.class);
            createTable(connectionSource, ResMessages.InviteEvent.class);
            createTable(connectionSource, ResMessages.InviteEvent.IntegerWrapper.class);
            createTable(connectionSource, ResMessages.LeaveEvent.class);
            createTable(connectionSource, ResMessages.AnnouncementCreateEvent.class);
            createTable(connectionSource, ResMessages.AnnouncementCreateEvent.Info.class);
            createTable(connectionSource, ResMessages.AnnouncementUpdateEvent.class);
            createTable(connectionSource, ResMessages.AnnouncementUpdateEvent.Info.class);
            createTable(connectionSource, ResMessages.AnnouncementDeleteEvent.class);

            createTable(connectionSource, ResMessages.TextMessage.class);
            createTable(connectionSource, ResMessages.TextContent.class);
            createTable(connectionSource, ResMessages.ConnectInfo.class);
            createTable(connectionSource, ResMessages.LinkPreview.class);
            createTable(connectionSource, ResMessages.FileMessage.class);
            createTable(connectionSource, ResMessages.FileContent.class);
            createTable(connectionSource, ResMessages.ThumbnailUrls.class);
            createTable(connectionSource, ResMessages.StickerMessage.class);
            createTable(connectionSource, ResMessages.StickerContent.class);
            createTable(connectionSource, RecentSticker.class);

            createTable(connectionSource, ResMessages.CommentStickerMessage.class);
            createTable(connectionSource, ResMessages.CommentStickerMessage.class);
            createTable(connectionSource, ResMessages.CommentMessage.class);

            createTable(connectionSource, MentionObject.class);

            createTable(connectionSource, ResChat.class);

            createTable(connectionSource, ReadyMessage.class);
            createTable(connectionSource, ReadyComment.class);
            createTable(connectionSource, SendMessage.class);


            createTable(connectionSource, ResRoomInfo.class);
            createTable(connectionSource, ResRoomInfo.MarkerInfo.class);

            createTable(connectionSource, ResAnnouncement.class);

            createTable(connectionSource, FileDetail.class);

            createTable(connectionSource, ResFolder.class);
            createTable(connectionSource, ResFolderItem.class);
            createTable(connectionSource, FolderExpand.class);

            createTable(connectionSource, UploadedFileInfo.class);

            createTable(connectionSource, BadgeCount.class);

            createTable(connectionSource, ResAccessToken.class);

            createTable(connectionSource, DownloadInfo.class);
            createTable(connectionSource, PushToken.class);
            createTable(connectionSource, PushHistory.class);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            List<UpgradeChecker> upgradeCheckers = Arrays.asList(
                    UpgradeChecker.create(() -> DATABASE_VERSION_FOLDER, () -> {
                        createTable(connectionSource, UploadedFileInfo.class);
                        createTable(connectionSource, ResFolder.class);
                        createTable(connectionSource, ResFolderItem.class);
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_BADGE, () -> {
                        // for Parse
                        createTable(connectionSource, BadgeCount.class);
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_FOLDER_MODIFY, () -> {
                        dropTable(connectionSource, ResFolderItem.class);
                        createTable(connectionSource, ResFolderItem.class);
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_STICKER_SEND_STATUS, () -> {
                        Dao<SendMessage, ?> dao = DaoManager.createDao(connectionSource, SendMessage.class);
                        dao.executeRawNoArgs("ALTER TABLE `message_send` ADD COLUMN stickerGroupId INTEGER;");
                        dao.executeRawNoArgs("ALTER TABLE `message_send` ADD COLUMN stickerId VARCHAR;");
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_ADD_TOKEN_TABLE, () -> {

                        createTable(connectionSource, ResAccessToken.class);

                        Context context = JandiApplication.getContext();
                        String accessToken = JandiPreference.getAccessToken(context);
                        String accessTokenType = JandiPreference.getAccessTokenType(context);
                        String refreshToken = JandiPreference.getRefreshToken(context);

                        if (!TextUtils.isEmpty(accessToken)
                                && !TextUtils.isEmpty(accessTokenType)
                                && !TextUtils.isEmpty(refreshToken)) {
                            ResAccessToken resAccessToken = new ResAccessToken();
                            resAccessToken.setRefreshToken(refreshToken);
                            resAccessToken.setAccessToken(accessToken);
                            resAccessToken.setTokenType(accessTokenType);

                            Dao<ResAccessToken, ?> dao = DaoManager.createDao(connectionSource, ResAccessToken.class);
                            dao.create(resAccessToken);

                        }

                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_ADD_READY_COMMENT, () -> {
                        createTable(connectionSource, ReadyComment.class);
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_SHARE_ENTITY_RESET, () -> {
                        MessageRepository.getRepository().deleteAllLink();
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_FILE_SHARE_INFO, () -> {
                        dropTable(connectionSource, ResMessages.FileContent.class);
                        dropTable(connectionSource, ResMessages.ThumbnailUrls.class);

                        createTable(connectionSource, ResMessages.FileContent.class);
                        createTable(connectionSource, ResMessages.ThumbnailUrls.class);

                        MessageRepository.getRepository().deleteAllLink();
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_DOWNLOAD_INFO, () -> {
                        createTable(connectionSource, DownloadInfo.class);
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_ADD_INTEGRATION, () -> {
                        Dao<ResMessages.TextContent, ?> dao = DaoManager.createDao(connectionSource, ResMessages.TextContent.class);
                        dao.executeRawNoArgs("ALTER TABLE `message_text_content` ADD COLUMN connectType VARCHAR;");
                        dao.executeRawNoArgs("ALTER TABLE `message_text_content` ADD COLUMN connectColor VARCHAR;");
                        createTable(connectionSource, ResMessages.ConnectInfo.class);
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_MODIFY_DATE_TYPE, () -> {
                        dropTable(connectionSource, RecentSticker.class);
                        createTable(connectionSource, RecentSticker.class);

                        dropTable(connectionSource, ResFolder.class);
                        createTable(connectionSource, ResFolder.class);

                        dropTable(connectionSource, UploadedFileInfo.class);
                        createTable(connectionSource, UploadedFileInfo.class);

                        MessageRepository.getRepository().deleteAllLink();
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_PUSH_TOKEN, () -> {
                        createTable(connectionSource, PushToken.class);
                        createTable(connectionSource, PushHistory.class);
                        Dao<ResAccessToken, ?> dao = DaoManager.createDao(connectionSource, ResAccessToken.class);
                        dao.executeRawNoArgs("ALTER TABLE `token` ADD COLUMN deviceId VARCHAR;");
                    }),
                    UpgradeChecker.create(() -> DATABASE_VERSION_MESSAGE_DIRTY, () -> {
                        Dao<ResMessages.Link, ?> dao = DaoManager.createDao(connectionSource, ResMessages.Link.class);
                        dao.executeRawNoArgs("ALTER TABLE `message_link` ADD COLUMN dirty SMALLINT;");
                    }));


            Observable.from(upgradeCheckers)
                    .subscribe(upgradeChecker -> upgradeChecker.run(oldVersion));

            return;
        }
    }

    private void createTable(ConnectionSource connectionSource, Class<?> dataClass) throws SQLException {
        TableUtils.createTableIfNotExists(connectionSource, dataClass);
    }

    private void dropTable(ConnectionSource connectionSource, Class<?> dataClass) throws SQLException {
        TableUtils.dropTable(connectionSource, dataClass, true);
    }

    public void clearAllData() {
        clearTable(getConnectionSource(), ResAccountInfo.ThumbnailInfo.class);
        clearTable(getConnectionSource(), ResAccountInfo.ThumbnailInfo.class);
        clearTable(getConnectionSource(), ResAccountInfo.UserDevice.class);
        clearTable(getConnectionSource(), ResAccountInfo.UserEmail.class);
        clearTable(getConnectionSource(), ResAccountInfo.UserTeam.class);
        clearTable(getConnectionSource(), ResAccountInfo.class);
        clearTable(getConnectionSource(), SelectedTeam.class);

        clearTable(getConnectionSource(), LeftSideMenu.class);

        clearTable(getConnectionSource(), ResMessages.Link.class);
        clearTable(getConnectionSource(), ResMessages.OriginalMessage.IntegerWrapper.class);
        clearTable(getConnectionSource(), ResMessages.CreateEvent.class);
        clearTable(getConnectionSource(), ResMessages.PublicCreateInfo.class);
        clearTable(getConnectionSource(), ResMessages.PublicCreateInfo.IntegerWrapper.class);
        clearTable(getConnectionSource(), ResMessages.JoinEvent.class);
        clearTable(getConnectionSource(), ResMessages.InviteEvent.class);
        clearTable(getConnectionSource(), ResMessages.InviteEvent.IntegerWrapper.class);
        clearTable(getConnectionSource(), ResMessages.LeaveEvent.class);
        clearTable(getConnectionSource(), ResMessages.AnnouncementCreateEvent.class);
        clearTable(getConnectionSource(), ResMessages.AnnouncementCreateEvent.Info.class);
        clearTable(getConnectionSource(), ResMessages.AnnouncementUpdateEvent.class);
        clearTable(getConnectionSource(), ResMessages.AnnouncementUpdateEvent.Info.class);
        clearTable(getConnectionSource(), ResMessages.AnnouncementDeleteEvent.class);

        clearTable(getConnectionSource(), ResMessages.TextMessage.class);
        clearTable(getConnectionSource(), ResMessages.TextContent.class);
        clearTable(getConnectionSource(), ResMessages.ConnectInfo.class);
        clearTable(getConnectionSource(), ResMessages.LinkPreview.class);
        clearTable(getConnectionSource(), ResMessages.FileMessage.class);
        clearTable(getConnectionSource(), ResMessages.FileContent.class);
        clearTable(getConnectionSource(), ResMessages.ThumbnailUrls.class);
        clearTable(getConnectionSource(), ResMessages.StickerMessage.class);
        clearTable(getConnectionSource(), RecentSticker.class);
        clearTable(getConnectionSource(), ResMessages.CommentStickerMessage.class);
        clearTable(getConnectionSource(), ResMessages.CommentStickerMessage.class);
        clearTable(getConnectionSource(), ResMessages.CommentMessage.class);

        clearTable(getConnectionSource(), ResChat.class);

        clearTable(getConnectionSource(), ResRoomInfo.class);
        clearTable(getConnectionSource(), ResRoomInfo.MarkerInfo.class);

        clearTable(getConnectionSource(), ResAnnouncement.class);

        clearTable(getConnectionSource(), FileDetail.class);

        clearTable(getConnectionSource(), ResFolder.class);
        clearTable(getConnectionSource(), ResFolderItem.class);
        clearTable(getConnectionSource(), UploadedFileInfo.class);


        clearTable(getConnectionSource(), ResFolder.class);
        clearTable(getConnectionSource(), ResFolderItem.class);
        clearTable(getConnectionSource(), FolderExpand.class);

        // for parse
        clearTable(getConnectionSource(), BadgeCount.class);

        clearTable(getConnectionSource(), ResAccessToken.class);
        clearTable(getConnectionSource(), PushToken.class);
        clearTable(getConnectionSource(), PushHistory.class);
    }

    private void clearTable(ConnectionSource connectionSource, Class<?> dataClass) {
        try {
            TableUtils.clearTable(connectionSource, dataClass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
