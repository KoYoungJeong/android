package com.tosslab.jandi.app.local.orm.repositories;

import android.content.res.AssetManager;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.RecentSticker;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
public class StickerRepository {

    public static final int DEFAULT_GROUP_ID_MOZZI = 100;
    public static final int DEFAULT_GROUP_ID_DAY = 101;
    public static final int DEFAULT_MOZZI_COUNT = 26;
    private static StickerRepository repository;
    private final OrmDatabaseHelper helper;

    private StickerRepository() {
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        prepareStickerContent();
    }

    public static StickerRepository getRepository() {
        if (repository == null) {
            repository = new StickerRepository();
        }
        return repository;
    }

    /**
     * It's for Only TestCode.
     */
    public static void release() {
        repository = null;
    }

    private void prepareStickerContent() {
        try {
            Dao<ResMessages.StickerContent, ?> dao = helper.getDao(ResMessages.StickerContent.class);

            AssetManager assetManager = JandiApplication.getContext().getAssets();

            // mozzi
            String[] mozziList = assetManager.list("stickers/default/mozzi");
            addStickerConetentIfNeed(dao, mozziList, DEFAULT_GROUP_ID_MOZZI);

            // day
            String[] dayList = assetManager.list("stickers/default/day");
            addStickerConetentIfNeed(dao, dayList, DEFAULT_GROUP_ID_DAY);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addStickerConetentIfNeed(Dao<ResMessages.StickerContent, ?> dao, String[] stickerAssetList, int stickerGroupId)
            throws SQLException {
        List<ResMessages.StickerContent> savedStickerList = dao.queryBuilder()
                .where()
                .eq("groupId", stickerGroupId)
                .query();
        if (savedStickerList.size() == stickerAssetList.length) {
            return;
        }

        Observable.from(stickerAssetList)
                .map(file -> {
                    ResMessages.StickerContent stickerContent = new ResMessages.StickerContent();
                    String[] split = file.split("\\.")[0].split("_");
                    stickerContent.groupId = Integer.parseInt(split[0]);
                    stickerContent.stickerId = split[1];
                    return stickerContent;
                })
                .subscribe(stickerContent1 -> {
                    try {
                        dao.createOrUpdate(stickerContent1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);
    }

    public List<ResMessages.StickerContent> getStickers() {
        try {
            return helper.getDao(ResMessages.StickerContent.class)
                    .queryBuilder()
                    .orderBy("groupId", false)
                    .orderBy("stickerId", false)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public List<ResMessages.StickerContent> getStickers(int groupId) {
        try {
            return helper.getDao(ResMessages.StickerContent.class)
                    .queryBuilder()
                    .where()
                    .eq("groupId", groupId)
                    .query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public boolean upsertRecentSticker(ResMessages.StickerContent sticker) {

        RecentSticker recentSticker = new RecentSticker();
        recentSticker.setStickerContent(sticker);
        recentSticker.setLastDate(new Date());
        try {
            Dao<RecentSticker, ?> dao = helper.getDao(RecentSticker.class);
            dao.createOrUpdate(recentSticker);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    public boolean upsertRecentSticker(int groupId, String stickerId) {

        try {
            Dao<ResMessages.StickerContent, ?> stickerContentDao = helper.getDao(ResMessages.StickerContent.class);
            ResMessages.StickerContent stickerContent = stickerContentDao.queryBuilder()
                    .where()
                    .eq("groupId", groupId)
                    .and()
                    .eq("stickerId", stickerId)
                    .queryForFirst();

            RecentSticker recentSticker = new RecentSticker();
            recentSticker.setStickerContent(stickerContent);
            recentSticker.setLastDate(new Date());

            Dao<RecentSticker, ?> dao = helper.getDao(RecentSticker.class);
            dao.createOrUpdate(recentSticker);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }


    public List<ResMessages.StickerContent> getRecentStickers() {
        List<ResMessages.StickerContent> stickerContents = new ArrayList<>();
        try {

            Dao<RecentSticker, ?> dao = helper.getDao(RecentSticker.class);
            List<RecentSticker> recentStickers = dao.queryBuilder()
                    .orderBy("lastDate", false)
                    .query();

            Observable.from(recentStickers)
                    .map(RecentSticker::getStickerContent)
                    .collect(() -> stickerContents, List::add)
                    .subscribe();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stickerContents;
    }

}
