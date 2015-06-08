package com.tosslab.jandi.app.ui.sticker;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.local.database.sticker.JandiStickerDatabaseManager;
import com.tosslab.jandi.app.network.models.sticker.ResSticker;

import java.util.HashSet;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 6. 8..
 */
public class StickerManager {

    public static final int DEFAULT_GROUP_MOZZI = 100;
    private static StickerManager stickerManager;

    private HashSet<Integer> localStickerGroupIds;

    private StickerManager() {
        this.localStickerGroupIds = new HashSet<Integer>();
        localStickerGroupIds.add(DEFAULT_GROUP_MOZZI);
    }

    public static StickerManager getInstance() {
        if (stickerManager == null) {
            stickerManager = new StickerManager();
        }

        return stickerManager;
    }

    public void loadSticker(Context context, ImageView view, int groupId, String stickerId) {

        if (isLocalSticker(groupId)) {
            String stickerAssetPath = getStickerAssetPath(context, groupId, stickerId);

            Glide.with(context)
                    .load(Uri.parse(stickerAssetPath))
                    .into(view);
        } else {

        }
    }

    private boolean isLocalSticker(int groupId) {
        return localStickerGroupIds.contains(groupId);
    }

    private String getStickerAssetPath(Context context, int groupId, String stickerId) {
        List<ResSticker> stickers = JandiStickerDatabaseManager.getInstance(context).getStickers(groupId);

        ResSticker defaultSticker = new ResSticker();
        ResSticker stickerItem = Observable.from(stickers)
                .filter(resSticker -> TextUtils.equals(resSticker.getId(), stickerId))
                .firstOrDefault(defaultSticker)
                .toBlocking().first();

        if (stickerItem != defaultSticker) {

            String fileName = stickerItem.getGroupId() + "_" + stickerItem.getId();

            return "file:///android_asset/stickers/default/mozzi/" + fileName + ".png";
        } else {
            return "";
        }
    }

}
